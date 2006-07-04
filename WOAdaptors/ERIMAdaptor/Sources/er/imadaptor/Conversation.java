package er.imadaptor;

import com.webobjects.foundation.NSMutableDictionary;

/**
 * Represents an open instant messenger conversation. 
 * 
 * @author mschrag
 */
public class Conversation {
  private String _buddyName;
  private String _sessionID;
  private String _requestUrl;
  private long _lastContact;
  private NSMutableDictionary _values;
  
  public Conversation() {
    _lastContact = System.currentTimeMillis();
    _values = new NSMutableDictionary();
  }

  public String get_requestUrl() {
    return _requestUrl;
  }

  public void set_requestUrl(String requestUrl) {
    _requestUrl = requestUrl;
  }

  public String getBuddyName() {
    return _buddyName;
  }

  public void setBuddyName(String buddyName) {
    _buddyName = buddyName;
  }

  public String get_sessionID() {
    return _sessionID;
  }

  public void set_sessionID(String sessionID) {
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
