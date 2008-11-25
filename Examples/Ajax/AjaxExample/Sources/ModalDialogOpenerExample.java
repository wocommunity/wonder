
import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.ajax.*;
import er.ajax.example.*;

/**
 * Example usage of AjaxModalDialogOpener.  Not intended as a best practice example of WO coding...
 *
 * @see AjaxModalDialog
 */
public class ModalDialogOpenerExample extends ModalDialogExample {
	
	public Company selectedCompany;
	public Company aCompany;
	
	public ModalDialogOpenerExample(WOContext context) {
        super(context);
    }
    
    public WOActionResults cancelEdit() {
		employee().editingContext().revert();
    	return context().page();
    }
    
    public WOActionResults saveEdit() {
    	try {
        	errorMessages = null;
    		employee().editingContext().saveChanges();
    	} catch (NSValidation.ValidationException e) {
        	errorMessages = e.getLocalizedMessage();
        	if (e.additionalExceptions().count() > 0) {
        		errorMessages += "<br/>" + ((NSArray)e.additionalExceptions().valueForKey("localizedMessage")).componentsJoinedByString("<br/>");
        	}
    	}
    	
    	return context().page();
    }
    
    /**
     * Ajax action method to select the company from the modal dialog.
     */
    public void selectCompany(){
    	employee().addObjectToBothSidesOfRelationshipWithKey(selectedCompany, Employee.COMPANY_KEY);
		AjaxModalDialog.close(context());
    }
    
    public void rememberCurrentCompany() {
    	selectedCompany = employee().company();
	}
    
    /**
     * Updates the editor forms when the dialog is closed.
     */
    public void updatePage() {
		if (AjaxRequestHandler.AjaxRequestHandlerKey.equals(context().request().requestHandlerKey())) {
	    	AjaxUtils.javascriptResponse("UIUpdater1Update(); UIUpdater2Update();", context());
		}
    }

}