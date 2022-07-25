package com.webobjects.jdbcadaptor;

import java.io.IOException;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

import microsoft.sql.Types;

/**
 * The {@code ERMicrosoftPlugIn} is a {@code JDBCPlugIn} implementation for Microsoft SQL Server database. This class
 * extends the standard {@code MicrosoftPlugIn} to use features available on newer versions of SQL Server.
 *
 * @author hprange
 * @see JDBCPlugIn
 * @see MicrosoftPlugIn
 */
public class ERMicrosoftPlugIn extends MicrosoftPlugIn {
    private static final String QUERY_STRING_USE_BUNDLED_JDBC_INFO = "useBundledJdbcInfo";

    public ERMicrosoftPlugIn(JDBCAdaptor adaptor) {
        super(adaptor);
    }

    /**
     * The original implementation of the {@code MicrosoftPlugIn} forcibly adds the {@code SelectMethod=cursor}
     * parameter to the connection URL. Even though this option may be useful in some scenarios, it may also cause
     * undesirable side effects. Each application may have different requirements regarding how and when to use adaptive
     * buffering. For this reason, the {@code ERMicrosoftPlugin} lets the user decide how to configure adaptive
     * buffering if needed.
     * <p>
     * See Microsoft documentation for more information about
     * <a href="https://docs.microsoft.com/en-us/sql/connect/jdbc/using-adaptive-buffering">adaptive buffering</a>
     */
    @Override
    public String connectionURL() {
        return adaptor().connectionDictionaryURL();
    }

    /**
     * The SQL Server driver name has changed over time. This method returns the fully qualified name of the
     * newest JDBC driver class that this plugin prefers to use.
     */
    @Override
    public String defaultDriverName() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    /**
     * Generates a batch of new primary keys for the given entity. Overrides the default implementation to provide a
     * more efficient mechanism for generating primary keys using database sequences.
     *
     * @param count   the batch size
     * @param entity  the entity requesting primary keys
     * @param channel an open JDBCChannel
     * @return Returns an {@code NSArray} of {@code NSDictionary} where each dictionary corresponds to a unique primary
     * key value.
     */
    @Override
    public NSArray<NSDictionary<String, Object>> newPrimaryKeys(int count, EOEntity entity, JDBCChannel channel) {
        if (!isPrimaryKeyGenerationSupported(entity)) {
            return null;
        }

        JDBCContext context = (JDBCContext) channel.adaptorContext();
        Connection connection = context.connection();
        boolean hasOpenTransaction = context.hasOpenTransaction();
        String sequenceName = sequenceNameForEntity(entity);

        if (!hasOpenTransaction) {
            context.beginTransaction();
        }

        String sql = "{call sys.sp_sequence_get_range(?, ?, ?, ?)}";

        try (CallableStatement statement = connection.prepareCall(sql)) {
            statement.setString("sequence_name", sequenceName);
            statement.setLong("range_size", count);
            statement.registerOutParameter("range_first_value", Types.SQL_VARIANT);
            statement.registerOutParameter("range_last_value", Types.SQL_VARIANT);

            if (NSLog.debugLoggingAllowedForLevelAndGroups(2, 65536L)) {
                NSLog.debug.appendln(String.format("newPrimaryKeys: <%s: \"%s\" withBindings: 1:\"%s\"(sequence_name), 2:%d(range_size), 3:null(range_first_value), 4:null(range_last_value)", getClass().getName(), sql, sequenceName, count));
            }

            statement.execute();

            long firstValue = statement.getLong("range_first_value");
            long lastValue = statement.getLong("range_last_value");

            if (firstValue + count - 1 != lastValue) {
                throw new IllegalStateException(String.format("newPrimaryKeys: attempt to generate primary key(s) from sequence '%s' has failed. Cannot generate %d primary key(s) ranging from %d to %d.", sequenceName, count, firstValue, lastValue));
            }

            EOAttribute attribute = entity.primaryKeyAttributes().lastObject();
            String attributeName = attribute.name();
            boolean isIntType = "i".equals(attribute.valueType());
            NSMutableArray<NSDictionary<String, Object>> results = new NSMutableArray<>(count);

            for (int i = 0; i < count; i++) {
                Number primaryKey = firstValue + i;
                primaryKey = isIntType ? primaryKey.intValue() : primaryKey.longValue();
                results.addObject(new NSDictionary<>(primaryKey, attributeName));
            }

            return results;
        } catch (SQLException exception) {
            if (!hasOpenTransaction) {
                context.rollbackTransaction();
            }

            throw new JDBCAdaptorException(String.format("newPrimaryKeys: attempt to generate primary key(s) from sequence '%s' has failed.", sequenceName), exception);
        } finally {
            if (!hasOpenTransaction) {
                context.commitTransaction();
            }
        }
    }

