package er.extensions.eof;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOTemporaryGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSet;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;

public class ERXDatabase extends EODatabase {
	public static final String SnapshotCacheChanged = "SnapshotCacheChanged";
	public static final String CacheChangeKey = "CacheChange";
	protected static int SnapshotCacheMapInitialCapacity = 1048576;
	protected static float SnapshotCacheMapInitialLoadFactor = 0.75f;

	private boolean _globalIDChanged;
	private boolean _decrementSnapshot;

	public static void setSnapshotCacheMapInitialCapacity( int capacity ) {
		NSLog.out.appendln( "Setting SnapshotCacheMapInitialCapacity = " + capacity );
		SnapshotCacheMapInitialCapacity = capacity;
	}
	
	public static void setSnapshotCacheMapInitialLoadFactor( float loadFactor ) {
		NSLog.out.appendln( "Setting SnapshotCacheMapInitialLoadFactor = " + loadFactor );
		SnapshotCacheMapInitialLoadFactor = loadFactor;
	}
	
	public ERXDatabase(EOAdaptor adaptor) {
		super(adaptor);

		// AK: huge performance optimization when you use badly distributed LONG keys

		NSLog.out.appendln( "Using SnapshotCacheMapInitialCapacity = " + SnapshotCacheMapInitialCapacity );
		NSLog.out.appendln( "Using SnapshotCacheMapInitialLoadFactor = " + SnapshotCacheMapInitialLoadFactor );
		_snapshots = new NSMutableDictionary() {
			/**
			 * Do I need to update serialVersionUID?
			 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
			 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
			 */
			private static final long serialVersionUID = 1L;

			Map hashMap = new HashMap( SnapshotCacheMapInitialCapacity, SnapshotCacheMapInitialLoadFactor );

			@Override
			public Object objectForKey(Object key) {
				return hashMap.get(key);
			}

			@Override
			public void setObjectForKey(Object object, Object key) {
				hashMap.put(key, object);
			}

			@Override
			public Object removeObjectForKey(Object key) {
				return hashMap.remove(key);
			}

			@Override
			public NSDictionary immutableClone() {
				return new NSDictionary(hashMap);
			}

			@Override
			public NSArray allKeys() {
				return new NSArray(hashMap.keySet());
			}

			@Override
			public int size() {
				return hashMap.size();
			}

			@Override
			public int count() {
				return hashMap.size();
			}
		};
	}

	public ERXDatabase(EOModel model) {
		super(model);
	}

	public ERXDatabase(EODatabase _database) {
		this(_database.adaptor());
		Enumeration modelsEnum = _database.models().objectEnumerator();
		while (modelsEnum.hasMoreElements()) {
			EOModel model = (EOModel) modelsEnum.nextElement();
			addModel(model);
		}
		_database.dispose();
	}

	public int snapshotCacheSize() {
		return _snapshots.size();
	}

	public synchronized void _notifyCacheChange(CacheChange cacheChange) {
		NSNotificationCenter.defaultCenter().postNotification(ERXDatabase.SnapshotCacheChanged, this, new NSDictionary(cacheChange, ERXDatabase.CacheChangeKey));
	}

	@Override
	protected NSSet _cachedFetchAttributesForEntityNamed(String name) {
		return super._cachedFetchAttributesForEntityNamed(name);
	}

	@Override
	protected void _clearLastRecords() {
		super._clearLastRecords();
	}

	@Override
	protected _DatabaseRecord _fastHashGet(EOGlobalID gid) {
		return super._fastHashGet(gid);
	}

	@Override
	protected void _fastHashInsert(_DatabaseRecord rec, EOGlobalID gid) {
		super._fastHashInsert(rec, gid);
		if (_globalIDChanged) {
			_notifyCacheChange(new SnapshotInserted(gid, snapshotForGlobalID(gid)));
		}
	}

	@Override
	protected void _fastHashRemove(EOGlobalID gid) {
		// if (_decrementSnapshot) {
		// System.out.println("ERXDatabase._fastHashRemove: remove " + gid);
		// NSLog.out.appendln(new RuntimeException());
		// }
		super._fastHashRemove(gid);
	}

	@Override
	public void _forgetSnapshotForGlobalID(EOGlobalID gid) {
		super._forgetSnapshotForGlobalID(gid);
	}

	@Override
	protected void _freeToManyMap(_DatabaseRecord rec) {
		super._freeToManyMap(rec);
	}

