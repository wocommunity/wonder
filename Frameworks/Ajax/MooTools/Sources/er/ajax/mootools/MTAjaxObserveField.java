package er.ajax.mootools;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxOption;
import er.ajax.AjaxOptions;
import er.ajax.AjaxSubmitButton;
import er.ajax.AjaxUpdateContainer;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ajax.ERXAjaxApplication;

/**
 * AjaxObserveField allows you to perform an Ajax submit (and optional update) based
 * on the state of a form field changing. If you specify an observeFieldID, that
 * single field will be observed for changes. If you also specify an updateContainerID,
 * the given container will be refreshed after the field changes. If you do NOT specify
 * an observeFieldID, all of the form fields contained within this component will be
 * observed for changes instead. The list of form fields to observe is obtained on 
 * the client side, so you should not put AjaxUpdateContainers INSIDE of this component
 * or any fields inside of the container will no be observed after an update. Instead,
 * AjaxObserveFields should be surrounded by a container.
 * 
 * If you leave of observeFieldID, AjaxObserveField must generate an HTML container, so
 * that it can find the form fields that correspond to this component from the client
 * side.
 * 
 * @binding id the ID of the observe field container (only useful if you leave off observeFieldID).
 * @binding elementName element to use for the observe field container. Defaults to <code>div</code>. 
 * 			(Only used if you leave off observeFieldID)
 * @binding observeFieldID the ID of the field to observe
 * @binding updateContainerID the ID of the container to update. Specify "_parent" to use the nearest one.
 * @binding action the action to call when the observer fires
 * @binding onBeforeSubmit called prior to submitting the observed content; return false to deny the submit
 * @binding observeFieldFrequency the polling observe frequency (in seconds)
 * @binding observeDelay the minimum time between submits (in seconds)
 * @binding fullSubmit When false, only the value of the field that changed is sent to the server (partial submit), 
 * 			when true, the whole form is sent. Defaults to false. 
 * 			Caution: Partial submit doesn't work correctly if you manually set the name on your inputs.
 * @binding class CSS class to use on the container. (Only used if you leave off observeFieldID)
 * @binding style CSS style to use on the container. (Only used if you leave off observeFieldID)
 * @binding onCreate Takes a JavaScript function which is called after the form has been serialized, 
 * 			but befor the Ajax request is sent to the server. Useful e.g. if you want to disable the 
 * 			form while the ajax request is running. 
 */	

public class MTAjaxObserveField extends AjaxDynamicElement {
	
    public MTAjaxObserveField(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

    
	
	@SuppressWarnings({"rawtypes","unchecked"})
	public NSMutableDictionary createAjaxOptions(WOComponent component) {
	
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("observeFieldFrequency", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("observeDelay", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("onCancel", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onException", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onRequest", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AjaxOption("async", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("useSpinner", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerTarget", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerOptions", AjaxOption.DICTIONARY));

		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		return options;

	}

	@Override
	@SuppressWarnings("rawtypes")
	public void appendToResponse(WOResponse response, WOContext context) {

		super.appendToResponse(response, context);
		
		WOComponent component = context.component();
		String observeFieldID = (String) valueForBinding("observeFieldID", component);
		String updateContainerID = MTAjaxUpdateContainer.updateContainerID(this, component); 		
		NSMutableDictionary options = createAjaxOptions(component);
		boolean fullSubmit = booleanValueForBinding("fullSubmit", false, component);
		boolean observeFieldDescendents;
		
		if (observeFieldID != null) {
			observeFieldDescendents = false;
		}
		else {
			observeFieldDescendents = true;
			observeFieldID = (String)valueForBinding("id", component);
			if (observeFieldID == null) {
				observeFieldID = ERXWOContext.safeIdentifierName(context, false);
			}
			String elementName = (String)valueForBinding("elementName", component);
			if (elementName == null) {
				elementName = "div";
			}
			response.appendContentString("<" + elementName + " id = \"" + observeFieldID + "\"");

			String className = stringValueForBinding("class", component);
			if (className != null && className.length() > 0) {
				response.appendContentString(" class=\"" + className + "\"");
			}

			String style = stringValueForBinding("style", component);
			if (style != null && style.length() > 0) {
				response.appendContentString(" style=\"" + style + "\"");
			}
			
			response.appendContentString(">");
			if (hasChildrenElements()) {
				appendChildrenToResponse(response, context);
			}
			response.appendContentString("</" + elementName + ">");
		}
		AjaxUtils.appendScriptHeader(response);
		MTAjaxObserveField.appendToResponse(response, context, this, observeFieldID, observeFieldDescendents, updateContainerID, fullSubmit, options);
		AjaxUtils.appendScriptFooter(response);
	}

	public static void appendToResponse(WOResponse response, WOContext context, AjaxDynamicElement element, String observeFieldID, boolean observeDescendentFields, String updateContainerID, boolean fullSubmit, NSMutableDictionary options) {
		WOComponent component = context.component();
		String submitButtonName = nameInContext(context, component, element);
		NSMutableDictionary observerOptions = new NSMutableDictionary();
		if (options != null) {
			observerOptions.addEntriesFromDictionary(options);
		}
		AjaxSubmitButton.fillInAjaxOptions(element, component, submitButtonName, observerOptions);

		Object observeFieldFrequency = observerOptions.removeObjectForKey("observeFieldFrequency");
		if (observeDescendentFields) {
			response.appendContentString("MTASB.observeDescendentFields");
		}
		else {
			response.appendContentString("MTASB.observeField");
		}

		Object observeDelay = observerOptions.removeObjectForKey("observeDelay");		
		response.appendContentString("(" + AjaxUtils.quote(updateContainerID) + ", " + AjaxUtils.quote(observeFieldID) + ", " + observeFieldFrequency + ", " + (!fullSubmit) + ", " + observeDelay + ", ");
		AjaxOptions.appendToResponse(observerOptions, response, context);
		response.appendContentString(");");
	}
	
    
	@Override
	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
		Boolean useSpinner = (Boolean)valueForBinding("useSpinner", Boolean.FALSE, context.component());
		if(useSpinner) {
			Boolean useDefaultSpinnerClass = (Boolean)valueForBinding("defaultSpinnerClass", Boolean.TRUE, context.component());
			if(useDefaultSpinnerClass) {
				AjaxUtils.addStylesheetResourceInHead(context, context.response(), "MooTools", "scripts/plugins/spinner/spinner.css");
			}
		}
		
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_WONDER_JS);
	}
	
	public static String nameInContext(WOContext context, WOComponent component, AjaxDynamicElement element) {
		return (String) element.valueForBinding("name", context.elementID(), component);
	}
	
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result = null;
		WOComponent wocomponent = context.component();
		String nameInContext = nameInContext(context, wocomponent, this);
		boolean shouldHandleRequest = !context.wasActionInvoked() && context.wasFormSubmitted() && nameInContext.equals(ERXAjaxApplication.ajaxSubmitButtonName(request));
		if (shouldHandleRequest) {
			String updateContainerID = MTAjaxUpdateContainer.updateContainerID(this, wocomponent);
			AjaxUpdateContainer.setUpdateContainerID(request, updateContainerID);
			context.setActionInvoked(true);
			result = (WOActionResults)valueForBinding("action", wocomponent);
			if (result == null) {
				result = handleRequest(request, context);
			}
			ERXAjaxApplication.enableShouldNotStorePage();
		} else {
			result = invokeChildrenAction(request, context);
		}
		return result;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
	    WOResponse response = AjaxUtils.createResponse(request, context);
		return response;
	}	
}