//
// Session.java
// Project ERMovies
//
// Created by max on Thu Feb 27 2003
//
package er.examples.movies;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

public class Session extends ERXSession {

    public WOComponent newMovieWithPageConfiguration(String pageConfig) {
        EOEditingContext ec = ERXEC.newEditingContext();
        EOEnterpriseObject movie = ERXUtilities.createEO("Movie", ec);
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed(pageConfig,
                                                                                           this);
        epi.setObject(movie);
        epi.setNextPage(context().page());
        return (WOComponent)epi;
    }

    public WOComponent newMovieTabInspectPage() {
        return newMovieWithPageConfiguration("EditTabMovie");
    }

    public WOComponent newMovieWizardPage() {
        return newMovieWithPageConfiguration("EditWizardMovie");
    }

    public WOComponent findAMovie() {
        QueryPageInterface qpi = (QueryPageInterface)D2W.factory().pageForConfigurationNamed("SearchMovie",
                                                                                             this);
        return (WOComponent)qpi;
    }

    public WOComponent findAnActor() {
        QueryPageInterface qpi = (QueryPageInterface)D2W.factory().pageForConfigurationNamed("FindTalent",
                                                                                             this);
        return (WOComponent)qpi;
    }

    public WOComponent listAllMovies() {
        ListPageInterface lpi = (ListPageInterface)D2W.factory().pageForConfigurationNamed("ListAllMovies",
                                                                                           this);
        EODataSource ds = new EODatabaseDataSource(ERXEC.newEditingContext(), "Movie");
        lpi.setDataSource(ds);
        return (WOComponent)lpi;
    }
    
    public WOComponent homePage() {
        // Reset the nav state when going home
        ERXNavigationManager.manager().navigationStateForSession(this).setState(NSArray.EmptyArray);
        return D2W.factory().defaultPage(this);
    }    
}
