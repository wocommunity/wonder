package er.extensions.migration;

import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

import er.extensions.eof.ERXModelGroup;
import er.extensions.foundation.ERXProperties;
import er.extensions.jdbc.ERXJDBCUtilities;
import er.extensions.jdbc.ERXSQLHelper;

/**
 * JDBC implementation of the migration lock.
 * 
 * @property er.migration.JDBC.dbUpdaterTableName the name of the db update
 *           version table (defaults to _DBUpdater)
 * @property er.migration.createTablesIfNecessary if true, the tables and model
 *           rows will be created automatically. *ONLY SET THIS IF YOU ARE
 *           RUNNING IN DEVELOPMENT MODE OR WITH A SINGLE INSTANCE*. If you are
 *           running multiple instances, the instances will not be able to
 *           acquire locks properly and you may end up with multiple instances
 *           attempting to create lock tables and/or failing to startup
 *           properly.
 * @property &lt;ModelName&gt;.InitialMigrationVersion the starting version number (in
 *           case you are retrofitting a project with migrations)
 * @author mschrag
 */
public class ERXJDBCMigrationLock implements IERXMigrationLock {
	private static final Logger log = LoggerFactory.getLogger(ERXJDBCMigrationLock.class);

	private EOModel _lastUpdatedModel;
	private EOModel _dbUpdaterModelCache;

	/**
	 * Adds support for overriding the name of the db updater table on a per-database product level.
	 * 
	 * @param adaptor the current jdbc adaptor
	 * @return the name of the dbupdater table
	 */
	protected String migrationTableName(JDBCAdaptor adaptor) {
		String migrationTableName = ERXProperties.stringForKey("er.migration.JDBC.dbUpdaterTableName");
		if (migrationTableName == null) {
			migrationTableName = ERXSQLHelper.newSQLHelper(adaptor).migrationTableName();
		}
		return migrationTableName;
	}

	protected boolean createIfMissing() {
		return ERXProperties.booleanForKeyWithDefault("er.migration.createTablesIfNecessary", false);
	}

	public boolean tryLock(EOAdaptorChannel channel, EOModel model, String lockOwnerName) {
		return _tryLock(channel, model, lockOwnerName, createIfMissing());
	}

