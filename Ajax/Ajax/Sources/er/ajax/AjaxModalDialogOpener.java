package er.ajax;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;

import er.extensions.ERXWOContext;

/**
 * <p>Generates a link to open a specific AjaxModalDialog.  This is useful when you want to physically separate the modal dialog from what
 * opens it, for example if you want a modal dialog containing a form to have an opener inside of another form.  Normally you will want to
 * bind showOpener=false; on the AjaxModalDialog that this opens.</p>
 * 
 * <p>The link shown can come from two sources:
 * <ul>
 * <li>the label binding</li>
 * <li>child elements</li>
 * </ul></p>
 * 
 * @binding dialogId required, ID of the AjaxModalDialog to open
 * @binding label the text for the link that opens the dialog box, if this used the child elements are ignored
 * @binding linkTitle used as title attribute of link opening dialog
 * @binding title the Title to be displayed in the ModalBox window header, can override what the dialog was created with
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
		response.appendContentString("<a href=\"javascript:void(0)\"");
		appendTagAttributeToResponse(response, "id", id());
		appendTagAttributeToResponse(response, "class", valueForBinding("class", null));
		appendTagAttributeToResponse(response, "style", valueForBinding("style", null));
		appendTagAttributeToResponse(response, "title", valueForBinding("linkTitle", null));
		
		// onclick calls the script that opens the AjaxModalDialog
		response.appendContentString(" onclick=\"");
		response.appendContentString(AjaxModalDialog.openDialogFunctionName(modalDialogId()));
		
		// Override for dialog name
		response.appendContentString("(");	
		if (hasBinding("title")) {
			response.appendContentString("'");	
			response.appendContentString((String) valueForBinding("title"));
			response.appendContentString("'");	
		}		
		response.appendContentString("); return false;\" >");	

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
	
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}

}