package com.webobjects.appserver;

import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.logging.CommonsLoggerFactory;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.util.CharsetUtil;

import com.webobjects.appserver._private.WOProperties;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDelayedCallbackCenter;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSMutableDictionary;

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
 *   -WOAllowRapidTurnaround false
 *   -WODirectConnectEnabled false
 * 
 * @author ravim
 * 
 * @version 1.0
 */
public class WONettyAdaptor extends WOAdaptor {

    private static final Logger log = Logger.getLogger(WONettyAdaptor.class);

    private int _port;
    private String _hostname;
    
    private ChannelFactory channelFactory;
    private Channel channel;
    
    private String hostname() {
    	if (_hostname == null) {
    		try {
    			InetAddress _host = InetAddress.getLocalHost();
				_hostname = _host.getHostName();
			} catch (UnknownHostException exception) {
				log.error("Failed to get localhost address");
			}
    	}
    	return _hostname;
    }

	public WONettyAdaptor(String name, NSDictionary config) {
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
		
		log.debug("Binding adaptor to address: " + channel.getLocalAddress());
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
	  * @property WOMaxIOBufferSize 	Max http chunking size. Defaults to WO default 8196 
	  * 								@see <a href="http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/handler/codec/http/HttpMessageDecoder.html">HttpMessageDecoder</a>
	  * 								{@link org.jboss.netty.handler.codec.http.HttpRequestDecoder}
	  */
	protected class PipelineFactory implements ChannelPipelineFactory {
		
		public final Integer maxChunkSize = Integer.getInteger("WOMaxIOBufferSize", 8196);		// TODO ravi: CHECKME Netty default is 8192; WO default is 8196(!?)
		
		public ChannelPipeline getPipeline() throws Exception {
			// Create a default pipeline implementation.
			ChannelPipeline pipeline = pipeline();

			// Uncomment the following line if you want HTTPS
			//SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
			//engine.setUseClientMode(false);
			//pipeline.addLast("ssl", new SslHandler(engine));

			pipeline.addLast("decoder", new HttpRequestDecoder(4096, 8192, maxChunkSize));
			pipeline.addLast("aggregator", new HttpChunkAggregator(1024*1024*100));		//TODO turn into property
			pipeline.addLast("encoder", new HttpResponseEncoder());
			// Remove the following line if you don't want automatic content compression.
			pipeline.addLast("deflater", new HttpContentCompressor());
			pipeline.addLast("handler", new RequestHandler());
			return pipeline;
		}
	}
	
	// error responses
	private static HttpResponse _badRequestResponse;
    private static HttpResponse _internalServerErrorResponse;
	static {
        _internalServerErrorResponse = new DefaultHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
        _internalServerErrorResponse.setContent(ChannelBuffers.copiedBuffer(INTERNAL_SERVER_ERROR.getReasonPhrase(), CharsetUtil.UTF_8));
        _internalServerErrorResponse.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        
        _badRequestResponse = new DefaultHttpResponse(HTTP_1_1, BAD_REQUEST);
        _badRequestResponse.setContent(ChannelBuffers.copiedBuffer("Failure: " + BAD_REQUEST.toString() + "\r\n", CharsetUtil.UTF_8));
        _badRequestResponse.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
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
	protected class RequestHandler extends SimpleChannelUpstreamHandler {
		
		private InternalLogger log = CommonsLoggerFactory.getDefaultFactory().newInstance(this.getClass().getName());

		private HttpRequest _request;
		private boolean readingChunks;
		private ChannelBuffer _content;

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (!readingChunks) {
				HttpRequest request = this._request = (HttpRequest) e.getMessage();

				_content = request.getContent();
				WORequest worequest = _convertHttpRequestToWORequest(_request);
				worequest._setOriginatingAddress(((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress());
				WOResponse woresponse = WOApplication.application().dispatchRequest(worequest);

				// send a response
				NSDelayedCallbackCenter.defaultCenter().eventEnded();
				writeResponse(new WOResponseWrapper(woresponse), e);
			}
		}
		
		private WORequest _convertHttpRequestToWORequest(HttpRequest request) throws IOException {
			// headers
	        NSMutableDictionary<String, NSArray<String>> headers = new NSMutableDictionary<String, NSArray<String>>();
	        for (Map.Entry<String, String> header: request.getHeaders()) {
	        	headers.setObjectForKey(new NSArray<String>(header.getValue().split(",")), header.getKey());
	        }
	        
	        // content
			NSData contentData = (_content.readable()) ? new NSMutableData(new ChannelBufferInputStream(_content), 4096) : NSData.EmptyData;	        
			
			// create request
			WORequest _worequest = WOApplication.application().createRequest(
					request.getMethod().getName(), 
					request.getUri(), 
					request.getProtocolVersion().getText(), 
	        		headers,
	        		contentData, 
	        		null);
			
			// cookies
			String cookieString = _request.getHeader(COOKIE);
			if (cookieString != null) {
				CookieDecoder cookieDecoder = new CookieDecoder();
				Set<Cookie> cookies = cookieDecoder.decode(cookieString);
				if(!cookies.isEmpty()) {
					for (Cookie cookie : cookies) {
						WOCookie wocookie = _convertCookieToWOCookie(cookie);
						_worequest.addCookie(wocookie);
					}
				}
			} 
			
			return _worequest;
		}
		
		private WOCookie _convertCookieToWOCookie(Cookie cookie) {
			WOCookie wocookie = new WOCookie(
					cookie.getName(),
					cookie.getValue(),
					cookie.getPath(),
					cookie.getDomain(),
					cookie.getMaxAge(),
					cookie.isSecure());
			return wocookie;
		}

		private void writeResponse(HttpResponse response, MessageEvent e) throws IOException {
			// Decide whether to close the connection or not.
			boolean keepAlive = isKeepAlive(_request);

			// Write the response.
			ChannelFuture future = e.getChannel().write(response);

			// Close the non-keep-alive connection after the write operation is done.
			if (!keepAlive) {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
	        Throwable cause = e.getCause();
	        /*
	        if (cause instanceof TooLongFrameException) {
	            ctx.getChannel().write(_badRequestResponse).addListener(ChannelFutureListener.CLOSE);
	            return;
	        } */

			log.warn(cause.getMessage());
			e.getChannel().close();
			/*
	        if (ctx.getChannel().isConnected()) {
	            ctx.getChannel().write(_internalServerErrorResponse).addListener(ChannelFutureListener.CLOSE);
	        } */
		}
	}
}
