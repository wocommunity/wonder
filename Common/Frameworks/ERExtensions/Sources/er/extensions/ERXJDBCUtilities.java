package er.extensions;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;

public class ERXJDBCUtilities {

    public static Logger log = Logger.getLogger(ERXJDBCUtilities.class);

    public static NSTimestampFormatter TIMESTAMP_FORMATTER = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S.%F");

    public static class CopyTask {
        protected Connection source;
        protected Connection dest;
        
        protected NSMutableArray entities = new NSMutableArray();
        
        public CopyTask(EOModelGroup aModelGroup) {
            addEntitiesFromModelGroup(aModelGroup);
        }

        public CopyTask(EOModel aModel) {
            addEntitiesFromModel(aModel);
        }

        public CopyTask(EOEntity anEntity) {
            addEntity(anEntity);
        }

        public void connect(NSDictionary aSourceConnectionDict, NSDictionary aDestConnectionDict) throws SQLException {
            source = connectionWithDictionary(aSourceConnectionDict);
            dest = connectionWithDictionary(aDestConnectionDict);
        }

        public void connect(String sourcePrefix, String destPrefix) throws SQLException {
            source = connectionWithDictionary(dictionaryFromPrefix(sourcePrefix));
            dest = connectionWithDictionary(dictionaryFromPrefix(destPrefix));
        }
        
        private NSDictionary dictionaryFromPrefix(String prefix) {
            NSMutableDictionary dict = new NSMutableDictionary();
            return dict;
        }
        protected void addEntitiesFromModelGroup(EOModelGroup group) {
            for (Enumeration enumeration = group.models().objectEnumerator(); enumeration.hasMoreElements();) {
                EOModel model = (EOModel) enumeration.nextElement();
                addEntitiesFromModel(model);
            }
        }
        
        protected void addEntitiesFromModel(EOModel model) {
            for (Enumeration enumeration = model.entities().objectEnumerator(); enumeration.hasMoreElements();) {
                EOEntity entity = (EOEntity) enumeration.nextElement();
                entities.addObject(entity);
            }
        }
        
        protected void addEntity(EOEntity entity) {
            entities.addObject(entity);
        }

        public void run() throws SQLException {
            run(true);
        }

        public void run(boolean commitAtEnd) throws SQLException {
            for (Enumeration models = entities.objectEnumerator(); models.hasMoreElements();) {
                EOEntity entity = (EOEntity) models.nextElement();
                copyEntity(entity);
            }
            if(commitAtEnd) {
                commit();
            }
        }

        public void commit() throws SQLException {
            dest.commit();
        }

        protected Connection connectionWithDictionary(NSDictionary dict) throws SQLException {
            String username = (String) dict.objectForKey("username");
            String password = (String) dict.objectForKey("password");
            String driver = (String) dict.objectForKey("driver");
            String url = (String) dict.objectForKey("url");
            Boolean autoCommit = (Boolean) dict.objectForKey("autoCommit");
            boolean ac = autoCommit == null ? true : autoCommit.booleanValue();
            Boolean readOnly = (Boolean) dict.objectForKey("readOnly");
            boolean ro = readOnly == null ? false : readOnly.booleanValue();

            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Could not find driver: " + driver);
            }
            Connection con = DriverManager.getConnection(url, username, password);
            DatabaseMetaData dbmd = con.getMetaData();
            log.info("Connection to " + dbmd.getDatabaseProductName() + " " + dbmd.getDatabaseProductVersion()
                    + " successful.");
            con.setAutoCommit(ac);
            return con;
        }

        protected String[] columnsFromAttributes(EOAttribute[] attributes, boolean quoteNames) {

            NSArray a = columnsFromAttributesAsArray(attributes, quoteNames);
            String[] result = new String[a.count()];
            for (int i = 0; i < a.count(); i++) {
                String s = (String) a.objectAtIndex(i);
                result[i] = s;
            }
            return result;
        }

