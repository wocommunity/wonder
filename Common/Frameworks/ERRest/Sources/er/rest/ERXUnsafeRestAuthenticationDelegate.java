package er.rest;

import er.extensions.ERXApplication;

public class ERXUnsafeRestAuthenticationDelegate implements IERXRestAuthenticationDelegate {
	public boolean authenticate(ERXRestContext context) {
		boolean developmentMode = ERXApplication.erxApplication().isDevelopmentMode();
		if (!developmentMode) {
			ERXRestRequestHandler.log.error("You are attempting to use ERXUnsafeRestAuthenticationDelegate outside of development mode!  Denying authentication.");
		}
		return developmentMode;
	}

}