//
//  ERXEC.java
//  ERExtensions
//
//  Created by Max Muller on Sun Feb 23 2003.
//
package er.extensions;
import java.util.Vector;

import com.webobjects.foundation.*;
import com.webobjects.appserver.WOApplication;
import com.webobjects.eocontrol.*;

/**
 * Subclass that has every publich method overridden to support automatic 
 * lock/unlock handling for you. This is very useful, as is is potentially very dangerous to rely on EOFs 
 * automatic lock handling - it will invariably lead into deadlocks. As you will need to
 * use this class and its subclasses exclusively as your ECs, it also contains a factory class to create
 * editing contexts. The Factory also sets a default delegate for you and is used everywhere in 
 * ERExtensions and ERDirectToWeb.
 * The Factory is actually and interface and you would create a new EC by using:
 *  <code>ERXEC.newEditingContext()</code>
 * You can also install your own Factory classes. It is recommended to subclass ERXEC.DefaultFactory and
 * override <code>_createEditingContext()</code>
 */
public class ERXEC extends EOEditingContext {
    /** general logging */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXEC.class);
    
    /** logs a message when set to DEBUG, autoLocking is enabled and an EC is used without a lock. */
    public static final ERXLogger lockLogger = ERXLogger.getERXLogger("er.extensions.ERXEC.LockLogger");
    
    /** logs a message with a stack trace when set to DEBUG and an EC is locked/unlocked. */
    public static final ERXLogger lockLoggerTrace = ERXLogger.getERXLogger("er.extensions.ERXEC.LockLoggerTrace");
    
    /** logs a message when set to DEBUG and an EC is locked/unlocked. */
    public static final ERXLogger lockTrace = ERXLogger.getERXLogger("er.extensions.ERXEC.LockTrace");
    
    /** name of the notification that is posted after editing context is created. */
    public static final String EditingContextDidCreateNotification = "EOEditingContextDidCreate";

    /** decides whether to lock/unlock automatically when used without a lock. */
    private Boolean useAutolock;
    
    /** true, if we did automatically lock. */
    private boolean autoLocked = false;

    /** how many times has the EC been locked. */
    private int lockCount = 0;
    
    /** holds a flag if the EC is in finalize(). This is needed because we can't autolock then. */
    private boolean isFinalizing;
    
    /** holds a flag if locked ECs should be unlocked after the request-response loop. */
    private static boolean useUnlocker;
    
    /** key for the thread storage used by the unlocker. */
    private static final String LockedContextsForCurrentThreadKey = "ERXEC.lockedContextsForCurrentThread";
    
    /** 
     * Sets up the automatic unlocking of left-over EC locks after the request-response loop.
     * @param doInstall true if you want to install the unlocking
	 */
    
	public static void installContextUnlocker(boolean doInstall) {
		if(doInstall) {
			NSSelector sel = new NSSelector("applicationDidDispatchRequest", new Class[] { NSNotification.class } );
			NSNotificationCenter.defaultCenter().addObserver( new Unlocker(), sel, WOApplication.ApplicationDidDispatchRequestNotification, null);
		}
		useUnlocker = doInstall;
	}
	
	/** 
	 * Pushes the given EC to the array of locked ECs in the current thread. The ECs left over
	 * after the RR-loop will be automagically unlocked.
	 * @param ec locked EOEditingContext
	 */
    public static void pushLockedContextForCurrentThread(EOEditingContext ec) {
    	if(useUnlocker && ec != null) {
    		Vector ecs = (Vector)ERXThreadStorage.valueForKey(LockedContextsForCurrentThreadKey);
    		if(ecs == null) {
    			ecs = new Vector();
    		}
    		ecs.add(ec);
    		ERXThreadStorage.takeValueForKey(ecs, LockedContextsForCurrentThreadKey);
    	}
    }
    
    /**
     * Pops the given EC from the array of contexts to unlock. The ECs left over
	 * after the RR-loop will be automagically unlocked.
     * @param ec unlocked EOEditingContext
     */
    public static void popLockedContextForCurrentThread(EOEditingContext ec) {
    	if(useUnlocker &&  ec != null) {
    		Vector ecs = (Vector)ERXThreadStorage.valueForKey(LockedContextsForCurrentThreadKey);
    		if(ecs != null) {
    			int index = ecs.lastIndexOf(ec);
    			if(index >= 0) {
    				ecs.remove(index);
    			}
    		}
    	}
    }
    
    /** 
     * Unlocks all remaining locked contexts in the current thread.
     * You shouldn't call this yourself, but let the Unlocker handle it for you.
     */
    public static void unlockAllContextsForCurrentThread() {
    	Vector ecs = (Vector)ERXThreadStorage.valueForKey(LockedContextsForCurrentThreadKey);
    	if(ecs != null) {
    		ERXThreadStorage.removeValueForKey(LockedContextsForCurrentThreadKey);
    		// we can't use an iterator, because calling unlock() will remove the EC from end of the vector
    		for (int i = ecs.size() - 1; i >= 0; i--) {
    			EOEditingContext ec = (EOEditingContext) ecs.get(i);
    			log.error("Unlocking context that wasn't unlocked in RR-Loop!: " + ec);
    			ec.unlock();
    		}
    	}
    }

    /** 
     * Handles the unlocking after the application has finished handling the request.
     */
    public static class Unlocker {
    	public void applicationDidDispatchRequest(NSNotification n) {
    		unlockAllContextsForCurrentThread();
    	}
    }
    
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
    
    /** Utility to delete a bunch of objects. */
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

    /** Decides on a per-EC-level if autoLocking should be used. */
    public boolean useAutoLock() {
        if (useAutolock == null) {
            useAutolock = defaultAutomaticLockUnlock() ? Boolean.TRUE : Boolean.FALSE;
        }
        return useAutolock.booleanValue();
    }
    
    /** Sets whether to use autoLocking on this EC. */
    public void setUseAutoLock(boolean value) {
        useAutolock = value ? Boolean.TRUE : Boolean.FALSE;
    }
    
    /** Returns the number of outstanding locks. */
    public int lockCount() { 
    	return lockCount; 
    }
    
    /** 
     * Overridden to emmit log messages and push this instance to the 
     * locked editing contexts in this thread.
     */
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
        pushLockedContextForCurrentThread(this);
    }
 
    /** 
     * Overridden to emmit log messages and pull this instance from the 
     * locked editing contexts in this thread.
     */
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
        popLockedContextForCurrentThread(this);
    }

    /**
     * Utility to actually emit the log messages and do the locking,
     * based on the result of {@link #useAutoLock()}.
     * @param method method name which to prepend to log message
     * @return whether we did lock automatically
     */
    protected boolean autoLock(String method) {
        boolean unlock = false;
        if (lockCount == 0 && useAutoLock()) {
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

    /**
     * Returns whether we did autolock this instance.
     * @return true if we were autolocked.
     */
    public boolean isAutoLocked() {
    	return autoLocked;
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void finalize() throws Throwable {
        isFinalizing = true;
        super.finalize();
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void reset() {
        boolean unlock = autoLock("reset");
        try {
            super.reset();
        } finally {
            if (unlock) unlock();
        }
    }

    /** Overriden to support automatic autoLocking. */ 
    public void recordObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid) {
        boolean unlock = autoLock("recordObject");
        try {
            super.recordObject(eoenterpriseobject, eoglobalid);
        } finally {
            if (unlock) unlock();
        }
    }
 
    /** Overriden to support automatic autoLocking. */ 
    public void forgetObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("forgetObject");
        try {
            super.forgetObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void processRecentChanges() {
        boolean unlock = autoLock("processRecentChanges");
        try {
            super.processRecentChanges();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public NSArray updatedObjects() {
        boolean unlock = autoLock("updatedObjects");
        try {
            return super.updatedObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public NSArray registeredObjects() {
        boolean unlock = autoLock("registeredObjects");
        try {
            return super.registeredObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public NSArray insertedObjects() {
        boolean unlock = autoLock("insertedObjects");
        try {
            return super.insertedObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public NSArray deletedObjects() {
        boolean unlock = autoLock("deletedObjects");
        try {
            return super.deletedObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void setSharedEditingContext(EOSharedEditingContext eosharededitingcontext) {
        boolean unlock = autoLock("setSharedEditingContext");
        try {
            super.setSharedEditingContext(eosharededitingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public EOEnterpriseObject objectForGlobalID(EOGlobalID eoglobalid) {
        boolean unlock = autoLock("objectForGlobalID");
        try {
            return super.objectForGlobalID(eoglobalid);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public EOGlobalID globalIDForObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("globalIDForObject");
        try {
            return super.globalIDForObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public NSDictionary committedSnapshotForObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("committedSnapshotForObject");
        try {
            return super.committedSnapshotForObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public NSDictionary currentEventSnapshotForObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("currentEventSnapshotForObject");
        try {
            return super.currentEventSnapshotForObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void objectWillChange(Object obj) {
        boolean unlock = autoLock("objectWillChange");
        try {
            super.objectWillChange(obj);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void insertObjectWithGlobalID(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid) {
        boolean unlock = autoLock("insertObjectWithGlobalID");
        try {
            super.insertObjectWithGlobalID(eoenterpriseobject, eoglobalid);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void insertObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("insertObject");
        try {
            super.insertObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void deleteObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("deleteObject");
        try {
            super.deleteObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public boolean hasChanges() {
        boolean unlock = autoLock("hasChanges");
        try {
            return super.hasChanges();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void saveChanges() {
        boolean unlock = autoLock("saveChanges");
        try {
            super.saveChanges();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public EOEnterpriseObject faultForGlobalID(EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("faultForGlobalID");
        try {
            return super.faultForGlobalID(eoglobalid, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public NSArray arrayFaultWithSourceGlobalID(EOGlobalID eoglobalid, String s, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("arrayFaultWithSourceGlobalID");
        try {
            return super.arrayFaultWithSourceGlobalID(eoglobalid, s, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void initializeObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("initializeObject");
        try {
            super.initializeObject(eoenterpriseobject, eoglobalid, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void editingContextDidForgetObjectWithGlobalID(EOEditingContext eoeditingcontext, EOGlobalID eoglobalid) {
        boolean unlock = autoLock("editingContextDidForgetObjectWithGlobalID");
        try {
            super.editingContextDidForgetObjectWithGlobalID(eoeditingcontext, eoglobalid);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public NSArray objectsForSourceGlobalID(EOGlobalID eoglobalid, String s, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("objectsForSourceGlobalID");
        try {
            return super.objectsForSourceGlobalID(eoglobalid, s, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void refaultObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("refaultObject");
        try {
            super.refaultObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void refaultObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("refaultObject");
        try {
            super.refaultObject(eoenterpriseobject, eoglobalid, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public NSArray objectsWithFetchSpecification(EOFetchSpecification eofetchspecification, EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("objectsWithFetchSpecification");
        try {
            return super.objectsWithFetchSpecification(eofetchspecification, eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void saveChangesInEditingContext(EOEditingContext eoeditingcontext) {
        boolean unlock = autoLock("saveChangesInEditingContext");
        try {
            super.saveChangesInEditingContext(eoeditingcontext);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void refaultAllObjects() {
        boolean unlock = autoLock("refaultAllObjects");
        try {
            super.refaultAllObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void invalidateObjectsWithGlobalIDs(NSArray nsarray) {
        boolean unlock = autoLock("invalidateObjectsWithGlobalIDs");
        try {
            super.invalidateObjectsWithGlobalIDs(nsarray);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void invalidateAllObjects() {
        boolean unlock = autoLock("invalidateAllObjects");
        try {
            super.invalidateAllObjects();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void lockObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("lockObject");
        try {
            super.lockObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void revert() {
        boolean unlock = autoLock("revert");
        try {
            super.revert();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void saveChanges(Object obj) {
        boolean unlock = autoLock("saveChanges");
        try {
            super.saveChanges(obj);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void refreshObject(EOEnterpriseObject eoenterpriseobject) {
        boolean unlock = autoLock("refreshObject");
        try {
            super.refreshObject(eoenterpriseobject);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void undo() {
        boolean unlock = autoLock("undo");
        try {
            super.undo();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void redo() {
        boolean unlock = autoLock("redo");
        try {
            super.redo();
        } finally {
            if (unlock) unlock();
        }
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public Object invokeRemoteMethod(EOEditingContext eoeditingcontext, EOGlobalID eoglobalid, String s, Class aclass[], Object aobj[]) {
        boolean unlock = autoLock("invokeRemoteMethod");
        try {
            return super.invokeRemoteMethod(eoeditingcontext, eoglobalid, s, aclass, aobj);
        } finally {
            if (unlock) unlock();
        }
    }
    
    /**
     * Sets the delegate for this context.
     */
    public void setDelegate(Object d) {
        if (log.isDebugEnabled()) {
            log.debug("setting delegate to "+d);
            log.debug(ERXUtilities.stackTrace());
        }
        super.setDelegate(d);
    }
    
    /** Default implementation of the Factory interface. */
    public static class DefaultFactory implements Factory {
    	
    	/** logging support */
    	public static final ERXLogger log = ERXLogger.getERXLogger(DefaultFactory.class);
    	
    	/** holds a reference to the default ec delegate */
    	protected Object defaultEditingContextDelegate;
    	
    	/** holds a reference to the default no validation delegate */
    	protected Object defaultNoValidationDelegate;
    	
    	/** holds whether to newly created instances use the shared editing context.*/
    	protected Boolean useSharedEditingContext = null;
    	
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
    	public Object defaultEditingContextDelegate() { 
    		return defaultEditingContextDelegate; 
    	}
    	
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
    	public Object defaultNoValidationDelegate() { 
    		return defaultNoValidationDelegate; 
    	}
    	
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
    	
    	public boolean useSharedEditingContext() {
    		if (useSharedEditingContext == null) {
    			useSharedEditingContext = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXEC.useSharedEditingContext", true) 
						? Boolean.TRUE : Boolean.FALSE;
    			log.info("setting useSharedEditingContext to "+ useSharedEditingContext);
    		}
    		return useSharedEditingContext.booleanValue();
    	}
    	
    	public void setUseSharedEditingContext(boolean value) {
    		useSharedEditingContext = value ? Boolean.TRUE : Boolean.FALSE;
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

