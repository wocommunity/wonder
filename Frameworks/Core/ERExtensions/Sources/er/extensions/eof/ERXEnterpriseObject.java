package er.extensions.eof;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSValidation;

import er.extensions.ERXExtensions;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSelectorUtilities;
import er.extensions.foundation.ERXSystem;

public interface ERXEnterpriseObject extends EOEnterpriseObject {
    
    /** logging support for modified objects */
    public static final Logger logMod = Logger.getLogger("er.transaction.delegate.EREditingContextDelegate.modifiedObjects");

    public static final boolean applyRestrictingQualifierOnInsert = ERXProperties.booleanForKey("er.extensions.ERXEnterpriseObject.applyRestrictingQualifierOnInsert");
    

    /**
     * Registers as a listener for various editing context notifications and calls up the willXXX and
     * didXXX methods in objects that implement ERXEnterpriseObject. Subclass this and set the system
     * property <code>er.extensions.ERXEnterpriseObject.Observer.className</code> to set up your own subclass.
     *
     * @author ak
     */
    public static class Observer {
        
        public void editingContextWillSaveChanges(NSNotification n) {
            EOEditingContext ec = (EOEditingContext) n.object();
                    boolean isNestedEditingContext = (ec.parentObjectStore() instanceof EOEditingContext);

                    ec.processRecentChanges(); // need to do this to make sure the updated objects list is current

            if (ec.hasChanges()) {
                NSNotificationCenter.defaultCenter().postNotification(ERXExtensions.objectsWillChangeInEditingContext, ec);
                // we don't need to lock ec because we can assume that we're locked
                // before this method is called, but we do need to lock our parent

                if (isNestedEditingContext) {
                    EOEditingContext parentEC = (EOEditingContext) ec.parentObjectStore();

                    parentEC.lock();

                    try {
                        if (ec.deletedObjects().count() > 0) {
                            final NSArray deletedObjectsToFlushInParent = ERXEOControlUtilities.localInstancesOfObjects(parentEC, ec.deletedObjects());

                            if (log.isDebugEnabled()) {
                                log.debug("saveChanges: before save to child context " + ec
                                        + ", need to flush caches on deleted objects in parent context " + parentEC + ": "
                                        + deletedObjectsToFlushInParent);
                            }
                            ERXEnterpriseObject.FlushCachesProcessor.perform(parentEC, deletedObjectsToFlushInParent);
                        }

                    } finally {
                        parentEC.unlock();
                    }
                }
                ERXEnterpriseObject.WillUpdateProcessor.perform(ec, ec.updatedObjects());
                ERXEnterpriseObject.WillDeleteProcessor.perform(ec, ec.deletedObjects());
                ERXEnterpriseObject.WillInsertProcessor.perform(ec, ec.insertedObjects());

                if (log.isDebugEnabled()) log.debug("EditingContextWillSaveChanges: done calling will*");
                if (logMod.isDebugEnabled()) {
                    if (ec.updatedObjects()!=null) logMod.debug("** Updated Objects "+ ec.updatedObjects().count()+" - "+ ec.updatedObjects());
                    if (ec.insertedObjects()!=null) logMod.debug("** Inserted Objects "+ ec.insertedObjects().count()+" - "+ ec.insertedObjects());
                    if (ec.deletedObjects()!=null) logMod.debug("** Deleted Objects "+ ec.deletedObjects().count()+" - "+ ec.deletedObjects());
                }
            }
       }

