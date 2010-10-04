package com.webobjects.appserver;

import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
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
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultCookie;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.logging.CommonsLoggerFactory;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.util.CharsetUtil;

import com.webobjects.appserver._private.WOInputStreamData;
import com.webobjects.appserver._private.WOProperties;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDelayedCallbackCenter;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * @author ravim
 */
public class WONettyAdaptor extends WOAdaptor {

    private static final Logger log = Logger.getLogger(WONettyAdaptor.class);

    private int _port;

    private String _host;
    
    private ChannelFactory channelFactory;
    private Channel channel;

	public WONettyAdaptor(String name, NSDictionary config) {
        super(name, config);

        Number number = (Number) config.objectForKey(WOProperties._PortKey);
        if (number != null)
            _port = number.intValue();
        if (_port < 0)
            _port = 0;
        WOApplication.application().setPort(_port);
        _host = (String) config.objectForKey(WOProperties._HostKey);
        WOApplication.application()._setHost(_host);
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
		channel = bootstrap.bind(new InetSocketAddress(_port));
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
	  * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
	  * @author Andy Taylor (andy.taylor@jboss.org)
	  * @author <a href="http://gleamynode.net/">Trustin Lee</a>
	  * 
	  * @see <a href="http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/HttpServerPipelineFactory.html">HttpServerPipelineFactory</a>
	  * 
	  * @author ravim ERWOAdaptor version
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
			// Uncomment the following line if you don't want to handle HttpChunks.
			//pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
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
	 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
	 * @author Andy Taylor (andy.taylor@jboss.org)
	 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
	 *
	 * @version $Rev: 2288 $, $Date: 2010-05-27 21:40:50 +0900 (Thu, 27 May 2010) $
	 * 
	 * @see <a href="http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/HttpRequestHandler.html">HttpRequestHandler</a>
	 * 
	 * @author ravim ERWOAdaptor version
	 */
	protected class RequestHandler extends SimpleChannelUpstreamHandler {
		
		private InternalLogger log = CommonsLoggerFactory.getDefaultFactory().newInstance(WONettyAdaptor.RequestHandler.class.getName());

		private HttpRequest _request;
		private boolean readingChunks;
		private ChannelBuffer _content;

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			if (!readingChunks) {
				HttpRequest request = this._request = (HttpRequest) e.getMessage();

				if (request.isChunked()) {
					readingChunks = true;
					_content = ChannelBuffers.EMPTY_BUFFER;
				} else {
					_content = request.getContent();
					WORequest worequest = _convertHttpRequestToWORequest(_request);
					worequest._setOriginatingAddress(((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress());
			        WOResponse woresponse = WOApplication.application().dispatchRequest(worequest);
			        
			        // send a response
			        NSDelayedCallbackCenter.defaultCenter().eventEnded();
			        writeResponse(woresponse, e);
				}
			} else {
				HttpChunk chunk = (HttpChunk) e.getMessage();
				_content = ChannelBuffers.copiedBuffer(_content, chunk.getContent());
				
				if (chunk.isLast()) {
					readingChunks = false;

					HttpChunkTrailer trailer = (HttpChunkTrailer) chunk;
					WORequest woreqest = _convertHttpChunkTrailerToWORequest(trailer);
			        WOResponse woresponse = WOApplication.application().dispatchRequest(woreqest);
			        
			        // send a response
			        NSDelayedCallbackCenter.defaultCenter().eventEnded();
					writeResponse(woresponse, e);
				}
			}
		}
		
		private WORequest _convertHttpRequestToWORequest(HttpRequest request) {
			// headers
	        NSMutableDictionary<String, NSArray<String>> headers = new NSMutableDictionary<String, NSArray<String>>();
	        for (Map.Entry<String, String> header: request.getHeaders()) {
	        	headers.setObjectForKey(new NSArray<String>(header.getValue().split(",")), header.getKey());
	        }
	        
	        // content
			NSData contentData = (_content.readable()) ? new NSData(ChannelBuffers.copiedBuffer(_content).array()) : NSData.EmptyData;	        
			
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
		
		private WORequest _convertHttpChunkTrailerToWORequest(HttpChunkTrailer trailer) {
			// headers
	        NSMutableDictionary<String, NSArray<String>> headers = new NSMutableDictionary<String, NSArray<String>>();
			if (!trailer.getHeaderNames().isEmpty()) {
				for (Map.Entry<String, String> header: trailer.getHeaders()) {
					headers.setObjectForKey(new NSArray<String>(header.getValue().split(",")), header.getKey());
				}
			}

			// content
			NSData contentData = (_content.readable()) ? new WOInputStreamData(new NSData(_content.array())) : NSData.EmptyData;

			// create request
			WORequest _worequest = WOApplication.application().createRequest(
	        		_request.getMethod().getName(), 			// FIXME should be trailer.getMethod().getName()
	        		_request.getUri(), 							// FIXME should be trailer.getUri()
	        		_request.getProtocolVersion().getText(), 	// FIXME should be trailer.getProtocolVersion().getText()
	        		headers,
	        		contentData, 
	        		null);
			// TODO set cookies (CHECKME is this really necessary here?!)
			return _worequest;
		}
		
		private HttpResponse _convertWOResponseToHttpResponse(WOResponse woresponse) throws IOException {
			// Build the response object.
			HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
			
			// get content from woresponse
			int length = woresponse._contentLength();
			if (length > 0) {
				String contentString = woresponse._content.toString();
				if (contentString!= null && contentString != "") {
					Charset charset = Charset.forName(woresponse.contentEncoding());
					response.setContent(ChannelBuffers.copiedBuffer(contentString, charset));
				} else
					response.setContent(ChannelBuffers.copiedBuffer(woresponse._contentData._bytesNoCopy()));
			} else if (woresponse.contentInputStream() != null) {
				ByteBuffer buffer = ByteBuffer.allocate(woresponse.contentInputStreamBufferSize());
				length = woresponse.contentInputStream().read(buffer.array());
				response.setContent(ChannelBuffers.copiedBuffer(buffer));
			}
			
			// set headers
			for (String headerKey: woresponse.headerKeys()) {
				String value = woresponse.headerForKey(headerKey);
				if (value != null) {
					if (value.contains(",")) {
						String[] values = value.split(",");
						response.setHeader(headerKey, values);
					} else response.setHeader(headerKey, value);
				}
			}
			//response.setHeader(CONTENT_LENGTH, length);

			// Encode the cookie.
			NSArray<WOCookie> wocookies = woresponse.cookies();
			if(!wocookies.isEmpty()) {
				// Reset the cookies if necessary.
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				for (WOCookie wocookie : wocookies) {
					Cookie cookie = _convertWOCookieToCookie(wocookie);
					cookieEncoder.addCookie(cookie);
				}
				response.addHeader(SET_COOKIE, cookieEncoder.encode());
			}
			return response;
		}
		
		private Cookie _convertWOCookieToCookie(WOCookie wocookie) {
			Cookie cookie = new DefaultCookie(wocookie.name(), wocookie.value());
			cookie.setPath(wocookie.path());
			cookie.setDomain(wocookie.domain());
			cookie.setMaxAge(wocookie.timeOut());
			cookie.setSecure(wocookie.isSecure());
			return cookie;
		}

		private void writeResponse(WOResponse woresponse, MessageEvent e) throws IOException {
			// Decide whether to close the connection or not.
			boolean keepAlive = isKeepAlive(_request);

			// Build the response object.
			HttpResponse response = _convertWOResponseToHttpResponse(woresponse);

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
	        if (cause instanceof TooLongFrameException) {
	            ctx.getChannel().write(_badRequestResponse).addListener(ChannelFutureListener.CLOSE);
	            return;
	        }

			log.warn("Exception caught", e.getCause());
	        if (ctx.getChannel().isConnected()) {
	            ctx.getChannel().write(_internalServerErrorResponse).addListener(ChannelFutureListener.CLOSE);
	        }
		}
	}
}
