package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXWOContext;

/**
 * <p>AjaxModalDialog is a modal dialog window based on ModalBox (see below for link).  It differs from AjaxModalContainer
 * in that it handles submitting forms and updating the container contents.  It also looks more like an OS X modal
 * dialog if you consider that to be a benefit.</p>
 * 
 * <p>The links shown to open the dialog can some from three sources:
 * <ul>
 * <li>the label binding</li>
 * <li>an in-line ERXWOTemplate named "link".  This allows for images etc to be used to open the dialog</li>
 * </ul></p>
 *
 * <p>The contents for the modal dialog can some from three sources:
 * <ul>
 * <li>in-line, between the open in close tags</li>
 * <li>externally (i.e. from another template), from an in-line WOComponent</li>
 * <li>externally (i.e. from another template), from an action method</li>
 * </ul></p>
 * 
 * <p>To cause the dialog to be closed in an Ajax action method, use this:
 * <code>
 * AjaxModalDialog.close(context());
 * </code>
 * </p>
 *  
 * <p>To cause the contents of the dialog to be updated in an Ajax action method, use this:
 * <code>
 * AjaxModalDialog.udpate(context());
 * </code>
 * <br /><b>NOTE: this does not work if you use the action binding.  You must manage your own updating if you use this binding.</b>
 * </p>
 * 
 * @binding action returns the contents of the dialog box
 * @binding label the text for the link that opens the dialog box
 * @binding title Title to be displayed in the ModalBox window header, also used as title attribute of link opening dialog
 *
 * @binding width integer Width in pixels. Default is 
 * @binding height integer Height in pixels. Then set Modalbox will operate in 'fixed-height' mode. 
 * 			Otherwise the height will be calculated to fit content.
 * 
 * @binding onOpen server side method that runs before the dialog is opened, the return value is discarded
 * @binding onClose server side method that runs before the dialog is opened, the return value is discarded.
 *                  This will be executed if the page is reloaded, but not if the user navigates elsewhere.
 * @binding closeUpdateContainerID the update container to refresh when onClose is called
 * 
 * @binding id HTML id for the link activating the modal dialog
 * @binding class CSS class for the link activating the modal dialog
 * @binding style CSS style for the link activating the modal dialog
 *
 * @binding overlayClose true | false Close modal box by clicking on overlay. Default is true.
 * @binding method get | post. Method of passing variables to a server. Default is 'get'.
 * @binding params {} Collection of parameters to pass on AJAX request. Should be URL-encoded. See PassingFormValues for details.
 * 
 * @binding loadingString string The message to show during loading. Default is "Please wait. Loading...".
 * @binding closeString Defines title attribute for close window link. Default is "Close window".
 * @binding closeValue Defines the string for close link in the header. Default is '&times;'
 * 
 * @binding overlayOpacity Overlay opacity. Must be between 0-1. Default is .65.
 * @binding overlayDuration Overlay fade in/out duration in seconds.
 * @binding slideDownDuration Modalbox appear slide down effect in seconds.
 * @binding slideUpDuration Modalbox hiding slide up effect in seconds.
 * @binding resizeDuration Modalbox resize duration in seconds.
 * @binding inactiveFade true | false, Toggles Modalbox window fade on inactive state.
 * @binding transitions true | false, Toggles transition effects. Transitions are enabled by default.
 * @binding autoFocusing true | false, Toggles auto-focusing for form elements. Disable it for long text pages.
 *
 * @binding beforeLoad client side method, fires right before loading contents into the ModalBox. If the callback function returns false, content loading will skipped. This can be used for redirecting user to another MB-page for authorization purposes for example.
 * @binding afterLoad client side method, fires after loading content into the ModalBox (i.e. after showing or updating existing window).
 * @binding beforeHide client side method, fires right before removing elements from the DOM. Might be useful to get form values before hiding modalbox.
 * @binding afterHide client side method, fires after hiding ModalBox from the screen.
 * @binding afterResize client side method, fires after calling resize method.
 * @binding onShow client side method, fires on first appearing of ModalBox before the contents are being loaded.
 * @binding onUpdate client side method, fires on updating the content of ModalBox (on call of Modalbox.show method from active ModalBox instance).
 * 
 * @see <a href="http://www.wildbit.com/labs/modalbox"/>Modalbox Page</a>
 * @see <a href="http://code.google.com/p/modalbox/">Google Group</a>
 * @author chill
 * 
 * TODO handle href to static content
 * TODO make dialog draggable
 * TODO lock dialog open unless closed by content
 * TODO add transitioning to other contents without closing dialog
 */
