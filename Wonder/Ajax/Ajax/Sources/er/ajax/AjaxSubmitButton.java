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

import er.extensions.ERXProperties;
import er.extensions.ERXWOForm;

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
 * @binding onClickBefore if the given function returns true, the onClick is executed.  This is to support confirm(..) dialogs. 
 * @binding onServerClick if the action defined in the action binding returns null, the value of this binding will be returned as javascript from the server
 * @binding onComplete JavaScript function to evaluate when the request has finished.
 * @binding onSuccess javascript to execute in response to the Ajax onSuccess event
 * @binding onFailure javascript to execute in response to the Ajax onFailure event
 * @binding onLoading javascript to execute when loading
 * @binding insertion JavaScript function to evaluate when the update takes place.
 * @binding evalScripts evaluate scripts on the result
 * @binding button if false, it will display a link
 * @binding formName if button is false, you must specify the name of the form to submit
 * @binding functionName if set, the link becomes a javascript function instead
 * @binding updateContainerID the id of the AjaxUpdateContainer to update after performing this action
 * @binding showUI if functionName is set, the UI defaults to hidden; showUI re-enables it
 * @binding formSerializer the name of the javascript function to call to serialize the form
 * @binding effect synonym of afterEffect except it always applies to updateContainerID
 * @binding beforeEffect the Scriptaculous effect to apply onSuccess ("highlight", "slideIn", "blindDown", etc);
 * @binding beforeEffectID the ID of the container to apply the "before" effect to (blank = try nearest container, then try updateContainerID)
 * @binding beforeEffectDuration the duration of the effect to apply before
 * @binding afterEffect the Scriptaculous effect to apply onSuccess ("highlight", "slideIn", "blindDown", etc);
 * @binding afterEffectID the ID of the container to apply the "after" effect to (blank = try nearest container, then try updateContainerID)
 * @property er.ajax.formSerializer the default form serializer to use for all ajax submits
 * 
 * @author anjo
 */
public class AjaxSubmitButton extends AjaxDynamicElement {
	// MS: If you change this value, make sure to change it in ERXSession.saveSession
  public static final String KEY_AJAX_SUBMIT_BUTTON_NAME = "AJAX_SUBMIT_BUTTON_NAME";

  public AjaxSubmitButton(String name, NSDictionary associations, WOElement children) {
    super(name, associations, children);
  }

  public static boolean isAjaxSubmit(WORequest request) {
	  return request.valueForKey(KEY_AJAX_SUBMIT_BUTTON_NAME) != null;
  }
  
  public boolean disabledInComponent(WOComponent component) {
    return booleanValueForBinding("disabled", false, component);
  }

  public String nameInContext(WOContext context, WOComponent component) {
    return (String) valueForBinding("name", context.elementID(), component);
  }

