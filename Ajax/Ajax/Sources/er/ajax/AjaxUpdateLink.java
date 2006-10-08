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
 * Updates a region on the screen by creating a request to an action, then returning a script that in turn
 * creates an Ajax.Updater for the area.  If you do not provide an action binding, it will just update the
 * specified area.
 * @binding onComplete JavaScript function to evaluate when the request has finished.
 * @binding onSuccess JavaScript function to evaluate when the request was successful.
 * @binding onFailure JavaScript function to evaluate when the request has failed.
 * @binding onException JavaScript function to evaluate when the request had errors.
 * @binding evalScripts boolean defining if the container update is expected to be a script.
 * @binding insertion JavaScript function to evaluate when the update takes place.
 * @binding ignoreActionResponse boolean defining if the action's response should be thrown away 
 *                  (useful when the same action has both Ajax and plain links)
 * @binding updateContainerID the id of the AjaxUpdateContainer to update after performing this action 
 * @binding replaceID the ID of the div (or other html element) whose contents will be replaced with the results of this action
 * @binding title title of the link
 * @binding style css style of the link
 * @binding class css class of the link
 * @binding id id of the link
 * @binding disabled boolean defining if the link renders the tag
 * @binding string string to get preprended to the contained elements
 * @binding function a custom function to call that takes a single parameter that is the action url
 */
public class AjaxUpdateLink extends AjaxDynamicElement {

  public AjaxUpdateLink(String name, NSDictionary associations, WOElement children) {
    super(name, associations, children);
  }

  public String onClick(WOContext context) {
    NSDictionary options = createAjaxOptions(context.component());
    StringBuffer sb = new StringBuffer();
    if (associations().valueForKey("action") == null) {
      String updateContainerID = (String) valueForBinding("updateContainerID", context.component());
      sb.append("new Ajax.Updater('" + updateContainerID + "', $('" + updateContainerID + "').getAttribute('updateUrl'), ");
      AjaxOptions.appendToBuffer(options, sb, context);
      sb.append(")");
    }
    else {
      String actionUrl = context.componentActionURL();
      if (associations().valueForKey("function") == null) {
        String replaceID = (String) valueForBinding("replaceID", context.component());
        if (replaceID == null) {
          sb.append("new Ajax.Request('");
          sb.append(actionUrl);
          sb.append("', ");
          AjaxOptions.appendToBuffer(options, sb, context);
          sb.append(")");
        }
        else {
          sb.append("new Ajax.Updater('" + replaceID + "', '" + actionUrl + "', ");
          AjaxOptions.appendToBuffer(options, sb, context);
          sb.append(")");
        }
      }
      else {
        String function = (String) valueForBinding("function", context.component());
        sb.append("return " + function + "('" + actionUrl + "')");
      }
    }
    String onClick = (String) valueForBinding("onClick", context.component());
    if (onClick != null) {
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
    ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());

    options.setObjectForKey("'get'", "method");

    return options;
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    boolean disabled = booleanValueForBinding("disabled", false, component);
    Object stringValue = valueForBinding("string", component);
    if (!disabled) {
      response.appendContentString("<a ");
      appendTagAttributeToResponse(response, "href", "#");
      appendTagAttributeToResponse(response, "onclick", onClick(context));
      appendTagAttributeToResponse(response, "title", valueForBinding("title", component));
      appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
      appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
      appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
      appendTagAttributeToResponse(response, "id", valueForBinding("id", component));
      // appendTagAttributeToResponse(response, "onclick", onClick(context));
      response.appendContentString(">");
    }
    if (stringValue != null) {
      response.appendContentHTMLString(stringValue.toString());
    }
    appendChildrenToResponse(response, context);
    if (!disabled) {
      response.appendContentString("</a>");
    }
    super.appendToResponse(response, context);
  }

  protected void addRequiredWebResources(WOResponse res, WOContext context) {
    addScriptResourceInHead(context, res, "prototype.js");
    addScriptResourceInHead(context, res, "scriptaculous.js");
  }

  protected WOActionResults handleRequest(WORequest request, WOContext context) {
    WOComponent component = context.component();
    WOActionResults results = (WOActionResults) valueForBinding("action", component);
    if (results == null || booleanValueForBinding("ignoreActionResponse", false, component)) {
      String updateContainerID = (String) valueForBinding("updateContainerID", component);
      if (updateContainerID != null) {
        WOResponse response = AjaxUtils.createResponse(context);
        response.setHeader("text/javascript", "content-type");
        response.setContent("new Ajax.Updater('" + updateContainerID + "', $('" + updateContainerID + "').getAttribute('updateUrl'), {" + " evalScripts: " + valueForBinding("evalScripts", "false", context.component()) + ", " + " insertion: " + valueForBinding("insertion", "function(receiver, response) {Element.update(receiver, response);}", context.component()) + " " + "})");
        results = response;
        if (log.isDebugEnabled()) {
          log.debug("Response: " + response.contentString());
        }
      }
    }
    return results;
  }
}