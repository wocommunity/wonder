//
// AllTogetherPage.java: Class file for WO Component 'AllTogetherPage'
// Project ValidityExample
//
// Created by msacket on Mon Jun 11 2001
//

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.gammastream.validity.*;

public class AllTogetherPage extends GSVExceptionableComponent {

    EOClassDescription description;
    User  newUser;
    boolean success = false;
    public String verifyPassword = null;

    public AllTogetherPage(WOContext context) {
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
            
            passwordsMatch = (verifyPassword!=null && verifyPassword.equals(newUser.password()));
            //now save the changes
            newUser.validateForInsert();
            success = true;
        }catch(NSValidation.ValidationException e){
            ve = e;
        }finally{
            if( !passwordsMatch ){
                ve = GSVEngine.exceptionByAppendingErrorToException("The Password and verified password did not match or were left blank.", "Verify", ve);
            }
            if( ve != null ){
                raiseGSVException(ve);
                success = false;
                return null;
            }
        } 
        return null;
    }
    
    public Main goToHomePage(){
        Main nextPage = (Main)pageWithName("Main");
        return nextPage;
    }
}