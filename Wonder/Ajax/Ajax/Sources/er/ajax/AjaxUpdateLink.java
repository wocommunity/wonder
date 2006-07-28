package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class AjaxUpdateLink extends AjaxDynamicElement {

  public AjaxUpdateLink(String name, NSDictionary associations, WOElement children) {
      super(name, associations, children);
  }

  public String onClick(WOContext context) {
    NSDictionary options = createAjaxOptions(context.component());
    String actionUrl = context.componentActionURL();
    String id = (String) valueForBinding("updateContainerID", context.component());
    StringBuffer sb = new StringBuffer();
    sb.append("new Ajax.Updater('");
    sb.append(id);
    sb.append("', '");
    sb.append(actionUrl);
    sb.append("', ");
    AjaxOptions.appendToBuffer(options, sb, context);
    sb.append(")");
    String onClick = (String)valueForBinding("onClick", context.component());
    if(onClick != null) {
        sb.append(";");
        sb.append(onClick);
    }
    return sb.toString();
  }

  protected NSDictionary createAjaxOptions(WOComponent component) {
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onException", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("insertion", AjaxOption.STRING));
    ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component);

    options.setObjectForKey("'get'", "method");
    String updateContainerID = (String) valueForBinding("updateContainerID", component);
    if (updateContainerID != null) {
      options.setObjectForKey("'invokeWOElementID=' + $(" + updateContainerID + ").getAttribute('woElementID')", "parameters");
    }

    return options;
  }
  
  public void appendToResponse(WOResponse response, WOContext context) {
      WOComponent component = context.component();

      response.appendContentString("<a ");
      appendTagAttributeToResponse(response, "href", "javascript:void(0)");
      appendTagAttributeToResponse(response, "title", valueForBinding("title", component ));
      appendTagAttributeToResponse(response, "value", valueForBinding("value", component ));
      appendTagAttributeToResponse(response, "class", valueForBinding("class", component ));
      appendTagAttributeToResponse(response, "style", valueForBinding("style", component ));
      appendTagAttributeToResponse(response, "id", valueForBinding("id", component ));
      appendTagAttributeToResponse(response, "onClick", onClick(context));
      response.appendContentString(">");
      appendChildrenToResponse(response, context);
      response.appendContentString("</a>");
      super.appendToResponse(response, context);
  }

  protected void addRequiredWebResources(WOResponse res, WOContext context) {
      addScriptResourceInHead(context, res, "prototype.js");
      addScriptResourceInHead(context, res, "scriptaculous.js");
      addScriptResourceInHead(context, res, "effects.js");
      addScriptResourceInHead(context, res, "builder.js");
      addScriptResourceInHead(context, res, "dragdrop.js");
      addScriptResourceInHead(context, res, "controls.js");
  }

  protected WOActionResults handleRequest(WORequest request, WOContext context) {
      WOActionResults results = (WOActionResults) valueForBinding("action", context.component());
      if (results == null) {
          WOResponse response = AjaxUtils.createResponse(context);
          response.setHeader("text/javascript", "content-type");
          response.setContent("alert($('tableBody').getAttribute('woElementID'))");
          // results = response;
      }
      return results;
  }
}