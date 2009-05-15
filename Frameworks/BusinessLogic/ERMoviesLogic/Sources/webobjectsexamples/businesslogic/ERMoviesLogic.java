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
        String jar = bundle.bundlePath() + "/Resources/movies.jar";
        System.setProperty("Movies.URL", "jdbc:derby:jar:(" + jar + ")movies");
        System.setProperty("Rentals.URL", "jdbc:derby:jar:(" + jar + ")movies");
    }

}