        public void editingContextDidSaveChanges(NSNotification n) {
            EOEditingContext ec = (EOEditingContext) n.object();
            final boolean isNestedEditingContext = (ec.parentObjectStore() instanceof EOEditingContext);
            
            NSArray insertedObjects = (NSArray)n.userInfo().objectForKey("inserted");
            NSArray updatedObjects = (NSArray)n.userInfo().objectForKey("updated");
            NSArray deletedObjects = (NSArray)n.userInfo().objectForKey("deleted");
            
            ERXEnterpriseObject.DidUpdateProcessor.perform(ec, updatedObjects);
            ERXEnterpriseObject.DidDeleteProcessor.perform(ec, deletedObjects);
            ERXEnterpriseObject.DidInsertProcessor.perform(ec, insertedObjects);

            if ( isNestedEditingContext ) {
                // we can assume insertedObjectGIDs and updatedObjectGIDs are non null.  if we execute this branch, they're at
                // least empty arrays.
                final EOEditingContext parentEC = (EOEditingContext)ec.parentObjectStore();

                if (insertedObjects.count() > 0 || updatedObjects.count() > 0) {
                    NSMutableArray flushableObjects = new NSMutableArray();
                    flushableObjects.addObjectsFromArray(insertedObjects);
                    flushableObjects.addObjectsFromArray(updatedObjects);

                    parentEC.lock();
                    try {
                        final NSArray flushableObjectsInParent = ERXEOControlUtilities.localInstancesOfObjects(parentEC, flushableObjects);

                        if ( log.isDebugEnabled() ) {
                            log.debug("saveChanges: before save to child context " + ec +
                                    ", need to flush caches on objects in parent context " + parentEC + ": " + flushableObjectsInParent);
                        }

                        ERXEnterpriseObject.FlushCachesProcessor.perform(parentEC, flushableObjectsInParent);
                    } finally {
                        parentEC.unlock();
                    }
                }
            }
        }

        private static volatile Observer observer;
        
        public static void install()  {
            if(observer == null) {
                synchronized(Observer.class) {
                    if(observer == null) {
                        String name = ERXSystem.getProperty("er.extensions.ERXEnterpriseObject.Observer.className", Observer.class.getName());
                        Class c = ERXPatcher.classForName(name);
                        try {
                            observer = (Observer) c.newInstance();
                            NSNotificationCenter.defaultCenter().addObserver(observer, 
                                    ERXSelectorUtilities.notificationSelector("editingContextWillSaveChanges"), 
                                    ERXEC.EditingContextWillSaveChangesNotification, null);
                            NSNotificationCenter.defaultCenter().addObserver(observer, 
                                    ERXSelectorUtilities.notificationSelector("editingContextDidSaveChanges"), 
                                    ERXEC.EditingContextDidSaveChangesNotification, null);
                        } catch (InstantiationException e) {
                            throw NSForwardException._runtimeExceptionForThrowable(e);
                        } catch (IllegalAccessException e) {
                            throw NSForwardException._runtimeExceptionForThrowable(e);
                        }
                    }
                }
            }
        }
    }
     
    public static abstract class Processor {
        
        protected abstract void perform(EOEditingContext ec, ERXEnterpriseObject eo);
        
        public void perform(EOEditingContext ec, NSArray eos) {
            if(eos != null && eos.count() > 0) { 
                for (Enumeration enumerator = eos.objectEnumerator(); enumerator.hasMoreElements();) {
                    EOEnterpriseObject eo = (EOEnterpriseObject) enumerator.nextElement();
                    if(eo instanceof ERXEnterpriseObject) {
                        perform(ec, (ERXEnterpriseObject)eo);
                    }
                }
            }
        }

        public void perform(EOEditingContext ec, EOEnterpriseObject eo) {
            if(eo instanceof ERXEnterpriseObject) {
                perform(ec, (ERXEnterpriseObject)eo);
            }
        }
    }

    public static Processor FlushCachesProcessor = new Processor() {
        @Override
        protected void perform(EOEditingContext ec, ERXEnterpriseObject eo) {
            eo.flushCaches();
        }
    };

    public static Processor WillInsertProcessor = new Processor() {
        @Override
        protected void perform(EOEditingContext ec, ERXEnterpriseObject eo) {
            eo.willInsert();
        }
    };

    public static Processor DidInsertProcessor = new Processor() {
        @Override
        protected void perform(EOEditingContext ec, ERXEnterpriseObject eo) {
            eo.didInsert();
        }
    };

    public static Processor WillUpdateProcessor = new Processor() {
        @Override
        protected void perform(EOEditingContext ec, ERXEnterpriseObject eo) {
            eo.willUpdate();
        }
    };

    public static Processor DidUpdateProcessor = new Processor() {
        @Override
        protected void perform(EOEditingContext ec, ERXEnterpriseObject eo) {
            eo.didUpdate();
        }
    };

    public static Processor WillDeleteProcessor = new Processor() {
        @Override
        protected void perform(EOEditingContext ec, ERXEnterpriseObject eo) {
            eo.willDelete();
        }
    };

