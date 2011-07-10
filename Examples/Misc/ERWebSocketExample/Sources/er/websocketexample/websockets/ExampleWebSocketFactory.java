package er.websocketexample.websockets;
import org.jboss.netty.channel.Channel;

import er.woadaptor.websockets.AbstractWOWebSocketFactory;


public class ExampleWebSocketFactory extends AbstractWOWebSocketFactory {

	@Override
	public ExampleWebSocket create(Channel channel) {
		ExampleWebSocket socket = new ExampleWebSocket(channel);
		return socket;
	}

}
