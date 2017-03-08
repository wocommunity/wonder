package er.woadaptor;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOCookie;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOResponseWrapper;
import com.webobjects.appserver.WOSession;
import com.webobjects.appserver._private.WOInputStreamData;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableDictionary;

public class ERWOAdaptorUtilities {
	
	/**
	 * Converts a Netty HttpRequest to a WORequest
	 * 
	 * @param request	Netty HttpRequest
	 * @return	a WORequest
	 * @throws IOException
	 */
	public static WORequest asWORequest(HttpRequest request) throws IOException {
		// headers
        NSMutableDictionary<String, NSArray<String>> headers = new NSMutableDictionary<String, NSArray<String>>();
        for (Map.Entry<String, String> header: request.getHeaders()) {
        	headers.setObjectForKey(new NSArray<>(header.getValue().split(",")), header.getKey());
        }
        
        // content
        ChannelBuffer _content = request.getContent();
		NSData contentData = (_content.readable()) ? new WOInputStreamData(new NSData(new ChannelBufferInputStream(_content), 4096)) : NSData.EmptyData;	        
		
		// create request
		WORequest _worequest = WOApplication.application().createRequest(
				request.getMethod().getName(), 
				request.getUri(), 
				request.getProtocolVersion().getText(), 
        		headers,
        		contentData, 
        		null);
		
		// cookies
		String cookieString = request.getHeader(Names.COOKIE);
		if (cookieString != null) {
			CookieDecoder cookieDecoder = new CookieDecoder();
			Set<Cookie> cookies = cookieDecoder.decode(cookieString);
			if(!cookies.isEmpty()) {
				for (Cookie cookie : cookies) {
					WOCookie wocookie = asWOCookie(cookie);
					_worequest.addCookie(wocookie);
				}
			}
		} 
		
		return _worequest;
	}
	
	/**
	 * Converts a Netty Cookie to a WOCookie
	 * 
	 * @param cookie	Netty Cookie
	 * @return	A WOCookie
	 */
	public static WOCookie asWOCookie(Cookie cookie) {
		WOCookie wocookie = new WOCookie(
				cookie.getName(),
				cookie.getValue(),
				cookie.getPath(),
				cookie.getDomain(),
				cookie.getMaxAge(),
				cookie.isSecure(),
				cookie.isHttpOnly());
		return wocookie;
	}
	
	public static WOSession existingSession(WORequest request) {
		WOApplication app = WOApplication.application();
		String sessionID = request.stringFormValueForKey(app.sessionIdKey());
		WOSession session = null;
		if(sessionID != null) {
			WOContext context = app.createContextForRequest(request);
			session = app.restoreSessionWithID(sessionID, context);
		}
		return session;
	}
	
	public static String websocketUrlInContext(WOContext context) {
		String serverName = context.request()._serverName();
		String serverPort = context.request()._serverPort();
		String sessionID = context.session().sessionID();
		//TODO secure websocket support
		//FIXME this only works for a single application
		return "ws://" + serverName + ":" + serverPort + "?" + WOApplication.application().sessionIdKey() + "=" + sessionID;
	}
	
	/**
	 * Converts a WOResponse to a Netty HttpResponse
	 * 
	 * @param woresponse	A WOResponse
	 * @return	HttpResponse
	 */
	public static HttpResponse asHttpResponse(WOResponse woresponse) {
		return new WOResponseWrapper(woresponse);
	}
	
	public static String getWebSocketLocation(HttpRequest req) {
		//TODO secure websocket support
		String result = "ws://" + req.getHeader(Names.HOST) + req.getUri();
		return result;
	}
}
