package er.examples.erjaxws;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;

import er.examples.erjaxws.components.Main;
import er.extensions.appserver.ERXDirectAction;

public class DirectAction extends ERXDirectAction {
	public DirectAction(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults defaultAction() {
		return pageWithName(Main.class.getName());
	}
	
	public Application application() {
		return (Application)WOApplication.application();
	}
	
	@Override
	public Session session() {
		return (Session)super.session();
	}
}
