package er.extensions;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;

public class ERXJDBCUtilities {
	public static ERXLogger log =
		ERXLogger.getERXLogger(ERXJDBCUtilities.class);
	public static NSTimestampFormatter TIMESTAMP_FORMATTER =
		new NSTimestampFormatter("%Y-%m-%d %H:%M:%S.%F");

	/**
	 * @param t
	 * @return
	 */
	public static String jdbcTimestamp(NSTimestamp t) {
		StringBuffer b = new StringBuffer();
		b.append("TIMESTAMP '").append(TIMESTAMP_FORMATTER.format(t)).append("'");
		return b.toString();
	}

	/**
	 * @param prototypeName
	 */
	public static void dumpJDBCTypesFromPrototype(String prototypeName) {
		EOModel m = EOModelGroup.defaultGroup().modelNamed("erprototypes");
		if (m == null) {
			log.error(
				"cannot dump jdbc types because ERPrototypes model is not loaded.");
			return;
		}

		EOEntity entity = m.entityNamed(prototypeName);
		if (entity == null) {
			log.error(
				"cannot dump jdb types because Entity "
					+ prototypeName
					+ " does not exist.");
			return;
		}

		NSArray a = entity.attributes();
		for (int i = a.count(); i-- > 0;) {
			EOAttribute att = (EOAttribute) a.objectAtIndex(i);
			log.info(
				"COLUMN = " + att.columnName() + " -> " + att.externalType());
		}
	}

	public static void copyDatabaseDefinedByEOModelAndConnectionDictionaryToDatabaseWithConnectionDictionary(EOModel m, NSDictionary sourceDict, NSDictionary destDict) {
		//create two connections with the sourceDict
		Connection sourceCon = connectionWithDictionary(sourceDict);
		Connection destCon = connectionWithDictionary(destDict);
		log.info("will copy model "+m.name());
		for (Enumeration e = m.entities().objectEnumerator(); e.hasMoreElements();) {
			EOEntity entity = (EOEntity)e.nextElement();
			if (entity.parentEntity() == null) {
			    log.info("will copy table "+entity.externalName());
			    copyTableDefinedByEntityAndConnectionToDatabaseWithConnection(entity, sourceCon, destCon, false);
			} else {
			    log.info("will not copy entity "+entity.name());
			    
			}
		}
		try {
			log.info("committing...");
			destCon.commit();
			log.info("committing... done");
		} catch (SQLException e) {
			log.error("could not commit destCon", e);
		}
	}

