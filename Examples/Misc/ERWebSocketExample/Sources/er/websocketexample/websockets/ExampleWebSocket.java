package er.websocketexample.websockets;

import org.jboss.netty.channel.Channel;

import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.foundation.ERXSelectorUtilities;
import er.websocketexample.Session;
import er.woadaptor.websockets.DefaultWOWebSocket;

/**
 * A simple example websocket
 */
public class ExampleWebSocket extends DefaultWOWebSocket {
	public static final String CHAT_MESSAGE_RECEIVED = "ChatMessageReceived";
	private NSSelector<Void> sel = ERXSelectorUtilities.notificationSelector("chatMessage");
	private String username;

	public ExampleWebSocket(Channel channel) {
		super(channel);
	}

	/**
	 * Posts a notification that a chat message is received
	 */
	@Override
	public void receive(String message) {
		NSDictionary<String, String> userInfo = new NSDictionary<String, String>(
				new String[] { message, username }, new String[] { "message", "username" });
		NSNotificationCenter.defaultCenter().postNotification(CHAT_MESSAGE_RECEIVED, this, userInfo);
	}

	/**
	 * Removes this socket as an observer for chat notifications when the socket
	 * is closed
	 */
	@Override
	public void didClose() {
		NSNotificationCenter.defaultCenter().removeObserver(this);
	}

	/**
	 * Adds this socket as an observer for chat messages after the socket
	 * upgrade is complete
	 */
	@Override
	public void didUpgrade() {
		NSNotificationCenter.defaultCenter().addObserver(this, sel, CHAT_MESSAGE_RECEIVED, null);
	}

	/**
	 * Receives the chat message received notification and sends the chat text
	 * to this socket's channel
	 * 
	 * @param n
	 */
	public void chatMessage(NSNotification n) {
		String message = (String) n.userInfo().objectForKey("message");
		String username = (String) n.userInfo().objectForKey("username");
		send(username + ": " + message);
	}

	/**
	 * Grabs the username from the session. Whatever you do, don't try to keep
	 * a copy of the session locally. That would be a bad idea.
	 */
	@Override
	public void init(WOSession session) {
		Session s = (Session) session;
		username = s.username();
	}

	public String username() {
		return username;
	}

}
