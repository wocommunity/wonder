//
//  ERXEC.java
//  ERExtensions
//
//  Created by Max Muller on Sun Feb 23 2003.
//
package er.extensions;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

/**
 * Subclass that has every public method overridden to support automatic
 * lock/unlock handling for you. This is very useful, as is is potentially very
 * dangerous to rely on EOFs automatic lock handling - it will invariably lead
 * into deadlocks. As you will need to use this class and its subclasses
 * exclusively as your ECs, it also contains a factory class to create editing
 * contexts. The Factory also sets a default delegate for you and is used
 * everywhere in ERExtensions and ERDirectToWeb. The Factory is actually and
 * interface and you would create a new EC by using:
 * <code>ERXEC.newEditingContext()</code> You can also install your own
 * Factory classes. It is recommended to subclass ERXEC.DefaultFactory and
 * override <code>_createEditingContext()</code>
 */
public class ERXEC extends EOEditingContext {
	/** general logging */
	public static final Logger log = Logger.getLogger(ERXEC.class);

	/**
	 * logs a message when set to DEBUG, autoLocking is enabled and an EC is
	 * used without a lock.
	 */
	public static final Logger lockLogger = Logger.getLogger("er.extensions.ERXEC.LockLogger");

	/**
	 * logs a message with a stack trace when set to DEBUG and an EC is
	 * locked/unlocked.
	 */
	public static final Logger lockLoggerTrace = Logger.getLogger("er.extensions.ERXEC.LockLoggerTrace");

	/** logs a message when set to DEBUG and an EC is locked/unlocked. */
	public static final Logger lockTrace = Logger.getLogger("er.extensions.ERXEC.LockTrace");

	/** name of the notification that is posted after editing context is created. */
	public static final String EditingContextDidCreateNotification = "EOEditingContextDidCreate";

	/**
	 * name of the notiification that is posted before an editing context is
	 * saved.
	 */
	public static final String EditingContextWillSaveChangesNotification = "EOEditingContextWillSaveChanges";

	/**
	 * if traceOpenEditingContextLocks is true, this contains the stack trace
	 * from this EC's call to lock
	 */
	private Exception creationTrace;
	private NSMutableArray openLockTraces;
	/**
	 * if traceOpenEditingContextLocks is true, this will contain the name of
	 * the locking thread
	 */
	private String nameOfLockingThread;

	/** decides whether to lock/unlock automatically when used without a lock. */
	private Boolean useAutolock;
	/**
	 * if true, then autolocks are left open inside of a request to be cleaned
	 * up at the end
	 */
	private Boolean coalesceAutoLocks;

	/** how many times we did automatically lock. */ 
	private boolean autoLocked;

	/** how many times has the EC been locked. */
	private int lockCount;

	/**
	 * holds a flag if the EC is in finalize(). This is needed because we can't
	 * autolock then.
	 */
	private boolean isFinalizing;

	/**
	 * holds a flag if locked ECs should be unlocked after the request-response
	 * loop.
	 */
	private static boolean useUnlocker;

	/** holds a flag if editing context locks should be traced */
	private static boolean traceOpenEditingContextLocks;

	/** key for the thread storage used by the unlocker. */
	private static final String LockedContextsForCurrentThreadKey = "ERXEC.lockedContextsForCurrentThread";

	private static final NSSelector EditingContextWillRevertObjectsDelegateSelector = new NSSelector("editingContextWillRevertObjects", new Class[] { EOEditingContext.class, NSArray.class, NSArray.class, NSArray.class });
	private static final NSSelector EditingContextDidRevertObjectsDelegateSelector = new NSSelector("editingContextDidRevertObjects", new Class[] { EOEditingContext.class, NSArray.class, NSArray.class, NSArray.class });
	private static final NSSelector EditingContextDidFailSaveChangesDelegateSelector = new NSSelector("editingContextDidFailSaveChanges", new Class[] { EOEditingContext.class, EOGeneralAdaptorException.class });

	public static void setUseUnlocker(boolean value) {
		useUnlocker = value;
	}

	/**
	 * Sets whether or not open editing context lock tracing is enabled.
	 */
	public static void setTraceOpenEditingContextLocks(boolean value) {
		traceOpenEditingContextLocks = value;
	}

	private static ThreadLocal locks = new ThreadLocal() {
		protected Object initialValue() {
			return new Vector();
		}
	};

	/**
	 * Pushes the given EC to the array of locked ECs in the current thread. The
	 * ECs left over after the RR-loop will be automagically unlocked.
	 * 
	 * @param ec
	 *            locked EOEditingContext
	 */
	public static void pushLockedContextForCurrentThread(EOEditingContext ec) {
		if (useUnlocker && ec != null) {
			List ecs = (List) locks.get();
			ecs.add(ec);
			if (log.isDebugEnabled()) {
				log.debug("After pushing: " + ecs);
			}
		}
	}

	/**
	 * Pops the given EC from the array of contexts to unlock. The ECs left over
	 * after the RR-loop will be automagically unlocked.
	 * 
	 * @param ec
	 *            unlocked EOEditingContext
	 */
	public static void popLockedContextForCurrentThread(EOEditingContext ec) {
		if (useUnlocker && ec != null) {
			List ecs = (List) locks.get();
			if (ecs != null) {
				int index = ecs.lastIndexOf(ec);
				if (index >= 0) {
					ecs.remove(index);
				}
				else {
					log.error("Should pop, but ec not found in Vector! " + Thread.currentThread().getName() + ", ec: " + ec + ", ecs:" + ecs);
				}
			}
			if (log.isDebugEnabled()) {
				log.debug("After popping: " + ecs);
			}
		}
	}