    public static Processor DidDeleteProcessor = new Processor() {
        @Override
        protected void perform(EOEditingContext ec, ERXEnterpriseObject eo) {
            eo.didDelete(ec);
        }
    };

    public static Processor WillRevertProcessor = new Processor() {
        @Override
        protected void perform(EOEditingContext ec, ERXEnterpriseObject eo) {
            eo.willRevert();
        }
    };

    public static Processor DidRevertProcessor = new Processor() {
        @Override
        protected void perform(EOEditingContext ec, ERXEnterpriseObject eo) {
            eo.didRevert(ec);
        }
    };
 
    /** logging support. Called after an object is successfully inserted */
    public static final Logger tranLogDidInsert = Logger
            .getLogger("er.transaction.eo.did.insert.ERXGenericRecord");

    /** logging support. Called after an object is successfully deleted */
    public static final Logger tranLogDidDelete = Logger
            .getLogger("er.transaction.eo.did.delete.ERXGenericRecord");

    /** logging support. Called after an object is successfully updated */
    public static final Logger tranLogDidUpdate = Logger
            .getLogger("er.transaction.eo.did.update.ERXGenericRecord");

    /** logging support. Called after an object is reverted. **/
    public static final Logger tranLogDidRevert = Logger
            .getLogger("er.transaction.eo.did.revert.ERXGenericRecord");

    /** logging support. Called before an object is deleted */
    public static final Logger tranLogMightDelete = Logger
            .getLogger("er.transaction.eo.might.delete.ERXGenericRecord");

    /** logging support. Called before an object is inserted */
    public static final Logger tranLogWillInsert = Logger
            .getLogger("er.transaction.eo.will.insert.ERXGenericRecord");

    /** logging support. Called before an object is deleted */
    public static final Logger tranLogWillDelete = Logger
            .getLogger("er.transaction.eo.will.delete.ERXGenericRecord");

    /** logging support. Called before an object is updated */
    public static final Logger tranLogWillUpdate = Logger
            .getLogger("er.transaction.eo.will.update.ERXGenericRecord");

    /** logging support. Called before an object is reverted. **/
    public static final Logger tranLogWillRevert = Logger
            .getLogger("er.transaction.eo.will.revert.ERXGenericRecord");

    /** logging support for validation information */
    public static final Logger validation = Logger
            .getLogger("er.eo.validation.ERXGenericRecord");

    /** logging support for validation exceptions */
    public static final Logger validationException = Logger
            .getLogger("er.eo.validationException.ERXGenericRecord");

    /** logging support for insertion tracking */
    public static final Logger insertionTrackingLog = Logger
            .getLogger("er.extensions.ERXGenericRecord.insertion");

    /** general logging support */
    public static final Logger log = Logger
            .getLogger("er.eo.ERXGenericRecord");

    // DELETEME: Once we get rid of the half baked rule validation here, we can delete this.
    public final static String KEY_MARKER = "** KEY_MARKER **";

    /** This methods checks if we already have created an Logger for this class
     * If not, one will be created, stored and returned on next request.
     * This method eliminates individual static variables for Logger's in all
     * subclasses. We use an NSDictionary here because static fields are class specific
     * and thus something like lazy initialization would not work in this case.
     *
     * @return an {@link Logger} for this objects class
     */
    public abstract Logger getClassLog();

    /**
     * self is useful for D2W purposes
     * 
     * @return the EO itself
     */
    public abstract ERXEnterpriseObject self();