	/** copies all rows from one database table to another database's table. 
	 * The table must be created before calling this method.
	 * 
	 * @param entity the EOEntity which is related to the table which we want to copy
	 * @param sourceCon, the Connection for the source database
	 * @param destCon, the Connection for the destination database
	 * @param commitAtEnd, a boolean which defines if the destination Connection should get a commit
	 * at the end. Set this to false if you want to transfer multiple tables with primary key - foreign key 
	 * dependencies
	 */
	public static void copyTableDefinedByEntityAndConnectionToDatabaseWithConnection(EOEntity entity, Connection sourceCon, Connection destCon, boolean commitAtEnd) {
	    boolean destinationIsPostgres;
        try {
            destinationIsPostgres = destCon.getMetaData().getDatabaseProductName().toLowerCase().indexOf("postgres") != -1;
        } catch (SQLException e4) {
            log.error("could not get product name from destination database", e4);
            return;
        }
        EOAttribute[] attributes = attributesArray(entity.attributes());
		String tableName = entity.externalName();
		String[] columnNames = columnsFromAttributes(attributes, true);
		String[] columnNamesWithoutQuotes = columnsFromAttributes(attributes, false);
		String columns =
			columnsFromAttributesAsArray(attributes, true).componentsJoinedByString(
			", ");
		
		//build the select statement, this selects -all- rows
		StringBuffer buf = new StringBuffer();
		buf.append("select ");
		buf.append(columns).append(" from ").append(tableName).append(";");
		String sql = buf.toString();

		Statement stmt;
		try {
			stmt = sourceCon.createStatement();
		} catch (SQLException e) {
			log.error("could not create statement for source database", e);
			log.error("skipping table "+tableName);
			return;
		}
		ResultSet rows = null;
		log.info("will execute select:"+sql);
		try {
			rows = stmt.executeQuery(sql);
		} catch (SQLException e1) {
			log.error("could not execute select " + sql, e1);
			log.error("skipping table "+tableName);
			return;
		}

		StringBuffer buf1 = new StringBuffer();
		buf1
		.append("insert into ")
		.append(""+tableName+"")
		.append(" (")
		.append(columns)
		.append(") values (");
		for (int i = columnNames.length; i-- > 0;) {
			buf1.append("?");
			if (i > 0)
				buf1.append(", ");
		}
		buf1.append(");");

		String insertSql = buf1.toString();

		PreparedStatement upps = null;
		try {
			upps = destCon.prepareStatement(insertSql);
		} catch (SQLException e3) {
			log.error(
					"could not create prepared statement for SQL " + insertSql,
					e3);
			log.error("skipping table "+tableName);
			return;
		}
		
		try {
			//transfer each row by first setting the values
			int rowsCount = 0;
		    while (rows.next()) {
			    rowsCount++;
				if (rows.getRow() % 1000 == 0) {
					log.info("table "+tableName+", inserted "+rows.getRow()+" rows");
				}
				
				NSMutableSet tempfilesToDelete = new NSMutableSet();
				//call upps.setInt, upps.setBinaryStream, ...
				for (int i = 0; i < columnNamesWithoutQuotes.length; i++) {
					//first we need to get the type
					String columnName = columnNamesWithoutQuotes[i];
					//HACKALERT: this works for me!
					if (destinationIsPostgres) columnName = columnName.toUpperCase();
					
					Object o = rows.getObject(columnName);
					if (log.isDebugEnabled()) {
						if (o != null) {
							log.info(
									"column="
									+ columnName
									+ ", value class="
									+ o.getClass().getName()
									+ ", value="
									+ o);
						} else {
							log.info(
									"column="
									+ columnName
									+ ", value class unknown, value is null");
						}
					}

					if (o instanceof Blob) {
						Blob b = (Blob)o;
						InputStream bis = b.getBinaryStream();
						//stream this to a file, we need the length...
						File tempFile = null;
						try {
						    tempFile = File.createTempFile("TempJDBC", ".blob");
							ERXFileUtilities.writeInputStreamToFile(bis, tempFile);
						} catch (IOException e5) {
							log.error("could not create tempFile for row "+rows.getRow()+" and column "+columnName+", setting column value to null!");
							EOAttribute at = attributes[i];
							int type = typeFromAttribute(at);
							upps.setNull(i + 1, type);
							if (tempFile != null)
							    if (!tempFile.delete()) tempFile.delete();
							
							continue;
						}
						FileInputStream fis;
						try {
							fis = new FileInputStream(tempFile);
						} catch (FileNotFoundException e6) {
							log.error("could not create FileInputStream from tempFile for row "+rows.getRow()+" and column "+columnName+", setting column value to null!");
							EOAttribute at = attributes[i];
							int type = typeFromAttribute(at);
							upps.setNull(i + 1, type);
							if (tempFile != null)
							    if (!tempFile.delete()) tempFile.delete();
							    
							continue;
						}
						upps.setBinaryStream(i + 1, fis, (int)tempFile.length());
						tempfilesToDelete.addObject(tempFile);
					} else if (o != null) {
						upps.setObject(i + 1, o);
					} else {
						EOAttribute at = attributes[i];
						int type = typeFromAttribute(at);
						upps.setNull(i + 1, type);
					}
				}
				upps.executeUpdate();
				upps.clearParameters();
				for (Enumeration e = tempfilesToDelete.objectEnumerator(); e.hasMoreElements();) {
				    File f = (File)e.nextElement();
				    if (!f.delete()) f.delete();
				}
				
				if (rows.getRow() % 1000 == 0) {
					log.info("committing at count="+rowsCount); 
					destCon.commit();
					log.info("committing done"); 
				}

			}
			log.info("table "+tableName+", inserted "+rowsCount+" rows");
			
			if (commitAtEnd) {
				destCon.commit();
			}
			rows.close();
		} catch (SQLException e2) {
			log.error("could not get next from resultset", e2);
		}
	}

