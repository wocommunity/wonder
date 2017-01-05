package er.ajax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOForm;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSPathUtilities;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.appserver.ERXWOContext;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.components._private.ERXWOForm;

/**
 * <p>AjaxModalDialog is a modal dialog window based on ModalBox (see below for link).  It differs from AjaxModalContainer
 * in that it handles submitting forms and updating the container contents.  It also looks more like an OS X modal
 * dialog if you consider that to be a benefit.</p>
 * 
 * <p>The AjaxModalDialog is not rendered where it is located in your page.  Because of this, it should not be physically 
 * nested in a form if it uses form input (needs a form), as it will be  rendered outside of the form.  If you want to have
 * such a dialog, place the AjaxModalDialog outside of the form and use an AjaxModalDialogOpener in the form.</p>
 * 
 * <p>Don't use AjaxModalDialogs inside of a repetition.  That will create a set of identical dialogs and will likely cause 
 * then to display the wrong data.  Instead, declare the dialogs outside of the repetition.  Then INSIDE the repetition use 
 * an AjaxModalDialogOpener for the link to open the dialog.  The action method of the AjaxModalDialogOpener should cache the
 * current item from the repetition in an instance variable.  The AjaxModalDialog should then look at that instance variable
 * to determine what data it should show.</p>
 * 
 * <p>The links shown to open the dialog can come from two sources:
 * <ul>
 * <li>the label binding</li>
 * <li>an in-line ERXWOTemplate named "link".  This allows for images etc to be used to open the dialog</li>
 * </ul>
 *
 * <p>The contents for the modal dialog can come from four sources:
 * <ul>
 * <li>in-line, between the open in close tags</li>
 * <li>externally from an in-line WOComponent</li>
 * <li>externally from an action method (see action binding)</li>
 * <li>externally from an named WOcomponent (see pageName binding)</li>
 * </ul>
 * 
 * <p>To cause the dialog to be closed in an Ajax action method, use this:
 * <code>
 * AjaxModalDialog.close(context());
 * </code>
 * </p>
 *  
 * <p>To cause the contents of the dialog to be updated in an Ajax action method, use this:
 * <code>
 * AjaxModalDialog.update(context());
 * </code>
 * </p>
 * 
 * <p>The modal dialog is opened by calling a JavaScript function.  While this is normally done from an onclick
 * handler, you can call it directly.  The function name is openAMD_&lt;ID&gt;():
 * <code>
 * openAMD_MyDialogId();
 * </code>
 * </p>
 * 
 * @binding action action method returning the contents of the dialog box
 * @binding pageName name of WOComponent for the contents of the dialog box
 * @binding label the text for the link that opens the dialog box
 * @binding title Title to be displayed in the ModalBox window header, also used as title attribute of link opening dialog
 * @binding linkTitle Title to be used as title attribute of link opening dialog, title is used if this is not present
 *
 * @binding width integer Width in pixels, use -1 for auto-width
 * @binding height integer Height in pixels, use -1 for auto-height. When set Modalbox will operate in 'fixed-height' mode. 
 * @binding centerVertically optional, if true the dialog is centered vertically on the page instead of appearing at the top
 * 
 * @binding open if true, the container is rendered already opened, the default is false
 * @binding showOpener if false, no HTML is generated for the link, button etc. to open this dialog, it can only be opened from
 * 			custom  JavaScript (see below).  The default is true
 * @binding enabled if false, nothing is rendered for this component.  This can be used instead of wrapping this in a WOConditional.
 *          The default is true.
 * @binding ignoreNesting optional, if true and this dialog is nested inside another, no warning will be output
 *
 * @binding onOpen server side method that runs before the dialog is opened, the return value is discarded
 * @binding onClose server side method that runs before the dialog is closed, the return value is discarded.
 *                  This will be executed if the page is reloaded, but not if the user navigates elsewhere.
 * @binding closeUpdateContainerID the update container to refresh when onClose is called
 * @binding onCloseBeforeUpdate if the given function returns true, the update container named in closeUpdateContainerID
 *                  is updated.  This is to allow conditional updating, e.g. not updating when the dialog is simply dismissed.
 * @binding clickOnReturnId optional, ID of clickable HTML element to click when the Return key is pressed.  This is ignored
 * 					if a clickable element has the focus
 * @binding clickOnEscId optional, ID of clickable HTML element to click when the Esc key is pressed.  This is ignored
 * 					if a clickable element has the focus but overrides the locked setting
 *  
 * @binding id HTML id for the link activating the modal dialog
 * @binding class CSS class for the link activating the modal dialog
 * @binding style CSS style for the link activating the modal dialog
 *
 * @binding overlayClose true | false Close modal box by clicking on overlay. Default is true.
 * @binding locked if true, suppresses the close window link, prevents Esc key and overlay from closing dialog.  Default is false,
 *          true implies overlayClose false.  If clickOnEscId is bound, this allows Esc to do something regardless of the locked binding
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
 * @binding autoFocusing true | false, Toggles auto-focusing for form elements. Disable it for long text pages.  Add the class MB_notFocusable to
 *          any inputs you want excluded from focusing.
 *
 * @binding beforeLoad client side method, fires right before loading contents into the ModalBox. If the callback function returns false, content loading will skipped. This can be used for redirecting user to another MB-page for authorization purposes for example.
 * @binding afterLoad client side method, fires after loading content into the ModalBox (i.e. after showing or updating existing window).
 * @binding beforeHide client side method, fires right before removing elements from the DOM. Might be useful to get form values before hiding modalbox.
 * @binding afterHide client side method, fires after hiding ModalBox from the screen.
 * @binding afterResize client side method, fires after calling resize method.
 * @binding onShow client side method, fires on first appearing of ModalBox before the contents are being loaded.
 * @binding onUpdate client side method, fires on updating the content of ModalBox (on call of Modalbox.show method from active ModalBox instance).
 * 
 * @see AjaxModalDialogOpener
 * @see <a href="http://www.wildbit.com/labs/modalbox">Modalbox Page</a>
 * @see <a href="http://code.google.com/p/modalbox/">Google Group</a>
 * @author chill
 * 
 * TODO handle href to static content
 * TODO make dialog draggable
 * TODO lock dialog open unless closed by content
 * TODO add transitioning to other contents without closing dialog
 */
