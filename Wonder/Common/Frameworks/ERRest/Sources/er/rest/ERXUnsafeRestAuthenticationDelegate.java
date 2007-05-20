package er.rest;

import er.extensions.ERXApplication;

/**
 * ERXUnsafeRestAuthenticationDelegate should NEVER be used in production.  This is an auth delegate
 * implementation designed to allow you to explore the features of ERRest without having to actually write
 * a custom delegate.  This implementation just returns true if you are in development mode.  It will 
 * always return false if ERXApplication.erxApplication().isDevelopmentMode() is false.
 *  
 * @author mschrag
 */
public class ERXUnsafeRestAuthenticationDelegate implements IERXRestAuthenticationDelegate {
	public boolean authenticate(ERXRestContext context) {
		boolean developmentMode = ERXApplication.erxApplication().isDevelopmentMode();
		if (!developmentMode) {
			ERXRestRequestHandler.log.error("You are attempting to use ERXUnsafeRestAuthenticationDelegate outside of development mode!  Denying authentication.");
		}
		return developmentMode;
	}

}