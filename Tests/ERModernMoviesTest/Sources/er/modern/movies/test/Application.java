package er.modern.movies.test;

import webobjectsexamples.businesslogic.rentals.common.User;
import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.navigation.ERXNavigationManager;

public class Application extends ERXApplication {
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");
		setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
        // handle jar resources in development
        // http://www.mail-archive.com/webobjects-dev@lists.apple.com/msg44507.html
        if (isDirectConnectEnabled()) {
            registerRequestHandler(new JarResourceRequestHandler(), "_wr_");
            registerRequestHandler(new JarResourceRequestHandler(), "wr");
        }
	}
	
    @Override
    public void finishInitialization() {
    	super.finishInitialization();
    	
    	// Setup main navigation
    	ERXNavigationManager.manager().configureNavigation();
    	
    	// Modify model to enable ERCPreference
    	ERCoreBusinessLogic.sharedInstance().addPreferenceRelationshipToActorEntity(
    	        User.ENTITY_NAME);
    }
}