        protected NSArray columnsFromAttributesAsArray(EOAttribute[] attributes, boolean quoteNames) {

            if (attributes == null) { 
                throw new NullPointerException("attributes cannot be null!"); 
            }

            NSMutableArray columns = new NSMutableArray();
            for (int i = attributes.length; i-- > 0;) {
                EOAttribute att = attributes[i];
                String column = att.columnName();
                if (!ERXStringUtilities.stringIsNullOrEmpty(column)) {
                    if (quoteNames) {
                        columns.addObject("" + column + "");
                    } else {
                        columns.addObject(column);
                    }
                } else {
                    log.warn("Attribute " + att.name() + " column was null or empty");
                }
            }
            return columns;
        }

        protected EOAttribute[] attributesArray(NSArray array) {
            NSMutableArray a = new NSMutableArray();
            for (int i = 0; i < array.count(); i++) {
                EOAttribute att = (EOAttribute) array.objectAtIndex(i);
                if (!ERXStringUtilities.stringIsNullOrEmpty(att.columnName())) {
                    a.addObject(att);
                }
            }

            EOAttribute[] result = new EOAttribute[a.count()];
            for (int i = 0; i < a.count(); i++) {
                result[i] = (EOAttribute) a.objectAtIndex(i);
            }
            return result;
        }

