package com.webobjects.appserver;

import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver._private.WOProperties;
import com.webobjects.foundation.NSDelayedCallbackCenter;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotificationCenter;

import er.woadaptor.ERWOAdaptorUtilities;
import er.woadaptor.websockets.WebSocket;
import er.woadaptor.websockets.WebSocketFactory;
import er.woadaptor.websockets.WebSocketStore;

/**
 * How to use the WONettyAdaptor:
 *
 * 1. Build/Install ERWOAdaptor framework
 * 2. Include ERWOAdaptor framework in your app/project
 * 3. Run your app with the property:
 *	
 *	 -WOAdaptor er.woadaptor.ERWOAdaptor 
 *
 *  OR:
 *  
 *	 -WOAdaptor WONettyAdaptor
 *
 * 4. (Optional) If developing with the WONettyAdaptor set the following properties as well:
 * 
 *   -WODirectConnectEnabled false
 *   
 *  AND (maybe) 
 *   
 *   -WOAllowRapidTurnaround false
 * 
 * @author ravim
 * @author ramsey (WebSocket support)
 * 
 * @version 2.0
 */
public class WONettyAdaptor extends WOAdaptor {
    private static final Logger log = LoggerFactory.getLogger(WONettyAdaptor.class);
    
    private int _port;
    private String _hostname;
    
    private ChannelFactory channelFactory;
    private Channel channel;
    
    private String hostname() {
    	if (_hostname == null) {
    		try {
    			InetAddress _host = InetAddress.getLocalHost();
				_hostname = _host.getHostName();
			} catch (UnknownHostException e) {
				log.error("Failed to get localhost address.", e);
			}
    	}
    	return _hostname;
    }

	public WONettyAdaptor(String name, NSDictionary<String, Object> config) {
        super(name, config);

        Number number = (Number) config.objectForKey(WOProperties._PortKey);
        if (number != null)
            _port = number.intValue();
        if (_port < 0)
            _port = 0;
        
        _hostname = (String) config.objectForKey(WOProperties._HostKey);
        WOApplication.application()._setHost(hostname());
	}

	@Override
	public void registerForEvents() {
		// Configure the server.
		channelFactory = new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new PipelineFactory());

		// Bind and start to accept incoming connections.
		channel = bootstrap.bind(new InetSocketAddress(hostname(), _port));
		
