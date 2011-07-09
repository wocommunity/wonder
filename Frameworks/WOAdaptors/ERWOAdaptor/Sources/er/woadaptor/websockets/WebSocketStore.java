package er.woadaptor.websockets;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.Channel;

import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXConstant;

public class WebSocketStore {
	private static final NSSelector<Void> CHANNEL_CLOSED = new NSSelector<Void>("channelClosed", ERXConstant.NotificationClassArray);
	private static final WebSocketStore _store = new WebSocketStore();
	
	private final Map<Channel, WebSocket> _map = Collections.synchronizedMap(new HashMap<Channel, WebSocket>());
	private WebSocketFactory _factory;
	
    public static final String CHANNEL_CLOSED_NOTIFICATION = "WebSocketStoreChannelClosed";
	
	public static WebSocketStore defaultWebSocketStore() {
		return _store;
	}

	public WebSocket socketForChannel(Channel channel) {
		return _map.get(channel);
	}
	
	public void takeSocketForChannel(WebSocket socket, Channel channel) {
		NSNotificationCenter.defaultCenter().addObserver(this, CHANNEL_CLOSED, CHANNEL_CLOSED_NOTIFICATION, channel);
		_map.put(channel, socket);
	}
	
	public WebSocketFactory factory() {
		return _factory;
	}
	
	public void setFactory(WebSocketFactory factory) {
		_factory = factory;
	}
	
	public void channelClosed(NSNotification n) {
		Channel channel = (Channel)n.object();
		WebSocket socket = _map.remove(channel);
		if(socket != null) {
			socket.didClose();
		}
	}
}
