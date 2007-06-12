package er.extensions.migration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;

import er.extensions.ERXJDBCUtilities;

/**
 * Convenience superclass for Migration classes. Checks for corresponding sql files name
 * "ClassnameX_Upgrade.migration" and "ClassnameX_Downgrade.migration" where
 * "Classname" is the classname of the migration class. The files have to to be
 * in a corresponding bundle. Implement "migrationBundleName" to return the
 * correct name of the bundle.
 * <br><br>
 * This makes migrations easier as you only have to create new Java class according to the 
 * migration naming conventions, inherit from this class and put your SQL in a properly 
 * named text file.
 * 
 * @author cug
 */
public abstract class ERXMigration implements IERXMigration {

	/**
	 * Logging support
	 */
	private Logger log = Logger.getLogger("net.events.Migration");

	/**
	 * No dependencies
	 */
	public NSArray modelDependencies() {
		return null;
	}

	/**
	 * Checks for a corresponding downgrade file which is performed as a raw SQL action
	 */
	public void downgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable {
		String sqlString = this.getSQLForMigration(this.getClass().getSimpleName() + "_Downgrade.migration");

		if (sqlString != null) {
			log.info("Applying migration for: " + this.getClass().getName());
			ERXJDBCUtilities.executeUpdate(channel, sqlString);
		}
		else {
			throw new ERXMigrationFailedException("No downgrade for migration: " + this.getClass().getName());
		}

	}

	/**
	 * Checks for a corresponding upgrade file which is performed as a raw SQL action
	 */
	public void upgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable {

		String sqlString = this.getSQLForMigration(this.getClass().getSimpleName() + "_Upgrade.migration");

		if (sqlString != null) {
			log.info("Applying migration for: " + this.getClass().getName());
			ERXJDBCUtilities.executeUpdate(channel, sqlString);
		}

	}

	/**
	 * Checks in the current bundle for migration files corresponding to this classes name
	 * 
	 * @param migrationName
	 * @return
	 */
	protected String getSQLForMigration(String migrationName) {
		NSBundle bundle = NSBundle.bundleForName(this.migrationBundleName());
		NSArray resourcePaths = bundle.resourcePathsForResources("migration", null);

		if (resourcePaths != null) {
			for (int i = 0; i < resourcePaths.count(); i++) {
				String currentPath = (String) resourcePaths.objectAtIndex(i);

				if (currentPath.endsWith(migrationName)) {
					return new String(bundle.bytesForResourcePath(currentPath));
				}
			}
		}
		return null;
	}

	/**
	 * The name to create the NSBundle for the current bundle
	 * @return
	 */
	protected abstract String migrationBundleName();

}