		log.debug("Binding adaptor to address: {}", channel.getLocalAddress());
		_port = ((InetSocketAddress) channel.getLocalAddress()).getPort();
		System.setProperty(WOProperties._PortKey, Integer.toString(_port));
	}

	@Override
	public void unregisterForEvents() {
		ChannelFuture future = channel.close();
		future.awaitUninterruptibly();
		channelFactory.releaseExternalResources();
	}
		
	@Override
	public int port() {
		return _port;
	}
	
	@Override
	public boolean dispatchesRequestsConcurrently() {
		return true;
	}
	
	/**
	  * Originally inspired by: 
	  * 
	  * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
	  * @author Andy Taylor (andy.taylor@jboss.org)
	  * @author <a href="http://gleamynode.net/">Trustin Lee</a>
	  * 
	  * @see <a href="http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/HttpServerPipelineFactory.html">HttpServerPipelineFactory</a>
	  * 
	  * @author ravim 	ERWOAdaptor/WONettyAdaptor
	  * 
	  * @property WOMaxIOBufferSize 		Max http chunking size. Defaults to WO default 8196 
	  * 									@see <a href="http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/handler/codec/http/HttpMessageDecoder.html">HttpMessageDecoder</a>
	  * 
	  * @property WOFileUpload.sizeLimit	Max file upload size permitted
	  */
	protected static class PipelineFactory implements ChannelPipelineFactory {
		
		// TODO ravi: CHECKME Netty default is 8192; WO default is 8196(!?)
		public final Integer maxChunkSize = Integer.getInteger("WOMaxIOBufferSize", 8196);
		public final Integer maxFileSize = Integer.getInteger("WOFileUpload.sizeLimit", 1024*1024*100);
		
		public ChannelPipeline getPipeline() throws Exception {
			// Create a default pipeline implementation.
			ChannelPipeline pipeline = pipeline();

			pipeline.addLast("decoder", new HttpRequestDecoder(4096, 8192, maxChunkSize));
			pipeline.addLast("aggregator", new HttpChunkAggregator(maxFileSize));
			pipeline.addLast("encoder", new HttpResponseEncoder());
			pipeline.addLast("handler", new RequestHandler());
			return pipeline;
		}
	}

	/**
	 * Originally inspired by:
	 * 
	 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
	 * @author Andy Taylor (andy.taylor@jboss.org)
	 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
	 * 
	 * @see <a href="http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/HttpRequestHandler.html">HttpRequestHandler</a>
	 * 
	 * @author ravim 	ERWOAdaptor/WONettyAdaptor version
	 */
	protected static class RequestHandler extends SimpleChannelUpstreamHandler {
		private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
		
		private WebSocketServerHandshaker handshaker;

		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
			super.channelClosed(ctx, e);
			NSNotificationCenter.defaultCenter().postNotification(WebSocketStore.CHANNEL_CLOSED_NOTIFICATION, ctx.getChannel());
		}
		
		/**
		 * @see <a href="http://docs.jboss.org/netty/3.2/api/org/jboss/netty/channel/SimpleChannelUpstreamHandler.html#messageReceived(org.jboss.netty.channel.ChannelHandlerContext,%20org.jboss.netty.channel.MessageEvent)">SimpleChannelUpstreamHandler</a>
		 */
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			Object msg = e.getMessage();
			if(msg instanceof WebSocketFrame) {
				handleWebSocketFrame(ctx, e, (WebSocketFrame)msg);
			} else if(msg instanceof HttpRequest) {
				handleHTTPRequest(ctx, e, (HttpRequest)msg);
			}
		}
		
		protected void handleWebSocketFrame(ChannelHandlerContext ctx, MessageEvent e, WebSocketFrame frame) {
			WebSocket socket = WebSocketStore.defaultWebSocketStore().socketForChannel(e.getChannel());
	        if (frame instanceof CloseWebSocketFrame) {
	        	//TODO remove from store?
	            handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
	        } else if (frame instanceof PingWebSocketFrame) {
	            ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
	        } else if(socket != null) {
				socket.receiveFrame(frame);
			}
		}
		
		protected void handleHTTPRequest(ChannelHandlerContext ctx, MessageEvent e, HttpRequest _request) throws IOException {
			
			if(_request.getHeader(Names.SEC_WEBSOCKET_VERSION) != null || 
					(Values.UPGRADE.equalsIgnoreCase(_request.getHeader(Names.CONNECTION)) && Values.WEBSOCKET.equalsIgnoreCase(_request.getHeader(Names.UPGRADE)))
					) {
				
				handleUpgradeRequest(ctx, _request);
			} else {
				
				WORequest worequest = ERWOAdaptorUtilities.asWORequest(_request);
				worequest._setOriginatingAddress(((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress());
				WOResponse woresponse = WOApplication.application().dispatchRequest(worequest);
	
				// send a response
				NSDelayedCallbackCenter.defaultCenter().eventEnded();
	
				// Decide whether to close the connection or not.
				boolean keepAlive = isKeepAlive(_request);
	
				//For reasons that escape me, empty responses fail to close properly.
				boolean close = !(woresponse._contentLength() > 0 || woresponse.contentInputStream() != null);

				// Write the response.
				HttpResponse response = ERWOAdaptorUtilities.asHttpResponse(woresponse);
				ChannelFuture future = e.getChannel().write(response);
	
				// Close the non-keep-alive connection after the write operation is done.
				if (close || !keepAlive) {
					future.addListener(ChannelFutureListener.CLOSE);
				}
			}
		}
		
		protected void handleUpgradeRequest(ChannelHandlerContext ctx, HttpRequest req) {
			//If factory doesn't exist, close the upgrade request channel
			WebSocketFactory factory = WebSocketStore.defaultWebSocketStore().factory();
			if(factory == null) {
				ctx.getChannel().close();
				return;
			}
			
			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
	                ERWOAdaptorUtilities.getWebSocketLocation(req), null, false);
			handshaker = wsFactory.newHandshaker(req);
			
			Channel socketChannel = ctx.getChannel();
			if(handshaker == null) {
				wsFactory.sendUnsupportedWebSocketVersionResponse(socketChannel);
			} else {
				ChannelFuture future = handshaker.handshake(socketChannel, req);
				//TODO tie this to the channel future result?
				//Create a WebSocket instance to handle frames
				WebSocket socket = factory.create(socketChannel, req);
				WebSocketStore.defaultWebSocketStore().takeSocketForChannel(socket, socketChannel);
				
				socket.didUpgrade();
			}
		}

		/**
		 * @see <a href="http://docs.jboss.org/netty/3.2/api/org/jboss/netty/channel/SimpleChannelUpstreamHandler.html#exceptionCaught(org.jboss.netty.channel.ChannelHandlerContext,%20org.jboss.netty.channel.ExceptionEvent)">SimpleChannelUpstreamHandler</a>
		 */
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
	        Throwable cause = e.getCause();

			log.warn(cause.getMessage());
			e.getChannel().close();
		}
	}
}
