package er.modern.movies.test;

import webobjectsexamples.businesslogic.rentals.common.User;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2W;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSLog;

import er.directtoweb.ERD2WDirectAction;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXStringUtilities;
import er.modern.movies.test.components.Main;


public class DirectAction extends ERD2WDirectAction {
	public DirectAction(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults defaultAction() {
		return pageWithName(Main.class.getName());
	}
	
    /**
     * Checks if a page configuration is allowed to render.
     * Provide a more intelligent access scheme as the default just returns false. And
     * be sure to read the javadoc to the super class.
     * @param pageConfiguration
     * @return
     */
    @Override
    protected boolean allowPageConfiguration(String pageConfiguration) {
        return false;
    }
    
    /**
     * This will log in anybody. If the given user name is null, "admin" is
     * used. If the user name does not match an existing user, one will be
     * created.
     * 
     * @return application home page
     */
	public WOActionResults loginAction() {
		String username = request().stringFormValueForKey("username");
		if (ERXStringUtilities.stringIsNullOrEmpty(username)) {
		    // use a default
		    username = "admin";
		}
		String password = request().stringFormValueForKey("password");
		
		// this is a demo, so everybody is welcome 
		EOEditingContext ec = ERXEC.newEditingContext();
        User user = User.fetchUser(ec, User.USERNAME_KEY, username);
        // if user does not yet exist, create it
		if (user == null) {
		    user = User.createUser(ec, User.AdministratorAccessLevel, "", username);
		    ec.saveChanges();
		}
		session().takeValueForKeyPath(user, "objectStore.user");
		
		NSLog.out.appendln("***DirectAction.loginAction - username: " + username + " : password: " + password + "***");
		
		return D2W.factory().defaultPage(session());
	}
	
}
