package er.imadaptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.webobjects.appserver.WOAdaptor;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicURL;
import com.webobjects.appserver._private.WOURLEncoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDelayedCallbackCenter;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXLogger;

public class InstantMessengerAdaptor extends WOAdaptor implements IMessageListener {
  private static ERXLogger log = ERXLogger.getERXLogger(InstantMessengerAdaptor.class);

  public static final String IM_FACTORY_KEY = "IMFactory";
  public static final String SCREEN_NAME_KEY = "IMScreenName";
  public static final String PASSWORD_KEY = "IMPassword";
  public static final String CONVERSATION_TIMEOUT_KEY = "IMTimeout";
  public static final String CONVERSATION_ACTION_NAME_KEY = "IMConversationActionName";
  public static final String IM_ACTION_URL_KEY = "IMActionURL";

  public static final String AUTO_LOGIN_KEY = "IMAutoLogin";
  public static final String WATCHER_ENABLED_KEY = "IMWatcherEnabled";
  public static final String WATCHER_IM_FACTORY_KEY = "IMWatcherFactory";
  public static final String WATCHER_SCREEN_NAME_KEY = "IMWatcherScreenName";
  public static final String WATCHER_PASSWORD_KEY = "IMWatcherPassword";

  public static final String IS_IM_KEY = "IsIM";
  public static final String CONVERSATION_KEY = "IMConversation";
  public static final String BUDDY_NAME_KEY = "BuddyName";
  public static final String MESSAGE_KEY = "Message";

  private WOApplication myApplication;
  private String myScreenName;
  private String myPassword;
  private IInstantMessengerFactory myFactory;
  private IInstantMessenger myInstantMessenger;

  private Map myConversations;
  private long myConversationTimeout;
  private boolean myRunning;
  private boolean myAutoLogin;
  private String myConversationActionName;

  private String myWatcherScreenName;
  private String myWatcherPassword;
  private IInstantMessengerFactory myWatcherFactory;
  private IInstantMessenger myWatcherInstantMessenger;
  private Thread myWatcherThread;
  private IMConnectionTester myWatcherTester;
  private Thread myWatchedThread;
  private IMConnectionTester myWatchedTester;

  public InstantMessengerAdaptor(String _name, NSDictionary _parameters) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    super(_name, _parameters);

    myConversations = new HashMap();
    myApplication = WOApplication.application();

    myScreenName = getScreenName(InstantMessengerAdaptor.SCREEN_NAME_KEY, _parameters);
    myPassword = getScreenName(InstantMessengerAdaptor.PASSWORD_KEY, _parameters);
    myFactory = getFactory(InstantMessengerAdaptor.IM_FACTORY_KEY, _parameters);

    myConversationTimeout = 1000 * 60 * 5;
    String conversationTimeoutStr = (String) _parameters.objectForKey(InstantMessengerAdaptor.CONVERSATION_TIMEOUT_KEY);
    if (conversationTimeoutStr == null) {
      conversationTimeoutStr = System.getProperty(InstantMessengerAdaptor.CONVERSATION_TIMEOUT_KEY);
    }
    if (conversationTimeoutStr != null) {
      myConversationTimeout = Long.valueOf(conversationTimeoutStr).longValue();
    }

    myConversationActionName = (String) _parameters.objectForKey(InstantMessengerAdaptor.CONVERSATION_ACTION_NAME_KEY);
    if (myConversationActionName == null) {
      myConversationActionName = System.getProperty(InstantMessengerAdaptor.CONVERSATION_ACTION_NAME_KEY);
      if (myConversationActionName == null) {
        myConversationActionName = "imConversation";
      }
    }

    myAutoLogin = true;
    String autoLoginStr = (String) _parameters.objectForKey(InstantMessengerAdaptor.AUTO_LOGIN_KEY);
    if (autoLoginStr == null) {
      autoLoginStr = System.getProperty(InstantMessengerAdaptor.AUTO_LOGIN_KEY);
    }
    if (autoLoginStr != null && !Boolean.valueOf(autoLoginStr).booleanValue()) {
      myAutoLogin = false;
    }

