package er.imadaptor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.walluck.oscar.AIMConstants;
import org.walluck.oscar.UserInfo;
import org.walluck.oscar.channel.aolim.AOLIM;
import org.walluck.oscar.client.AbstractOscarClient;
import org.walluck.oscar.client.Buddy;
import org.walluck.oscar.client.DaimLoginEvent;

public class DaimInstantMessenger extends AbstractInstantMessenger {
  private boolean myConnected;
  private DaimOscarClient myOscarClient;
  private long myLastConnectionAttempt;

  public DaimInstantMessenger(String _screenName, String _password) {
    super(_screenName, _password);
  }

  public void addBuddy(String _buddyName) throws InstantMessengerException {
    try {
      if (myOscarClient != null) {
        myOscarClient.addBuddy(_buddyName, "Group");
      }
    }
    catch (IOException e) {
      throw new InstantMessengerException("Failed to add buddy.", e);
    }
  }

  public void connect() throws IMConnectionException {
    if (myConnected) {
      disconnect();
    }
    long now = System.currentTimeMillis();
    if (now - myLastConnectionAttempt > (1000 * 60 * 15)) {
      myLastConnectionAttempt = now;
      try {
        myOscarClient = new DaimOscarClient();
        myOscarClient.login(getScreenName(), getPassword());
      }
      catch (IOException e) {
        throw new IMConnectionException("Failed to connect to AIM.", e);
      }
    }
    else {
      throw new ConnectedTooFastException("You attempted to connect repeatedly too quickly.");
    }
  }

  public void disconnect() {
    if (myOscarClient != null) {
      myOscarClient.logout();
      myOscarClient = null;
    }
  }

  public boolean isConnected() {
    return myConnected;
  }

  public boolean isBuddyOnline(String _buddyName) {
    return false;
  }

  public void sendMessage(String _buddyName, String _message) throws MessageException {
    try {
      if (myOscarClient != null) {
        myOscarClient.sendIM(_buddyName, _message, AIMConstants.AIM_FLAG_AOL);
      }
    }
    catch (IOException e) {
      throw new MessageException("Failed to send message.", e);
    }
  }

  public class DaimOscarClient extends AbstractOscarClient {
    private List myBuddies;
    private List myOnlineBuddies;
    private List myOfflineBuddies;

    public DaimOscarClient() {
      myBuddies = new LinkedList();
      myOnlineBuddies = new LinkedList();
      myOfflineBuddies = new LinkedList();
    }

    public boolean isBuddyOnline(String _buddyName) {
      boolean online;
      synchronized (myBuddies) {
        online = myOnlineBuddies.contains(_buddyName.toLowerCase());
      }
      return online;
    }

    public void buddyOffline(String _buddyName, Buddy _buddy) {
      if (_buddyName != null) {
        String lcBuddyName = _buddyName.toLowerCase();
        myOnlineBuddies.remove(lcBuddyName);
        myOfflineBuddies.add(lcBuddyName);
      }
    }

    public void buddyOnline(String _buddyName, Buddy _buddy) {
      if (_buddyName != null) {
        String lcBuddyName = _buddyName.toLowerCase();
        myOfflineBuddies.remove(lcBuddyName);
        myOnlineBuddies.add(lcBuddyName);
      }
    }

    public void newBuddyList(Buddy[] _buddies) {
      synchronized (myBuddies) {
        myBuddies.clear();
        myOnlineBuddies.clear();
        myOfflineBuddies.clear();
        for (int i = 0; i < _buddies.length; i++) {
          myBuddies.add(_buddies[i].getName().toLowerCase());
        }
      }
    }

    public void loginDone(DaimLoginEvent _event) {
      super.loginDone(_event);
      myConnected = true;
    }

    public void incomingICQ(UserInfo _userInfo, int _arg1, int _arg2, String _message) {
      super.incomingICQ(_userInfo, _arg1, _arg2, _message);
      String message = _message;
      if (_userInfo != null) {
        message = message.replaceAll("\\<.*?\\>", "");
        DaimInstantMessenger.this.fireMessageReceived(_userInfo.getSN(), message);
      }
    }

    public void incomingIM(Buddy _buddy, UserInfo _userInfo, AOLIM _im) {
      super.incomingIM(_buddy, _userInfo, _im);
      String message = _im.getMsg();
      if (_buddy != null) {
        message = message.replaceAll("\\<.*?\\>", "");
        DaimInstantMessenger.this.fireMessageReceived(_buddy.getName(), message);
      }
    }

    public void login(String _screenName, String _password) throws IOException {
      super.login(_screenName, _password);
    }

    public void loginError(DaimLoginEvent _event) {
      super.loginError(_event);
      myConnected = false;
    }

    public void logout() {
      super.logout();
      myConnected = false;
    }
  }

  public static class Factory implements IInstantMessengerFactory {
    public IInstantMessenger createInstantMessenger(String _screenName, String _password) {
      return new DaimInstantMessenger(_screenName, _password);
    }
  }
}