	public boolean _tryLock(EOAdaptorChannel channel, EOModel model, String lockOwnerName, boolean createTableIfMissing) {
		JDBCAdaptor adaptor = (JDBCAdaptor) channel.adaptorContext().adaptor();
		try {
			// MS: This forces the models to load their jdbc2Info if they aren't already.  This
			// prevents deadlocks from happening further within migrations when the new migration
			// API is used.
			adaptor.externalTypesWithModel(model);
			
			int count;
			boolean wasOpen = true;
			if (!channel.isOpen()) {
				channel.openChannel();
				wasOpen = false;
			}
			try {
				EOModel dbUpdaterModel = dbUpdaterModelWithModel(model, adaptor);
				NSMutableDictionary<String, Object> row = new NSMutableDictionary<String, Object>();
				row.setObjectForKey(Integer.valueOf(1), "updateLock");
				row.setObjectForKey(lockOwnerName, "lockOwner");
				EOEntity dbUpdaterEntity = dbUpdaterModel.entityNamed(migrationTableName(adaptor));
				try {
					count = channel.updateValuesInRowsDescribedByQualifier(row, EOQualifier.qualifierWithQualifierFormat("modelName = '" + model.name() + "' and (updateLock = 0 or lockOwner = '" + lockOwnerName + "')", null), dbUpdaterEntity);
				}
				finally {
					channel.cancelFetch();
				}
				if (count == 0) {
					EOFetchSpecification fetchSpec = new EOFetchSpecification(migrationTableName(adaptor), new EOKeyValueQualifier("modelName", EOQualifier.QualifierOperatorEqual, model.name()), null);
					NSDictionary nextRow;
					try {
						channel.selectAttributes(new NSArray<EOAttribute>(dbUpdaterEntity.attributeNamed("updateLock")), fetchSpec, false, dbUpdaterEntity);
						nextRow = channel.fetchRow();
					}
					finally {
						channel.cancelFetch();
					}
					if (nextRow == null) {
						if (createIfMissing()) {
							row.setObjectForKey(Integer.valueOf(initialVersionForModel(model)), "version");
							row.setObjectForKey(model.name(), "modelName");
							try {
								channel.insertRow(row, dbUpdaterEntity);
							}
							catch (EOGeneralAdaptorException e) {
								// Assume this is the unique constraint on modelName that failed
								log.info("Exception creating row for model '{}', assuming another process has already added this and has the lock.", model.name(), e);
								return false;
							}
							count = 1;
						}
						else {
							throw new ERXMigrationFailedException("Unable to migrate because there is not a row for the model '" + model.name() + ".");
						}
					}
					log.info("Waiting on updateLock for model '{}' ...", model.name());
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
			channel.adaptorContext().beginTransaction();
			String createTableStatement = dbUpdaterCreateStatement(model, adaptor);
			if (createTableIfMissing) {
				try {
					log.warn("Locking failed, but this might be OK if this is the first time you are running migrations.  If things keep running, it probably worked fine.  The original reason for the failure: ", e);
					ERXJDBCUtilities.executeUpdateScript(channel, createTableStatement);
					return _tryLock(channel, model, lockOwnerName, false);
				}
				catch (Throwable t) {
					//log.warn("The original reason tryLock failed was: ", e);
					throw new ERXMigrationFailedException("Failed to create lock table. Try executing:\n" + createTableStatement + ".", t);
				}
			}
			throw new ERXMigrationFailedException("Failed to lock " + migrationTableName(adaptor) + " table.  It might be missing? Try executing:\n" + createTableStatement + ".", e);
		}
	}

	public void unlock(EOAdaptorChannel channel, EOModel model) {
		JDBCAdaptor adaptor = (JDBCAdaptor) channel.adaptorContext().adaptor();
		boolean wasOpen = true;
		if (!channel.isOpen()) {
			channel.openChannel();
			wasOpen = false;
		}
		try {
			EOModel dbUpdaterModel = dbUpdaterModelWithModel(model, adaptor);
			NSMutableDictionary<String, Object> row = new NSMutableDictionary<String, Object>();
			row.setObjectForKey(Integer.valueOf(0), "updateLock");
			row.setObjectForKey(NSKeyValueCoding.NullValue, "lockOwner");
			EOEntity dbUpdaterEntity = dbUpdaterModel.entityNamed(migrationTableName(adaptor));
			channel.adaptorContext().commitTransaction();
			try {
				channel.updateValuesInRowsDescribedByQualifier(row, new EOKeyValueQualifier("modelName", EOQualifier.QualifierOperatorEqual, model.name()), dbUpdaterEntity);
			}
			finally {
				channel.cancelFetch();
			}
			channel.adaptorContext().commitTransaction();
			channel.adaptorContext().beginTransaction();
		}
		catch (Exception e) {
			throw new ERXMigrationFailedException("Failed to unlock " + migrationTableName(adaptor) + " table.", e);
		}
		finally {
			if (!wasOpen) {
				channel.closeChannel();
			}
		}
	}

	public int versionNumber(EOAdaptorChannel channel, EOModel model) {
		JDBCAdaptor adaptor = (JDBCAdaptor) channel.adaptorContext().adaptor();

		boolean wasOpen = true;
		if (!channel.isOpen()) {
			channel.openChannel();
			wasOpen = false;
		}
		int version;
		try {
			EOModel dbUpdaterModel = dbUpdaterModelWithModel(model, adaptor);
			EOEntity dbUpdaterEntity = dbUpdaterModel.entityNamed(migrationTableName(adaptor));
			EOFetchSpecification fetchSpec = new EOFetchSpecification(migrationTableName(adaptor), new EOKeyValueQualifier("modelName", EOQualifier.QualifierOperatorEqual, model.name()), null);
			try {
				channel.selectAttributes(new NSArray<EOAttribute>(dbUpdaterEntity.attributeNamed("version")), fetchSpec, false, dbUpdaterEntity);
				NSDictionary nextRow = channel.fetchRow();
				if (nextRow == null) {
					version = initialVersionForModel(model);
				}
				else {
					Integer versionInteger = (Integer) nextRow.objectForKey("version");
					version = Math.max(versionInteger.intValue(), initialVersionForModel(model));
				}
			}
			finally {
				channel.cancelFetch();
			}
		}
		catch (Exception e) {
			throw new ERXMigrationFailedException("Failed to get version number from " + migrationTableName(adaptor) + " table.", e);
		}
		finally {
			if (!wasOpen) {
				channel.closeChannel();
			}
		}
		return version;
	}

	public void setVersionNumber(EOAdaptorChannel channel, EOModel model, int versionNumber) {
		JDBCAdaptor adaptor = (JDBCAdaptor) channel.adaptorContext().adaptor();

		boolean wasOpen = true;
		if (!channel.isOpen()) {
			channel.openChannel();
			wasOpen = false;
		}
		try {
			EOModel dbUpdaterModel = dbUpdaterModelWithModel(model, adaptor);
			NSMutableDictionary<String, Object> row = new NSMutableDictionary<String, Object>();
			row.setObjectForKey(Integer.valueOf(versionNumber), "version");
			EOEntity dbUpdaterEntity = dbUpdaterModel.entityNamed(migrationTableName(adaptor));
			int count;
			try {
				count = channel.updateValuesInRowsDescribedByQualifier(row, new EOKeyValueQualifier("modelName", EOQualifier.QualifierOperatorEqual, model.name()), dbUpdaterEntity);
			}
			finally {
				channel.cancelFetch();
			}
			if (count == 0) {
				throw new ERXMigrationFailedException("Unable to migrate because there is not a row for the model '" + model.name() + ".");
			}
		}
		catch (Exception e) {
			throw new ERXMigrationFailedException("Failed to set version number of " + migrationTableName(adaptor) + ".", e);
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
			EOModelGroup modelGroup = model.modelGroup();
			EOEntity prototypeEntity = modelGroup.entityNamed(ERXModelGroup.prototypeEntityNameForModel(model));
			boolean isWonderPrototype = (prototypeEntity != null && prototypeEntity.model().name().equals("erprototypes"));

			dbUpdaterModel = new EOModel();
			dbUpdaterModel.setConnectionDictionary(model.connectionDictionary());
			dbUpdaterModel.setAdaptorName(model.adaptorName());

			EOEntity dbUpdaterEntity = new EOEntity();
			dbUpdaterEntity.setExternalName(migrationTableName(adaptor));
			dbUpdaterEntity.setName(migrationTableName(adaptor));
			dbUpdaterModel.addEntity(dbUpdaterEntity);

			EOAttribute modelNameAttribute = new EOAttribute();
			if (isWonderPrototype) {
				modelNameAttribute.setExternalType(prototypeEntity.attributeNamed("varchar100").externalType());
			}
			else {
				modelNameAttribute.setExternalType(ERXSQLHelper.newSQLHelper(adaptor).externalTypeForJDBCType(adaptor, Types.VARCHAR));
			}
			modelNameAttribute.setName("modelName");
			modelNameAttribute.setColumnName("modelname");
			modelNameAttribute.setClassName("java.lang.String");
			modelNameAttribute.setWidth(100);
			modelNameAttribute.setAllowsNull(false);
			dbUpdaterEntity.addAttribute(modelNameAttribute);
			dbUpdaterEntity.setPrimaryKeyAttributes(new NSArray<EOAttribute>(modelNameAttribute));

			EOAttribute versionAttribute = new EOAttribute();
			if (isWonderPrototype) {
				versionAttribute.setExternalType(prototypeEntity.attributeNamed("intNumber").externalType());
			}
			else {
				versionAttribute.setExternalType(ERXSQLHelper.newSQLHelper(adaptor).externalTypeForJDBCType(adaptor, Types.INTEGER));
			}
			versionAttribute.setName("version");
			versionAttribute.setColumnName("version");
			versionAttribute.setClassName("java.lang.Number");
			versionAttribute.setValueType("i");
			versionAttribute.setAllowsNull(false);
			dbUpdaterEntity.addAttribute(versionAttribute);

			EOAttribute updateLockAttribute = new EOAttribute();
			if (isWonderPrototype) {
				updateLockAttribute.setExternalType(prototypeEntity.attributeNamed("intNumber").externalType());
			}
			else {
				updateLockAttribute.setExternalType(ERXSQLHelper.newSQLHelper(adaptor).externalTypeForJDBCType(adaptor, Types.INTEGER));
			}
			updateLockAttribute.setName("updateLock");
			updateLockAttribute.setColumnName("updatelock");
			updateLockAttribute.setClassName("java.lang.Number");
			updateLockAttribute.setValueType("i");
			updateLockAttribute.setAllowsNull(false);
			dbUpdaterEntity.addAttribute(updateLockAttribute);

			EOAttribute lockOwnerAttribute = new EOAttribute();
			if (isWonderPrototype) {
				lockOwnerAttribute.setExternalType(prototypeEntity.attributeNamed("varchar100").externalType());
			}
			else {
				lockOwnerAttribute.setExternalType(ERXSQLHelper.newSQLHelper(adaptor).externalTypeForJDBCType(adaptor, Types.VARCHAR));
			}
			lockOwnerAttribute.setName("lockOwner");
			lockOwnerAttribute.setColumnName("lockowner");
			lockOwnerAttribute.setClassName("java.lang.String");
			lockOwnerAttribute.setWidth(100);
			lockOwnerAttribute.setAllowsNull(true);
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
		flags.setObjectForKey("YES", EOSchemaGeneration.PrimaryKeyConstraintsKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.ForeignKeyConstraintsKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.CreateDatabaseKey);
		flags.setObjectForKey("NO", EOSchemaGeneration.DropDatabaseKey);
		String createTableScript = ERXSQLHelper.newSQLHelper(adaptor).createSchemaSQLForEntitiesWithOptions(new NSArray<EOEntity>(dbUpdaterModel.entityNamed(migrationTableName(adaptor))), adaptor, flags);
		return createTableScript;
	}
}