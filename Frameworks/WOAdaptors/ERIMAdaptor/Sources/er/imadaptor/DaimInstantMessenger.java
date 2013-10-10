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
	private boolean _connected;
	private DaimOscarClient _oscarClient;
	private long _lastConnectionAttempt;

	public DaimInstantMessenger(String screenName, String password) {
		super(screenName, password);
	}
	
	public long buddyListLastModified() {
		return System.currentTimeMillis();
	}

	public void addBuddy(String buddyName) throws InstantMessengerException {
		try {
			if (_oscarClient != null) {
				_oscarClient.addBuddy(buddyName, "Group");
			}
		}
		catch (IOException e) {
			throw new InstantMessengerException("Failed to add buddy.", e);
		}
	}

	public void removeBuddy(String buddyName) throws InstantMessengerException {
		throw new InstantMessengerException("I can't do this right now.");
	}

	public void connect() throws IMConnectionException {
		if (_connected) {
			disconnect();
		}
		long now = System.currentTimeMillis();
		if (now - _lastConnectionAttempt > (1000 * 60 * 15)) {
			_lastConnectionAttempt = now;
			try {
				_oscarClient = new DaimOscarClient();
				_oscarClient.login(getScreenName(), getPassword());
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
		if (_oscarClient != null) {
			_oscarClient.logout();
			_oscarClient = null;
		}
	}

	public boolean isConnected() {
		return _connected;
	}

	public boolean isBuddyOnline(String buddyName) {
		return _oscarClient != null && _oscarClient.isBuddyOnline(buddyName);
	}

	public String[] getGroupNames() {
		return new String[] { "Buddies" };
	}

	@SuppressWarnings("unchecked")
	public String[] getBuddiesInGroupNamed(String groupName) {
		List<String> buddiesList = _oscarClient.getBuddies();
		return buddiesList.toArray(new String[buddiesList.size()]);
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

	public void sendMessage(String buddyName, String message, boolean ignoreIfOffline) throws MessageException {
		try {
			if (_oscarClient != null) {
				_oscarClient.sendIM(buddyName, message, AIMConstants.AIM_FLAG_AOL);
			}
		}
		catch (IOException e) {
			throw new MessageException("Failed to send message.", e);
		}
	}

	public class DaimOscarClient extends AbstractOscarClient {
		private List<String> _buddies;
		private List<String> _onlineBuddies;
		private List<String> _offlineBuddies;

		public DaimOscarClient() {
			_buddies = new LinkedList<String>();
			_onlineBuddies = new LinkedList<String>();
			_offlineBuddies = new LinkedList<String>();
		}

		public boolean isBuddyOnline(String buddyName) {
			boolean online;
			synchronized (_buddies) {
				online = _onlineBuddies.contains(buddyName.toLowerCase());
			}
			return online;
		}

		public List getBuddies() {
			return _buddies;
		}

		@Override
		public void buddyOffline(String buddyName, Buddy buddy) {
			if (buddyName != null) {
				String lcBuddyName = buddyName.toLowerCase();
				_onlineBuddies.remove(lcBuddyName);
				_offlineBuddies.add(lcBuddyName);
			}
		}

		@Override
		public void buddyOnline(String buddyName, Buddy buddy) {
			if (buddyName != null) {
				String lcBuddyName = buddyName.toLowerCase();
				_offlineBuddies.remove(lcBuddyName);
				_onlineBuddies.add(lcBuddyName);
			}
		}

		public void removeBuddy(String buddyName) {

		}

		@Override
		public void newBuddyList(Buddy[] buddies) {
			synchronized (_buddies) {
				_buddies.clear();
				_onlineBuddies.clear();
				_offlineBuddies.clear();
				for (int i = 0; i < buddies.length; i++) {
					_buddies.add(buddies[i].getName().toLowerCase());
				}
			}
		}

		@Override
		public void loginDone(DaimLoginEvent event) {
			super.loginDone(event);
			_connected = true;
		}

		@Override
		public void incomingICQ(UserInfo userInfo, int arg1, int arg2, String message) {
			super.incomingICQ(userInfo, arg1, arg2, message);
			if (userInfo != null) {
				message = message.replaceAll("\\<.*?\\>", "");
				fireMessageReceived(userInfo.getSN(), message);
			}
		}

		@Override
		public void incomingIM(Buddy buddy, UserInfo userInfo, AOLIM im) {
			super.incomingIM(buddy, userInfo, im);
			String message = im.getMsg();
			if (buddy != null) {
				message = message.replaceAll("\\<.*?\\>", "");
				fireMessageReceived(buddy.getName(), message);
			}
		}

		@Override
		public void login(String screenName, String password) throws IOException {
			super.login(screenName, password);
		}

		@Override
		public void loginError(DaimLoginEvent event) {
			super.loginError(event);
			_connected = false;
		}

		@Override
		public void logout() {
			super.logout();
			_connected = false;
		}
	}

	public static class Factory implements IInstantMessengerFactory {
		public IInstantMessenger createInstantMessenger(String screenName, String password) {
			return new DaimInstantMessenger(screenName, password);
		}
	}
}
