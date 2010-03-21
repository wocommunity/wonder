package er.snapshotexplorer.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class SEComponent extends ERXComponent {
  public SEComponent(WOContext context) {
    super(context);
  }

  @Override
  protected boolean isPageAccessAllowed() {
    return false;
  }
}
