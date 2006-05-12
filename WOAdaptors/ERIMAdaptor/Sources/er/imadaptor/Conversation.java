package er.imadaptor;

import com.webobjects.foundation.NSMutableDictionary;

/**
 * Represents an open instant messenger conversation. 
 * 
 * @author mschrag
 */
public class Conversation {
  private String myBuddyName;
  private String mySessionID;
  private String myRequestUrl;
  private long myLastContact;
  private NSMutableDictionary myValues;
  
  public Conversation() {
    myLastContact = System.currentTimeMillis();
    myValues = new NSMutableDictionary();
  }

  public String getRequestUrl() {
    return myRequestUrl;
  }

  public void setRequestUrl(String _requestUrl) {
    myRequestUrl = _requestUrl;
  }

  public String getBuddyName() {
    return myBuddyName;
  }

  public void setBuddyName(String _buddyName) {
    myBuddyName = _buddyName;
  }

  public String getSessionID() {
    return mySessionID;
  }

  public void setSessionID(String _sessionID) {
    mySessionID = _sessionID;
  }

  public void ping() {
    myLastContact = System.currentTimeMillis();
  }

  public void expire() {
    myLastContact = System.currentTimeMillis();
  }

  public boolean isExpired(long _timeout) {
    return (System.currentTimeMillis() - myLastContact) > _timeout;
  }
  
  public void setObjectForKey(Object _value, String _key) {
    myValues.setObjectForKey(_value, _key);
  }
  
  public Object objectForKey(String _key) {
    return myValues.objectForKey(_key);
  }
}
