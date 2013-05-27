package er.ajax.mootools;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxObserveField;
import er.ajax.AjaxOption;
import er.ajax.AjaxOptions;
import er.ajax.AjaxUpdateContainer;
import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXValueUtilities;


/**
 * observeFieldID requires ERExtensions, specifically ERXWOForm
 * @binding elementName the container's element defaults to DIV.
 * @binding action the action to call when this updateContainer refreshes
 * @binding method - (string: defaults to 'post') The HTTP method for the request, can be either 'post' or 'get'.
 * @binding encoding - (string: defaults to 'utf-8') The encoding to be set in the request header.
 * @binding emulation - (boolean: defaults to true) If set to true, other methods than 'post' or 'get' are appended as post-data named '_method' (as used in rails)
 * @binding headers - (object) An object to use in order to set the request headers.
 * @binding isSuccess - (function) Overrides the built-in isSuccess function.
 * @binding evalScripts - (boolean: defaults to true) If set to true, script tags inside the response will be evaluated.
 * @binding evalResponse -  (boolean: defaults to false) If set to true, the entire response will be evaluated. Responses with javascript content-type will be evaluated automatically.
 * @binding urlEncoded - (boolean: defaults to true) If set to true, the content-type header is set to www-form-urlencoded + encoding
 * @binding noCache - (boolean; defaults to false) If true, appends a unique noCache value to the request to prevent caching. (IE has a bad habit of caching ajax request values. Including this script and setting the noCache value to true will prevent it from caching. The server should ignore the noCache value.)
 * @binding async - (boolean: defaults to true) If set to false, the requests will be synchronous and freeze the browser during request.
 * @binding optional set to true if you want the container tags to be skipped if this is already in an update container (similar to ERXOptionalForm). 
 *                   If optional is true and there is a container, it's as if this AUC doesn't exist, and only its children will render to the page. 
 * @binding onCancel Fired when a request has been cancelled.
 * @binding onComplete Fired when the Request is completed.
 * @binding onException Fired when setting a request header fails.
 * @binding onFailure Fired when the request failed (error status code).
 * @binding onRequest Fired when the Request is sent.
 * @binding onSuccess(responseTree, responseElements, responseHTML, responseJavaScript) Fired when the Request is completed successfully.
 * @binding frequency the frequency (in seconds) of a periodic update
 * @binding initialDelay - (number; defaults to 5000) The initial delay to wait for the request after a call to the start method
 * @binding delay - (number; defaults to 5000) The delay between requests and the number of ms to add if no valid data has been returned
 * @binding limit - (number; defaults to 60000) The maximum time the interval uses to request the server
 * responseTree - (element) The node list of the remote response.
 * responseElements - (array) An array containing all elements of the remote response.
 * responseHTML - (string) The content of the remote response.
	 * responseJavaScript - (string) The portion of JavaScript from the remote response.
 * @binding onFailure Fired when the request failed (error status code).
 * @binding useSpinner (boolean) use the Spinner class with this request
 * @binding defaultSpinnerClass inclue the default spinner css class in the headers - if false provide your own.
 * @binding spinnerOptions - (object) the options object for the Spinner class
 * @binding spinnerTarget - (mixed) a string of the id for an Element or an Element reference to use instead of the one specifed in the update option. This is useful if you want to overlay a different area (or, say, the parent of the one being updated).
 */


public class MTAjaxUpdateContainer extends AjaxUpdateContainer {

	public MTAjaxUpdateContainer(String name, NSDictionary<String, WOAssociation> associations, WOElement children) {
		super(name, associations, children);
	}

	public static String updateContainerID(AjaxDynamicElement element, WOComponent component) {
		return AjaxUpdateContainer.updateContainerID(element, component);
	}	
	
