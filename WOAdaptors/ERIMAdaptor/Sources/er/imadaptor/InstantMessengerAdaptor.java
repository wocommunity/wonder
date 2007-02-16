package er.imadaptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.kano.joscar.logging.LoggingSystem;

import org.apache.log4j.Logger;

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
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXAsyncQueue;

public class InstantMessengerAdaptor extends WOAdaptor implements IMessageListener {
	static {
		LoggingSystem.setLogManager(new JOscarLogManager());
	}
	private static Logger log = Logger.getLogger(InstantMessengerAdaptor.class);

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
	public static final String CENTRALIZE_SCREEN_NAME_KEY = "IMCentralizeScreenName";

	public static final String IS_IM_KEY = "IsIM";
	public static final String CONVERSATION_KEY = "IMConversation";
	public static final String BUDDY_NAME_KEY = "BuddyName";
	public static final String MESSAGE_KEY = "Message";
	public static final String RAW_MESSAGE_KEY = "RawMessage";

	private WOApplication _application;
	private String _screenName;
	private String _password;
	private IInstantMessengerFactory _factory;
	private IInstantMessenger _instantMessenger;

	private Map _conversations;
	private long _conversationTimeout;
	private boolean _running;
	private boolean _autoLogin;
	private String _conversationActionName;

	private String _centralizeScreenName;
	private String _watcherScreenName;
	private String _watcherPassword;
	private IInstantMessengerFactory _watcherFactory;
	private IInstantMessenger _watcherInstantMessenger;
	private Thread _watcherThread;
	private IMConnectionTester _watcherTester;
	private Thread _watchedThread;
	private IMConnectionTester _watchedTester;
	private InstantMessageQueue _messageQueue;

