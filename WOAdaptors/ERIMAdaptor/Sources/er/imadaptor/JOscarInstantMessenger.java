package er.imadaptor;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.kano.joustsim.Screenname;
import net.kano.joustsim.oscar.AimConnection;
import net.kano.joustsim.oscar.AimConnectionProperties;
import net.kano.joustsim.oscar.AimSession;
import net.kano.joustsim.oscar.BuddyInfo;
import net.kano.joustsim.oscar.BuddyInfoManager;
import net.kano.joustsim.oscar.DefaultAimSession;
import net.kano.joustsim.oscar.State;
import net.kano.joustsim.oscar.StateEvent;
import net.kano.joustsim.oscar.StateListener;
import net.kano.joustsim.oscar.oscar.service.icbm.Conversation;
import net.kano.joustsim.oscar.oscar.service.icbm.ConversationEventInfo;
import net.kano.joustsim.oscar.oscar.service.icbm.ConversationListener;
import net.kano.joustsim.oscar.oscar.service.icbm.IcbmBuddyInfo;
import net.kano.joustsim.oscar.oscar.service.icbm.IcbmListener;
import net.kano.joustsim.oscar.oscar.service.icbm.IcbmService;
import net.kano.joustsim.oscar.oscar.service.icbm.Message;
import net.kano.joustsim.oscar.oscar.service.icbm.MessageInfo;
import net.kano.joustsim.oscar.oscar.service.icbm.SimpleMessage;
import net.kano.joustsim.oscar.oscar.service.ssi.Buddy;
import net.kano.joustsim.oscar.oscar.service.ssi.MutableBuddyList;
import net.kano.joustsim.oscar.oscar.service.ssi.MutableGroup;
import net.kano.joustsim.oscar.oscar.service.ssi.SsiService;

public class JOscarInstantMessenger extends AbstractInstantMessenger {
	private AimSession _aimSession;
	private AimConnection _conn;
	private IcbmHandler _icbmHandler;
	private ConversationHandler _conversationHandler;

	private boolean _connected;
	private long _lastConnectionAttempt;

	public JOscarInstantMessenger(String screenName, String password) {
		super(screenName, password);
		_icbmHandler = new IcbmHandler();
		_conversationHandler = new ConversationHandler();
	}

	protected class AimStateHandler implements StateListener {
		public void handleStateChange(StateEvent event) {
			State state = event.getNewState();
			if (state == State.ONLINE) {
				_conn = event.getAimConnection();
				IcbmService icbmService = _conn.getIcbmService();
				icbmService.addIcbmListener(_icbmHandler);
				_connected = true;
			}
			else {
				AimConnection conn = event.getAimConnection();
				IcbmService icbmService = conn.getIcbmService();
				if (icbmService != null) {
					icbmService.removeIcbmListener(_icbmHandler);
				}
				_connected = false;
			}
		}
	}

	protected class IcbmHandler implements IcbmListener {
		public void buddyInfoUpdated(IcbmService service, Screenname sn, IcbmBuddyInfo buddyInfo) {
		}

		public void newConversation(IcbmService service, Conversation conv) {
			conv.addConversationListener(_conversationHandler);
		}

		public void sendAutomaticallyFailed(IcbmService service, Message message, Set conversations) {
		}
	}

	public synchronized boolean isBuddyOnline(String buddyName) {
		boolean buddyOnline = false;
		BuddyInfo buddyInfo = _addBuddyIfNecessary(buddyName);
		if (buddyInfo != null) {
			buddyOnline = buddyInfo.isOnline();
		}
		return buddyOnline;
	}

	public synchronized boolean isBuddyAway(String buddyName) {
		boolean buddyAway = false;
		BuddyInfo buddyInfo = _addBuddyIfNecessary(buddyName);
		if (buddyInfo != null) {
			buddyAway = buddyInfo.isAway();
		}
		return buddyAway;
	}

	public synchronized String getStatusMessage(String buddyName) {
		String statusMessage = null;
		BuddyInfo buddyInfo = _addBuddyIfNecessary(buddyName);
		if (buddyInfo != null) {
			statusMessage = buddyInfo.getStatusMessage();
		}
		return statusMessage;
	}

	public synchronized String getAwayMessage(String buddyName) {
		String awayMessage = null;
		BuddyInfo buddyInfo = _addBuddyIfNecessary(buddyName);
		if (buddyInfo != null) {
			awayMessage = buddyInfo.getAwayMessage();
		}
		return awayMessage;
	}

