package er.extensions.jdbc;

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

import javax.sql.rowset.CachedRowSet;

import org.apache.log4j.Logger;

import com.sun.rowset.CachedRowSetImpl;
import com.webobjects.appserver.WOApplication;
import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOQualifierSQLGeneration;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
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

import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXExceptionUtilities;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXStringUtilities;

public class ERXJDBCUtilities {

	public static final Logger log = Logger.getLogger(ERXJDBCUtilities.class);

	public static final NSTimestampFormatter TIMESTAMP_FORMATTER = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S.%F");

	public static class CopyTask {
		protected NSDictionary _sourceDictionary;
		protected NSDictionary _destDictionary;
		protected Connection _source;
		protected Connection _dest;
		protected boolean _quoteSource;
		protected boolean _quoteDestination;

		protected NSMutableArray<EOEntity> _entities = new NSMutableArray<EOEntity>();

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
			_sourceDictionary = aSourceConnectionDict;
			_destDictionary = aDestConnectionDict;
			_source = connectionWithDictionary(aSourceConnectionDict);
			_dest = connectionWithDictionary(aDestConnectionDict);
			_quoteSource = Boolean.valueOf((String) aSourceConnectionDict.objectForKey("quote")).booleanValue();
			_quoteDestination = Boolean.valueOf((String) aDestConnectionDict.objectForKey("quote")).booleanValue();
		}

