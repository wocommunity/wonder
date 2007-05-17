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

	public DaimInstantMessenger(String screenName, String password) {
		super(screenName, password);
	}

	public void addBuddy(String buddyName) throws InstantMessengerException {
		try {
			if (myOscarClient != null) {
				myOscarClient.addBuddy(buddyName, "Group");
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

	public boolean isBuddyOnline(String buddyName) {
		return myOscarClient != null && myOscarClient.isBuddyOnline(buddyName);
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
			if (myOscarClient != null) {
				myOscarClient.sendIM(buddyName, message, AIMConstants.AIM_FLAG_AOL);
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

		public boolean isBuddyOnline(String buddyName) {
			boolean online;
			synchronized (myBuddies) {
				online = myOnlineBuddies.contains(buddyName.toLowerCase());
			}
			return online;
		}

		public void buddyOffline(String buddyName, Buddy buddy) {
			if (buddyName != null) {
				String lcBuddyName = buddyName.toLowerCase();
				myOnlineBuddies.remove(lcBuddyName);
				myOfflineBuddies.add(lcBuddyName);
			}
		}

		public void buddyOnline(String buddyName, Buddy buddy) {
			if (buddyName != null) {
				String lcBuddyName = buddyName.toLowerCase();
				myOfflineBuddies.remove(lcBuddyName);
				myOnlineBuddies.add(lcBuddyName);
			}
		}

		public void removeBuddy(String buddyName) {
			
		}
		public void newBuddyList(Buddy[] buddies) {
			synchronized (myBuddies) {
				myBuddies.clear();
				myOnlineBuddies.clear();
				myOfflineBuddies.clear();
				for (int i = 0; i < buddies.length; i++) {
					myBuddies.add(buddies[i].getName().toLowerCase());
				}
			}
		}

		public void loginDone(DaimLoginEvent event) {
			super.loginDone(event);
			myConnected = true;
		}

		public void incomingICQ(UserInfo userInfo, int arg1, int arg2, String message) {
			super.incomingICQ(userInfo, arg1, arg2, message);
			if (userInfo != null) {
				message = message.replaceAll("\\<.*?\\>", "");
				DaimInstantMessenger.this.fireMessageReceived(userInfo.getSN(), message);
			}
		}

		public void incomingIM(Buddy buddy, UserInfo userInfo, AOLIM im) {
			super.incomingIM(buddy, userInfo, im);
			String message = im.getMsg();
			if (buddy != null) {
				message = message.replaceAll("\\<.*?\\>", "");
				DaimInstantMessenger.this.fireMessageReceived(buddy.getName(), message);
			}
		}

		public void login(String screenName, String password) throws IOException {
			super.login(screenName, password);
		}

		public void loginError(DaimLoginEvent event) {
			super.loginError(event);
			myConnected = false;
		}

		public void logout() {
			super.logout();
			myConnected = false;
		}
	}

	public static class Factory implements IInstantMessengerFactory {
		public IInstantMessenger createInstantMessenger(String screenName, String password) {
			return new DaimInstantMessenger(screenName, password);
		}
	}
}
