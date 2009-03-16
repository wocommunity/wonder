package er.sproutcore.example;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;

import er.extensions.appserver.ERXDirectAction;
import er.extensions.foundation.ERXStringUtilities;

import er.sproutcore.example.components.Main;

public class DirectAction extends ERXDirectAction {
	public DirectAction(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults defaultAction() {
		return pageWithName(Main.class.getName());
	}

	@Override
	public WOActionResults performActionNamed(String actionName) {
		if("default".equals(actionName) || actionName.length() == 0) {
			return defaultAction();
		}
		String component = ERXStringUtilities.capitalize(actionName);
		return pageWithName(component);
	}

}
