package er.snapshotexplorer;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXDirectAction;
import er.snapshotexplorer.components.pages.SEEOModelIndexPage;

public class SEDirectAction extends ERXDirectAction {
  public SEDirectAction(WORequest r) {
    super(r);
  }

  @Override
  public WOActionResults defaultAction() {
    return pageWithName(SEEOModelIndexPage.class);
  }
}
