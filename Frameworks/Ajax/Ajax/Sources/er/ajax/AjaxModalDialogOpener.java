package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXWOContext;

/**
 * <p>Generates a link to open a specific AjaxModalDialog.  This is useful when you want to physically separate the modal dialog from what
 * opens it, for example if you want a modal dialog containing a form to have an opener inside of another form.  It is also useful if you
 * want to use a dialog in a repetition.  Using an AjaxModalDialogOpener in the repetition and moving the AjaxModalDialog outside of the
 * repetition will result in only a single rendering of the AjaxModalDialog in the page.  You can also move it before the repetition to 
 * speed up request handling when the dialog is open. Normally you will want to bind showOpener=false; on the AjaxModalDialog that this opens.
 * </p>
 * <p> If you need to do some preparation before the dialog opens, use the action
 * method.This is called synchronously so make it quick!  The action method is useful for things like copying the item from a repetition
 * to use in a dialog that is not nested in the repetition. </p>
 * 
 * <p>The link shown can come from two sources:
 * <ul>
 * <li>the label binding</li>
 * <li>child elements</li>
 * </ul></p>
 * 
 * <p>As the opener functions on the client only, it is possible for it to be rendered when <code>enabled</code> evaluates to 
 * <code>true</code> and clicked later when <code>enabled</code> would evaluate to <code>false</code>.  This condition is checked for
 * when the opener is clicked.  If <code>enabled</code> evaluates to <code>false</code> at that time: the action method
 * is not called, the AjaxModalDialog is not opened, and the onFailure handler (if any) is run.
 * </p>
 * 
 * @binding dialogId required, ID of the AjaxModalDialog to open
 * @binding label the text for the link that opens the dialog box, if this used the child elements are ignored
 * @binding linkTitle used as title attribute of link opening dialog
 * @binding title the Title to be displayed in the ModalBox window header, can override what the dialog was created with
 * @binding action, optional action to call before opening the modal dialog.  
 * @binding enabled if false, nothing is rendered for this component.  This can be used instead of wrapping this in a WOConditional.
 *          The default is true.
 * @binding onFailure optional JavaScript (not a function()!) to run if the opener is clicked and enabled evaluates to false. This can remove the element, show
 * 			an alert, etc.  e.g. onFailure = "alert('This is no longer available');";
 * 
 * @binding id HTML id for the link
 * @binding class CSS class for the link
 * @binding style CSS style for the link
 *
 * @see AjaxModalDialog
 * @see <a href="http://www.wildbit.com/labs/modalbox"/>Modalbox Page</a>
 * @see <a href="http://code.google.com/p/modalbox/">Google Group</a>
 * @author chill
 */
public class AjaxModalDialogOpener extends AjaxComponent {
	
	
    public AjaxModalDialogOpener(WOContext context) {
        super(context);
    }
    
    public boolean isStateless() {
    	return true;
    }
    
	/**
	 * Generate a link that opens the indicated dialog.
	 *
	 * @see er.ajax.AjaxComponent#appendToResponse(com.webobjects.appserver.WOResponse, com.webobjects.appserver.WOContext)
	 */
	public void appendToResponse(WOResponse response, WOContext context) {
		if( ! booleanValueForBinding("enabled", true)) {
			return;
		}
		
		response.appendContentString("<a href=\"javascript:void(0)\"");
		appendTagAttributeToResponse(response, "id", id());
		appendTagAttributeToResponse(response, "class", valueForBinding("class", null));
		appendTagAttributeToResponse(response, "style", valueForBinding("style", null));
		appendTagAttributeToResponse(response, "title", valueForBinding("linkTitle", null));
		
		// onclick calls the script that opens the AjaxModalDialog
		response.appendContentString(" onclick=\"");
		
		response.appendContentString("new Ajax.Request('");
		response.appendContentString(AjaxUtils.ajaxComponentActionUrl(context()));
		response.appendContentString("', ");
		AjaxOptions.appendToResponse(ajaxRequestOptions(), response, context);
		response.appendContentString("); ");

		response.appendContentString("return false;\" >");	

		if (hasBinding("label")) {
			response.appendContentString((String) valueForBinding("label"));
		} else {
			// This will append the contents inside of the link
			super.appendToResponse(response, context);
		}
		response.appendContentString("</a>");
	}

	/**
	 * @return the value bound to id or an manufactured string if id is not bound
	 */
	public String id() {
		return hasBinding("id") ? (String) valueForBinding("id") : ERXWOContext.safeIdentifierName(context(), false);
	}
	
	/**
	 * @return the value bound to dialogId
	 */
	public String modalDialogId() {
		return (String) valueForBinding("dialogId");
	}
	
	protected void addRequiredWebResources(WOResponse res) {
	}
	
	/**
	 * Runs action and returns success status if enabled, otherwise returns failed status.
	 */
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		if( booleanValueForBinding("enabled", true)) {
			valueForBinding("action");
		}
		else {
			// Set the response status code to an error code so that the onSuccess callback is not executed
			// If there is an onFailure callback, it will get executed
			// Status 409 is "Conflict" which seemed like the best match for this
			AjaxUtils.createResponse(request, context).setStatus(409);
		}
		
		return null;
	}
	
	
	/**
	 * @return options for Ajax.Request that is made when the link is clicked
	 */
	protected NSMutableDictionary ajaxRequestOptions() {
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("asynchronous", Boolean.FALSE, AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("evalScripts", Boolean.FALSE, AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.FUNCTION_1));
		
		// onSuccess callback handler to open AMD
		StringBuilder sb = new StringBuilder(500);
		sb.append(AjaxModalDialog.openDialogFunctionName(modalDialogId()));
		sb.append("(");	
		
		// Override for dialog name
		if (hasBinding("title")) {	
			sb.append(AjaxValue.javaScriptEscaped(valueForBinding("title")));
		}		
		sb.append(");");
		ajaxOptionsArray.addObject(new AjaxOption("onSuccess", sb.toString(), AjaxOption.FUNCTION_1));

		return AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, this);
	}

}