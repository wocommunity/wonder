package er.extensions.migration;

import java.sql.Types;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

import er.extensions.ERXJDBCUtilities;
import er.extensions.ERXProperties;

/**
 * JDBC implementation of the migration lock.
 * 
 * @property er.migration.JDBC.dbUpdaterTableName the name of the db update version table (defaults to _DBUpdater)
 * @property er.migration.createTablesIfNecessary if true, the tables and model rows will be created automatically.
 *           *ONLY SET THIS IF YOU ARE RUNNING IN DEVELOPMENT MODE OR WITH A SINGLE INSTANCE*. If you are running
 *           multiple instances, the instances will not be able to acquire locks properly and you may end up with
 *           multiple instances attempting to create lock tables and/or failing to startup properly.
 * @property <ModelName>.InitialMigrationVersion the starting version number (in case you are retrofitting a project
 *           with migrations)
 * @author mschrag
 */
public class ERXJDBCMigrationLock implements IERXMigrationLock {
	public static final Logger log = Logger.getLogger(ERXJDBCMigrationLock.class);

	private String _dbUpdaterTableName;
	private EOModel _lastUpdatedModel;
	private EOModel _dbUpdaterModelCache;

	public ERXJDBCMigrationLock() {
		_dbUpdaterTableName = ERXProperties.stringForKeyWithDefault("er.migration.JDBC.dbUpdaterTableName", "_dbupdater");
	}

	protected boolean createIfMissing() {
		return ERXProperties.booleanForKeyWithDefault("er.migration.createTablesIfNecessary", false);
	}

	public boolean tryLock(EOAdaptorChannel channel, EOModel model, String lockOwnerName) {
		return _tryLock(channel, model, lockOwnerName, createIfMissing());
	}

	public boolean _tryLock(EOAdaptorChannel channel, EOModel model, String lockOwnerName, boolean createTableIfMissing) {
		JDBCAdaptor adaptor = (JDBCAdaptor)channel.adaptorContext().adaptor();
		try {
			int count;
			boolean wasOpen = true;
			if (!channel.isOpen()) {
				channel.openChannel();
				wasOpen = false;
			}
			try {
				EOModel dbUpdaterModel = dbUpdaterModelWithModel(model, adaptor);
				NSMutableDictionary<String, Object> row = new NSMutableDictionary<String, Object>();
				row.setObjectForKey(new Integer(1), "updateLock");
				row.setObjectForKey(lockOwnerName, "lockOwner");
				EOEntity dbUpdaterEntity = dbUpdaterModel.entityNamed(_dbUpdaterTableName);
				count = channel.updateValuesInRowsDescribedByQualifier(row, EOQualifier.qualifierWithQualifierFormat("modelName = '" + model.name() + "' and (updateLock = 0 or lockOwner = '" + lockOwnerName + "')", null), dbUpdaterEntity);
				channel.cancelFetch();
				if (count == 0) {
					EOFetchSpecification fetchSpec = new EOFetchSpecification(_dbUpdaterTableName, new EOKeyValueQualifier("modelName", EOQualifier.QualifierOperatorEqual, model.name()), null);
					channel.selectAttributes(new NSArray<EOAttribute>(dbUpdaterEntity.attributeNamed("updateLock")), fetchSpec, false, dbUpdaterEntity);
					NSDictionary nextRow;
					try {
						nextRow = channel.fetchRow();
					}
					finally {
						channel.cancelFetch();
					}
					if (nextRow == null) {
						if (createIfMissing()) {
							String modelStatement = dbUpdaterInsertStatement(model, adaptor, new Integer(initialVersionForModel(model)), new Integer(1), lockOwnerName);
							count = ERXJDBCUtilities.executeUpdateScript(channel, modelStatement);
						}
						else {
							String modelStatement = dbUpdaterInsertStatement(model, adaptor, new Integer(initialVersionForModel(model)), new Integer(0), null);
							throw new ERXMigrationFailedException("Unable to migrate because there is not a row for the model '" + model.name() + ".  Please execute:\n" + modelStatement);
						}
					}
					if (ERXJDBCMigrationLock.log.isInfoEnabled()) {
						ERXJDBCMigrationLock.log.info("Waiting on updateLock for model '" + model.name() + "' ...");
					}
				}
				channel.adaptorContext().commitTransaction();
				channel.adaptorContext().beginTransaction();
			}
			finally {
				if (!wasOpen) {
					channel.closeChannel();
				}
			}
			return count == 1;
		}
		catch (ERXMigrationFailedException e) {
			throw e;
		}
		catch (Exception e) {
			channel.adaptorContext().rollbackTransaction();
			String createTableStatement = dbUpdaterCreateStatement(model, adaptor);
			if (createTableIfMissing) {
				try {
					log.warn("Failed to lock.  Attempting to create the table and lock again.", e);
					ERXJDBCUtilities.executeUpdateScript(channel, createTableStatement);
					return _tryLock(channel, model, lockOwnerName, false);
				}
				catch (Throwable t) {
					log.warn("The original reason tryLock failed was: ", e);
					throw new ERXMigrationFailedException("Failed to create lock table. Try executing:\n" + createTableStatement + ".", t);
				}
			}
			throw new ERXMigrationFailedException("Failed to lock " + _dbUpdaterTableName + " table.  It might be missing? Try executing:\n" + createTableStatement + ".", e);
		}
	}

