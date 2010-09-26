package com.secretpal.components.person;

import com.secretpal.SPUtilities;
import com.secretpal.components.application.Main;
import com.secretpal.components.application.SPPage;
import com.secretpal.model.SPPerson;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.eof.ERXEC;

public class SPForgotPasswordPage extends SPPage {
	public String _emailAddress;
	
    public SPForgotPasswordPage(WOContext context) {
        super(context);
    }
    
    @Override
    protected boolean isAuthenticationRequired() {
    	return false;
    }
    
    public WOActionResults sendEmail() {
    	EOEditingContext editingContext = ERXEC.newEditingContext();
    	SPPerson person = SPPerson.fetchSPPerson(editingContext, SPPerson.EMAIL_ADDRESS.is(_emailAddress));
    	if (person != null) {
    		SPUtilities.sendResetPasswordEmail(person, context(), session().errors());
    	}
    	session().notifications().addNotice("An password reset email has been sent to the email address you provided.");
    	return pageWithName(Main.class);
    }
}