		public void connect(String sourcePrefix, String destPrefix) throws SQLException {
			_sourceDictionary = dictionaryFromPrefix(sourcePrefix);
			_destDictionary = dictionaryFromPrefix(destPrefix);
			connect(_sourceDictionary, _destDictionary);
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
				_entities.addObject(entity);
			}
		}

		public void addEntity(EOEntity entity) {
			_entities.addObject(entity);
		}

		public void run() throws SQLException {
			run(true);
		}

		public void run(boolean commitAtEnd) throws SQLException {
			for (Enumeration models = _entities.objectEnumerator(); models.hasMoreElements();) {
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
			_dest.commit();
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

			NSArray<String> a = columnsFromAttributesAsArray(attributes, quoteNames);
			String[] result = new String[a.count()];
			for (int i = 0; i < a.count(); i++) {
				String s = a.objectAtIndex(i);
				result[i] = s;
			}
			return result;
		}

		protected NSArray<String> columnsFromAttributesAsArray(EOAttribute[] attributes, boolean quoteNames) {

			if (attributes == null) {
				throw new NullPointerException("attributes cannot be null!");
			}

			NSMutableArray<String> columns = new NSMutableArray<String>();
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

		protected EOAttribute[] attributesArray(NSArray<EOAttribute> array) {
			NSMutableArray<EOAttribute> attributes = new NSMutableArray<EOAttribute>();
			for (int i = 0; i < array.count(); i++) {
				EOAttribute att = array.objectAtIndex(i);
				if (!ERXStringUtilities.stringIsNullOrEmpty(att.columnName())) {
					attributes.addObject(att);
				}
			}

			EOAttribute[] result = new EOAttribute[attributes.count()];
			for (int i = 0; i < attributes.count(); i++) {
				result[i] = attributes.objectAtIndex(i);
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		protected void copyEntity(EOEntity entity) throws SQLException {
			EOAttribute[] attributes = attributesArray(entity.attributes());
			String tableName = entity.externalName();
			String[] columnNames = columnsFromAttributes(attributes, true);
			String[] columnNamesWithoutQuotes = columnsFromAttributes(attributes, false);

			// build the select statement, this selects -all- rows
			StringBuilder selectBuf = new StringBuilder();
			selectBuf.append("select ");
			selectBuf.append(columnsFromAttributesAsArray(attributes, _quoteSource).componentsJoinedByString(", ")).append(" from ");
			if (_quoteSource) {
				selectBuf.append('"');
				selectBuf.append(tableName);
				selectBuf.append('"');
			}
			else {
				selectBuf.append(tableName);
			}
			EOQualifier qualifier = entity.restrictingQualifier();
			if (qualifier != null) {
				EOAdaptor adaptor = EOAdaptor.adaptorWithName("JDBC");
				adaptor.setConnectionDictionary(_sourceDictionary);
				EOSQLExpressionFactory factory = adaptor.expressionFactory();
				EOSQLExpression sqlExpression = factory.createExpression(entity);
				String sqlString = EOQualifierSQLGeneration.Support._sqlStringForSQLExpression(qualifier, sqlExpression);
				selectBuf.append(" where ").append(sqlString);
			}
			selectBuf.append(';');
			String sql = selectBuf.toString();
			Statement stmt = _source.createStatement();

			StringBuilder insertBuf = new StringBuilder();
			insertBuf.append("insert into ");
			if (_quoteDestination) {
				insertBuf.append('"');
				insertBuf.append(tableName);
				insertBuf.append('"');
			}
			else {
				insertBuf.append(tableName);
			}
			insertBuf.append(" (").append(columnsFromAttributesAsArray(attributes, _quoteDestination).componentsJoinedByString(", ")).append(") values (");
			for (int i = columnNames.length; i-- > 0;) {
				insertBuf.append('?');
				if (i > 0) {
					insertBuf.append(", ");
				}
			}
			insertBuf.append(");");

			String insertSql = insertBuf.toString();
			System.out.println("CopyTask.copyEntity: " + insertSql);
			PreparedStatement upps = _dest.prepareStatement(insertSql);

			ResultSet rows = stmt.executeQuery(sql);
			// transfer each row by first setting the values
			int rowsCount = 0;
			while (rows.next()) {
				rowsCount++;
				if (rows.getRow() % 1000 == 0) {
					System.out.println("CopyTask.copyEntity: table " + tableName + ", inserted " + rows.getRow() + " rows");
					log.info("table " + tableName + ", inserted " + rows.getRow() + " rows");
				}

				NSMutableSet<File> tempfilesToDelete = new NSMutableSet<File>();
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
						} finally {
							try { bis.close(); } catch (IOException e) {}
						}
						FileInputStream fis = null;
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
						} finally {
							if (fis != null) {
								try { fis.close(); } catch (IOException e) {}
							}
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
		StringBuilder b = new StringBuilder();
		b.append("TIMESTAMP '").append(TIMESTAMP_FORMATTER.format(t)).append('\'');
		return b.toString();
	}

	/**
	 * Copies all rows from one database to another database. The tables must
	 * exist before calling this method.
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
	 * @param m the model that defines the database to copy
	 * 
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
	 *            readOnly or not, default is false. Its a good
	 *            <li>quote, a Boolean defining if the table and field names
	 *            should be "quoted" idea to make the sourceDict readOnly,
	 *            because one does not write.
	 *            </ol>
	 * @param destDict
	 *            same as sourceDict just used for the destination database.
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
	 * @see #_copyDatabaseDefinedByEOModelAndConnectionDictionaryToDatabaseWithConnectionDictionary(EOModel, NSDictionary, NSDictionary)
	 * @param modelGroup
	 *            the model group to copy
	 * @param sourceDict
	 *            the source connection dictionary
	 * @param destDict
	 *            the destination connection dictionary
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
	 * Returns an adaptor channel with the given username and password.
	 * 
	 * @param model the model to base this connection off of
	 * @param userName the new username
	 * @param password the new password
	 * @return a new adaptor channel
	 */
	public static EOAdaptorChannel adaptorChannelWithUserAndPassword(EOModel model, String userName, String password) {
		String adaptorName = model.adaptorName();
		NSDictionary connectionDictionary = model.connectionDictionary();
		return ERXJDBCUtilities.adaptorChannelWithUserAndPassword(adaptorName, connectionDictionary, userName, password);
	}

	/**
	 * Returns an adaptor channel with the given username and password.
	 * 
	 * @param adaptorName the name of the adaptor to user
	 * @param originalConnectionDictionary the original connection dictionary
	 * @param userName the new username
	 * @param password the new password
	 * @return a new adaptor channel
	 */
	public static EOAdaptorChannel adaptorChannelWithUserAndPassword(String adaptorName, NSDictionary originalConnectionDictionary, String userName, String password) {
		EOAdaptor adaptor = EOAdaptor.adaptorWithName(adaptorName);
		NSMutableDictionary newConnectionDictionary = originalConnectionDictionary.mutableClone();
		if (userName == null) {
			newConnectionDictionary.removeObjectForKey(JDBCAdaptor.UsernameKey);
		}
		else {
			newConnectionDictionary.setObjectForKey(userName, JDBCAdaptor.UsernameKey);
		}
		if (password == null) {
			newConnectionDictionary.removeObjectForKey(JDBCAdaptor.PasswordKey);
		}
		else {
			newConnectionDictionary.setObjectForKey(password, JDBCAdaptor.PasswordKey);
		}
		adaptor.setConnectionDictionary(newConnectionDictionary);
		return adaptor.createAdaptorContext().createAdaptorChannel();
	}

	/**
	 * Shortcut to java.sql.Statement.executeUpdate(..) that operates on an
	 * EOAdaptorChannel.
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
		return ERXJDBCUtilities.executeUpdate(channel, sql, false);
	}

	/**
	 * Shortcut to java.sql.Statement.executeUpdate(..) that operates on an
	 * EOAdaptorChannel. and optionally commits.
	 * 
	 * @param channel
	 *            the JDBCChannel to work with
	 * @param sql
	 *            the sql to execute
	 * @param autoCommit if true, autocommit the connection after executing
	 * @return the number of rows updated
	 * @throws SQLException
	 *             if there is a problem
	 */
	public static int executeUpdate(EOAdaptorChannel channel, String sql, boolean autoCommit) throws SQLException {
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
				if(autoCommit) {
					conn.commit();
				}
			}
			catch(SQLException ex) {
				if(autoCommit) {
					conn.rollback();
				}
				throw new RuntimeException("Failed to execute the statement '" + sql + "'.", ex);
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
	 * Splits the given sqlscript and executes each of the statements in a
	 * single transaction
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
		return ERXJDBCUtilities.executeUpdateScript(channel, sqlScript, false);
	}

	/**
	 * Splits the given sqlscript and executes each of the statements in a
	 * single transaction
	 * 
	 * @param channel
	 *            the JDBCChannel to work with
	 * @param sqlScript
	 *            the sql script to execute
	 * @param ignoreFailures if true, failures in a particular statement are ignored
	 * @return the number of rows updated
	 * @throws SQLException
	 *             if there is a problem
	 */
	public static int executeUpdateScript(EOAdaptorChannel channel, String sqlScript, boolean ignoreFailures) throws SQLException {
		NSArray<String> sqlStatements = ERXSQLHelper.newSQLHelper(channel).splitSQLStatements(sqlScript);
		return ERXJDBCUtilities.executeUpdateScript(channel, sqlStatements, ignoreFailures);
	}

	/**
	 * Splits the given sqlscript and executes each of the statements in a
	 * single transaction
	 * 
	 * @param channel
	 *            the JDBCChannel to work with
	 * @param sqlStatements
	 *            the array of sql scripts to execute
	 * 
	 * @return the number of rows updated
	 * @throws SQLException
	 *             if there is a problem
	 */
	public static int executeUpdateScript(EOAdaptorChannel channel, NSArray<String> sqlStatements) throws SQLException {
		return executeUpdateScript(channel, sqlStatements, false);
	}
	
	/**
	 * Splits the given sqlscript and executes each of the statements in a
	 * single transaction
	 * 
	 * @param channel
	 *            the JDBCChannel to work with
	 * @param sqlStatements
	 *            the array of sql scripts to execute
	 * @param ignoreFailures if true, failures in a particular statement are ignored
	 * 
	 * @return the number of rows updated
	 * @throws SQLException
	 *             if there is a problem
	 */
	public static int executeUpdateScript(EOAdaptorChannel channel, NSArray<String> sqlStatements, boolean ignoreFailures) throws SQLException {
		EOAdaptorContext adaptorContext = channel.adaptorContext();
		// MS: Hack to support memory migrations
		if (!(adaptorContext instanceof JDBCContext)) {
			return 0;
		}

		ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(channel);
		int rowsUpdated = 0;
		boolean wasOpen = channel.isOpen();
		if (!wasOpen) {
			channel.openChannel();
		}
		Connection conn = ((JDBCContext) adaptorContext).connection();
		try {
			Statement stmt = conn.createStatement();
			try {
				Enumeration<String> sqlStatementsEnum = sqlStatements.objectEnumerator();
				while (sqlStatementsEnum.hasMoreElements()) {
					String sql = sqlStatementsEnum.nextElement();
					if (sqlHelper.shouldExecute(sql)) {
						if (ERXJDBCUtilities.log.isInfoEnabled()) {
							ERXJDBCUtilities.log.info("Executing " + sql);
						}
						try {
							rowsUpdated += stmt.executeUpdate(sql);
						}
						catch (Throwable t) {
							if (!ignoreFailures) {
								throw new RuntimeException("Failed to execute '" + sql + "'.", t);
							}
							ERXJDBCUtilities.log.warn("Failed to execute '" + sql + "', but ignoring: " + ERXExceptionUtilities.toParagraph(t));
						}
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
	 * Runs a given sql script and executes each of the statements in a
	 * one transaction.
	 * 
	 * @param channel
	 *            the JDBCChannel to work with
	 * @param script
	 *            the array of sql scripts to execute
	 * 
	 * @return the number of rows updated
	 * @throws SQLException
	 *             if there is a problem
	 * @deprecated use {@link #executeUpdateScript(EOAdaptorChannel, String, boolean)}
	 */
    @Deprecated
	public static int executeUpdateScriptIgnoringErrors(EOAdaptorChannel channel, String script) throws SQLException {
		return ERXJDBCUtilities.executeUpdateScript(channel, script, true);
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
	@SuppressWarnings("unchecked")
	public static int executeUpdateScriptFromResourceNamed(EOAdaptorChannel channel, String resourceName, String frameworkName) throws SQLException, IOException {
		ERXJDBCUtilities.log.info("Executing SQL script '" + resourceName + "' from " + frameworkName + " ...");
		InputStream sqlScript = WOApplication.application().resourceManager().inputStreamForResourceNamed(resourceName, frameworkName, NSArray.EmptyArray);
		if (sqlScript == null) {
			throw new IllegalArgumentException("There is no resource named '" + resourceName + "'.");
		}
		NSArray<String> sqlStatements;
		try {
			sqlStatements = ERXSQLHelper.newSQLHelper(channel).splitSQLStatementsFromInputStream(sqlScript);
		}
		finally {
			sqlScript.close();
		}
		return ERXJDBCUtilities.executeUpdateScript(channel, sqlStatements);
	}

	/**
	 * Drops tables, primary keys, and foreign keys for the tables in the
	 * given model.
	 * 
	 * @param channel
	 *            the channel to use for execution
	 * @param model
	 *            the model to drop tables for
	 * @param ignoreFailures if true, failures in a particular statement are ignored
	 * @throws SQLException
	 *             if something fails
	 */
	public static void dropTablesForModel(EOAdaptorChannel channel, EOModel model, boolean ignoreFailures) throws SQLException {
		ERXJDBCUtilities.dropTablesForEntities(channel, model.entities(), ignoreFailures);
	}

	/**
	 * Drops tables, primary keys, and foreign keys for the given list of
	 * entities. This is useful in your Migration #0 class.
	 * 
	 * @param channel
	 *            the channel to use for execution
	 * @param entities
	 *            the entities to drop tables for
	 * @param ignoreFailures if true, failures in a particular statement are ignored
	 * @throws SQLException
	 *             if something fails
	 */
	public static void dropTablesForEntities(EOAdaptorChannel channel, NSArray<EOEntity> entities, boolean ignoreFailures) throws SQLException {
		ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(channel);
		String sqlScript = sqlHelper.createSchemaSQLForEntitiesWithOptions(entities, channel.adaptorContext().adaptor(), sqlHelper.defaultOptionDictionary(false, true));
		ERXJDBCUtilities.executeUpdateScript(channel, sqlScript, ignoreFailures);
	}

	/**
	 * Creates tables, primary keys, and foreign keys for the tables in the
	 * given model. This is useful in your Migration #0 class.
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
	 * Creates tables, primary keys, and foreign keys for the given list of
	 * entities. This is useful in your Migration #0 class.
	 * 
	 * @param channel
	 *            the channel to use for execution
	 * @param entities
	 *            the entities to create tables for
	 * @throws SQLException
	 *             if something fails
	 */
	public static void createTablesForEntities(EOAdaptorChannel channel, NSArray<EOEntity> entities) throws SQLException {
		ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(channel);
		String sqlScript = sqlHelper.createSchemaSQLForEntitiesWithOptions(entities, channel.adaptorContext().adaptor(), sqlHelper.defaultOptionDictionary(true, false));
		ERXJDBCUtilities.executeUpdateScript(channel, sqlScript);
	}

	/**
	 * Returns the name of the database product for the given channel (handy
	 * when loading database-vendor-specific sql scripts in migrations).
	 * 
	 * @param channel
	 *            the channel
	 * @return the database the database product name ("FrontBase",
	 *         "PostgreSQL")
	 */
	public static String databaseProductName(EOAdaptorChannel channel) {
		return ((JDBCAdaptor) channel.adaptorContext().adaptor()).plugIn().databaseProductName();
	}

	/**
	 * Returns the name of the database product for the given an eomodel (handy
	 * when loading database-vendor-specific sql scripts in migrations).
	 * 
	 * @param model
	 *            the EOModel
	 * @return the database the database product name ("FrontBase",
	 *         "PostgreSQL")
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

	/**
	 * Using the backing connection from the adaptor context, executes the given
	 * query and calls delegate.processConnection(conn) for the Connection. This 
	 * handles properly closing all the underlying JDBC resources.
	 * 
	 * @param adaptorChannel
	 *            the adaptor channel
	 * @param delegate
	 *            the connection delegate
	 * @throws Exception
	 *             if something goes wrong
	 */
	public static void processConnection(EOAdaptorChannel adaptorChannel, IConnectionDelegate delegate) throws Exception {
		boolean wasOpen = adaptorChannel.isOpen();
		if (!wasOpen) {
			adaptorChannel.openChannel();
		}
		try {
			Connection conn = ((JDBCContext) adaptorChannel.adaptorContext()).connection();
			delegate.processConnection(adaptorChannel, conn);
		}
		finally {
			if (!wasOpen) {
				adaptorChannel.closeChannel();
			}
		}
	}

	/**
	 * Using the backing connection from the adaptor context, executes the given
	 * query and calls delegate.processResultSet(rs) once for the ResultSet. This 
	 * handles properly closing all the underlying JDBC resources.
	 * 
	 * @param adaptorChannel
	 *            the adaptor channel
	 * @param query
	 *            the query to execute
	 * @param delegate
	 *            the processor delegate
	 * @throws Exception
	 *             if something goes wrong
	 */
	public static void executeQuery(EOAdaptorChannel adaptorChannel, final String query, final IResultSetDelegate delegate) throws Exception {
		ERXJDBCUtilities.processConnection(adaptorChannel, new IConnectionDelegate() {
			public void processConnection(EOAdaptorChannel innerAdaptorChannel, Connection conn) throws Exception {
				Statement stmt = conn.createStatement();
				try {
					ResultSet rs = stmt.executeQuery(query);
					try {
						delegate.processResultSet(innerAdaptorChannel, rs);
					}
					finally {
						rs.close();
					}
				}
				finally {
					stmt.close();
				}
			}
		});
	}

	/**
	 * Using the backing connection from the adaptor context, executes the given
	 * query and calls processor.process(rs) for each row of the ResultSet. This 
	 * handles properly closing all the underlying JDBC resources.
	 * 
	 * @param adaptorChannel
	 *            the adaptor channel
	 * @param query
	 *            the query to execute
	 * @param delegate
	 *            the processor delegate
	 * @throws Exception
	 *             if something goes wrong
	 */
	public static void processResultSetRows(EOAdaptorChannel adaptorChannel, String query, final IResultSetDelegate delegate) throws Exception {
		ERXJDBCUtilities.executeQuery(adaptorChannel, query, new IResultSetDelegate() {
			public void processResultSet(EOAdaptorChannel innerAdaptorChannel, ResultSet rs) throws Exception {
				while (rs.next()) {
					delegate.processResultSet(innerAdaptorChannel, rs);
				}
			}
		});
	}

	/**
	 * Using the backing connection from the adaptor context, executes the given
	 * query and returns a CachedRowSet of the results. This
	 * can be useful for more complicated migrations. This handles properly
	 * closing all the underlying JDBC resources.
	 * 
	 * @param adaptorChannel
	 *            the adaptor channel
	 * @param query
	 *            the query to execute
	 * @return a CachedRowSet of the results
	 * @throws Exception
	 *             if something goes wrong
	 */
	public static CachedRowSet fetchRowSet(EOAdaptorChannel adaptorChannel, String query) throws Exception {
		final CachedRowSetImpl rowSet = new CachedRowSetImpl();
		ERXJDBCUtilities.executeQuery(adaptorChannel, query, new IResultSetDelegate() {
			public void processResultSet(EOAdaptorChannel innerAdaptorChannel, ResultSet rs) throws Exception {
				rowSet.populate(rs);
			}
		});
		return rowSet;
	}

	/**
	 * IConnectionDelegate is like a closure for connection operations.
	 * 
	 * @author mschrag
	 */
	public static interface IConnectionDelegate {
		/**
		 * This method is called to give you the opportunity to process a Connection.
		 * 
		 * @param adaptorChannel
		 *            the original adaptor channel
		 * @param conn
		 *            the JDBC Connection
		 * @throws Exception
		 *             if something goes wrong
		 */
		public void processConnection(EOAdaptorChannel adaptorChannel, Connection conn) throws Exception;
	}

	/**
	 * IResultSetDelegate is like a closure for ResultSet operations.
	 * 
	 * @author mschrag
	 */
	public static interface IResultSetDelegate {
		/**
		 * This method is called to give you the opportunity to process a ResultSet or a
		 * row of a ResultSet (depending on the context).
		 * 
		 * @param adaptorChannel
		 *            the original adaptor channel
		 * @param rs
		 *            the ResultSet
		 * @throws Exception
		 *             if something goes wrong
		 */
		public void processResultSet(EOAdaptorChannel adaptorChannel, ResultSet rs) throws Exception;
	}
}