	public void unlock(EOAdaptorChannel channel, EOModel model) {
		boolean wasOpen = true;
		if (!channel.isOpen()) {
			channel.openChannel();
			wasOpen = false;
		}
		try {
			JDBCAdaptor adaptor = (JDBCAdaptor)channel.adaptorContext().adaptor();
			EOModel dbUpdaterModel = dbUpdaterModelWithModel(model, adaptor);
			NSMutableDictionary<String, Object> row = new NSMutableDictionary<String, Object>();
			row.setObjectForKey(new Integer(0), "updateLock");
			row.setObjectForKey(NSKeyValueCoding.NullValue, "lockOwner");
			EOEntity dbUpdaterEntity = dbUpdaterModel.entityNamed(_dbUpdaterTableName);
			channel.adaptorContext().commitTransaction();
			channel.updateValuesInRowsDescribedByQualifier(row, new EOKeyValueQualifier("modelName", EOQualifier.QualifierOperatorEqual, model.name()), dbUpdaterEntity);
			channel.cancelFetch();
			channel.adaptorContext().commitTransaction();
			channel.adaptorContext().beginTransaction();
		}
		catch (Exception e) {
			throw new ERXMigrationFailedException("Failed to unlock " + _dbUpdaterTableName + " table.", e);
		}
		finally {
			if (!wasOpen) {
				channel.closeChannel();
			}
		}
	}

	public int versionNumber(EOAdaptorChannel channel, EOModel model) {
		boolean wasOpen = true;
		if (!channel.isOpen()) {
			channel.openChannel();
			wasOpen = false;
		}
		int version;
		try {
			JDBCAdaptor adaptor = (JDBCAdaptor)channel.adaptorContext().adaptor();
			EOModel dbUpdaterModel = dbUpdaterModelWithModel(model, adaptor);
			EOEntity dbUpdaterEntity = dbUpdaterModel.entityNamed(_dbUpdaterTableName);
			EOFetchSpecification fetchSpec = new EOFetchSpecification(_dbUpdaterTableName, new EOKeyValueQualifier("modelName", EOQualifier.QualifierOperatorEqual, model.name()), null);
			channel.selectAttributes(new NSArray<EOAttribute>(dbUpdaterEntity.attributeNamed("version")), fetchSpec, false, dbUpdaterEntity);
			NSDictionary nextRow = channel.fetchRow();
			if (nextRow == null) {
				version = initialVersionForModel(model);
			}
			else {
				Integer versionInteger = (Integer) nextRow.objectForKey("version");
				version = Math.max(versionInteger.intValue(), initialVersionForModel(model));
			}
			channel.cancelFetch();
		}
		catch (Exception e) {
			throw new ERXMigrationFailedException("Failed to get version number from " + _dbUpdaterTableName + " table.", e);
		}
		finally {
			if (!wasOpen) {
				channel.closeChannel();
			}
		}
		return version;
	}

	public void setVersionNumber(EOAdaptorChannel channel, EOModel model, int versionNumber) {
		boolean wasOpen = true;
		if (!channel.isOpen()) {
			channel.openChannel();
			wasOpen = false;
		}
		try {
			JDBCAdaptor adaptor = (JDBCAdaptor)channel.adaptorContext().adaptor();
			EOModel dbUpdaterModel = dbUpdaterModelWithModel(model, adaptor);
			NSMutableDictionary<String, Object> row = new NSMutableDictionary<String, Object>();
			row.setObjectForKey(new Integer(versionNumber), "version");
			EOEntity dbUpdaterEntity = dbUpdaterModel.entityNamed(_dbUpdaterTableName);
			int count = channel.updateValuesInRowsDescribedByQualifier(row, new EOKeyValueQualifier("modelName", EOQualifier.QualifierOperatorEqual, model.name()), dbUpdaterEntity);
			channel.cancelFetch();
			if (count == 0) {
				String modelStatement = dbUpdaterInsertStatement(model, adaptor, new Integer(initialVersionForModel(model)), new Integer(0), null);
				throw new ERXMigrationFailedException("Unable to migrate because there is not a row for the model '" + model.name() + ".  Please execute:\n" + modelStatement);
			}
		}
		catch (Exception e) {
			throw new ERXMigrationFailedException("Failed to set version number of " + _dbUpdaterTableName + ".", e);
		}
		finally {
			if (!wasOpen) {
				channel.closeChannel();
			}
		}
	}

