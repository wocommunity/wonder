package er.extensions.eof;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOTemporaryGlobalID;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXStringUtilities;

public class ERXDatabase extends EODatabase.Implementation {
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
	// WO 5.6: VERIFY THIS
	public void recordSnapshotForGlobalID(Map<String, Object> snapshot, EOGlobalID gid) {
		Map<String, Object> existingSnapshot = snapshotForGlobalID(gid);
		super.recordSnapshotForGlobalID(snapshot, gid);
		System.out.println("ERXDatabase.recordSnapshotForGlobalID: *************************** THIS HAS NOT BEEN VERIFIED ******************************");
		if (!ERXDatabaseContext.isFetching() && !(gid instanceof EOTemporaryGlobalID)) {
			if (existingSnapshot == null) {
				_notifyCacheChange(new SnapshotInserted(gid, snapshotForGlobalID(gid)));
			}
			else {
				_notifyCacheChange(new SnapshotUpdated(gid, snapshot));
			}
		}
	}

	@Override
	public void recordSnapshotForSourceGlobalID(List<EOGlobalID> gids, EOGlobalID gid, String name) {
		if (!ERXDatabaseContext.isFetching()) {
			List<EOGlobalID> originalToManyGIDs = snapshotForSourceGlobalID(gid, name);
			_notifyCacheChange(new ToManySnapshotUpdated(gid, name, originalToManyGIDs, gids));
		}
		super.recordSnapshotForSourceGlobalID(gids, gid, name);
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
		private Map<String, Object> _snapshot;

		public SnapshotCacheChange(EOGlobalID gid, Map<String, Object> snapshot) {
			super(gid);
			_snapshot = snapshot;
		}

		public Map<String, Object> snapshot() {
			return _snapshot;
		}
	}

	public static class SnapshotInserted extends SnapshotCacheChange {
		public SnapshotInserted(EOGlobalID gid, Map<String, Object> snapshot) {
			super(gid, snapshot);
		}
	}

	public static class SnapshotUpdated extends SnapshotCacheChange {
		public SnapshotUpdated(EOGlobalID gid, Map<String, Object> snapshot) {
			super(gid, snapshot);
		}
	}

	public static class SnapshotDeleted extends SnapshotCacheChange {
		public SnapshotDeleted(EOGlobalID gid, Map<String, Object> snapshot) {
			super(gid, snapshot);
		}
	}

	public static class ToManySnapshotUpdated extends CacheChange {
		private String _name;
		private List<EOGlobalID> _addedGIDs;
		private List<EOGlobalID> _removedGIDs;
		private boolean _removeAll;

		public ToManySnapshotUpdated(EOGlobalID sourceGID, String name, List<EOGlobalID> addedGIDs, List<EOGlobalID> removedGIDs, boolean removeAll) {
			super(sourceGID);
			_name = name;
			_addedGIDs = addedGIDs;
			_removedGIDs = removedGIDs;
			_removeAll = removeAll;
		}
		
		public ToManySnapshotUpdated(EOGlobalID sourceGID, String name, List<EOGlobalID> originalToManyGIDs, List<EOGlobalID> newToManyGIDs) {
			super(sourceGID);
			_name = name;
			if (originalToManyGIDs == null || originalToManyGIDs.size() == 0) {
				_addedGIDs = newToManyGIDs;
			}
			else if (newToManyGIDs == null || newToManyGIDs.size() == 0) {
				_removeAll = true;
				_removedGIDs = originalToManyGIDs;
			}
			else {
				_addedGIDs = ERXArrayUtilities.listMinusList(newToManyGIDs, originalToManyGIDs);
				_removedGIDs = ERXArrayUtilities.listMinusList(originalToManyGIDs, newToManyGIDs);
			}
		}

		public String name() {
			return _name;
		}

		public List<EOGlobalID> removedGIDs() {
			return _removedGIDs;
		}

		public boolean removeAll() {
			return _removeAll;
		}

		public List<EOGlobalID> addedGIDs() {
			return _addedGIDs;
		}

		public String toString() {
			return "[ToManySnapshotChanged: sourceGID = " + gid() + "; name = " + _name + "; added = " + ((_addedGIDs == null) ? 0 : _addedGIDs.size()) + "; removed = " + ((_removedGIDs == null) ? 0 : _removedGIDs.size()) + "; removeAll = " + _removeAll + "]";
		}
	}
}
