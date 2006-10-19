package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * AjaxSubmitButton behaves just like a WOSubmitButton except that it submits in the background with an Ajax.Request.
 * 
 * @binding name the HTML name of this submit button (optional)
 * @binding value the HTML value of this submit button (optional)
 * @binding action the action to execute when this button is pressed
 * @binding id the HTML ID of this submit button
 * @binding class the HTML class of this submit button
 * @binding style the HTML style of this submit button
 * @binding onClick arbitrary Javascript to execute when the client clicks the button
 * @binding onServerClick if the action defined in the action binding returns null, the value of this binding will be returned as javascript from the server
 * @binding onSuccess javascript to execute in response to the Ajax onSuccess event
 * @binding onFailure javascript to execute in response to the Ajax onFailure event
 * @binding button if false, it will display a link
 * @binding formName if button is false, you must specify the name of the form to submit
 * 
 * @author anjo
 */
public class AjaxSubmitButton extends AjaxDynamicElement {

  private static final String KEY_AJAX_SUBMIT_BUTTON_NAME = "AJAX_SUBMIT_BUTTON_NAME";

  public AjaxSubmitButton(String name, NSDictionary associations, WOElement children) {
    super(name, associations, children);
  }

  public boolean disabledInComponent(WOComponent component) {
    return booleanValueForBinding("disabled", false, component);
  }

  public String nameInContext(WOContext context, WOComponent component) {
    return (String) valueForBinding("name", context.elementID(), component);
  }

  public NSDictionary createAjaxOptions(WOComponent component, String formReference) {
    String name = nameInContext(component.context(), component);
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
    options.setObjectForKey("Form.serialize(" + formReference + ") + \"&" + KEY_AJAX_SUBMIT_BUTTON_NAME + "=" + name + "\"", "parameters");
    return options;
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();

    boolean showButton = booleanValueForBinding("button", true, component);
    String formName = (String)valueForBinding("formName", component);
    String formReference;
    if (!showButton && formName == null) {
      throw new WODynamicElementCreationException("If button = false, you must specify a formName.");
    }
    else if (formName == null) {
      formReference = "this.form";
    }
    else {
      formReference = "document." + formName;
    }
    if (showButton) {
      response.appendContentString("<input ");
      appendTagAttributeToResponse(response, "type", "button");
      String name = nameInContext(context, component);
      appendTagAttributeToResponse(response, "name", name);
      appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
    }
    else {
      response.appendContentString("<a href = \"javascript:void(0)\" ");
    }
    appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
    appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
    appendTagAttributeToResponse(response, "id", valueForBinding("id", component));
    if (disabledInComponent(component)) {
      appendTagAttributeToResponse(response, "disabled", "disabled");
    }
    StringBuffer sb = new StringBuffer();
    sb.append("new Ajax.Request(" + formReference + ".action,");
    NSDictionary options = createAjaxOptions(component, formReference);
    AjaxOptions.appendToBuffer(options, sb, context);
    sb.append(")");
    String onClick = (String) valueForBinding("onClick", component);
    if (onClick != null) {
      sb.append(";");
      sb.append(onClick);
    }
    appendTagAttributeToResponse(response, "onclick", sb.toString());
    if (showButton) {
      response.appendContentString(" />");
    }
    else {
      response.appendContentString(">");
      if (hasChildrenElements()) {
        appendChildrenToResponse(response, context);
      }
      response.appendContentString("</a>");
    }
    super.appendToResponse(response, context);
  }

  protected void addRequiredWebResources(WOResponse res, WOContext context) {
    addScriptResourceInHead(context, res, "prototype.js");
    addScriptResourceInHead(context, res, "scriptaculous.js");
    addScriptResourceInHead(context, res, "effects.js");
    addScriptResourceInHead(context, res, "builder.js");
    addScriptResourceInHead(context, res, "controls.js");
  }

  public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
    WOActionResults result = null;
    WOComponent wocomponent = wocontext.component();

    String nameInContext = nameInContext(wocontext, wocomponent);
    boolean shouldHandleRequest = (!disabledInComponent(wocomponent) && wocontext._wasFormSubmitted()) && ((wocontext._isMultipleSubmitForm() && nameInContext.equals(worequest.formValueForKey(KEY_AJAX_SUBMIT_BUTTON_NAME))) || !wocontext._isMultipleSubmitForm());

    //System.out.println("shouldHandleRequest("+nameInContext+")="+shouldHandleRequest);

    if (shouldHandleRequest) {
      wocontext._setActionInvoked(true);
      result = handleRequest(worequest, wocontext);
      AjaxUtils.updateMutableUserInfoWithAjaxInfo(wocontext);
    }

    return result;
  }

  protected WOActionResults handleRequest(WORequest worequest, WOContext wocontext) {
    WOResponse result = AjaxUtils.createResponse(wocontext);
    WOComponent wocomponent = wocontext.component();
    Object obj = valueForBinding("action", wocomponent);
    if (obj == null)
      obj = wocontext.page();
    String onClickServer = (String) valueForBinding("onClickServer", wocomponent);
    if (onClickServer != null) {
      result.setHeader("text/javascript", "content-type");
      result.setContent(onClickServer);
    }

    return result;
  }

}
