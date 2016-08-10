package er.examples;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.examples.ui.Main;
import er.extensions.appserver.ERXDirectAction;

public class DirectAction extends ERXDirectAction {
	public DirectAction(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults defaultAction() {
		return pageWithName(Main.class);
	}
}