public class AjaxModalDialog extends AjaxComponent {

	/** JavaScript to execute on the client to close the modal dialog */
	public static final String Close = "AMD.close();";

	private boolean _open;
	private WOComponent _actionResults;

	public AjaxModalDialog(WOContext context) {
		super(context);
	}

	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	public boolean isOpen() {
		return _open;
	}

	public void setOpen(boolean open) {
		_open = open;
	}

	/**
	 * Call this method to have a JavaScript response returned that closes the modal dialog.
	 *
	 * @param context the current WOContext
	 */
	public static void close(WOContext context) {
		AjaxUtils.javascriptResponse(AjaxModalDialog.Close, context);
	}

	/**
	 * Call this method to have a JavaScript response returned that updates the contents of the modal dialog.
	 * Note that this does not work with the action binding.  You need to manage your own AjaxUpdateContainer
	 * if you use an action method for the contents of the dialog.
	 *
	 * @param context the current WOContext
	 */
	public static void update(WOContext context) {
		AjaxModalDialog thisDialog = (AjaxModalDialog) ERXWOContext.contextDictionary().objectForKey(AjaxModalDialog.class.getName());
		AjaxUtils.javascriptResponse(thisDialog.updateContainerID() + "Update();", context);
	}

	/**
	 * Call this method to have a JavaScript response returned that updates the title of the modal dialog.
	 *
	 * @param context the current WOContext
	 * @param title the new title for the dialog window
	 */
	public static void setTitle(WOContext context, String title) {
		AjaxUtils.javascriptResponse("$wi('MB_caption').innerHTML='" + title + "';", context);
	}

	/**
	 * Start of R-R loop.  awakes the components from action if action is bound.
	 *
	 * @see com.webobjects.appserver.WOComponent#awake()
	 */
	public void awake() {
		super.awake();
		if (_actionResults != null) {
			_actionResults._awakeInContext(context());
		}
	}

