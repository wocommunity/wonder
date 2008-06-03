package er.extensions.migration;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

/**
 * IERXMigration is the interface that you must implemnt to provide a migration from one version to another.
 * 
 * You should not saveChanges() on the editingcontext passed into the methods of this interface (unless it cannot be
 * avoided), because there is a transaction above you.
 * 
 * You also must be very careful of the EO operations you perform with this editing context. You may be in the process
 * of upgrading across multiple model versions, so it is possible that you model you are using may be out of sync with
 * the underlying database at the time this particular migration is being performed in the sequence. For instance, you
 * would always be running with the latest version model, but this might be migration 3 of 5 required to get your
 * database in sync with this latest model version, so the transitional model that existed when version 3 was valid may
 * no longer exist. In general, you should only do low level channel operations during migration. Though, the editing
 * context is provided here as a convenience for use under controlled circumstances if you know it will not be a
 * problem.  You can use the IERXPostMigration interface if you need to execute (in a restricted way) EO operations.
 * 
 * @author mschrag
 */
public interface IERXMigration {
	/**
	 * Returns an array of ModelVersion objects that this migration depends on.
	 * 
	 * @return an array of model versions that this migration depends on
	 */
	public NSArray<ERXModelVersion> modelDependencies();

	/**
	 * Called when migrating the database from the last version to this version. For instance if this is AuthModel1, it
	 * will be called to migrate from version 0 to version 1.
	 * 
	 * @param editingContext
	 *            the editing context you can perform EO operations with.
	 * @param channel
	 *            the channel to perform low level operations with
	 * @param model
	 *            the model being upgraded
	 * @throws Throwable
	 *             if something fails
	 */
	public void upgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable;

	/**
	 * Called when migrating the database from the next version to this version. For instance if this is AuthModel1, it
	 * will be called to migrate from version 1 back to version 0.
	 * 
	 * If this is the lowest migration you support, downgrade should throw an ERXMigrationFailedException.
	 * 
	 * @param editingContext
	 *            the editing context you can perform EO operations with.
	 * @param channel
	 *            the channel to perform low level operations with
	 * @param model
	 *            the model being downgraded
	 * @throws Throwable
	 *             if something fails
	 */
	public void downgrade(EOEditingContext editingContext, EOAdaptorChannel channel, EOModel model) throws Throwable;
}
