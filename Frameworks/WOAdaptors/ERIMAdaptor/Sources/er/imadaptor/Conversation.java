package er.imadaptor;

import com.webobjects.foundation.NSMutableDictionary;

/**
 * Represents an open instant messenger conversation.
 * 
 * @author mschrag
 */
public class Conversation {
	private String _screenName;
	private String _buddyName;
	private String _sessionID;
	private String _requestUrl;
	private long _lastContact;
	private NSMutableDictionary _values;

	public Conversation() {
		_lastContact = System.currentTimeMillis();
		_values = new NSMutableDictionary();
	}
	
	public void setScreenName(String screenName) {
		_screenName = screenName;
	}
	
	public String screenName() {
		return _screenName;
	}

	public String requestUrl() {
		return _requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		_requestUrl = requestUrl;
	}

	public String buddyName() {
		return _buddyName;
	}

	public void setBuddyName(String buddyName) {
		_buddyName = buddyName;
	}

	public String sessionID() {
		return _sessionID;
	}

	public void setSessionID(String sessionID) {
		_sessionID = sessionID;
	}

	public void ping() {
		_lastContact = System.currentTimeMillis();
	}

	public void expire() {
		_lastContact = System.currentTimeMillis();
	}

	public boolean isExpired(long timeout) {
		return (System.currentTimeMillis() - _lastContact) > timeout;
	}

	public void setObjectForKey(Object value, String key) {
		_values.setObjectForKey(value, key);
	}

	public Object objectForKey(String key) {
		return _values.objectForKey(key);
	}
}
