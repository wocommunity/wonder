package org.svgobjects.examples.ezforms;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;

public class Main extends WOComponent  {
    private Applicant contact;
    private EOEnterpriseObject spouse;
    public String accountType;
    
    public Main(WOContext context) {
        super(context);
    }
    
    /*
    * accessors
    */
    public Applicant contact() {
        if (contact == null) {
            EOEditingContext defaultEditingContext = session().defaultEditingContext();
            EOClassDescription classDescription = EOClassDescription.classDescriptionForEntityName("Applicant");
            contact = (Applicant) classDescription.createInstanceWithEditingContext(defaultEditingContext, null);
            defaultEditingContext.insertObject(contact);
        }

        return contact;
    }
    
    public void setContact(Applicant newContact) {
        contact = newContact;
    }
    
    public EOEnterpriseObject spouse() {
        if (spouse == null) {
            EOEditingContext defaultEditingContext = session().defaultEditingContext();
            EOClassDescription classDescription = EOClassDescription.classDescriptionForEntityName("Spouse");
            spouse = classDescription.createInstanceWithEditingContext(defaultEditingContext, null);
            defaultEditingContext.insertObject(spouse);
        }

        return spouse;
    }

    public void setSpouse(Applicant newSpouse) {
        spouse = newSpouse;
    }
    

    public EOEnterpriseObject account() {
        EOEnterpriseObject account = (EOEnterpriseObject) contact.valueForKey("account");

        if (account == null) {
            EOEditingContext defaultEditingContext = session().defaultEditingContext();
            EOClassDescription classDescription = EOClassDescription.classDescriptionForEntityName("Account");
            account = classDescription.createInstanceWithEditingContext(defaultEditingContext, null);
            defaultEditingContext.insertObject(account);
            contact.addObjectToBothSidesOfRelationshipWithKey(account, "account");
        }

        return account;
    }

    /*
    * web actions
    */
    public void submit() {
    	EOEditingContext defaultEditingContext = session().defaultEditingContext();

	// validate spouce details
	try {
            contact.validateForSave();
	    spouse.validateForSave(); 
	    
	    // add the spouce to the applicant
	    contact.addObjectToBothSidesOfRelationshipWithKey(spouse, "spouse");
	} catch (NSValidation.ValidationException e) {}
		
	// debug
        NSLog.debug.appendln("Main: contact details: " + contact);
    }

    public WOComponent print() {
        WOComponent nextPage = pageWithName("EZ1040");
        nextPage.takeValueForKey(contact, "contact");
        return nextPage;
    }
}