package er.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.jdbcadaptor.JDBCAdaptor;
import com.webobjects.jdbcadaptor.JDBCContext;

public class ERXJDBCUtilities {

	public static Logger log = Logger.getLogger(ERXJDBCUtilities.class);

	public static NSTimestampFormatter TIMESTAMP_FORMATTER = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S.%F");

	public static class CopyTask {
		protected NSDictionary sourceDictionary;
		protected NSDictionary destDictionary;
		protected Connection source;
		protected Connection dest;
		protected boolean _quoteSource;
		protected boolean _quoteDestination;

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

		public CopyTask() {
		}

		public void connect(NSDictionary aSourceConnectionDict, NSDictionary aDestConnectionDict) throws SQLException {
			sourceDictionary = aSourceConnectionDict;
			destDictionary = aDestConnectionDict;
			source = connectionWithDictionary(aSourceConnectionDict);
			dest = connectionWithDictionary(aDestConnectionDict);
			_quoteSource = Boolean.valueOf((String) aSourceConnectionDict.objectForKey("quote")).booleanValue();
			_quoteDestination = Boolean.valueOf((String) aDestConnectionDict.objectForKey("quote")).booleanValue();
		}

		public void connect(String sourcePrefix, String destPrefix) throws SQLException {
			sourceDictionary = dictionaryFromPrefix(sourcePrefix);
			destDictionary = dictionaryFromPrefix(destPrefix);
			connect(sourceDictionary, destDictionary);
		}

		private NSDictionary dictionaryFromPrefix(String prefix) {
			NSMutableDictionary dict = new NSMutableDictionary();
			return dict;
		}

		protected void addEntitiesFromModelGroup(EOModelGroup group) {
			for (Enumeration enumeration = group.models().objectEnumerator(); enumeration.hasMoreElements();) {
				EOModel model = (EOModel) enumeration.nextElement();
				if ("JDBC".equalsIgnoreCase(model.adaptorName())) {
					addEntitiesFromModel(model);
				}
			}
		}

		protected void addEntitiesFromModel(EOModel model) {
			for (Enumeration enumeration = model.entities().objectEnumerator(); enumeration.hasMoreElements();) {
				EOEntity entity = (EOEntity) enumeration.nextElement();
				entities.addObject(entity);
			}
		}

		public void addEntity(EOEntity entity) {
			entities.addObject(entity);
		}

		public void run() throws SQLException {
			run(true);
		}

