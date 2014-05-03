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
import com.webobjects.appserver.WODynamicURL;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver._private.WOURLEncoder;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDelayedCallbackCenter;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.foundation.ERXProperties;

public class InstantMessengerAdaptor extends WOAdaptor implements IMessageListener {
	static {
		LoggingSystem.setLogManager(new JOscarLogManager());
	}
	public static Logger log = Logger.getLogger(InstantMessengerAdaptor.class);

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
	private IInstantMessengerFactory _factory;
	private Map<String, InstantMessengerConnection> _instantMessengers;

	private String _centralizeScreenName;
	private String _defaultScreenName;
	private String _conversationActionName;
	private long _conversationTimeout;
	private boolean _autoLogin;

	private boolean _running;

	public InstantMessengerAdaptor(String name, NSDictionary parameters) {
		super(name, parameters);

		NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("sessionDidCreate", new Class[] { NSNotification.class }), WOSession.SessionDidCreateNotification, null);

		_application = WOApplication.application();
		_instantMessengers = new HashMap<String, InstantMessengerConnection>();

		_centralizeScreenName = ERXProperties.stringForKey(InstantMessengerAdaptor.CENTRALIZE_SCREEN_NAME_KEY);
		_factory = getFactory(InstantMessengerAdaptor.IM_FACTORY_KEY);

		_conversationTimeout = ERXProperties.longForKeyWithDefault(InstantMessengerAdaptor.CONVERSATION_TIMEOUT_KEY, 1000 * 60 * 5);
		_conversationActionName = ERXProperties.stringForKeyWithDefault(InstantMessengerAdaptor.CONVERSATION_ACTION_NAME_KEY, "imConversation");
		_autoLogin = ERXProperties.booleanForKeyWithDefault(InstantMessengerAdaptor.AUTO_LOGIN_KEY, false);

		String defaultScreenName = ERXProperties.stringForKey(InstantMessengerAdaptor.SCREEN_NAME_KEY);
		String defaultPassword = ERXProperties.stringForKey(InstantMessengerAdaptor.PASSWORD_KEY);
		if (defaultScreenName != null) {
			setDefaultInstantMessenger(defaultScreenName, defaultPassword);
		}

		boolean watcherEnabled = ERXProperties.booleanForKeyWithDefault(InstantMessengerAdaptor.WATCHER_ENABLED_KEY, false);
		if (watcherEnabled) {
			String watcherScreenName = ERXProperties.stringForKey(InstantMessengerAdaptor.WATCHER_SCREEN_NAME_KEY);
			String watcherPassword = ERXProperties.stringForKey(InstantMessengerAdaptor.WATCHER_PASSWORD_KEY);
			if (watcherScreenName == null || watcherPassword == null) {
				throw new IllegalArgumentException("You must set both '" + InstantMessengerAdaptor.WATCHER_SCREEN_NAME_KEY + "' and '" + InstantMessengerAdaptor.WATCHER_PASSWORD_KEY + "' if '" + InstantMessengerAdaptor.WATCHER_ENABLED_KEY + "' is true.");
			}
			IInstantMessengerFactory watcherFactory = getFactory(InstantMessengerAdaptor.WATCHER_IM_FACTORY_KEY);
			InstantMessengerConnection defaultInstantMessengerConnection = _defaultInstantMessengerConnection();
			if (defaultInstantMessengerConnection != null) {
				defaultInstantMessengerConnection.setWatchDog(watcherScreenName, watcherPassword, watcherFactory);
			}
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
		
		if (matchingAdaptor == null) {
			throw new IllegalStateException("You must set WOAdditionalAdaptors=({WOAdaptor=\"er.imadaptor.InstantMessengerAdaptor\";})");
		}
		
		return matchingAdaptor;
	}

	public InstantMessengerConnection setDefaultInstantMessenger(String screenName, String password) {
		_defaultScreenName = screenName;
		return _addInstantMessenger(screenName, password);
	}

	public InstantMessengerConnection addInstantMessenger(String screenName, String password) {
		return _addInstantMessenger(screenName, password);
	}

