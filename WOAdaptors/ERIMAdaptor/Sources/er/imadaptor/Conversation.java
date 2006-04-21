package er.imadaptor;

public class Conversation {
  private String myBuddyName;
  private String mySessionID;
  private String myRequestUrl;
  private long myLastContact;
 
  public Conversation() {
    myLastContact = System.currentTimeMillis();
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
}