	@Override
	public void _globalIDChanged(NSNotification notification) {
		boolean oldGlobalIDChanged = _globalIDChanged;
		_globalIDChanged = true;
		try {
			super._globalIDChanged(notification);
		}
		finally {
			_globalIDChanged = oldGlobalIDChanged;
		}
	}

	@Override
	public void recordSnapshotForGlobalID(NSDictionary snapshot, EOGlobalID gid) {
		if (!ERXDatabaseContext.isFetching() && !(gid instanceof EOTemporaryGlobalID)) {
			_notifyCacheChange(new SnapshotUpdated(gid, snapshot));
		}
		super.recordSnapshotForGlobalID(snapshot, gid);
	}

	@Override
	public void recordSnapshotForSourceGlobalID(NSArray gids, EOGlobalID gid, String name) {
		if (!ERXDatabaseContext.isFetching()) {
			NSArray originalToManyGIDs = snapshotForSourceGlobalID(gid, name);
			_notifyCacheChange(new ToManySnapshotUpdated(gid, name, originalToManyGIDs, gids));
		}
		super.recordSnapshotForSourceGlobalID(gids, gid, name);
	}

	@Override
	public int _indexOfRegisteredContext(EODatabaseContext context) {
		return super._indexOfRegisteredContext(context);
	}

	@Override
	protected EOGlobalID _recordedGIDForSnapshotWithGid(EOGlobalID gid) {
		return super._recordedGIDForSnapshotWithGid(gid);
	}

	@Override
	protected void _setTimestampForCachedGlobalID(EOGlobalID gid) {
		super._setTimestampForCachedGlobalID(gid);
	}

	@Override
	public int _snapshotCountForGlobalID(EOGlobalID gid) {
		return super._snapshotCountForGlobalID(gid);
	}

	@Override
	public EOAdaptor adaptor() {
		return super.adaptor();
	}

	@Override
	public void addModel(EOModel model) {
		super.addModel(model);
	}

	@Override
	public boolean addModelIfCompatible(EOModel model) {
		return super.addModelIfCompatible(model);
	}