	/** copies all rows from one database to another database. The tabes must be created before calling
	 * this method.
	 * 
	 * @param entity the EOEntity which is related to the table which we want to copy
	 * @param sourceDict a NSDictionary containing the following keys for the source database:
	 * <ol>
	 * <li>username, the username for the connection
	 * <li>password, the password for the connection
	 * <li>url, the JDBC URL for the connection, for FrontBase its <code>jdbc:FrontBase://host/database</code>
	 * , for PostgreSQL its <code>jdbc:postgresql://host/database</code>
	 * <li>driver, the full class name of the driver, for FrontBase its <code>com.frontbase.jdbc.FBJDriver</code>
	 * , for PostgreSQL its <code>org.postgresql.Driver</code>
	 * <li>autoCommit, a Boolean defining if autoCommit should be on or off, default is true
	 * <li>readOnly, a Boolean defining if the Connection is readOnly or not, default is false. 
	 * Its a good idea to make the sourceDict readOnly, because one does not write.
	 * @param destDict same as sourceDict just used for the destination database.
	 * @param commitAtEnd, a boolean which defines if the destination Connection should get a commit
	 * at the end. Set this to false if you want to transfer multiple tables with primary key - foreign key 
	 * dependencies
	 */
	public static void copyTableDefinedByEntityAndConnectionDictionaryToDatabaseWithConnectionDictionary(
		EOEntity entity,
		NSDictionary sourceDict,
		NSDictionary destDict, boolean commitAtEnd) {

		//create two connections with the sourceDict
		Connection sourceCon = connectionWithDictionary(sourceDict);
		Connection destCon = connectionWithDictionary(destDict);

		copyTableDefinedByEntityAndConnectionToDatabaseWithConnection(entity, sourceCon, destCon, commitAtEnd);
	}

	/**
	 * @param columnName
	 * @return
	 */
	private static int typeFromAttribute(EOAttribute attribute) {
		String pn = attribute.prototypeName();
		
		if (pn.equals("amount") || pn.equals("doubleNumber")) {
			return Types.DOUBLE;
		} else if (
			pn.equals("blob")
				|| pn.equals("mutableArray")
				|| pn.equals("mutableDictionary")) {

			return Types.BLOB;
		} else if (
			pn.equals("boolean")
				|| pn.equals("charFlag")
				|| pn.equals("longText")
				|| pn.equals("osType")
				|| pn.equals("shortString")
				|| pn.equals("type")
				|| pn.equals("type")
				|| pn.startsWith("varchar")) {
			return Types.VARCHAR;
			
		} else if (
			pn.equals("id") || pn.equals("intNumber") || pn.equals("flag")) {
			return Types.INTEGER;
			
		} else if (pn.equals("date") || pn.equals("dateTime")) {
			return Types.TIMESTAMP;
			
		} else {
			log.error("unknown prototype " + pn);
			return 0;
		}

	}


	public static String[] columnsFromAttributes(
			EOAttribute[] attributes,
			boolean quoteNames) {
		
		NSArray a = columnsFromAttributesAsArray(attributes, quoteNames);
		String[] result = new String[a.count()];
		for (int i = 0; i < a.count(); i++) {
			String s = (String) a.objectAtIndex(i);
			result[i] = s;
		}
		return result;
	}

	/**
	 * @return
	 */
	private static NSArray columnsFromAttributesAsArray(
		EOAttribute[] attributes,
		boolean quoteNames) {
		
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
				log.warn(
					"Attribute " + att.name() + " column was null or empty");
			}
		}
		return columns;
	}

	/**
	 * @param array
	 * @return
	 */
	private static EOAttribute[] attributesArray(NSArray array) {
		NSMutableArray a = new NSMutableArray();
		for (int i = 0; i < array.count(); i++) {
			EOAttribute att = (EOAttribute)array.objectAtIndex(i);
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

	public static Connection connectionWithDictionary(NSDictionary dict) {
		String username = (String) dict.objectForKey("username");
		String password = (String) dict.objectForKey("password");
		String driver = (String) dict.objectForKey("driver");
		String url = (String) dict.objectForKey("url");
		Boolean autoCommit = (Boolean)dict.objectForKey("autoCommit");
		boolean ac = autoCommit == null ? true : autoCommit.booleanValue();
		Boolean readOnly = (Boolean)dict.objectForKey("readOnly");
		boolean ro = readOnly == null ? false: readOnly.booleanValue();
		
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			log.error("could not find driver " + driver);
			return null;
		}
		try {
			Connection con =
				DriverManager.getConnection(url, username, password);
			DatabaseMetaData dbmd = con.getMetaData();
			log.info(
				"Connection to "
					+ dbmd.getDatabaseProductName()
					+ " "
					+ dbmd.getDatabaseProductVersion()
					+ " successful.");
			con.setAutoCommit(ac);
			return con;
		} catch (SQLException e1) {
			log.info("could not create Connection, error ", e1);
			return null;
		}
	}

}
