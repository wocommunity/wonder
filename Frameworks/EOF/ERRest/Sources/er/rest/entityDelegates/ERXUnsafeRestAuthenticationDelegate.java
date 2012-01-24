package er.rest.entityDelegates;

import er.extensions.appserver.ERXApplication;

/**
 * ERXUnsafeRestAuthenticationDelegate should NEVER be used in production.  This is an auth delegate
 * implementation designed to allow you to explore the features of ERRest without having to actually write
 * a custom delegate.  This implementation just returns true if you are in development mode.  It will 
 * always return false if ERXApplication.erxApplication().isDevelopmentMode() is false.
 *  
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
public class ERXUnsafeRestAuthenticationDelegate implements IERXRestAuthenticationDelegate {
	public boolean authenticate(ERXRestContext context) {
		boolean developmentMode = ERXApplication.isDevelopmentModeSafe();
		if (!developmentMode) {
			ERXRestRequestHandler.log.error("You are attempting to use ERXUnsafeRestAuthenticationDelegate outside of development mode!  Denying authentication.");
		}
		return developmentMode;
	}

}