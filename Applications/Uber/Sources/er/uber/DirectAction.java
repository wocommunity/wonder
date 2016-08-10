package er.uber;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXDirectAction;
import er.uber.components.Main;
import er.uber.components.RewriteBroken;
import er.uber.components.RewriteWorked;

public class DirectAction extends ERXDirectAction {
  public DirectAction(WORequest request) {
    super(request);
  }

  @Override
  public WOActionResults defaultAction() {
    return pageWithName(Main.class);
  }

  public WOActionResults rewriteBrokenAction() {
    return pageWithName(RewriteBroken.class);
  }

  public WOActionResults rewriteWorkedAction() {
    return pageWithName(RewriteWorked.class);
  }
}
