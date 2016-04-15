package er.rest.example;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXDirectAction;
import er.rest.example.components.Main;

/**
 * Nothing interesting to see here.
 * 
 * @author mschrag
 */
public class DirectAction extends ERXDirectAction {
	public DirectAction(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults defaultAction() {
		return pageWithName(Main.class);
	}
}
