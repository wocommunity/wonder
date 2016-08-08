//
//  ERXEC.java
//  ERExtensions
//
//  Created by Max Muller on Sun Feb 23 2003.
//
package er.extensions.eof;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSDelegate;

import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSelectorUtilities;
import er.extensions.foundation.ERXSignalHandler;
import er.extensions.foundation.ERXUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Subclass of {@link com.webobjects.eocontrol.EOEditingContext} that has every
 * public method overridden to support automatic lock/unlock handling for you.
 * This is very useful, as it is potentially very dangerous to rely on EOFs automatic
 * lock handling - it will invariably lead into deadlocks. As you will need to use
 * this class and its subclasses exclusively as your ECs, it also contains a factory
 * instances. The Factory also sets a default delegate for you and is used
 * everywhere in ERExtensions and ERDirectToWeb. The Factory is actually an
 * interface and you would create a new EC by using:
 * <code>ERXEC.newEditingContext()</code> You can also install your own
 * Factory classes. It is recommended to subclass ERXEC.DefaultFactory and
 * override <code>_createEditingContext()</code> or use the 
 * er.extensions.ERXEC.editingContextClassName property to specify the name of the 
 * editing context class to be instantiated by ERXEC.DefaultFactory's
 * _createEditingContext() core method. 
 *
 * @property er.extensions.ERXEC.useSharedEditingContext
 * @property er.extensions.ERXEC.markOpenLocks
 * @property er.extensions.ERXEC.traceOpenLocks
 * @property er.extensions.ERXEC.useUnlocker
 * @property er.extensions.ERXEC.denyMerges
 * @property er.extensions.ERXEC.defaultAutomaticLockUnlock
 * @property er.extensions.ERXEC.defaultCoalesceAutoLocks
 * @property er.extensions.ERXEC.safeLocking
 * @property er.extensions.ERXEC.editingContextClassName
 */
public class ERXEC extends EOEditingContext {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ERXEC.class);

	/**
	 * Logs a message when set to DEBUG and an EC is locked/unlocked. Logs a message with
	 * trace if {@link #lockTrace} is also set to DEBUG.
	 */
	public static final Logger lockLogger = LoggerFactory.getLogger("er.extensions.ERXEC.LockLogger");

	/** 
	 * Logs a message with trace when set to DEBUG if autoLocking is enabled and an EC is
	 * used without a lock. Adds traces to messages by {@link #lockLogger} if it is also set to DEBUG.
	 */
	public static final Logger lockTrace = LoggerFactory.getLogger("er.extensions.ERXEC.LockTrace");

	/** Name of the notification that is posted after editing context is created. */
	public static final String EditingContextDidCreateNotification = "EOEditingContextDidCreate";

	/**
	 * Name of the notification that is posted before an editing context is
	 * saved.
	 */
	public static final String EditingContextWillSaveChangesNotification = "EOEditingContextWillSaveChanges";

	/**
	 * Name of the notification that is posted when an editing context is
	 * reverted.
	 */
	public static final String EditingContextDidRevertChanges = "EOEditingContextDidRevertChanges";

	/**
	 * Name of the notification that is posted when an editing context has
	 * failed to save changes.
	 */
	public static final String EditingContextFailedToSaveChanges = "EOEditingContextFailedToSaveChanges";

	/**
	 * if traceOpenEditingContextLocks is true, this contains the stack trace
	 * from this EC's call to lock
	 */
	private Exception creationTrace;
	NSMutableDictionary<Thread, NSMutableArray<Exception>> openLockTraces = new NSMutableDictionary<Thread, NSMutableArray<Exception>>();

	/**
	 * if traceOpenEditingContextLocks is true, this will contain
	 * the locking thread
	 */
	transient Thread lockingThread;
	
	/**
	 * And, as the name might change, also the name of the locking thread (might contain session ID or other info)
	 */
	String lockingThreadName;

	/** decides whether to lock/unlock automatically when used without a lock. */
	private Boolean useAutolock;
	/**
	 * if true, then autolocks are left open inside of a request to be cleaned
	 * up at the end
	 */
	private Boolean coalesceAutoLocks;

	/** if &gt; 0, there is an autolock on this editingContext */ 
	private int autoLocked;

	/**
	 * holds a flag if the EC is in finalize(). This is needed because we can't
	 * autolock then.
	 */
	private boolean isFinalizing;

	/**
	 * holds a flag if locked ECs should be unlocked after the request-response
	 * loop.
	 */
	private static volatile Boolean useUnlocker;

	/** holds a flag if editing context locks should be traced */
	private static volatile Boolean traceOpenLocks;

	/** holds a flag if editing context locks should be marked */
	private static volatile Boolean markOpenLocks;

