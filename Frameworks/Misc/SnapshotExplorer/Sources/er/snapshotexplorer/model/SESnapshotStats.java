package er.snapshotexplorer.model;

import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSDictionary;

public class SESnapshotStats {
  private EODatabaseContext _databaseContext;
  private EOGlobalID _gid;
  private NSDictionary<String, Object> _snapshot;

  public SESnapshotStats(EODatabaseContext databaseContext, EOGlobalID gid, NSDictionary<String, Object> snapshot) {
    _databaseContext = databaseContext;
    _gid = gid;
    _snapshot = snapshot;
  }

  public EOGlobalID gid() {
    return _gid;
  }

  public NSDictionary<String, Object> snapshot() {
    return _snapshot;
  }

  public int retainCount() {
    _databaseContext.lock();
    try {
      return _databaseContext.database()._snapshotCountForGlobalID(_gid);
    }
    finally {
      _databaseContext.unlock();
    }
  }

  public void forget() {
    _databaseContext.lock();
    try {
      _databaseContext.database().forgetSnapshotForGlobalID(_gid);
    }
    finally {
      _databaseContext.unlock();
    }
  }
}