    String watcherEnabledStr = (String) _parameters.objectForKey(InstantMessengerAdaptor.WATCHER_ENABLED_KEY);
    if (watcherEnabledStr == null) {
      watcherEnabledStr = System.getProperty(InstantMessengerAdaptor.WATCHER_ENABLED_KEY);
    }
    if (watcherEnabledStr != null && Boolean.valueOf(watcherEnabledStr).booleanValue()) {
      myWatcherScreenName = getScreenName(InstantMessengerAdaptor.WATCHER_SCREEN_NAME_KEY, _parameters);
      myWatcherPassword = getScreenName(InstantMessengerAdaptor.WATCHER_PASSWORD_KEY, _parameters);
      myWatcherFactory = getFactory(InstantMessengerAdaptor.WATCHER_IM_FACTORY_KEY, _parameters);
    }
  }

  public static InstantMessengerAdaptor instantMessengerAdaptor() {
    NSArray adaptors = WOApplication.application().adaptors();
    InstantMessengerAdaptor matchingAdaptor = null;
    Enumeration adaptorsEnum = adaptors.objectEnumerator();
    while (matchingAdaptor == null && adaptorsEnum.hasMoreElements()) {
      WOAdaptor adaptor = (WOAdaptor) adaptorsEnum.nextElement();
      if (adaptor instanceof InstantMessengerAdaptor) {
        matchingAdaptor = (InstantMessengerAdaptor) adaptor;
      }
    }
    return matchingAdaptor;
  }

  public IInstantMessenger instantMessenger() {
    return myInstantMessenger;
  }

  protected String getScreenName(String _key, NSDictionary _parameters) {
    String screenName = (String) _parameters.objectForKey(_key);
    if (screenName == null) {
      screenName = System.getProperty(_key);
    }
    if (screenName == null) {
      throw new RuntimeException("Missing required property " + _key + ".");
    }
    return screenName;
  }

  protected String getPassword(String _key, NSDictionary _parameters) {
    String password = (String) _parameters.objectForKey(_key);
    if (password == null) {
      password = System.getProperty(_key);
    }
    if (password == null) {
      throw new RuntimeException("Missing required property " + _key + ".");
    }
    return password;
  }

  protected IInstantMessengerFactory getFactory(String _key, NSDictionary _parameters) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    String factoryClass = (String) _parameters.objectForKey(_key);
    if (factoryClass == null) {
      factoryClass = System.getProperty(_key);
    }
    IInstantMessengerFactory factory;
    if (factoryClass == null) {
      factory = new AimBotInstantMessenger.Factory();
    }
    else {
      factory = (IInstantMessengerFactory) Class.forName(factoryClass).newInstance();
    }
    return factory;
  }

  public static boolean isIMRequest(WOContext _context) {
    return InstantMessengerAdaptor.isIMRequest(_context.request());
  }

  public static boolean isIMRequest(WORequest _request) {
    NSDictionary userInfo = _request.userInfo();
    return (userInfo != null && userInfo.objectForKey(InstantMessengerAdaptor.IS_IM_KEY) != null);
  }

  public static String message(WORequest _request) {
    return (String) _request.userInfo().objectForKey(InstantMessengerAdaptor.MESSAGE_KEY);
  }

  public static String buddyName(WORequest _request) {
    return (String) _request.userInfo().objectForKey(InstantMessengerAdaptor.BUDDY_NAME_KEY);
  }

  public static Conversation conversation(WORequest _request) {
    return (Conversation) _request.userInfo().objectForKey(InstantMessengerAdaptor.CONVERSATION_KEY);
  }

  public void registerForEvents() {
    myInstantMessenger = myFactory.createInstantMessenger(myScreenName, myPassword);
    if (myAutoLogin) {
      connect();
    }
  }

  public void connect() {
    myInstantMessenger.addMessageListener(this);
    try {
      myInstantMessenger.connect();
    }
    catch (Throwable e) {
      InstantMessengerAdaptor.log.debugStackTrace(e);
    }

    if (myWatcherFactory != null) {
      myWatcherInstantMessenger = myWatcherFactory.createInstantMessenger(myWatcherScreenName, myWatcherPassword);
      try {
        myWatcherInstantMessenger.connect();
      }
      catch (Throwable e) {
        InstantMessengerAdaptor.log.debugStackTrace(e);
      }

      myWatcherTester = new IMConnectionTester(myWatcherInstantMessenger, myInstantMessenger, 60000, 30000);
      myWatcherThread = new Thread(myWatcherTester);
      myWatcherThread.start();

      myWatchedTester = new IMConnectionTester(myInstantMessenger, myWatcherInstantMessenger, 60000, 30000);
      myWatchedThread = new Thread(myWatchedTester);
      myWatchedThread.start();
    }

    myRunning = true;
    Thread conversationExpiration = new Thread(new ConversationExpirationRunnable());
    conversationExpiration.start();
  }

  public void unregisterForEvents() {
    myRunning = false;
    if (myInstantMessenger != null) {
      myInstantMessenger.disconnect();
    }
    if (myWatcherInstantMessenger != null) {
      myWatchedTester.stop();
      myWatcherTester.stop();
      myWatcherInstantMessenger.disconnect();
    }
  }

  public boolean dispatchesRequestsConcurrently() {
    return true;
  }

  protected void conversationExpired(Conversation _conversation) {

  }

  protected void removeExpiredConversations() {
    synchronized (myConversations) {
      Iterator conversationsIter = myConversations.entrySet().iterator();
      while (conversationsIter.hasNext()) {
        Map.Entry entry = (Map.Entry) conversationsIter.next();
        Conversation conversation = (Conversation) entry.getValue();
        if (conversation.isExpired(myConversationTimeout)) {
          conversationExpired(conversation);
          conversationsIter.remove();
        }
      }
    }
  }

  public synchronized void messageReceived(IInstantMessenger _instantMessenger, String _buddyName, String _message) {
    Conversation conversation;
    synchronized (myConversations) {
      conversation = (Conversation) myConversations.get(_buddyName);
      if (conversation == null || conversation.isExpired(myConversationTimeout)) {
        conversation = new Conversation();
        conversation.setBuddyName(_buddyName);
        myConversations.put(_buddyName, conversation);
      }
      else {
        conversation.ping();
      }
    }

    StringBuffer uri = new StringBuffer();
    String requestUrl = conversation.getRequestUrl();
    if (requestUrl == null) {
      String webserverConnectUrl = myApplication.webserverConnectURL();
      WODynamicURL imConversationUrl = new WODynamicURL();
      imConversationUrl.setRequestHandlerKey(myApplication.directActionRequestHandlerKey());
      imConversationUrl.setPrefix(myApplication.applicationBaseURL());
      imConversationUrl.setApplicationName(myApplication.name());
      imConversationUrl.setApplicationNumber(myApplication.number());
      imConversationUrl.setRequestHandlerPath(myConversationActionName);
      uri.append(imConversationUrl.toString());
    }
    else {
      uri.append(requestUrl);
    }
    uri.append("?");
    uri.append(InstantMessengerAdaptor.BUDDY_NAME_KEY);
    uri.append("=");
    uri.append(WOURLEncoder.encode(_buddyName));
    uri.append("&");
    uri.append(InstantMessengerAdaptor.MESSAGE_KEY);
    uri.append("=");
    uri.append(WOURLEncoder.encode(_message));
    String sessionID = conversation.getSessionID();
    if (sessionID != null) {
      uri.append("&wosid=" + sessionID);
    }

    NSMutableDictionary headers = new NSMutableDictionary();

    NSMutableDictionary userInfo = new NSMutableDictionary();
    userInfo.setObjectForKey(Boolean.TRUE, InstantMessengerAdaptor.IS_IM_KEY);
    userInfo.setObjectForKey(_buddyName, InstantMessengerAdaptor.BUDDY_NAME_KEY);
    userInfo.setObjectForKey(_message, InstantMessengerAdaptor.MESSAGE_KEY);
    userInfo.setObjectForKey(conversation, InstantMessengerAdaptor.CONVERSATION_KEY);

    WORequest request = myApplication.createRequest("GET", uri.toString(), "HTTP/1.0", headers, null, userInfo);
    WOResponse response;
    try {
      response = myApplication.dispatchRequest(request);
      String newSessionID = request.sessionID();
      if (newSessionID != null) {
        conversation.setSessionID(newSessionID);
      }
      String nextRequestUrl = response.headerForKey(InstantMessengerAdaptor.IM_ACTION_URL_KEY);
      conversation.setRequestUrl(nextRequestUrl);
      String responseMessage = response.contentString();
      if (responseMessage != null) {
        responseMessage = responseMessage.trim();
      }
      myInstantMessenger.sendMessage(_buddyName, responseMessage);
    }
    catch (Throwable t) {
      NSLog.err.appendln(toString() + " Exception occurred while responding to client: " + t.toString());
      if (NSLog.debugLoggingAllowedForLevelAndGroups(2, 0L)) {
        NSLog.debug.appendln(t);
      }
    }
    NSDelayedCallbackCenter.defaultCenter().eventEnded();
  }

  protected class ConversationExpirationRunnable implements Runnable {
    public void run() {
      while (myRunning) {
        try {
          Thread.sleep(1000 * 60);
        }
        catch (InterruptedException t) {
          // ignore
        }
        InstantMessengerAdaptor.this.removeExpiredConversations();
      }
    }
  }
}
