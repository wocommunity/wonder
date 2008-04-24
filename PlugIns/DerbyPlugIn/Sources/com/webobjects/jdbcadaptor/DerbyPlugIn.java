package com.webobjects.jdbcadaptor;

import java.io.IOException;
import java.io.InputStream;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSPropertyListSerialization;

public class DerbyPlugIn extends JDBCPlugIn {

    private static final String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final String DRIVER_NAME = "Derby";

    static {
        setPlugInNameForSubprotocol(DerbyPlugIn.class.getName(), "derby");
    }

    public static class DerbyExpression extends JDBCExpression {
        // more to come
        public DerbyExpression(EOEntity entity) {
            super(entity);
        }
    }

    public static class DerbySynchronizationFactory extends EOSynchronizationFactory {

        public NSArray primaryKeySupportStatementsForEntityGroups(NSArray entityGroups) {
            String pkTable = ((JDBCAdaptor) adaptor()).plugIn().primaryKeyTableName();
            return new NSArray(_expressionForString("create table " + pkTable + " (name char(40) primary key, pk INT)"));
        }

        public NSArray dropPrimaryKeySupportStatementsForEntityGroups(NSArray entityGroups) {
            String pkTable = ((JDBCAdaptor) adaptor()).plugIn().primaryKeyTableName();
            return new NSArray(_expressionForString("drop table " + pkTable));
        }

        public NSArray dropTableStatementsForEntityGroup(NSArray entityGroup) {
            return new NSArray(_expressionForString("drop table " + ((EOEntity) entityGroup.objectAtIndex(0)).externalName()));
        }

        public NSArray _statementsToDropPrimaryKeyConstraintsOnTableNamed(String tableName) {
            return new NSArray(_expressionForString("alter table " + tableName + " drop primary key"));
        }

        public boolean supportsSchemaSynchronization() {
            return true;
        }

        public DerbySynchronizationFactory(EOAdaptor adaptor) {
            super(adaptor);
        }
    }

    public DerbyPlugIn(JDBCAdaptor adaptor) {
        super(adaptor);
    }

    /**
     * <P>
     * This method returns true if the connection URL for the database has
     * <code>useBundledJdbcInfo=true</code> on it which indicates to the
     * system that the jdbcInfo which has been bundled into the plugin is
     * acceptable to use in place of actually going to the database and getting
     * it.
     * 
     * @return
     */
    protected boolean shouldUseBundledJdbcInfo() {
        boolean shouldUseBundledJdbcInfo = false;
        String url = connectionURL();
        if (url != null) {
            shouldUseBundledJdbcInfo = url.toLowerCase().matches(".*(\\?|\\?.*&)useBundledJdbcInfo=(true|yes)(\\&|$)".toLowerCase());
        }
        return shouldUseBundledJdbcInfo;
    }

    /**
     * <P>
     * This is usually extracted from the the database using JDBC, but this is
     * really inconvenient for users who are trying to generate SQL at some. A
     * specific version of the data has been written into the property list of
     * the framework and this can be used as a hard-coded equivalent.
     * </P>
     */
    public NSDictionary jdbcInfo() {
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

        NSDictionary jdbcInfo;
        // have a look at the JDBC connection URL to see if the flag has been
        // set to
        // specify that the hard-coded jdbcInfo information should be used.
        if (shouldUseBundledJdbcInfo()) {
            if (NSLog.debugLoggingAllowedForLevel(NSLog.DebugLevelDetailed)) {
                NSLog.debug.appendln("Loading jdbcInfo from JDBCInfo.plist as opposed to using the JDBCPlugIn default implementation.");
            }

            InputStream jdbcInfoStream = getClass().getResourceAsStream("/JDBCInfo.plist");
            if (jdbcInfoStream == null) {
                throw new IllegalStateException("Unable to find 'JDBCInfo.plist' in this plugin jar.");
            }

            try {
                jdbcInfo = (NSDictionary) NSPropertyListSerialization.propertyListFromData(new NSData(jdbcInfoStream, 2048), "US-ASCII");
            } catch (IOException e) {
                throw new RuntimeException("Failed to load 'JDBCInfo.plist' from this plugin jar: " + e, e);
            }
        } else {
            jdbcInfo = super.jdbcInfo();
        }
        return jdbcInfo;
    }

    public String defaultDriverName() {
        return DRIVER_CLASS_NAME;
    }

    public String databaseProductName() {
        return DRIVER_NAME;
    }

    public String name() {
        return DRIVER_NAME;
    }

    public EOSynchronizationFactory createSynchronizationFactory() {
        return new DerbySynchronizationFactory(adaptor());
    }

}
