package er.modern.movies.test;

import java.sql.SQLException;

import org.h2.tools.Server;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.navigation.ERXNavigationManager;
import webobjectsexamples.businesslogic.rentals.common.User;

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

		try {
			Server.createWebServer("-web", "-webAllowOthers", "-webPort",
					"8082").start();
		} catch (SQLException e) {
			System.out.println("Failed to start H2 webserver: "
					+ e.getStackTrace());
		}
    	
    	// Modify model to enable ERCPreference
    	ERCoreBusinessLogic.sharedInstance().addPreferenceRelationshipToActorEntity(
    	        User.ENTITY_NAME);
    }
	
}
