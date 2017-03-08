package er.imadaptor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.levelonelabs.aim.AIMBuddy;
import com.levelonelabs.aim.AIMClient;
import com.levelonelabs.aim.AIMListener;

public class AimBotInstantMessenger extends AbstractInstantMessenger {
	private boolean _connected;
	private AIMClient _sender;
	private AimBotListener _listener;
	private long _lastConnectionAttempt;

	public AimBotInstantMessenger(String screenName, String password) {
		super(screenName, password);
		_listener = new AimBotListener();
	}
	
	public long buddyListLastModified() {
		return System.currentTimeMillis();
	}

	public synchronized boolean isBuddyOnline(String buddyName) {
		AIMBuddy buddy = getBuddy(buddyName);
		boolean online = buddy != null && buddy.isOnline();
		return online;
	}

	public synchronized AIMBuddy getBuddy(String buddyName) {
		AIMBuddy buddy;
		if (_sender == null) {
			buddy = null;
		}
		else {
			buddy = _sender.getBuddy(buddyName);
			if (buddy == null) {
				_sender.addBuddy(new AIMBuddy(buddyName));
				buddy = _sender.getBuddy(buddyName);
			}
		}
		return buddy;
	}

	public synchronized void addBuddy(String buddyName) {
		if (_sender != null) {
			_sender.addBuddy(new AIMBuddy(buddyName));
		}
	}

	public void removeBuddy(String buddyName) {
		if (_sender != null) {
			_sender.removeBuddy(new AIMBuddy(buddyName));
		}
	}

	public String[] getGroupNames() {
		return new String[] { "Buddies" };
	}

	public String[] getBuddiesInGroupNamed(String groupName) {
		List<String> buddyNamesList = new LinkedList<>();
		Iterator buddyNamesIter = _sender.getBuddyNames();
		while (buddyNamesIter.hasNext()) {
			String buddyName = (String) buddyNamesIter.next();
			buddyNamesList.add(buddyName);
		}
		String[] buddyNames = buddyNamesList.toArray(new String[buddyNamesList.size()]);
		return buddyNames;
	}

	public synchronized void connect() throws IMConnectionException {
		if (_connected) {
			disconnect();
		}
		long now = System.currentTimeMillis();
		if (now - _lastConnectionAttempt > (1000 * 60 * 15)) {
			_lastConnectionAttempt = now;
			_sender = new AIMClient(getScreenName(), getPassword(), "", true);
			_sender.addAIMListener(_listener);
			_sender.signOn();
			_sender.setAvailable();
			_connected = true;
			// System.out.println("AimBotInstantMessenger.connect: Connected to " + getScreenName());
		}
		else {
			throw new ConnectedTooFastException("You attempted to connect repeatedly too quickly.");
		}
	}

	public synchronized void disconnect() {
		if (_connected) {
			_sender.signOff();
			_sender = null;
			_connected = false;
		}
	}

	public synchronized boolean isConnected() {
		return _connected;
	}

	public String getAwayMessage(String buddyName) {
		return null;
	}

	public String getStatusMessage(String buddyName) {
		return null;
	}

	public boolean isBuddyAway(String buddyName) {
		return false;
	}

	public synchronized void sendMessage(String buddyName, String message, boolean ignoreIfOffline) throws MessageException {
		if (_sender != null) {
			AIMBuddy buddy = getBuddy(buddyName);
			if (buddy != null) {
				if (!buddy.isOnline()) {
					if (!ignoreIfOffline) {
						throw new BuddyOfflineException("The buddy '" + buddyName + "' is not online.");
					}
				}
				else {
					_sender.sendMessage(buddy, message);
				}
			}
		}
	}

	protected class AimBotListener implements AIMListener {
		public void handleBuddyAvailable(AIMBuddy buddy, String _message) {
		}

		public void handleBuddySignOff(AIMBuddy buddy, String info) {
		}

		public void handleBuddySignOn(AIMBuddy buddy, String info) {
		}

		public void handleBuddyUnavailable(AIMBuddy buddy, String message) {
		}

		public void handleConnected() {
		}

		public void handleDisconnected() {
		}

		public void handleError(String error, String message) {
		}

		public void handleMessage(AIMBuddy buddy, String message) {
			fireMessageReceived(buddy.getName(), message);
		}

		public void handleWarning(AIMBuddy buddy, int amount) {
		}
	}

	public static class Factory implements IInstantMessengerFactory {
		public IInstantMessenger createInstantMessenger(String screenName, String password) {
			return new AimBotInstantMessenger(screenName, password);
		}
	}
}