	protected int initialVersionForModel(EOModel model) {
		String modelName = model.name();
		int initialVersion = ERXProperties.intForKeyWithDefault(modelName + ".InitialMigrationVersion", -1);
		return initialVersion;
	}

	protected EOModel dbUpdaterModelWithModel(EOModel model, JDBCAdaptor adaptor) {
		EOModel dbUpdaterModel;
		if (_lastUpdatedModel == model) {
			dbUpdaterModel = _dbUpdaterModelCache;
		}
		else {
			dbUpdaterModel = new EOModel();
			dbUpdaterModel.setConnectionDictionary(model.connectionDictionary());
			dbUpdaterModel.setAdaptorName(model.adaptorName());

			EOEntity dbUpdaterEntity = new EOEntity();
			dbUpdaterEntity.setExternalName(_dbUpdaterTableName);
			dbUpdaterEntity.setName(_dbUpdaterTableName);
			dbUpdaterModel.addEntity(dbUpdaterEntity);

			EOAttribute modelNameAttribute = new EOAttribute();
			modelNameAttribute.setName("modelName");
			modelNameAttribute.setColumnName("modelname");
			modelNameAttribute.setClassName("java.lang.String");
			modelNameAttribute.setWidth(100);
			modelNameAttribute.setAllowsNull(false);
			
			modelNameAttribute.setExternalType(adaptor.externalTypeForJDBCType(Types.VARCHAR));
			dbUpdaterEntity.addAttribute(modelNameAttribute);

			EOAttribute versionAttribute = new EOAttribute();
			versionAttribute.setName("version");
			versionAttribute.setColumnName("version");
			versionAttribute.setClassName("java.lang.Number");
			versionAttribute.setExternalType(adaptor.externalTypeForJDBCType(Types.INTEGER));
			versionAttribute.setAllowsNull(false);

			dbUpdaterEntity.addAttribute(versionAttribute);

			EOAttribute updateLockAttribute = new EOAttribute();
			updateLockAttribute.setName("updateLock");
			updateLockAttribute.setColumnName("updatelock");
			updateLockAttribute.setClassName("java.lang.Number");
			updateLockAttribute.setAllowsNull(false);
			updateLockAttribute.setExternalType(adaptor.externalTypeForJDBCType(Types.INTEGER));
			dbUpdaterEntity.addAttribute(updateLockAttribute);

			EOAttribute lockOwnerAttribute = new EOAttribute();
			lockOwnerAttribute.setName("lockOwner");
			lockOwnerAttribute.setColumnName("lockowner");
			lockOwnerAttribute.setClassName("java.lang.String");
			lockOwnerAttribute.setWidth(100);
			lockOwnerAttribute.setAllowsNull(true);
			lockOwnerAttribute.setExternalType(adaptor.externalTypeForJDBCType(Types.VARCHAR));
			dbUpdaterEntity.addAttribute(lockOwnerAttribute);

			_lastUpdatedModel = model;
			_dbUpdaterModelCache = dbUpdaterModel;
		}
		return dbUpdaterModel;
	}

	protected String dbUpdaterCreateStatement(EOModel model, JDBCAdaptor adaptor) {
		EOModel dbUpdaterModel = dbUpdaterModelWithModel(model, adaptor);
		NSMutableDictionary<String, String> flags = new NSMutableDictionary<String, String>();
		flags.setObjectForKey("NO", EOSchemaGeneration.DropTablesKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.DropPrimaryKeySupportKey);
		flags.setObjectForKey("YES", EOSchemaGeneration.CreateTablesKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.CreatePrimaryKeySupportKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.PrimaryKeyConstraintsKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.ForeignKeyConstraintsKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.CreateDatabaseKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.DropDatabaseKey);
		String createTableScript = adaptor.synchronizationFactory().schemaCreationScriptForEntities(new NSArray<EOEntity>(dbUpdaterModel.entityNamed(_dbUpdaterTableName)), flags);
		return createTableScript;
	}

	protected String dbUpdaterInsertStatement(EOModel model, JDBCAdaptor adaptor, Integer version, Integer updateLock, String lockOwnerName) {
		EOModel dbUpdaterModel = dbUpdaterModelWithModel(model, adaptor);
		NSMutableDictionary<String, Object> row = new NSMutableDictionary<String, Object>();
		row.setObjectForKey(model.name(), "modelName");
		row.setObjectForKey(updateLock, "updateLock");
		row.setObjectForKey(version, "version");
		if (lockOwnerName != null) {
			row.setObjectForKey(lockOwnerName, "lockOwner");
		}
		EOSQLExpressionFactory sqlExpressionFactory = adaptor.expressionFactory();
		EOSQLExpression insertExpression = sqlExpressionFactory.expressionForEntity(dbUpdaterModel.entityNamed(_dbUpdaterTableName));
		insertExpression.setUseAliases(false);
		insertExpression.setUseBindVariables(false);
		insertExpression.prepareInsertExpressionWithRow(row);
		return insertExpression.statement();
	}
}