package er.snapshotexplorer.components;

import com.webobjects.appserver.WOContext;

import er.extensions.appserver.ERXApplication;

public class SEPage extends SEComponent {
  public SEPage(WOContext context) {
    super(context);
  }

  @Override
  protected void checkAccess() throws SecurityException {
    super.checkAccess();
    if (!ERXApplication.isDevelopmentModeSafe()) {
      throw new SecurityException("SnapshotExplorer is currently only available in development mode.");
    }
  }

  @Override
  protected boolean isPageAccessAllowed() {
    return true;
  }
}
