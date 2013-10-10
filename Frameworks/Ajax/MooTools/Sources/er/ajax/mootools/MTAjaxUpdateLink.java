package er.ajax.mootools;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxOption;
import er.ajax.AjaxOptions;
import er.ajax.AjaxUpdateContainer;
import er.ajax.AjaxUpdateLink;
import er.ajax.AjaxUtils;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Updates a region on the screen by creating a request to an action, then returning a script that in turn creates an
 * Ajax.Updater for the area. If you do not provide an action binding, it will just update the specified area.
 * @binding action the action to call when the link executes
 * @binding directActionName the direct action to call when link executes
 * @binding updateContainerID the id of the AjaxUpdateContainer to update after performing this action
 * @binding title title of the link
 * @binding style css style of the link
 * @binding class css class of the link
 * @binding id of the link
 * @binding disabled boolean defining if the link renders the tag
 * @binding string string to get preprended to the contained elements
 * @binding function a custom function to call that takes a single parameter that is the action url
 * @binding elementName the element name to use (defaults to "a")
 * @binding functionName if set, the link becomes a javascript function
 * @binding button if true, this is rendered as a javascript button
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
 * @binding useSpinner (boolean) use the Spinner class with this request
 * @binding defaultSpinnerClass inclue the default spinner css class in the headers - if false provide your own.
 * @binding spinnerOptions - (object) the options object for the Spinner class
 * @binding spinnerTarget - (mixed) a string of the id for an Element or an Element reference to use instead of the one specifed in the update option. This is useful if you want to overlay a different area (or, say, the parent of the one being updated).

**/