public class AjaxModalDialog extends AjaxComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	/** JavaScript to execute on the client to close the modal dialog */
	public static final String Close = "AMD.close();";
	
	/** Element ID suffix indicating an Open Dialog action. */
	public static final String Open_ElementID_Suffix = ".open";
	
	/** Element ID suffix indicating an C Dialog action. */
	public static final String Close_ElementID_Suffix = ".close";
	
	
	private boolean _open;
	private WOComponent _actionResults;
	private AjaxModalDialog outerDialog;
	private boolean hasWarnedOnNesting = false;
	private WOComponent previousComponent;
	private String ajaxComponentActionUrl;
	
	private static final Logger log = LoggerFactory.getLogger(AjaxModalDialog.class);

	public AjaxModalDialog(WOContext context) {
		super(context);
	}

	@Override
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
	 * Call this method to have a JavaScript response returned that opens the modal dialog.
	 * The title of the dialog will be what it was when rendered.
	 *
	 * @param context the current WOContext
	 * @param id the HTML ID of the AjaxModalDialog to open
	 */
	public static void open(WOContext context, String id) {
		AjaxUtils.javascriptResponse(openDialogFunctionName(id) + "();", context);
	}
	
	/**
	 * Call this method to have a JavaScript response returned that opens the modal dialog.
	 * The title of the dialog will be the passed title.  This is useful if the script to
	 * open this dialog was rendered without the title or with an incorrect title.
	 *
	 * @param context the current WOContext
	 * @param id the HTML ID of the AjaxModalDialog to open
	 * @param title the title for the AjaxModalDialog
	 */
	public static void open(WOContext context, String id, String title) {
		AjaxUtils.javascriptResponse(openDialogFunctionName(id) + "(" + AjaxValue.javaScriptEscaped(title) + ");", context);
	}
	
	/**
	 * Returns the JavaScript function name for the function to open the AjaxModalDialog with
	 * the specified ID.
	 *
	 * @param id the HTML ID of the AjaxModalDialog to open
	 * @return JavaScript function name for the function to open the AjaxModalDialog
	 */
	public static String openDialogFunctionName(String id) {
		return "openAMD_" + id;
	}
	
	/**
	 * Call this method to have a JavaScript response returned that closes the modal dialog.
	 *
	 * @param context the current WOContext
	 */
	public static void close(WOContext context) {
		// If the page structure changes as a result of changes from the dialog, and the dialog is no longer
		// part of the page, the onClose action can't be triggered when the dialog closes.  To ensure that this
		// action is always called, it is invoked on the server side when the message is sent to the client to hide
		// the dialog.  In theory this should not be needed, in practice page state can get messy.
		currentDialog(context).closeDialog();
		AjaxUtils.javascriptResponse(AjaxModalDialog.Close, context);
	}

	/**
	 * Call this method to have a JavaScript response returned that updates the contents of the modal dialog.
	 *
	 * @param context the current WOContext
	 * @param title optional new title for the updated dialog
	 */
	public static void update(WOContext context, String title) {
		AjaxModalDialog currentDialog = currentDialog(context);
		StringBuilder js = new StringBuilder(300);
		js.append("Modalbox.show('");
		js.append(currentDialog.openDialogURL(context));
		js.append("', ");
		
		NSMutableDictionary options = currentDialog.createModalBoxOptions();
		if (title != null) {
			options.setObjectForKey(AjaxUtils.quote(title), "title");
		}
		AjaxOptions.appendToBuffer(options, js, context);
		js.append(");\n");
		AjaxUtils.javascriptResponse(js.toString(), context);

		// Register the id of this component on the page in the request so that when 
		// it comes time to cache the context, it knows that this area is an Ajax updating area
		AjaxUtils.setPageReplacementCacheKey(context, currentDialog._containerID(context));
	}

	/**
	 * Call this method to have a JavaScript response returned that updates the contents of the modal dialog.
	 *
	 * @param context the current WOContext
	 * @param newContent the new content for the updated dialog
	 * @param title optional new title for the updated dialog
	 */
	public static void update(WOContext context, WOComponent newContent, String title) {
		AjaxModalDialog currentDialog = currentDialog(context);
		currentDialog._actionResults = newContent;
		update(context, title);
	}

	public void setCurrentDialogInPageIfNecessary(WOActionResults results, WORequest request, WOContext context) {
		if (AjaxUtils.isAjaxRequest(request) && results instanceof WOComponent && results != context.page()) {
			ERXResponseRewriter.pageUserInfo((WOComponent)results).setObjectForKey(this, AjaxModalDialog.class.getName());
		}
	}

	/**
	 * @param context the current WOContext
	 * @return the AjaxModalDialog currently being processed
	 * @throws RuntimeException if no AjaxModalDialog is currently being processed
	 */
	public static AjaxModalDialog _currentDialog(WOContext context) {
		AjaxModalDialog currentDialog = (AjaxModalDialog) ERXWOContext.contextDictionary().objectForKey(AjaxModalDialog.class.getName());
		if (currentDialog == null) {
			currentDialog = (AjaxModalDialog) ERXResponseRewriter.pageUserInfo(context).objectForKey(AjaxModalDialog.class.getName());
		}
		return currentDialog;
	}

	/**
	 * @param context the current WOContext
	 * @return the AjaxModalDialog currently being processed
	 * @throws RuntimeException if no AjaxModalDialog is currently being processed
	 */
	public static AjaxModalDialog currentDialog(WOContext context) {
		AjaxModalDialog currentDialog = _currentDialog(context);
		if (currentDialog == null) {
			throw new RuntimeException("Attempted to get current AjaxModalDialog when none active.  Check your page structure.");
		}
		return currentDialog;
	}

	/**
	 * @param context the current WOContext
	 * @return true if an AjaxModalDialog currently being processed
	 */
	public static boolean isInDialog(WOContext context) {
		return _currentDialog(context) != null;
	}
	
	/**
	 * Start of R-R loop.  awakes the components from action if action is bound.
	 *
	 * @see com.webobjects.appserver.WOComponent#awake()
	 */
	@Override
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
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		ajaxComponentActionUrl = AjaxUtils.ajaxComponentActionUrl(context());
		if (isOpen()) {
			try {
				pushDialog();
				if (_actionResults != null) {
					pushActionResultsIntoContext(context);
					try {
						_actionResults.takeValuesFromRequest(request, context);
					}
					finally {
						popActionResultsFromContext(context);
					}
				}
				else {
					super.takeValuesFromRequest(request, context);
				}
			}
			finally {
				popDialog();
			}
		}
	}

	/**
	 * Only handle this phase if the modal box is open or it is our action (opening the box).  
	 * Overridden to include result returned by action binding if bound.
	 *
	 * @see #close(WOContext)
	 * @see #update(WOContext, String)
	 * @see com.webobjects.appserver.WOComponent#takeValuesFromRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		ajaxComponentActionUrl = AjaxUtils.ajaxComponentActionUrl(context());
		pushDialog();		
		try {
			WOActionResults result = null;
			if (shouldHandleRequest(request, context)) {
					result = super.invokeAction(request, context);
			}
			else if (isOpen()) {
				if (_actionResults != null) {
					pushActionResultsIntoContext(context);
					try {
						result = _actionResults.invokeAction(request, context);
					}
					finally {
						popActionResultsFromContext(context);
					}
				}
				else {
					result = super.invokeAction(request, context);
				}
			}
			setCurrentDialogInPageIfNecessary(result, request, context);

			return result;
		}
		finally {
			popDialog();
		}
	}
	
	/**
	 * Removes Open_ElementID_Suffix or Close_ElementID_Suffix before evaluating senderID.
	 *
	 * @see er.ajax.AjaxComponent#shouldHandleRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
	 *
	 * @return <code>true</code> if this request is for this component
	 */
	@Override
    protected boolean shouldHandleRequest(WORequest request, WOContext context) {
		String elementID = context.elementID();
		String senderID = context.senderID();

		if (senderID != null && (senderID.endsWith(Open_ElementID_Suffix) || senderID.endsWith(Close_ElementID_Suffix))) {
			senderID = NSPathUtilities.stringByDeletingPathExtension(senderID);
		}

		boolean shouldHandleRequest = elementID != null && (elementID.equals(senderID) || 
				elementID.equals(ERXAjaxApplication.ajaxSubmitButtonName(request)));
		return shouldHandleRequest;
	}

	/**
	 * Handles the open and close dialog actions.
	 *
	 * @see er.ajax.AjaxComponent#handleRequest(com.webobjects.appserver.WORequest, com.webobjects.appserver.WOContext)
	 *
	 * @return null or dialog contents
	 */
	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOActionResults response = null;
		String modalBoxAction = NSPathUtilities.pathExtension(context.senderID());

		if ("close".equals(modalBoxAction)) {
			// This update can't be done in the closeDialog() method as that also gets called from close(WOContext) and
			// and Ajax update is not taking place.  If the page structure changes, this update will not take place,
			// but the correct container ID is on the URL and the update will still happen thanks to the magic in
			// AjaxResponse.AjaxResponseDelegate
			String closeUpdateContainerID = AjaxUpdateContainer.updateContainerID((String) valueForBinding("closeUpdateContainerID"));
			if (closeUpdateContainerID != null) {
				AjaxUpdateContainer.setUpdateContainerID(request, closeUpdateContainerID);
			}
	
			// This needs to happen AFTER setting up for an update so that AjaxUtils.appendScriptHeaderIfNecessary
			// knows if the script header is needed or not.  Doing this before and setting up a JS response in 
			// the onClose callback, resulted in plain text getting injected into the page.
			closeDialog();
		}
		else if ("open".equals(modalBoxAction) && !isOpen()) {
			openDialog();

			// If there is an action or pageName binding, we need to cache the result of calling that so that
			// the awake, takeValues, etc. messages can get passed onto it
			if (hasBinding("action")) {
				_actionResults = (WOComponent) valueForBinding("action");
				_actionResults._awakeInContext(context);
			}
			else if (hasBinding("pageName")) {
				_actionResults = pageWithName((String)valueForBinding("pageName"));
				_actionResults._awakeInContext(context);
			}

			// WOForm expects that it is inside a WODynamicGroup and relies on WODynamicGroup having setup the WOContext to correctly 
			// generate the URL.  It should not (IMO), but it does.  If you have anything before the WebObject tag for the form 
			// (a space, a carriage return, text, HTML tags, anything at all), then the WO parser creates a WODynamicGroup to hold that.  
			// If  the WebObject tag for the form is the very first thing in the template, then the WODynamicGroup is not created and 
			// invalid URLs are generated rendering the submit controls non-functional.  Throw an exception so the developer knows what is 
			// wrong and can correct it.
			if (_actionResults != null && (_actionResults.template() instanceof WOForm ||
			  		                       _actionResults.template() instanceof ERXWOForm)) {
				throw new RuntimeException(_actionResults.name() + " is used as contents of AjaxModalDialog, but starts with WOForm tag.  " +
						"Action elements inside the dialog will not function.  Add a space at the start or end of " + _actionResults.name() + ".html");
			}
				
		}

		if (isOpen()) {
			response = AjaxUtils.createResponse(request, context);
			
			// Register the id of this component on the page in the request so that when 
			// it comes time to cache the context, it knows that this area is an Ajax updating area
			AjaxUtils.setPageReplacementCacheKey(context, _containerID(context));

			if (_actionResults != null) {
				pushActionResultsIntoContext(context);
				try {
					_actionResults.appendToResponse((WOResponse) response, context);
				}
				finally {
					popActionResultsFromContext(context);
				}
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
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		ajaxComponentActionUrl = AjaxUtils.ajaxComponentActionUrl(context());
		if (context.isInForm()) {
			log.warn("The AjaxModalDialog should not be used inside of a WOForm ({}" +
					") if it contains any form inputs or buttons.  Remove this AMD from this form, add a form of its own. Replace it with " +
					"an AjaxModalDialogOpener with a dialogID that matches the ID of this dialog.", ERXWOForm.formName(context, "- not specified -"));
					log.warn("    page: {}", context.page());
					log.warn("    component: {}", context.component());
		}
		
		if( ! booleanValueForBinding("enabled", true)) {
			return;
		}

		// If this is not an Ajax request, the page has been reloaded.  Try to recover state
		if (isOpen() && ! AjaxRequestHandler.AjaxRequestHandlerKey.equals(context().request().requestHandlerKey())) {
			closeDialog();
		}

		// If we are open, but the request is not for us, don't render the content.
		// This shouldHandleRequest prevents showing an open dialog in the page when 
		// an AUC refreshes
		if (isOpen() && shouldHandleRequest(context.request(), context)) {
			if (_actionResults != null) {
				throw new RuntimeException("Unexpected call to appendToResponse");
			}
			try {
				pushDialog();
				super.appendToResponse(response, context);
			}
			finally {
				popDialog();
			}
		}
		else {
			boolean showOpener = booleanValueForBinding("showOpener", true);
			
			if (showOpener) {
				response.appendContentString("<a href=\"javascript:void(0)\"");
				appendTagAttributeToResponse(response, "id", id());
				appendTagAttributeToResponse(response, "class", valueForBinding("class", null));
				appendTagAttributeToResponse(response, "style", valueForBinding("style", null));
				if (hasBinding("linkTitle")) {
					appendTagAttributeToResponse(response, "title", valueForBinding("linkTitle", null));
				} else {
					appendTagAttributeToResponse(response, "title", valueForBinding("title", null));
				}
				
				// onclick calls the script below
				response.appendContentString(" onclick=\"");
				response.appendContentString(openDialogFunctionName(id()));
				response.appendContentString("(); return false;\" >");	
				
				if (hasBinding("label")) {
					response.appendContentString((String) valueForBinding("label"));
				} else {
					// This will append the contents of the ERXWOTemplate named "link"
					super.appendToResponse(response, context);
				}
				response.appendContentString("</a>");
			}
			
			// This script can also be called directly by other code to show the modal dialog
			AjaxUtils.appendScriptHeader(response);
			response.appendContentString(openDialogFunctionName(id()));
			response.appendContentString(" = function(titleBarText) {\n");
			appendOpenModalDialogFunction(response, context);
			response.appendContentString("}\n");
			
			// Auto-open
			if (booleanValueForBinding("open", false)) {
				response.appendContentString(openDialogFunctionName(id()));
				response.appendContentString("();\n");
			}
			AjaxUtils.appendScriptFooter(response);
			
			// normally this would be done in super, but we're not always calling super here
			addRequiredWebResources(response);
		}
	}

	/**
	 * Appends function body to open the modal dialog window.
	 * 
	 * @see #openDialogFunctionName(String)
	 * 
	 * @param response WOResponse to append to
	 * @param context WOContext of response
	 */
	protected void appendOpenModalDialogFunction(WOResponse response, WOContext context) {
		response.appendContentString("    options = ");
		AjaxOptions.appendToResponse(createModalBoxOptions(), response, context);
		response.appendContentString(";\n");
		response.appendContentString("    if (titleBarText) options.title = titleBarText;\n");
		response.appendContentString("    Modalbox.show('");
		response.appendContentString(openDialogURL(context));
		response.appendContentString("', options);\n");
	}

	/**
	 * End of R-R loop.  Puts the components from action to sleep if action is bound.
	 *
	 * @see com.webobjects.appserver.WOComponent#sleep()
	 */
	@Override
	public void sleep() {
		if (_actionResults != null) {
			_actionResults._sleepInContext(context());
		}
		ajaxComponentActionUrl = null;
		super.sleep();
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
	 * If the dialog is open, calls the method bound to onClose (if any), and marks the dialog state as closed.  
	 * This method can get called if the page gets reloaded so be careful modifying the response if 
	 * <code>! AjaxRequestHandler.AjaxRequestHandlerKey.equals(context().request().requestHandlerKey())</code>
	 */
	public void closeDialog() {
		if (isOpen()) {
			if (hasBinding("onClose")) {
				valueForBinding("onClose");
			}

			setOpen(false);
			_actionResults = null;
		}			
	}

	/**
	 * @see er.ajax.AjaxComponent#_containerID(com.webobjects.appserver.WOContext)
	 *
	 * @return id()
	 */
	@Override
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
//	public String updateContainerID() {
//		return id() + "Updater";
//	}
	
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
		ajaxOptionsArray.addObject(new AjaxOption("centerVertically", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("overlayClose", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("height", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("method", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("params", AjaxOption.DICTIONARY));
		ajaxOptionsArray.addObject(new AjaxOption("loadingString", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("closeString", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("closeValue", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("locked", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("overlayOpacity", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("overlayDuration", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("slideDownDuration", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("slideUpDuration", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("resizeDuration", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("inactiveFade", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("transitions", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("autoFocusing", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("clickOnReturnId", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("clickOnEscId", AjaxOption.STRING));
		
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
			serverUpdate = " AUL.request('" + closeDialogURL(context()) + "', null, null, null);";
		}
		else {
			String onCloseBeforeUpdate = (String)valueForBinding("onCloseBeforeUpdate", "AMD.shouldRefreshCloseUpdateContainer");
			String verifyUpdateContainerRefreshScript = " if (" + onCloseBeforeUpdate + ") { ";
			serverUpdate = verifyUpdateContainerRefreshScript + "AUL._update('" + closeUpdateContainerID + "', '" + closeDialogURL(context())  + "', null, null, null); } else { new Ajax.Request('" + closeDialogURL(context()) + "'); }";
		}

		if (hasBinding("afterHide")) {
			String afterHide = (String) valueForBinding("afterHide");
			int openingBraceIndex = afterHide.indexOf('{');
			if (openingBraceIndex > -1) {
				serverUpdate = "function() {" + serverUpdate + " " + afterHide.substring(openingBraceIndex + 1);
			}
			else
				throw new RuntimeException("Don't know how to handle afterHide value '" + afterHide + "', did you forget to wrap it in function() { ...}?");
		}
		else {
			serverUpdate = "function(v) { " + serverUpdate + '}';
		}
		ajaxOptionsArray.addObject(new AjaxConstantOption("afterHide", serverUpdate, AjaxOption.SCRIPT));

		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);

		return options;
	}

	/**
	 * @see er.ajax.AjaxComponent#addRequiredWebResources(com.webobjects.appserver.WOResponse)
	 */
	@Override
	protected void addRequiredWebResources(WOResponse response) {
		addScriptResourceInHead(response, "prototype.js");
		addScriptResourceInHead(response, "wonder.js");
		addScriptResourceInHead(response, "effects.js");
		addScriptResourceInHead(response, "modalbox.js");
		ERXResponseRewriter.addStylesheetResourceInHead(response, context(), cssFileFrameworkName(), cssFileName());
	}
	
    
    /**
     * @return value for modalboxCSS binding, or default of "modalbox.css"
     */
    protected String cssFileName() {
    	return (String)valueForBinding("modalboxCSS", "modalbox.css");
    }
    
    /**
     * @return value for modalboxCSSFramework binding, or default of "Ajax"
     */
    protected String cssFileFrameworkName() {
    	return (String)valueForBinding("modalboxCSSFramework", "Ajax");
    }

	/**
	 * Stash this dialog instance in the context so we can access it from the static methods.  If there is one AMD 
	 * nested in another (a rather dubious thing to do that we warn about but it may have its uses), we need to remember 
	 * the outer one while processing this inner one
	 * @see #popDialog()
	 */
	protected void pushDialog() {
		outerDialog = (AjaxModalDialog) ERXWOContext.contextDictionary().objectForKey(AjaxModalDialog.class.getName());
		ERXWOContext.contextDictionary().setObjectForKey(this, AjaxModalDialog.class.getName());
		if ( ! hasWarnedOnNesting && outerDialog != null) {
			hasWarnedOnNesting = true;
			if (! ERXComponentUtilities.booleanValueForBinding(this, "ignoreNesting", false))
			{
				log.warn("AjaxModalDialog {} is nested inside of {}. Are you sure you want to do this?", id(), outerDialog.id());
			}
		}		
	}
	
	/**
	 * Remove this dialog instance from the context, replacing the previous one if any.
	 * @see #pushDialog()
	 */
	protected void popDialog() {
		if (outerDialog != null) {
			ERXWOContext.contextDictionary().setObjectForKey(outerDialog, AjaxModalDialog.class.getName());
		}
		else {
			ERXWOContext.contextDictionary().removeObjectForKey(AjaxModalDialog.class.getName());
		}
	}
	
	/**
	 * Make _actionResults (result of the action binding) the current component in context for WO processing.
	 * Remembers the current component so that it can be restored.
	 * @param context WOContext to push _actionResults into
	 * @see #popActionResultsFromContext(WOContext)
	 */
	protected void pushActionResultsIntoContext(WOContext context) {
		previousComponent = context.component();
		context._setCurrentComponent(_actionResults);
	}
	
	/**
	 * Sets the current component in context to the one there before pushActionResultsIntoContext
	 * was called.
	 * @param context WOContext to restore previous component in
	 * @see #pushActionResultsIntoContext(WOContext)
	 */
	protected void popActionResultsFromContext(WOContext context) {
		context._setCurrentComponent(previousComponent);
	}
		
	/**
	 * @param context WOContext to create URL in
	 * @return URL to invoke when the dialog is opened
	 */
	protected String openDialogURL(WOContext context) {
		return new StringBuilder(ajaxComponentActionUrl).append(Open_ElementID_Suffix).toString();
	}
	
	/**
	 * @param context WOContext to create URL in
	 * @return URL to invoke when the dialog is closed
	 */
	protected String closeDialogURL(WOContext context) {
		return new StringBuilder(ajaxComponentActionUrl).append(Close_ElementID_Suffix).toString();
	}
}
