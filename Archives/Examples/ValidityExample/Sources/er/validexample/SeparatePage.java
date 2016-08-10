package er.validexample;
//
// SeparatePage.java: Class file for WO Component 'SeparatePage'
// Project ValidityExample
//
// Created by msacket on Mon Jun 11 2001
//

import com.gammastream.validity.GSVEngine;
import com.gammastream.validity.GSVExceptionableComponent;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSValidation;

public class SeparatePage extends GSVExceptionableComponent {

    public EOClassDescription description;
    public User  newUser;
    public boolean success = false;
    public String verifyPassword = null;


    public SeparatePage(WOContext context) {
        super(context);
        description = EOClassDescription.classDescriptionForEntityName("User");
        newUser = (User)description.createInstanceWithEditingContext(null, null);
    }
    
    public WOComponent addAgain(){
        success = false;
        newUser = (User)description.createInstanceWithEditingContext(null, null);
        return null;
    }
    
    public WOComponent add() {
        boolean passwordsMatch = false;
        NSValidation.ValidationException ve = null;
        try{
            //first we need to make sure that the password and verify password match.
            passwordsMatch = ( (newUser.password() != null) ? newUser.password().equals(verifyPassword) : true);
            
            //now save the changes
            newUser.validateForInsert();
            success = true;
            verifyPassword = null;
        }catch(NSValidation.ValidationException e){
            System.out.println(e);
            ve = e;
        }finally{
            if( !passwordsMatch ){
                ve = GSVEngine.exceptionByAppendingErrorToException("The Password and verified password did not match.", "Verify", ve);
            }
            if( ve != null ){
                raiseGSVException(ve);
                success = false;
                return null;
            }
        } 
         return null;
    }
    
    
    public Main goToHomePage() {
        return (Main)pageWithName("Main");
    }
}
