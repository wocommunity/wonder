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
/**
 * Ajax enabled Hyperlink. Calls an action on the server, then executes methods on the client.
 * 
 * @binding onSuccess JS function, called on a 2xx response on the client
 * @binding onFailure JS function, called on a non-200 response on the client
 * @binding onClick JS function, called after the click on the client
 * @binding action method to call
 * @binding title title of the link
 * @binding id id of the link
 * @binding class class of the link
 * @binding style style of the link
 * @author ak
 */
public class AjaxHyperlink extends AjaxDynamicElement {

  public AjaxHyperlink(String name, NSDictionary associations, WOElement children) {
      super(name, associations, children);
  }

  public String onClick(WOContext context) {
    NSDictionary options = createAjaxOptions(context.component());
    String actionUrl = context.componentActionURL();
    StringBuffer sb = new StringBuffer();
    sb.append("new Ajax.Request('");
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
    ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
    options.setObjectForKey("'get'", "method");
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
      appendTagAttributeToResponse(response, "onclick", onClick(context));
      response.appendContentString(">");
      appendChildrenToResponse(response, context);
      response.appendContentString("</a>");
      super.appendToResponse(response, context);
  }

  protected void addRequiredWebResources(WOResponse res, WOContext context) {
      addScriptResourceInHead(context, res, "prototype.js");
      addScriptResourceInHead(context, res, "scriptaculous.js");
  }

  protected WOActionResults handleRequest(WORequest request, WOContext context) {
      WOComponent component = context.component();
      WOActionResults results = (WOActionResults) valueForBinding("action", component);
      if (results == null) {
          String script = (String) valueForBinding("onClickServer", component);
          if(script != null) {
              WOResponse response = AjaxUtils.createResponse(context);
              response.setHeader("text/javascript", "content-type");
              response.setContent(script);
              results = response;
          }
      }
      return results;
  }
}