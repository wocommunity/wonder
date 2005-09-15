package er.extensions;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

public interface ERXEnterpriseObject {
	
	/** Selector for flushCaches() */
    public static final NSSelector FlushCachesSelector = new NSSelector("flushCaches");

    /** logging support. Called after an object is successfully inserted */
    public static final ERXLogger tranLogDidInsert = ERXLogger
            .getERXLogger("er.transaction.eo.did.insert.ERXGenericRecord");

    /** logging support. Called after an object is successfully deleted */
    public static final ERXLogger tranLogDidDelete = ERXLogger
            .getERXLogger("er.transaction.eo.did.delete.ERXGenericRecord");

    /** logging support. Called after an object is successfully updated */
    public static final ERXLogger tranLogDidUpdate = ERXLogger
            .getERXLogger("er.transaction.eo.did.update.ERXGenericRecord");

    /** logging support. Called after an object is reverted. **/
    public static final ERXLogger tranLogDidRevert = ERXLogger
            .getERXLogger("er.transaction.eo.did.revert.ERXGenericRecord");

    /** logging support. Called before an object is inserted */
    public static final ERXLogger tranLogWillInsert = ERXLogger
            .getERXLogger("er.transaction.eo.will.insert.ERXGenericRecord");

    /** logging support. Called before an object is deleted */
    public static final ERXLogger tranLogWillDelete = ERXLogger
            .getERXLogger("er.transaction.eo.will.delete.ERXGenericRecord");

    /** logging support. Called before an object is updated */
    public static final ERXLogger tranLogWillUpdate = ERXLogger
            .getERXLogger("er.transaction.eo.will.update.ERXGenericRecord");

    /** logging support. Called before an object is reverted. **/
    public static final ERXLogger tranLogWillRevert = ERXLogger
            .getERXLogger("er.transaction.eo.will.revert.ERXGenericRecord");

    /** logging support for validation information */
    public static final ERXLogger validation = ERXLogger
            .getERXLogger("er.eo.validation.ERXGenericRecord");

    /** logging support for validation exceptions */
    public static final ERXLogger validationException = ERXLogger
            .getERXLogger("er.eo.validationException.ERXGenericRecord");

    /** logging support for insertion tracking */
    public static final ERXLogger insertionTrackingLog = ERXLogger
            .getERXLogger("er.extensions.ERXGenericRecord.insertion");

    /** general logging support */
    public static final ERXLogger log = ERXLogger
            .getERXLogger("er.eo.ERXGenericRecord");

    // DELETEME: Once we get rid of the half baked rule validation here, we can delete this.
    public final static String KEY_MARKER = "** KEY_MARKER **";

    /** This methods checks if we already have created an ERXLogger for this class
     * If not, one will be created, stored and returned on next request.
     * This method eliminates individual static variables for ERXLogger's in all
     * subclasses. We use an NSDictionary here because static fields are class specific
     * and thus something like lazy initialization would not work in this case.
     *
     * @return an {@link ERXLogger} for this objects class
     */
    public abstract ERXLogger getClassLog();

    /**
     * self is usefull for directtoweb purposes
     */
    public abstract ERXEnterpriseObject self();

    /**
     * Called as part of the augmented transaction process.
     * This method is called after saveChanges is called on
     * the editing context, but before the object is actually
     * deleted from the database. This method is also called
     * before <code>validateForDelete</code> is called on this
     * object. This method is called by the editing context
     * delegate {@link ERXDefaultEditingContextDelegate}.
     * @throws NSValidation.ValidationException to stop the object
     *		from being deleted.
     */
    public abstract void willDelete() throws NSValidation.ValidationException;

    /**
     * Called as part of the augmented transaction process.
     * This method is called after saveChanges is called on
     * the editing context, but before the object is actually
     * inserted into the database. This method is also called
     * before <code>validateForInsert</code> is called on this
     * object. This method is called by the editing context
     * delegate {@link ERXDefaultEditingContextDelegate}.
     */
    public abstract void willInsert();

    /**
     * Called as part of the augmented transaction process.
     * This method is called after saveChanges is called on
     * the editing context, but before the object is actually
     * updated in the database. This method is also called
     * before <code>validateForSave</code> is called on this
     * object. This method is called by the editing context
     * delegate {@link ERXDefaultEditingContextDelegate}.
     */
    public abstract void willUpdate();

