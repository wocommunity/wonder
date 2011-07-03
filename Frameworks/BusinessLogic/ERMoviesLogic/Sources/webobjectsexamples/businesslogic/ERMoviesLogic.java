package webobjectsexamples.businesslogic;

import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSLog;

import er.extensions.ERXFrameworkPrincipal;
import er.extensions.foundation.ERXProperties;

/**
 *
 * @property webobjectsexamples.ERMoviesLogic.useEmbeddedH2Database
 */
public class ERMoviesLogic extends ERXFrameworkPrincipal {
    
    static {
        setUpFrameworkPrincipalClass(ERMoviesLogic.class);
    }

    @Override
    public void finishInitialization() {
    	
    	if (ERXProperties.booleanForKeyWithDefault("webobjectsexamples.ERMoviesLogic.useEmbeddedH2Database", false)) {
    		NSBundle bundle = NSBundle.bundleForClass(ERMoviesLogic.class);

    		String file = bundle.bundlePathURL().getPath() + "/Resources/Movies";
    		String h2URL = "jdbc:h2:file:" + file;
    		NSLog.out.appendln("ERMoviesLogic.finishInitialization, re-writing connection url to use embedded H2 db at: " + h2URL);
    		System.setProperty("Movies.URL", h2URL);
    		System.setProperty("Rentals.URL", h2URL);
    		System.setProperty("ERAttachment.URL", h2URL);
    		System.setProperty("ERTaggable.URL", h2URL);
    	}
		
    }

}
