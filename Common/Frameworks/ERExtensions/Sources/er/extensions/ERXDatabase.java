package er.extensions;

import java.util.Enumeration;

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
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSet;

public class ERXDatabase extends EODatabase {
	public static final String SnapshotCacheChanged = "SnapshotCacheChanged";
	public static final String CacheChangeKey = "CacheChange";
	
	private boolean _globalIDChanged;
	private boolean _decrementSnapshot;

	public ERXDatabase(EOAdaptor adaptor) {
		super(adaptor);
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

	public synchronized void _notifyCacheChange(CacheChange cacheChange) {
		NSNotificationCenter.defaultCenter().postNotification(ERXDatabase.SnapshotCacheChanged, this, new NSDictionary(cacheChange, ERXDatabase.CacheChangeKey));
	}

	protected NSSet _cachedFetchAttributesForEntityNamed(String name) {
		return super._cachedFetchAttributesForEntityNamed(name);
	}

	protected void _clearLastRecords() {
		super._clearLastRecords();
	}

	protected _DatabaseRecord _fastHashGet(EOGlobalID gid) {
		return super._fastHashGet(gid);
	}

	protected void _fastHashInsert(_DatabaseRecord rec, EOGlobalID gid) {
		super._fastHashInsert(rec, gid);
		if (_globalIDChanged) {
			_notifyCacheChange(new SnapshotInserted(gid, snapshotForGlobalID(gid)));
		}
	}

	protected void _fastHashRemove(EOGlobalID gid) {
		// if (_decrementSnapshot) {
		// System.out.println("ERXDatabase._fastHashRemove: remove " + gid);
		// NSLog.out.appendln(new RuntimeException());
		// }
		super._fastHashRemove(gid);
	}

	public void _forgetSnapshotForGlobalID(EOGlobalID gid) {
		super._forgetSnapshotForGlobalID(gid);
	}

	protected void _freeToManyMap(_DatabaseRecord rec) {
		super._freeToManyMap(rec);
	}

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

	public void recordSnapshotForGlobalID(NSDictionary snapshot, EOGlobalID gid) {
		if (!ERXDatabaseContext.isFetching() && !(gid instanceof EOTemporaryGlobalID)) {
			_notifyCacheChange(new SnapshotUpdated(gid, snapshot));
		}
		super.recordSnapshotForGlobalID(snapshot, gid);
	}

	public void recordSnapshotForSourceGlobalID(NSArray gids, EOGlobalID gid, String name) {
		if (!ERXDatabaseContext.isFetching()) {
			NSArray originalToManyGIDs = snapshotForSourceGlobalID(gid, name);
			_notifyCacheChange(new ToManySnapshotUpdated(gid, name, originalToManyGIDs, gids));
		}
		super.recordSnapshotForSourceGlobalID(gids, gid, name);
	}

	public int _indexOfRegisteredContext(EODatabaseContext context) {
		return super._indexOfRegisteredContext(context);
	}

	protected EOGlobalID _recordedGIDForSnapshotWithGid(EOGlobalID gid) {
		return super._recordedGIDForSnapshotWithGid(gid);
	}

	protected void _setTimestampForCachedGlobalID(EOGlobalID gid) {
		super._setTimestampForCachedGlobalID(gid);
	}

	public int _snapshotCountForGlobalID(EOGlobalID gid) {
		return super._snapshotCountForGlobalID(gid);
	}

	public EOAdaptor adaptor() {
		return super.adaptor();
	}

	public void addModel(EOModel model) {
		super.addModel(model);
	}

	public boolean addModelIfCompatible(EOModel model) {
		return super.addModelIfCompatible(model);
	}

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

	public void dispose() {
		super.dispose();
	}

	public EOEntity entityForObject(EOEnterpriseObject object) {
		return super.entityForObject(object);
	}

	public EOEntity entityNamed(String entityName) {
		return super.entityNamed(entityName);
	}

	public void forgetAllSnapshots() {
		super.forgetAllSnapshots();
	}

	public void forgetSnapshotForGlobalID(EOGlobalID gid) {
		super.forgetSnapshotForGlobalID(gid);
	}

	public void forgetSnapshotsForGlobalIDs(NSArray array) {
		super.forgetSnapshotsForGlobalIDs(array);
	}

	public void handleDroppedConnection() {
		super.handleDroppedConnection();
	}

	public void incrementSnapshotCountForGlobalID(EOGlobalID gid) {
		super.incrementSnapshotCountForGlobalID(gid);
	}

	public void invalidateResultCache() {
		super.invalidateResultCache();
	}

	public void invalidateResultCacheForEntityNamed(String name) {
		super.invalidateResultCacheForEntityNamed(name);
	}

	public NSArray models() {
		return super.models();
	}

	public void recordSnapshots(NSDictionary snapshots) {
		super.recordSnapshots(snapshots);
	}

	public void recordToManySnapshots(NSDictionary snapshots) {
		super.recordToManySnapshots(snapshots);
	}

	public void registerContext(EODatabaseContext context) {
		super.registerContext(context);
	}

	public NSArray registeredContexts() {
		return super.registeredContexts();
	}

	public void removeModel(EOModel model) {
		super.removeModel(model);
	}

	public NSArray resultCacheForEntityNamed(String name) {
		return super.resultCacheForEntityNamed(name);
	}

	public void setResultCache(NSArray cache, String name) {
		super.setResultCache(cache, name);
	}

	public void setTimestampToNow() {
		super.setTimestampToNow();
	}

	public NSDictionary snapshotForGlobalID(EOGlobalID gid, long timestamp) {
		return super.snapshotForGlobalID(gid, timestamp);
	}

	public NSDictionary snapshotForGlobalID(EOGlobalID gid) {
		return super.snapshotForGlobalID(gid);
	}

	public NSArray snapshotForSourceGlobalID(EOGlobalID gid, String name, long timestamp) {
		return super.snapshotForSourceGlobalID(gid, name, timestamp);
	}

	public NSArray snapshotForSourceGlobalID(EOGlobalID gid, String name) {
		return super.snapshotForSourceGlobalID(gid, name);
	}

	public NSDictionary snapshots() {
		return super.snapshots();
	}

	public long timestampForGlobalID(EOGlobalID gid) {
		return super.timestampForGlobalID(gid);
	}

	public long timestampForSourceGlobalID(EOGlobalID gid, String name) {
		return super.timestampForSourceGlobalID(gid, name);
	}

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

		public String toString() {
			return "[ToManySnapshotChanged: sourceGID = " + gid() + "; name = " + _name + "; added = " + ((_addedGIDs == null) ? 0 : _addedGIDs.count()) + "; removed = " + ((_removedGIDs == null) ? 0 : _removedGIDs.count()) + "; removeAll = " + _removeAll + "]";
		}
	}
}