    /**
     * This is called when an object has had
     * changes merged into it by the editing context.
     * This is called by {@link ERXDefaultEditingContextDelegate}
     * after it merges changes. Any caches that an object
     * keeps based on any of it's values it should flush.
     * The default implementation of this method does
     * nothing.
     */
    public abstract void flushCaches();

    /**
     * Called on the object after is has been deleted.
     * The editing context is passed to the object since
     * by this point the editingContext of the object is
     * null. You should check if the <code>ec</code>
     * is a child context when doing something here that
     * can't be undone.
     * @param ec editing context that used to be associated
     *		with the object.
     */
    public abstract void didDelete(EOEditingContext ec);

    /**
     * Called on the object after is has successfully
     * been updated in the database.
     */
    public abstract void didUpdate();

    /**
     * Called on the object after is has successfully
     * been inserted into the database.
     */
    public abstract void didInsert();

    /**
     * Called on the object before it will be reverted.
     *
     * Default implementation does nothing other than log.
     */
    public abstract void willRevert();

    /**
     * Called on the object after it has been reverted.
     * The editing context is passed to the object because
     * if the object was in the insertedObjects list before
     * the revert, the object has had its editingContext
     * nulled.
     *
     * Default implementation calls <code>flushCaches</code>.
     *
     * @param ec editing context that is either currently associated
     * with the object if the object was marked as changed or deleted before
     * the revert, otherwise the editing context that was associated with the object
     * before the revert.
     */
    public abstract void didRevert(EOEditingContext ec);

    /**
     * Adds a collection of objects to a given relationship by calling
     * <code>addObjectToBothSidesOfRelationshipWithKey</code> for all
     * objects in the collection.
     * @param objects objects to add to both sides of the given relationship
     * @param key relationship key
     */
    public abstract void addObjectsToBothSidesOfRelationshipWithKey(
            NSArray objects, String key);

    /**
     * Removes a collection of objects to a given relationship by calling
     * <code>removeObjectFromBothSidesOfRelationshipWithKey</code> for all
     * objects in the collection.
     * @param objects objects to be removed from both sides of the given relationship
     * @param key relationship key
     */
    public abstract void removeObjectsFromBothSidesOfRelationshipWithKey(
            NSArray objects, String key);

    /**
     * Removes a collection of objects to a given relationship by calling
     * <code>removeObjectFromPropertyWithKey</code> for all
     * objects in the collection.
     * @param objects objects to be removed from both sides of the given relationship
     * @param key relationship key
     */
    public abstract void removeObjectsFromPropertyWithKey(NSArray objects,
            String key);

    /**
     * Primary key of the object as a String.
     * @return primary key for the given object as a String
     */
    public abstract String primaryKey();

    /**
     * Calling this method will return the primary key of the
     * given enterprise object or if one has not been asigned
     * to it yet, then it will have the adaptor channel generate
     * one for it, cache it and then use that primary key when it
     * is saved to the database. If you just want the
     * primary key of the object or null if it doesn't have one
     * yet, use the method <code>rawPrimaryKey</code>.
     * @return the primary key of this object.
     */
    public abstract Object rawPrimaryKeyInTransaction();

    /**
     * Calling this method will return the primary key of the
     * given enterprise object or if one has not been assigned
     * to it yet, then it will have the adaptor channel generate
     * one for it, cache it and then use that primary key when it
     * is saved to the database. This method returns the string
     * representation of the primary key. If you just want the
     * primary key of the object or null if it doesn't have one
     * yet, use the method <code>primaryKey</code>.
     * @return string representation of the primary key of this
     *		object.
     */
    public abstract String primaryKeyInTransaction();

    /**
     * Gives the raw primary key of the object. This could be anything from
     * an NSData to a BigDecimal.
     * @return the raw primary key of this object.
     */
    public abstract Object rawPrimaryKey();

    /**
     * Takes the primary key of the object and encrypts it
     * with the blowfish cipher using {@link ERXCrypto ERXCrypto}.
     * @return blowfish encrypted primary key
     */
    public abstract String encryptedPrimaryKey();

    /**
     * Returns the foreign key for a given relationship.
     * @param rel relationship key
     * @return foreign key for a given relationship.
     */
    public abstract Object foreignKeyForRelationshipWithKey(String rel);

