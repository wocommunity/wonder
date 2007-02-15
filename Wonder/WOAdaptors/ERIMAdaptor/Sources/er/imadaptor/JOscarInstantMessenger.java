package er.imadaptor;

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
		if (_connected && _conn != null) {
			BuddyInfoManager buddyInfoManager = _conn.getBuddyInfoManager();
			if (buddyInfoManager != null) {
				BuddyInfo buddyInfo = buddyInfoManager.getBuddyInfo(new Screenname(buddyName));
				if (buddyInfo != null) {
					buddyOnline = buddyInfo.isOnline();
				}
			}
		}
		return buddyOnline;
	}

	public synchronized boolean isBuddyAway(String buddyName) {
		boolean buddyAway = false;
		if (_connected && _conn != null) {
			BuddyInfoManager buddyInfoManager = _conn.getBuddyInfoManager();
			if (buddyInfoManager != null) {
				BuddyInfo buddyInfo = buddyInfoManager.getBuddyInfo(new Screenname(buddyName));
				if (buddyInfo != null) {
					buddyAway = buddyInfo.isAway();
				}
			}
		}
		return buddyAway;
	}
	
	public synchronized String getStatusMessage(String buddyName) {
		String statusMessage = null;
		if (_connected && _conn != null) {
			BuddyInfoManager buddyInfoManager = _conn.getBuddyInfoManager();
			if (buddyInfoManager != null) {
				BuddyInfo buddyInfo = buddyInfoManager.getBuddyInfo(new Screenname(buddyName));
				if (buddyInfo != null) {
					statusMessage = buddyInfo.getStatusMessage();
				}
			}
		}
		return statusMessage;
	}
	
	public synchronized String getAwayMessage(String buddyName) {
		String awayMessage = null;
		if (_connected && _conn != null) {
			BuddyInfoManager buddyInfoManager = _conn.getBuddyInfoManager();
			if (buddyInfoManager != null) {
				BuddyInfo buddyInfo = buddyInfoManager.getBuddyInfo(new Screenname(buddyName));
				if (buddyInfo != null) {
					awayMessage = buddyInfo.getAwayMessage();
				}
			}
		}
		return awayMessage;
	}

	public void addBuddy(String buddyName) {
		// ignore
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
		if (_connected && _conn != null) {
			if (!isBuddyOnline(buddyName)) {
				if (!ignoreIfOffline) {
					throw new BuddyOfflineException("The buddy '" + buddyName + "' is not online.");
				}
			}
			else {
				Conversation conv = _conn.getIcbmService().getImConversation(new Screenname(buddyName));
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
