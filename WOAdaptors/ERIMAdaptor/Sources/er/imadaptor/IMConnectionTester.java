package er.imadaptor;

public class IMConnectionTester implements Runnable, IMessageListener {
  private static final String PING_MESSAGE = "~Ping~";
  private static final String PONG_MESSAGE = "~Pong~";

  private IInstantMessenger myWatcher;
  private IInstantMessenger myWatched;

  private Object myPingPongMessageLock;
  private int myFailureCount;
  private boolean myPinged;
  private boolean myPonged;
  private long myPingPongFrequencyMillis;
  private long myTimeoutMillis;
  private long myLastConnectionAttempt;

  private boolean myRunning;

  public IMConnectionTester(IInstantMessenger _watcher, IInstantMessenger _watched, long _pingPongFrequencyMillis, long _timeoutMillis) {
    myRunning = true;

    myPingPongMessageLock = new Object();

    myWatcher = _watcher;
    myWatched = _watched;

    myPingPongFrequencyMillis = _pingPongFrequencyMillis;
    myTimeoutMillis = _timeoutMillis;

    synchronized (myPingPongMessageLock) {
      myWatched.addMessageListener(this);
      myWatcher.addMessageListener(this);
    }
  }

  public void stop() {
    myRunning = false;
  }

  public void messageReceived(IInstantMessenger _instantMessenger, String _buddyName, String _message) {
    //System.out.println("IMConnectionTester.messageReceived: " + _buddyName + ", " + _message);
    if (_instantMessenger == myWatched && myWatcher.getScreenName().equals(_buddyName) && IMConnectionTester.PING_MESSAGE.equals(_message)) {
      synchronized (myPingPongMessageLock) {
        try {
          //System.out.println("IMConnectionTester.testConnection: Sending PONG to " + myWatcher.getScreenName());
          myWatched.sendMessage(_buddyName, IMConnectionTester.PONG_MESSAGE);
        }
        catch (MessageException e) {
          // We failed to pong!
          e.printStackTrace();
        }
      }
    }
    else if (_instantMessenger == myWatcher && myWatched.getScreenName().equals(_buddyName) && IMConnectionTester.PONG_MESSAGE.equals(_message)) {
      synchronized (myPingPongMessageLock) {
        myPonged = true;
        //System.out.println("IMConnectionTester.testConnection: Recevied PONG from " + myWatched.getScreenName());
        myPingPongMessageLock.notifyAll();
      }
    }
  }

  protected void testConnection() throws IMConnectionException {
    if (myRunning && !myWatched.isConnected()) {
      myWatched.connect();
      myFailureCount = 0;
    }

    if (myRunning && !myWatcher.isConnected()) {
      myWatcher.connect();
      myFailureCount = 0;
    }

    synchronized (myPingPongMessageLock) {
      try {
        //System.out.println("IMConnectionTester.testConnection: Sending PING to " + myWatched.getScreenName());
        myWatcher.sendMessage(myWatched.getScreenName(), IMConnectionTester.PING_MESSAGE);
        myPonged = false;
        myPingPongMessageLock.wait(myTimeoutMillis);
        if (!myPonged) {
          //System.out.println("IMConnectionTester.testConnection: " + myWatcher.getScreenName() + " did not respond to PING");
          myFailureCount++;
          if (myRunning && myFailureCount > 5) {
            //System.out.println("IMConnectionTester.reconnect: Reconnecting " + myWatched.getScreenName());
            myWatched.connect();
            myFailureCount = 0;
          }
        }
      }
      catch (MessageException e) {
        e.printStackTrace();
      }
      catch (InterruptedException e) {
        // ignore
      }
      finally {
        myPinged = false;
        myPonged = false;
      }
    }
  }

  public void run() {
    while (myRunning) {
      try {
        Thread.sleep(myPingPongFrequencyMillis);
      }
      catch (InterruptedException e) {
        // who cares
      }

      if (myRunning) {
        //System.out.println("IMConnectionTester.run: Testing " + myWatched.getScreenName());
        try {
          testConnection();
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }
  }

  public static void main(String[] args) throws IMConnectionException {
    IInstantMessenger watchedInstantMessenger = new AimBotInstantMessenger.Factory().createInstantMessenger("iykwias", "wec1kl");
    watchedInstantMessenger.connect();

    IInstantMessenger watcherInstantMessenger = new AimBotInstantMessenger.Factory().createInstantMessenger("iykwiasjr", "tc2001");
    watcherInstantMessenger.connect();

    Thread watcherThread = new Thread(new IMConnectionTester(watcherInstantMessenger, watchedInstantMessenger, 60000, 30000));
    watcherThread.start();

    //myWatchedThread = new Thread(new IMConnectionTester(myInstantMessenger, myWatcherInstantMessenger, 60000, 30000));
    //myWatchedThread.start();
  }
}
