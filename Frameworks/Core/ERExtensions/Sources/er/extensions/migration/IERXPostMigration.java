package er.extensions.migration;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;

/**
 * IERXPostMigration is the interface that you must implement to provide migrations that execute after the database
 * structurally matches your EOModels.  Post upgrades are not versioned independently, so there is an assumption
 * (for now) that if the upgrade migration itself succeeded, that the postUpgrades will also and can just queue
 * up to execute at the end.  This feature is here as a "just in case".  Under almost all circumstances, you 
 * should be using direct SQL in your normal migration classes. 
 * 
 * @author mschrag
 */
public interface IERXPostMigration extends IERXMigration {
	/**
	 * Called after executing all of the upgrade() migrations. At this point, the databases match the current EOModel,
	 * so it is safe to perform EO operations.
	 * 
	 * @param editingContext
	 *            the editing context you can perform EO operations with.
	 * @param model
	 *            the model being upgraded
	 * @throws Throwable
	 *             if something fails
	 */
	public void postUpgrade(EOEditingContext editingContext, EOModel model) throws Throwable;
}
