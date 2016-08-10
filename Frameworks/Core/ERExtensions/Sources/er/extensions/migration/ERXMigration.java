package er.extensions.migration;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation._NSStringUtilities;

import er.extensions.foundation.ERXProperties;
import er.extensions.jdbc.ERXJDBCUtilities;

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
 * <p>If you need database specific migrations use:
 * 
 * <blockquote><code>er.extensions.migration.ERXMigration.useDatabaseSpecificMigrations=true</code></blockquote>
 * 
 * in your Properties. The default is not to use database specific migrations. A filename
 * for a database specific migration is then, for example, <code>ClassnameX_FrontBase_Upgrade.migration</code> or
 * <code>ClassnameX_Postgresql_Upgrade.migration</code>.
 * 
 * <p>For the database specific part of the filename, the databaseProductName as from the JDBC
 * adaptor is used. So make sure, you're using the correct filename. The migration will throw
 * an exception if the appropriate migration textfile can't be found.</p>
 * 
 * @author cug
 */
public abstract class ERXMigration implements IERXMigration {
	private static final Logger log = LoggerFactory.getLogger(ERXMigration.class);
	
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
			sqlString = getSQLForMigration(getClass().getSimpleName() + "_" + ERXJDBCUtilities.databaseProductName(channel) + "_Downgrade.migration");
		}
		else {
			sqlString = getSQLForMigration(getClass().getSimpleName() + "_Downgrade.migration");
		}

		if (sqlString != null) {
			log.info("Applying migration for: {}", getClass());
			ERXJDBCUtilities.executeUpdateScript(channel, sqlString);
		}
		else {
			if (useDatabaseSpecificMigrations()) {
				throw new ERXMigrationFailedException("No downgrade for migration: " + getClass().getName() + "found for database: " + ERXJDBCUtilities.databaseProductName(channel));
			}
			throw new ERXMigrationFailedException("No downgrade for migration: " + getClass().getName());
		}

	}

	/**
	 * Checks for a corresponding upgrade file which is performed as a raw SQL action
	 */
	public void upgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable {

		String sqlString = null;
		if (useDatabaseSpecificMigrations()) {
			sqlString = getSQLForMigration(getClass().getSimpleName() + "_" + ERXJDBCUtilities.databaseProductName(channel) + "_Upgrade.migration");
		}
		else {
			sqlString = getSQLForMigration(getClass().getSimpleName() + "_Upgrade.migration");
		}

		if (sqlString != null) {
			log.info("Applying migration for: {}", getClass());
			ERXJDBCUtilities.executeUpdateScript(channel, sqlString);
		}
		else {
			if (useDatabaseSpecificMigrations()) {
				throw new ERXMigrationFailedException("No upgrade for migration: " + getClass().getName() + " found for database: " + ERXJDBCUtilities.databaseProductName(channel));
			}
			throw new ERXMigrationFailedException("No upgrade for migration: " + getClass().getName() + " found.");
		}
	}

	/**
	 * Checks in the current bundle for migration files corresponding to this classes name
	 * 
	 * @param migrationName
	 * @return SQL string
	 */
	protected String getSQLForMigration(String migrationName) {
		NSBundle bundle;
		String migrationBundleName = migrationBundleName();
		if (migrationBundleName == null) {
			bundle = NSBundle.bundleForClass(getClass());
		}
		else {
			bundle = NSBundle.bundleForName(migrationBundleName());
			if (bundle == null) {
				bundle = NSBundle._appBundleForName(migrationBundleName());
			}
		}
		NSArray<String> resourcePaths = bundle.resourcePathsForResources("migration", null);

		if (resourcePaths != null) {
			for (String currentPath : resourcePaths) {
				if (currentPath.endsWith(migrationName)) {
					try {
						return new String(bundle.bytesForResourcePath(currentPath), _NSStringUtilities.UTF8_ENCODING);
					}
					catch (UnsupportedEncodingException e) {
						log.error("Could not use UTF-8 encoding.", e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * The name to create the NSBundle for the current bundle, defaults to the
	 * bundle that contains the migration class.
	 * 
	 * @return <code>null</code> 
	 */
	protected String migrationBundleName() {
		return null;
	}

	protected boolean useDatabaseSpecificMigrations() {
		if (_useDatabaseSpecificMigrations == null) {
			_useDatabaseSpecificMigrations = Boolean.valueOf(
					ERXProperties.booleanForKeyWithDefault("er.extensions.migration.ERXMigration.useDatabaseSpecificMigrations", false));
		}
		return _useDatabaseSpecificMigrations.booleanValue();
	}

}