		public void run(boolean commitAtEnd) throws SQLException {
			for (Enumeration models = entities.objectEnumerator(); models.hasMoreElements();) {
				EOEntity entity = (EOEntity) models.nextElement();
				if (!entity.isAbstractEntity()) {
					copyEntity(entity);
				}
			}
			if (commitAtEnd) {
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
			String url = (String) dict.objectForKey("URL");
			if (url == null) {
				url = (String) dict.objectForKey("url");
			}
			Boolean autoCommit;
			Object autoCommitObj = dict.objectForKey("autoCommit");
			if (autoCommitObj instanceof String) {
				autoCommit = Boolean.valueOf((String) autoCommitObj);
			}
			else {
				autoCommit = (Boolean) autoCommitObj;
			}
			boolean ac = autoCommit == null ? true : autoCommit.booleanValue();
			// Boolean readOnly = (Boolean) dict.objectForKey("readOnly");
			// boolean ro = readOnly == null ? false : readOnly.booleanValue();

			try {
				Class.forName(driver);
			}
			catch (ClassNotFoundException e) {
				throw new SQLException("Could not find driver: " + driver);
			}
			Connection con = DriverManager.getConnection(url, username, password);
			DatabaseMetaData dbmd = con.getMetaData();
			log.info("Connection to " + dbmd.getDatabaseProductName() + " " + dbmd.getDatabaseProductVersion() + " successful.");
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
						columns.addObject("\"" + column + "\"");
					}
					else {
						columns.addObject(column);
					}
				}
				else {
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

			// build the select statement, this selects -all- rows
			StringBuffer selectBuf = new StringBuffer();
			selectBuf.append("select ");
			selectBuf.append(columnsFromAttributesAsArray(attributes, _quoteSource).componentsJoinedByString(", ")).append(" from ");
			if (_quoteSource) {
				selectBuf.append("\"" + tableName + "\"");
			}
			else {
				selectBuf.append(tableName);
			}
			EOQualifier qualifier = entity.restrictingQualifier();
			if (qualifier != null) {
				EOAdaptor adaptor = EOAdaptor.adaptorWithName("JDBC");
				adaptor.setConnectionDictionary(sourceDictionary);
				EOSQLExpressionFactory factory = adaptor.expressionFactory();
				EOSQLExpression sqlExpression = factory.createExpression(entity);
				String sqlString = EOQualifierSQLGeneration.Support._sqlStringForSQLExpression(qualifier, sqlExpression);
				selectBuf.append(" where ").append(sqlString);
			}
			selectBuf.append(";");
			String sql = selectBuf.toString();
			Statement stmt = source.createStatement();

			StringBuffer insertBuf = new StringBuffer();
			insertBuf.append("insert into ");
			if (_quoteDestination) {
				insertBuf.append("\"" + tableName + "\"");
			}
			else {
				insertBuf.append(tableName);
			}
			insertBuf.append(" (").append(columnsFromAttributesAsArray(attributes, _quoteDestination).componentsJoinedByString(", ")).append(") values (");
			for (int i = columnNames.length; i-- > 0;) {
				insertBuf.append("?");
				if (i > 0) {
					insertBuf.append(", ");
				}
			}
			insertBuf.append(");");

			String insertSql = insertBuf.toString();
			System.out.println("CopyTask.copyEntity: " + insertSql);
			PreparedStatement upps = dest.prepareStatement(insertSql);

			ResultSet rows = stmt.executeQuery(sql);
			// transfer each row by first setting the values
			int rowsCount = 0;
			while (rows.next()) {
				rowsCount++;
				if (rows.getRow() % 1000 == 0) {
					System.out.println("CopyTask.copyEntity: table " + tableName + ", inserted " + rows.getRow() + " rows");
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
							log.info("column=" + columnName + ", value class=" + o.getClass().getName() + ", value=" + o);
						}
						else {
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
						}
						catch (IOException e5) {
							log.error("could not create tempFile for row " + rows.getRow() + " and column " + columnName + ", setting column value to null!");
							upps.setNull(i + 1, type);
							if (tempFile != null)
								if (!tempFile.delete())
									tempFile.delete();

							continue;
						}
						FileInputStream fis;
						try {
							fis = new FileInputStream(tempFile);
						}
						catch (FileNotFoundException e6) {
							log.error("could not create FileInputStream from tempFile for row " + rows.getRow() + " and column " + columnName + ", setting column value to null!");
							upps.setNull(i + 1, type);
							if (tempFile != null)
								if (!tempFile.delete())
									tempFile.delete();

							continue;
						}
						upps.setBinaryStream(i + 1, fis, (int) tempFile.length());
						tempfilesToDelete.addObject(tempFile);
					}
					else if (o != null) {
						upps.setObject(i + 1, o);
					}
					else {
						upps.setNull(i + 1, type);
					}
				}
				upps.executeUpdate();
				upps.clearParameters();
				for (Enumeration e = tempfilesToDelete.objectEnumerator(); e.hasMoreElements();) {
					File f = (File) e.nextElement();
					if (!f.delete())
						f.delete();
				}

