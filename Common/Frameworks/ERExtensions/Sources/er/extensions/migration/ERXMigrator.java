package er.extensions.migration;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXEC;
import er.extensions.ERXJDBCUtilities;
import er.extensions.ERXProperties;
import er.extensions.ERXEOAccessUtilities.ChannelAction;

/**
 * ERXMigrator provides a simple mechanism for performing database migrations (both upgrading and downgrading).
 * <p>
 * To have ERXMigrator run at the start of your application, set er.migration.migrateAtStartup = true. This will perform
 * a migration of your EOModels using a series of provided migration classes. By default, models are migrated in the
 * order they appear in the EOModelGroup (that is, effectively random). If you want more control over the order at
 * startup, you can set the er.migration.modelNames property to a comma-separate list of model names. The migrator will
 * migrate them in this order.
 * <p>
 * For each EOModel, the migrator will lookup [modelName].MigrationClassPrefix and then append version numbers to that
 * name. For instance, if your model named "AuthModel" is being migrated, and you set
 * AuthModel.MigrationClassPrefix=com.mdimension.migration.AuthModel, it will then look for classes named like
 * com.mdimension.migration.AuthModel0, com.mdimension.migration.AuthModel1, etc where the number corresponds to a
 * zero-offset migration version.
 * <p>
 * The com.mdimension.migration.AuthModelX classes should implement the IERXMigration interface, and should at least
 * provide an implementation of the upgrade method. If you do not provide a downgrade method, you should throw
 * ERXMigrationFailedException when that method is called to notify the system that the requested migration cannot be
 * performed. As an example, AuthModel1.upgrade(..) will be called to move from version 1 to version 2, and
 * AuthModel2.downgrade(..) will be called to move from version 2 back to version 1. Your lowest version number
 * migration should throw an ERXMigrationFailedException when its downgrade method is called.
 * <p>
 * Because of complications with database locking, the system will not autocreate tables and per-model rows for you, so
 * if you are using JDBC migration, you should create a table named _DBUpdater with the following (approximately) create
 * statement:
 * 
 * <pre>
 *               create table _dbupdater (
 *                 modelname varchar(100) not null,
 *                 version integer not null,
 *                 updatelock integer not null,
 *                 lockowner varchar(100)
 *               )
 * </pre>
 * 
 * and for each model you want to be able to migrate, you should:
 * 
 * <pre>
 *               insert into _dbupdater(modelname, version, updatelock, lockowner) values ('YourModelName', -1, 0, NULL)
 * </pre>
 * 
 * Be aware that not all databases are able to perform DDL operations in a transaction. The result of this is that if a
 * DDL operation fails, your database may be left in an unknown state because the subsequent rollback will fail. Version
 * numbers only increase when each migration completes sucessfully, so in this case, your migration version would be
 * left at the previous version number.
 * <p>
 * Startup migration runs in response to the ApplicationDidFinishLaunchingNotification, so you should not access your
 * EO's until after that notification is complete.
 * 
 * @property er.migration.migrateAtStartup if true, migrateToLatest is automatically called at startup
 * @property er.migration.[adaptorName].lockClassName the name of the IERXMigrationLock class to use (defaults to
 *           er.extensions.migration.ERX[adaptorName]MigrationLock)
 * @property er.migration.modelNames a comma-separated list of model names to be migrated in a particular order. If
 *           missing, it will default to modelgroup.models() order.
 * @property [modelName].MigrationClassPrefix the prefix of the class name to use to upgrade the model named
 *           [modelName]. Defaults to [modelName].
 * 
 * @author mschrag
 */
public class ERXMigrator {
	public static final Logger log = Logger.getLogger(ERXMigrator.class);

	/**
	 * Symbolic version number for migrating to "the latest" version.
	 */
	public static final int LATEST_VERSION = Integer.MAX_VALUE;

	private String _lockOwnerName;

	/**
	 * Costructs an ERXMigrator with the given lock owner. For an application, the lock owner name defaults to
	 * appname-instancenumber.
	 * 
	 * @param lockOwnerName
	 *            the name of the lock owner
	 */
	public ERXMigrator(String lockOwnerName) {
		_lockOwnerName = lockOwnerName;
	}

	/**
	 * Returns whether or not migration should run at startup. Defaults to false.
	 */
	public static boolean shouldMigrateAtStartup() {
		return ERXProperties.booleanForKeyWithDefault("er.migration.migrateAtStartup", false);
	}

