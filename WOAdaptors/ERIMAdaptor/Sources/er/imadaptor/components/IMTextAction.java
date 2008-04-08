package er.imadaptor.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.imadaptor.InstantMessengerAdaptor;

public class IMTextAction extends IMAction {
	private NSMutableDictionary _associations;
	private WOAssociation _value;
	private WOAssociation _allowBlanks;

	public IMTextAction(String name, NSDictionary associations, WOElement element) {
		super(name, associations, element);
		_value = (WOAssociation) associations.objectForKey("value");
		_allowBlanks = (WOAssociation) associations.objectForKey("allowBlanks");
	}

	@Override
	protected void actionInvoked(WORequest request, WOContext context) {
		WOComponent component = context.component();
		String message = InstantMessengerAdaptor.message(request);
		boolean allowBlanks = (_allowBlanks != null && ((Boolean) _allowBlanks.valueInComponent(component)).booleanValue());
		if (allowBlanks || (message != null || message.trim().length() > 0)) {
			_value.setValue(message, component);
		}
		else {
			_value.setValue(null, component);
		}
	}
}
