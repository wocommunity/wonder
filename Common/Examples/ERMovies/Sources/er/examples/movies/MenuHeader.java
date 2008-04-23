// Created by Direct to Web's Project Builder Wizard
package er.examples.movies;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORedirect;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.directtoweb.QueryPageInterface;

import er.extensions.ERXEC;
import er.extensions.ERXUtilities;

public class MenuHeader extends WOComponent {
    public String entityNameInList;
    private String _manipulatedEntityName;

    public MenuHeader(WOContext aContext) {
        super(aContext);
    }

    public String manipulatedEntityName() {
        if (_manipulatedEntityName == null) {
            WOComponent currentPage = context().page();
            _manipulatedEntityName = D2W.factory().entityNameFromPage(currentPage);
        }
        return _manipulatedEntityName;
    }

    public void setManipulatedEntityName(String newValue) {
        _manipulatedEntityName = newValue;
    }

    public NSArray visibleEntityNames() {
        return D2W.factory().visibleEntityNames(session());
    }

    public WOComponent findEntityAction() {
        QueryPageInterface newQueryPage = D2W.factory().queryPageForEntityNamed(_manipulatedEntityName, session());
        return (WOComponent) newQueryPage;
    }

    public WOComponent newObjectAction() {
        WOComponent nextPage = null;
        try {
            EditPageInterface epi = D2W.factory().editPageForNewObjectWithEntityNamed(_manipulatedEntityName, session());
            epi.setNextPage(context().page());
            nextPage = (WOComponent) epi;
        } catch (IllegalArgumentException e) {
            ErrorPageInterface epf = D2W.factory().errorPage(session());
            epf.setMessage(e.toString());
            epf.setNextPage(context().page());
            nextPage = (WOComponent) epf;
        }
        return nextPage;
    }

    public WOComponent logout() {
        WOComponent redirectPage = pageWithName("WORedirect");
        ((WORedirect) redirectPage).setUrl(D2W.factory().homeHrefInContext(context()));
        session().terminate();
        return redirectPage;
    }

    public WOComponent homeAction() {
        return D2W.factory().defaultPage(session());
    }

    public WOComponent showWebAssistant() {
        return D2W.factory().webAssistantInContext(context());
    }

    public boolean isWebAssistantEnabled () {
        return D2W.factory().isWebAssistantEnabled();
    }

    /////////////////////////////////////////////////////////
    // Added by Max
    /////////////////////////////////////////////////////////

    public WOComponent newMovieWithPageConfiguration(String pageConfig) {
        EOEditingContext ec = ERXEC.newEditingContext();
        EOEnterpriseObject movie = ERXUtilities.createEO("Movie", ec);
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed(pageConfig,
                                                                                           session());
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
}
