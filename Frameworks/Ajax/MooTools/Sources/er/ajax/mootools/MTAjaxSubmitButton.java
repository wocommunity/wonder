package er.ajax.mootools;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxOption;
import er.ajax.AjaxOptions;
import er.ajax.AjaxUpdateContainer;
import er.ajax.AjaxUtils;
import er.ajax.IAjaxElement;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components._private.ERXWOForm;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 * AjaxSubmitButton behaves just like a WOSubmitButton except that it submits in the background with an Ajax.Request.
 * 
 * @binding name the HTML name of this submit button (optional)
 * @binding value the HTML value of this submit button (optional)
 * @binding action the action to execute when this button is pressed
 * @binding id the HTML ID of this submit button
 * @binding class the HTML class of this submit button
 * @binding style the HTML style of this submit button
 * @binding title the HTML title of this submit button
 * @binding onClick arbitrary Javascript to execute when the client clicks the button
 * @binding onClickBefore if the given function returns true, the onClick is executed.  This is to support confirm(..) dialogs. 
 * @binding onClickServer if the action defined in the action binding returns null, the value of this binding will be returned as javascript from the server
 * @binding async boolean defining if the update request is sent asynchronously or synchronously, defaults to true
 * @binding accesskey hot key that should trigger the link (optional)
 * @binding onCancel Fired when a request has been cancelled.
 * @binding onClickBefore if the given function returns true, the onClick is executed. This is to support confirm(..) dialogs.
 * @binding onClick JS function, called after the click on the client
 * @binding onComplete Fired when the Request is completed.
 * @binding onException Fired when setting a request header fails.
 * @binding onFailure Fired when the request failed (error status code).
 * @binding onRequest Fired when the Request is sent.
 * @binding onSuccess(responseTree, responseElements, responseHTML, responseJavaScript) Fired when the Request is completed successfully.
 * @binding evalScripts evaluate scripts on the result
 * @binding button if false, it will display a link
 * @binding formName if button is false, you must specify the name of the form to submit
 * @binding functionName if set, the link becomes a javascript function instead
 * @binding updateContainerID the id of the AjaxUpdateContainer to update after performing this action
 * @binding showUI if functionName is set, the UI defaults to hidden; showUI re-enables it
 * @binding formSerializer the name of the javascript function to call to serialize the form
 * @binding elementName the element name to use (defaults to "a")
 * @binding async boolean defining if the request is sent asynchronously or synchronously, defaults to true
 * @binding accesskey hot key that should trigger the button (optional)
 * @binding disabled if true, the button will be disabled (defaults to false)
 * @property er.ajax.formSerializer the default form serializer to use for all ajax submits
 */


public class MTAjaxSubmitButton extends AjaxDynamicElement {

	// MS: If you change this value, make sure to change it in ERXAjaxApplication
	public static final String KEY_AJAX_SUBMIT_BUTTON_NAME = "AJAX_SUBMIT_BUTTON_NAME";
	// MS: If you change this value, make sure to change it in ERXAjaxApplication and in wonder.js
	public static final String KEY_PARTIAL_FORM_SENDER_ID = "_partialSenderID";

