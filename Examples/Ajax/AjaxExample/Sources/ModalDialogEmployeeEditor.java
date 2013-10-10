import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSValidation;

import er.ajax.AjaxModalDialog;
import er.ajax.example.Employee;

public class ModalDialogEmployeeEditor extends WOComponent {
	
	private Employee employee;
	private String errorMessages;
	
    public ModalDialogEmployeeEditor(WOContext context) {
        super(context);
    }

    

    public WOActionResults cancelEdit() {
    	NSLog.out.appendln("ModalDialogEmployeeEditor cancelEdit");
		employee().editingContext().revert();
    	AjaxModalDialog.close(context());
    	
    	return null;
    }
    
    public WOActionResults saveEdit()
    {
    	NSLog.out.appendln("ModalDialogEmployeeEditor saveEdit");
    	try {
        	errorMessages = null;
    		employee().editingContext().saveChanges();
    		AjaxModalDialog.close(context());
    	} catch (NSValidation.ValidationException e) {
        	errorMessages = e.getLocalizedMessage();
        	if (e.additionalExceptions().count() > 0) {
        		errorMessages += "<br/>" + ((NSArray)e.additionalExceptions().valueForKey("localizedMessage")).componentsJoinedByString("<br/>");
        	}
        	AjaxModalDialog.update(context());
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
		NSLog.out.appendln("ModalDialogEmployeeEditor setFirstName to " +name);
		employee().setFirstName(name);
	}
    
	public String lastName() {
		return employee().lastName();
	}
	
	/**
	 * This is used instead of employee.lastName so that we can see which method is called
	 */
	public void setLastName(String name) {
		NSLog.out.appendln("ModalDialogEmployeeEditor setLastName to " +name);
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
