package er.uber.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;

public class AppserverTools extends UberComponent {
  public AppserverTools(WOContext context) {
    super(context);
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    super.appendToResponse(response, context);
    AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
    AjaxUtils.addScriptResourceInHead(context, response, "effects.js");
    AjaxUtils.addScriptResourceInHead(context, response, "wonder.js");
  }
}