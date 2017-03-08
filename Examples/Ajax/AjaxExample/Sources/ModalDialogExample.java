
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSValidation;

import er.ajax.AjaxModalDialog;
import er.ajax.example.Company;
import er.ajax.example.Employee;
import er.extensions.eof.ERXQ;


/**
 * Example usage of AjaxModalDialog.  Not intended as a best practice example of WO coding...
 *
 * @see AjaxModalDialog
 */
public class ModalDialogExample extends WOComponent {

	private boolean isSecondConfirmation;
	private Employee employee;
	protected String errorMessages;
	
	public final NSArray<String> companyNames = new NSArray<>(new String[]{
			"Acme Ajax, Inc.",
			"Betty's Baubles, LLC",
			"Gimcrack, Intl.", 
			 "Sally's Seashore Seashells",
			 "The Fu Bar"
	});
	
	public NSMutableArray<Company> companies = new NSMutableArray<>();

	public ModalDialogExample(WOContext context) {
        super(context);
        
        // Fetch/create some sample data to edit
        EOEditingContext ec = session().defaultEditingContext();
        
        for (int i = 0; i < companyNames.count(); i++) {
			String name = companyNames.objectAtIndex(i);
	        Company company = Company.fetchCompany(ec, Company.NAME_KEY, name);
	        if (company == null) {
	        	company = Company.createCompany(ec, name);
	        }
	        companies.addObject(company);
		}
        ec.saveChanges();
        
        employee = Employee.fetchEmployee(ec, ERXQ.and(ERXQ.equals(Employee.FIRST_NAME_KEY, "Bill"),
        										       ERXQ.equals(Employee.LAST_NAME_KEY, "Wratchit")));
        if (employee == null) {
            Company company = Company.fetchCompany(ec, Company.NAME_KEY, companyNames.objectAtIndex(2));
            employee = Employee.createEmployee(ec, "Bill", "Wratchit", company);
        }
        ec.saveChanges();
	}

	
    
    /** 
     * Ajax method that is called when deletion is confirmed in the Ajax Dialog with the Yes hyperlink
     */
    public WOActionResults deleteIt() {
    	NSLog.out.appendln("deleteIt called");
    	return null;
    }
    
    
    /** 
     * Ajax method that is called when deletion is confirmed in the Ajax Dialog with the Yes2 hyperlink.
     * This shows how to update the dialog contents and how to close the box from Java.
     */
    public WOActionResults deleteIt2() {
    	NSLog.out.appendln("deleteIt2 called");
    	isSecondConfirmation = ! isSecondConfirmation;
    	
    	if (isSecondConfirmation) {
    		AjaxModalDialog.update(context(), "Think again...");
    	} else {
    		AjaxModalDialog.close(context());
    	}

    	return null;
    }
    
    
    /**
     * Binding for WOString that is in AjaxUpdateContainer "ConfirmationMessage". Returns an initial or
     * repeated confirmation message.
     */
    public String confirmationMessage()
    {
    	NSLog.out.appendln("confirmationMessage called");
    	return isSecondConfirmation ? "Are you really, really, really sure you want to delete this?" : "Are you sure you want to delete this?";
    }

    
    /**
     * Shows how to bind the action method to a component that needs initialization before
     * being shown in the dialog.  This component contains form elements.
     */
    public WOActionResults editEmployee() {
    	NSLog.out.appendln("editEmployee called");
    	ModalDialogEmployeeEditor result = (ModalDialogEmployeeEditor) pageWithName(ModalDialogEmployeeEditor.class.getCanonicalName());
    	result.setEmployee(employee());
    	
    	return result;
    }
    
    
    /**
     * Shows the use of an AjaxSubmitButton to cancel a dialog.
     */
    public WOActionResults cancelEdit() {
    	NSLog.out.appendln("ModalDialogExample cancelEdit");
    	errorMessages = null;
    	employee().editingContext().revert();
    	AjaxModalDialog.close(context());
    	
    	return null;
    }
    
    
    /**
     * Shows the use of an AjaxSubmitButton to submit a form, validate the contents, and either update or close the dialog.
     */
    public WOActionResults saveEdit() {
    	NSLog.out.appendln("ModalDialogExample saveEdit");
    	try {
        	errorMessages = null;
    		employee().editingContext().saveChanges();
    		AjaxModalDialog.close(context());
    	} catch (NSValidation.ValidationException e) {
        	errorMessages = e.getLocalizedMessage();
        	if (e.additionalExceptions().count() > 0) {
        		errorMessages += "<br/>" + ((NSArray)e.additionalExceptions().valueForKey("localizedMessage")).componentsJoinedByString("<br/>");
        	}
        	AjaxModalDialog.update(context(), "Edit");
    	}
    	
    	return null;
    }

    
	/**
	 * @return the employee
	 */
	public Employee employee() {
		return employee;
	}

	/**
	 * @param person the employee to set
	 */
	public void setEmployee(Employee person) {
		employee = person;
	}
    
	public String firstName() {
		return employee().firstName();
	}
	
	/**
	 * This is used instead of employee.firstName so that we can see which method is called
	 */
	public void setFirstName(String name) {
		NSLog.out.appendln("ModalDialogExample setFirstName to " +name);
		employee().setFirstName(name);
	}
    
	public String lastName() {
		return employee().lastName();
	}
	
	/**
	 * This is used instead of employee.lastName so that we can see which method is called
	 */
	public void setLastName(String name) {
		NSLog.out.appendln("ModalDialogExample setLastName to " +name);
		employee().setLastName(name);
	}
	
	
    /**
     * Binding for WOString that is in AjaxUpdateContainer "ValidationMessage". Returns any error messages
     * from trying to save employee().
     */
	public String errorMessages() {
		return errorMessages;
	}
    
    
}