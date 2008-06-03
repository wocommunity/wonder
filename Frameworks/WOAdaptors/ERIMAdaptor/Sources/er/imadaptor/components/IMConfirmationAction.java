package er.imadaptor.components;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableSet;

import er.imadaptor.InstantMessengerAdaptor;

/**
 * IMConfirmation has a single binding "confirmed". If the response from the IM buddy matches any of a set of common
 * "yes", "no", etc words, confirmed is set to the appropriate value. If neither a yes nor a no word is found, confirmed
 * is set to null. You should bind this to a Boolean rather than a boolean so that you can detect the third state
 * properly and re-ask the question.
 * 
 * @author mschrag
 */
public class IMConfirmationAction extends IMAction {
	private WOAssociation _confirmed;

	public IMConfirmationAction(String name, NSDictionary associations, WOElement children) {
		super(name, associations, children);
		_confirmed = (WOAssociation) associations.objectForKey("confirmed");
	}

	@Override
	protected void actionInvoked(WORequest request, WOContext context) {
		String message = request.stringFormValueForKey(InstantMessengerAdaptor.MESSAGE_KEY);
		String lowercaseMessage = message.trim().toLowerCase();
		NSMutableSet yes = new NSMutableSet();
		yes.addObject("yes");
		yes.addObject("y");
		yes.addObject("yep");
		yes.addObject("true");

		NSMutableSet no = new NSMutableSet();
		no.addObject("no");
		no.addObject("n");
		no.addObject("nope");
		no.addObject("nah");

		WOComponent component = context.component();
		if (yes.containsObject(lowercaseMessage)) {
			_confirmed.setValue(Boolean.TRUE, component);
		}
		else if (no.containsObject(lowercaseMessage)) {
			_confirmed.setValue(Boolean.FALSE, component);
		}
		else {
			_confirmed.setValue(null, component);
		}
	}
}