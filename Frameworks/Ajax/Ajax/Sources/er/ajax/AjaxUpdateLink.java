package er.ajax;

import java.net.MalformedURLException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXRequest;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXMutableURL;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Updates a region on the screen by creating a request to an action, then returning a script that in turn creates an
 * Ajax.Updater for the area. If you do not provide an action binding, it will just update the specified area.
 * 
 * @binding action the action to call when the link executes
 * @binding directActionName the direct action to call when link executes
 * @binding onLoading JavaScript function to evaluate when the request begins
 * @binding onComplete JavaScript function to evaluate when the request has finished.
 * @binding onSuccess JavaScript function to evaluate when the request was successful.
 * @binding onFailure JavaScript function to evaluate when the request has failed.
 * @binding onException JavaScript function to evaluate when the request had errors.
 * @binding evalScripts boolean defining if the container update is expected to be a script.
 * @binding ignoreActionResponse boolean defining if the action's response should be thrown away (useful when the same
 *          action has both Ajax and plain links)
 * @binding onClickBefore if the given function returns true, the onClick is executed. This is to support confirm(..)
 *          dialogs.
 * @binding onClick JS function, called after the click on the client
 * @binding onClickServer JS returned from the server after the update
 * @binding updateContainerID the id of the AjaxUpdateContainer to update after performing this action
 * @binding replaceID the ID of the div (or other html element) whose contents will be replaced with the results of this
 *          action
 * @binding title title of the link
 * @binding style css style of the link
 * @binding class css class of the link
 * @binding id id of the link
 * @binding disabled boolean defining if the link renders the tag
 * @binding string string to get preprended to the contained elements
 * @binding function a custom function to call that takes a single parameter that is the action url
 * @binding elementName the element name to use (defaults to "a")
 * @binding functionName if set, the link becomes a javascript function
 * @binding button if true, this is rendered as a javascript button
 * @binding asynchronous boolean defining if the update request is sent asynchronously or synchronously, defaults to true
 * @binding accesskey hot key that should trigger the link (optional)

 * // PROTOTYPE EFFECTS
 * @binding effect synonym of afterEffect except it always applies to updateContainerID
 * @binding effectDuration the duration of the effect to apply before
 * // PROTOTYPE EFFECTS
 * @binding beforeEffect the Scriptaculous effect to apply onSuccess ("highlight", "slideIn", "blindDown", etc);
 * @binding beforeEffectID the ID of the container to apply the "before" effect to (blank = try nearest container, then
 *          try updateContainerID)
 * @binding beforeEffectDuration the duration of the effect to apply before
 * // PROTOTYPE EFFECTS
 * @binding afterEffect the Scriptaculous effect to apply onSuccess ("highlight", "slideIn", "blindDown", etc);
 * @binding afterEffectID the ID of the container to apply the "after" effect to (blank = try nearest container, then
 *          try updateContainerID)
 * @binding afterEffectDuration the duration of the effect to apply before
 * 
 * // PROTOTYPE EFFECTS
 * @binding insertion JavaScript function to evaluate when the update takes place (or effect shortcuts like "Effect.blind", or "Effect.BlindUp")
 * @binding insertionDuration the duration of the before and after insertion animation (if using insertion) 
 * @binding beforeInsertionDuration the duration of the before insertion animation (if using insertion) 
 * @binding afterInsertionDuration the duration of the after insertion animation (if using insertion) 
 */
public class AjaxUpdateLink extends AjaxDynamicElement {

	public AjaxUpdateLink(String name, NSDictionary associations, WOElement children) {
		super(name, associations, children);
	}
	
