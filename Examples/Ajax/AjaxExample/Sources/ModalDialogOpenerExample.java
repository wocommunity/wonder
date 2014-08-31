
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSValidation;

import er.ajax.AjaxModalDialog;
import er.ajax.AjaxRequestHandler;
import er.ajax.AjaxUtils;
import er.ajax.example.Company;
import er.ajax.example.Employee;
import er.extensions.eof.ERXS;

/**
 * Example usage of AjaxModalDialogOpener.  Not intended as a best practice example of WO coding...
 *
 * @see AjaxModalDialog
 */
public class ModalDialogOpenerExample extends ModalDialogExample {

	public Company selectedCompany;
	public Company aCompany;
	public int companyIndex;
	public Company companyToEdit;
	public String validationMessage;

	public ModalDialogOpenerExample(WOContext context) {
        super(context);
    }
    
    @Override
	public WOActionResults cancelEdit() {
		employee().editingContext().revert();
    	return context().page();
    }
    
    @Override
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
	    	AjaxUtils.javascriptResponse("UIUpdater1Update(); UIUpdater2Update();  CompanyListUpdaterUpdate();", context());
		}
    }
    
    public void willOpen() {
    	System.out.println("willOpen called by AjaxModalDialogOpener");
    }
	
    public String editCompanyTitle() {
    	return "Edit " + aCompany.name();
	}
    
    public void selectCompanyToEdit() {
    	companyToEdit = aCompany;
    	System.out.println("selectCompanyToEdit: " + companyToEdit.name());
    }
    
    public void saveCompanyChanges() {
    	System.out.println("saveCompanyChanges: " + companyToEdit.name());
    	try {
        	companyToEdit.editingContext().saveChanges();
        	AjaxModalDialog.close(context());
    	}
    	catch (Exception e)
    	{
    		validationMessage = e.getMessage();
        	AjaxModalDialog.update(context(), "Edit");
    	}
    }
    
    public void deleteCompany() {
    	System.out.println("deleteCompany: " + companyToEdit.name());
    	try {
    		EOEditingContext ec = companyToEdit.editingContext();
    		companyToEdit.editingContext().revert();
    		companyToEdit.editingContext().deleteObject(companyToEdit);
    		companyToEdit.editingContext().saveChanges();
        	companies = new NSMutableArray<Company>(Company.fetchAllCompanies(ec, ERXS.ascs(Company.NAME_KEY)));
        	AjaxModalDialog.close(context());
    	}
    	catch (Exception e)
    	{
    		validationMessage = e.getMessage();
        	AjaxModalDialog.update(context(), "Edit");
    	}
    }
    
    
    public void cleanupAfterCompanyEdit() {
    	System.out.println("cleanupAfterCompanyEdit");
    	if (companyToEdit.editingContext() != null) {
        	companyToEdit.editingContext().revert();
    	}
    	validationMessage = null;
    	companyToEdit = null;
    	updatePage();
    }
    
    
}