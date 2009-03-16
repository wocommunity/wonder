package er.uber;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXDirectAction;

public class PageAction extends ERXDirectAction {
  public PageAction(WORequest request) {
    super(request);
  }

  @Override
  public WOActionResults performActionNamed(String actionName) {
    return pageWithName(actionName);
  }
}