	public String onClick(WOContext context, boolean generateFunctionWrapper) {
		WOComponent component = context.component();
		NSMutableDictionary options = createAjaxOptions(component);
		StringBuffer onClickBuffer = new StringBuffer();

		String onClick = (String) valueForBinding("onClick", component);
		String onClickBefore = (String) valueForBinding("onClickBefore", component);
		String updateContainerID = AjaxUpdateContainer.updateContainerID(this, component); 
		String functionName = (String) valueForBinding("functionName", component);
		String function = (String) valueForBinding("function", component);
		String replaceID = (String) valueForBinding("replaceID", component);

		// PROTOTYPE EFFECTS
		AjaxUpdateLink.addEffect(options, (String) valueForBinding("effect", component), updateContainerID, (String) valueForBinding("effectDuration", component));
		String afterEffectID = (String) valueForBinding("afterEffectID", component);
		if (afterEffectID == null) {
			afterEffectID = AjaxUpdateContainer.currentUpdateContainerID();
			if (afterEffectID == null) {
				afterEffectID = updateContainerID;
			}
		}
		// PROTOTYPE EFFECTS
		AjaxUpdateLink.addEffect(options, (String) valueForBinding("afterEffect", component), afterEffectID, (String) valueForBinding("afterEffectDuration", component));

		// PROTOTYPE EFFECTS
		String beforeEffect = (String) valueForBinding("beforeEffect", component);

		WOAssociation directActionNameAssociation = (WOAssociation) associations().valueForKey("directActionName");
		if (beforeEffect == null && updateContainerID != null && directActionNameAssociation == null && replaceID == null && function == null && onClick == null && onClickBefore == null) {
			NSDictionary nonDefaultOptions = AjaxUpdateContainer.removeDefaultOptions(options);
			onClickBuffer.append("AUL.");
			if (generateFunctionWrapper) {
				onClickBuffer.append("updateFunc");
			}
			else {
				onClickBuffer.append("update");
			}
			onClickBuffer.append("('");
			onClickBuffer.append(updateContainerID);
			onClickBuffer.append("', ");
			AjaxOptions.appendToBuffer(nonDefaultOptions, onClickBuffer, context);
			onClickBuffer.append(", '");
			onClickBuffer.append(context.contextID());
			onClickBuffer.append('.');
			onClickBuffer.append(context.elementID());
			onClickBuffer.append('\'');
			// if (generateFunctionWrapper) {
			// onClickBuffer.append(", additionalParams");
			// }
			onClickBuffer.append(')');
			onClickBuffer.append(';');
		}
		else {
			if (generateFunctionWrapper) {
				onClickBuffer.append("function(additionalParams) {");
			}
			if (onClickBefore != null) {
				onClickBuffer.append("if (");
				onClickBuffer.append(onClickBefore);
				onClickBuffer.append(") {");
			}

			// PROTOTYPE EFFECTS
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

			String actionUrl = null;
			if (directActionNameAssociation != null) {
				actionUrl = context._directActionURL((String) directActionNameAssociation.valueInComponent(component), ERXComponentUtilities.queryParametersInComponent(associations(), component), ERXRequest.isRequestSecure(context.request()), 0, false).replaceAll("&amp;", "&");
			}
			else {
				actionUrl = AjaxUtils.ajaxComponentActionUrl(context);
			}
			
			if (replaceID != null) {
				try {
					ERXMutableURL tempActionUrl = new ERXMutableURL(actionUrl);
					tempActionUrl.addQueryParameter(ERXAjaxApplication.KEY_REPLACED, "true");
					actionUrl = tempActionUrl.toExternalForm();
				}
				catch (MalformedURLException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				}
			}

			actionUrl = "'" + actionUrl + "'";
			if (functionName != null) {
				actionUrl = actionUrl + ".addQueryParameters(additionalParams)";
			}

			if (function != null) {
				onClickBuffer.append("return " + function + "(" + actionUrl + ")");
			}
			else {
				// PROTOTYPE FUNCTIONS
				if (replaceID == null) {
					if (updateContainerID == null) {
						onClickBuffer.append("new Ajax.Request(" + actionUrl + ", ");
						AjaxOptions.appendToBuffer(options, onClickBuffer, context);
						onClickBuffer.append(')');
					}
					else {
						onClickBuffer.append("new Ajax.Updater('" + updateContainerID + "', " + actionUrl + ", ");
						AjaxOptions.appendToBuffer(options, onClickBuffer, context);
						onClickBuffer.append(')');
					}
				}
				else {
					onClickBuffer.append("new Ajax.Updater('" + replaceID + "', " + actionUrl + ", ");
					AjaxOptions.appendToBuffer(options, onClickBuffer, context);
					onClickBuffer.append(')');
				}
			}

			if (onClick != null) {
				onClickBuffer.append(';');
				onClickBuffer.append(onClick);
			}

			if (beforeEffect != null) {
				onClickBuffer.append("}});");
			}

			if (onClickBefore != null) {
				onClickBuffer.append('}');
			}

			if (generateFunctionWrapper) {
				onClickBuffer.append('}');
			}
		}

		return onClickBuffer.toString();
	}

	// PROTOTYPE EFFECTS
	public static void addEffect(NSMutableDictionary options, String effect, String updateContainerID, String duration) {
		if (effect != null) {
			if (options.objectForKey("onSuccess") != null) {
				throw new WODynamicElementCreationException("You cannot specify both an effect and a custom onSuccess function.");
			}

			if (updateContainerID == null) {
				throw new WODynamicElementCreationException("You cannot specify an effect without an updateContainerID.");
			}
			
			StringBuilder effectBuffer = new StringBuilder();
			effectBuffer.append("function() { new " + AjaxUpdateLink.fullEffectName(effect) + "('" + updateContainerID + "', {  queue:'end'");
			if (duration != null) {
				effectBuffer.append(", duration: ");
				effectBuffer.append(duration);
			}
			effectBuffer.append("}) }");

			options.setObjectForKey(effectBuffer.toString(), "onSuccess");
		}
	}

