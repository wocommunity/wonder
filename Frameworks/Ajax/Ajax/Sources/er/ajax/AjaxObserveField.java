package er.ajax;

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

import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ajax.ERXAjaxApplication;

/**
 * <p>
 * AjaxObserveField allows you to perform an Ajax submit (and optional update) based
 * on the state of a form field changing. If you specify an observeFieldID, that
 * single field will be observed for changes. If you also specify an updateContainerID,
 * the given container will be refreshed after the field changes. If you do NOT specify
 * an observeFieldID, all of the form fields contained within this component will be
 * observed for changes instead. The list of form fields to observe is obtained on 
 * the client side, so you should not put AjaxUpdateContainers INSIDE of this component
 * or none fields inside of the container will be observed after an update. Instead,
 * AjaxObserveFields should be surrounded by an update container.
 * </p>
 * 
 * <p>
 * If you set an observeFieldID you must place the AjaxObserveField tag after the field you would 
 * like to observe.
 * </p>
 * 
 * <p>
 * If you omit the observeFieldID, AjaxObserveField must generate an HTML container, so
 * that it can find the form fields that correspond to this component from the client
 * side, so you will get an extra DOM element when used as a container.
 * </p>
 * 
 * <p>
 * AjaxObserveFields observe specific instances of DOM elements. If you Ajax replace the
 * DOM elements being watched, the observe field will cease to function. To prevent this
 * problem, you should always ensure that any Ajax update of an observed field also 
 * updates the AjaxObserveField component as well. The rule of thumb is that all 
 * AjaxObserveFields should be in the same AjaxUpdateContainer as the fields they observe. 
 * </p>
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
 * @binding fullSubmit When <code>false</code>, only the value of the field that changed is sent to the server
 * 			(partial submit), when <code>true</code>, the whole form is sent. Defaults to <code>false</code>. 
 * 			Caution: Partial submit doesn't work correctly if you manually set the name on your inputs.
 * @binding class CSS class to use on the container. (Only used if you leave off observeFieldID)
 * @binding style CSS style to use on the container. (Only used if you leave off observeFieldID)
 * @binding onCreate Takes a JavaScript function which is called after the form has been serialized, 
 * 			but before the Ajax request is sent to the server. Useful e.g. if you want to disable the 
 * 			form while the Ajax request is running. 
 */
public class AjaxObserveField extends AjaxDynamicElement {
	public AjaxObserveField(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		addScriptResourceInHead(context, response, "prototype.js");
		addScriptResourceInHead(context, response, "wonder.js");
	}

	public NSMutableDictionary<String, String> createAjaxOptions(WOComponent component) {
		// PROTOTYPE OPTIONS
		NSMutableArray<AjaxOption> ajaxOptionsArray = new NSMutableArray<AjaxOption>();
		ajaxOptionsArray.addObject(new AjaxOption("observeFieldFrequency", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("observeDelay", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("onCreate", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onLoading", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onBeforeSubmit", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onException", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("insertion", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));

		NSMutableDictionary<String, String> options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		return options;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		
		WOComponent component = context.component();
		String observeFieldID = stringValueForBinding("observeFieldID", component);
		String updateContainerID = AjaxUpdateContainer.updateContainerID(this, component); 
		NSMutableDictionary<String, String> options = createAjaxOptions(component);
		boolean fullSubmit = booleanValueForBinding("fullSubmit", false, component);
		boolean observeFieldDescendents;
		if (observeFieldID != null) {
			observeFieldDescendents = false;
		}
		else {
			observeFieldDescendents = true;
			observeFieldID = stringValueForBinding("id", component);
			if (observeFieldID == null) {
				observeFieldID = ERXWOContext.safeIdentifierName(context, false);
			}
			String elementName = stringValueForBinding("elementName", "div", component);
			response.appendContentString("<" + elementName);
			appendTagAttributeToResponse(response, "id", observeFieldID);
			appendTagAttributeToResponse(response, "class", stringValueForBinding("class", component));
			appendTagAttributeToResponse(response, "style", stringValueForBinding("style", component));
			response.appendContentString(">");
			
			if (hasChildrenElements()) {
				appendChildrenToResponse(response, context);
			}
			response.appendContentString("</" + elementName + ">");
		}
		AjaxUtils.appendScriptHeader(response);
		AjaxObserveField.appendToResponse(response, context, this, observeFieldID, observeFieldDescendents, updateContainerID, fullSubmit, options);
		AjaxUtils.appendScriptFooter(response);
	}

	public static void appendToResponse(WOResponse response, WOContext context, AjaxDynamicElement element, String observeFieldID, boolean observeDescendentFields, String updateContainerID, boolean fullSubmit, NSMutableDictionary<String, String> options) {
		WOComponent component = context.component();
		String submitButtonName = nameInContext(context, component, element);
		NSMutableDictionary<String, String> observerOptions = new NSMutableDictionary<String, String>();
		if (options != null) {
			observerOptions.addEntriesFromDictionary(options);
		}
		AjaxSubmitButton.fillInAjaxOptions(element, component, submitButtonName, observerOptions);

		String observeFieldFrequency = observerOptions.removeObjectForKey("observeFieldFrequency");
		if (observeDescendentFields) {
			response.appendContentString("ASB.observeDescendentFields");
		} else {
			response.appendContentString("ASB.observeField");
		}

		String observeDelay = observerOptions.removeObjectForKey("observeDelay");
		response.appendContentString("(");
		response.appendContentString(AjaxUtils.quote(updateContainerID));
		response.appendContentString(", ");
		response.appendContentString(AjaxUtils.quote(observeFieldID));
		response.appendContentString(", ");
		response.appendContentString(observeFieldFrequency);
		response.appendContentString(", ");
		response.appendContentString(String.valueOf(!fullSubmit));
		response.appendContentString(", ");
		response.appendContentString(observeDelay);
		response.appendContentString(", ");
		AjaxOptions.appendToResponse(observerOptions, response, context);
		response.appendContentString(");");
	}

	public static String nameInContext(WOContext context, WOComponent component, AjaxDynamicElement element) {
		return element.stringValueForBinding("name", context.elementID(), component);
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result = null;
		WOComponent component = context.component();
		String nameInContext = nameInContext(context, component, this);
		boolean shouldHandleRequest = !context.wasActionInvoked() && context.wasFormSubmitted() && nameInContext.equals(ERXAjaxApplication.ajaxSubmitButtonName(request));
		if (shouldHandleRequest) {
			String updateContainerID = AjaxUpdateContainer.updateContainerID(this, component);
			AjaxUpdateContainer.setUpdateContainerID(request, updateContainerID);
			context.setActionInvoked(true);
			result = (WOActionResults)valueForBinding("action", component);
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
