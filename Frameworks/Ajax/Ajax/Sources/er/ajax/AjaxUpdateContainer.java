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

import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.foundation.ERXValueUtilities;

/**
 * observeFieldID requires ERExtensions, specifically ERXWOForm
 * 
 * @binding onRefreshComplete the script to execute at the end of refreshing the container
 * @binding action the action to call when this updateContainer refreshes
 * 
 * @binding insertion JavaScript function to evaluate when the update takes place (or effect shortcuts like "Effect.blind", or "Effect.BlindUp")
 * @binding insertionDuration the duration of the before and after insertion animation (if using insertion) 
 * @binding beforeInsertionDuration the duration of the before insertion animation (if using insertion) 
 * @binding afterInsertionDuration the duration of the after insertion animation (if using insertion) 
 * @binding asynchronous set to false to force a synchronous refresh of the container. Defaults to true.
 * @binding optional set to true if you want the container tags to be skipped if this is already in an update container (similar to ERXOptionalForm). 
 *                   If optional is true and there is a container, it's as if this AUC doesn't exist, and only its children will render to the page. 
 * 
 * @binding frequency the frequency (in seconds) of a periodic update
 * @binding decay a multiplier (default is one) applied to the frequency if the response of the update is unchanged
 * @binding stopped determines whether a periodic update container loads as stopped.
 */
public class AjaxUpdateContainer extends AjaxDynamicElement {
	private static final String CURRENT_UPDATE_CONTAINER_ID_KEY = "er.ajax.AjaxUpdateContainer.currentID";

	public AjaxUpdateContainer(String name, NSDictionary associations, WOElement children) {
		super(name, associations, children);
	}