	public InstantMessengerConnection _addInstantMessenger(String screenName, String password) {
		InstantMessengerConnection existingConnection = _instantMessengers.get(screenName);
		if (existingConnection != null) {
			existingConnection.disconnect();
		}
		InstantMessengerConnection connection = new InstantMessengerConnection(screenName, password, _factory);
		_instantMessengers.put(screenName, connection);
		if (_running && _autoLogin) {
			connection.connect(this);
		}
		return connection;
	}
	
	public void _removeInstantMessengerConnection(InstantMessengerConnection connection) {
		connection.disconnect();
		_instantMessengers.remove(connection.instantMessenger().getScreenName());
	}

	public void removeInstantMessenger(String screenName) {
		InstantMessengerConnection existingConnection = _instantMessengers.remove(screenName);
		if (existingConnection != null) {
			existingConnection.disconnect();
		}
	}

	public IInstantMessenger instantMessengerForScreenName(String screenName) {
		IInstantMessenger instantMessenger = null;
		InstantMessengerConnection connection = _instantMessengerConnectionNamed(screenName);
		if (connection != null) {
			instantMessenger = connection.instantMessenger();
		}
		return instantMessenger;
	}

	/**
	 * @deprecated use {@link #defaultInstantMessenger()}
	 */
	@Deprecated
	public IInstantMessenger instantMessenger() {
		return defaultInstantMessenger();
	}

	public IInstantMessenger defaultInstantMessenger() {
		return instantMessengerForScreenName(_defaultScreenName);
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
		NSDictionary userInfo = request.userInfo();
		Conversation conversation = null;
		if (userInfo != null) {
			conversation = (Conversation) userInfo.objectForKey(InstantMessengerAdaptor.CONVERSATION_KEY);
		}
		return conversation;
	}

	@Override
	public void registerForEvents() {
		if (_autoLogin) {
			Iterator instantMessengerIter = _instantMessengers.entrySet().iterator();
			while (instantMessengerIter.hasNext()) {
				Map.Entry instantMessengerEntry = (Map.Entry) instantMessengerIter.next();
				// String screenName = (String) instantMessengerEntry.getKey();
				InstantMessengerConnection connection = (InstantMessengerConnection) instantMessengerEntry.getValue();
				connection.connect(this);
			}
		}
		_running = true;
		Thread conversationExpiration = new Thread(new ConversationExpirationRunnable());
		conversationExpiration.start();
	}

	@Override
	public void unregisterForEvents() {
		_running = false;
		Iterator instantMessengerIter = _instantMessengers.entrySet().iterator();
		while (instantMessengerIter.hasNext()) {
			Map.Entry instantMessengerEntry = (Map.Entry) instantMessengerIter.next();
			// String screenName = (String) instantMessengerEntry.getKey();
			InstantMessengerConnection connection = (InstantMessengerConnection) instantMessengerEntry.getValue();
			connection.disconnect();
			instantMessengerIter.remove();
		}
	}

	@Override
	public boolean dispatchesRequestsConcurrently() {
		return true;
	}

