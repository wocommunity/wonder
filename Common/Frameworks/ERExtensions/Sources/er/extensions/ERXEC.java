//
//  ERXEC.java
//  ERExtensions
//
//  Created by Max Muller on Sun Feb 23 2003.
//
package er.extensions;
import java.util.*;
import java.lang.reflect.InvocationTargetException;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Subclass that has every public method overridden to support automatic 
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
    
    /** how many times we did automatically lock. */
    private int autoLockCount = 0;

    /** how many times has the EC been locked. */
    private int lockCount = 0;
    
    /** holds a flag if the EC is in finalize(). This is needed because we can't autolock then. */
    private boolean isFinalizing;
    
    /** holds a flag if locked ECs should be unlocked after the request-response loop. */
    private static boolean useUnlocker;
    
    /** key for the thread storage used by the unlocker. */
    private static final String LockedContextsForCurrentThreadKey = "ERXEC.lockedContextsForCurrentThread";
    
    private static final NSSelector EditingContextWillRevertObjectsDelegateSelector =
            new NSSelector("editingContextWillRevertObjects",
                           new Class[] { EOEditingContext.class, NSArray.class, NSArray.class, NSArray.class });
    private static final NSSelector EditingContextDidRevertObjectsDelegateSelector =
            new NSSelector("editingContextDidRevertObjects",
                           new Class[] { EOEditingContext.class, NSArray.class, NSArray.class, NSArray.class });
    
    public static void setUseUnlocker(boolean value) {
    	useUnlocker = value;
    }
    
	/** 
	 * Pushes the given EC to the array of locked ECs in the current thread. The ECs left over
	 * after the RR-loop will be automagically unlocked.
	 * @param ec locked EOEditingContext
	 */
    public static void pushLockedContextForCurrentThread(EOEditingContext ec) {
    	if(useUnlocker && ec != null) {
    		Vector ecs = (Vector)ERXThreadStorage.valueForKey(lockedContextsThreadKey());
    		if(ecs == null) {
    			ecs = new Vector();
        		ERXThreadStorage.takeValueForKey(ecs, lockedContextsThreadKey());
    		}
    		ecs.add(ec);
    		if(log.isDebugEnabled()) {
    		    log.debug("After pushing: " + ecs);
    		}
    	}
    }
    
    /**
     * ERXThreadStorage is inherited by newly created threads, and we don't
     * want to have the child thread's locked ECs unlocked when the parent exits dispatchRequest(),
     * so we need to append something to the thread key, to distinguish it from
     * the parent.
     * @return thread-unique thread-storage key
     */
    private static String lockedContextsThreadKey() {
        return LockedContextsForCurrentThreadKey + ":" + System.identityHashCode(Thread.currentThread());
    }

    /**
     * Pops the given EC from the array of contexts to unlock. The ECs left over
	 * after the RR-loop will be automagically unlocked.
     * @param ec unlocked EOEditingContext
     */
    public static void popLockedContextForCurrentThread(EOEditingContext ec) {
    	if(useUnlocker &&  ec != null) {
    		Vector ecs = (Vector)ERXThreadStorage.valueForKey(lockedContextsThreadKey());
    		if(ecs != null) {
    			int index = ecs.lastIndexOf(ec);
    			if(index >= 0) {
    				ecs.remove(index);
    			} else {
    				log.error("Should pop, but ec not found in Vector! " + Thread.currentThread().getName() + ", ec: " + ec + ", ecs:" + ecs);
    			}
    		}
       		if(log.isDebugEnabled()) {
    		    log.debug("After popping: " + ecs);
    		}
    	}
    }
    
    /** 
     * Unlocks all remaining locked contexts in the current thread.
     * You shouldn't call this yourself, but let the Unlocker handle it for you.
     */
    public static void unlockAllContextsForCurrentThread() {
    	Vector ecs = (Vector)ERXThreadStorage.valueForKey(lockedContextsThreadKey());
    	ERXThreadStorage.removeValueForKey(lockedContextsThreadKey());
    	if(ecs != null && ecs.size() > 0) {
       		if(log.isDebugEnabled()) {
    		    log.debug("Unlock remaining: " + ecs);
    		}
        	// we can't use an iterator, because calling unlock() will remove the EC from end of the vector
    		for (int i = ecs.size() - 1; i >= 0; i--) {
    			EOEditingContext ec = (EOEditingContext) ecs.get(i);
    			log.warn("Unlocking context that wasn't unlocked in RR-Loop!: " + ec);
    			try {
    				ec.unlock();
    			} catch(IllegalStateException ex) {
    				log.error("Could not unlock EC: " + ec, ex);
    			}
    		}
    	}
    }

    /**
     * Extensions for the EOEditingContext.Delegate interface.
     */
    public static interface Delegate extends EOEditingContext.Delegate {

        /**
         * If the delegate implements this method, this method is invoked before a revert of an editing
         * context.  We pass the objects that are marked as inserted, updated and deleted.
         *
         * @param ec the editing context that just reverted.
         * @param insertedObjects objects that were marked as inserted in the editing context before the revert
         * took place.
         * @param updatedObjects objects that were marked as updated in the editing context before the revert
         * took place.
         * @param deletedObjects objects that were marked as deleted in the editing context before the revert
         * took place.
         */
        public void editingContextWillRevertObjects(EOEditingContext ec, NSArray insertedObjects,
                                                    NSArray updatedObjects, NSArray deletedObjects);

        /**
         * If the delegate implements this method, this method is invoked following a revert of an editing
         * context.  We pass the objects that were marked as inserted, updated and deleted before the revert
         * took place.
         *
         * @param ec the editing context that just reverted.
         * @param insertedObjects objects that were marked as inserted in the editing context before the revert
         * took place.
         * @param updatedObjects objects that were marked as updated in the editing context before the revert
         * took place.
         * @param deletedObjects objects that were marked as deleted in the editing context before the revert
         * took place.
         */
        public void editingContextDidRevertObjects(EOEditingContext ec, NSArray insertedObjects,
                                                   NSArray updatedObjects, NSArray deletedObjects);

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
        if (!isAutoLocked() && lockLogger.isDebugEnabled()) {
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
    	popLockedContextForCurrentThread(this);
    	super.unlock();
        if (!isAutoLocked() && lockLogger.isDebugEnabled()) {
            if(lockTrace.isDebugEnabled()) {
                lockLogger.debug("unlocked "+this+" "+ ERXUtilities.stackTrace());
            } else {
                lockLogger.debug("unlocked "+this);
            }
        }
        lockCount--;
    }

    /**
     * Utility to actually emit the log messages and do the locking,
     * based on the result of {@link #useAutoLock()}.
     * @param method method name which to prepend to log message
     * @return whether we did lock automatically
     */
    protected boolean autoLock(String method) {
        boolean wasAutoLocked = false;
        
        if (lockCount == 0 && useAutoLock()) {
            wasAutoLocked = true;
            autoLockCount++;
            lock();
        }

        if(lockCount == 0) {
	        if (!isAutoLocked() && !isFinalizing) {
	            if(lockLoggerTrace.isDebugEnabled()) {
	                lockLoggerTrace.debug("called method " + method + " without a lock, ec="+this, new Exception());
	            } else {
	                lockLogger.warn("called method " + method + " without a lock, ec="+this);
	            }
	        }
        }
        
        return wasAutoLocked;
    }
    /**
     * Utility to unlock the EC is it was locked in the previous invocation.
     * @param wasAutoLocked true if the EC was autolocked
     */
    protected void autoUnlock(boolean wasAutoLocked) {
        if (wasAutoLocked) {
            unlock();
            autoLockCount--;
        }
    }
    
    /**
     * Returns whether we did autolock this instance.
     * @return true if we were autolocked.
     */
    public boolean isAutoLocked() {
    	return autoLockCount > 0;
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void finalize() throws Throwable {
        isFinalizing = true;
        super.finalize();
    }
    
    /** Overriden to support automatic autoLocking. */ 
    public void reset() {
        boolean wasAutoLocked = autoLock("reset");
        try {
            super.reset();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }

    /** Overriden to support autoLocking. */ 
    public void recordObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid) {
        boolean wasAutoLocked = autoLock("recordObject");
        try {
            super.recordObject(eoenterpriseobject, eoglobalid);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
 
    /** Overriden to support autoLocking. */ 
    public void forgetObject(EOEnterpriseObject eoenterpriseobject) {
        boolean wasAutoLocked = autoLock("forgetObject");
        try {
            super.forgetObject(eoenterpriseobject);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void processRecentChanges() {
        boolean wasAutoLocked = autoLock("processRecentChanges");
        try {
            super.processRecentChanges();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public NSArray updatedObjects() {
        boolean wasAutoLocked = autoLock("updatedObjects");
        try {
            return super.updatedObjects();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public NSArray registeredObjects() {
        boolean wasAutoLocked = autoLock("registeredObjects");
        try {
            return super.registeredObjects();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public NSArray insertedObjects() {
        boolean wasAutoLocked = autoLock("insertedObjects");
        try {
            return super.insertedObjects();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public NSArray deletedObjects() {
        boolean wasAutoLocked = autoLock("deletedObjects");
        try {
            return super.deletedObjects();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void setSharedEditingContext(EOSharedEditingContext eosharededitingcontext) {
        boolean wasAutoLocked = autoLock("setSharedEditingContext");
        try {
            super.setSharedEditingContext(eosharededitingcontext);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public EOEnterpriseObject objectForGlobalID(EOGlobalID eoglobalid) {
        boolean wasAutoLocked = autoLock("objectForGlobalID");
        try {
            return super.objectForGlobalID(eoglobalid);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public EOGlobalID globalIDForObject(EOEnterpriseObject eoenterpriseobject) {
        boolean wasAutoLocked = autoLock("globalIDForObject");
        try {
            return super.globalIDForObject(eoenterpriseobject);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public NSDictionary committedSnapshotForObject(EOEnterpriseObject eoenterpriseobject) {
        boolean wasAutoLocked = autoLock("committedSnapshotForObject");
        try {
            return super.committedSnapshotForObject(eoenterpriseobject);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public NSDictionary currentEventSnapshotForObject(EOEnterpriseObject eoenterpriseobject) {
        boolean wasAutoLocked = autoLock("currentEventSnapshotForObject");
        try {
            return super.currentEventSnapshotForObject(eoenterpriseobject);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void objectWillChange(Object obj) {
        boolean wasAutoLocked = autoLock("objectWillChange");
        try {
            super.objectWillChange(obj);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void insertObjectWithGlobalID(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid) {
        boolean wasAutoLocked = autoLock("insertObjectWithGlobalID");
        try {
            super.insertObjectWithGlobalID(eoenterpriseobject, eoglobalid);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void insertObject(EOEnterpriseObject eoenterpriseobject) {
        boolean wasAutoLocked = autoLock("insertObject");
        try {
            super.insertObject(eoenterpriseobject);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void deleteObject(EOEnterpriseObject eoenterpriseobject) {
        boolean wasAutoLocked = autoLock("deleteObject");
        try {
            super.deleteObject(eoenterpriseobject);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public boolean hasChanges() {
        boolean wasAutoLocked = autoLock("hasChanges");
        try {
            return super.hasChanges();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void saveChanges() {
        boolean wasAutoLocked = autoLock("saveChanges");
        try {
            super.saveChanges();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public EOEnterpriseObject faultForGlobalID(EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
        boolean wasAutoLocked = autoLock("faultForGlobalID");
        try {
            return super.faultForGlobalID(eoglobalid, eoeditingcontext);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public NSArray arrayFaultWithSourceGlobalID(EOGlobalID eoglobalid, String s, EOEditingContext eoeditingcontext) {
        boolean wasAutoLocked = autoLock("arrayFaultWithSourceGlobalID");
        try {
            return super.arrayFaultWithSourceGlobalID(eoglobalid, s, eoeditingcontext);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void initializeObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
        boolean wasAutoLocked = autoLock("initializeObject");
        try {
            super.initializeObject(eoenterpriseobject, eoglobalid, eoeditingcontext);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void editingContextDidForgetObjectWithGlobalID(EOEditingContext eoeditingcontext, EOGlobalID eoglobalid) {
        boolean wasAutoLocked = autoLock("editingContextDidForgetObjectWithGlobalID");
        try {
            super.editingContextDidForgetObjectWithGlobalID(eoeditingcontext, eoglobalid);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public NSArray objectsForSourceGlobalID(EOGlobalID eoglobalid, String s, EOEditingContext eoeditingcontext) {
        boolean wasAutoLocked = autoLock("objectsForSourceGlobalID");
        try {
            return super.objectsForSourceGlobalID(eoglobalid, s, eoeditingcontext);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void refaultObject(EOEnterpriseObject eoenterpriseobject) {
        boolean wasAutoLocked = autoLock("refaultObject");
        try {
            super.refaultObject(eoenterpriseobject);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void refaultObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
        boolean wasAutoLocked = autoLock("refaultObject");
        try {
            super.refaultObject(eoenterpriseobject, eoglobalid, eoeditingcontext);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public NSArray objectsWithFetchSpecification(EOFetchSpecification eofetchspecification, EOEditingContext eoeditingcontext) {
        boolean wasAutoLocked = autoLock("objectsWithFetchSpecification");
        try {
            return super.objectsWithFetchSpecification(eofetchspecification, eoeditingcontext);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void saveChangesInEditingContext(EOEditingContext eoeditingcontext) {
        boolean wasAutoLocked = autoLock("saveChangesInEditingContext");
        try {
            super.saveChangesInEditingContext(eoeditingcontext);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void refaultAllObjects() {
        boolean wasAutoLocked = autoLock("refaultAllObjects");
        try {
            super.refaultAllObjects();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void invalidateObjectsWithGlobalIDs(NSArray nsarray) {
        boolean wasAutoLocked = autoLock("invalidateObjectsWithGlobalIDs");
        try {
            super.invalidateObjectsWithGlobalIDs(nsarray);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void invalidateAllObjects() {
        boolean wasAutoLocked = autoLock("invalidateAllObjects");
        try {
            super.invalidateAllObjects();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void lockObject(EOEnterpriseObject eoenterpriseobject) {
        boolean wasAutoLocked = autoLock("lockObject");
        try {
            super.lockObject(eoenterpriseobject);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }

    /** Overriden to support autoLocking and will/did revert delegate methods. **/
    public void revert() {
        boolean wasAutoLocked = autoLock("revert");
        try {
            final Object delegate = delegate();
            final boolean delegateImplementsWillRevert =
                    delegate != null && EditingContextWillRevertObjectsDelegateSelector.implementedByObject(delegate);
            final boolean delegateImplementsDidRevert =
                    delegate != null && EditingContextDidRevertObjectsDelegateSelector.implementedByObject(delegate);
            final boolean needToCallDelegate = delegateImplementsWillRevert || delegateImplementsDidRevert;
            final NSArray insertedObjects = needToCallDelegate ? insertedObjects().immutableClone() : null;
            final NSArray updatedObjects = needToCallDelegate ? updatedObjects().immutableClone() : null;
            final NSArray deletedObjects = needToCallDelegate ? deletedObjects().immutableClone() : null;
            final Object[] parameters = needToCallDelegate ? new Object[] {this, insertedObjects,updatedObjects, deletedObjects} : null;

            if( delegateImplementsWillRevert )
                ERXSelectorUtilities.invoke(EditingContextWillRevertObjectsDelegateSelector, delegate, parameters);

            super.revert();

            if ( delegateImplementsDidRevert )
                ERXSelectorUtilities.invoke(EditingContextDidRevertObjectsDelegateSelector, delegate, parameters);

        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */
    /** @deprecated */
    public void saveChanges(Object obj) {
        boolean wasAutoLocked = autoLock("saveChanges");
        try {
            super.saveChanges(obj);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void refreshObject(EOEnterpriseObject eoenterpriseobject) {
        boolean wasAutoLocked = autoLock("refreshObject");
        try {
            super.refreshObject(eoenterpriseobject);
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void undo() {
        boolean wasAutoLocked = autoLock("undo");
        try {
            super.undo();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public void redo() {
        boolean wasAutoLocked = autoLock("redo");
        try {
            super.redo();
        } finally {
            autoUnlock(wasAutoLocked);
        }
    }
    
    /** Overriden to support autoLocking. */ 
    public Object invokeRemoteMethod(EOEditingContext eoeditingcontext, EOGlobalID eoglobalid, String s, Class aclass[], Object aobj[]) {
        boolean wasAutoLocked = autoLock("invokeRemoteMethod");
        try {
            return super.invokeRemoteMethod(eoeditingcontext, eoglobalid, s, aclass, aobj);
        } finally {
            autoUnlock(wasAutoLocked);
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
    			log.debug("setting useSharedEditingContext to "+ useSharedEditingContext);
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

