package er.woadaptor.websockets;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpRequest;

public interface WebSocketFactory {
	
	public WebSocket create(Channel channel, HttpRequest req);
	
}