	/**
	 * Adds all required resources.
	 */
	@Override
	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		addScriptResourceInHead(context, response, "prototype.js");
		addScriptResourceInHead(context, response, "effects.js");
		addScriptResourceInHead(context, response, "wonder.js");
	}

	protected boolean shouldRenderContainer(WOComponent component) {
		boolean renderContainer = !booleanValueForBinding("optional", false, component) || AjaxUpdateContainer.currentUpdateContainerID() == null;
		return renderContainer;
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (shouldRenderContainer(context.component())) {
			String previousUpdateContainerID = AjaxUpdateContainer.currentUpdateContainerID();
			try {
				AjaxUpdateContainer.setCurrentUpdateContainerID(_containerID(context));
				super.takeValuesFromRequest(request, context);
			}
			finally {
				AjaxUpdateContainer.setCurrentUpdateContainerID(previousUpdateContainerID);
			}
		}
		else {
			super.takeValuesFromRequest(request, context);
		}
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults results;
		if (shouldRenderContainer(context.component())) {
			String previousUpdateContainerID = AjaxUpdateContainer.currentUpdateContainerID();
			try {
				AjaxUpdateContainer.setCurrentUpdateContainerID(_containerID(context));
				results = super.invokeAction(request, context);
			}
			finally {
				AjaxUpdateContainer.setCurrentUpdateContainerID(previousUpdateContainerID);
			}
		}
		else {
			results = super.invokeAction(request, context);
		}
		return results;
	}

	public NSDictionary createAjaxOptions(WOComponent component) {
		// PROTOTYPE OPTIONS
		NSMutableArray<AjaxOption> ajaxOptionsArray = new NSMutableArray<AjaxOption>();
		ajaxOptionsArray.addObject(new AjaxOption("frequency", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("decay", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("onLoading", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onSuccess", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onException", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("insertion", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", Boolean.TRUE, AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("asynchronous", Boolean.TRUE, AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("method", "get", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", Boolean.TRUE, AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("parameters", AjaxOption.STRING));
		NSMutableDictionary<String, String> options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		AjaxUpdateContainer.expandInsertionFromOptions(options, this, component);
		return options;
	}

	public static void expandInsertionFromOptions(NSMutableDictionary<String, String> options, IAjaxElement element, WOComponent component) {
		// PROTOTYPE EFFECTS
		String insertionDuration = (String) element.valueForBinding("insertionDuration", component);
		String beforeInsertionDuration = (String) element.valueForBinding("beforeInsertionDuration", component);
		if (beforeInsertionDuration == null) {
			beforeInsertionDuration = insertionDuration;
		}
		String afterInsertionDuration = (String) element.valueForBinding("afterInsertionDuration", component);
		if (afterInsertionDuration == null) {
			afterInsertionDuration = insertionDuration;
		}
		String insertion = options.objectForKey("insertion");
		String expandedInsertion = AjaxUpdateContainer.expandInsertion(insertion, beforeInsertionDuration, afterInsertionDuration);
		if (expandedInsertion != null) {
			options.setObjectForKey(expandedInsertion, "insertion");
		}
	}

	public static String expandInsertion(String originalInsertion, String beforeDuration, String afterDuration) {
		// PROTOTYPE EFFECTS
		String expandedInsertion = originalInsertion;
		if (originalInsertion != null && originalInsertion.startsWith("Effect.")) {
			String effectPairName = originalInsertion.substring("Effect.".length());
			expandedInsertion = "AUC.insertionFunc('" + effectPairName + "', " + beforeDuration + "," + afterDuration + ")";

		}
		return expandedInsertion;
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
		if ("true".equals(mutableOptions.objectForKey("asynchronous"))) {
			mutableOptions.removeObjectForKey("asynchronous");
		}
		return mutableOptions;
	}

	public NSMutableDictionary createObserveFieldOptions(WOComponent component) {
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("observeFieldFrequency", AjaxOption.NUMBER));
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		return options;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		if (!shouldRenderContainer(component)) {
			if (hasChildrenElements()) {
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
				// appendTagAttributeToResponse(response, "woElementID", context.elementID());
				response.appendContentString(">");
				if (hasChildrenElements()) {
					appendChildrenToResponse(response, context);
				}
				response.appendContentString("</" + elementName + ">");

				super.appendToResponse(response, context);

				NSDictionary options = createAjaxOptions(component);

				Object frequency = valueForBinding("frequency", component);
				String observeFieldID = (String) valueForBinding("observeFieldID", component);

				boolean skipFunction = frequency == null && observeFieldID == null && booleanValueForBinding("skipFunction", false, component);
				if (!skipFunction) {
					AjaxUtils.appendScriptHeader(response);

					if (frequency != null) {
						// try to convert to a number to check whether it is 0
						boolean isNotZero = true;
						try {
							float numberFrequency = ERXValueUtilities.floatValue(frequency);
							if (numberFrequency == 0.0) {
								// set this only to false if it can be converted to 0
								isNotZero = false;
							}
						}
						catch (RuntimeException e) {
							throw new IllegalStateException("Error parsing float from value : <" + frequency + ">");
						}

						if (isNotZero) {
							boolean canStop = false;
							boolean stopped = false;
							if (associations().objectForKey("stopped") != null) {
								canStop = true;
								stopped = booleanValueForBinding("stopped", false, component);
							}
							response.appendContentString("AUC.registerPeriodic('" + id + "'," + canStop + "," + stopped + ",");
							AjaxOptions.appendToResponse(options, response, context);
							response.appendContentString(");");
						}
					}

					if (observeFieldID != null) {
						boolean fullSubmit = booleanValueForBinding("fullSubmit", false, component);
						AjaxObserveField.appendToResponse(response, context, this, observeFieldID, false, id, fullSubmit, createObserveFieldOptions(component));
					}

					response.appendContentString("AUC.register('" + id + "'");
					NSDictionary nonDefaultOptions = AjaxUpdateContainer.removeDefaultOptions(options);
					if (nonDefaultOptions.count() > 0) {
						response.appendContentString(", ");
						AjaxOptions.appendToResponse(nonDefaultOptions, response, context);
					}
					response.appendContentString(");");

					AjaxUtils.appendScriptFooter(response);
				}
			}
			finally {
				AjaxUpdateContainer.setCurrentUpdateContainerID(previousUpdateContainerID);
			}
		}
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		String id = _containerID(context);

		if (associations().objectForKey("action") != null) {
			@SuppressWarnings("unused")
			WOActionResults results = (WOActionResults) valueForBinding("action", component);
			// ignore results
		}

		WOResponse response = AjaxUtils.createResponse(request, context);
		AjaxUtils.setPageReplacementCacheKey(context, id);
		if (hasChildrenElements()) {
			appendChildrenToResponse(response, context);
		}
		String onRefreshComplete = (String) valueForBinding("onRefreshComplete", component);
		if (onRefreshComplete != null) {
			AjaxUtils.appendScriptHeader(response);
			response.appendContentString(onRefreshComplete);
			AjaxUtils.appendScriptFooter(response);
		}
		if (AjaxModalDialog.isInDialog(context)) {
			AjaxUtils.appendScriptHeader(response);
			response.appendContentString("AMD.contentUpdated();");
			AjaxUtils.appendScriptFooter(response);
		}
		return null;
	}

	@Override
	protected String _containerID(WOContext context) {
		String id = (String) valueForBinding("id", context.component());
		if (id == null) {
			id = ERXWOContext.safeIdentifierName(context, false);
		}
		return id;
	}

	public static String updateContainerID(WORequest request) {
		return (String) ERXWOContext.contextDictionary().objectForKey(ERXAjaxApplication.KEY_UPDATE_CONTAINER_ID);
	}

	public static void setUpdateContainerID(WORequest request, String updateContainerID) {
		if (updateContainerID != null) {
			ERXWOContext.contextDictionary().setObjectForKey(updateContainerID, ERXAjaxApplication.KEY_UPDATE_CONTAINER_ID);
		}
	}

	public static boolean hasUpdateContainerID(WORequest request) {
		return AjaxUpdateContainer.updateContainerID(request) != null;
	}

	public static String currentUpdateContainerID() {
		return (String) ERXWOContext.contextDictionary().objectForKey(AjaxUpdateContainer.CURRENT_UPDATE_CONTAINER_ID_KEY);
	}

	public static void setCurrentUpdateContainerID(String updateContainerID) {
		if (updateContainerID == null) {
			ERXWOContext.contextDictionary().removeObjectForKey(AjaxUpdateContainer.CURRENT_UPDATE_CONTAINER_ID_KEY);
		}
		else {
			ERXWOContext.contextDictionary().setObjectForKey(updateContainerID, AjaxUpdateContainer.CURRENT_UPDATE_CONTAINER_ID_KEY);
		}
	}

	public static String updateContainerID(AjaxDynamicElement element, WOComponent component) {
		return AjaxUpdateContainer.updateContainerID(element, "updateContainerID", component);
	}

	public static String updateContainerID(AjaxDynamicElement element, String bindingName, WOComponent component) {
		String updateContainerID = (String) element.valueForBinding("updateContainerID", component);
		return AjaxUpdateContainer.updateContainerID(updateContainerID);
	}

	public static String updateContainerID(String updateContainerID) {
		if ("_parent".equals(updateContainerID)) {
			updateContainerID = AjaxUpdateContainer.currentUpdateContainerID();
		}
		return updateContainerID;
	}
	
	/**
	 * Creates or updates Ajax response so that the indicated AUC will get updated when the response is processed in the browser.
	 * Adds JavaScript like <code>AUC.update('SomeContainerID');</code>
	 * 
	 * @param updateContainerID the HTML ID of the element implementing the AUC
	 * @param context WOContext for response
	 */
	public static void updateContainerWithID(String updateContainerID, WOContext context) {
        String containerID = "'" + updateContainerID + "'";
        AjaxUtils.javascriptResponse("AUC.update(" + containerID + ");", context);
	}
	
	/**
	 * Creates or updates Ajax response so that the indicated AUC will get updated when the response is processed in the browser.
	 * If the container element does not exist, does nothing.
	 * Adds JavaScript like <code>if ( $('SomeContainerID') != null ) AUC.update('SomeContainerID');</code>
	 * 
	 * @param updateContainerID the HTML ID of the element implementing the AUC
	 * @param context WOContext for response
	 */
	public static void safeUpdateContainerWithID(String updateContainerID, WOContext context) {
        String containerID = "'" + updateContainerID + "'";
        AjaxUtils.javascriptResponse("if ( $(" + containerID + ") != null ) AUC.update(" + containerID + ");", context);
	}
}