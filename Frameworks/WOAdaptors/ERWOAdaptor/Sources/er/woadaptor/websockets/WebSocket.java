package er.woadaptor.websockets;

import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;


public interface WebSocket {
	/**
	 * Method to be called on the WebSocket when its channel is closed.
	 */
	public void didClose();

	/**
	 * Called after returning the upgrade response to the client
	 */
	public void didUpgrade();
	
	/**
	 * Handles WebSocketFrames received by this socket's channel
	 * 
	 * @param frame the WebSocketFrame received
	 */
	public void receiveFrame(WebSocketFrame frame);
	
	/**
	 * Method used to write WebSocketFrames to this socket's channel
	 * 
	 * @param frame The message to send
	 */
	public void sendFrame(WebSocketFrame frame);
}
