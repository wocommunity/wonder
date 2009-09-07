package er.extensions.eof;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EODatabaseOperation;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXUtilities;

public class ERXDatabaseContext extends EODatabaseContext {

	/** general logging */
	public static final Logger log = Logger.getLogger(ERXDatabaseContext.class);

	private static ThreadLocal _fetching = new ThreadLocal();

	NSMutableDictionary<Thread, NSMutableArray<Exception>> openLockTraces;
	/**
	 * if traceOpenEditingContextLocks is true, this will contain the name of
	 * the locking thread
	 */
	Thread lockingThread;

	protected static Map<ERXDatabaseContext, String> activeDatabaseContexts = Collections.synchronizedMap(new WeakHashMap());

	private long lockCount = 0;

	public ERXDatabaseContext(EODatabase database) {
		super(new ERXDatabase(database));
		if (ERXEC.traceOpenLocks()) {
			activeDatabaseContexts.put(this, Thread.currentThread().getName());
		}
	}

	/**
	 * Adds the current stack trace to openLockTraces. 
	 */
	private synchronized void traceLock() {
		if(openLockTraces == null) {
			openLockTraces = new NSMutableDictionary<Thread, NSMutableArray<Exception>>();
		}
		Exception openLockTrace = new Exception("Locked");
		openLockTrace.fillInStackTrace();
		Thread currentThread = Thread.currentThread();
		NSMutableArray<Exception> currentTraces = openLockTraces.objectForKey(currentThread);
		if(currentTraces == null) {
			currentTraces = new NSMutableArray<Exception>();
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
			traces.removeLastObject();
			if (traces.count() == 0) {
				openLockTraces.removeObjectForKey(lockingThread);
			}
			if (openLockTraces.count() == 0) {
				openLockTraces = null;
			}
		}
		if(lockCount == 0) {
			lockingThread = null;
		}
	}
	
	/**
	 * Overridden to emmit log messages and push this instance to the locked
	 * editing contexts in this thread.
	 */
	public void lock() {
		boolean tracing = ERXEC.traceOpenLocks();
		if (tracing) {
			traceLock();
		}
		super.lock();
		lockCount++;
		lockingThread = Thread.currentThread();
	}

	/**
	 * Overridden to emmit log messages and pull this instance from the locked
	 * editing contexts in this thread.
	 */
	public void unlock() {
		lockCount--;
		super.unlock();
		if (ERXEC.traceOpenLocks()) {
			traceUnlock();
		}
	}

	public static boolean isFetching() {
		Boolean fetching = (Boolean) _fetching.get();
		// System.out.println("ERXDatabaseContext.isFetching: " +
		// Thread.currentThread() + ", " + fetching);
		return fetching != null && fetching.booleanValue();
	}

	public static void setFetching(boolean fetching) {
		// System.out.println("ERXDatabaseContext.setFetching: " +
		// Thread.currentThread() + ", " + fetching);
		_fetching.set(Boolean.valueOf(fetching));
	}

	public NSArray objectsForSourceGlobalID(EOGlobalID gid, String name, EOEditingContext context) {
		NSArray results;
		boolean fetching = isFetching();
		if (!fetching) {
			setFetching(true);
		}
		try {
			results = super.objectsForSourceGlobalID(gid, name, context);
		}
		finally {
			if (!fetching) {
				setFetching(false);
			}
		}
		return results;
	}

	public NSArray _objectsWithFetchSpecificationEditingContext(EOFetchSpecification fetchSpec, EOEditingContext context) {
		NSArray results;
		boolean fetching = isFetching();
		if (!fetching) {
			setFetching(!fetchSpec.refreshesRefetchedObjects());
		}
		try {
			results = super._objectsWithFetchSpecificationEditingContext(fetchSpec, context);
		}
		finally {
			if (!fetching) {
				setFetching(false);
			}
		}
		return results;
	}

	public void _verifyNoChangesToReadonlyEntity(EODatabaseOperation dbOp) {
		EOEntity entity = dbOp.entity();
		if (entity.isReadOnly()) {
			switch (dbOp.databaseOperator()) {
			case 0: // '\0'
				return;

			case 1: // '\001'
				throw new IllegalStateException("cannot insert object:" + dbOp.object() + " that corresponds to read-only entity: " + entity.name() + " in databaseContext " + this);

			case 3: // '\003'
				throw new IllegalStateException("cannot delete object:" + dbOp.object() + " that corresponds to read-only entity:" + entity.name() + " in databaseContext " + this);

			case 2: // '\002'
				if (!dbOp.dbSnapshot().equals(dbOp.newRow())) {
					throw new IllegalStateException("cannot update '" + dbOp.rowDiffsForAttributes(entity.attributes()).allKeys() + "' keys on object:" + dbOp.object() + " that corresponds to read-only entity: " + entity.name() + " in databaseContext " + this);
				}
				else {
					return;
				}
			}
		}
		// HACK: ak these methods are protected, so we call them via KVC
		if (dbOp.databaseOperator() == 2 && ((Boolean) NSKeyValueCoding.Utility.valueForKey(entity, "_hasNonUpdateableAttributes")).booleanValue()) {
			NSArray keys = (NSArray) NSKeyValueCoding.Utility.valueForKey(entity, "dbSnapshotKeys");
			NSDictionary dbSnapshot = dbOp.dbSnapshot();
			NSDictionary newRow = dbOp.newRow();
			for (int i = keys.count() - 1; i >= 0; i--) {
				String key = (String) keys.objectAtIndex(i);
				EOAttribute att = entity.attributeNamed(key);
				// FIX: ak when you have single-table inheritance and in the
				// child there are foreign keys that are not in the parent
				// THEN, if the entity _hasNonUpdateableAttributes (public PK or
				// read only props) the DB op is checked
				// against the attributes. BUT this dictionary has all entries,
				// even from the child (most likely NULL values)
				// and the current implementation doesn't check against the case
				// when the attribute isn't present in the first place.
				// SO we add this check and live happily ever after
				if (att != null && att._isNonUpdateable() && !dbSnapshot.objectForKey(key).equals(newRow.objectForKey(key))) {
					if (att.isReadOnly()) {
						throw new IllegalStateException("cannot update read-only key '" + key + "' on object:" + dbOp.object() + " of entity: " + entity.name() + " in databaseContext " + this);
					}
					else {
						throw new IllegalStateException("cannot update primary-key '" + key + "' from '" + dbSnapshot.objectForKey(key) + "' to '" + newRow.objectForKey(key) + "' on object:" + dbOp.object() + " of entity: " + entity.name() + " in databaseContext " + this);
					}
				}
			}

		}
	}

	public static class OpenDatabaseContextLockSignalHandler implements SignalHandler {
		public void handle(Signal signal) {
			boolean hadLocks = false;
			for (ERXDatabaseContext dbc : activeDatabaseContexts.keySet()) {
				NSMutableDictionary<Thread, NSMutableArray<Exception>> traces = dbc.openLockTraces;
				if (traces != null && traces.count() > 0) {
					hadLocks = true;
					log.info("  Database Context: " + dbc + " Lock active: " + dbc.lockingThread.getName());
					for (Thread thread : traces.keySet()) {
						log.info("  By: " + thread);
						for(Exception ex: traces.objectForKey(thread)) {
							log.info("", ex);
							log.info("");
						}
					}
				}
			}
			if(!hadLocks) {
				log.info("No open database contexts");
			}
		}
	}
}