  public NSMutableDictionary createAjaxOptions(WOComponent component, String formReference) {
    String name = nameInContext(component.context(), component);
    NSMutableArray ajaxOptionsArray = new NSMutableArray();
    ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("onLoading", AjaxOption.SCRIPT));
    ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
	ajaxOptionsArray.addObject(new AjaxOption("insertion", AjaxOption.SCRIPT));
    NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
    StringBuffer parametersBuffer = new StringBuffer();
    String formSerializer = (String) valueForBinding("formSerializer", ERXProperties.stringForKeyWithDefault("er.ajax.formSerializer", "Form.serializeWithoutSubmits"), component);
    parametersBuffer.append(formSerializer);
    parametersBuffer.append("(" + formReference + ")");
    parametersBuffer.append(" + '");
    parametersBuffer.append("&" + AjaxSubmitButton.KEY_AJAX_SUBMIT_BUTTON_NAME + "=" + name);
    parametersBuffer.append("'");
    options.setObjectForKey(parametersBuffer.toString(), "parameters");
	if (options.objectForKey("evalScripts") == null) {
		options.setObjectForKey("true", "evalScripts");
	}
    return options;
  }

  public void appendToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();

    String functionName = (String)valueForBinding("functionName", null, component);
    String formName = (String)valueForBinding("formName", component);
    boolean showUI = (functionName == null || booleanValueForBinding("showUI", false, component));
    boolean showButton = showUI && booleanValueForBinding("button", true, component);
    String formReference;
    if ((!showButton || functionName != null) && formName == null) {
      formName = ERXWOForm.formName(context, null);
      if (formName == null) {
        throw new WODynamicElementCreationException("If button = false or functionName is not null, the containing form must have an explicit name.");
      }
    }
    if (formName == null) {
      formReference = "this.form";
    }
    else {
      formReference = "document." + formName;
    }
    
    StringBuffer onClickBuffer = new StringBuffer();

	String onClickBefore = (String)valueForBinding("onClickBefore", component);
	if (onClickBefore != null) {
		onClickBuffer.append("if (");
		onClickBuffer.append(onClickBefore);
		onClickBuffer.append(") {");
	}
	
	String updateContainerID = (String)valueForBinding("updateContainerID", component);
	
	String beforeEffect = (String) valueForBinding("beforeEffect", component);

	if (beforeEffect != null) {
		onClickBuffer.append("new ");
		onClickBuffer.append(AjaxUpdateLink.fullEffectName(beforeEffect));
		onClickBuffer.append("('");
		
		String beforeEffectID = (String) valueForBinding("beforeEffectID", component);
		if (beforeEffectID == null) {
			beforeEffectID = AjaxUpdateContainer.currentUpdateContainerID();
			if (beforeEffectID == null) {
				beforeEffectID = updateContainerID;
			}
		}
		onClickBuffer.append(beforeEffectID);
		
		onClickBuffer.append("', { ");
		
		String beforeEffectDuration = (String) valueForBinding("beforeEffectDuration", component);
		if (beforeEffectDuration != null) {
			onClickBuffer.append("duration: ");
			onClickBuffer.append(beforeEffectDuration);
			onClickBuffer.append(", ");
		}
		
		onClickBuffer.append("queue:'end', afterFinish: function() {");
	}

	if (updateContainerID != null) {
		onClickBuffer.append("new Ajax.Updater('" + updateContainerID + "',");
	}
	else {
		onClickBuffer.append("new Ajax.Request(");
	}
	if (valueForBinding("functionName", component) != null) {
		onClickBuffer.append(formReference + ".action.addQueryParameters(additionalParams)");
	}
	else {
		onClickBuffer.append(formReference + ".action");
	}
	onClickBuffer.append(",");
	
    NSMutableDictionary options = createAjaxOptions(component, formReference);
	
	AjaxUpdateLink.addEffect(options, (String) valueForBinding("effect", component), updateContainerID);
	String afterEffectID = (String) valueForBinding("afterEffectID", component);
	if (afterEffectID == null) {
		afterEffectID = AjaxUpdateContainer.currentUpdateContainerID();
		if (afterEffectID == null) {
			afterEffectID = updateContainerID;
		}
	}
	AjaxUpdateLink.addEffect(options, (String) valueForBinding("afterEffect", component), afterEffectID);
	
    AjaxOptions.appendToBuffer(options, onClickBuffer, context);
    onClickBuffer.append(")");
    String onClick = (String) valueForBinding("onClick", component);
    if (onClick != null) {
      onClickBuffer.append(";");
      onClickBuffer.append(onClick);
    }
	
	if (beforeEffect != null) {
		onClickBuffer.append("}});");
	}

    if (onClickBefore != null) {
    	onClickBuffer.append("}");
    }

    
    if (functionName != null) {
      	AjaxUtils.appendScriptHeader(response);
    	response.appendContentString(functionName + " = function(additionalParams) { " + onClickBuffer + " }\n");
    	AjaxUtils.appendScriptFooter(response);
    }
    if (showUI) {
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
    	if (functionName == null) {
    		appendTagAttributeToResponse(response, "onclick", onClickBuffer.toString());
    	}
    	else {
    		appendTagAttributeToResponse(response, "onclick", functionName + "()");
    	}
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
    }
    super.appendToResponse(response, context);
  }

  protected void addRequiredWebResources(WOResponse res, WOContext context) {
    addScriptResourceInHead(context, res, "prototype.js");
    addScriptResourceInHead(context, res, "scriptaculous.js");
    addScriptResourceInHead(context, res, "wonder.js");
  }

  public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
    WOActionResults result = null;
    WOComponent wocomponent = wocontext.component();

    String nameInContext = nameInContext(wocontext, wocomponent);
    boolean shouldHandleRequest = (!disabledInComponent(wocomponent) && wocontext._wasFormSubmitted()) && ((wocontext._isMultipleSubmitForm() && nameInContext.equals(worequest.formValueForKey(KEY_AJAX_SUBMIT_BUTTON_NAME))) || !wocontext._isMultipleSubmitForm());
    if (shouldHandleRequest) {
      AjaxUpdateContainer.setUpdateContainerID(worequest, (String) valueForBinding("updateContainerID", wocomponent));
      wocontext._setActionInvoked(true);
      result = handleRequest(worequest, wocontext);
      AjaxUtils.updateMutableUserInfoWithAjaxInfo(wocontext);
    }
    
    return result;
  }

  public WOActionResults handleRequest(WORequest worequest, WOContext wocontext) {
    WOResponse response = AjaxUtils.createResponse(worequest, wocontext);
    WOComponent wocomponent = wocontext.component();
    Object obj = valueForBinding("action", wocomponent);
    String onClickServer = (String) valueForBinding("onClickServer", wocomponent);
    if (onClickServer != null) {
		AjaxUtils.appendScriptHeaderIfNecessary(worequest, response);
		response.appendContentString(onClickServer);
		AjaxUtils.appendScriptFooterIfNecessary(worequest, response);
    }
    return response;
  }

}
