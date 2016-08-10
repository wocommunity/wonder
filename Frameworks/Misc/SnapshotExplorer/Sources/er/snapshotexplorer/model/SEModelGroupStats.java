package er.snapshotexplorer.model;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableDictionary;

public class SEModelGroupStats {
  private EOModelGroup _modelGroup;
  private NSMutableDictionary<String, SEModelStats> _modelStats;

  public SEModelGroupStats(EOModelGroup modelGroup) {
    _modelGroup = modelGroup;
  }

  public SEModelStats modelStatsForModelNamed(String modelName) {
    ensureModelsLoaded();
    return _modelStats.objectForKey(modelName);
  }

  public SEEntityStats entityStatsForEntityNamed(String entityName) {
    EOEntity entity = _modelGroup.entityNamed(entityName);
    EOModel model = entity.model();
    SEModelStats modelStats = modelStatsForModelNamed(model.name());
    SEEntityStats entityStats = modelStats.entityStatsForEntityNamed(entityName);
    return entityStats;
  }

  public NSArray<SEModelStats> modelStats() {
    ensureModelsLoaded();
    return new NSArray<SEModelStats>(_modelStats.values());
  }

  public int snapshotCount() {
    int snapshotCount = 0;
    for (SEModelStats modelStats : modelStats()) {
      snapshotCount += modelStats.snapshotCount();
    }
    return snapshotCount;
  }

  @SuppressWarnings( { "unchecked" })
  protected void ensureModelsLoaded() {
    if (_modelStats == null) {
      _modelStats = new NSMutableDictionary<String, SEModelStats>();
      for (EOModel model : _modelGroup.models()) {
        SEModelStats modelStats = new SEModelStats(model);
        _modelStats.setObjectForKey(modelStats, model.name());
      }
    }
  }
}
