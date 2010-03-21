package er.snapshotexplorer.controllers;

import com.webobjects.appserver.WORequest;

import er.rest.routes.ERXRouteController;

public class SEController extends ERXRouteController {
  public SEController(WORequest request) {
    super(request);
  }

  @Override
  protected boolean isAutomaticHtmlRoutingEnabled() {
    return true;
  }

  @Override
  protected String pageNameForAction(String actionName) {
    return "SE" + super.pageNameForAction(actionName);
  }
}
