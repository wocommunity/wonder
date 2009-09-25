package er.snapshotexplorer.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;
import er.snapshotexplorer.model.SEEntityStats;

public class SEEntitySnapshots extends ERXComponent {
  private SEEntityStats _entityStats;

  public SEEntitySnapshots(WOContext context) {
    super(context);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }

  public SEEntityStats entityStats() {
    return (SEEntityStats) valueForBinding("entityStats");
  }
}