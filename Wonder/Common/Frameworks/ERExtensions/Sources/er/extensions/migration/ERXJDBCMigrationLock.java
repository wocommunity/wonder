package er.extensions.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.jdbcadaptor.JDBCChannel;
import com.webobjects.jdbcadaptor.JDBCContext;

import er.extensions.ERXProperties;

/**
 * JDBC implementation of the migration lock.
 * 
 * @property er.migration.JDBC.dbUpdaterTableName the name of the db update
 *           version table (defaults to _DBUpdater)
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
							ensureModelRowExists(resultSet.next(), model);
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
			throw new ERXMigrationFailedException("Failed to lock " + _dbUpdaterTableName + " table.  It might be missing? Try executing:\n" + dbUpdaterCreateStatement(model) + ".", e);
		}
	}

	protected EOModel dbUpdaterModelWithModel(EOModel model) {
		EOModel dbUpdateModel = new EOModel();
		dbUpdateModel.setConnectionDictionary(model.connectionDictionary());
		dbUpdateModel.setAdaptorName(model.adaptorName());
		EOAdaptor adaptor = EOAdaptor.adaptorWithModel(model);

		EOEntity dbUpdaterEntity = new EOEntity();
		dbUpdaterEntity.setExternalName(_dbUpdaterTableName);
		dbUpdaterEntity.setName(_dbUpdaterTableName);
		dbUpdateModel.addEntity(dbUpdaterEntity);

		EOAttribute modelNameAttribute = new EOAttribute();
		modelNameAttribute.setName("ModelName");
		modelNameAttribute.setColumnName("ModelName");
		modelNameAttribute.setClassName("java.lang.String");
		modelNameAttribute.setWidth(100);
		modelNameAttribute.setAllowsNull(false);
		adaptor.assignExternalTypeForAttribute(modelNameAttribute);
		dbUpdaterEntity.addAttribute(modelNameAttribute);

		EOAttribute versionAttribute = new EOAttribute();
		versionAttribute.setName("Version");
		versionAttribute.setColumnName("Version");
		versionAttribute.setClassName("java.lang.Number");
		versionAttribute.setAllowsNull(false);
		adaptor.assignExternalTypeForAttribute(versionAttribute);
		dbUpdaterEntity.addAttribute(versionAttribute);

		EOAttribute updateLockAttribute = new EOAttribute();
		updateLockAttribute.setName("UpdateLock");
		updateLockAttribute.setColumnName("UpdateLock");
		updateLockAttribute.setClassName("java.lang.Number");
		updateLockAttribute.setAllowsNull(false);
		adaptor.assignExternalTypeForAttribute(updateLockAttribute);
		dbUpdaterEntity.addAttribute(updateLockAttribute);

		EOAttribute lockOwnerAttribute = new EOAttribute();
		lockOwnerAttribute.setName("LockOwner");
		lockOwnerAttribute.setColumnName("LockOwner");
		lockOwnerAttribute.setClassName("java.lang.String");
		lockOwnerAttribute.setWidth(100);
		lockOwnerAttribute.setAllowsNull(true);
		adaptor.assignExternalTypeForAttribute(lockOwnerAttribute);
		dbUpdaterEntity.addAttribute(lockOwnerAttribute);

		return dbUpdateModel;
	}

	protected String dbUpdaterCreateStatement(EOModel model) {
		EOModel dbUpdaterModel = dbUpdaterModelWithModel(model);
		NSMutableDictionary flags = new NSMutableDictionary();
		flags.setObjectForKey("NO", EOSchemaGeneration.DropTablesKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.DropPrimaryKeySupportKey);
		flags.setObjectForKey("YES", EOSchemaGeneration.CreateTablesKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.CreatePrimaryKeySupportKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.PrimaryKeyConstraintsKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.ForeignKeyConstraintsKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.CreateDatabaseKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.DropDatabaseKey);
		String createTableScript = EOAdaptor.adaptorWithModel(dbUpdaterModel).synchronizationFactory().schemaCreationScriptForEntities(new NSArray(dbUpdaterModel.entityNamed(_dbUpdaterTableName)), flags);
		return createTableScript;
	}

	protected String dbUpdaterInsertStatement(EOModel model, Integer version, Integer updateLock, String lockOwnerName) {
		EOModel dbUpdaterModel = dbUpdaterModelWithModel(model);
		NSMutableDictionary row = new NSMutableDictionary();
		row.setObjectForKey(model.name(), "ModelName");
		row.setObjectForKey(updateLock, "UpdateLock");
		row.setObjectForKey(version, "Version");
		if (lockOwnerName != null) {
			row.setObjectForKey(lockOwnerName, "LockOwner");
		}
		EOSQLExpression insertExpression = EOAdaptor.adaptorWithModel(dbUpdaterModel).expressionFactory().insertStatementForRow(row, dbUpdaterModel.entityNamed(_dbUpdaterTableName));
		return insertExpression.statement();
	}

	protected void ensureModelRowExists(boolean exists, EOModel model) {
		if (!exists) {
			throw new ERXMigrationFailedException("Unable to migrate because there is not a row for the model '" + model.name() + ".  Please execute:\n" + dbUpdaterInsertStatement(model, new Integer(-1), new Integer(0), null));
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
				ensureModelRowExists(count != 0, model);
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