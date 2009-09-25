package er.snapshotexplorer.components;

import com.webobjects.appserver.WOContext;

public class SEHeaderFooter extends SEComponent {
  public SEHeaderFooter(WOContext context) {
    super(context);
  }
  
  @Override
  public boolean synchronizesVariablesWithBindings() {
    return false;
  }
}