//
//  ERXEC.java
//  ERExtensions
//
//  Created by Max Muller on Sun Feb 23 2003.
//
package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

/**
* Factory for creating editing contexts.
 */
public class ERXEC extends EOEditingContext {
    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXEC.class);
    public static final ERXLogger lockLogger = ERXLogger.getERXLogger("er.extensions.ERXEC.LockLogger");
    public static final ERXLogger lockLoggerTrace = ERXLogger.getERXLogger("er.extensions.ERXEC.LockLoggerTrace");
    public static final ERXLogger lockTrace = ERXLogger.getERXLogger("er.extensions.ERXEC.LockTrace");
    /** name of the notification that is posted after editing context is created */
    public static final String EditingContextDidCreateNotification = "EOEditingContextDidCreate";

    private boolean automaticLockUnlock = false;
    private boolean automaticLockUnlockSet = false;
    private int lockCount = 0;
    private boolean isFinalizing;
    
    public static interface Factory {
        public Object defaultEditingContextDelegate();
        public void setDefaultEditingContextDelegate(Object delegate);
        public Object defaultNoValidationDelegate();
        public void setDefaultNoValidationDelegate(Object delegate);
        public void setDefaultDelegateOnEditingContext(EOEditingContext ec);
        public void setDefaultDelegateOnEditingContext(EOEditingContext ec, boolean validation);
        public EOEditingContext _newEditingContext();
        public EOEditingContext _newEditingContext(boolean validationEnabled);
        public EOEditingContext _newEditingContext(EOObjectStore objectStore);
        public EOEditingContext _newEditingContext(EOObjectStore objectStore, boolean validationEnabled);
    }
    /** default constructor */
    public ERXEC() {
        super();
    }
    /** alternative constructor */
    public ERXEC(EOObjectStore os) {
        super(os);
    }

    public static boolean defaultAutomaticLockUnlock() {
        return "true".equals(System.getProperty("er.extensions.ERXEC.defaultAutomaticLockUnlock"));
    }
    
    public void deleteObjects(NSArray objects) {
    	for (int i = objects.count(); i-- > 0;) {
    		Object o = objects.objectAtIndex(i);
    		if (o instanceof EOEnterpriseObject) {
    			EOEnterpriseObject eo = (EOEnterpriseObject)o;
    			if (eo.editingContext() != null) {
    				eo.editingContext().deleteObject(eo);
    			}
    		}
    	}
    }

    public boolean automaticLockUnlock() {
        if (!automaticLockUnlockSet) {
            automaticLockUnlock = defaultAutomaticLockUnlock();
            automaticLockUnlockSet = true;
        }
        return automaticLockUnlock;
    }
    public void setAutomaticLockUnlock(boolean value) {
        automaticLockUnlock = value;
        automaticLockUnlockSet = true;
    }
    
    public int lockCount() { return lockCount; }
    public void lock() {
        lockCount++;
        super.lock();
        if (!autoLocked && lockLogger.isDebugEnabled()) {
            if(lockTrace.isDebugEnabled()) {
                lockLogger.debug("locked "+this, new Exception());
            } else {
                lockLogger.debug("locked "+this);
            }
        }
    }
    public void unlock() {
        super.unlock();
        if (!autoLocked && lockLogger.isDebugEnabled()) {
            if(lockTrace.isDebugEnabled()) {
                lockLogger.debug("unlocked "+this, new Exception());
            } else {
                lockLogger.debug("unlocked "+this);
            }
        }
        lockCount--;
    }

    public boolean autoLock(String method) {
        boolean unlock = false;
        if (lockCount == 0 && automaticLockUnlock()) {
            unlock = true;
            lock();
            if (!autoLocked && !isFinalizing) {
                if(lockLoggerTrace.isDebugEnabled()) {
                    lockLoggerTrace.debug("called method " + method + " without a lock, ec="+this, new Exception());
                } else {
                    lockLogger.warn("called method " + method + " without a lock, ec="+this);
                }
            }
        }
        return unlock;
    }

    public void finalize() throws Throwable {
        isFinalizing = true;
        super.finalize();
    }
    
    public void reset() {
        boolean unlock = autoLock("reset");
        try {
            super.reset();
        } finally {
            if (unlock) unlock();
        }
    }
    public void recordObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid) {
        boolean unlock = autoLock("recordObject");
        try {
            super.recordObject(eoenterpriseobject, eoglobalid);
        } finally {
            if (unlock) unlock();
        }
    }
    public void forgetObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("forgetObject");
        try {
            super.forgetObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    public void _undoUpdate(Object obj) {
        super._undoUpdate(obj);
        //EOEnterpriseObject eoenterpriseobject = (EOEnterpriseObject)Array.get(obj, 0);
        //NSDictionary nsdictionary = (NSDictionary)Array.get(obj, 1);
        //eoenterpriseobject.updateFromSnapshot(nsdictionary);
    }
    public void processRecentChanges() {
        boolean unlock = autoLock("processRecentChanges");
        try {
            super.processRecentChanges();
        } finally {
            if (unlock) unlock();
        }
    }
    public NSArray updatedObjects() {
        boolean unlock = autoLock("updatedObjects");
        try {
            return super.updatedObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    public NSArray registeredObjects() {
        boolean unlock = autoLock("registeredObjects");
        try {
            return super.registeredObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    public NSArray insertedObjects() {
        boolean unlock = autoLock("insertedObjects");
        try {
            return super.insertedObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    public NSArray deletedObjects() {
        boolean unlock = autoLock("deletedObjects");
        try {
            return super.deletedObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    public void setSharedEditingContext(EOSharedEditingContext eosharededitingcontext) {
        boolean unlock = autoLock("setSharedEditingContext");
        try {
            super.setSharedEditingContext(eosharededitingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    public EOEnterpriseObject objectForGlobalID(EOGlobalID eoglobalid) {
        boolean unlock = autoLock("objectForGlobalID");
        try {
            return super.objectForGlobalID(eoglobalid);
        } finally {
            if (unlock) unlock();
        }
    }
    public EOGlobalID globalIDForObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("globalIDForObject");
        try {
            return super.globalIDForObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    public NSDictionary committedSnapshotForObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("committedSnapshotForObject");
        try {
            return super.committedSnapshotForObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    public NSDictionary currentEventSnapshotForObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("currentEventSnapshotForObject");
        try {
            return super.currentEventSnapshotForObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    public void objectWillChange(Object obj) {
        boolean unlock = autoLock("objectWillChange");
        try {
            super.objectWillChange(obj);
        } finally {
            if (unlock) unlock();
        }
    }
    public void insertObjectWithGlobalID(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid) {
        boolean unlock = autoLock("insertObjectWithGlobalID");
        try {
            super.insertObjectWithGlobalID(eoenterpriseobject, eoglobalid);
        } finally {
            if (unlock) unlock();
        }
    }
    public void insertObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("insertObject");
        try {
            super.insertObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    public void deleteObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("deleteObject");
        try {
            super.deleteObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    public boolean hasChanges() {
        boolean unlock = autoLock("hasChanges");
        try {
            return super.hasChanges();
        } finally {
            if (unlock) unlock();
        }
    }
    public void saveChanges() {
        boolean unlock = autoLock("saveChanges");
        try {
            super.saveChanges();
        } finally {
            if (unlock) unlock();
        }
    }
    public EOEnterpriseObject faultForGlobalID(EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("faultForGlobalID");
        try {
            return super.faultForGlobalID(eoglobalid, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    public NSArray arrayFaultWithSourceGlobalID(EOGlobalID eoglobalid, String s, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("arrayFaultWithSourceGlobalID");
        try {
            return super.arrayFaultWithSourceGlobalID(eoglobalid, s, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    public void initializeObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("initializeObject");
        try {
            super.initializeObject(eoenterpriseobject, eoglobalid, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    public void editingContextDidForgetObjectWithGlobalID(EOEditingContext eoeditingcontext, EOGlobalID eoglobalid) {
        boolean unlock = autoLock("editingContextDidForgetObjectWithGlobalID");
        try {
            super.editingContextDidForgetObjectWithGlobalID(eoeditingcontext, eoglobalid);
        } finally {
            if (unlock) unlock();
        }
    }
    public NSArray objectsForSourceGlobalID(EOGlobalID eoglobalid, String s, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("objectsForSourceGlobalID");
        try {
            return super.objectsForSourceGlobalID(eoglobalid, s, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    public void refaultObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("refaultObject");
        try {
            super.refaultObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    public void refaultObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("refaultObject");
        try {
            super.refaultObject(eoenterpriseobject, eoglobalid, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    public NSArray objectsWithFetchSpecification(EOFetchSpecification eofetchspecification, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("objectsWithFetchSpecification");
        try {
            return super.objectsWithFetchSpecification(eofetchspecification, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    public void saveChangesInEditingContext(EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("saveChangesInEditingContext");
        try {
            super.saveChangesInEditingContext(eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    public void refaultAllObjects() {
        boolean unlock = autoLock("refaultAllObjects");
        try {
            super.refaultAllObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    public void invalidateObjectsWithGlobalIDs(NSArray nsarray) {
        boolean unlock = autoLock("invalidateObjectsWithGlobalIDs");
        try {
            super.invalidateObjectsWithGlobalIDs(nsarray);
        } finally {
            if (unlock) unlock();
        }
    }
    public void invalidateAllObjects() {
        boolean unlock = autoLock("invalidateAllObjects");
        try {
            super.invalidateAllObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    public void lockObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("lockObject");
        try {
            super.lockObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    public void revert() {
        boolean unlock = autoLock("revert");
        try {
            super.revert();
        } finally {
            if (unlock) unlock();
        }
    }
    public void saveChanges(Object obj) {
        boolean unlock = autoLock("saveChanges");
        try {
            super.saveChanges(obj);
        } finally {
            if (unlock) unlock();
        }
    }
    public void refreshObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("refreshObject");
        try {
            super.refreshObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    public void undo() {
        boolean unlock = autoLock("undo");
        try {
            super.undo();
        } finally {
            if (unlock) unlock();
        }
    }
    public void redo() {
        boolean unlock = autoLock("redo");
        try {
            super.redo();
        } finally {
            if (unlock) unlock();
        }
    }
    public Object invokeRemoteMethod(EOEditingContext eoeditingcontext, EOGlobalID eoglobalid, String s, Class aclass[], Object aobj[]) {
        boolean unlock = autoLock("invokeRemoteMethod");
        try {
            return super.invokeRemoteMethod(eoeditingcontext, eoglobalid, s, aclass, aobj);
        } finally {
            if (unlock) unlock();
        }
    }
    public void setDelegate(Object d) {
        if (log.isDebugEnabled()) {
            log.debug("setting delegate to "+d);
            log.debug(ERXUtilities.stackTrace());
        }
        super.setDelegate(d);
    }

    public boolean autoLocked = false;
    public void setAutoLocked(boolean v) {
        autoLocked = v;
    }
    public boolean isAutoLocked() {
        return autoLocked;
    }
        public static class DefaultFactory implements Factory {
            /** logging support */
            public static final ERXLogger log = ERXLogger.getERXLogger(DefaultFactory.class);
            /** holds a reference to the default ec delegate */
            protected Object defaultEditingContextDelegate;
            /** holds a reference to the default no validation delegate */
            protected Object defaultNoValidationDelegate;
            public DefaultFactory() {
                // Initing defaultEditingContext delegates
                defaultEditingContextDelegate = new ERXDefaultEditingContextDelegate();
                defaultNoValidationDelegate = new ERXECNoValidationDelegate();
            }
            /**
                * Returns the default editing context delegate.
             * This delegate is used by default for all editing
             * contexts that are created.
             * @return the default editing context delegate
             */
            public Object defaultEditingContextDelegate() { return defaultEditingContextDelegate; }
            /**
                * Sets the default editing context delegate to be
             * used for editing context creation.
             * @param delegate to be set on every created editing
             *		context by default.
             */
            public void setDefaultEditingContextDelegate(Object delegate) {
                defaultEditingContextDelegate = delegate;
                if (log.isDebugEnabled()) {
                    log.debug("setting defaultEditingContextDelegate to "+delegate);
                }
            }
            /**
                * Default delegate that does not perform validation.
             * Not performing validation can be a good thing when
             * using nested editing contexts as sometimes you only
             * want to validation one object, not all the objects.
             * @return default delegate that doesn't perform validation
             */
            public Object defaultNoValidationDelegate() { return defaultNoValidationDelegate; }
            /**
                * Sets the default editing context delegate to be
             * used for editing context creation that does not
             * allow validation.
             * @param delegate to be set on every created editing
             *		context that doesn't allow validation.
             */
            public void setDefaultNoValidationDelegate(Object delegate) {
                defaultNoValidationDelegate = delegate;
            }
            /**
                * Sets either the default editing context delegate
             * that does or does not allow validation based on
             * the validation flag passed in on the given editing context.
             * @param ec editing context to have it's delegate set.
             * @param validation flag that determines if the editing context
             * 		should perform validation on objects being saved.
             */
            public void setDefaultDelegateOnEditingContext(EOEditingContext ec, boolean validation) {
                if (log.isDebugEnabled()) {
                    log.debug("Setting default delegate on editing context: " + ec
                              + " allows validation: " + validation);
                }
                if (ec != null) {
                    if (validation) {
                        ec.setDelegate(defaultEditingContextDelegate());
                    } else {
                        ec.setDelegate(defaultNoValidationDelegate());
                    }
                } else {
                    log.warn("Attempting to set a default delegate on a null ec!");
                }
            }
            /**
                * Sets the default editing context delegate on
             * the given editing context.
             * @param ec editing context to have it's delegate set.
             */
            public void setDefaultDelegateOnEditingContext(EOEditingContext ec) {
                setDefaultDelegateOnEditingContext(ec, true);
            }
            /**
                * See static method for documentation.
             */
            public EOEditingContext _newEditingContext() {
                return _newEditingContext(EOEditingContext.defaultParentObjectStore(), true);
            }
            /**
                * See static method for documentation.
             */
            public EOEditingContext _newEditingContext(boolean validationEnabled) {
                return _newEditingContext(EOEditingContext.defaultParentObjectStore(), validationEnabled);
            }
            /**
                * See static method for documentation.
             */
            public EOEditingContext _newEditingContext(EOObjectStore objectStore) {
                return _newEditingContext(objectStore, true);
            }
            /**
                * See static method for documentation.
             */
            public EOEditingContext _newEditingContext(EOObjectStore objectStore, boolean validationEnabled) {
                EOEditingContext ec = _createEditingContext(objectStore);
                int levelsOfUndo = ERXValueUtilities.intValueWithDefault(System.getProperty("WODefaultUndoStackLimit"), 10);
                if (levelsOfUndo == 0) {
                    ec.setUndoManager(null);
                } else {
                    ec.undoManager().setLevelsOfUndo(levelsOfUndo < 0 ? 10 : levelsOfUndo);
                }
                setDefaultDelegateOnEditingContext(ec, validationEnabled);
                if (!useSharedEditingContext()) {
                    ec.lock();
                    ec.setSharedEditingContext(null);
                    ec.unlock();
                }
                NSNotificationCenter.defaultCenter().postNotification(EditingContextDidCreateNotification, ec);
                return ec;
            }
            /** Actual EC creation bottleneck. Override this to return other subclasses. */
            protected EOEditingContext _createEditingContext(EOObjectStore parent) {
                return new ERXEC(parent == null ? EOEditingContext.defaultParentObjectStore() : parent);
            }
            public Boolean _useSharedEditingContext = null;
            public boolean useSharedEditingContext() {
                if (_useSharedEditingContext == null) {
                    _useSharedEditingContext = !"false".equals(System.getProperty("er.extensions.ERXEC.useSharedEditingContext")) ? Boolean.TRUE : Boolean.FALSE;
                    log.info("setting useSharedEditingContext to "+_useSharedEditingContext);
                }
                return _useSharedEditingContext.booleanValue();
            }
        }
    /** holds a reference to the factory used to create editing contexts */
    protected static Factory factory;
    /**
        * Gets the factory used to create editing contexts
     * @return editing context factory
     */
    public static Factory factory() {
        if (factory == null) {
            factory = new DefaultFactory();
        }
        return factory;
    }
    /**
        * Sets the default editing context factory
     * @param factory factory used to create editing contexts
     */
    public static void setFactory(Factory aFactory) {
        factory = aFactory;
    }
    /**
        * Factory method to create a new editing context. Sets
     * the current default delegate on the newly created
     * editing context.
     * @return a newly created editing context with the
     *		default delegate set.
     */
    public static EOEditingContext newEditingContext() {
        return factory()._newEditingContext();
    }
    /**
        * Creates a new editing context with the specified object
     * store as the parent object store and with validation turned
     * on or off depending on the flag passed in. This method is useful
     * when creating nested editing contexts. After creating
     * the editing context the default delegate is set on the
     * editing context if validation is enabled or the default no
     * validation delegate is set if validation is disabled.<br/>
     * <br/>
     * Note: an {@link com.webobjects.eocontrol.EOEditingContext EOEditingContext} is a subclass of EOObjectStore
     * so passing in another editing context to this method is
     * completely kosher.
     * @param parent object store for the newly created
     *		editing context.
     * @param validationEnabled determines if the editing context should perform
     *		validation
     * @return new editing context with the given parent object store
     *		and the delegate corresponding to the validation flag
     */
    public static EOEditingContext newEditingContext(EOObjectStore parent, boolean validationEnabled) {
        return factory()._newEditingContext(parent, validationEnabled);
    }
    /**
        * Factory method to create a new editing context with
     * validation disabled. Sets the default no validation
     * delegate on the editing context. Becareful an
     * editing context that does not perform validation
     * means that none of the usual validation methods are
     * called on the enterprise objects before they are saved
     * to the database.
     * @param validation flag that determines if validation
     *		should or should not be enabled.
     * @return a newly created editing context with a delegate
     *		set that has disabled validation.
     */
    public static EOEditingContext newEditingContext(boolean validation) {
        return factory()._newEditingContext(validation);
    }
    /**
        * Creates a new editing context with the specified object
     * store as the parent object store. This method is useful
     * when creating nested editing contexts. After creating
     * the editing context the default delegate is set on the
     * editing context.<br/>
     * <br/>
     * Note: an {@link EOEditingContext} is a subclass of EOObjectStore
     * so passing in another editing context to this method is
     * completely kosher.
     * @param objectStore parent object store for the newly created
     *		editing context.
     * @return new editing context with the given parent object store
     */
    public static EOEditingContext newEditingContext(EOObjectStore objectStore) {
        return factory()._newEditingContext(objectStore);
    }
}