				// if (rows.getRow() % 1000 == 0) {
				// log.info("committing at count=" + rowsCount);
				// dest.commit();
				// log.info("committing done");
				// }

			}
			log.info("table " + tableName + ", inserted " + rowsCount + " rows");
			rows.close();
		}
	}

	public static String jdbcTimestamp(NSTimestamp t) {
		StringBuffer b = new StringBuffer();
		b.append("TIMESTAMP '").append(TIMESTAMP_FORMATTER.format(t)).append("'");
		return b.toString();
	}

	/**
	 * Copies all rows from one database to another database. The tables must exist before calling this method.
	 * 
	 * Example:
	 * 
	 * <pre>
	 * NSMutableDictionary sourceDict = new NSMutableDictionary();
	 * sourceDict.setObjectForKey(&quot;YourPassword&quot;, &quot;password&quot;);
	 * sourceDict.setObjectForKey(&quot;YourUserName&quot;, &quot;username&quot;);
	 * sourceDict.setObjectForKey(&quot;jdbc:FrontBase://127.0.0.1/YourSourceDatabase&quot;, &quot;URL&quot;);
	 * sourceDict.setObjectForKey(&quot;com.frontbase.jdbc.FBJDriver&quot;, &quot;driver&quot;);
	 * sourceDict.setObjectForKey(Boolean.FALSE.toString(), &quot;autoCommit&quot;);
	 * sourceDict.setObjectForKey(Boolean.TRUE.toString(), &quot;readOnly&quot;);
	 * sourceDict.setObjectForKey(Boolean.TRUE.toString(), &quot;quote&quot;);
	 * 
	 * NSMutableDictionary destDict = sourceDict.mutableClone();
	 * destDict.setObjectForKey(&quot;jdbc:postgresql://localhost/YourDestinationDatabase&quot;, &quot;URL&quot;);
	 * destDict.setObjectForKey(&quot;YourPassword&quot;, &quot;password&quot;);
	 * destDict.setObjectForKey(&quot;YourUserName&quot;, &quot;username&quot;);
	 * destDict.setObjectForKey(&quot;org.postgresql.Driver&quot;, &quot;driver&quot;);
	 * destDict.setObjectForKey(Boolean.FALSE.toString(), &quot;autoCommit&quot;);
	 * destDict.setObjectForKey(Boolean.FALSE.toString(), &quot;readOnly&quot;);
	 * destDict.setObjectForKey(Boolean.FALSE.toString(), &quot;quote&quot;);
	 * 
	 * EOModel model = EOModelGroup.defaultGroup().modelNamed(&quot;YourModelName&quot;);
	 * ERXJDBCUtilities._copyDatabaseDefinedByEOModelAndConnectionDictionaryToDatabaseWithConnectionDictionary(model, sourceDict, destDict);
	 * </pre>
	 * 
	 * @param entity
	 *            the EOEntity which is related to the table which we want to copy
	 * @param sourceDict
	 *            a NSDictionary containing the following keys for the source database:
	 *            <ol>
	 *            <li>username, the username for the connection
	 *            <li>password, the password for the connection
	 *            <li>url, the JDBC URL for the connection, for FrontBase its
	 *            <code>jdbc:FrontBase://host/database</code> , for PostgreSQL its
	 *            <code>jdbc:postgresql://host/database</code>
	 *            <li>driver, the full class name of the driver, for FrontBase its
	 *            <code>com.frontbase.jdbc.FBJDriver</code> , for PostgreSQL its <code>org.postgresql.Driver</code>
	 *            <li>autoCommit, a Boolean defining if autoCommit should be on or off, default is true
	 *            <li>readOnly, a Boolean defining if the Connection is readOnly or not, default is false. Its a good
	 *            <li>quote, a Boolean defining if the table and field names should be "quoted" idea to make the
	 *            sourceDict readOnly, because one does not write.
	 *            </ol>
	 * @param destDict
	 *            same as sourceDict just used for the destination database.
	 * @param commitAtEnd,
	 *            a boolean which defines if the destination Connection should get a commit at the end. Set this to
	 *            false if you want to transfer multiple tables with primary key - foreign key dependencies
	 */
	public static void _copyDatabaseDefinedByEOModelAndConnectionDictionaryToDatabaseWithConnectionDictionary(EOModel m, NSDictionary sourceDict, NSDictionary destDict) {
		try {
			CopyTask task = new CopyTask(m);
			task.connect(sourceDict, destDict);
			task.run(false);
			log.info("committing...");
			task.commit();
			log.info("committing... done");
		}
		catch (SQLException e) {
			log.error("could not commit destCon", e);
		}
	}

	/**
	 * @see _copyDatabaseDefinedByEOModelAndConnectionDictionaryToDatabaseWithConnectionDictionary(EOModel, NSDictionary, NSDictionary)
	 * @param modelGroup the model group to copy
	 * @param sourceDict the source connection dictionary
	 * @param destDict the destination connection dictionary
	 * @return
	 */
	public static void _copyDatabaseDefinedByEOModelAndConnectionDictionaryToDatabaseWithConnectionDictionary(EOModelGroup modelGroup, NSDictionary sourceDict, NSDictionary destDict) {
		try {
			CopyTask task = new CopyTask(modelGroup);
			task.connect(sourceDict, destDict);
			task.run(false);
			log.info("committing...");
			task.commit();
			log.info("committing... done");
		}
		catch (SQLException e) {
			log.error("could not commit destCon", e);
		}
	}

	/**
	 * Shortcut to java.sql.Statement.executeUpdate(..) that operates on an EOAdaptorChannel.
	 * 
	 * @param channel
	 *            the JDBCChannel to work with
	 * @param sql
	 *            the sql to execute
	 * @return the number of rows updated
	 * @throws SQLException
	 *             if there is a problem
	 */
	public static int executeUpdate(EOAdaptorChannel channel, String sql) throws SQLException {
		int rowsUpdated;
		boolean wasOpen = channel.isOpen();
		if (!wasOpen) {
			channel.openChannel();
		}
		Connection conn = ((JDBCContext) channel.adaptorContext()).connection();
		try {
			Statement stmt = conn.createStatement();
			try {
				rowsUpdated = stmt.executeUpdate(sql);
			}
			finally {
				stmt.close();
			}
		}
		finally {
			if (!wasOpen) {
				channel.closeChannel();
			}
		}
		return rowsUpdated;
	}

	/**
	 * Splits the given sqlscript and executes each of the statements in a single transaction
	 * 
	 * @param channel
	 *            the JDBCChannel to work with
	 * @param sqlScript
	 *            the sql script to execute
	 * @return the number of rows updated
	 * @throws SQLException
	 *             if there is a problem
	 */
	public static int executeUpdateScript(EOAdaptorChannel channel, String sqlScript) throws SQLException {
		NSArray sqlStatements = ERXSQLHelper.newSQLHelper(channel).splitSQLStatements(sqlScript);
		return ERXJDBCUtilities.executeUpdateScript(channel, sqlStatements);
	}

	/**
	 * Splits the given sqlscript and executes each of the statements in a single transaction
	 * 
	 * @param channel
	 *            the JDBCChannel to work with
	 * @param sqlScript
	 *            the array of sql scripts to execute
	 * @param filter
	 *            the sql filter to use to filter out unwanted statements
	 * @return the number of rows updated
	 * @throws SQLException
	 *             if there is a problem
	 */
	public static int executeUpdateScript(EOAdaptorChannel channel, NSArray sqlStatements) throws SQLException {
		ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(channel);
		int rowsUpdated = 0;
		boolean wasOpen = channel.isOpen();
		if (!wasOpen) {
			channel.openChannel();
		}
		Connection conn = ((JDBCContext) channel.adaptorContext()).connection();
		try {
			Statement stmt = conn.createStatement();
			try {
				Enumeration sqlStatementsEnum = sqlStatements.objectEnumerator();
				while (sqlStatementsEnum.hasMoreElements()) {
					String sql = (String) sqlStatementsEnum.nextElement();
					if (sqlHelper.shouldExecute(sql)) {
						if (ERXJDBCUtilities.log.isInfoEnabled()) {
							ERXJDBCUtilities.log.info("Executing " + sql);
						}
						rowsUpdated += stmt.executeUpdate(sql);
					}
					else {
						if (ERXJDBCUtilities.log.isInfoEnabled()) {
							ERXJDBCUtilities.log.info("Skipping " + sql);
						}
					}
				}
			}
			finally {
				stmt.close();
			}
		}
		finally {
			if (!wasOpen) {
				if (!conn.getAutoCommit()) {
					conn.commit();
				}
				channel.closeChannel();
			}
		}
		return rowsUpdated;
	}

	/**
	 * Executes a SQL script that is stored as a resource.
	 * 
	 * @param channel
	 *            the channel to execute the scripts within
	 * @param resourceName
	 *            the name of the SQL script resource
	 * @param frameworkName
	 *            the name of the framework that contains the resource
	 * @return the number of rows updated
	 * @throws SQLException
	 *             if a SQL error occurs
	 * @throws IOException
	 *             if an error occurs reading the script
	 */
	public static int executeUpdateScriptFromResourceNamed(EOAdaptorChannel channel, String resourceName, String frameworkName) throws SQLException, IOException {
		ERXJDBCUtilities.log.info("Executing SQL script '" + resourceName + "' from " + frameworkName + " ...");
		InputStream sqlScript = WOApplication.application().resourceManager().inputStreamForResourceNamed(resourceName, frameworkName, NSArray.EmptyArray);
		if (sqlScript == null) {
			throw new IllegalArgumentException("There is no resource named '" + resourceName + "'.");
		}
		NSArray sqlStatements;
		try {
			sqlStatements = ERXSQLHelper.newSQLHelper(channel).splitSQLStatementsFromInputStream(sqlScript);
		}
		finally {
			if (sqlScript != null) {
				sqlScript.close();
			}
		}
		return ERXJDBCUtilities.executeUpdateScript(channel, sqlStatements);
	}

	/**
	 * Creates tables, primary keys, and foreign keys for the tables in the given model. This is useful in your
	 * Migration #0 class.
	 * 
	 * @param channel
	 *            the channel to use for execution
	 * @param model
	 *            the model to create tables for
	 * @throws SQLException
	 *             if something fails
	 */
	public static void createTablesForModel(EOAdaptorChannel channel, EOModel model) throws SQLException {
		ERXJDBCUtilities.createTablesForEntities(channel, model.entities());
	}

	/**
	 * Creates tables, primary keys, and foreign keys for the given list of entities. This is useful in your Migration
	 * #0 class.
	 * 
	 * @param channel
	 *            the channel to use for execution
	 * @param entities
	 *            the entities to create tables for
	 * @throws SQLException
	 *             if something fails
	 */
	public static void createTablesForEntities(EOAdaptorChannel channel, NSArray entities) throws SQLException {
		NSMutableDictionary options = new NSMutableDictionary();
		options.setObjectForKey("NO", EOSchemaGeneration.DropTablesKey);
		options.setObjectForKey("NO", EOSchemaGeneration.DropPrimaryKeySupportKey);
		options.setObjectForKey("YES", EOSchemaGeneration.CreateTablesKey);
		options.setObjectForKey("YES", EOSchemaGeneration.CreatePrimaryKeySupportKey);
		options.setObjectForKey("YES", EOSchemaGeneration.PrimaryKeyConstraintsKey);
		options.setObjectForKey("YES", EOSchemaGeneration.ForeignKeyConstraintsKey);
		options.setObjectForKey("NO", EOSchemaGeneration.CreateDatabaseKey);
		options.setObjectForKey("NO", EOSchemaGeneration.DropDatabaseKey);
		EOSynchronizationFactory syncFactory = (EOSynchronizationFactory) channel.adaptorContext().adaptor().synchronizationFactory();
		String sqlScript = syncFactory.schemaCreationScriptForEntities(entities, options);
		ERXJDBCUtilities.executeUpdateScript(channel, sqlScript);
	}

	/**
	 * Returns the name of the database product for the given channel (handy when loading database-vendor-specific sql
	 * scripts in migrations).
	 * 
	 * @param channel
	 *            the channel
	 * @return the database the database product name ("FrontBase", "PostgreSQL")
	 */
	public static String databaseProductName(EOAdaptorChannel channel) {
		return ((JDBCAdaptor) channel.adaptorContext().adaptor()).plugIn().databaseProductName();
	}

	/**
	 * Returns the name of the database product for the given an eomodel (handy when loading database-vendor-specific
	 * sql scripts in migrations).
	 * 
	 * @param model
	 *            the EOModel
	 * @return the database the database product name ("FrontBase", "PostgreSQL")
	 */
	public static String databaseProductName(EOModel model) {
		EODatabaseContext databaseContext = EODatabaseContext.registeredDatabaseContextForModel(model, ERXEC.newEditingContext());
		EOAdaptor adaptor = databaseContext.database().adaptor();
		String databaseProductName;
		if (adaptor instanceof JDBCAdaptor) {
			databaseProductName = ((JDBCAdaptor) adaptor).plugIn().databaseProductName();
		}
		else {
			databaseProductName = adaptor.name();
		}
		return databaseProductName;
	}
}