	public void removeBuddy(String buddyName) {
		if (_connected && _conn != null) {
			Screenname buddyScreenName = new Screenname(buddyName);
			BuddyInfoManager buddyInfoManager = _conn.getBuddyInfoManager();
			SsiService ssiService = _conn.getSsiService();
			MutableBuddyList buddyList = ssiService.getBuddyList();
			List groups = buddyList.getGroups();
			Iterator groupsIter = groups.iterator();
			while (groupsIter.hasNext()) {
				MutableGroup group = (MutableGroup) groupsIter.next();
				Buddy matchingBuddy = null;
				Iterator buddiesIter = group.getBuddiesCopy().iterator();
				while (matchingBuddy == null && buddiesIter.hasNext()) {
					Buddy buddy = (Buddy) buddiesIter.next();
					if (buddy.getScreenname().equals(buddyScreenName)) {
						matchingBuddy = buddy;
					}
				}
				if (matchingBuddy != null) {
					group.deleteBuddy(matchingBuddy);
				}
			}
		}
	}

	public void addBuddy(String buddyName) {
		_addBuddyIfNecessary(buddyName);
	}

	public BuddyInfo _addBuddyIfNecessary(String buddyName) {
		BuddyInfo buddyInfo = null;
		if (_connected && _conn != null) {
			Screenname buddy = new Screenname(buddyName);
			BuddyInfoManager buddyInfoManager = _conn.getBuddyInfoManager();
			buddyInfo = buddyInfoManager.getBuddyInfo(buddy);
			boolean isOnBuddyList = false;
			if (buddyInfo != null) {
				isOnBuddyList = buddyInfo.isOnBuddyList();
			}
			if (!isOnBuddyList) {
				SsiService ssiService = _conn.getSsiService();
				MutableBuddyList buddyList = ssiService.getBuddyList();
				List groups = buddyList.getGroups();
				if (groups.size() == 0) {
					buddyList.addGroup("Buddies");
					groups = buddyList.getGroups();
				}
				MutableGroup group = (MutableGroup) groups.get(0);
				group.addBuddy(buddyName);

				for (int attempt = 0; attempt < 10 && !buddyInfo.isOnBuddyList(); attempt++) {
					try {
						Thread.sleep(100);
					}
					catch (Throwable t) {
						// ignore
					}
					buddyInfo = buddyInfoManager.getBuddyInfo(buddy);
				}
			}
		}
		return buddyInfo;
	}

	public synchronized void connect() throws IMConnectionException {
		if (_connected || _conn != null) {
			disconnect();
		}
		long now = System.currentTimeMillis();
		if (now - _lastConnectionAttempt > (1000 * 60 * 15)) {
			_lastConnectionAttempt = now;

			Screenname sn = new Screenname(getScreenName());
			_aimSession = new DefaultAimSession(sn);
			AimConnectionProperties props = new AimConnectionProperties(sn, getPassword());
			AimConnection conn = _aimSession.openConnection(props);
			conn.addStateListener(new AimStateHandler());
			conn.connect();
		}
		else {
			throw new ConnectedTooFastException("You attempted to connect repeatedly too quickly.");
		}
	}

	public synchronized void disconnect() {
		if (_connected) {
			if (_conn != null) {
				_conn.disconnect();
				_conn = null;
				_aimSession = null;
			}
			_connected = false;
		}
	}

	public synchronized boolean isConnected() {
		return _connected;
	}

	public synchronized void sendMessage(String buddyName, String message, boolean ignoreIfOffline) throws MessageException {
		BuddyInfo buddyInfo = _addBuddyIfNecessary(buddyName);
		if (buddyInfo != null) {
			if (!buddyInfo.isOnline()) {
				if (!ignoreIfOffline) {
					throw new BuddyOfflineException("The buddy '" + buddyName + "' is not online.");
				}
			}
			else {
				if (message.length() > 2048) {
					message = message.substring(0, 2048);
				}
				Conversation conv = _conn.getIcbmService().getImConversation(buddyInfo.getScreenname());
				conv.open();
				conv.sendMessage(new SimpleMessage(message));
			}
		}
	}

	protected class ConversationHandler implements ConversationListener {
		public void canSendMessageChanged(Conversation c, boolean canSend) {
			// do nothing
		}

		public void conversationClosed(Conversation c) {
			// do nothing
		}

		public void conversationOpened(Conversation c) {
			// do nothing
		}

		public void gotMessage(Conversation c, MessageInfo minfo) {
			JOscarInstantMessenger.this.fireMessageReceived(minfo.getFrom().getNormal(), minfo.getMessage().getMessageBody());
		}

		public void gotOtherEvent(Conversation conversation, ConversationEventInfo event) {
			// do nothing
		}

		public void sentMessage(Conversation c, MessageInfo minfo) {
			// do nothing
		}

		public void sentOtherEvent(Conversation conversation, ConversationEventInfo event) {
			// do nothing
		}
	}

	public static class Factory implements IInstantMessengerFactory {
		public IInstantMessenger createInstantMessenger(String screenName, String password) {
			return new JOscarInstantMessenger(screenName, password);
		}
	}
}