public class MTAjaxUpdateLink extends AjaxUpdateLink {

	
	public MTAjaxUpdateLink(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected NSMutableDictionary<String, Object> createAjaxOptions(WOComponent component) {
		
		NSMutableArray<AjaxOption> ajaxOptionsArray = new NSMutableArray<AjaxOption>();
		ajaxOptionsArray.addObject(new AjaxOption("async", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("onRequest", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onException", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("useSpinner", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerTarget", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerOptions", AjaxOption.DICTIONARY));
		
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());

		options.setObjectForKey("'get'", "method");
		if (options.objectForKey("async") == null) {
			options.setObjectForKey("true", "async");
		}		
		if (options.objectForKey("evalScripts") == null) {
			options.setObjectForKey("true", "evalScripts");
		}
		
		AjaxUpdateContainer.expandInsertionFromOptions(options, this, component);
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
	public String onClick(WOContext context, boolean generateFunctionWrapper) {
		
		WOComponent component = context.component();
		NSMutableDictionary options = createAjaxOptions(component);

		StringBuffer onClickBuffer = new StringBuffer();

		String onClick = (String) valueForBinding("onClick", component);
		String onClickBefore = (String) valueForBinding("onClickBefore", component);
		String updateContainerID = (String) valueForBinding("updateContainerID", component);
		String functionName = (String) valueForBinding("functionName", component);
		String function = (String) valueForBinding("function", component);
		String replaceID = (String) valueForBinding("replaceID", component);
		String effect = (String) valueForBinding("effect", component);
		String afterEffect = (String) valueForBinding("afterEffect", component);
		String beforeEffect = (String) valueForBinding("beforeEffect", component);

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
				
		WOAssociation directActionNameAssociation = (WOAssociation) associations().valueForKey("directActionName");
		
		if(beforeEffect == null && updateContainerID != null && directActionNameAssociation == null && replaceID == null
				 && function == null && onClick == null && onClickBefore == null) {
			NSDictionary nonDefaultOptions = MTAjaxUpdateContainer.removeDefaultOptions(options);
			onClickBuffer.append("MTAUL.")
			.append(generateFunctionWrapper ? "updateFunc" : "update")
			.append("('").append(updateContainerID).append("', ");
			AjaxOptions.appendToBuffer(nonDefaultOptions, onClickBuffer, context);
			onClickBuffer.append(", '").append(context.contextID()).append('.').append(context.elementID())
			.append("'").append(")").append(";");

		} else {
					
			if(generateFunctionWrapper) {
				onClickBuffer.append("function(additionalParams) {");
			}
		
			if(onClickBefore != null) {
				onClickBuffer.append("if (")
				.append(onClickBefore)
				.append(") { ");
			}
			
			// EFFECTS
			if(beforeEffect != null) {
				String beforeEffectID = (String)valueForBinding("beforeEffectID", component);
				String beforeEffectDuration = (String) valueForBinding("beforeEffectDuration", component);
				String beforeEffectProperty = (String)valueForBinding("beforeEffectProperty", component);
				String beforeEffectStart = (String)valueForBinding("beforeEffectStart", component);

				if(beforeEffectID == null) {
					beforeEffectID = AjaxUpdateContainer.currentUpdateContainerID() != null ?
							AjaxUpdateContainer.currentUpdateContainerID() : updateContainerID;
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
							onClickBuffer.append("duration: '").append(beforeEffectDuration).append("'").append(mode != null || transition != null ? "," : "");
						}
						if(mode != null) {
							onClickBuffer.append("mode: '").append(mode).append("'").append(transition != null ? "," : "");
						}
						if(transition != null) {
							onClickBuffer.append("transition: ").append(transition);
						}
						onClickBuffer.append("}");
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
			
			String actionUrl = null;
			if(directActionNameAssociation != null) {
				actionUrl = context.directActionURLForActionNamed((String) directActionNameAssociation.valueInComponent(component), ERXComponentUtilities.queryParametersInComponent(associations(), component)).replaceAll("&amp;", "&");
			} else {
				actionUrl = AjaxUtils.ajaxComponentActionUrl(context);
			}
			actionUrl = "'" + actionUrl + "'";

			if(functionName != null) {
				actionUrl = actionUrl + ".addQueryParameters(additionalParams);";
			}
			
			if(function != null) {
				onClickBuffer.append("return " + function + "(" + actionUrl + ")");
			} else {
				options.setObjectForKey(actionUrl, "url");
				if(replaceID == null) {
					if(updateContainerID == null) {
						onClickBuffer.append("new Request(");
						AjaxOptions.appendToBuffer(options, onClickBuffer, context);
						onClickBuffer.append(").send();");
					} else {
						options.takeValueForKey("'" + updateContainerID + "'", "update");
						onClickBuffer.append("new Request.HTML(");
						AjaxOptions.appendToBuffer(options, onClickBuffer, context);
						onClickBuffer.append(").send();");
					}
				} else {
					options.takeValueForKey("'" + replaceID + "'", "update");
					onClickBuffer.append("new Request.HTML(");
					AjaxOptions.appendToBuffer(options, onClickBuffer, context);
					onClickBuffer.append(").send();");
				}
			}
			
			if(onClick != null) {
				onClickBuffer.append(";")
				.append(onClick);
			}
			
			if(beforeEffect != null) {
				onClickBuffer.append("});");
			}
			
			if(onClickBefore != null) {
				onClickBuffer.append(" } ");
			}
			
			if(generateFunctionWrapper) {
				onClickBuffer.append("}");
			}
		}

		return onClickBuffer.toString();
	
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void addEffect(NSMutableDictionary options, String effect, String updateContainerID, String effectProperty, String effectStart, String duration, String mode) {

		if(effect != null) {
			
			if(options.objectForKey("onSuccess") != null) {
				throw new WODynamicElementCreationException("You cannot specify both an effect and a custom onSuccess function.");
			} 
			if(updateContainerID == null) {
				throw new WODynamicElementCreationException("You cannot specify an effect without an updateContainerID.");
			}
			StringBuffer effectBuffer = new StringBuffer();
			effectBuffer.append("function() { ");
			if(effect.equals("tween")) {
				if(duration != null) {
					effectBuffer.append("$('").append(updateContainerID).append("').set('tween', { duration: '").append(duration).append("', property: '")
					.append(effectProperty).append("' });");
				} else {
					effectBuffer.append("$('").append(updateContainerID).append("').set('tween', { property: '").append(effectProperty).append("' });");
				}
				effectBuffer.append("$('").append(updateContainerID).append("').get('tween').start(" + effectStart + ");");
			} else if(effect.equals("morph")) {
				if(duration != null) {
					effectBuffer.append("$('").append(updateContainerID).append("').set('morph', { duration: '").append(duration).append("'});");
				}
				effectBuffer.append("$('").append(updateContainerID).append("').get('morph').start('." + effectStart + "');");
			} else if(effect.equals("slide")) {
				effectBuffer.append("$('").append(updateContainerID).append("').get('slide').slide").append(ERXStringUtilities.capitalize(effectProperty)).append("(); ");
			} else if(effect.equals("highlight")) {
				effectBuffer.append("$('").append(updateContainerID).append("').highlight(").append(effectProperty != null ? effectProperty : "").append(");");
			}
			effectBuffer.append("}");
			options.setObjectForKey(effectBuffer.toString(), "onSuccess");
		}
	}
	


}