	public MTAjaxSubmitButton(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	public static boolean isAjaxSubmit(WORequest request) {
		return request.formValueForKey(KEY_AJAX_SUBMIT_BUTTON_NAME) != null;
	}

	public boolean disabledInComponent(WOComponent component) {
		return booleanValueForBinding("disabled", false, component);
	}

	public String nameInContext(WOContext context, WOComponent component) {
		return (String) valueForBinding("name", context.elementID(), component);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public NSMutableDictionary createAjaxOptions(WOComponent component) {

		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("onCancel", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onException", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onRequest", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("async", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("useSpinner", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerTarget", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerOptions", AjaxOption.DICTIONARY));

		String name = nameInContext(component.context(), component);
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		MTAjaxSubmitButton.fillInAjaxOptions(this, component, name, options);
		return options;		

	}

	@Override
	public void addRequiredWebResources(WOResponse response, WOContext context) {
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
		Boolean useSpinner = (Boolean)valueForBinding("useSpinner", Boolean.FALSE, context.component());
		if(useSpinner.booleanValue()) {
			Boolean useDefaultSpinnerClass = (Boolean)valueForBinding("defaultSpinnerClass", Boolean.TRUE, context.component());
			if(useDefaultSpinnerClass.booleanValue()) {
				AjaxUtils.addStylesheetResourceInHead(context, context.response(), "MooTools", "scripts/plugins/spinner/spinner.css");
			}
		}

		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_WONDER_JS);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void appendToResponse(WOResponse response, WOContext context) {

		WOComponent component = context.component();
		String functionName = (String)valueForBinding("functionName", null, component);
		String formName = (String)valueForBinding("formName", component);

		boolean showUI = (functionName == null || booleanValueForBinding("showUI", false, component));
		boolean showButton = showUI && booleanValueForBinding("button", true, component);
		String formReference;

		if((!showButton || functionName != null) && formName == null) {
			formName = ERXWOForm.formName(context, null);
			if(formName == null) {
				throw new WODynamicElementCreationException("If button = false or functionName is not null, the containing form must have an explicit name.");
			}
		}

		if(formName == null) {
			formReference = "this.form";
		} else {
			formReference = "document." + formName;
		}

		StringBuffer onClickBuffer = new StringBuffer();

		// JavaScript function to be fired before submit is sent i.e. confirm();
		String onClickBefore = (String)valueForBinding("onClickBefore", component);
		if(onClickBefore != null) {
			onClickBuffer.append("if(")
			.append(onClickBefore)
			.append(") {");
		}

		String updateContainerID = (String)valueForBinding("updateContainerID", component);

		// Needs to be refactored.  Same function as MoAjaxUpdateLink
		// Maybe create a helper class with a static function that takes the component as an argument?
		// Could add it to MoAjaxUpdateLink like addEffect.
		String beforeEffect = (String)valueForBinding("beforeEffect", component);
		if(beforeEffect != null) {

			String beforeEffectID = (String)valueForBinding("beforeEffectID", component);
			String beforeEffectDuration = (String) valueForBinding("beforeEffectDuration", component);
			String beforeEffectProperty = (String) valueForBinding("beforeEffectProperty", component);
			String beforeEffectStart = (String) valueForBinding("beforeEffectStart", component);

			if(beforeEffectID == null) {
				beforeEffectID = AjaxUpdateContainer.currentUpdateContainerID();
				if (beforeEffectID == null) {
					beforeEffectID = updateContainerID;
				}
			}

			if(beforeEffect.equals("tween")) {
				if(beforeEffectDuration != null) {
					onClickBuffer.append("$('").append(beforeEffectID)
					.append("').set('tween', { duration: '")
					.append(beforeEffectDuration).append("', property: '" + beforeEffectProperty + "' });");
				} else {
					onClickBuffer.append("$('").append(beforeEffectID).append("').set('tween', { property: '" + beforeEffectProperty + "' });");
				}
				onClickBuffer.append("$('").append(beforeEffectID).append("').get('tween').start(")
				.append(beforeEffectStart).append(").chain(function() {");
			} else if(beforeEffect.equals("morph")) {
				if(beforeEffectDuration != null) {
					onClickBuffer.append("$('").append(beforeEffectID).append("').set('morph', { duration: '").append(beforeEffectDuration).append("' });");
				} 
				onClickBuffer.append("$('").append(beforeEffectID).append("').get('morph').start('." + beforeEffectStart + "'").append(").chain(function() {");
			} else if(beforeEffect.equals("slide")) {
				String mode = (String) valueForBinding("effectSlideMode", component);
				String transition = (String) valueForBinding("beforeEffectTransition", component);
				onClickBuffer.append("$('").append(beforeEffectID).append("').set('slide'");
				if(beforeEffectDuration != null || mode != null) {
					onClickBuffer.append(", { ");
					if(beforeEffectDuration != null) {
						onClickBuffer.append("duration: '").append(beforeEffectDuration).append('\'').append(mode != null || transition != null ? "," : "");
					}
					if(mode != null) {
						onClickBuffer.append("mode: '").append(mode).append('\'').append(transition != null ? "," : "");
					}
					if(transition != null) {
						onClickBuffer.append("transition: ").append(transition);
					}
					onClickBuffer.append('}');
				}
				onClickBuffer.append("); $('").append(beforeEffectID).append("').get('slide').slide").append(ERXStringUtilities.capitalize(beforeEffectProperty)).append("().chain(function() {");
			} else if(beforeEffect.equals("highlight")) {
				if(beforeEffectDuration != null) {
					onClickBuffer.append("$('").append(beforeEffectID)
					.append("').set('tween', { duration: '").append(beforeEffectDuration).append("', property: 'background-color'});");
				} else {
					onClickBuffer.append("$('").append(beforeEffectID)
					.append("').set('tween', { property: 'background-color' });");
				}
				onClickBuffer.append("$('").append(updateContainerID).append("').get('tween').start('").append(beforeEffectProperty != null ? beforeEffectProperty : "#ffff88', '#ffffff")
				.append("').chain(function() { ");
			}
		}

		if(updateContainerID != null) {
			onClickBuffer.append("MTASB.update('").append(updateContainerID).append("', ");
		} else {
			onClickBuffer.append("MTASB.request(");
		}
		onClickBuffer.append(formReference);
		if(valueForBinding("functionName", component) != null) {
			onClickBuffer.append(", additionalParams");
		} else {
			onClickBuffer.append(", null");
		}

		onClickBuffer.append(',');
		NSMutableDictionary options = createAjaxOptions(component);

		String effect = (String) valueForBinding("effect", component);
		String afterEffect = (String) valueForBinding("afterEffect", component);

		if(effect != null) {
			String duration = (String) valueForBinding("effectDuration", component);
			String property = (String) valueForBinding("effectProperty", component);
			String start = (String) valueForBinding("effectStart", component);
			String mode = (String) valueForBinding("effectSlideMode", component);
			MTAjaxUpdateLink.addEffect(options, effect, updateContainerID, property, start, duration, mode);
		} else if(afterEffect != null) {
			String duration = (String) valueForBinding("afterEffectDuration", component);
			String property = (String) valueForBinding("afterEffectProperty", component);
			String start = (String) valueForBinding("afterEffectStart", component);
			String afterEffectID = (String) valueForBinding("afterEffectID", component);
			String mode = (String) valueForBinding("effectSlideMode", component);
			if(afterEffectID == null) {
				afterEffectID = AjaxUpdateContainer.currentUpdateContainerID() != null ? AjaxUpdateContainer.currentUpdateContainerID() :
					updateContainerID;
			}
			MTAjaxUpdateLink.addEffect(options, afterEffect, afterEffectID, property, start, duration, mode);
		}

		AjaxOptions.appendToBuffer(options, onClickBuffer, context);
		onClickBuffer.append(')');
		String onClick = (String)valueForBinding("onClick", component);
		if(onClick != null) {
			onClickBuffer.append("; ").append(onClick);
		}

		if(beforeEffect != null) {
			onClickBuffer.append("}.bind(this));");
		}

		if(onClickBefore != null) {
			onClickBuffer.append('}');
		}

		if(functionName != null) {
			AjaxUtils.appendScriptHeader(response);
			response.appendContentString(functionName + " = function(additionalParams) { " + onClickBuffer + " }\n");
			AjaxUtils.appendScriptFooter(response);
		}

		if(showUI) {

			boolean disabled = disabledInComponent(component);
			String elementName = (String)valueForBinding("elementName", "a", component);

			if(showButton) {
				response.appendContentString("<input ");
				appendTagAttributeToResponse(response, "type", "button");
				String name = nameInContext(context, component);
				appendTagAttributeToResponse(response, "name", name);
				appendTagAttributeToResponse(response, "value", valueForBinding("value", component));

				if(disabled) {
					appendTagAttributeToResponse(response, "disabled", "disabled");
				}
			} else {
				boolean isATag = "a".equalsIgnoreCase(elementName);
				if(isATag) {
					response.appendContentString("<a href = \"javascript:void(0)\" ");
				} else {
					response.appendContentString("<" + elementName + " ");
				}
			}

			String classString = (String)valueForBinding("class", component);
			classString = (classString != null) ? classString + " m-a-s-b" : "m-a-s-b";
			appendTagAttributeToResponse(response, "class", classString);
			appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
			appendTagAttributeToResponse(response, "id", valueForBinding("id", component));

			if(functionName == null) {
				appendTagAttributeToResponse(response, "onclick", onClickBuffer.toString());
			} else {
				appendTagAttributeToResponse(response, "onclick", functionName + "()");
			}

			if(showButton) {
				response.appendContentString(" />");
			} else {
				response.appendContentString(">");
				if(hasChildrenElements()) {
					appendChildrenToResponse(response, context);
				}
				response.appendContentString("</" + elementName + ">");
			}

		}

		super.appendToResponse(response, context);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillInAjaxOptions(IAjaxElement element, WOComponent component, String submitButtonName, NSMutableDictionary options) {

		String systemDefaultFormSerializer = "Form.serializeWithoutSubmits";
		String defaultFormSerializer = ERXProperties.stringForKeyWithDefault("er.ajax.formSerializer", systemDefaultFormSerializer);
		String formSerializer = (String) element.valueForBinding("formSerializer", defaultFormSerializer, component);
		if(!defaultFormSerializer.equals(systemDefaultFormSerializer)) {
			options.setObjectForKey(formSerializer, "_fs");
		}

		options.setObjectForKey("'" + submitButtonName + "'", "_asbn");

		if("true".equals(options.objectForKey("async"))) {
			options.removeObjectForKey("async");
		}

		if("true".equals(options.objectForKey("evalScripts"))) {
			options.removeObjectForKey("evalScripts");
		}

		MTAjaxUpdateContainer.expandInsertionFromOptions(options, element, component);

	}	

	@Override
	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {

		WOActionResults result = null;
		WOComponent wocomponent = wocontext.component();

		String nameInContext = nameInContext(wocontext, wocomponent);
		boolean shouldHandleRequest = (!disabledInComponent(wocomponent) && wocontext.wasFormSubmitted()) && ((wocontext.isMultipleSubmitForm() && nameInContext.equals(worequest.formValueForKey(KEY_AJAX_SUBMIT_BUTTON_NAME))) || !wocontext.isMultipleSubmitForm());

		if (shouldHandleRequest) {
			String updateContainerID = MTAjaxUpdateContainer.updateContainerID(this, wocomponent);
			MTAjaxUpdateContainer.setUpdateContainerID(worequest, updateContainerID);
			wocontext.setActionInvoked(true);
			result = handleRequest(worequest, wocontext);
			ERXAjaxApplication.enableShouldNotStorePage();
		}

		return result;
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {

		WOComponent component = context.component();
		WOActionResults result = (WOActionResults) valueForBinding("action", component);

		if (ERXAjaxApplication.isAjaxReplacement(request)) {
			AjaxUtils.setPageReplacementCacheKey(context, (String)valueForBinding("replaceID", component));
		}
		else if (result == null || booleanValueForBinding("ignoreActionResponse", false, component)) {
			WOResponse response = AjaxUtils.createResponse(request, context);
			String onClickServer = (String) valueForBinding("onClickServer", component);
			if (onClickServer != null) {
				AjaxUtils.appendScriptHeaderIfNecessary(request, response);
				response.appendContentString(onClickServer);
				AjaxUtils.appendScriptFooterIfNecessary(request, response);
			}
			result = response;
		}
		else {
			String updateContainerID = MTAjaxUpdateContainer.updateContainerID(this, component);
			if (updateContainerID != null) {
				AjaxUtils.setPageReplacementCacheKey(context, updateContainerID);
			}
		}

		return result;
	}

}