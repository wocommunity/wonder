package er.woadaptor.websockets;

import java.io.IOException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSForwardException;

import er.extensions.appserver.ERXApplication;
import er.woadaptor.ERWOAdaptorUtilities;

public abstract class AbstractWOWebSocketFactory implements WebSocketFactory {

	/**
	 * Adds WO specific extensions to WebSocket creation. Do not override this
	 * create method. Instead, override the {@link #create(Channel)} method
	 * instead.
	 */
	public WebSocket create(Channel channel, HttpRequest req) {
		ERXApplication._startRequest();
		try {
			WORequest request = null;
			try {
				request = ERWOAdaptorUtilities.asWORequest(req);
			} catch (IOException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
			WOSession session = ERWOAdaptorUtilities.existingSession(request);
			DefaultWOWebSocket socket = create(channel);
			socket.init(session);
			if (session != null) {
				WOApplication.application().saveSessionForContext(session.context());
			}
			return socket;
		} finally {
			ERXApplication._endRequest();
		}
	}

	/**
	 * Override this abstract method to return an instance of your own
	 * WOWebSocket subclass.
	 * 
	 * @param channel
	 *            the channel for the WebSocket
	 * @return a new websocket
	 */
	public abstract DefaultWOWebSocket create(Channel channel);
}