    /**
     * This is usually extracted from the database using JDBC, but this is
     * really inconvenient for users who are trying to generate SQL at some. A
     * specific version of the data has been written into the property list of
     * the framework and this can be used as a hard-coded equivalent.
     *
     * @return JDBC info
     */
    @Override
    @SuppressWarnings("unchecked")
    public NSDictionary<String, Object> jdbcInfo() {
        // you can swap this code out to write the property list out in order
        // to get a fresh copy of the JDBCInfo.plist.
        // try {
        // String jdbcInfoS =
        // NSPropertyListSerialization.stringFromPropertyList(super.jdbcInfo());
        // FileOutputStream fos = new FileOutputStream("/tmp/JDBCInfo.plist");
        // fos.write(jdbcInfoS.getBytes());
        // fos.close();
        // }
        // catch(Exception e) {
        // throw new IllegalStateException("problem writing JDBCInfo.plist",e);
        // }

        NSDictionary<String, Object> jdbcInfo;
        // have a look at the JDBC connection URL to see if the flag has been
        // set to specify that the hard-coded jdbcInfo information should be used.
        if (shouldUseBundledJdbcInfo()) {
            if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed)) {
                NSLog.debug.appendln("Loading jdbcInfo from JDBCInfo.plist as opposed to using the JDBCPlugIn default implementation.");
            }

            try (InputStream jdbcInfoStream = NSBundle.bundleForClass(getClass()).inputStreamForResourcePath("JDBCInfo.plist")) {
                if (jdbcInfoStream == null) {
                    throw new IllegalStateException("Unable to find 'JDBCInfo.plist' in this plugin jar.");
                }

                jdbcInfo = (NSDictionary<String, Object>) NSPropertyListSerialization.propertyListFromData(new NSData(jdbcInfoStream, 2048), "US-ASCII");
            } catch (IOException e) {
                throw new RuntimeException("Failed to load 'JDBCInfo.plist' from this plugin jar: " + e, e);
            }
        } else {
            jdbcInfo = super.jdbcInfo();
        }

        return jdbcInfo;
    }

    /**
     * This method returns <code>true</code> if the connection URL for the
     * database has a special flag on it which indicates to the
     * system that the jdbcInfo which has been bundled into the
     * plugin is acceptable to use in place of actually going to
     * the database and getting it.
     *
     * @return <code>true</code> if bundled jdbcInfo should be used
     */
    protected boolean shouldUseBundledJdbcInfo() {
        boolean shouldUseBundledJdbcInfo = false;
        String url = connectionURL();
        if (url != null) {
            Matcher matcher = Pattern.compile(ERMicrosoftPlugIn.QUERY_STRING_USE_BUNDLED_JDBC_INFO.toLowerCase() + "=(true|yes)").matcher(url.toLowerCase());
            shouldUseBundledJdbcInfo = matcher.find();
        }
        return shouldUseBundledJdbcInfo;
    }

    /**
     * Utility method that returns the name of the sequence associated
     * with <code>entity</code>.
     *
     * @param entity the entity
     * @return the name of the sequence.
     */
    protected String sequenceNameForEntity(EOEntity entity) {
        return entity.primaryKeyRootName() + "_seq";
    }

    /**
     * Checks whether primary key generation can be supported for entity.
     *
     * @param entity the entity to be checked
     * @return Returns {@code true} if supported or {@code false} otherwise.
     */
    protected static boolean isPrimaryKeyGenerationSupported(EOEntity entity) {
        return entity.primaryKeyAttributes().count() == 1 && entity.primaryKeyAttributes().lastObject().adaptorValueType() == EOAttribute.AdaptorNumberType;
    }
}
