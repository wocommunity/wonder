package er.extensions.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.jdbcadaptor.JDBCChannel;
import com.webobjects.jdbcadaptor.JDBCContext;

import er.extensions.ERXProperties;

/**
 * JDBC implementation of the migration lock.
 *
 * @property er.migration.JDBC.dbUpdaterTableName the name of the db update version table (defaults to _DBUpdater)
 * 
 * @author mschrag
 */
public class ERXJDBCMigrationLock implements IERXMigrationLock {
	public static final Logger log = Logger.getLogger(ERXJDBCMigrationLock.class);

	private String _dbUpdaterTableName;
	
	public ERXJDBCMigrationLock() {
		_dbUpdaterTableName = ERXProperties.stringForKeyWithDefault("er.migration.JDBC.dbUpdaterTableName", "_DBUpdater");
	}

	public boolean tryLock(EOAdaptorChannel channel, EOModel model, String lockOwnerName) {
		try {
			int count;
			JDBCChannel jdbcChannel = (JDBCChannel) channel;
			boolean wasOpen = true;
			if (!jdbcChannel.isOpen()) {
				jdbcChannel.openChannel();
				wasOpen = false;
			}
			try {
				JDBCContext context = (JDBCContext) channel.adaptorContext();
				Connection connection = context.connection();
				Statement statement = connection.createStatement();
				try {
					count = statement.executeUpdate("update \"" + _dbUpdaterTableName + "\" set \"UpdateLock\" = 1, \"LockOwner\" = '" + lockOwnerName + "' where \"ModelName\" = '" + model.name() + "' and (\"UpdateLock\" = 0 or \"LockOwner\" = '" + lockOwnerName + "')");
					if (count == 0) {
						ResultSet resultSet = statement.executeQuery("select \"UpdateLock\" from \"" + _dbUpdaterTableName + "\" where \"ModelName\" = '" + model.name() + "'");
						try {
							ensureModelRowExists(model, resultSet.next() ? 1 : 0);
						}
						finally {
							resultSet.close();
						}
						if (ERXJDBCMigrationLock.log.isInfoEnabled()) {
							ERXJDBCMigrationLock.log.info("Waiting on UpdateLock| for model '" + model.name() + "' ...");
						}
					}
				}
				finally {
					statement.close();
				}
				if (count > 0 && !connection.getAutoCommit()) {
					connection.commit();
				}
			}
			finally {
				if (!wasOpen) {
					jdbcChannel.closeChannel();
				}
			}
			return count == 1;
		}
		catch (ERXMigrationFailedException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ERXMigrationFailedException("Failed to lock " + _dbUpdaterTableName + " table.  It might be missing?\nTry executing: " + dbUpdaterCreateStatement() + ".", e);
		}
	}

	protected String dbUpdaterCreateStatement() {
		StringBuffer createStatementBuffer = new StringBuffer();
		createStatementBuffer.append("create table \"" + _dbUpdaterTableName + "\" (");
		createStatementBuffer.append("\"ModelName\" varchar(100) not null, ");
		createStatementBuffer.append("\"Version\" integer not null, ");
		createStatementBuffer.append("\"UpdateLock\" integer not null, ");
		createStatementBuffer.append("\"LockOwner\" varchar(100)");
		createStatementBuffer.append(")");
		return createStatementBuffer.toString();
	}

	protected void ensureModelRowExists(EOModel model, int count) {
		if (count == 0) {
			throw new ERXMigrationFailedException("Unable to migrate because there is not a row for the model '" + model.name() + ".  Please execute:\ninsert into \"" + _dbUpdaterTableName + "\"(\"ModelName\", \"Version\", \"UpdateLock\", \"LockOwner\") values ('" + model.name() + "', -1, 0, NULL)");
		}
	}

	public void unlock(EOAdaptorChannel channel, EOModel model) {
		JDBCChannel jdbcChannel = (JDBCChannel) channel;
		boolean wasOpen = true;
		if (!jdbcChannel.isOpen()) {
			jdbcChannel.openChannel();
			wasOpen = false;
		}
		try {
			JDBCContext context = (JDBCContext) channel.adaptorContext();
			Connection connection = context.connection();
			Statement statement = connection.createStatement();
			try {
				statement.executeUpdate("update \"" + _dbUpdaterTableName + "\" set \"UpdateLock\" = 0, \"LockOwner\" = NULL where \"ModelName\" = '" + model.name() + "'");
			}
			finally {
				statement.close();
			}
			if (!connection.getAutoCommit()) {
				connection.commit();
			}
		}
		catch (Exception e) {
			throw new ERXMigrationFailedException("Failed to unlock " + _dbUpdaterTableName + " table.", e);
		}
		finally {
			if (!wasOpen) {
				jdbcChannel.closeChannel();
			}
		}
	}

	public int versionNumber(EOAdaptorChannel channel, EOModel model) {
		int version;
		JDBCChannel jdbcChannel = (JDBCChannel) channel;
		boolean wasOpen = true;
		if (!jdbcChannel.isOpen()) {
			jdbcChannel.openChannel();
			wasOpen = false;
		}
		try {
			JDBCContext context = (JDBCContext) channel.adaptorContext();
			Connection connection = context.connection();
			Statement statement = connection.createStatement();
			try {
				ResultSet results = statement.executeQuery("select \"Version\" from \"" + _dbUpdaterTableName + "\" where \"ModelName\" = '" + model.name() + "'");
				try {
					if (results.next()) {
						version = results.getInt(1);
					}
					else {
						version = -1;
					}
				}
				finally {
					results.close();
				}
			}
			finally {
				statement.close();
			}
		}
		catch (Exception e) {
			throw new ERXMigrationFailedException("Failed to get version number from " + _dbUpdaterTableName + " table.", e);
		}
		finally {
			if (!wasOpen) {
				jdbcChannel.closeChannel();
			}
		}
		return version;
	}

	public void setVersionNumber(EOAdaptorChannel channel, EOModel model, int versionNumber) {
		JDBCChannel jdbcChannel = (JDBCChannel) channel;
		boolean wasOpen = true;
		if (!jdbcChannel.isOpen()) {
			jdbcChannel.openChannel();
			wasOpen = false;
		}
		try {
			JDBCContext context = (JDBCContext) channel.adaptorContext();
			Connection connection = context.connection();
			Statement statement = connection.createStatement();
			try {
				int count = statement.executeUpdate("update \"" + _dbUpdaterTableName + "\" set \"Version\" = " + versionNumber + " where \"ModelName\" = '" + model.name() + "'");
				ensureModelRowExists(model, count);
			}
			finally {
				statement.close();
			}
			if (!connection.getAutoCommit()) {
				connection.commit();
			}
		}
		catch (Exception e) {
			throw new ERXMigrationFailedException("Failed to set version number of " + _dbUpdaterTableName + ".", e);
		}
		finally {
			if (!wasOpen) {
				jdbcChannel.closeChannel();
			}
		}
	}
}