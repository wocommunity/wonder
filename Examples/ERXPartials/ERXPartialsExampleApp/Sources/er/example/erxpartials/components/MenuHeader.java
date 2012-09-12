package er.example.erxpartials.components;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

import er.extensions.appserver.navigation.ERXNavigationManager;
import er.extensions.appserver.navigation.ERXNavigationState;

public class MenuHeader extends WOComponent {
	private static final long serialVersionUID = 1L;

    public MenuHeader(WOContext aContext) {
        super(aContext);
    }

	// ERXModernNavigationMenu Support

	public NSKeyValueCoding navigationContext()
	{

		NSKeyValueCoding context = (NSKeyValueCoding) session().objectForKey("navigationContext");

		if (context().page() instanceof D2WPage)
		{
			context = ((D2WPage) context().page()).d2wContext();
		}

		// log.debug(ERXNavigationManager.manager().navigationStateForSession(session()));
		if (context == null)
		{
			context = new NSMutableDictionary<Object, String>();
			session().setObjectForKey(context, "navigationContext");
		}
		@SuppressWarnings("unused")
		ERXNavigationState state = ERXNavigationManager.manager().navigationStateForSession(session());
		// log.debug("NavigationState:" + state + "," + state.state() + "," +
		// state.stateAsString());
		// log.info("navigationContext:" +
		// session().objectForKey("navigationContext"));
		return context;
	}


	// Actions

	public WOComponent logout()
	{
		WOComponent redirectPage = pageWithName("WORedirect");
		((WORedirect) redirectPage).setUrl(D2W.factory().homeHrefInContext(context()));
		session().terminate();
		return redirectPage;
	}


	public WOComponent homeAction()
	{
		return D2W.factory().defaultPage(session());
	}

}