	/**
	 * Only handle this phase if the modal box is open.  Also includes result returned by action binding if bound.
	 *
	 * @see com.webobjects.appserver.WOComponent#takeValuesFromRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
	 */
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (isOpen()) {
			if (_actionResults != null) {
				context._setCurrentComponent(_actionResults);
				_actionResults.takeValuesFromRequest(request, context);
			}
			else {
				super.takeValuesFromRequest(request, context);
			}
		}
	}

	/**
	 * Only handle this phase if the modal box is open or it is our action (opening the box).  
	 * Overridden to include result returned by action binding if bound.
	 *
	 * @see #close(WOContext)
	 * @see #update(WOContext)
	 * @see com.webobjects.appserver.WOComponent#takeValuesFromRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
	 */
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		try {
			// Stash this component in the context so we can access it from the static methods.
			ERXWOContext.contextDictionary().setObjectForKey(this, AjaxModalDialog.class.getName());

			WOActionResults result = null;
			if (AjaxUtils.shouldHandleRequest(request, context, _containerID(context))) {
				result = super.invokeAction(request, context);
			}
			else if (isOpen()) {
				if (_actionResults != null) {
					context._setCurrentComponent(_actionResults);
					result = _actionResults.invokeAction(request, context);
				}
				else {
					super.invokeAction(request, context);
				}
			}

			return result;

		}
		finally {
			// Remove this component from the context
			ERXWOContext.contextDictionary().removeObjectForKey(AjaxModalDialog.class.getName());
		}

	}

	/**
	 * Handles the open and close dialog actions.
	 *
	 * @see er.ajax.AjaxComponent#handleRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
	 *
	 * @return null or dialog contents
	 */
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOActionResults response = null;
		String modalBoxAction = request.stringFormValueForKey("modalBoxAction");

		if ("close".equals(modalBoxAction) && isOpen()) {
			closeDialog();
			String closeUpdateContainerID = AjaxUpdateContainer.updateContainerID((String) valueForBinding("closeUpdateContainerID"));
			if (closeUpdateContainerID != null) {
				AjaxUpdateContainer.setUpdateContainerID(request, closeUpdateContainerID);
			}
		}
		else if ("open".equals(modalBoxAction) && !isOpen()) {
			openDialog();
			// Register the id of this component on the page in the request so that when 
			// it comes time to cache the context, it knows that this area is an Ajax updating area
			AjaxUtils.setPageReplacementCacheKey(context, _containerID(context));

			// If there is an action binding, we need to cache the result of calling that so that
			// the awake, takeValues, etc. messages can get passed onto it
			if (hasBinding("action")) {
				_actionResults = (WOComponent) valueForBinding("action");
				_actionResults._awakeInContext(context);
			}
		}

		if (isOpen()) {
			response = AjaxUtils.createResponse(request, context);
			if (_actionResults != null) {
				context._setCurrentComponent(_actionResults);
				_actionResults.appendToResponse((WOResponse) response, context);
			}
			else {
				// This loads the content from the default ERWOTemplate (our component contents that are not
				// in the "link" template.
				super.appendToResponse((WOResponse) response, context);
			}
		}

		return response;
	}

	/**
	 * This has two modes.  One is to generate the link that opens the dialog.  The other is to return the contents
	 * of the dialog (the result returned by action binding is handled in handleRequest, not here).
	 *
	 * @see er.ajax.AjaxComponent#appendToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
	 */
	public void appendToResponse(WOResponse response, WOContext context) {

		// If this is not an Ajax request, the page has been reloaded.  Try to recover state
		if (!context().request().requestHandlerKey().equals(AjaxRequestHandler.AjaxRequestHandlerKey)) {
			closeDialog();
		}

		if (isOpen()) {
			if (_actionResults != null) {
				throw new RuntimeException("Unexpected call to appendToResponse");
			}
			super.appendToResponse(response, context);
		}
		else {
			response.appendContentString("<a href=\"javascript:void(0)\"");

			WOComponent component = context.component();

			appendTagAttributeToResponse(response, "id", id());
			appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
			appendTagAttributeToResponse(response, "style", valueForBinding("style", component));
			appendTagAttributeToResponse(response, "title", valueForBinding("title", component));

			response.appendContentString(" onclick=\"Modalbox.show('");
			response.appendContentString(AjaxUtils.ajaxComponentActionUrl(component.context()));
			response.appendContentString("?modalBoxAction=open");
			response.appendContentString("', ");
			AjaxOptions.appendToResponse(createModalBoxOptions(), response, context);
			response.appendContentString("); return false;\" >");

			if (hasBinding("label")) {
				// normally this would be done in super, but we're not supering here
				addRequiredWebResources(response);
				response.appendContentString((String) valueForBinding("label"));
			}
			else {
				// This will append the contents of the ERXWOTemplate named "link"
				super.appendToResponse(response, context);
			}

			response.appendContentString("</a>");
		}
	}

	/**
	 * End of R-R loop.  Puts the components from action to sleep if action is bound.
	 *
	 * @see com.webobjects.appserver.WOComponent#sleep()
	 */
	public void sleep() {
		super.sleep();
		if (_actionResults != null) {
			_actionResults._sleepInContext(context());
		}
	}

	/**
	 * Calls the method bound to onOpen (if any), and marks the dialog state as open.
	 */
	public void openDialog() {
		if (hasBinding("onOpen")) {
			valueForBinding("onOpen");
		}

		setOpen(true);
	}

	/**
	 * Calls the method bound to onClose (if any), and marks the dialog state as closed.
	 */
	public void closeDialog() {
		if (hasBinding("onClose")) {
			valueForBinding("onClose");
		}

		setOpen(false);
		_actionResults = null;
	}

	/**
	 * @see er.ajax.AjaxComponent#_containerID(com.webobjects.appserver.WOContext)
	 *
	 * @return id()
	 */
	protected String _containerID(WOContext context) {
		return id();
	}

	/**
	 * @return the value bound to id or an manufactured string if id is not bound
	 */
	public String id() {
		return hasBinding("id") ? (String) valueForBinding("id") : ERXWOContext.safeIdentifierName(context(), false);
	}

	/**
	 * Returns the ID of the AjaxUpdateContainer that wraps the in-line contents of this dialog.
	 * 
	 * @return  id() + "Updater"
	 */
	public String updateContainerID() {
		return id() + "Updater";
	}

	/**
	 * Returns the template name for the ERXWOComponentContent: null to show the dialog (default) contents
	 * and "link" to show the link contents
	 * 
	 * @return null or "link"
	 */
	public String templateName() {
		return !isOpen() && !hasBinding("label") ? "link" : null;
	}

	/**
	 * @return binding values converted into Ajax options for ModalBox
	 */
	protected NSMutableDictionary createModalBoxOptions() {
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("title", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("width", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("overlayClose", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("height", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("method", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("params", AjaxOption.DICTIONARY));
		ajaxOptionsArray.addObject(new AjaxOption("loadingString", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("closeString", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("closeValue", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("overlayOpacity", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("overlayDuration", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("slideDownDuration", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("slideUpDuration", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("resizeDuration", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("inactiveFade", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("transitions", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("autoFocusing", AjaxOption.BOOLEAN));

		// IMPORTANT NOTICE. Each callback gets removed from options of the ModalBox after execution
		ajaxOptionsArray.addObject(new AjaxOption("beforeLoad", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("afterLoad", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("beforeHide", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("afterResize", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onShow", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onUpdate", AjaxOption.SCRIPT));

		// JS to notify server when the dialog box is closed.  This needs to be added to anything
		// bound to afterHide
		String closeUpdateContainerID = AjaxUpdateContainer.updateContainerID((String) valueForBinding("closeUpdateContainerID"));
		String serverUpdate;
		if (closeUpdateContainerID == null) {
			serverUpdate = " AUL.request('" + AjaxUtils.ajaxComponentActionUrl(context()) + "', null, null, 'modalBoxAction=close');";
		}
		else {
			serverUpdate = " AUL._update('" + closeUpdateContainerID + "', '" + AjaxUtils.ajaxComponentActionUrl(context()) + "', null, null, 'modalBoxAction=close');";
		}
		//String serverUpdate = " new Ajax.Request('"+ AjaxUtils.ajaxComponentActionUrl(context()) + "', {asynchronous:1, evalScripts:true, parameters: 'modalBoxAction=close'});";
		if (hasBinding("afterHide")) {
			String afterHide = (String) valueForBinding("afterHide");
			int closingBraceIndex = afterHide.lastIndexOf('}');
			if (closingBraceIndex > -1) {
				serverUpdate = afterHide.substring(0, closingBraceIndex) + serverUpdate + '}';
			}
			else
				throw new RuntimeException("Don't know how to handle afterHide value '" + afterHide + "', did you forget to wrap it in function() { ...}?");
		}
		else {
			serverUpdate = "function(v) { " + serverUpdate + '}';
		}
		ajaxOptionsArray.addObject(new AjaxOption("afterHide", serverUpdate, AjaxOption.SCRIPT));

		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);

		return options;
	}

	/**
	 * @see er.ajax.AjaxComponent#addRequiredWebResources(com.webobjects.appserver.WOResponse)
	 */
	protected void addRequiredWebResources(WOResponse response) {
		addScriptResourceInHead(response, "prototype.js");
		addScriptResourceInHead(response, "wonder.js");
		addScriptResourceInHead(response, "effects.js");
		addScriptResourceInHead(response, "modalbox.js");
		addStylesheetResourceInHead(response, "modalbox.css");
	}

}
