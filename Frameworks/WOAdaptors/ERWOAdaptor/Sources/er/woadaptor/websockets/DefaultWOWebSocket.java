package er.woadaptor.websockets;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

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
	
	@Override
	public void didClose() {
		//Do nothing
	}

	@Override
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
	
	@Override
	public void receiveFrame(WebSocketFrame frame) {
		ERXApplication._startRequest();
		try {
			if(frame.isText()) {
				receive(frame.getTextData());
			} else if(frame.isBinary() && frame.getBinaryData().hasArray()) {
				//Not supported. May change drastically.
				receive(frame.getBinaryData().array());
			}
		} finally {
			ERXApplication._endRequest();
		}
	}
	
	@Override
	public void sendFrame(WebSocketFrame frame) {
		channel().write(frame);
	}
	
	public void receive(String message) {}

	public void receive(byte[] message) {
		throw new UnsupportedOperationException();
	}

	public void send(String message) {
		//FIXME break long messages into smaller pieces?
		WebSocketFrame frame = new DefaultWebSocketFrame(message);
		channel().write(frame);
	}

	public void send(byte[] message) {
		throw new UnsupportedOperationException();
	}

}
