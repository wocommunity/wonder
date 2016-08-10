package er.woadaptor.websockets;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;

import com.webobjects.appserver.WOSession;

import er.extensions.appserver.ERXApplication;

public class DefaultWOWebSocket implements WebSocket {
	protected final Channel channel;
	
	public DefaultWOWebSocket(Channel channel) {
		this.channel = channel;
	}
	
	public Channel channel() {
		return channel;
	}
	
	public void didClose() {
		//Do nothing
	}

	public void didUpgrade() {
		//Do nothing
	}
	
	/**
	 * Danger! Do NOT stash an instance of the session on your websocket.
	 * Subclasses will be run on threads outside of the RR loop, so it is
	 * extremely unwise to try that. Also, do not keep references to ECs
	 * or EO instances in your websocket. Create new ECs when needed and 
	 * store references to EOs as globalIDs. Basically, the same general
	 * principals that apply to background threads apply to websockets.
	 * 
	 * @param session
	 */
	public void init(WOSession session) {}
	
	public void receiveFrame(WebSocketFrame frame) {
		ERXApplication._startRequest();
		try {
			if(frame instanceof TextWebSocketFrame) {
				TextWebSocketFrame textFrame = (TextWebSocketFrame)frame;
				receive(textFrame.getText());
			} else if(frame instanceof BinaryWebSocketFrame && frame.getBinaryData().hasArray()) {
				//Not supported. May change drastically.
				receive(frame.getBinaryData().array());
			} else {
				String message = String.format("%s frame types not supported", frame.getClass().getName());
				throw new UnsupportedOperationException(message);
			}
		} finally {
			ERXApplication._endRequest();
		}
	}
	
	public void sendFrame(WebSocketFrame frame) {
		channel().write(frame);
	}
	
	public void receive(String message) {}

	public void receive(byte[] message) {
		throw new UnsupportedOperationException();
	}

	public void send(String message) {
		WebSocketFrame frame = new TextWebSocketFrame(message);
		channel().write(frame);
	}

	public void send(byte[] message) {
		throw new UnsupportedOperationException();
	}

}
