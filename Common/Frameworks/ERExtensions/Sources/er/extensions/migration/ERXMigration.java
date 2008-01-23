package er.extensions.migration;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation._NSStringUtilities;

import er.extensions.ERXJDBCUtilities;

/**
 * <p>Convenience superclass for Migration classes. Checks for corresponding sql files name
 * "ClassnameX_Upgrade.migration" and "ClassnameX_Downgrade.migration" where
 * "Classname" is the classname of the migration class. The files have to to be
 * in a corresponding bundle. Implement "migrationBundleName" to return the
 * correct name of the bundle.</p>
 * 
 * <p>This makes migrations easier as you only have to create a new Java class according to the 
 * migration naming conventions, inherit from this class and put your SQL in a properly 
 * named text file (or more than one file, if you use database specific migrations).</p>
 * 
 * <p>If you need database specific migrations use</p>
 * 
 * <p><code>er.extensions.migration.ERXMigration.useDatabaseSpecificMigrations=true</code></p>
 * 
 * <p>in your Properties. The default is not to use database specific migrations. A filename
 * for a database specific migration is then for example</p>
 * <p><code>ClassnameX_FrontBase_Upgrade.migration</code> or</p>
 * <p><code>ClassnameX_Postgresql_Upgrade.migration</code></p>
 * 
 * <p>For the database specific part of the filename, the databaseProductName as from the JDBC
 * adaptor is used. So make sure, you're using the correct filename. The migration will throw
 * an exception if the appropriate migration textfile can't be found.</p>
 * 
 * @author cug
 */
public abstract class ERXMigration implements IERXMigration {

	/**
	 * Logging support
	 */
	private Logger log = Logger.getLogger(ERXMigration.class.getName());
	
	private Boolean _useDatabaseSpecificMigrations;

	/**
	 * Override the global application preference on per-database migrations.
	 * 
	 * @param useDatabaseSpecificMigrations if true, database-specific migrations will be used
	 */
	public ERXMigration(boolean useDatabaseSpecificMigrations) {
		_useDatabaseSpecificMigrations = Boolean.valueOf(useDatabaseSpecificMigrations);
	}
	
	public ERXMigration() {
	}
	
	/**
	 * No dependencies
	 */
	public NSArray<ERXModelVersion> modelDependencies() {
		return null;
	}

	/**
	 * Checks for a corresponding downgrade file which is performed as a raw SQL action
	 */
	public void downgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable {
		String sqlString = null;
		if (useDatabaseSpecificMigrations()) {
			sqlString = this.getSQLForMigration(this.getClass().getSimpleName() + "_" + ERXJDBCUtilities.databaseProductName(channel) + "_Downgrade.migration");
		}
		else {
			sqlString = this.getSQLForMigration(this.getClass().getSimpleName() + "_Downgrade.migration");
		}

		if (sqlString != null) {
			log.info("Applying migration for: " + this.getClass().getName());
			ERXJDBCUtilities.executeUpdateScript(channel, sqlString);
		}
		else {
			if (useDatabaseSpecificMigrations()) {
				throw new ERXMigrationFailedException("No downgrade for migration: " + this.getClass().getName() + "found for database: " + ERXJDBCUtilities.databaseProductName(channel));
			}
			else {
				throw new ERXMigrationFailedException("No downgrade for migration: " + this.getClass().getName());
			}
		}

	}

	/**
	 * Checks for a corresponding upgrade file which is performed as a raw SQL action
	 */
	public void upgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable {

		String sqlString = null;
		if (useDatabaseSpecificMigrations()) {
			sqlString = this.getSQLForMigration(this.getClass().getSimpleName() + "_" + ERXJDBCUtilities.databaseProductName(channel) + "_Upgrade.migration");
		}
		else {
			sqlString = this.getSQLForMigration(this.getClass().getSimpleName() + "_Upgrade.migration");
		}

		if (sqlString != null) {
			log.info("Applying migration for: " + this.getClass().getName());
			ERXJDBCUtilities.executeUpdateScript(channel, sqlString);
		}
		else {
			if (useDatabaseSpecificMigrations()) {
				throw new ERXMigrationFailedException("No upgrade for migration: " + this.getClass().getName() + " found for database: " + ERXJDBCUtilities.databaseProductName(channel));
			}
			else {
				throw new ERXMigrationFailedException("No upgrade for migration: " + this.getClass().getName() + " found.");
			}
		}
	}

	/**
	 * Checks in the current bundle for migration files corresponding to this classes name
	 * 
	 * @param migrationName
	 */
	protected String getSQLForMigration(String migrationName) {
		NSBundle bundle;
		String migrationBundleName = this.migrationBundleName();
		if (migrationBundleName == null) {
			bundle = NSBundle.bundleForClass(getClass());
		}
		else {
			bundle = NSBundle.bundleForName(this.migrationBundleName());
			if (bundle == null) {
				bundle = NSBundle._appBundleForName(this.migrationBundleName());
			}
		}
		NSArray resourcePaths = bundle.resourcePathsForResources("migration", null);

		if (resourcePaths != null) {
			for (int i = 0; i < resourcePaths.count(); i++) {
				String currentPath = (String) resourcePaths.objectAtIndex(i);

				if (currentPath.endsWith(migrationName)) {
					try {
						return new String(bundle.bytesForResourcePath(currentPath), _NSStringUtilities.UTF8_ENCODING);
					}
					catch (UnsupportedEncodingException e) {
						log.error(e, e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * The name to create the NSBundle for the current bundle, defaults to the
	 * bundle that contains the migration class.
	 */
	protected String migrationBundleName() {
		return null;
	}

	protected boolean useDatabaseSpecificMigrations() {
		if (this._useDatabaseSpecificMigrations == null) {
			this._useDatabaseSpecificMigrations = new Boolean (Boolean.getBoolean("er.extensions.migration.ERXMigration.useDatabaseSpecificMigrations"));
		}
		return this._useDatabaseSpecificMigrations.booleanValue();
	}

}