//	/** key for the thread storage used by the unlocker. */
//	private static final String LockedContextsForCurrentThreadKey = "ERXEC.lockedContextsForCurrentThread";

	private static final NSSelector EditingContextWillRevertObjectsDelegateSelector = new NSSelector("editingContextWillRevertObjects", new Class[] { EOEditingContext.class, NSArray.class, NSArray.class, NSArray.class });
	private static final NSSelector EditingContextDidRevertObjectsDelegateSelector = new NSSelector("editingContextDidRevertObjects", new Class[] { EOEditingContext.class, NSArray.class, NSArray.class, NSArray.class });
	private static final NSSelector EditingContextDidFailSaveChangesDelegateSelector = new NSSelector("editingContextDidFailSaveChanges", new Class[] { EOEditingContext.class, EOGeneralAdaptorException.class });
	
	private static final String ERXECProcessQueuedNotificationsNotification = "processQueuedNotifications";
	public static final NSSelector ERXECProcessQueuedNotificationsSelector = ERXSelectorUtilities.notificationSelector("processQueuedNotificationsNotification");

	/**
	 * @return the value of the <code>er.extensions.ERXEC.editingContextClassName</code> property, which
	 * is the name of the editing context class instantiated by ERXEC.DefaultFactory's _createEditingContext() 
	 * core method. It defaults to the name of the ERXEC class.
	 *  
	 */
	public static String editingContextClassName() {
		return ERXProperties.stringForKeyWithDefault("er.extensions.ERXEC.editingContextClassName", ERXEC.class.getName());
	}
	
	/**
	 * Returns the value of the <code>er.extensions.ERXEC.safeLocking</code> property, which is the
	 * new catch-all setting that turns on all of the recommended locking settings.
	 * @return the value of the <code>er.extensions.ERXEC.safeLocking</code> property
	 */
	public static boolean safeLocking() {
		return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXEC.safeLocking", false);
	}

	/**
	 * Returns the value of the <code>er.extensions.ERXEC.defaultAutomaticLockUnlock</code> property, which 
	 * turns on auto-locking by default. Also returns true if <code>safeLocking</code> is true.
	 * @return the value of the <code>er.extensions.ERXEC.defaultAutomaticLockUnlock</code> property
	 */
	public static boolean defaultAutomaticLockUnlock() {
		return ERXProperties.booleanForKey("er.extensions.ERXEC.defaultAutomaticLockUnlock") || ERXEC.safeLocking();
	}

	/**
	 * Returns the value of the <code>er.extensions.ERXEC.defaultCoalesceAutoLocks</code> property, which 
	 * turns on coalescing auto-locks, meaning that the EC gets locked once and unlocked at the end of the RR loop.
	 * Also returns true if <code>safeLocking</code> is true.
	 * @return the value of the <code>er.extensions.ERXEC.defaultCoalesceAutoLocks</code> property
	 */
	public static boolean defaultCoalesceAutoLocks() {
		return ERXProperties.booleanForKey("er.extensions.ERXEC.defaultCoalesceAutoLocks") || ERXEC.safeLocking();
	}

	/**
	 * Returns the value of the <code>er.extensions.ERXEC.useUnlocker</code> property, which 
	 * turns on unlocking at the end of the RR loop.
	 * Also returns true if <code>safeLocking</code> is true.
	 * @return the value of the <code>er.extensions.ERXEC.useUnlocker</code> property
	 */
	public static boolean useUnlocker() {
		if (useUnlocker == null) {
			synchronized (ERXEC.class) {
				if(useUnlocker == null) {
			useUnlocker = Boolean.valueOf(ERXProperties.booleanForKey("er.extensions.ERXEC.useUnlocker") || ERXEC.safeLocking());
			log.debug("setting useUnlocker to {}", useUnlocker);
		}
			}
		}
		return useUnlocker.booleanValue();
	}
	
	public static void setUseUnlocker(boolean value) {
		useUnlocker = Boolean.valueOf(value);
	}

	/**
	 * Returns the value of the <code>er.extensions.ERXEC.traceOpenLocks</code>
	 * property, which turns on tracing of locks. You can see the trace either by
	 * <code>kill -HUP</code>, by the
	 * <code>ERXDirectAction/showOpenEditingContextLockTraces</code> action or 
	 * by setting your App's statistic store to <code>ERXStatisticStore</code>.
	 * @return the value of the <code>er.extensions.ERXEC.traceOpenLocks</code> property
	 */
	public static boolean traceOpenLocks() {
		if (traceOpenLocks == null) {
			synchronized (ERXEC.class) {
				if(traceOpenLocks == null) {
			traceOpenLocks = Boolean.valueOf(ERXProperties.booleanForKeyWithDefault("er.extensions.ERXEC.traceOpenLocks", false));
			log.debug("setting traceOpenLocks to {}", traceOpenLocks);
		}
			}
		}
		return traceOpenLocks.booleanValue();
	}

	/**
	 * Returns the value of the <code>er.extensions.ERXEC.markOpenLocks</code>
	 * property, which turns on marking of locks. You can see the threads that hold the lock either by
	 * <code>kill -HUP</code>, by the
	 * <code>ERXDirectAction/showOpenEditingContextLockTraces</code> action or 
	 * by setting your App's statistic store to <code>ERXStatisticStore</code>.
	 * Also returns true if <code>traceOpenLocks</code> is true.
	 * @return the value of the <code>er.extensions.ERXEC.markOpenLocks</code> property
	 */
	public static boolean markOpenLocks() {
		if (markOpenLocks == null) {
			synchronized (ERXEC.class) {
				if(markOpenLocks == null) {
			markOpenLocks = Boolean.valueOf(ERXProperties.booleanForKeyWithDefault("er.extensions.ERXEC.markOpenLocks", false));
			log.debug("setting markOpenLocks to {}", markOpenLocks);
		}
			}
		}
		return markOpenLocks.booleanValue() || traceOpenLocks();
	}
	
	/**
	 * Sets whether or not open editing context lock tracing is enabled.
	 * @param value if open locks should be traced
	 */
	public static void setTraceOpenLocks(boolean value) {
		traceOpenLocks = Boolean.valueOf(value);
	}
	
	/**
	 * Sets whether or not open editing context lock marking is enabled.
	 * @param value if open locks should be marked
	 */
	public static void setMarkOpenLocks(boolean value) {
		markOpenLocks = Boolean.valueOf(value);
	}

	private static ThreadLocal<List> locks = new ThreadLocal() {
		@Override
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
		if (useUnlocker() && ec != null) {
			List ecs = locks.get();
			ecs.add(ec);
			log.debug("After pushing: {}", ecs);
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
		if (useUnlocker() && ec != null) {
			List ecs = locks.get();
			if (ecs != null) {
				int index = ecs.lastIndexOf(ec);
				if (index >= 0) {
					ecs.remove(index);
				}
				else {
					log.error("Should pop, but ec not found in Vector! {}, ec: {}, ecs: {}", Thread.currentThread().getName(), ec, ecs);
				}
			}
			log.debug("After popping: {}", ecs);
		}
	}

	/**
	 * Unlocks all remaining locked contexts in the current thread. You
	 * shouldn't call this yourself, but let the Unlocker handle it for you.
	 */
	public static void unlockAllContextsForCurrentThread() {
		List ecs = locks.get();
		if (useUnlocker() && ecs != null && ecs.size() > 0) {
			log.debug("Unlock remaining: {}", ecs);
			// we can't use an iterator, because calling unlock() will remove
			// the EC from end of the vector
			for (int i = ecs.size() - 1; i >= 0; i--) {
				EOEditingContext ec = (EOEditingContext) ecs.get(i);
				boolean openAutoLocks = (ec instanceof ERXEC && ((ERXEC) ec).isAutoLocked());
				if (openAutoLocks) {
					log.debug("Unlocking autolocked editing context: {}", ec);
					((ERXEC) ec).autoLocked--;
				}
				else {
					log.warn("Unlocking context that wasn't unlocked in RR-Loop: {}", ec);
				}
				try {
					ec.unlock();
				}
				catch (IllegalStateException ex) {
					log.error("Could not unlock EC: {}", ec, ex);
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

	public ERXEC() {
		this(defaultParentObjectStore());
	}

	public ERXEC(EOObjectStore os) {
		super(os);
		//super._initWithParentObjectStore(os);
		
		ERXEnterpriseObject.Observer.install();
		if (ERXEC.markOpenLocks()) {
			creationTrace = new Exception("Creation");
			creationTrace.fillInStackTrace();
			activeEditingContexts.put(this, Thread.currentThread().getName());
		}
	}
	
//	protected void _initWithParentObjectStore(EOObjectStore parent) {
//		/* NOTE: This method is called from EOEditingContext's constructor. Doing nothing here to avoid the following race condition:
//		 * - new ERXEC() is called, it starts with invoking EOEditingContext constructor
//		 * - EOEditingContext constructor registers notifications' handlers
//		 * - One of the handlers gets triggered in another thread
//		 * - As some handlers are overriden in ERXEC, the ERXEC's handler is called - which means that ERXEC's method is invoked when ERXEC is still
//		 * under construction. This leads to disaster as all instance variables are not initialized at the time of the method call.
//		 */
//	}

	/**
	 * Utility to delete a bunch of objects.
	 * @param objects objects to delete
	 */
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

	/**
	 * Decides on a per-EC-level if autoLocking should be used.
	 * @return true if autoLocking should be used
	 */
	public boolean useAutoLock() {
		if (useAutolock == null) {
			useAutolock = Boolean.valueOf(defaultAutomaticLockUnlock());
		}
		return useAutolock.booleanValue();
	}

	/**
	 * Sets whether to use autoLocking on this EC.
	 * @param value if autolocking should be used
	 */
	public void setUseAutoLock(boolean value) {
		useAutolock = Boolean.valueOf(value);
	}

	/**
	 * If you just use autolocking, you will end up churning locks constantly.
	 * Additionally, you can still end up with race conditions since you're not
	 * actually locking across your entire request. Coalescing auto locks
	 * attempts to solve this problem by leaving your auto lock open after the
	 * first use. This "hanging lock" will be cleaned up at the end of the RR
	 * loop by the unlocker.
	 * @return true if coalesceAutoLocks is enabled
	 */
	public boolean coalesceAutoLocks() {
		if (coalesceAutoLocks == null) {
			coalesceAutoLocks = Boolean.valueOf(defaultCoalesceAutoLocks());
			if (coalesceAutoLocks.booleanValue() && !useUnlocker()) {
				throw new IllegalStateException("You must enable the EC unlocker if you want to coalesce autolocks.");
			}
		}
		return coalesceAutoLocks.booleanValue() && ERXApplication.isInRequest();
	}

	/**
	 * Sets whether or not coalescing auto locks should be enabled.
	 * @param value if coalescing auto locks should be enabled
	 */
	public void setCoalesceAutoLocks(boolean value) {
		coalesceAutoLocks = Boolean.valueOf(value);
	}

	/**
	 * If traceOpenEditingContextLocks is true, returns the stack trace from
	 * when this EC was created
	 * @return the stack trace from when this EC was created
	 */
	public Exception creationTrace() {
		return creationTrace;
	}

	/**
	 * @return If traceOpenEditingContextLocks is true, returns the stack trace from
	 * when this EC was locked
	 */
	public synchronized NSDictionary<Thread, NSMutableArray<Exception>> openLockTraces() {
		return openLockTraces;
	}

	/**
	 * Overridden to emit log messages and push this instance to the locked
	 * editing contexts in this thread.
	 */
	@Override
	public void lock() {
		if (markOpenLocks()) {
			traceLock();
		}
		lockAttempts().set(lockAttempts().get().intValue()+1);
		super.lock();
		pushLockedContextForCurrentThread(this);
		if (markOpenLocks()) {
			synchronized(this) {
			lockingThread = Thread.currentThread();
			lockingThreadName = lockingThread.getName();
		}
		}
		if (!isAutoLocked() && lockLogger.isDebugEnabled()) {
			if (lockTrace.isDebugEnabled()) {
				lockLogger.debug("locked {}", this, new Exception());
			}
			else {
				lockLogger.debug("locked {}", this);
			}
		}
	}

	private static Exception defaultTrace = new Exception("DefaultTrace");
	
	/**
	 * Adds the current stack trace to openLockTraces. 
	 */
	private synchronized void traceLock() {
		if(openLockTraces == null) {
			openLockTraces = new NSMutableDictionary<Thread, NSMutableArray<Exception>>();
		}
		Exception openLockTrace = defaultTrace;
		if(traceOpenLocks()) {
			openLockTrace = new Exception("Locked");
		}
		Thread currentThread = Thread.currentThread();
		NSMutableArray<Exception> currentTraces = openLockTraces.objectForKey(currentThread);
		if(currentTraces == null) {
			currentTraces = new NSMutableArray<Exception>();
			openLockTraces.setObjectForKey(currentTraces, currentThread);
		}
		currentTraces.addObject(openLockTrace);
		// AK: disabled, because do we really need this? It's really annoying while debugging.
//		if (!currentThread.equals(lockingThread) && false) {
//			StringBuffer buf = new StringBuffer(1024);
//			buf.append(System.identityHashCode(this) + " Attempting to lock editing context from " + currentThread.getName() + " that was previously locked in " + lockingThread.getName() + "\n");
//			buf.append(" Current stack trace: " + ERXUtilities.stackTrace(openLockTrace) + "\n");
//			buf.append(" Lock count: " + openLockTraces.count() + "\n");
//			Enumeration openLockTracesEnum = openLockTraces.objectEnumerator();
//			while (openLockTracesEnum.hasMoreElements()) {
//				Exception existingOpenLockTrace = (Exception) openLockTracesEnum.nextElement();
//				buf.append(" Existing lock: " + ERXUtilities.stackTrace(existingOpenLockTrace));
//			}
//			log.info(buf);
//		}
	}

	private synchronized void traceUnlock() {
		if (openLockTraces != null) {
			NSMutableArray<Exception> traces = openLockTraces.objectForKey(lockingThread);
			if(traces != null) {
				traces.removeLastObject();
				if (traces.count() == 0) {
					openLockTraces.removeObjectForKey(lockingThread);
				}
			} else {
				log.error("Missing lock: {}", lockingThread);
			}
			if (openLockTraces.count() == 0) {
				openLockTraces = null;
			}
		}
		if(!isLockedInThread()) {
			lockingThread = null;
			lockingThreadName = null;
		}
	}

	/**
	 * Overridden to emit log messages and pull this instance from the locked
	 * editing contexts in this thread.
	 */
	@Override
	public void unlock() {
		popLockedContextForCurrentThread(this);
		if (markOpenLocks()) {
			traceUnlock();
		}
		if (!isAutoLocked() && lockLogger.isDebugEnabled()) {
			if (lockTrace.isDebugEnabled()) {
				lockLogger.debug("unlocked {}", this, new Exception());
			}
			else {
				lockLogger.debug("unlocked {}", this);
			}
		}
		super.unlock();
		lockAttempts().set(lockAttempts().get().intValue()-1);
	}

	private boolean isLockedInThread() {
		return locks.get().contains(this);
	}

	private transient ThreadLocal<Integer> lockAttempts;
	
	private ThreadLocal<Integer> lockAttempts() {
		if(lockAttempts == null) {
			lockAttempts = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return Integer.valueOf(0);
		}
	};
		}
		return lockAttempts;
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
		if (!useAutoLock() || isFinalizing || isLockedInThread() || lockAttempts().get().intValue() > 0)
			return false;

		boolean wasAutoLocked = false;

		if (!isAutoLocked() || !coalesceAutoLocks()) {
			wasAutoLocked = true;
			lock();
			autoLocked++;

			if (!isFinalizing) {
				if (lockTrace.isDebugEnabled()) {
					lockTrace.debug("called method {} without a lock, ec={}", method, this, new Exception());
				}
				else {
					//lockLogger.warn("called method {} without a lock, ec={}", method, this);
				}
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
			// MS: Coalescing autolocks leaves the last autolock open to be
			// closed by the request.
			if (!coalesceAutoLocks()) {
				autoLocked--;
				unlock();
			}
		}
	}

	/**
	 * Returns whether we did autolock this instance.
	 * 
	 * @return true if we were autolocked.
	 */
	public boolean isAutoLocked() {
		return autoLocked > 0;
	}

	protected synchronized void _checkOpenLockTraces() {
		NSMutableDictionary<Thread, NSMutableArray<Exception>> traces = openLockTraces;
		if (traces != null && traces.count() != 0) {
			String instance = getClass().getSimpleName() + "@" + System.identityHashCode(this);
			log.error("{} Disposed with {} locks (finalizing = {})", instance, traces.count(), isFinalizing);
			for (NSMutableArray<Exception> actual : traces.values()) {
				for (Exception existingOpenLockTrace : actual) {
					log.error("{} Existing lock.", instance, existingOpenLockTrace);
				}
			}
			log.error("{} Created.", instance, creationTrace);
		}
	}

	@Override
	public void dispose() {
		if (markOpenLocks()) {
			_checkOpenLockTraces();
		}
		super.dispose();
	}

	@Override
	public void finalize() throws Throwable {
		isFinalizing = true;
		try {
			if (markOpenLocks()) {
				// log.info("Finalize {}@{}", getClass(), System.identityHashCode(this));
				_checkOpenLockTraces();
			}
		} catch(Throwable ex) {
			// we *must* not fail in a finalizer
			log.error("Error finalizing.", ex);
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * Technically, the OSC is public API and as such should also get
	 * auto-locked. In practice, it's called too often to warrant it. If you
	 * want, you can turn it on on a case by case basis.
	 */
	public static boolean _shouldLockOnLockObjectStore = false;
	
	@Override
	public void lockObjectStore() {
		if(!_shouldLockOnLockObjectStore || (parentObjectStore() instanceof EOEditingContext)) {
			super.lockObjectStore();
		} else {
			boolean wasAutoLocked = autoLock("lockObjectStore");
			try {
				super.lockObjectStore();
			}
			finally {
				autoUnlock(wasAutoLocked);
			}
		}
	}
	
	@Override
	public void unlockObjectStore() {
		if(!_shouldLockOnLockObjectStore || (parentObjectStore() instanceof EOEditingContext)) {
			super.unlockObjectStore();
		} else {
			boolean wasAutoLocked = autoLock("unlockObjectStore");
			try {
				super.unlockObjectStore();
			}
			finally {
				autoUnlock(wasAutoLocked);
			}
		}
	}

	@Override
	public void reset() {
		boolean wasAutoLocked = autoLock("reset");
		try {
			super.reset();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void recordObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid) {
		boolean wasAutoLocked = autoLock("recordObject");
		try {
			super.recordObject(eoenterpriseobject, eoglobalid);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void forgetObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("forgetObject");
		try {
			super.forgetObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void processRecentChanges() {
		boolean wasAutoLocked = autoLock("processRecentChanges");
		try {
			super.processRecentChanges();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public NSArray<EOEnterpriseObject> updatedObjects() {
		boolean wasAutoLocked = autoLock("updatedObjects");
		try {
			return super.updatedObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public NSArray registeredObjects() {
		boolean wasAutoLocked = autoLock("registeredObjects");
		try {
			return super.registeredObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public NSArray<EOEnterpriseObject> insertedObjects() {
		boolean wasAutoLocked = autoLock("insertedObjects");
		try {
			return super.insertedObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public NSArray<EOEnterpriseObject> deletedObjects() {
		boolean wasAutoLocked = autoLock("deletedObjects");
		try {
			return super.deletedObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void setSharedEditingContext(EOSharedEditingContext eosharededitingcontext) {
		boolean wasAutoLocked = autoLock("setSharedEditingContext");
		try {
			super.setSharedEditingContext(eosharededitingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public EOEnterpriseObject objectForGlobalID(EOGlobalID eoglobalid) {
		boolean wasAutoLocked = autoLock("objectForGlobalID");
		try {
			return super.objectForGlobalID(eoglobalid);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public EOGlobalID globalIDForObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("globalIDForObject");
		try {
			return super.globalIDForObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public NSDictionary committedSnapshotForObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("committedSnapshotForObject");
		try {
			return super.committedSnapshotForObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public NSDictionary currentEventSnapshotForObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("currentEventSnapshotForObject");
		try {
			return super.currentEventSnapshotForObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void objectWillChange(Object obj) {
		boolean wasAutoLocked = autoLock("objectWillChange");
		try {
			super.objectWillChange(obj);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void insertObjectWithGlobalID(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid) {
		boolean wasAutoLocked = autoLock("insertObjectWithGlobalID");
		try {
			super.insertObjectWithGlobalID(eoenterpriseobject, eoglobalid);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
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
	 * Overridden to support autoLocking and to call mightDelete() on subclasses
	 * of ERXEnterpriseObject.
	 */
	@Override
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

	@Override
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
	 * Smarter version of normal <code>saveChanges()</code> method. Overridden
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
	 * <li> Call <code>saveChanges()</code> on the child, committing the child
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
	 * there may be dependent computed state that needs to be reset. But
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
	 */
	@Override
	public void saveChanges() {
		boolean wasAutoLocked = autoLock("saveChanges");
        _EOAssertSafeMultiThreadedAccess("saveChanges()");
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
			NSNotificationCenter.defaultCenter().postNotification(EditingContextFailedToSaveChanges, this);
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
		processQueuedNotifications();
	}
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
	 *            when true, saves again after resolving. when false, throws the
	 *            optimistic locking after resolving
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
				log.warn("_saveChangesTolerantly: Exception occurred", e);
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

	@Override
	public EOEnterpriseObject faultForGlobalID(EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("faultForGlobalID");
		try {
			return super.faultForGlobalID(eoglobalid, eoeditingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public NSArray arrayFaultWithSourceGlobalID(EOGlobalID eoglobalid, String s, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("arrayFaultWithSourceGlobalID");
		try {
			return super.arrayFaultWithSourceGlobalID(eoglobalid, s, eoeditingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void initializeObject(EOEnterpriseObject eoenterpriseobject, EOGlobalID eoglobalid, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("initializeObject");
		try {
			if (eoenterpriseobject instanceof ERXGenericRecord) {
				ERXGenericRecord eo = (ERXGenericRecord) eoenterpriseobject;
				// gross hack to not trigger the two-way relationships
				boolean old = eo._setUpdateInverseRelationships(false);
				try {
					super.initializeObject(eoenterpriseobject, eoglobalid, eoeditingcontext);
				}
				finally {
					eo._setUpdateInverseRelationships(old);
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

	@Override
	public void editingContextDidForgetObjectWithGlobalID(EOEditingContext eoeditingcontext, EOGlobalID eoglobalid) {
		boolean wasAutoLocked = autoLock("editingContextDidForgetObjectWithGlobalID");
		try {
			super.editingContextDidForgetObjectWithGlobalID(eoeditingcontext, eoglobalid);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public NSArray objectsForSourceGlobalID(EOGlobalID eoglobalid, String s, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("objectsForSourceGlobalID");
		try {
			return super.objectsForSourceGlobalID(eoglobalid, s, eoeditingcontext);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
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
	 * Overridden to support autoLocking and to flush the cache of all
	 * ERXEnterpriseObjects.
	 */
	@Override
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

	@Override
	public NSArray objectsWithFetchSpecification(EOFetchSpecification fs, EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("objectsWithFetchSpecification");
		try {
			NSArray objects = super.objectsWithFetchSpecification(fs, eoeditingcontext);

			if (fs instanceof ERXFetchSpecification && ((ERXFetchSpecification)fs).includeEditingContextChanges()) {
				objects = ERXEOControlUtilities.filteredObjectsWithQualifier(this, objects, fs.entityName(), fs.qualifier(), fs.sortOrderings(), fs.usesDistinct(), fs.isDeep(), true, true, true, true);
			}
			return objects;
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void saveChangesInEditingContext(EOEditingContext eoeditingcontext) {
		boolean wasAutoLocked = autoLock("saveChangesInEditingContext");
		try {
			super.saveChangesInEditingContext(eoeditingcontext);
			
			NSArray<EOEnterpriseObject> childInsertedObjects = eoeditingcontext.insertedObjects();
			for (EOEnterpriseObject childInsertedObject : childInsertedObjects) {
				EOEnterpriseObject parentInsertedObject = objectForGlobalID(eoeditingcontext.globalIDForObject(childInsertedObject));
				if (parentInsertedObject instanceof ERXGenericRecord && childInsertedObject instanceof ERXGenericRecord) {
					((ERXGenericRecord)parentInsertedObject).didCopyFromChildInEditingContext((ERXGenericRecord)childInsertedObject, eoeditingcontext);
				}
			}
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void refaultAllObjects() {
		boolean wasAutoLocked = autoLock("refaultAllObjects");
		try {
			super.refaultAllObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void invalidateObjectsWithGlobalIDs(NSArray nsarray) {
		boolean wasAutoLocked = autoLock("invalidateObjectsWithGlobalIDs");
		try {
			super.invalidateObjectsWithGlobalIDs(nsarray);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void invalidateAllObjects() {
		boolean wasAutoLocked = autoLock("invalidateAllObjects");
		try {
			super.invalidateAllObjects();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	@Override
	public void lockObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("lockObject");
		try {
			super.lockObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

	/** Overridden to support autoLocking and will/did revert delegate methods. */
	@Override
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

			NSNotificationCenter.defaultCenter().postNotification(EditingContextDidRevertChanges, this);

		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

    @Override
	public void refreshObject(EOEnterpriseObject eoenterpriseobject) {
		boolean wasAutoLocked = autoLock("refreshObject");
		try {
			super.refreshObject(eoenterpriseobject);
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

    @Override
	public void undo() {
		boolean wasAutoLocked = autoLock("undo");
		try {
			super.undo();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

    @Override
	public void redo() {
		boolean wasAutoLocked = autoLock("redo");
		try {
			super.redo();
		}
		finally {
			autoUnlock(wasAutoLocked);
		}
	}

    @Override
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
	private NSMutableArray<NSNotification> queuedNotifications = new NSMutableArray<NSNotification>();

	protected static Map<ERXEC, String> activeEditingContexts = Collections.synchronizedMap(new WeakHashMap());

	/**
	 * Overridden so add a bugfix from Lenny Marks
	 */
	@Override
	public void _objectsChangedInStore(NSNotification nsnotification) {
		ERXEnterpriseObject.FlushCachesProcessor.perform(this, (NSArray) nsnotification.userInfo().objectForKey("objects"));
		
		/*
		 * Check to see if this context, or any parent context, is saving changes. 
		 * If so, queue up the notifications.
		 */
		boolean isSavingChanges = savingChanges;
		EOObjectStore parent = parentObjectStore();
		while(!isSavingChanges && parent instanceof ERXEC) {
			ERXEC parentEc = (ERXEC) parent;
			isSavingChanges = parentEc.savingChanges;
			parent = parentEc.parentObjectStore();
		}
		
		if (isSavingChanges) {
			synchronized (queuedNotifications) {
				queuedNotifications.addObject(nsnotification);
			}
		}
		else {
			super._objectsChangedInStore(nsnotification);
		}
	}

	/**
	 * Temp EC delegate that prevents merging of changes to objects that already have changes.
	 * 
	 * @author ak
	 */
	public static class _DenyMergeDelegate {
		
		private _NSDelegate delegate;
		
        public _DenyMergeDelegate(Object original) {
			if (original != null) {
				delegate = new _NSDelegate(original.getClass(), original);
			}
		}

		public boolean editingContextShouldMergeChangesForObject(EOEditingContext ec, EOEnterpriseObject eo) {
        	if(delegate != null && delegate.respondsTo("editingContextShouldMergeChangesForObject")) {
        		return delegate.booleanPerform("editingContextShouldMergeChangesForObject", ec, eo);
        	}
        	NSDictionary committedSnapshotForObject = ec.committedSnapshotForObject(eo);
        	if(committedSnapshotForObject != null) {
        		return eo.changesFromSnapshot(committedSnapshotForObject).count() == 0;
        	}
        	//AK: this is when the object is simply registered, not sure if true or false would be better... setting?
        	return true;
        }
        
        public void editingContextDidMergeChanges(EOEditingContext ec) {
        	if(delegate != null && delegate.respondsTo("editingContextShouldMergeChangesForObject")) {
        		delegate.perform("editingContextDidMergeChanges", ec);
        	}
        }
	}

	private _NSDelegate _denyDelegate;
	
	/**
	 * Quick and experimental hack to hook into the merge-changes system. If
	 * it's not useful, it should be removed again.
	 */
	@Override
	public void _processObjectStoreChanges(NSDictionary info) {
		if (ERXProperties.booleanForKey("er.extensions.ERXEC.denyMerges")) {
			Object original = delegate();
			try {
				if (_denyDelegate == null) {
					_denyDelegate = new _NSDelegate(_DenyMergeDelegate.class, new _DenyMergeDelegate(original));
				}
				setDelegate(_denyDelegate);
				super._processObjectStoreChanges(info);
			}
			finally {
				setDelegate(original);
			}
		}
		else {
			super._processObjectStoreChanges(info);
		}
	}
	
	/**
	 * Overridden so add a bugfix from Lenny Marks
	 * 
	 */
	private void processQueuedNotifications() {
		NSMutableArray<NSNotification> queuedNotificationsClone;
		synchronized (queuedNotifications) {
			queuedNotificationsClone = new NSMutableArray<NSNotification>(queuedNotifications);
			queuedNotifications.removeAllObjects();
		}
		for (NSNotification notification : queuedNotificationsClone) {
			_objectsChangedInStore(notification);
		}
		NSNotificationCenter.defaultCenter().postNotification(ERXECProcessQueuedNotificationsNotification, this);
	}
	
	public void processQueuedNotificationsNotification(NSNotification n) {
		processQueuedNotifications();
	}

	/**
	 * Sets the delegate for this context.
	 */
	@Override
	public void setDelegate(Object d) {
		if (log.isDebugEnabled()) {
			log.debug("setting delegate to {}", d);
			log.debug(ERXUtilities.stackTrace());
		}
		super.setDelegate(d);
	}

	/** Default implementation of the Factory interface. */
	public static class DefaultFactory implements Factory {
		private static final Logger log = LoggerFactory.getLogger(DefaultFactory.class);

		/** holds a reference to the default ec delegate */
		protected Object defaultEditingContextDelegate;

		/** holds a reference to the default no validation delegate */
		protected Object defaultNoValidationDelegate;

		/**
		 * holds whether to newly created instances use the shared editing
		 * context.
		 */
		protected Boolean useSharedEditingContext = null;

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
			log.debug("setting defaultEditingContextDelegate to {}", delegate);
		}

		/**
		 * Default delegate that does not perform validation. Not performing
		 * validation can be a good thing when using nested editing contexts as
		 * sometimes you only want to validate one object, not all the
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
			log.debug("Setting default delegate on editing context: {} allows validation: {}", ec, validation);
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
			NSNotificationCenter.defaultCenter().postNotification(EditingContextDidCreateNotification, ec);
			if(objectStore instanceof ERXEC) {
				ERXEC parent = (ERXEC)objectStore;
				NSNotificationCenter.defaultCenter().addObserver(ec, ERXECProcessQueuedNotificationsSelector, ERXECProcessQueuedNotificationsNotification, parent);
			}
			return ec;
		}

		private Constructor _editingContextConstructor;
		
		/**
		 * @return The Constructor used by _createEditingContext() to create a new editing context by instantiating
		 * the class named ERXEC.editingContextClassName().
		 */
		protected Constructor editingContextConstructor() {
			if (_editingContextConstructor == null) {
				Class editingContextClass;
				try {
					editingContextClass = Class.forName(ERXEC.editingContextClassName());
				}
				catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
				Class[] parameterTypes = new Class[] { EOObjectStore.class };
				try {
					_editingContextConstructor = editingContextClass.getConstructor(parameterTypes);
				}
				catch (SecurityException e) {
					throw new RuntimeException(e);
				}
				catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}
			
			return _editingContextConstructor;
		}
		
		/**
		 * Actual EC creation bottleneck. Override this to return other
		 * subclasses or use the <code>er.extensions.ERXEC.editingContextClassName</code> property to 
		 * specify the name of your editing context class to be instantiated and returned by this method.
		 * 
		 * @param parent	The parent EOObjectStore for the editing context created and returned by this method
		 * @return 			The editing context created by this method
		 */
		protected EOEditingContext _createEditingContext(EOObjectStore parent) {
			
			EOObjectStore arg = (parent == null ? EOEditingContext.defaultParentObjectStore() : parent);
			EOEditingContext editingContext;
			
			try {
				editingContext = (EOEditingContext) editingContextConstructor().newInstance(arg);
			}
			catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}
			catch (InstantiationException e) {
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
			
			return editingContext;
		}

		public boolean useSharedEditingContext() {
			if (useSharedEditingContext == null) {
				useSharedEditingContext = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXEC.useSharedEditingContext", true) ? Boolean.TRUE : Boolean.FALSE;
				log.debug("setting useSharedEditingContext to {}", useSharedEditingContext);
			}
			return useSharedEditingContext.booleanValue();
		}

		public void setUseSharedEditingContext(boolean value) {
			useSharedEditingContext = value ? Boolean.TRUE : Boolean.FALSE;
		}
	}

	/** holds a reference to the factory used to create editing contexts */
	protected static volatile Factory factory;

	/**
	 * Gets the factory used to create editing contexts
	 * 
	 * @return editing context factory
	 */
	public static Factory _factory() {
		if (factory == null) {
			synchronized(ERXEC.class) {
				if(factory == null) {
			factory = new DefaultFactory();
		}
			}
		}
		return factory;
	}

	/**
	 * Sets the default editing context factory
	 * 
	 * @param aFactory
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
		return _factory()._newEditingContext();
	}

	/**
	 * Factory method to create a new tolerant editing context.
	 * @param parent the parent object store
	 * @param retry true if the ec should retry on failed save
	 * @param merge true if ec changes should be merged
	 * @return a new ec
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
	 * validation delegate is set if validation is disabled.
	 * <p>
	 * Note:
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
		return _factory()._newEditingContext(parent, validationEnabled);
	}

	/**
	 * Factory method to create a new editing context with validation disabled.
	 * Sets the default no validation delegate on the editing context. Be careful -
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
		return _factory()._newEditingContext(validation);
	}

	/**
	 * Creates a new editing context with the specified object store as the
	 * parent object store. This method is useful when creating nested editing
	 * contexts. After creating the editing context the default delegate is set
	 * on the editing context.
	 * <p>
	 * Note: an {@link EOEditingContext} is
	 * a subclass of EOObjectStore so passing in another editing context to this
	 * method is completely kosher.
	 * 
	 * @param objectStore
	 *            parent object store for the newly created editing context.
	 * @return new editing context with the given parent object store
	 */
	public static EOEditingContext newEditingContext(EOObjectStore objectStore) {
		return _factory()._newEditingContext(objectStore);
	}

	/**
	 * Register the OpenEditingContextLockSignalHandler signal handle on the HUP
	 * signal.
	 */
	public static void registerOpenEditingContextLockSignalHandler() {
		try {
			ERXEC.registerOpenEditingContextLockSignalHandler("HUP");
		}
		catch (IllegalArgumentException e) {
			log.warn("ERXEC's HUP signal handler was not registered, probably because your operating system does not support this signal.");
		}
	}

	/**
	 * Register the OpenEditingContextLockSignalHandler signal handle on the
	 * named signal.
	 * 
	 * @param signalName
	 *            the name of the signal to handle
	 */
	public static void registerOpenEditingContextLockSignalHandler(String signalName) {
		ERXSignalHandler.register(signalName, new ERXEC.DumpLocksSignalHandler());
		ERXSignalHandler.register(signalName, new ERXObjectStoreCoordinator.DumpLocksSignalHandler());
	}

	/**
	 * Returns a string describing outstanding locks for the created ECs 
	 * @return description of locks
	 */

	public static String outstandingLockDescription() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		boolean hadLocks = false;
		pw.print("Currently " +activeEditingContexts.size() + " active ECs : "+ activeEditingContexts + ")");
		for (ERXEC ec : ERXEC.activeEditingContexts.keySet()) {
			synchronized (ec) {
			NSMutableDictionary<Thread, NSMutableArray<Exception>> traces = ec.openLockTraces;
			if (traces != null && traces.count() > 0) {
				hadLocks = true;
				pw.println("\n------------------------");
				pw.println("Editing Context: " + ec + " Locking thread: " + ec.lockingThreadName + "->" + ec.lockingThread);
				if(ec.creationTrace != null) {
					ec.creationTrace.printStackTrace(pw);
				}
				if(!ERXEC.traceOpenLocks()) {
					pw.println("Stack tracing is disabled");
				} else {
					for (Thread thread : traces.keySet()) {
						pw.println("Outstanding at @" + thread);
						for(Exception ex: traces.objectForKey(thread)) {
							ex.printStackTrace(pw);
						}
					}
				}
			} else {
				// pw.println("\n------------------------");
				// pw.println("Editing Context: " + ec + " unlocked");
			}
		}
		}
		if(!hadLocks) {
			pw.print("No open editing contexts (of " + activeEditingContexts.size() + ")");
		}
        pw.close();
		return sw.toString();
	}
	
	/**
	 * OpenEditingContextLockSignalHandler provides a signal handler that prints
	 * out open editing context locks. By default, the handler attaches to
	 * SIGHUP.
	 * <p>
	 * Call ERXEC.registerOpenEditingContextLockSignalHandler() to attach it.
	 */
	public static class DumpLocksSignalHandler implements SignalHandler {

		public void handle(Signal signal) {
			log.info(outstandingLockDescription());
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		/*
		 * The superclass serialization methods register for notifications during
		 * readObject, but do not unregister during writeObject. Since the
		 * NSNotificationCenter hangs on to the ec until it is gc'ed, then it is
		 * possible to have an ec containing insertedObjects which were later
		 * saved in a different deserialized ec. If one of these objects is then
		 * deleted, the notification center broadcasts an invalidate objects
		 * notification to the ec which still contains the eo in insertedObjects.
		 * 
		 * When this happens, the ec throws an exception. To prevent this, the ec 
		 * is removed here as an observer for notifications when it is serialized.
		 */
		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();
		nc.removeObserver(this);
	}
}
