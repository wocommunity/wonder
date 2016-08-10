package er.imadaptor.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.imadaptor.IInstantMessenger;
import er.imadaptor.InstantMessengerAdaptor;
import er.imadaptor.InstantMessengerException;

public class IMBuddyStatus extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private String _buddyName;
	private Boolean _buddyOnline;
	private Boolean _buddyAway;
	private String _screenName;

	public IMBuddyStatus(WOContext context) {
		super(context);
	}

	public void setBuddyName(String buddyName) {
		_buddyName = buddyName;
	}

	public String buddyName() {
		return _buddyName;
	}

	public void setScreenName(String screenName) {
		_screenName = screenName;
	}

	public String screenName() {
		return _screenName;
	}

	@Override
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		_buddyOnline = null;
		_buddyAway = null;
		super.appendToResponse(woresponse, wocontext);
	}

	protected IInstantMessenger instantMessenger() {
		IInstantMessenger instantMessenger;
		if (_screenName == null) {
			instantMessenger = InstantMessengerAdaptor.instantMessengerAdaptor().defaultInstantMessenger();
		}
		else {
			instantMessenger = InstantMessengerAdaptor.instantMessengerAdaptor().instantMessengerForScreenName(_screenName);
		}
		return instantMessenger;
	}

	public boolean isBuddyOnline() {
		boolean buddyOnline;
		if (_buddyOnline == null) {
			IInstantMessenger instantMessenger = instantMessenger();
			if (instantMessenger != null) {
				try {
					buddyOnline = instantMessenger.isBuddyOnline(_buddyName);
					_buddyOnline = Boolean.valueOf(buddyOnline);
				}
				catch (InstantMessengerException e) {
					buddyOnline = false;
				}
			}
			else {
				buddyOnline = false;
			}
		}
		else {
			buddyOnline = _buddyOnline.booleanValue();
		}
		return buddyOnline;
	}

	public boolean isBuddyAway() {
		boolean buddyAway;
		if (_buddyAway == null) {
			IInstantMessenger instantMessenger = instantMessenger();
			if (instantMessenger != null) {
				try {
					buddyAway = instantMessenger().isBuddyAway(_buddyName);
					_buddyAway = Boolean.valueOf(buddyAway);
				}
				catch (InstantMessengerException e) {
					buddyAway = false;
				}
			}
			else {
				buddyAway = false;
			}
		}
		else {
			buddyAway = _buddyAway.booleanValue();
		}
		return buddyAway;
	}
}