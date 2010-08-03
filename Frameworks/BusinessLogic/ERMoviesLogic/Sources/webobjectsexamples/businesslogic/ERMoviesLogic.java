package webobjectsexamples.businesslogic;

import com.webobjects.foundation.NSBundle;

import er.extensions.ERXFrameworkPrincipal;

public class ERMoviesLogic extends ERXFrameworkPrincipal {
    
    static {
        setUpFrameworkPrincipalClass(ERMoviesLogic.class);
    }

    @Override
    public void finishInitialization() {
        NSBundle bundle = NSBundle.bundleForClass(ERMoviesLogic.class);
        // AK: deprecation, I know,,,
//        String file = bundle.bundlePath() + "/Resources/Movies";
//        System.setProperty("Movies.URL", "jdbc:h2:file:" + file);
//        System.setProperty("Rentals.URL", "jdbc:h2:file:" + file);
    }

}
