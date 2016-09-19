package er.snapshotexplorer.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

import er.extensions.appserver.ERXDisplayGroup;
import er.extensions.components.ERXComponent;
import er.snapshotexplorer.model.SESnapshotStats;

public class SESnapshotsList extends ERXComponent {
  private ERXDisplayGroup<SESnapshotStats> _snapshotStatsDisplayGroup;
  public EOAttribute _attribute;
  public SESnapshotStats _snapshotStat;

  public SESnapshotsList(WOContext context) {
    super(context);
    _snapshotStatsDisplayGroup = new ERXDisplayGroup<>();
    _snapshotStatsDisplayGroup.setNumberOfObjectsPerBatch(25);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public EOEntity entity() {
    return (EOEntity) valueForBinding("entity");
  }

  @SuppressWarnings("unchecked")
  public ERXDisplayGroup<SESnapshotStats> snapshotStatsDisplayGroup() {
    NSArray<SESnapshotStats> snapshotStats = (NSArray<SESnapshotStats>) valueForBinding("snapshotStats");
    _snapshotStatsDisplayGroup.setObjectArray(snapshotStats);
    return _snapshotStatsDisplayGroup;
  }

  public Object value() {
    Object value = _snapshotStat.snapshot().objectForKey(_attribute.name());
    if (value instanceof NSKeyValueCoding.Null) {
      value = null;
    }
    return value;
  }
}