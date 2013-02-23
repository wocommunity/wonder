package er.divalite.example;

import webobjectsexamples.businesslogic.ERMoviesLogic;

import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSLog;

import er.extensions.appserver.ERXApplication;

public class Application extends ERXApplication {
    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }

    public Application() {
        NSLog.out.appendln("Welcome to " + this.name() + " !");
        /* ** put your initialization code in here ** */
		setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
    }
    
    @Override
    public void finishInitialization() {
    	super.finishInitialization();
    	
        NSBundle bundle = NSBundle.bundleForClass(ERMoviesLogic.class);
        String file = bundle.bundlePath() + "/Resources/Movies";
        System.setProperty("Movies.URL", "jdbc:h2:file:" + file);
        System.setProperty("Rentals.URL", "jdbc:h2:file:" + file);
    }
}