	/**
	 * Migrates all models specified in the default model group to the latest versions.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void migrateToLatest() {
		EOModelGroup modelGroup = EOModelGroup.defaultGroup();
		NSArray modelNames;
		String modelNamesStr = ERXProperties.stringForKey("er.migration.modelNames");
		if (modelNamesStr == null) {
			ERXMigrator.log.warn("er.migration.modelNames is not set, defaulting to modelGroup.models() order instead.");
			modelNames = modelGroup.modelNames();
		}
		else {
			modelNames = NSArray.componentsSeparatedByString(modelNamesStr, ",");
		}
		Map migrations = _buildDependenciesForModelsNamed(modelNames);

		Map postMigrations = new HashMap();
		Iterator migrationsIter = migrations.keySet().iterator();
		while (migrationsIter.hasNext()) {
			IERXMigration migration = (IERXMigration) migrationsIter.next();
			ModelVersion modelVersion = (ModelVersion) migrations.get(migration);
			EOModel model = modelVersion.model();
			String adaptorName = model.adaptorName();
			String migrationLockClassName = ERXProperties.stringForKeyWithDefault("er.migration." + adaptorName + ".lockClassName", "er.extensions.migration.ERX" + adaptorName + "MigrationLock");
			IERXMigrationLock database;
			try {
				Class migrationLockClass = Class.forName(migrationLockClassName);
				database = (IERXMigrationLock) migrationLockClass.newInstance();
			}
			catch (Throwable t) {
				throw new ERXMigrationFailedException("Failed to create migration lock class '" + migrationLockClassName + "'.", t);
			}
			EOEditingContext editingContext = ERXEC.newEditingContext();
			ERXMigrationAction migrationAction = new ERXMigrationAction(editingContext, migration, modelVersion, database, _lockOwnerName, postMigrations);
			try {
				migrationAction.perform(editingContext, model.name());
			}
			catch (ERXMigrationFailedException e) {
				throw e;
			}
			catch (Throwable t) {
				throw new ERXMigrationFailedException("Failed to migrate model '" + model.name() + "'.", t);
			}
		}

		Iterator postMigrationsIter = postMigrations.keySet().iterator();
		while (postMigrationsIter.hasNext()) {
			IERXPostMigration postMigration = (IERXPostMigration) postMigrationsIter.next();
			ModelVersion modelVersion = (ModelVersion) postMigrations.get(postMigration);
			EOEditingContext editingContext = ERXEC.newEditingContext();
			try {
				postMigration.postUpgrade(editingContext, modelVersion.model());
				editingContext.saveChanges();
			}
			catch (Throwable t) {
				throw new ERXMigrationFailedException("Failed on post migrations for model '" + modelVersion.model().name() + "'.", t);
			}
		}
	}

	protected Map _buildDependenciesForModelsNamed(NSArray modelNames) {
		Map migrations = new LinkedHashMap();
		try {
			Map versions = new HashMap();

			EOModelGroup modelGroup = EOModelGroup.defaultGroup();
			Enumeration modelNamesEnum = modelNames.objectEnumerator();
			while (modelNamesEnum.hasMoreElements()) {
				String modelName = (String) modelNamesEnum.nextElement();
				EOModel model = modelGroup.modelNamed(modelName);
				_buildDependenciesForModel(model, ERXMigrator.LATEST_VERSION, versions, migrations);
			}

			Iterator modelNamesIter = versions.keySet().iterator();
			while (modelNamesIter.hasNext()) {
				String modelName = (String) modelNamesIter.next();
				EOModel model = modelGroup.modelNamed(modelName);
				Enumeration entitiesEnum = model.entities().objectEnumerator();
				while (entitiesEnum.hasMoreElements()) {
					EOEntity entity = (EOEntity) entitiesEnum.nextElement();
					EOEntity parentEntity = entity.parentEntity();
					if (parentEntity != null && !parentEntity.model().equals(model)) {
						_buildDependenciesForModel(parentEntity.model(), LATEST_VERSION, versions, migrations);
					}
					Enumeration relationshipsEnum = entity.relationships().objectEnumerator();
					while (relationshipsEnum.hasMoreElements()) {
						EORelationship relationship = (EORelationship) relationshipsEnum.nextElement();
						EOEntity destinationEntity = relationship.destinationEntity();
						if (destinationEntity != null && !destinationEntity.model().equals(model)) {
							_buildDependenciesForModel(destinationEntity.model(), LATEST_VERSION, versions, migrations);
						}
					}
				}
				_buildDependenciesForModel(model, LATEST_VERSION, versions, migrations);
			}
		}
		catch (InstantiationException e) {
			throw new RuntimeException("Migration failed.", e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException("Migration failed.", e);
		}
		return migrations;
	}

	protected void _buildDependenciesForModel(EOModel model, int migrateToVersion, Map versions, Map migrations) throws InstantiationException, IllegalAccessException {
		String modelName = model.name();

		Integer migratorVersion = (Integer) versions.get(modelName);
		if (migratorVersion == null) {
			migratorVersion = new Integer(-1);
		}

		if (migratorVersion.intValue() != ERXMigrator.LATEST_VERSION) {
			boolean done = false;
			for (int versionNum = migratorVersion.intValue() + 1; !done && versionNum <= migrateToVersion; versionNum++) {
				String migrationClassPrefix = ERXProperties.stringForKeyWithDefault(modelName + ".MigrationClassPrefix", modelName);
				String erMigrationClassName = migrationClassPrefix + versionNum;
				String vendorMigrationClassName = migrationClassPrefix + ERXJDBCUtilities.databaseProductName(model) + versionNum;
				try {
					Class erMigrationClass;
					try {
						if (ERXMigrator.log.isDebugEnabled()) {
							ERXMigrator.log.debug("Looking for migration '" + erMigrationClassName + "' ...");
						}
						erMigrationClass = Class.forName(erMigrationClassName);
					}
					catch (ClassNotFoundException e) {
						if (ERXMigrator.log.isDebugEnabled()) {
							ERXMigrator.log.debug("Looking for vendor-specific migration '" + vendorMigrationClassName + "-' ...");
						}
						erMigrationClass = Class.forName(vendorMigrationClassName);
					}

					IERXMigration migration = (IERXMigration) erMigrationClass.newInstance();
					versions.put(modelName, new Integer(versionNum));
					NSArray migrationDependencies = migration.modelDependencies();
					if (migrationDependencies != null) {
						Enumeration migrationDependenciesEnum = migrationDependencies.objectEnumerator();
						while (migrationDependenciesEnum.hasMoreElements()) {
							ModelVersion modelVersion = (ModelVersion) migrationDependenciesEnum.nextElement();
							EOModel dependsOnModel = modelVersion.model();
							int dependsOnVersion = modelVersion.version();
							_buildDependenciesForModel(dependsOnModel, dependsOnVersion, versions, migrations);
						}
					}

					migrations.put(migration, new ModelVersion(model, versionNum));
				}
				catch (ClassNotFoundException e) {
					done = true;
					if (ERXMigrator.log.isDebugEnabled()) {
						ERXMigrator.log.debug("  Migration " + erMigrationClassName + " and/or " + vendorMigrationClassName + " do not exist.");
					}
					versions.put(modelName, new Integer(ERXMigrator.LATEST_VERSION));
				}
			}
		}
	}

	/**
	 * ModelVersion represents a particular version of an EOModel.
	 * 
	 * @author mschrag
	 */
	public static class ModelVersion {
		private EOModel _model;
		private int _version;