	public InstantMessengerAdaptor(String name, NSDictionary parameters) {
		super(name, parameters);

		_messageQueue = new InstantMessageQueue();
		_messageQueue.start();

		_conversations = new HashMap();
		_application = WOApplication.application();

		_centralizeScreenName = propertyNamed(InstantMessengerAdaptor.CENTRALIZE_SCREEN_NAME_KEY, parameters, false);
		_screenName = propertyNamed(InstantMessengerAdaptor.SCREEN_NAME_KEY, parameters, true);
		_password = propertyNamed(InstantMessengerAdaptor.PASSWORD_KEY, parameters, true);
		_factory = getFactory(InstantMessengerAdaptor.IM_FACTORY_KEY, parameters);

		_conversationTimeout = 1000 * 60 * 5;
		String conversationTimeoutStr = (String) parameters.objectForKey(InstantMessengerAdaptor.CONVERSATION_TIMEOUT_KEY);
		if (conversationTimeoutStr == null) {
			conversationTimeoutStr = System.getProperty(InstantMessengerAdaptor.CONVERSATION_TIMEOUT_KEY);
		}
		if (conversationTimeoutStr != null) {
			_conversationTimeout = Long.valueOf(conversationTimeoutStr).longValue();
		}

		_conversationActionName = (String) parameters.objectForKey(InstantMessengerAdaptor.CONVERSATION_ACTION_NAME_KEY);
		if (_conversationActionName == null) {
			_conversationActionName = System.getProperty(InstantMessengerAdaptor.CONVERSATION_ACTION_NAME_KEY);
			if (_conversationActionName == null) {
				_conversationActionName = "imConversation";
			}
		}

		_autoLogin = true;
		String autoLoginStr = (String) parameters.objectForKey(InstantMessengerAdaptor.AUTO_LOGIN_KEY);
		if (autoLoginStr == null) {
			autoLoginStr = System.getProperty(InstantMessengerAdaptor.AUTO_LOGIN_KEY);
		}
		if (autoLoginStr != null && !Boolean.valueOf(autoLoginStr).booleanValue()) {
			_autoLogin = false;
		}

		String watcherEnabledStr = (String) parameters.objectForKey(InstantMessengerAdaptor.WATCHER_ENABLED_KEY);
		if (watcherEnabledStr == null) {
			watcherEnabledStr = System.getProperty(InstantMessengerAdaptor.WATCHER_ENABLED_KEY);
		}
		if (watcherEnabledStr != null && Boolean.valueOf(watcherEnabledStr).booleanValue()) {
			_watcherScreenName = propertyNamed(InstantMessengerAdaptor.WATCHER_SCREEN_NAME_KEY, parameters, true);
			_watcherPassword = propertyNamed(InstantMessengerAdaptor.WATCHER_PASSWORD_KEY, parameters, true);
			_watcherFactory = getFactory(InstantMessengerAdaptor.WATCHER_IM_FACTORY_KEY, parameters);
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
		return _instantMessenger;
	}

	protected String propertyNamed(String key, NSDictionary parameters, boolean required) {
		String value = (String) parameters.objectForKey(key);
		if (value == null) {
			value = System.getProperty(key);
		}
		if (required && value == null) {
			throw new RuntimeException("Missing required property " + key + ".");
		}
		return value;
	}

	protected IInstantMessengerFactory getFactory(String key, NSDictionary parameters) {
		String factoryClass = propertyNamed(key, parameters, false);
		try {
			IInstantMessengerFactory factory;
			if (factoryClass == null) {
				factory = new JOscarInstantMessenger.Factory();
			}
			else {
				factory = (IInstantMessengerFactory) Class.forName(factoryClass).newInstance();
			}
			return factory;
		}
		catch (Throwable t) {
			throw new RuntimeException("Invalidate InstantMessengerFactory: " + factoryClass, t);
		}
	}

	public static boolean isIMRequest(WOContext context) {
		return InstantMessengerAdaptor.isIMRequest(context.request());
	}

	public static boolean isIMRequest(WORequest request) {
		NSDictionary userInfo = request.userInfo();
		return (userInfo != null && userInfo.objectForKey(InstantMessengerAdaptor.IS_IM_KEY) != null);
	}

	public static String message(WORequest request) {
		return (String) request.userInfo().objectForKey(InstantMessengerAdaptor.MESSAGE_KEY);
	}

	public static String rawMessage(WORequest request) {
		return (String) request.userInfo().objectForKey(InstantMessengerAdaptor.RAW_MESSAGE_KEY);
	}

	public static String buddyName(WORequest request) {
		return (String) request.userInfo().objectForKey(InstantMessengerAdaptor.BUDDY_NAME_KEY);
	}

	public static Conversation conversation(WORequest request) {
		return (Conversation) request.userInfo().objectForKey(InstantMessengerAdaptor.CONVERSATION_KEY);
	}

	public void registerForEvents() {
		_instantMessenger = _factory.createInstantMessenger(_screenName, _password);
		if (_autoLogin) {
			connect();
		}
	}

	public void connect() {
		_instantMessenger.addMessageListener(this);
		try {
			_instantMessenger.connect();
		}
		catch (Throwable e) {
			InstantMessengerAdaptor.log.debug("Failed to connect to provider.", e);
		}

		if (_watcherFactory != null) {
			_watcherInstantMessenger = _watcherFactory.createInstantMessenger(_watcherScreenName, _watcherPassword);
			try {
				_watcherInstantMessenger.connect();
			}
			catch (Throwable e) {
				InstantMessengerAdaptor.log.debug("Failed to connect watcher to provider.", e);
			}

			_watcherTester = new IMConnectionTester(_watcherInstantMessenger, _instantMessenger, 60000, 30000);
			_watcherThread = new Thread(_watcherTester);
			_watcherThread.start();

			_watchedTester = new IMConnectionTester(_instantMessenger, _watcherInstantMessenger, 60000, 30000);
			_watchedThread = new Thread(_watchedTester);
			_watchedThread.start();
		}

		_running = true;
		Thread conversationExpiration = new Thread(new ConversationExpirationRunnable());
		conversationExpiration.start();
	}

	public void unregisterForEvents() {
		_running = false;
		if (_instantMessenger != null) {
			_instantMessenger.disconnect();
		}
		if (_watcherInstantMessenger != null) {
			_watchedTester.stop();
			_watcherTester.stop();
			_watcherInstantMessenger.disconnect();
		}
	}

	public boolean dispatchesRequestsConcurrently() {
		return true;
	}

	protected void conversationExpired(Conversation conversation) {

	}

	protected void removeExpiredConversations() {
		synchronized (_conversations) {
			Iterator conversationsIter = _conversations.entrySet().iterator();
			while (conversationsIter.hasNext()) {
				Map.Entry entry = (Map.Entry) conversationsIter.next();
				Conversation conversation = (Conversation) entry.getValue();
				if (conversation.isExpired(_conversationTimeout)) {
					conversationExpired(conversation);
					conversationsIter.remove();
				}
			}
		}
	}

	public synchronized void messageReceived(IInstantMessenger instantMessenger, String buddyName, String rawMessage) {
		if (log.isInfoEnabled()) {
			log.info("Received message from '" + buddyName + "': " + rawMessage);
		}
		String message = rawMessage;
		if (message != null) {
			message = message.replaceAll("<[^>]+>", "");
			message = message.trim();
		}
		Conversation conversation;
		synchronized (_conversations) {
			conversation = (Conversation) _conversations.get(buddyName);
			if (conversation == null || conversation.isExpired(_conversationTimeout)) {
				conversation = new Conversation();
				conversation.setBuddyName(buddyName);
				_conversations.put(buddyName, conversation);
			}
			else {
				conversation.ping();
			}
		}

		StringBuffer uri = new StringBuffer();
		String requestUrl = conversation.requestUrl();
		if (requestUrl == null) {
			String webserverConnectUrl = _application.webserverConnectURL();
			String cgiAdaptorURL = _application.cgiAdaptorURL();
			WODynamicURL imConversationUrl = new WODynamicURL();

			int j = cgiAdaptorURL.indexOf("//");
			int i = 0;
			if (j > 0 && cgiAdaptorURL.length() - j > 2) {
				i = cgiAdaptorURL.indexOf('/', j + 2);
			}
			if (i > 0) {
				imConversationUrl.setPrefix(cgiAdaptorURL.substring(i));
			}
			else {
				imConversationUrl.setPrefix(_application.applicationBaseURL());
			}
			imConversationUrl.setRequestHandlerKey(_application.directActionRequestHandlerKey());
			imConversationUrl.setApplicationName(_application.name());
			imConversationUrl.setApplicationNumber(_application.number());
			imConversationUrl.setRequestHandlerPath(_conversationActionName);
			uri.append(imConversationUrl.toString());
		}
		else {
			uri.append(requestUrl);
		}
		uri.append("?");
		uri.append(InstantMessengerAdaptor.BUDDY_NAME_KEY);
		uri.append("=");
		uri.append(WOURLEncoder.encode(buddyName));
		uri.append("&");
		uri.append(InstantMessengerAdaptor.MESSAGE_KEY);
		uri.append("=");
		uri.append(WOURLEncoder.encode(message));
		uri.append("&");
		uri.append(InstantMessengerAdaptor.RAW_MESSAGE_KEY);
		uri.append("=");
		uri.append(WOURLEncoder.encode(rawMessage));
		String sessionID = conversation.sessionID();
		if (sessionID != null) {
			uri.append("&wosid=" + sessionID);
		}

		NSMutableDictionary headers = new NSMutableDictionary();

		NSMutableDictionary userInfo = new NSMutableDictionary();
		userInfo.setObjectForKey(Boolean.TRUE, InstantMessengerAdaptor.IS_IM_KEY);
		userInfo.setObjectForKey(buddyName, InstantMessengerAdaptor.BUDDY_NAME_KEY);
		userInfo.setObjectForKey(message, InstantMessengerAdaptor.MESSAGE_KEY);
		userInfo.setObjectForKey(rawMessage, InstantMessengerAdaptor.RAW_MESSAGE_KEY);
		userInfo.setObjectForKey(conversation, InstantMessengerAdaptor.CONVERSATION_KEY);

		WORequest request = _application.createRequest("GET", uri.toString(), "HTTP/1.0", headers, null, userInfo);
		WOResponse response;
		try {
			response = _application.dispatchRequest(request);
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
			if (log.isInfoEnabled()) {
				log.info("Sending message to '" + buddyName + "': " + responseMessage);
			}
			_instantMessenger.sendMessage(buddyName, responseMessage, true);
		}
		catch (Throwable t) {
			InstantMessengerAdaptor.log.error(toString() + " Exception occurred while responding to client: " + t.toString(), t);
		}
		NSDelayedCallbackCenter.defaultCenter().eventEnded();
	}

	public void sendMessage(String buddyName, String message, boolean block) throws MessageException {
		if (_centralizeScreenName != null) {
			log.warn("IM's are centralized; replacing '" + buddyName + "' with '" + _centralizeScreenName + "'");
			buddyName = _centralizeScreenName;
		}
		if (block) {
			instantMessenger().sendMessage(buddyName, message, true);
		}
		else {
			_messageQueue.enqueue(new Message(buddyName, message));
		}
	}

	protected class InstantMessageQueue extends ERXAsyncQueue {
		public void process(Object object) {
			Message message = (Message) object;
			try {
				IInstantMessenger instantMessenger = InstantMessengerAdaptor.this.instantMessenger();
				InstantMessengerAdaptor.this.instantMessenger().sendMessage(message.buddyName(), message.contents(), true);
			}
			catch (MessageException e) {
				InstantMessengerAdaptor.log.error(e);
			}
		}
	}

	protected class Message {
		private String _contents;
		private String _buddyName;

		public Message(String buddyName, String contents) {
			_buddyName = buddyName;
			_contents = contents;
		}

		public String contents() {
			return _contents;
		}

		public String buddyName() {
			return _buddyName;
		}
	}

	protected class ConversationExpirationRunnable implements Runnable {
		public void run() {
			while (_running) {
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
