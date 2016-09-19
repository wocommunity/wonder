package er.extensions.eof;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.webobjects.eocontrol.EOCooperatingObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;


/**
 * Adds functionality to automatically close all related JDBC Connections. Also has open-lock debugging.
 * @author david
 * @author ak
 */
public class ERXObjectStoreCoordinator extends EOObjectStoreCoordinator {

	public static final Logger log = Logger.getLogger(ERXObjectStoreCoordinator.class);

	NSMutableDictionary<Thread, NSMutableArray<Exception>> openLockTraces;

	Thread lockingThread;
	
	String lockingThreadName;

	protected static Map<ERXObjectStoreCoordinator, String> activeDatabaseContexts = Collections.synchronizedMap(new WeakHashMap());

	private long lockCount = 0;

	public boolean _didClose = false;
	
	public boolean _shouldClose = false;

	/**
	 * @see com.webobjects.eocontrol.EOObjectStoreCoordinator
	 * 
	 */
	public ERXObjectStoreCoordinator() {
		if (ERXEC.markOpenLocks()) {
			activeDatabaseContexts.put(this, Thread.currentThread().getName());
		}
	}

	public ERXObjectStoreCoordinator(boolean shouldClose) {
		_shouldClose = shouldClose;
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
		if(ERXEC.traceOpenLocks()) {
			openLockTrace = new Exception("Locked");
		}

		Thread currentThread = Thread.currentThread();
		NSMutableArray<Exception> currentTraces = openLockTraces.objectForKey(currentThread);
		if(currentTraces == null) {
			currentTraces = new NSMutableArray<>();
			openLockTraces.setObjectForKey(currentTraces, currentThread);
		}
		currentTraces.addObject(openLockTrace);
	}

	/**
	 * Removes the current trace from the openLockTraces.
	 */
	private synchronized void traceUnlock() {
		if (openLockTraces != null) {
			NSMutableArray<Exception> traces = openLockTraces.objectForKey(lockingThread);
			if(traces != null) {
				//log.error("unlock: " + lockingThread);
				traces.removeLastObject();

				if (traces.count() == 0) {
					openLockTraces.removeObjectForKey(lockingThread);
				}
			} else {
				log.error("Missing lock: " + lockingThread);
			}
			if (openLockTraces.count() == 0) {
				openLockTraces = null;
			}
		}
		if(lockCount == 0) {
			lockingThread = null;
			lockingThreadName = null;
		}
	}
	
	/**
	 * Overridden to emit log messages and push this instance to the locked
	 * editing contexts in this thread.
	 */
	@Override
	public void lock() {
		boolean tracing = ERXEC.markOpenLocks();
		if (tracing) {
			traceLock();
		}
		super.lock();
		lockCount++;
		lockingThread = Thread.currentThread();
		lockingThreadName = lockingThread.getName();
		//log.error("locked: " + lockingThread);
	}

	/**
	 * Overridden to emit log messages and pull this instance from the locked
	 * editing contexts in this thread.
	 */
	@Override
	public void unlock() {
		boolean tracing = ERXEC.markOpenLocks();
		if (lockingThread != null && lockingThread != Thread.currentThread()) {
			log.fatal("Unlocking thread is not locking thread: LOCKING " + lockingThread + " vs UNLOCKING " + Thread.currentThread(), new RuntimeException("UnlockingTrace"));
			if (tracing) {
				NSMutableArray<Exception> traces = openLockTraces.objectForKey(lockingThread);
				if (traces != null) {
					for (Exception trace : traces) {
						log.fatal("Currenty locking threads: " + lockingThread, trace);
					}
				}
				else {
					log.fatal("Trace for locking thread is MISSING");
				}
			}
		}
		lockCount--;
		if (tracing) {
			traceUnlock();
		}
		super.unlock();
	}


	@Override
	public void addCooperatingObjectStore(EOCooperatingObjectStore objectStore) {
		if (cooperatingObjectStores().indexOfIdenticalObject(objectStore) < 0) {
			if (objectStore.coordinator() != null) {
				throw new IllegalStateException("Cannot add " + objectStore + " to this EOObjectStoreCoordinator because it already has another.");
			}
			super.addCooperatingObjectStore(objectStore);
		}
	}

	@Override
	public void dispose() {
		if (_shouldClose) {
			_didClose = ERXEOAccessUtilities.closeDatabaseConnections(this);
			if (!_didClose && _shouldClose) {
				log.error("shouldClose was true but could not close all Connections!");
			}
		}
		super.dispose();
	}

	public static String outstandingLockDescription() {
		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
			boolean hadLocks = false;
			pw.print(activeDatabaseContexts.size() + " active ObjectStoreCoordinators : "+ activeDatabaseContexts + ")");
			for (ERXObjectStoreCoordinator ec : activeDatabaseContexts.keySet()) {
				NSMutableDictionary<Thread, NSMutableArray<Exception>> traces = ec.openLockTraces;
				if (traces != null && traces.count() > 0) {
					hadLocks = true;
					pw.println("\n------------------------");
					pw.println("ObjectStoreCoordinator: " + ec + " Locking thread: " + ec.lockingThreadName + "->" + ec.lockingThread);
					for (Thread thread : traces.keySet()) {
						pw.println("Outstanding at @" + thread);
						for(Exception ex: traces.objectForKey(thread)) {
							if(ex == defaultTrace) {
								pw.println("Stack tracing is disabled");
							} else {
								ex.printStackTrace(pw);
							}
						}
					}
				}
			}
			if(!hadLocks) {
				pw.print("No open ObjectStoreCoordinator (of " + activeDatabaseContexts.size() + ")");
			}
			return sw.toString();
		}
		catch (IOException e) {
			// ignore
		}
		return null;
	}

	public static class DumpLocksSignalHandler implements SignalHandler {
		public void handle(Signal signal) {
			log.info(outstandingLockDescription());
		}
	}
	
	public static EOObjectStoreCoordinator create() {
		return new ERXObjectStoreCoordinator();
	}
	
	public static EOObjectStoreCoordinator create(boolean shouldClose) {
		return new ERXObjectStoreCoordinator(shouldClose);
	}
	
	protected String _name = "unnamed";
	
	/** @return a meaningful name for this OSC */
	public String name() {
		return _name;
	}
	
	/** @param name a meaningful name for this OSC */
	public void setName(String name) {
		_name = name;
	}
	
	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.append("name", name());
		return b.toString();
	}
}