	// PROTOTYPE EFFECTS
	public static String fullEffectName(String effectName) {
		String fullEffectName;
		if (effectName == null) {
			fullEffectName = null;
		}
		else if (effectName.indexOf('.') == -1) {
			fullEffectName = "Effect." + ERXStringUtilities.capitalize(effectName);
		}
		else {
			fullEffectName = effectName;
		}
		return fullEffectName;
	}

	// PROTOTYPE OPTIONS
	protected NSMutableDictionary createAjaxOptions(WOComponent component) {
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("onLoading", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onException", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("insertion", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("asynchronous", AjaxOption.BOOLEAN));
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());

		options.setObjectForKey("'get'", "method");
		if (options.objectForKey("asynchronous") == null) {
			options.setObjectForKey("true", "asynchronous");
		}
		if (options.objectForKey("evalScripts") == null) {
			options.setObjectForKey("true", "evalScripts");
		}

		AjaxUpdateContainer.expandInsertionFromOptions(options, this, component);
		return options;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		
		boolean disabled = booleanValueForBinding("disabled", false, component);
		Object stringValue = valueForBinding("string", component);
		String functionName = (String) valueForBinding("functionName", component);
		if (functionName == null) {
			String elementName;
			boolean button = booleanValueForBinding("button", false, component);
			if (button) {
				elementName = "input";
			}
			else {
				elementName = (String) valueForBinding("elementName", "a", component);
			}
			boolean isATag = "a".equalsIgnoreCase(elementName);
			boolean renderTags = (!disabled || !isATag);
			if (renderTags) {
				response.appendContentString("<");
				response.appendContentString(elementName);
				response.appendContentString(" ");
				if (button) {
					appendTagAttributeToResponse(response, "type", "button");
				}
				if (isATag) {
					appendTagAttributeToResponse(response, "href", "javascript:void(0);");
				}
				if (!disabled) {
					appendTagAttributeToResponse(response, "onclick", onClick(context, false));
				}
				appendTagAttributeToResponse(response, "title", valueForBinding("title", component));
				appendTagAttributeToResponse(response, "value", valueForBinding("value", component));
				appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
				appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
				appendTagAttributeToResponse(response, "id", valueForBinding("id", component));
				appendTagAttributeToResponse(response, "accesskey", valueForBinding("accesskey", component));
				if (button) {
					if (stringValue != null) {
						appendTagAttributeToResponse(response, "value", stringValue);
					}
					if (disabled) {
						response.appendContentString(" disabled");
					}
				}
				// appendTagAttributeToResponse(response, "onclick",
				// onClick(context));
				response.appendContentString(">");
			}
			if (stringValue != null && !button) {
				response.appendContentHTMLString(stringValue.toString());
			}
			appendChildrenToResponse(response, context);
			if (renderTags) {
				response.appendContentString("</");
				response.appendContentString(elementName);
				response.appendContentString(">");
			}
		}
		else {
			AjaxUtils.appendScriptHeader(response);
			response.appendContentString(functionName);
			response.appendContentString(" = ");
			response.appendContentString(onClick(context, true));
			AjaxUtils.appendScriptFooter(response);
		}
		super.appendToResponse(response, context);
	}

	@Override
	protected void addRequiredWebResources(WOResponse res, WOContext context) {
		addScriptResourceInHead(context, res, "prototype.js");
    	addScriptResourceInHead(context, res, "effects.js");
		addScriptResourceInHead(context, res, "wonder.js");
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		boolean disabled = booleanValueForBinding("disabled", false, component);
		String updateContainerID = AjaxUpdateContainer.updateContainerID(this, component); 
		AjaxUpdateContainer.setUpdateContainerID(request, updateContainerID);
		WOActionResults results = null;
		if (!disabled) {
			results = (WOActionResults) valueForBinding("action", component);
		}

		if (ERXAjaxApplication.isAjaxReplacement(request)) {
			AjaxUtils.setPageReplacementCacheKey(context, (String)valueForBinding("replaceID", component));
		}
		else if (results == null || booleanValueForBinding("ignoreActionResponse", false, component)) {
			String script = (String) valueForBinding("onClickServer", component);
			if (script != null) {
				WOResponse response = AjaxUtils.createResponse(request, context);
				AjaxUtils.appendScriptHeaderIfNecessary(request, response);
				response.appendContentString(script);
				AjaxUtils.appendScriptFooterIfNecessary(request, response);
				results = response;
			}
		}
		else if (updateContainerID != null) {
			AjaxUtils.setPageReplacementCacheKey(context, updateContainerID);
		}

		return results;
	}
}