	/**
	 * Unlocks all remaining locked contexts in the current thread. You
	 * shouldn't call this yourself, but let the Unlocker handle it for you.
	 */
	public static void unlockAllContextsForCurrentThread() {
		List ecs = (List) locks.get();
		if (useUnlocker && ecs != null && ecs.size() > 0) {
			if (log.isDebugEnabled()) {
				log.debug("Unlock remaining: " + ecs);
			}
			// we can't use an iterator, because calling unlock() will remove
			// the EC from end of the vector
			for (int i = ecs.size() - 1; i >= 0; i--) {
				EOEditingContext ec = (EOEditingContext) ecs.get(i);
				boolean openAutoLocks = (ec instanceof ERXEC && ((ERXEC) ec).isAutoLocked());
				if (openAutoLocks) {
					log.debug("Unlocking autolocked editing context: " + ec);
				}
				else {
					log.warn("Unlocking context that wasn't unlocked in RR-Loop!: " + ec);
				}
				try {
					ec.unlock();
				}
				catch (IllegalStateException ex) {
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
		 * If the delegate implements this method, this method is invoked if a
		 * <code>EOGeneralAdaptorException</code> is thrown.
		 * 
		 * @param ec
		 *            the editing context that tried to save changes.
		 * @param exception
		 *            the exception thrown during the operation
		 */
		public void editingContextDidFailSaveChanges(EOEditingContext ec, EOGeneralAdaptorException exception);

		/**
		 * If the delegate implements this method, this method is invoked before
		 * a revert of an editing context. We pass the objects that are marked
		 * as inserted, updated and deleted.
		 * 
		 * @param ec
		 *            the editing context that just reverted.
		 * @param insertedObjects
		 *            objects that were marked as inserted in the editing
		 *            context before the revert took place.
		 * @param updatedObjects
		 *            objects that were marked as updated in the editing context
		 *            before the revert took place.
		 * @param deletedObjects
		 *            objects that were marked as deleted in the editing context
		 *            before the revert took place.
		 */
		public void editingContextWillRevertObjects(EOEditingContext ec, NSArray insertedObjects, NSArray updatedObjects, NSArray deletedObjects);

		/**
		 * If the delegate implements this method, this method is invoked
		 * following a revert of an editing context. We pass the objects that
		 * were marked as inserted, updated and deleted before the revert took
		 * place.
		 * 
		 * @param ec
		 *            the editing context that just reverted.
		 * @param insertedObjects
		 *            objects that were marked as inserted in the editing
		 *            context before the revert took place.
		 * @param updatedObjects
		 *            objects that were marked as updated in the editing context
		 *            before the revert took place.
		 * @param deletedObjects
		 *            objects that were marked as deleted in the editing context
		 *            before the revert took place.
		 */
		public void editingContextDidRevertObjects(EOEditingContext ec, NSArray insertedObjects, NSArray updatedObjects, NSArray deletedObjects);

	}

	public static interface Factory {
		public Object defaultEditingContextDelegate();

		public void setDefaultEditingContextDelegate(Object delegate);

		public Object defaultNoValidationDelegate();

		public void setDefaultNoValidationDelegate(Object delegate);

		public void setDefaultDelegateOnEditingContext(EOEditingContext ec);

		public void setDefaultDelegateOnEditingContext(EOEditingContext ec, boolean validation);

		public boolean useSharedEditingContext();

		public void setUseSharedEditingContext(boolean value);

		public EOEditingContext _newEditingContext();

		public EOEditingContext _newEditingContext(boolean validationEnabled);

		public EOEditingContext _newEditingContext(EOObjectStore objectStore);

		public EOEditingContext _newEditingContext(EOObjectStore objectStore, boolean validationEnabled);
	}

	/** default constructor */
	public ERXEC() {
		this(defaultParentObjectStore());
	}

	/** alternative constructor */
	public ERXEC(EOObjectStore os) {
		super(os);
		ERXEnterpriseObject.Observer.install();
		if (traceOpenEditingContextLocks) {
			creationTrace = new Exception("Creation");
			creationTrace.fillInStackTrace();
		}
	}

	public static boolean defaultAutomaticLockUnlock() {
		return "true".equals(System.getProperty("er.extensions.ERXEC.defaultAutomaticLockUnlock"));
	}

	public static boolean defaultCoalesceAutoLocks() {
		return "true".equals(System.getProperty("er.extensions.ERXEC.defaultCoalesceAutoLocks"));
	}

	/** Utility to delete a bunch of objects. */
	public void deleteObjects(NSArray objects) {
		for (int i = objects.count(); i-- > 0;) {
			Object o = objects.objectAtIndex(i);
			if (o instanceof EOEnterpriseObject) {
				EOEnterpriseObject eo = (EOEnterpriseObject) o;
				if (eo.editingContext() != null) {
					eo.editingContext().deleteObject(eo);
				}
			}
		}
	}

	/** Decides on a per-EC-level if autoLocking should be used. */
	public boolean useAutoLock() {
		if (useAutolock == null) {
			useAutolock = Boolean.valueOf(defaultAutomaticLockUnlock());
		}
		return useAutolock.booleanValue();
	}

	/** Sets whether to use autoLocking on this EC. */
	public void setUseAutoLock(boolean value) {
		useAutolock = Boolean.valueOf(value);
	}

	/**
	 * If you just use autolocking, you will end up churning locks constantly.
	 * Additionally, you can still end up with race conditions since you're not
	 * actually locking across your entire request. Coalescing auto locks
	 * attempts to solve this problem by leaving your auto lock open after the
	 * first use. This "hanging lock" will be cleaned up at the end of the RR
	 * loop but the unlocker.
	 */
	public boolean coalesceAutoLocks() {
		if (coalesceAutoLocks == null) {
			coalesceAutoLocks = Boolean.valueOf(defaultCoalesceAutoLocks());
			if (coalesceAutoLocks.booleanValue() && !useUnlocker) {
				throw new IllegalStateException("You must enable the EC unlocker if you want to coalesce autolocks.");
			}
		}
		return coalesceAutoLocks.booleanValue() && ERXApplication.isInRequest();
	}

	/**
	 * Returns whether or not coalescing auto locks is enabled.
	 */
	public void setCoalesceAutoLocks(boolean value) {
		coalesceAutoLocks = Boolean.valueOf(value);
	}

	/** Returns the number of outstanding locks. */
	public int lockCount() {
		return lockCount;
	}

	/**
	 * If traceOpenEditingContextLocks is true, returns the stack trace from
	 * when this EC was created
	 */
	public Exception creationTrace() {
		return creationTrace;
	}

	/**
	 * If traceOpenEditingContextLocks is true, returns the stack trace from
	 * when this EC was locked
	 */
	public NSArray openLockTraces() {
		return openLockTraces;
	}

	/**
	 * Overridden to emmit log messages and push this instance to the locked
	 * editing contexts in this thread.
	 */
	public void lock() {
		if (traceOpenEditingContextLocks) {
			synchronized (this) {
				if (openLockTraces == null) {
					openLockTraces = new NSMutableArray();
				}
				Exception openLockTrace = new Exception("Locked");
				openLockTrace.fillInStackTrace();
				String nameOfCurrentThread = Thread.currentThread().getName();
				if (openLockTraces.count() == 0) {
					openLockTraces.addObject(openLockTrace);
					nameOfLockingThread = nameOfCurrentThread;
					// NSLog.err.appendln("+++ Lock number (" +
					// _stackTraces.count() + ") in " + nameOfCurrentThread);
				}
				else {
					if (nameOfCurrentThread.equals(nameOfLockingThread)) {
						openLockTraces.addObject(openLockTrace);
						// NSLog.err.appendln("+++ Lock number (" +
						// _stackTraces.count() + ") in " +
						// nameOfCurrentThread);
					}
					else {
						StringBuffer buf = new StringBuffer(1024);
						buf.append(System.identityHashCode(this) + " Attempting to lock editing context from " + nameOfCurrentThread + " that was previously locked in " + nameOfLockingThread + "\n");
						buf.append(" Current stack trace: " + ERXUtilities.stackTrace(openLockTrace) + "\n");
						buf.append(" Lock count: " + openLockTraces.count() + "\n");
						Enumeration openLockTracesEnum = openLockTraces.objectEnumerator();
						while (openLockTracesEnum.hasMoreElements()) {
							Exception existingOpenLockTrace = (Exception) openLockTracesEnum.nextElement();
							buf.append(" Existing lock: " + ERXUtilities.stackTrace(existingOpenLockTrace));
						}
						buf.append(" Created: " + ERXUtilities.stackTrace(creationTrace));
						log.warn(buf);
					}
				}
			}
		}
		lockCount++;
		super.lock();
		if (!isAutoLocked() && lockLogger.isDebugEnabled()) {
			if (lockTrace.isDebugEnabled()) {
				lockLogger.debug("locked " + this, new Exception());
			}
			else {
				lockLogger.debug("locked " + this);
			}
		}
		pushLockedContextForCurrentThread(this);
	}

	/**
	 * Overridden to emmit log messages and pull this instance from the locked
	 * editing contexts in this thread.
	 */
	public void unlock() {
		popLockedContextForCurrentThread(this);
		super.unlock();
		if (!isAutoLocked() && lockLogger.isDebugEnabled()) {
			if (lockTrace.isDebugEnabled()) {
				lockLogger.debug("unlocked " + this, new Exception());
			}
			else {
				lockLogger.debug("unlocked " + this);
			}
		}
		lockCount--;
		// If coalesceAutoLocks is true, then we will often end up with
		// a hanging autoLock at the final unlock, so we want to reset the
		// autolock count to zero so things behave properly when you
		// next use autolocking
		if (lockCount == 0 && autoLocked) {
			autoLocked = false;
		}
		if (traceOpenEditingContextLocks) {
			synchronized (this) {
				if (openLockTraces != null) {
					// FIXME AK: as long as we only remove the last object,
					// this whole procedure is pretty bogus. What we'd need to
					// do is
					// at least find the best-matching stack trace suffix and
					// remove
					// that entry
					if (openLockTraces.count() > 0) {
						openLockTraces.removeLastObject();
					}
					if (openLockTraces.count() == 0) {
						nameOfLockingThread = null;
						openLockTraces = null;
					}
				}
			}
		}
	}

	/**
	 * Utility to actually emit the log messages and do the locking, based on
	 * the result of {@link #useAutoLock()}.
	 * 
	 * @param method
	 *            method name which to prepend to log message
	 * @return whether we did lock automatically
	 */
	protected boolean autoLock(String method) {
		if (!useAutoLock())
			return false;

		boolean wasAutoLocked = false;

		if (lockCount == 0) {
			wasAutoLocked = true;
			autoLocked = true;
			lock();
		}

		if (lockCount == 0 && !isAutoLocked() && !isFinalizing) {
			if (lockTrace.isDebugEnabled()) {
				lockTrace.debug("called method " + method + " without a lock, ec=" + this, new Exception());
			}
			else {
				lockLogger.warn("called method " + method + " without a lock, ec=" + this);
			}
		}

		return wasAutoLocked;
	}

	/**
	 * Utility to unlock the EC is it was locked in the previous invocation.
	 * 
	 * @param wasAutoLocked
	 *            true if the EC was autolocked
	 */
	protected void autoUnlock(boolean wasAutoLocked) {
		if (wasAutoLocked) {
			// MS: Coalescing autolocks leaves the last autolock open to be closed
			// by the request.
			if (autoLocked && !coalesceAutoLocks()) {
				unlock();
				autoLocked = false;
			}
		}
	}

	/**
	 * Returns whether we did autolock this instance.
	 * 
	 * @return true if we were autolocked.
	 */
	public boolean isAutoLocked() {
		return autoLocked;
	}

	protected void _checkOpenLockTraces() {
		if (openLockTraces != null && openLockTraces.count() != 0) {
			log.error(System.identityHashCode(this) + " Disposed with " + openLockTraces.count() + " locks (finalizing = " + isFinalizing + ")");
			Enumeration openLockTracesEnum = openLockTraces.objectEnumerator();
			while (openLockTracesEnum.hasMoreElements()) {
				Exception existingOpenLockTrace = (Exception) openLockTracesEnum.nextElement();
				log.error(System.identityHashCode(this) + " Existing lock: ", existingOpenLockTrace);
			}
			log.error(System.identityHashCode(this) + " created: ", creationTrace);
		}
	}

	public void dispose() {
		if (traceOpenEditingContextLocks) {
			_checkOpenLockTraces();
		}
		super.dispose();
	}

	/** Overriden to support automatic autoLocking. */
	public void finalize() throws Throwable {
		isFinalizing = true;
		if (traceOpenEditingContextLocks) {
			_checkOpenLockTraces();
		}
		super.finalize();
	}

	/** Overriden to support automatic autoLocking. */
	public void reset() {
		boolean wasAutoLocked = autoLock("reset");
		try {
			super.reset();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void recordObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid) {
		boolean wasAutoLocked = autoLock("recordObject");
		try {
			super.recordObject(eoenterpriseobject, eoglobalid);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void forgetObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("forgetObject");
		try {
			super.forgetObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void processRecentChanges() {
		boolean wasAutoLocked = autoLock("processRecentChanges");
		try {
			super.processRecentChanges();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public NSArray updatedObjects() {
		boolean wasAutoLocked = autoLock("updatedObjects");
		try {
			return super.updatedObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public NSArray registeredObjects() {
		boolean wasAutoLocked = autoLock("registeredObjects");
		try {
			return super.registeredObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public NSArray insertedObjects() {
		boolean wasAutoLocked = autoLock("insertedObjects");
		try {
			return super.insertedObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public NSArray deletedObjects() {
		boolean wasAutoLocked = autoLock("deletedObjects");
		try {
			return super.deletedObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void setSharedEditingContext(EOSharedEditingContext eosharededitingcontext) {
		boolean wasAutoLocked = autoLock("setSharedEditingContext");
		try {
			super.setSharedEditingContext(eosharededitingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public EOEnterpriseObject objectForGlobalID(EOGlobalID eoglobalid) {
		boolean wasAutoLocked = autoLock("objectForGlobalID");
		try {
			return super.objectForGlobalID(eoglobalid);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public EOGlobalID globalIDForObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("globalIDForObject");
		try {
			return super.globalIDForObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public NSDictionary committedSnapshotForObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("committedSnapshotForObject");
		try {
			return super.committedSnapshotForObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public NSDictionary currentEventSnapshotForObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("currentEventSnapshotForObject");
		try {
			return super.currentEventSnapshotForObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void objectWillChange(Object obj) {
		boolean wasAutoLocked = autoLock("objectWillChange");
		try {
			super.objectWillChange(obj);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void insertObjectWithGlobalID(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid) {
		boolean wasAutoLocked = autoLock("insertObjectWithGlobalID");
		try {
			super.insertObjectWithGlobalID(eoenterpriseobject, eoglobalid);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void insertObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("insertObject");
		try {
			super.insertObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/**
	 * Overriden to support autoLocking and to call mightDelete() on subclasses
	 * of ERXEnterpriseObject.
	 */
	public void deleteObject(EOEnterpriseObject eo) {
		boolean wasAutoLocked = autoLock("deleteObject");
		try {
			if (eo instanceof ERXEnterpriseObject) {
				ERXEnterpriseObject erxeo = (ERXEnterpriseObject) eo;
				erxeo.mightDelete();
			}
			super.deleteObject(eo);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public boolean hasChanges() {
		boolean wasAutoLocked = autoLock("hasChanges");
		try {
			return super.hasChanges();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	protected void willSaveChanges(NSArray insertedObjects, NSArray updatedObjects, NSArray deletedObjects) {
	}

	protected void didSaveChanges(NSArray insertedObjects, NSArray updatedObjects, NSArray deletedObjects) {
	}

	/**
	 * Smarter version of normal <code>saveChanges()</code> method. Overriden
	 * to support autoLocking and a bugfix from Lenny Marks, calls up Will/Did
	 * methods on ERXEnterpriseObjects and corrects issues with
	 * <code>flushCaches()</code> needing to be called on objects in the
	 * parent context when committing the child context to the parent. If the
	 * editing context is a child of the object-store coordinator---that is,
	 * it's not a nested context---this method behaves exactly the same as
	 * <code>EOEditingContext.saveChanges()</code>. Otherwise, this method
	 * looks over the changed objects in <code>ec</code> (<code>updatedObjects()</code>,
	 * <code>insertedObjects()</code> and <code>deletedObjects()</code>).
	 * The changed objects lists are filtered for instances of
	 * <code>ERXGenericRecord</code>. The order of operations then becomes:
	 * 
	 * <ol>
	 * <li> Call <code>processRecentChanges()</code> on the child context to
	 * propagate changes.
	 * <li> Lock the parent editing context.
	 * <li> On the deleted objects list in the child editing context, call
	 * <code>flushCaches()</code> on each corresponding EO in the parent
	 * context.
	 * <li> Unlock the parent editing context.
	 * <li> Call <code>saveChanges()</code> on the child, commiting the child
	 * changes to the parent editing context.
	 * <li> Lock the parent editing context.
	 * <li> On the objects that were updated or inserted in the child, call
	 * <code>flushCaches()</code> on each corresponding EO in the parent
	 * context.
	 * <li> Unlock the parent editing context.
	 * </ol>
	 * 
	 * <p>
	 * 
	 * The order of operations is a bit peculiar: flush deletes, save, flush
	 * inserts and updates. This is done because deletes must be flushed because
	 * there may be dependant computed state that needs to be reset. But
	 * following the delete being committed, the relationships to other objects
	 * cannot be relied upon so it isn't reliable to call flushCaches after the
	 * commit. It's not entirely correct to flush the deletes like this, but
	 * it's the best we can do.
	 * 
	 * <p>
	 * 
	 * This works around an issue in EOF that we don't get a merge notification
	 * when a child EC saves to its parent. Because there's no merge
	 * notification, <code>flushCaches()</code> isn't called by the EC
	 * delegate and we're essentially screwed vis-a-vis resetting computed
	 * state.
	 * 
	 * <p>
	 * 
	 * This method assumes that the <code>ec</code> is locked before this
	 * method is invoked, but this method will take the lock on the parent
	 * editing context if the <code>ec</code> is a nested context before and
	 * after the save in order to get the objects and to flush caches on them.
	 * 
	 * @param ec
	 *            editing context to save
	 */

	public void saveChanges() {
		_EOAssertSafeMultiThreadedAccess("saveChanges()");
		boolean wasAutoLocked = autoLock("saveChanges");
		savingChanges = true;
		try {
			NSArray insertedObjects = insertedObjects().immutableClone();
			NSArray updatedObjects = updatedObjects().immutableClone();
			NSArray deletedObjects = deletedObjects().immutableClone();

			willSaveChanges(insertedObjects, updatedObjects, deletedObjects);

			NSNotificationCenter.defaultCenter().postNotification(EditingContextWillSaveChangesNotification, this);

			_saveChanges();

			didSaveChanges(insertedObjects, updatedObjects, deletedObjects);
		}
		catch (com.webobjects.eoaccess.EOGeneralAdaptorException e) {
			Object delegate = delegate();
			boolean delegateImplementsDidSaveFailed = delegate != null && EditingContextDidFailSaveChangesDelegateSelector.implementedByObject(delegate);
			if (delegateImplementsDidSaveFailed) {
				final Object[] parameters = new Object[] { this, e };
				RuntimeException ex = (RuntimeException) ERXSelectorUtilities.invoke(EditingContextDidFailSaveChangesDelegateSelector, delegate, parameters);
				if (ex != null) {
					throw ex;
				}
			}
			else {
				throw e;
			}
		}
		finally {
			autoUnlock(wasAutoLocked);
			savingChanges = false;
		}

		processQueuedNotifications();
	}

	/**
	 * Saves changes and tries to recover from optimistic locking exceptions by
	 * refaulting the object in question, optionally merging the changed values
	 * and optionally retrying the save.
	 * 
	 * @param doesRetry
	 *            when true, saves again after resolving. when false, throws the
	 *            optimistic locking after resolving
	 * @param mergesChanges
	 */
	public void saveChangesTolerantly(boolean doesRetry, boolean mergesChanges) {
		_EOAssertSafeMultiThreadedAccess("saveChangesTolerantly()");

		boolean recover = _recoversFromException;
		boolean retry = _doesRetry;
		boolean merge = _mergesChanges;

		setOptions(true, doesRetry, mergesChanges);
		saveChanges();
		setOptions(recover, retry, merge);
	}

	public void saveChangesTolerantly() {
		saveChangesTolerantly(true);
	}

	public void saveChangesTolerantly(boolean doesRetry) {
		saveChangesTolerantly(doesRetry, true);
	}

	private boolean _recoversFromException;
	private boolean _doesRetry;
	private boolean _mergesChanges;

	/**
	 * Set the options for the saveChanges() operation.
	 * 
	 * @param recoversFromException
	 * @param doesRetry
	 * @param mergesChanges
	 */
	public void setOptions(boolean recoversFromException, boolean doesRetry, boolean mergesChanges) {
		_EOAssertSafeMultiThreadedAccess("setOptions()");
		_recoversFromException = recoversFromException;
		_doesRetry = doesRetry;
		_mergesChanges = mergesChanges;
	}

	protected void _saveChanges() {
		boolean saved = true;
		try {
			super.saveChanges();
		}
		catch (EOGeneralAdaptorException e) {
			saved = false;
			if (_recoversFromException) {
				log.warn("_saveChangesTolerantly: Exception occurred: " + e, e);
				if (ERXEOAccessUtilities.isOptimisticLockingFailure(e)) {
					EOEnterpriseObject eo = ERXEOAccessUtilities.refetchFailedObject(this, e);
					if (_mergesChanges) {
						ERXEOAccessUtilities.reapplyChanges(eo, e);
					}
					if (_doesRetry) {
						_saveChanges();
						saved = true;
					}
				}
			}
			else {
				// log.warn("_saveChangesTolerantly: Exception occurred: "+ e +
				// NSPropertyListSerialization.stringFromPropertyList(e.userInfo()),
				// e);
			}
			if (!saved) {
				throw e;
			}
		}
	}

	/** Overriden to support autoLocking. */
	public EOEnterpriseObject faultForGlobalID(EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("faultForGlobalID");
		try {
			return super.faultForGlobalID(eoglobalid, eoeditingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public NSArray arrayFaultWithSourceGlobalID(EOGlobalID eoglobalid, String s, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("arrayFaultWithSourceGlobalID");
		try {
			return super.arrayFaultWithSourceGlobalID(eoglobalid, s, eoeditingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void initializeObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("initializeObject");
		try {
			if (eoenterpriseobject instanceof ERXGenericRecord) {
				ERXGenericRecord eo = (ERXGenericRecord) eoenterpriseobject;
				// gross hack to not trigger the two-way relationships
				boolean old = eo.updateInverseRelationships(false);
				try {
					super.initializeObject(eoenterpriseobject, eoglobalid, eoeditingcontext);
				}
				finally {
					eo.updateInverseRelationships(old);
				}
			}
			else {
				super.initializeObject(eoenterpriseobject, eoglobalid, eoeditingcontext);
			}
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void editingContextDidForgetObjectWithGlobalID(EOEditingContext eoeditingcontext, EOGlobalID eoglobalid) {
		boolean wasAutoLocked = autoLock("editingContextDidForgetObjectWithGlobalID");
		try {
			super.editingContextDidForgetObjectWithGlobalID(eoeditingcontext, eoglobalid);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public NSArray objectsForSourceGlobalID(EOGlobalID eoglobalid, String s, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("objectsForSourceGlobalID");
		try {
			return super.objectsForSourceGlobalID(eoglobalid, s, eoeditingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void refaultObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("refaultObject");
		try {
			super.refaultObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/**
	 * Overriden to support autoLocking and to flush the cache of all
	 * ERXEnterpriseObjects.
	 */
	public void refaultObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("refaultObject");
		try {
			// ak: need to flush caches
			ERXEnterpriseObject.FlushCachesProcessor.perform(this, eoenterpriseobject);

			super.refaultObject(eoenterpriseobject, eoglobalid, eoeditingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public NSArray objectsWithFetchSpecification(EOFetchSpecification eofetchspecification, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("objectsWithFetchSpecification");
		try {
			return super.objectsWithFetchSpecification(eofetchspecification, eoeditingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void saveChangesInEditingContext(EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("saveChangesInEditingContext");
		try {
			super.saveChangesInEditingContext(eoeditingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void refaultAllObjects() {
		boolean wasAutoLocked = autoLock("refaultAllObjects");
		try {
			super.refaultAllObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void invalidateObjectsWithGlobalIDs(NSArray nsarray) {
		boolean wasAutoLocked = autoLock("invalidateObjectsWithGlobalIDs");
		try {
			super.invalidateObjectsWithGlobalIDs(nsarray);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void invalidateAllObjects() {
		boolean wasAutoLocked = autoLock("invalidateAllObjects");
		try {
			super.invalidateAllObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void lockObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("lockObject");
		try {
			super.lockObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking and will/did revert delegate methods. * */
	public void revert() {
		boolean wasAutoLocked = autoLock("revert");
		try {
			final NSArray insertedObjects = insertedObjects().immutableClone();
			final NSArray updatedObjects = updatedObjects().immutableClone();
			final NSArray deletedObjects = deletedObjects().immutableClone();

			ERXEnterpriseObject.WillRevertProcessor.perform(this, insertedObjects);
			ERXEnterpriseObject.WillRevertProcessor.perform(this, updatedObjects);
			ERXEnterpriseObject.WillRevertProcessor.perform(this, deletedObjects);

			final Object delegate = delegate();
			final boolean delegateImplementsWillRevert = delegate != null && EditingContextWillRevertObjectsDelegateSelector.implementedByObject(delegate);
			final boolean delegateImplementsDidRevert = delegate != null && EditingContextDidRevertObjectsDelegateSelector.implementedByObject(delegate);
			final boolean needToCallDelegate = delegateImplementsWillRevert || delegateImplementsDidRevert;
			final Object[] parameters = needToCallDelegate ? new Object[] { this, insertedObjects, updatedObjects, deletedObjects } : null;

			if (delegateImplementsWillRevert)
				ERXSelectorUtilities.invoke(EditingContextWillRevertObjectsDelegateSelector, delegate, parameters);

			super.revert();

			if (delegateImplementsDidRevert)
				ERXSelectorUtilities.invoke(EditingContextDidRevertObjectsDelegateSelector, delegate, parameters);

			ERXEnterpriseObject.DidRevertProcessor.perform(this, insertedObjects);
			ERXEnterpriseObject.DidRevertProcessor.perform(this, updatedObjects);
			ERXEnterpriseObject.DidRevertProcessor.perform(this, deletedObjects);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	/** @deprecated */
	public void saveChanges(Object obj) {
		boolean wasAutoLocked = autoLock("saveChanges");
		try {
			saveChanges();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void refreshObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("refreshObject");
		try {
			super.refreshObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void undo() {
		boolean wasAutoLocked = autoLock("undo");
		try {
			super.undo();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public void redo() {
		boolean wasAutoLocked = autoLock("redo");
		try {
			super.redo();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overriden to support autoLocking. */
	public Object invokeRemoteMethod(EOEditingContext eoeditingcontext, EOGlobalID eoglobalid, String s, Class aclass[], Object aobj[]) {
		boolean wasAutoLocked = autoLock("invokeRemoteMethod");
		try {
			return super.invokeRemoteMethod(eoeditingcontext, eoglobalid, s, aclass, aobj);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	private boolean savingChanges;
	private NSMutableArray queuedNotifications = new NSMutableArray();

	/**
	 * Overridden so add a bugfix from Lenny Marks
	 * 
	 */
	public void _objectsChangedInStore(NSNotification nsnotification) {
		ERXEnterpriseObject.FlushCachesProcessor.perform(this, (NSArray) nsnotification.userInfo().objectForKey("objects"));
		if (savingChanges) {
			queuedNotifications.addObject(nsnotification);
		}
		else {
			super._objectsChangedInStore(nsnotification);
		}
	}

	/**
	 * Overridden so add a bugfix from Lenny Marks
	 * 
	 */
	private void processQueuedNotifications() {
		synchronized (queuedNotifications) {
			for (Enumeration e = queuedNotifications.objectEnumerator(); e.hasMoreElements();) {

				NSNotification n = (NSNotification) e.nextElement();

				_objectsChangedInStore(n);

			}
			queuedNotifications.removeAllObjects();
		}
	}

	/**
	 * Sets the delegate for this context.
	 */
	public void setDelegate(Object d) {
		if (log.isDebugEnabled()) {
			log.debug("setting delegate to " + d);
			log.debug(ERXUtilities.stackTrace());
		}
		super.setDelegate(d);
	}

	/** Default implementation of the Factory interface. */
	public static class DefaultFactory implements Factory {

		/** logging support */
		public static final Logger log = Logger.getLogger(DefaultFactory.class);

		/** holds a reference to the default ec delegate */
		protected Object defaultEditingContextDelegate;

		/** holds a reference to the default no validation delegate */
		protected Object defaultNoValidationDelegate;

		/**
		 * holds whether to newly created instances use the shared editing
		 * context.
		 */
		protected Boolean useSharedEditingContext = null;

		protected Map activeEditingContexts = Collections.synchronizedMap(new WeakHashMap());

		public DefaultFactory() {
			// Initing defaultEditingContext delegates
			defaultEditingContextDelegate = new ERXDefaultEditingContextDelegate();
			defaultNoValidationDelegate = new ERXECNoValidationDelegate();
		}

		/**
		 * Returns the default editing context delegate. This delegate is used
		 * by default for all editing contexts that are created.
		 * 
		 * @return the default editing context delegate
		 */
		public Object defaultEditingContextDelegate() {
			return defaultEditingContextDelegate;
		}

		/**
		 * Sets the default editing context delegate to be used for editing
		 * context creation.
		 * 
		 * @param delegate
		 *            to be set on every created editing context by default.
		 */
		public void setDefaultEditingContextDelegate(Object delegate) {
			defaultEditingContextDelegate = delegate;
			if (log.isDebugEnabled()) {
				log.debug("setting defaultEditingContextDelegate to " + delegate);
			}
		}

		/**
		 * Default delegate that does not perform validation. Not performing
		 * validation can be a good thing when using nested editing contexts as
		 * sometimes you only want to validation one object, not all the
		 * objects.
		 * 
		 * @return default delegate that doesn't perform validation
		 */
		public Object defaultNoValidationDelegate() {
			return defaultNoValidationDelegate;
		}

		/**
		 * Sets the default editing context delegate to be used for editing
		 * context creation that does not allow validation.
		 * 
		 * @param delegate
		 *            to be set on every created editing context that doesn't
		 *            allow validation.
		 */
		public void setDefaultNoValidationDelegate(Object delegate) {
			defaultNoValidationDelegate = delegate;
		}

		/**
		 * Sets either the default editing context delegate that does or does
		 * not allow validation based on the validation flag passed in on the
		 * given editing context.
		 * 
		 * @param ec
		 *            editing context to have it's delegate set.
		 * @param validation
		 *            flag that determines if the editing context should perform
		 *            validation on objects being saved.
		 */
		public void setDefaultDelegateOnEditingContext(EOEditingContext ec, boolean validation) {
			if (log.isDebugEnabled()) {
				log.debug("Setting default delegate on editing context: " + ec + " allows validation: " + validation);
			}
			if (ec != null) {
				if (validation) {
					ec.setDelegate(defaultEditingContextDelegate());
				}
				else {
					ec.setDelegate(defaultNoValidationDelegate());
				}
			}
			else {
				log.warn("Attempting to set a default delegate on a null ec!");
			}
		}

		/**
		 * Sets the default editing context delegate on the given editing
		 * context.
		 * 
		 * @param ec
		 *            editing context to have it's delegate set.
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
			}
			else {
				ec.undoManager().setLevelsOfUndo(levelsOfUndo < 0 ? 10 : levelsOfUndo);
			}
			setDefaultDelegateOnEditingContext(ec, validationEnabled);
			if (!useSharedEditingContext()) {
				ec.lock();
				ec.setSharedEditingContext(null);
				ec.unlock();
			}
			if (traceOpenEditingContextLocks) {
				activeEditingContexts.put(ec, Thread.currentThread().getName());
			}
			NSNotificationCenter.defaultCenter().postNotification(EditingContextDidCreateNotification, ec);
			return ec;
		}

		public NSArray lockedEditingContexts() {
			NSMutableArray ecs = new NSMutableArray();
			for (Iterator e = activeEditingContexts.keySet().iterator(); e.hasNext();) {
				ERXEC ec = (ERXEC) e.next();
				if (ec.lockCount() > 0) {
					ecs.addObject(ec);
				}
			}
			return ecs;
		}

		/**
		 * Actual EC creation bottleneck. Override this to return other
		 * subclasses.
		 */
		protected EOEditingContext _createEditingContext(EOObjectStore parent) {
			return new ERXEC(parent == null ? EOEditingContext.defaultParentObjectStore() : parent);
		}

		public boolean useSharedEditingContext() {
			if (useSharedEditingContext == null) {
				useSharedEditingContext = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXEC.useSharedEditingContext", true) ? Boolean.TRUE : Boolean.FALSE;
				log.debug("setting useSharedEditingContext to " + useSharedEditingContext);
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
	 * 
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
	 * 
	 * @param factory
	 *            factory used to create editing contexts
	 */
	public static void setFactory(Factory aFactory) {
		factory = aFactory;
	}

	/**
	 * Factory method to create a new editing context. Sets the current default
	 * delegate on the newly created editing context.
	 * 
	 * @return a newly created editing context with the default delegate set.
	 */
	public static EOEditingContext newEditingContext() {
		return factory()._newEditingContext();
	}

	/**
	 * Factory method to create a new tolerant editing context.
	 */
	public static EOEditingContext newTolerantEditingContext(EOObjectStore parent, boolean retry, boolean merge) {
		ERXEC ec = (ERXEC) newEditingContext(parent);
		ec.lock();
		try {
			ec.setOptions(true, retry, merge);
		}
		finally {
			ec.unlock();
		}
		return ec;
	}

	public static EOEditingContext newTolerantEditingContext() {
		return newTolerantEditingContext(null, true, true);
	}

	public static EOEditingContext newTolerantEditingContext(EOObjectStore osc) {
		return newTolerantEditingContext(osc, true, true);
	}

	public static void saveChangesTolerantly(EOEditingContext ec, boolean doesRetry, boolean mergesChanges) {
		if (ec instanceof ERXEC) {
			ERXEC erxec = (ERXEC) ec;
			erxec.saveChangesTolerantly(doesRetry, mergesChanges);
		}
		else {
			ERXTolerantSaver.save(ec, doesRetry, mergesChanges);
		}
	}

	public static void saveChangesTolerantly(EOEditingContext ec) {
		saveChangesTolerantly(ec, true, true);
	}

	/**
	 * Creates a new editing context with the specified object store as the
	 * parent object store and with validation turned on or off depending on the
	 * flag passed in. This method is useful when creating nested editing
	 * contexts. After creating the editing context the default delegate is set
	 * on the editing context if validation is enabled or the default no
	 * validation delegate is set if validation is disabled.<br/> <br/> Note:
	 * an {@link com.webobjects.eocontrol.EOEditingContext EOEditingContext} is
	 * a subclass of EOObjectStore so passing in another editing context to this
	 * method is completely kosher.
	 * 
	 * @param parent
	 *            object store for the newly created editing context.
	 * @param validationEnabled
	 *            determines if the editing context should perform validation
	 * @return new editing context with the given parent object store and the
	 *         delegate corresponding to the validation flag
	 */
	public static EOEditingContext newEditingContext(EOObjectStore parent, boolean validationEnabled) {
		return factory()._newEditingContext(parent, validationEnabled);
	}

	/**
	 * Factory method to create a new editing context with validation disabled.
	 * Sets the default no validation delegate on the editing context. Becareful
	 * an editing context that does not perform validation means that none of
	 * the usual validation methods are called on the enterprise objects before
	 * they are saved to the database.
	 * 
	 * @param validation
	 *            flag that determines if validation should or should not be
	 *            enabled.
	 * @return a newly created editing context with a delegate set that has
	 *         disabled validation.
	 */
	public static EOEditingContext newEditingContext(boolean validation) {
		return factory()._newEditingContext(validation);
	}

	/**
	 * Creates a new editing context with the specified object store as the
	 * parent object store. This method is useful when creating nested editing
	 * contexts. After creating the editing context the default delegate is set
	 * on the editing context.<br/> <br/> Note: an {@link EOEditingContext} is
	 * a subclass of EOObjectStore so passing in another editing context to this
	 * method is completely kosher.
	 * 
	 * @param objectStore
	 *            parent object store for the newly created editing context.
	 * @return new editing context with the given parent object store
	 */
	public static EOEditingContext newEditingContext(EOObjectStore objectStore) {
		return factory()._newEditingContext(objectStore);
	}

	/**
	 * Register the OpenEditingContextLockSignalHandler signal handle on the HUP
	 * signal.
	 */
	public static void registerOpenEditingContextLockSignalHandler() {
		ERXEC.registerOpenEditingContextLockSignalHandler("HUP");
	}

	/**
	 * Register the OpenEditingContextLockSignalHandler signal handle on the
	 * named signal.
	 * 
	 * @param signalName
	 *            the name of the signal to handle
	 */
	public static void registerOpenEditingContextLockSignalHandler(String signalName) {
		Signal.handle(new Signal(signalName), new ERXEC.OpenEditingContextLockSignalHandler());
	}

	/**
	 * OpenEditingContextLockSignalHandler provides a signal handler that prints
	 * out open editing context locks. By default, the handler attaches to
	 * SIGHUP.
	 * <p>
	 * Call ERXEC.registerOpenEditingContextLockSignalHandler() to attach it.
	 */
	public static class OpenEditingContextLockSignalHandler implements SignalHandler {
		public void handle(Signal signal) {
			ERXEC.Factory ecFactory = ERXEC.factory();
			if (ecFactory instanceof ERXEC.DefaultFactory) {
				NSArray lockedEditingContexts = ((ERXEC.DefaultFactory) ecFactory).lockedEditingContexts();
				if (lockedEditingContexts.count() != 0) {
					log.info(lockedEditingContexts.count() + " open EC locks:");
				}
				else {
					log.info("No open editing contexts.");
				}
				Enumeration lockedEditingContextEnum = lockedEditingContexts.objectEnumerator();
				while (lockedEditingContextEnum.hasMoreElements()) {
					EOEditingContext lockedEditingContext = (EOEditingContext) lockedEditingContextEnum.nextElement();
					log.info("   Editing Context " + lockedEditingContext);
					Exception openCreationTrace = ((ERXEC) lockedEditingContext).creationTrace();
					if (openCreationTrace != null) {
						log.info("  Created:");
						log.info("", openCreationTrace);
					}
					NSArray openLockTracesArray = ((ERXEC) lockedEditingContext).openLockTraces();
					if (openLockTracesArray != null) {
						log.info("  Locks:");
						Enumeration openLockTracesEnum = openLockTracesArray.objectEnumerator();
						while (openLockTracesEnum.hasMoreElements()) {
							Exception ecOpenLockTrace = (Exception) openLockTracesEnum.nextElement();
							log.info("", ecOpenLockTrace);
							log.info("");
						}
					}
				}
			}
			else {
				log.info("OpenEditingContextLockSignalHandler is only available for ERXEC.DefaultFactory.");
			}
		}
	}
}
