package er.imadaptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import er.extensions.concurrency.ERXAsyncQueue;

public class InstantMessengerConnection {
	private InstantMessageQueue _messageQueue;
	private IInstantMessenger _instantMessenger;
	private InstantMessengerWatchDog _watchDog;
	private Map<String, Conversation> _conversations;

	protected InstantMessengerConnection() {
		_conversations = new HashMap<String, Conversation>();
		_messageQueue = new InstantMessageQueue();
		_messageQueue.start();
	}

	public InstantMessengerConnection(String screenName, String password, IInstantMessengerFactory factory) {
		this();
		_instantMessenger = factory.createInstantMessenger(screenName, password);
	}

	public InstantMessengerConnection(IInstantMessenger instantMessenger) {
		this();
		_instantMessenger = instantMessenger;
	}

	public void setWatchDog(String screenName, String password, IInstantMessengerFactory watchdogFactory) {
		if (_watchDog != null) {
			_watchDog.stop();
		}
		IInstantMessenger watchdogInstantMessenger = watchdogFactory.createInstantMessenger(screenName, password);
		setWatchDog(watchdogInstantMessenger);
	}

	public void setWatchDog(IInstantMessenger instantMessenger) {
		if (_watchDog != null) {
			_watchDog.stop();
		}
		_watchDog = new InstantMessengerWatchDog(_instantMessenger, instantMessenger);
	}

	public IInstantMessenger instantMessenger() {
		return _instantMessenger;
	}

	public InstantMessengerWatchDog watchDog() {
		return _watchDog;
	}

	public List<Conversation> conversations() {
		List<Conversation> conversations = new LinkedList<Conversation>(_conversations.values());
		return conversations;
	}

	public Conversation conversationForBuddyNamed(String buddyName, long conversationTimeout) {
		Conversation conversation;
		synchronized (_conversations) {
			conversation = _conversations.get(buddyName);
			System.out.println("InstantMessengerConnection.conversationForBuddyNamed: conversation = " + conversation);
			if (conversation == null || conversation.isExpired(conversationTimeout)) {
				System.out.println("InstantMessengerConnection.conversationForBuddyNamed:   ... created a new one");
				conversation = new Conversation();
				conversation.setScreenName(_instantMessenger.getScreenName());
				conversation.setBuddyName(buddyName);
				_conversations.put(buddyName, conversation);
			}
			else {
				conversation.ping();
			}
		}
		return conversation;
	}

	public void removeExpiredConversations(long conversationTimeout) {
		synchronized (_conversations) {
			Iterator conversationsIter = _conversations.entrySet().iterator();
			while (conversationsIter.hasNext()) {
				Map.Entry entry = (Map.Entry) conversationsIter.next();
				Conversation conversation = (Conversation) entry.getValue();
				if (conversation.isExpired(conversationTimeout)) {
					conversationExpired(conversation);
					conversationsIter.remove();
				}
			}
		}
	}

	protected void conversationExpired(Conversation conversation) {
		// DO NOTHING
	}

	public void sendMessage(String buddyName, String message, boolean block) throws MessageException {
		if (block) {
			_instantMessenger.sendMessage(buddyName, message, true);
		}
		else {
			_messageQueue.enqueue(new Message(buddyName, message));
		}
	}

	public void connect(IMessageListener messageListener) {
		_instantMessenger.addMessageListener(messageListener);
		try {
			_instantMessenger.connect();
		}
		catch (Throwable e) {
			InstantMessengerAdaptor.log.debug("Failed to connect to provider.", e);
		}

		if (_watchDog != null) {
			_watchDog.start();
		}
	}

	public void disconnect() {
		if (_instantMessenger != null) {
			_instantMessenger.disconnect();
		}
		if (_watchDog != null) {
			_watchDog.stop();
		}
	}

	protected static class Message {
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

	protected class InstantMessageQueue extends ERXAsyncQueue<Message> {
		@Override
		public void process(Message message) {
			try {
				IInstantMessenger instantMessenger = instantMessenger();
				instantMessenger.sendMessage(message.buddyName(), message.contents(), true);
			}
			catch (MessageException e) {
				InstantMessengerAdaptor.log.error(e);
			}
		}
	}
}
