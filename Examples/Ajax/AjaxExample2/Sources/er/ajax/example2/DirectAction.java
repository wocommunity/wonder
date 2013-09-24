package er.ajax.example2;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.ajax.example2.components.Main;
import er.extensions.appserver.ERXDirectAction;

public class DirectAction extends ERXDirectAction {
  public DirectAction(WORequest request) {
    super(request);
  }

  @Override
  public WOActionResults defaultAction() {
    return pageWithName(Main.class.getName());
  }

  public WOActionResults sessionExpiredAction() {
    ((Session) session()).addNotification("Your session expired.");
    return defaultAction();
  }
}