        protected void copyEntity(EOEntity entity) throws SQLException {
            EOAttribute[] attributes = attributesArray(entity.attributes());
            String tableName = entity.externalName();
            String[] columnNames = columnsFromAttributes(attributes, true);
            String[] columnNamesWithoutQuotes = columnsFromAttributes(attributes, false);
            String columns = columnsFromAttributesAsArray(attributes, true).componentsJoinedByString(", ");

            // build the select statement, this selects -all- rows
            StringBuffer selectBuf = new StringBuffer();
            selectBuf.append("select ");
            selectBuf.append(columns).append(" from ").append(tableName).append(";");
            String sql = selectBuf.toString();

            Statement stmt = source.createStatement();

            StringBuffer insertBuf = new StringBuffer();
            insertBuf.append("insert into ").append("" + tableName + "").append(" (").append(columns).append(") values (");
            for (int i = columnNames.length; i-- > 0;) {
                insertBuf.append("?");
                if (i > 0) {
                    insertBuf.append(", ");
                }
            }
            insertBuf.append(");");

            String insertSql = insertBuf.toString();
            PreparedStatement upps = dest.prepareStatement(insertSql);

            try {
                ResultSet rows = stmt.executeQuery(sql);
                // transfer each row by first setting the values
                int rowsCount = 0;
                while (rows.next()) {
                    rowsCount++;
                    if (rows.getRow() % 1000 == 0) {
                        log.info("table " + tableName + ", inserted " + rows.getRow() + " rows");
                    }

                    NSMutableSet tempfilesToDelete = new NSMutableSet();
                    // call upps.setInt, upps.setBinaryStream, ...
                    for (int i = 0; i < columnNamesWithoutQuotes.length; i++) {
                        // first we need to get the type
                        String columnName = columnNamesWithoutQuotes[i];
                        int type = rows.getMetaData().getColumnType(i + 1);

                        Object o = rows.getObject(columnName);
                        if (log.isDebugEnabled()) {
                            if (o != null) {
                                log.info("column=" + columnName + ", value class=" + o.getClass().getName() + ", value="
                                        + o);
                            } else {
                                log.info("column=" + columnName + ", value class unknown, value is null");
                            }
                        }

                        if (o instanceof Blob) {
                            Blob b = (Blob) o;
                            InputStream bis = b.getBinaryStream();
                            // stream this to a file, we need the length...
                            File tempFile = null;
                            try {
                                tempFile = File.createTempFile("TempJDBC", ".blob");
                                ERXFileUtilities.writeInputStreamToFile(bis, tempFile);
                            } catch (IOException e5) {
                                log.error("could not create tempFile for row " + rows.getRow() + " and column "
                                        + columnName + ", setting column value to null!");
                                EOAttribute at = attributes[i];
                                upps.setNull(i + 1, type);
                                if (tempFile != null) if (!tempFile.delete()) tempFile.delete();

                                continue;
                            }
                            FileInputStream fis;
                            try {
                                fis = new FileInputStream(tempFile);
                            } catch (FileNotFoundException e6) {
                                log.error("could not create FileInputStream from tempFile for row " + rows.getRow()
                                        + " and column " + columnName + ", setting column value to null!");
                                EOAttribute at = attributes[i];
                                upps.setNull(i + 1, type);
                                if (tempFile != null) if (!tempFile.delete()) tempFile.delete();

                                continue;
                            }
                            upps.setBinaryStream(i + 1, fis, (int) tempFile.length());
                            tempfilesToDelete.addObject(tempFile);
                        } else if (o != null) {
                            upps.setObject(i + 1, o);
                        } else {
                            EOAttribute at = attributes[i];
                            upps.setNull(i + 1, type);
                        }
                    }
                    upps.executeUpdate();
                    upps.clearParameters();
                    for (Enumeration e = tempfilesToDelete.objectEnumerator(); e.hasMoreElements();) {
                        File f = (File) e.nextElement();
                        if (!f.delete()) f.delete();
                    }

                    if (rows.getRow() % 1000 == 0) {
                        log.info("committing at count=" + rowsCount);
                        dest.commit();
                        log.info("committing done");
                    }

                }
                log.info("table " + tableName + ", inserted " + rowsCount + " rows");
                rows.close();
            } catch (SQLException e2) {
                log.error("could not get next from resultset", e2);
            }
        }        
    }

    public static String jdbcTimestamp(NSTimestamp t) {
        StringBuffer b = new StringBuffer();
        b.append("TIMESTAMP '").append(TIMESTAMP_FORMATTER.format(t)).append("'");
        return b.toString();
    }

    /**
     * Copies all rows from one database to another database. The tables must
     * exist before calling this method.
     * 
     * @param entity
     *            the EOEntity which is related to the table which we want to
     *            copy
     * @param sourceDict
     *            a NSDictionary containing the following keys for the source
     *            database:
     *            <ol>
     *            <li>username, the username for the connection
     *            <li>password, the password for the connection
     *            <li>url, the JDBC URL for the connection, for FrontBase its
     *            <code>jdbc:FrontBase://host/database</code> , for PostgreSQL
     *            its <code>jdbc:postgresql://host/database</code>
     *            <li>driver, the full class name of the driver, for FrontBase
     *            its <code>com.frontbase.jdbc.FBJDriver</code> , for
     *            PostgreSQL its <code>org.postgresql.Driver</code>
     *            <li>autoCommit, a Boolean defining if autoCommit should be on
     *            or off, default is true
     *            <li>readOnly, a Boolean defining if the Connection is
     *            readOnly or not, default is false. Its a good idea to make the
     *            sourceDict readOnly, because one does not write.
     *            </ol>
     * @param destDict
     *            same as sourceDict just used for the destination database.
     * @param commitAtEnd,
     *            a boolean which defines if the destination Connection should
     *            get a commit at the end. Set this to false if you want to
     *            transfer multiple tables with primary key - foreign key
     *            dependencies
     */

    public static void _copyDatabaseDefinedByEOModelAndConnectionDictionaryToDatabaseWithConnectionDictionary(EOModel m,
            NSDictionary sourceDict, NSDictionary destDict) {
        try {
            CopyTask task = new CopyTask(m);
            task.connect(sourceDict, destDict);
            task.run(false);
            log.info("committing...");
            task.commit();
            log.info("committing... done");
        } catch (SQLException e) {
            log.error("could not commit destCon", e);
        }
    }
    
}
