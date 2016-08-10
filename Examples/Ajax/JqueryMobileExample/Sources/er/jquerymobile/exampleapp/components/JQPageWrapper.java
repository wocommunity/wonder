package er.jquerymobile.exampleapp.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXNonSynchronizingComponent;
import er.jquerymobile.exampleapp.Application;
import er.jquerymobile.exampleapp.Session;

public class JQPageWrapper extends ERXNonSynchronizingComponent {
	private static final long serialVersionUID = 1L;

	public JQPageWrapper(WOContext context) {
		super(context);
	}

	public String id() {
		return stringValueForBinding("id", title().toLowerCase() + "_id");
	}

	public String title() {
		return stringValueForBinding("title", ((Application) WOApplication.application()).name());
	}

	public boolean isHomepage() {
		return booleanValueForBinding("isHomepage", false);
	}

	public boolean actAsGuest() {
		return ((Session) session()).actAsGuest();
	}

	public WOActionResults showInfo() {
		return pageWithName(InfoDialog.class);
	}

	public WOActionResults logout() {
		((Session) session()).setActAsGuest(true);
		return null;
	}

	public WOActionResults login() {
		((Session) session()).setActAsGuest(false);
		return null;
	}
}
