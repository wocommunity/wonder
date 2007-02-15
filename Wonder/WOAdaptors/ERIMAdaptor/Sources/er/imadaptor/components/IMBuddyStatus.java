package er.imadaptor.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.imadaptor.InstantMessengerAdaptor;
import er.imadaptor.InstantMessengerException;

public class IMBuddyStatus extends WOComponent {
	private String _buddyName;
	private Boolean _buddyOnline;
	private Boolean _buddyAway;

	public IMBuddyStatus(WOContext context) {
		super(context);
	}

	public void setBuddyName(String buddyName) {
		_buddyName = buddyName;
	}

	public String buddyName() {
		return _buddyName;
	}

	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		_buddyOnline = null;
		_buddyAway = null;
		super.appendToResponse(woresponse, wocontext);
	}

	public boolean isBuddyOnline() {
		boolean buddyOnline;
		if (_buddyOnline == null) {
			try {
				buddyOnline = InstantMessengerAdaptor.instantMessengerAdaptor().instantMessenger().isBuddyOnline(_buddyName);
			}
			catch (InstantMessengerException e) {
				buddyOnline = false;
			}
			_buddyOnline = Boolean.valueOf(buddyOnline);
		}
		else {
			buddyOnline = _buddyOnline.booleanValue();
		}
		return buddyOnline;
	}

	public boolean isBuddyAway() {
		boolean buddyAway;
		if (_buddyAway == null) {
			try {
				buddyAway = InstantMessengerAdaptor.instantMessengerAdaptor().instantMessenger().isBuddyAway(_buddyName);
			}
			catch (InstantMessengerException e) {
				buddyAway = false;
			}
			_buddyAway = Boolean.valueOf(buddyAway);
		}
		else {
			buddyAway = _buddyAway.booleanValue();
		}
		return buddyAway;
	}
}