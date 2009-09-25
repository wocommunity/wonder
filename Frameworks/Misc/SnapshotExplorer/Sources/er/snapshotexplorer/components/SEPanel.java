package er.snapshotexplorer.components;

import com.webobjects.appserver.WOContext;

public class SEPanel extends SEComponent {
  public SEPanel(WOContext context) {
    super(context);
  }

  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
}