	@Override
	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
		Boolean useSpinner = (Boolean)valueForBinding("useSpinner", Boolean.FALSE, context.component());
		if(useSpinner.booleanValue()) {
			Boolean useDefaultSpinnerClass = (Boolean)valueForBinding("defaultSpinnerClass", Boolean.TRUE, context.component());
			if(useDefaultSpinnerClass.booleanValue()) {
				AjaxUtils.addStylesheetResourceInHead(context, context.response(), "MTAjax", "scripts/plugins/spinner/spinner.css");
			}
		}
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_WONDER_JS);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public NSDictionary createAjaxOptions(WOComponent component) {

		NSMutableArray<AjaxOption> ajaxOptionsArray = new NSMutableArray<AjaxOption>();
		ajaxOptionsArray.addObject(new AjaxOption("method", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("frequency", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("initialDelay", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("delay", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("limit", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("encoding", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("emulation", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("headers", AjaxOption.ARRAY));
		ajaxOptionsArray.addObject(new AjaxOption("isSuccess", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("evalResponse", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("urlEncoded", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("async", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("noCache", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("onRequest", AjaxOption.FUNCTION));
		ajaxOptionsArray.addObject(new AjaxOption("onCancel", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.FUNCTION_1));
		ajaxOptionsArray.addObject(new AjaxOption("onException", AjaxOption.FUNCTION_2));
		ajaxOptionsArray.addObject(new AjaxOption("useSpinner", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerTarget", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("spinnerOptions", AjaxOption.DICTIONARY));

		NSMutableDictionary<String, String> options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		options.setObjectForKey("'get'", "method");
		if (options.objectForKey("evalScripts") == null) {
			options.setObjectForKey("true", "evalScripts");
		}  

		AjaxUpdateContainer.expandInsertionFromOptions(options, this, component);

		return options;

	}
	
	public static NSDictionary removeDefaultOptions(NSDictionary options) {
		// PROTOTYPE OPTIONS
		NSMutableDictionary mutableOptions = options.mutableClone();
		if ("'get'".equals(mutableOptions.objectForKey("method"))) {
			mutableOptions.removeObjectForKey("method");
		}
		if ("true".equals(mutableOptions.objectForKey("evalScripts"))) {
			mutableOptions.removeObjectForKey("evalScripts");
		}
		if ("true".equals(mutableOptions.objectForKey("async"))) {
			mutableOptions.removeObjectForKey("async");
		}
		return mutableOptions;
	}	
	

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {

		WOComponent component = context.component();
		
		if(!shouldRenderContainer(component)) {
			if(hasChildrenElements()) {
				appendChildrenToResponse(response, context);
			}
			super.appendToResponse(response, context);
		}

		else {
			
			String previousUpdateContainerID = AjaxUpdateContainer.currentUpdateContainerID();
			try {

				String elementName = (String) valueForBinding("elementName", "div", component);
				String id = _containerID(context);
				AjaxUpdateContainer.setCurrentUpdateContainerID(_containerID(context));
				response.appendContentString("<" + elementName + " ");
				appendTagAttributeToResponse(response, "id", id);
				appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
				appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
				appendTagAttributeToResponse(response, "data-updateUrl", AjaxUtils.ajaxComponentActionUrl(context));
				response.appendContentString(">");
			
				if(hasChildrenElements()) {
					appendChildrenToResponse(response, context);
				}
				
				response.appendContentString("</" + elementName + ">");

				addRequiredWebResources(response, context);
//				super.appendToResponse(response, context);
				
				NSDictionary options = createAjaxOptions(component);
				
				Object frequency = valueForBinding("frequency", component);
				String observeFieldID = (String) valueForBinding("observeFieldID", component);
				
				boolean skipFunction = frequency == null && observeFieldID == null && booleanValueForBinding("skipFunction", false, component);

				if(!skipFunction) {
					AjaxUtils.appendScriptHeader(response);
					if(frequency != null) {
						boolean isNotZero = true;
						try {
							float numberFrequency = ERXValueUtilities.floatValue(frequency);
							if(numberFrequency == 0.0) {
								isNotZero = false;
							}
						} catch (RuntimeException e) {
							throw new IllegalStateException("Error parsing float from value : <" + frequency + ">");
						}
						if(isNotZero) {
							boolean canStop = false;
							boolean stopped = false;
							if(associations().objectForKey("stopped") != null) {
								canStop = true;
								stopped = booleanValueForBinding("stopped", false, component);
							}
							response.appendContentString("MTAUC.registerPeriodic('" + id + "'," + canStop + "," + stopped + ",");
							AjaxOptions.appendToResponse(options, response, context);		
							response.appendContentString(");");
						}
					}
					
					if(observeFieldID != null) {
						boolean fullSubmit = booleanValueForBinding("fullSubmit", false, component);
						AjaxObserveField.appendToResponse(response, context, this, observeFieldID, false, id, fullSubmit, createObserveFieldOptions(component));
					}

					response.appendContentString("MTAUC.register('" + id + "'");
					NSDictionary nonDefaultOptions = AjaxUpdateContainer.removeDefaultOptions(options);
					if (nonDefaultOptions.count() > 0) {
						response.appendContentString(", ");
						AjaxOptions.appendToResponse(nonDefaultOptions, response, context);
					}
					response.appendContentString(");");

					AjaxUtils.appendScriptFooter(response);
					
				}
				
				
			} finally {
				AjaxUpdateContainer.setCurrentUpdateContainerID(previousUpdateContainerID);
			}
			
		}
		
	}
	
	
}