	@Override
	public void decrementSnapshotCountForGlobalID(EOGlobalID gid) {
		boolean oldDecrementSnapshot = _decrementSnapshot;
		_decrementSnapshot = true;
		try {
			super.decrementSnapshotCountForGlobalID(gid);
		}
		finally {
			_decrementSnapshot = oldDecrementSnapshot;
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public EOEntity entityForObject(EOEnterpriseObject object) {
		return super.entityForObject(object);
	}

	@Override
	public EOEntity entityNamed(String entityName) {
		return super.entityNamed(entityName);
	}

	@Override
	public void forgetAllSnapshots() {
		super.forgetAllSnapshots();
	}

	@Override
	public void forgetSnapshotForGlobalID(EOGlobalID gid) {
		super.forgetSnapshotForGlobalID(gid);
	}

	@Override
	public void forgetSnapshotsForGlobalIDs(NSArray array) {
		super.forgetSnapshotsForGlobalIDs(array);
	}

	@Override
	public void handleDroppedConnection() {
		super.handleDroppedConnection();
	}

	@Override
	public void incrementSnapshotCountForGlobalID(EOGlobalID gid) {
		super.incrementSnapshotCountForGlobalID(gid);
	}

	@Override
	public void invalidateResultCache() {
		super.invalidateResultCache();
	}

	@Override
	public void invalidateResultCacheForEntityNamed(String name) {
		super.invalidateResultCacheForEntityNamed(name);
	}

	@Override
	public NSArray models() {
		return super.models();
	}

	@Override
	public void recordSnapshots(NSDictionary snapshots) {
		super.recordSnapshots(snapshots);
	}

	@Override
	public void recordToManySnapshots(NSDictionary snapshots) {
		super.recordToManySnapshots(snapshots);
	}

	@Override
	public void registerContext(EODatabaseContext context) {
		super.registerContext(context);
	}

	@Override
	public NSArray registeredContexts() {
		return super.registeredContexts();
	}

	@Override
	public void removeModel(EOModel model) {
		super.removeModel(model);
	}

	@Override
	public NSArray resultCacheForEntityNamed(String name) {
		return super.resultCacheForEntityNamed(name);
	}

	@Override
	public void setResultCache(NSArray cache, String name) {
		super.setResultCache(cache, name);
	}

	@Override
	public void setTimestampToNow() {
		super.setTimestampToNow();
	}

	@Override
	public NSDictionary snapshotForGlobalID(EOGlobalID gid, long timestamp) {
		return super.snapshotForGlobalID(gid, timestamp);
	}

	@Override
	public NSDictionary snapshotForGlobalID(EOGlobalID gid) {
		return super.snapshotForGlobalID(gid);
	}

	@Override
	public NSArray snapshotForSourceGlobalID(EOGlobalID gid, String name, long timestamp) {
		return super.snapshotForSourceGlobalID(gid, name, timestamp);
	}

	@Override
	public NSArray snapshotForSourceGlobalID(EOGlobalID gid, String name) {
		return super.snapshotForSourceGlobalID(gid, name);
	}

	@Override
	public NSDictionary snapshots() {
		return super.snapshots();
	}

	@Override
	public long timestampForGlobalID(EOGlobalID gid) {
		return super.timestampForGlobalID(gid);
	}

	@Override
	public long timestampForSourceGlobalID(EOGlobalID gid, String name) {
		return super.timestampForSourceGlobalID(gid, name);
	}

	@Override
	public void unregisterContext(EODatabaseContext context) {
		super.unregisterContext(context);
	}

	public static abstract class CacheChange {
		private EOGlobalID _gid;

		public CacheChange(EOGlobalID gid) {
			_gid = gid;
		}

		public EOGlobalID gid() {
			return _gid;
		}

		@Override
		public String toString() {
			return "[" + ERXStringUtilities.getSimpleClassName(getClass()) + ": " + _gid + "]";
		}
	}

	public static abstract class SnapshotCacheChange extends CacheChange {
		private NSDictionary _snapshot;

		public SnapshotCacheChange(EOGlobalID gid, NSDictionary snapshot) {
			super(gid);
			_snapshot = snapshot;
		}

		public NSDictionary snapshot() {
			return _snapshot;
		}
	}

	public static class SnapshotInserted extends SnapshotCacheChange {
		public SnapshotInserted(EOGlobalID gid, NSDictionary snapshot) {
			super(gid, snapshot);
		}
	}

	public static class SnapshotUpdated extends SnapshotCacheChange {
		public SnapshotUpdated(EOGlobalID gid, NSDictionary snapshot) {
			super(gid, snapshot);
		}
	}

	public static class SnapshotDeleted extends SnapshotCacheChange {
		public SnapshotDeleted(EOGlobalID gid, NSDictionary snapshot) {
			super(gid, snapshot);
		}
	}

	public static class ToManySnapshotUpdated extends CacheChange {
		private String _name;
		private NSArray _addedGIDs;
		private NSArray _removedGIDs;
		private boolean _removeAll;

		public ToManySnapshotUpdated(EOGlobalID sourceGID, String name, NSArray addedGIDs, NSArray removedGIDs, boolean removeAll) {
			super(sourceGID);
			_name = name;
			_addedGIDs = addedGIDs;
			_removedGIDs = removedGIDs;
			_removeAll = removeAll;
		}
		
		public ToManySnapshotUpdated(EOGlobalID sourceGID, String name, NSArray originalToManyGIDs, NSArray newToManyGIDs) {
			super(sourceGID);
			_name = name;
			if (originalToManyGIDs == null || originalToManyGIDs.count() == 0) {
				_addedGIDs = newToManyGIDs;
			}
			else if (newToManyGIDs == null || newToManyGIDs.count() == 0) {
				_removeAll = true;
				_removedGIDs = originalToManyGIDs;
			}
			else {
				_addedGIDs = ERXArrayUtilities.arrayMinusArray(newToManyGIDs, originalToManyGIDs);
				_removedGIDs = ERXArrayUtilities.arrayMinusArray(originalToManyGIDs, newToManyGIDs);
			}
		}

		public String name() {
			return _name;
		}

		public NSArray removedGIDs() {
			return _removedGIDs;
		}

		public boolean removeAll() {
			return _removeAll;
		}

		public NSArray addedGIDs() {
			return _addedGIDs;
		}

		@Override
		public String toString() {
			return "[ToManySnapshotChanged: sourceGID = " + gid() + "; name = " + _name + "; added = " + ((_addedGIDs == null) ? 0 : _addedGIDs.count()) + "; removed = " + ((_removedGIDs == null) ? 0 : _removedGIDs.count()) + "; removeAll = " + _removeAll + "]";
		}
	}
}
