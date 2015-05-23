package er.modern.movies.test.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORedirect;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.navigation.ERXNavigationManager;
import er.extensions.appserver.navigation.ERXNavigationState;
import er.extensions.components.ERXComponent;

public class MenuHeader extends ERXComponent {
	
    public MenuHeader(WOContext context) {
        super(context);
    }
    
	// ERXModernNavigationMenu Support
	
    public NSKeyValueCoding navigationContext() {
    	
        NSKeyValueCoding context = (NSKeyValueCoding)session().objectForKey("navigationContext");

        if (context().page() instanceof D2WPage) {
            context = ((D2WPage)context().page()).d2wContext();
        }

        //log.debug(ERXNavigationManager.manager().navigationStateForSession(session()));
        if(context == null) {
            context = new NSMutableDictionary<Object, String>();
            session().setObjectForKey(context, "navigationContext");
        }
        @SuppressWarnings("unused")
		ERXNavigationState state = ERXNavigationManager.manager().navigationStateForSession(session());
        // log.debug("NavigationState:" + state + "," + state.state() + "," + state.stateAsString());
        //log.info("navigationContext:" + session().objectForKey("navigationContext"));
        return context;
    }
    
    // Actions
    
    public WOComponent logout() {
        WOComponent redirectPage = pageWithName("WORedirect");
        ((WORedirect) redirectPage).setUrl(D2W.factory().homeHrefInContext(context()));
        session().terminate();
        return redirectPage;
    }
    
    public WOComponent homeAction() {
        return D2W.factory().defaultPage(session());
    }
}