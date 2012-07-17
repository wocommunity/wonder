package er.imadaptor.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.imadaptor.InstantMessengerAdaptor;

public class IMAction extends WODynamicElement {
	protected WOAssociation _action;

	public IMAction(String name, NSDictionary assocationsDictionary, WOElement template) {
		super("link", assocationsDictionary, template);
		_action = (WOAssociation) assocationsDictionary.objectForKey("action");
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		String actionUrl = context.componentActionURL(WOApplication.application().componentRequestHandlerKey(), false);
		response.setHeader(actionUrl, InstantMessengerAdaptor.IM_ACTION_URL_KEY);
		super.appendToResponse(response, context);
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults results = null;
		if (context.elementID().equals(context.senderID())) {
			actionInvoked(request, context);
			WOComponent component = context.component();
			results = (WOActionResults) _action.valueInComponent(component);
			if (results == null) {
				results = context.page();
			}
		}
		return results;
	}

	protected void actionInvoked(WORequest request, WOContext context) {
	}
}