    /**
     * Returns the names of all primary key attibutes. 
     * @return
     */
    public abstract NSArray primaryKeyAttributeNames();

    /**
     * Determines what the value of the given key is in the committed
     * snapshot
     * @param key to be checked in committed snapshot
     * @return the committed snapshot value for the given key
     */
    public abstract Object committedSnapshotValueForKey(String key);

    /**
     * Returns an EO in the same editing context as the caller.
     * @return an EO in the same editing context as the caller.
     */
    public abstract EOEnterpriseObject localInstanceOf(EOEnterpriseObject eo);

    /**
     * Returns an array of EOs in the same editing context as the caller.
     * @return  array of EOs in the same editing context as the caller.
     */
    public abstract NSArray localInstancesOf(NSArray eos);

    /**
     * Computes the current set of changes that this object has from the
     * currently committed snapshot.
     * @return a dictionary holding the changed values from the currently
     *         committed snapshot.
     */
    public abstract NSDictionary changesFromCommittedSnapshot();

    /**
     * Simple method that will return if the parent object store of this object's editing
     * context is an instance of {@link com.webobjects.eocontrol.EOObjectStoreCoordinator EOObjectStoreCoordinator}. The reason this is important
     * is because if this condition evaluates to true then when changes are saved in this
     * editing context they will be propagated to the database.
     * @return if the parent object store of this object's editing context is an EOObjectStoreCoordinator.
     */
    public abstract boolean parentObjectStoreIsObjectStoreCoordinator();

    /**
     * Method that will make sure to fetch an eo from the Database and
     * place it in the editingContext provided
     * as an argument
     * @param the editing context in which the result will be placed
     * @return a fresh instance of an EO fetched from the DB and placed in the editing context argument
     */
    public abstract ERXEnterpriseObject refetchObjectFromDBinEditingContext(
            EOEditingContext ec);

    /**
     * Cover method to return <code>toString</code>.
     * @return the results of calling toString.
     */
    public abstract String description();

    /**
     * Returns the super classes implementation of toString
     * which prints out the current key-value pairs for all
     * of the attributes and relationships for the current
     * object. Very verbose.
     * @return super's implementation of <code>toString</code>.
     */
    public abstract String toLongString();

    /**
     * This method will trim the leading and trailing white
     * space from any attributes that are mapped to a String
     * object. This method is called before the object is saved
     * to the database. Override this method to do nothing if
     * you wish to preserve your leading and trailing white space.
     */
    public abstract void trimSpaces();

    /**
     * Determines if this object is a deleted object by
     * checking to see if it is included in the deletedObjects
     * array of the editing context or if it's editing context
     * is null.<br/>
     * <br/>
     * Note: An object that has just been created will also not
     * have an editing context and by this method would test
     * positive for being a deleted object.
     * @return if the object is a deleted object
     */
    public abstract boolean isDeletedEO();

    /**
     * Determines if this object is a new object and
     * hasn't been saved to the database yet. This
     * method just calls the method ERExtensions.isNewObject
     * passing in this object as the current parameter. Note
     * that an object that has been successfully deleted will
     * also look as if it is a new object because it will have
     * a null editingcontext.
     * @return if the object is a new enterprise object.
     */

    public abstract boolean isNewObject();

    /**
     * Debugging method that will be called on an object before it is
     * saved to the database if the property key: <b>ERDebuggingEnabled</b>
     * is enabled. This allows for adding in a bunch of expensive validation
     * checks that should only be enabled in developement and testing
     * environments.
     * @throws NSValidation.ValidationException if the object is not consistent
     */
    // CHECKME: This method was very useful at NS, might not be as useful here.
    public abstract void checkConsistency()
            throws NSValidation.ValidationException;

    /**
     * This method is very similiar to the <code>checkConsistency</code> method
     * except that this method is only called from an outside process, usually
     * a batch process, to verify that the data this object holds is consistent.
     * JUnit tests are great for testing that all of the methods of a single
     * object function correctly, batch checking of consistency is a good way
     * of checking that all of the data in a given database is consistent. Hopefully
     * in the future we will add a batch check consistency application to demonstrate
     * the use of this method.
     * @throws NSValidation.ValidationException if the object fails consisntency
     */
    public abstract void batchCheckConsistency()
            throws NSValidation.ValidationException;

}