		/**
		 * @param model
		 *            a model
		 * @param version
		 *            the version of that model
		 */
		public ModelVersion(EOModel model, int version) {
			_model = model;
			_version = version;
		}

		/**
		 * @param modelName
		 *            the name of a model
		 * @param version
		 *            the version of that model
		 */
		public ModelVersion(String modelName, int version) {
			this(EOModelGroup.defaultGroup().modelNamed(modelName), version);
		}

		/**
		 * Returns the model for this version.
		 * 
		 * @return the model for this version
		 */
		public EOModel model() {
			return _model;
		}

		/**
		 * Returns the version dependency for this model.
		 * 
		 * @return the version dependency for this model
		 */
		public int version() {
			return _version;
		}
	}

	protected static class ERXMigrationAction extends ChannelAction {
		private EOEditingContext _editingContext;
		private IERXMigrationLock _database;
		private IERXMigration _migration;
		private ModelVersion _modelVersion;
		private Map _migrations;
		private String _lockOwnerName;
		private Map _postMigrations;

		public ERXMigrationAction(EOEditingContext editingContext, IERXMigration migration, ModelVersion modelVersion, IERXMigrationLock database, String lockOwnerName, Map postMigrations) {
			_editingContext = editingContext;
			_modelVersion = modelVersion;
			_migration = migration;
			_database = database;
			_lockOwnerName = lockOwnerName;
			_postMigrations = postMigrations;
		}

		protected int doPerform(EOAdaptorChannel channel) {
			EOModel model = _modelVersion.model();
			boolean locked;
			do {
				locked = _database.tryLock(channel, model, _lockOwnerName);
				if (!locked) {
					try {
						Thread.sleep(5 * 1000);
					}
					catch (InterruptedException e) {
						// do nothing
					}
				}
				// MS: Do we put a timeout here? It could take a very long time
				// ...
			}
			while (!locked);

			if (locked) {
				try {
					int currentVersion = _database.versionNumber(channel, model);
					int nextVersion = _modelVersion.version();
					if (currentVersion < nextVersion) {
						if (ERXMigrator.log.isInfoEnabled()) {
							ERXMigrator.log.info("Upgrading " + model.name() + " to version " + nextVersion + " with migration '" + _migration + "'");
						}
						_migration.upgrade(_editingContext, channel, model);
						_database.setVersionNumber(channel, model, nextVersion);
						_editingContext.saveChanges();
						channel.adaptorContext().commitTransaction();
						channel.adaptorContext().beginTransaction();
						if (ERXMigrator.log.isInfoEnabled()) {
							ERXMigrator.log.info(model.name() + " is now version " + nextVersion);
						}
						if (_migration instanceof IERXPostMigration) {
							_postMigrations.put(_migration, _modelVersion);
						}
					}
					else {
						ERXMigrator.log.debug("Already upgraded " + model.name() + " to " + nextVersion + ", skipping");
					}
				}
				catch (Throwable t) {
					throw new ERXMigrationFailedException("Migration failed.", t);
				}
				finally {
					_database.unlock(channel, model);
				}
			}

			return 0;
		}
	}
}
