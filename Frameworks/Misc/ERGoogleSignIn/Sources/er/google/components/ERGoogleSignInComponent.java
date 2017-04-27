package er.google.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;
import er.google.utils.ERGoogleSignInUtilities;

public class ERGoogleSignInComponent extends ERXStatelessComponent {
	public ERGoogleSignInComponent(WOContext context) {
		super(context);
	}
	
	public String clientID() {
    	String name = stringValueForBinding("clientIDName");
    	if (name != null) {
    		return ERGoogleSignInUtilities.clientID(name);
    	}
    	else {
    		return stringValueForBinding("clientID", ERGoogleSignInUtilities.clientID());
    	}
    }
    
    public boolean includePlatformScript() {
    	return booleanValueForBinding("includePlatformScript", true);
    }
    
    public boolean includeInitializationScript() {
    	return booleanValueForBinding("includeInitializationScript", true);
    }
}