    /**
     * Called as part of the augmented transaction process.
     * This method is called when deleteObject() is called on
     * the editing context. The benefit over willDelete() is that in this 
     * method, the relationships are still intact. Mostly, at least,
     * as it's also called when the deletes cascade.
     */
    public abstract void mightDelete();

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
            NSArray<? extends EOEnterpriseObject> objects, String key);

    /**
     * Removes a collection of objects to a given relationship by calling
     * <code>removeObjectFromBothSidesOfRelationshipWithKey</code> for all
     * objects in the collection.
     * @param objects objects to be removed from both sides of the given relationship
     * @param key relationship key
     */
    public abstract void removeObjectsFromBothSidesOfRelationshipWithKey(
            NSArray<? extends EOEnterpriseObject> objects, String key);

    /**
     * Removes a collection of objects to a given relationship by calling
     * <code>removeObjectFromPropertyWithKey</code> for all
     * objects in the collection.
     * @param objects objects to be removed from both sides of the given relationship
     * @param key relationship key
     */
    public abstract void removeObjectsFromPropertyWithKey(NSArray<? extends EOEnterpriseObject> objects,
            String key);

    /**
     * Primary key of the object as a String.
     * @return primary key for the given object as a String
     */
    public abstract String primaryKey();

    /**
     * Calling this method will return the primary key of the
     * given enterprise object or if one has not been assigned
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
     * with the blowfish cipher using {@link er.extensions.crypting.ERXCrypto ERXCrypto}.
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
     * Returns the names of all primary key attributes.
     * 
     * @return list of attribute names
     */
    public abstract NSArray<String> primaryKeyAttributeNames();

    /**
     * Determines what the value of the given key is in the committed
     * snapshot
     * @param key to be checked in committed snapshot
     * @return the committed snapshot value for the given key
     */
    public abstract Object committedSnapshotValueForKey(String key);

    /**
     * Returns an EO in the same editing context as the caller.
     * 
     * @param eo to local instance
     * @return an EO in the same editing context as the caller.
     */
    public abstract <T extends EOEnterpriseObject> T localInstanceOf(T eo);

    /**
     * Returns this EO in the supplied editing context.
     * 
     * @param ec editing context to local instance in
     * @return this EO in the supplied editing context.
     */
	public EOEnterpriseObject localInstanceIn(EOEditingContext ec);

    /**
     * Returns an array of EOs in the same editing context as the caller.
     * 
     * @param eos array of EOs to local instance
     * @return array of EOs in the same editing context as the caller.
     */
    public abstract <T extends EOEnterpriseObject> NSArray<T> localInstancesOf(NSArray<T> eos);

    /**
     * Computes the current set of changes that this object has from the
     * currently committed snapshot.
     * @return a dictionary holding the changed values from the currently
     *         committed snapshot.
     */
    public abstract NSDictionary<String, Object> changesFromCommittedSnapshot();

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
     * @param ec the editing context in which the result will be placed
     * @return fresh instance of an EO fetched from the DB and placed in the editing context argument
     */
    public abstract ERXEnterpriseObject refetchObjectFromDBinEditingContext(
            EOEditingContext ec);

    /**
     * Cover method to return <code>toString</code>.
     * @return the results of calling toString.
     * @deprecated use {@link #toString()} instead
     */
    @Deprecated
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
     * array of the editing context or - if it's editing context
     * is null - it already has a global id. 
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
     * a null editing context.
     * @return if the object is a new enterprise object.
     */
    public abstract boolean isNewObject();

    /**
     * Debugging method that will be called on an object before it is
     * saved to the database if the property key: <b>ERDebuggingEnabled</b>
     * is enabled. This allows for adding in a bunch of expensive validation
     * checks that should only be enabled in development and testing
     * environments.
     * @throws NSValidation.ValidationException if the object is not consistent
     */
    @Deprecated
    public abstract void checkConsistency()
            throws NSValidation.ValidationException;

    /**
     * This method is very similar to the <code>checkConsistency</code> method
     * except that this method is only called from an outside process, usually
     * a batch process, to verify that the data this object holds is consistent.
     * JUnit tests are great for testing that all of the methods of a single
     * object function correctly, batch checking of consistency is a good way
     * of checking that all of the data in a given database is consistent. Hopefully
     * in the future we will add a batch check consistency application to demonstrate
     * the use of this method.
     * @throws NSValidation.ValidationException if the object fails consistency
     */
    @Deprecated
    public abstract void batchCheckConsistency()
            throws NSValidation.ValidationException;

	
    /**
     * Toggles whether or not inverse relationships should be updates.  This is
     * called by ERXGenericRecord.InverseRelationshipUpdater to prevent infinite
     * loops and should not be called by anything else unless you know exactly
     * what you are doing.
     * 
     * @param newValue whether or not inverse relationships should be updated
     * @return the previous setting of the updateInverseRelationships setting
     */
	public abstract boolean _setUpdateInverseRelationships(boolean newValue);
}
