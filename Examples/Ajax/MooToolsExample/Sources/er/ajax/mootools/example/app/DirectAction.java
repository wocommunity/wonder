package er.ajax.mootools.example.app;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.ajax.mootools.example.components.HomePage;
import er.extensions.appserver.ERXDirectAction;


public class DirectAction extends ERXDirectAction {
	public DirectAction(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults defaultAction() {
		return pageWithName(HomePage.class);
	}
}
