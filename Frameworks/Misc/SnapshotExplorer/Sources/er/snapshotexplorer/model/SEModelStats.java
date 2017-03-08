package er.snapshotexplorer.model;

import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXEC;

public class SEModelStats {
  private EOModel _model;
  private NSMutableDictionary<String, SEEntityStats> _entityStats;

  public SEModelStats(EOModel model) {
    _model = model;
  }

  public EOModel model() {
    return _model;
  }

  public SEEntityStats entityStatsForEntityNamed(String entityName) {
    ensureSnapshotsLoaded();
    return _entityStats.objectForKey(entityName);
  }

  public NSArray<SEEntityStats> entityStats() {
    ensureSnapshotsLoaded();
    return new NSArray<>(_entityStats.values());
  }

  public int snapshotCount() {
    int snapshotCount = 0;
    for (SEEntityStats entityStats : entityStats()) {
      snapshotCount += entityStats.snapshotCount();
    }
    return snapshotCount;
  }

  @SuppressWarnings( { "unchecked", "cast" })
  protected void ensureSnapshotsLoaded() {
    if (_entityStats == null) {
      _entityStats = new NSMutableDictionary<>();
      EODatabaseContext databaseContext = EODatabaseContext.registeredDatabaseContextForModel(_model, ERXEC.newEditingContext());
      databaseContext.lock();
      try {
        EODatabase database = databaseContext.database();
        for (EOGlobalID gid : (NSArray<EOGlobalID>) database.snapshots().allKeys()) {
          if (gid instanceof EOKeyGlobalID) {
            EOKeyGlobalID kgid = (EOKeyGlobalID) gid;
            String entityName = kgid.entityName();
            SEEntityStats entityStats = _entityStats.get(entityName);
            if (entityStats == null) {
              EOEntity entity = _model.entityNamed(entityName);
              if (entity != null) {
                entityStats = new SEEntityStats(databaseContext, entity);
                _entityStats.put(entityName, entityStats);
              }
            }
            if (entityStats != null) {
              NSDictionary<String, Object> snapshot = (NSDictionary<String, Object>) database.snapshotForGlobalID(gid);
              entityStats.addSnapshot(kgid, snapshot);
            }
          }
        }
      }
      finally {
        databaseContext.unlock();
      }
    }
  }
}
