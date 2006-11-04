package er.extensions.migration;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.ERXEC;
import er.extensions.ERXProperties;
import er.extensions.ERXEOAccessUtilities.ChannelAction;

/**
 * ERXMigrator provides a simple mechanism for performing database migrations
 * (both upgrading and downgrading).
 * <p>
 * To have ERXMigrator run at the start of your application, set
 * er.migration.migrateAtStartup = true. This will perform a migration of your
 * EOModels using a series of provided migration classes. By default, models are
 * migrated in the order they appear in the EOModelGroup (that is, effectively
 * random). If you want more control over the order at startup, you can set the
 * er.migration.modelNames property to a comma-separate list of model names. The
 * migrator will migrate them in this order.
 * <p>
 * For each EOModel, the migrator will lookup [modelName].MigrationClassPrefix
 * and then append version numbers to that name. For instance, if your model
 * named "AuthModel" is being migrated, and you set
 * AuthModel.MigrationClassPrefix=com.mdimension.migration.AuthModel, it will
 * then look for classes named like com.mdimension.migration.AuthModel0,
 * com.mdimension.migration.AuthModel1, etc where the number corresponds to a
 * zero-offset migration version.
 * <p>
 * The com.mdimension.migration.AuthModelX classes should implement the
 * IERXMigration interface, and should at least provide an implementation of the
 * upgrade method. If you do not provide a downgrade method, you should throw
 * ERXMigrationFailedException when that method is called to notify the system
 * that the requested migration cannot be performed.  As an example, 
 * AuthModel1.upgrade(..) will be called to move from version 1 to version 2,
 * and AuthModel2.downgrade(..) will be called to move from version 2 back to
 * version 1.  Your lowest version number migration should throw an
 * ERXMigrationFailedException when its downgrade method is called.
 * <p>
 * Because of complications with database locking, the system will not
 * autocreate tables and per-model rows for you, so if you are using JDBC
 * migration, you should create a table named _DBUpdater with the following
 * (approximately) create statement:
 * 
 * <pre>
 *     create table &quot;_DBUpdater&quot; (
 *       &quot;ModelName&quot; varchar(100) not null,
 *       &quot;Version&quot; integer not null,
 *       &quot;UpdateLock&quot; integer not null,
 *       &quot;LockOwner&quot; varchar(100)
 *     )
 * </pre>
 * 
 * and for each model you want to be able to migrate, you should:
 * 
 * <pre>
 *     insert into &quot;_DBUpdater&quot;(&quot;ModelName&quot;, &quot;Version&quot;, &quot;UpdateLock&quot;, &quot;LockOwner&quot;) values ('YourModelName', -1, 0, NULL)
 * </pre>
 * 
 * Be aware that not all databases are able to perform DDL operations in a
 * transaction. The result of this is that if a DDL operation fails, your
 * database may be left in an unknown state because the subsequent rollback will
 * fail. Version numbers only increase when each migration completes
 * sucessfully, so in this case, your migration version would be left at the
 * previous version number.
 * <p>
 * Startup migration runs in response to the
 * ApplicationWillFinishLaunchingNotification, so you should not access your
 * EO's until after that notification is complete.
 * 
 * @property er.migration.migrateAtStartup if true, migrateToLatest is
 *           automatically called at startup
 * @property er.migration.[adaptorName].lockClassName the name of the
 *           IERXMigrationLock class to use (defaults to
 *           er.extensions.migration.ERX[adaptorName]MigrationLock)
 * @property er.migration.modelNames a comma-separated list of model names to be
 *           migrated in a particular order. If missing, it will default to
 *           modelgroup.models() order.
 * @property [modelName].MigrationClassPrefix the prefix of the class name to
 *           use to upgrade the model named [modelName]. Defaults to
 *           [modelName].
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
	 * Costructs an ERXMigrator with the given lock owner. For an application,
	 * the lock owner name defaults to appname-instancenumber.
	 * 
	 * @param lockOwnerName
	 *            the name of the lock owner
	 */
	public ERXMigrator(String lockOwnerName) {
		_lockOwnerName = lockOwnerName;
	}

	/**
	 * Returns whether or not migration should run at startup. Defaults to
	 * false.
	 */
	public static boolean shouldMigrateAtStartup() {
		return ERXProperties.booleanForKeyWithDefault("er.migration.migrateAtStartup", false);
	}

	/**
	 * Migrates all models specified in the default model group to the latest
	 * versions.
	 */
	public void migrateToLatest() {
		migrateModelGroupToLatest(EOModelGroup.defaultGroup());
	}

	/**
	 * Migrates all models specified from this model group to the latest
	 * versions.
	 * 
	 * @param modelGroup the model group to work within
	 */
	public void migrateModelGroupToLatest(EOModelGroup modelGroup) {
		String modelNamesStr = ERXProperties.stringForKey("er.migration.modelNames");
		if (modelNamesStr == null) {
			ERXMigrator.log.info("er.migration.modelNames is not set, defaulting to modelGroup.models() order instead.");
			Enumeration modelsEnum = modelGroup.models().objectEnumerator();
			while (modelsEnum.hasMoreElements()) {
				EOModel model = (EOModel) modelsEnum.nextElement();
				migrateModelToLatest(model);
			}
		}
		else {
			NSArray modelNames = NSArray.componentsSeparatedByString(modelNamesStr, ",");
			migrateToLatestWithModelsNamed(modelGroup, modelNames);
		}
	}

	/**
	 * Migrates all the models specified in the modelNames array within the given
	 * model group to the latest version.
	 * 
	 * @param modelGroup the model group to work within
	 * @param modelNames the names of models to migrate
	 */
	public void migrateToLatestWithModelsNamed(EOModelGroup modelGroup, NSArray modelNames) {
		Enumeration modelNamesEnum = modelNames.objectEnumerator();
		while (modelNamesEnum.hasMoreElements()) {
			String modelName = (String) modelNamesEnum.nextElement();
			EOModel model = modelGroup.modelNamed(modelName);
			migrateModelToLatest(model);
		}
	}

	/**
	 * Migrates the given model to the latest version.
	 * 
	 * @param model the model to migrate
	 */
	public void migrateModelToLatest(EOModel model) {
		migrateModelToVersion(model, ERXMigrator.LATEST_VERSION);
	}

	/**
	 * Migrates the given model to the specified target version. 
	 * 
	 * @param model the model to migrate
	 * @param targetVersion the target version number to migrate it to
	 */
	public void migrateModelToVersion(EOModel model, int targetVersion) {
		if (ERXMigrator.log.isInfoEnabled()) {
			if (targetVersion == ERXMigrator.LATEST_VERSION) {
				ERXMigrator.log.info("Migrating " + model.name() + " to latest version.");
			}
			else {
				ERXMigrator.log.info("Migrating " + model.name() + " to version " + targetVersion + ".");
			}
		}
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
		String modelName = model.name();
		String migrationClassPrefix = ERXProperties.stringForKeyWithDefault(modelName + ".MigrationClassPrefix", modelName);
		EOEditingContext editingContext = ERXEC.newEditingContext();
		ERXMigrationAction migrations = new ERXMigrationAction(editingContext, database, modelName, migrationClassPrefix, targetVersion, _lockOwnerName);
		try {
			migrations.perform(editingContext, model.name());
		}
		catch (ERXMigrationFailedException e) {
			throw e;
		}
		catch (Throwable t) {
			throw new ERXMigrationFailedException("Failed to migrate model '" + model.name() + "'.", t);
		}
	}

	protected static class ERXMigrationAction extends ChannelAction {
		private EOEditingContext _editingContext;
		private IERXMigrationLock _database;
		private String _modelName;
		private String _migrationClassPrefix;
		private int _targetVersion;
		private String _lockOwnerName;

		public ERXMigrationAction(EOEditingContext editingContext, IERXMigrationLock database, String modelName, String migrationClassPrefix, int targetVersion, String lockOwnerName) {
			_editingContext = editingContext;
			_database = database;
			_modelName = modelName;
			_migrationClassPrefix = migrationClassPrefix;
			_targetVersion = targetVersion;
			_lockOwnerName = lockOwnerName;
		}

		protected int doPerform(EOAdaptorChannel channel) {
			boolean locked;
			do {
				locked = _database.tryLock(channel, _modelName, _lockOwnerName);
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
					int previousVersion = _database.versionNumber(channel, _modelName);
					if (ERXMigrator.log.isInfoEnabled()) {
						ERXMigrator.log.info(_modelName + " is currently version " + previousVersion);
					}
					boolean done = false;
					int direction = (previousVersion < _targetVersion) ? 1 : -1;
					int currentVersion = previousVersion;
					int nextVersion = -1;
					while (!done && currentVersion != _targetVersion) {
						nextVersion = currentVersion + direction;

						int migratorVersion;
						if (direction == 1) {
							migratorVersion = currentVersion + 1;
						}
						else if (direction == -1) {
							migratorVersion = currentVersion;
						}
						else {
							throw new IllegalStateException("Unknown direction " + direction);
						}

						String erMigrationClassName = _migrationClassPrefix + migratorVersion;
						if (ERXMigrator.log.isInfoEnabled()) {
							ERXMigrator.log.info("Looking for migration '" + erMigrationClassName + "' ...");
						}
						try {
							Class erMigrationClass = Class.forName(erMigrationClassName);
							IERXMigration migration = (IERXMigration) erMigrationClass.newInstance();
							try {
								if (direction == 1) {
									if (ERXMigrator.log.isInfoEnabled()) {
										ERXMigrator.log.info("Upgrading " + _modelName + " to version " + nextVersion + " with migration '" + erMigrationClassName + "'");
									}
									migration.upgrade(_editingContext, channel);
								}
								else if (direction == -1) {
									if (ERXMigrator.log.isInfoEnabled()) {
										ERXMigrator.log.info("Dowgrading " + _modelName + " to version " + nextVersion + " with migration '" + erMigrationClassName + "'");
									}
									migration.downgrade(_editingContext, channel);
								}
								else {
									throw new IllegalStateException("Unknown direction " + direction);
								}
							}
							catch (Throwable e) {
								throw new ERXMigrationFailedException("Failed to perform migration of " + migration + ".", e);
							}
							_database.setVersionNumber(channel, _modelName, nextVersion);
							_editingContext.saveChanges();
							channel.adaptorContext().commitTransaction();
							channel.adaptorContext().beginTransaction();
							currentVersion = nextVersion;
							if (ERXMigrator.log.isInfoEnabled()) {
								ERXMigrator.log.info(_modelName + " is now version " + currentVersion);
							}
						}
						catch (ClassNotFoundException e) {
							done = true;
						}
						catch (InstantiationException e) {
							throw new ERXMigrationFailedException("Failed to instantiate migration " + erMigrationClassName, e);
						}
						catch (IllegalAccessException e) {
							throw new ERXMigrationFailedException("Failed to instantiate migration " + erMigrationClassName, e);
						}
					}

					if (_targetVersion != ERXMigrator.LATEST_VERSION && currentVersion != _targetVersion) {
						throw new ERXMigrationFailedException("You asked to migrate from version " + previousVersion + " to version " + _targetVersion + ", but there was no migration available for version " + nextVersion + ".");
					}
				}
				finally {
					_database.unlock(channel, _modelName);
				}

				if (ERXMigrator.log.isInfoEnabled()) {
					ERXMigrator.log.info("Migration of " + _modelName + " is complete.");
				}
			}
			return 0;
		}
	}

}
