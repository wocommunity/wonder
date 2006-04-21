package er.imadaptor;

import com.levelonelabs.aim.AIMBuddy;
import com.levelonelabs.aim.AIMClient;
import com.levelonelabs.aim.AIMListener;

public class AimBotInstantMessenger extends AbstractInstantMessenger {
  private boolean myConnected;
  private AIMClient mySender;
  private AimBotListener myListener;
  private long myLastConnectionAttempt;

  public AimBotInstantMessenger(String _screenName, String _password) {
    super(_screenName, _password);
    myListener = new AimBotListener();
  }

  public synchronized void addBuddy(String _buddyName) {
    mySender.addBuddy(new AIMBuddy(_buddyName));
  }

  public synchronized void connect() throws IMConnectionException {
    if (myConnected) {
      disconnect();
    }
    long now = System.currentTimeMillis();
    if (now - myLastConnectionAttempt > (1000 * 60 * 15)) {
      myLastConnectionAttempt = now;
      mySender = new AIMClient(getScreenName(), getPassword(), "", true);
      mySender.addAIMListener(myListener);
      mySender.signOn();
      mySender.setAvailable();
      myConnected = true;
      //System.out.println("AimBotInstantMessenger.connect: Connected to " + getScreenName());
    }
    else {
      throw new ConnectedTooFastException("You attempted to connect repeatedly too quickly.");
    }
  }

  public synchronized void disconnect() {
    if (myConnected) {
      mySender.signOff();
      mySender = null;
      myConnected = false;
    }
  }

  public synchronized boolean isConnected() {
    return myConnected;
  }

  public synchronized void sendMessage(String _buddyName, String _message) throws MessageException {
    if (mySender != null) {
      AIMBuddy buddy = mySender.getBuddy(_buddyName);
      if (buddy == null) {
        mySender.addBuddy(new AIMBuddy(_buddyName));
        buddy = mySender.getBuddy(_buddyName);
      }
      if (buddy != null) {
        if (!buddy.isOnline()) {
          throw new BuddyOfflineException("The buddy '" + _buddyName + "' is not online.");
        }
        mySender.sendMessage(buddy, _message);
      }
    }
  }

  protected class AimBotListener implements AIMListener {
    public void handleBuddyAvailable(AIMBuddy _buddy, String _message) {
    }

    public void handleBuddySignOff(AIMBuddy _buddy, String _info) {
    }

    public void handleBuddySignOn(AIMBuddy _buddy, String _info) {
    }

    public void handleBuddyUnavailable(AIMBuddy _buddy, String _message) {
    }

    public void handleConnected() {
    }

    public void handleDisconnected() {
    }

    public void handleError(String _error, String _message) {
    }

    public void handleMessage(AIMBuddy _buddy, String _message) {
      AimBotInstantMessenger.this.fireMessageReceived(_buddy.getName(), _message);
    }

    public void handleWarning(AIMBuddy _buddy, int _amount) {
    }
  }

  public static class Factory implements IInstantMessengerFactory {
    public IInstantMessenger createInstantMessenger(String _screenName, String _password) {
      return new AimBotInstantMessenger(_screenName, _password);
    }
  }
}