	public synchronized void messageReceived(IInstantMessenger instantMessenger, String buddyName, String rawMessage) {
		if (log.isInfoEnabled()) {
			log.info("Received message from '" + buddyName + "': " + rawMessage);
		}
		String screenName = instantMessenger.getScreenName();
		String message = rawMessage;
		if (message != null) {
			message = message.replaceAll("<[^>]+>", "");
			message = message.trim();
		}

		StringBuilder uri = new StringBuilder();
		Conversation conversation = _instantMessengerConnectionNamed(screenName).conversationForBuddyNamed(buddyName, _conversationTimeout);
		String requestUrl = conversation.requestUrl();
		if (requestUrl == null) {
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
		uri.append('?');
		uri.append(InstantMessengerAdaptor.BUDDY_NAME_KEY);
		uri.append('=');
		uri.append(WOURLEncoder.encode(buddyName));
		uri.append('&');
		uri.append(InstantMessengerAdaptor.MESSAGE_KEY);
		uri.append('=');
		uri.append(WOURLEncoder.encode(message));
		uri.append('&');
		uri.append(InstantMessengerAdaptor.RAW_MESSAGE_KEY);
		uri.append('=');
		uri.append(WOURLEncoder.encode(rawMessage));
		String sessionID = conversation.sessionID();
		if (sessionID != null) {
			uri.append('&');
			uri.append(WOApplication.application().sessionIdKey());
			uri.append('=');
			uri.append(sessionID);
		}

		NSMutableDictionary headers = new NSMutableDictionary();

		NSMutableDictionary userInfo = new NSMutableDictionary();
		userInfo.setObjectForKey(Boolean.TRUE, InstantMessengerAdaptor.IS_IM_KEY);
		userInfo.setObjectForKey(screenName, InstantMessengerAdaptor.SCREEN_NAME_KEY);
		userInfo.setObjectForKey(buddyName, InstantMessengerAdaptor.BUDDY_NAME_KEY);
		userInfo.setObjectForKey(message, InstantMessengerAdaptor.MESSAGE_KEY);
		userInfo.setObjectForKey(rawMessage, InstantMessengerAdaptor.RAW_MESSAGE_KEY);
		userInfo.setObjectForKey(conversation, InstantMessengerAdaptor.CONVERSATION_KEY);

		WORequest request = _application.createRequest("GET", uri.toString(), "HTTP/1.0", headers, null, userInfo);
		WOResponse response;
		try {
			response = _application.dispatchRequest(request);
			// String newSessionID = request.sessionID();
			// if (newSessionID != null) {
			// conversation.setSessionID(newSessionID);
			// }
			if (response != null) {
				String nextRequestUrl = response.headerForKey(InstantMessengerAdaptor.IM_ACTION_URL_KEY);
				conversation.setRequestUrl(nextRequestUrl);
				String responseMessage = response.contentString();
				if (responseMessage != null) {
					responseMessage = responseMessage.trim();
				}
				if (responseMessage != null && responseMessage.length() > 0) {
					if (log.isInfoEnabled()) {
						log.info("Sending message to '" + buddyName + "': " + responseMessage);
					}
					sendMessage(screenName, buddyName, responseMessage, true);
				}
			}
		}
		catch (Throwable t) {
			InstantMessengerAdaptor.log.error(toString() + " Exception occurred while responding to client: " + t.toString(), t);
		}
		NSDelayedCallbackCenter.defaultCenter().eventEnded();
	}

	public void sessionDidCreate(NSNotification notification) {
		WOSession session = (WOSession) notification.object();
		WORequest request = session.context().request();
		Conversation conversation = InstantMessengerAdaptor.conversation(request);
		if (conversation != null) {
			conversation.setSessionID(session.sessionID());
		}
	}

	public void sendMessage(String screenName, String buddyName, String message, boolean block) throws MessageException {
		if (_centralizeScreenName != null) {
			log.warn("IM's are centralized; replacing '" + buddyName + "' with '" + _centralizeScreenName + "'");
			buddyName = _centralizeScreenName;
		}
		IInstantMessenger instantMessenger = instantMessengerForScreenName(screenName);
		if (instantMessenger == null) {
			log.error("There is no connection for the screen name '" + screenName + "'.");
		}
		else {
			instantMessenger.sendMessage(buddyName, message, true);
		}
	}

	public InstantMessengerConnection _instantMessengerConnectionNamed(String screenName) {
		return _instantMessengers.get(screenName);
	}

	public InstantMessengerConnection _defaultInstantMessengerConnection() {
		return _instantMessengerConnectionNamed(_defaultScreenName);
	}

	protected IInstantMessengerFactory getFactory(String key) {
		String factoryClass = ERXProperties.stringForKey(key);
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
			throw new RuntimeException("Invalid InstantMessengerFactory: " + factoryClass, t);
		}
	}

	protected void removeExpiredConversations() {
		Iterator instantMessengerIter = _instantMessengers.entrySet().iterator();
		while (instantMessengerIter.hasNext()) {
			Map.Entry instantMessengerEntry = (Map.Entry) instantMessengerIter.next();
			// String screenName = (String) instantMessengerEntry.getKey();
			InstantMessengerConnection connection = (InstantMessengerConnection) instantMessengerEntry.getValue();
			connection.removeExpiredConversations(_conversationTimeout);
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
				removeExpiredConversations();
			}
		}
	}
}
