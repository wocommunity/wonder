package er.snapshotexplorer.model;

import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

public class SEEntityStats {
  private EODatabaseContext _databaseContext;
  private EOEntity _entity;
  private int _retainCount;
  private NSMutableArray<SESnapshotStats> _snapshotStats;

  public SEEntityStats(EODatabaseContext databaseContext, EOEntity entity) {
    _databaseContext = databaseContext;
    _entity = entity;
    _snapshotStats = new NSMutableArray<>();
  }

  public EOEntity entity() {
    return _entity;
  }

  public int snapshotCount() {
    return _snapshotStats.count();
  }

  public NSArray<SESnapshotStats> snapshotStats() {
    return _snapshotStats;
  }

  public void addSnapshot(EOKeyGlobalID gid, NSDictionary<String, Object> snapshot) {
    _snapshotStats.addObject(new SESnapshotStats(_databaseContext, gid, snapshot));
  }

  public void forget() {
    NSMutableArray<EOGlobalID> gids = new NSMutableArray<>();
    for (SESnapshotStats snapshotStats : snapshotStats()) {
      gids.addObject(snapshotStats.gid());
    }
    _databaseContext.lock();
    try {
      _databaseContext.database().forgetSnapshotsForGlobalIDs(gids);
    }
    finally {
      _databaseContext.unlock();
    }
  }

  public void invalidateResultCache() {
    _databaseContext.lock();
    try {
      _databaseContext.database().invalidateResultCacheForEntityNamed(_entity.name());
    }
    finally {
      _databaseContext.unlock();
    }
  }
}
