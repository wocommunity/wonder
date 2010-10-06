package com.webobjects.appserver;

import static org.jboss.netty.buffer.ChannelBuffers.buffer;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSSet;

/**
 * Wrapper for WOResponse to pass to Netty HttpResponseEncoder
 * 
 * @author ravim
 */
public class WOResponseWrapper implements HttpResponse {
    private static final Logger log = Logger.getLogger(WOResponseWrapper.class);

	private WOResponse wrapping;
	private ChannelBuffer _content = ChannelBuffers.EMPTY_BUFFER;
	
	public WOResponseWrapper(WOResponse response) {
		super();
		wrapping = response;
		
		// set content string
		if (wrapping._contentLength() > 0) {
			if (wrapping._content.length() > 0) {
				Charset charset = Charset.forName(wrapping.contentEncoding());
				_content = ChannelBuffers.copiedBuffer(wrapping._content.toString(), charset);
				wrapping._content = new StringBuilder(0);
			} else {
				int _length = wrapping._contentData.length();
				this.setHeader(CONTENT_LENGTH, _length);
				_content = ChannelBuffers.copiedBuffer(wrapping._contentData._bytesNoCopy());
				wrapping._contentData = new NSMutableData(0);
			}
		} else if (wrapping.contentInputStream() != null) {
			try {
				_content = buffer(wrapping.contentInputStreamBufferSize());
				wrapping.contentInputStream().read(_content.array());
				wrapping.setContentStream(null, 0, 0);
			} catch (IOException exception) {
				log.error(exception.getCause().getMessage());
			}
		}
	}

	@Override
	public HttpResponseStatus getStatus() {
		return HttpResponseStatus.valueOf(wrapping.status());
	}

	@Override
	public void setStatus(HttpResponseStatus status) {
		wrapping.setStatus(status.getCode());
	}

	@Override
	public void addHeader(String name, Object value) {
		if (value != null) {
			if (value instanceof String) {
				wrapping.appendHeader((String) value, name);
			}
		}
	}

	@Override
	public void clearHeaders() {
		wrapping._httpHeaders = null;
	}

	@Override
	public boolean containsHeader(String name) {
		return wrapping.hasHeaderForKey(name);
	}

	@Override
	public ChannelBuffer getContent() {
		return _content;
	}

	@Deprecated
	@Override
	public long getContentLength() {
		return HttpHeaders.getContentLength(this);
	}

	@Deprecated
	@Override
	public long getContentLength(long defaultValue) {
		return HttpHeaders.getContentLength(this, defaultValue);
	}

	@Override
	public String getHeader(String name) {
		if (name.equals(COOKIE)) {
			// Encode the cookie.
			NSArray<WOCookie> wocookies = wrapping.cookies();
			if(!wocookies.isEmpty()) {
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				for (WOCookie wocookie : wocookies) {
					Cookie cookie = new WOCookieWrapper(wocookie);
					cookieEncoder.addCookie(cookie);
				} return cookieEncoder.encode();
			} else return null;
		} else return wrapping.headerForKey(name);
	}

	@Override
	public Set<String> getHeaderNames() {
		return new NSSet<String>(wrapping.headerKeys());
	}
    
	@Override
	public List<Map.Entry<String, String>> getHeaders() {
		List<Map.Entry<String, String>> headers = new LinkedList<Map.Entry<String, String>>();
		
		for(String headerKey: wrapping.headerKeys()) {
			String value = wrapping.headerForKey(headerKey);
			if (value != null) {
				headers.add(new SimpleEntry<String, String>(headerKey, value));
			}
		}
		
		return headers;
	}

	@Override
	public List<String> getHeaders(String name) {
		return wrapping.headersForKey(name);
	}

	@Override
	public HttpVersion getProtocolVersion() {
		return HttpVersion.valueOf(wrapping.httpVersion());
	}

	@Override
	public boolean isChunked() {
		return false;
	}

	@Override
	public boolean isKeepAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeHeader(String name) {
		wrapping.removeHeadersForKey(name);
	}

	@Override
	public void setChunked(boolean chunked) {
		// TODO Checkme - irrelevant for http response
		log.error("Trying to set http response content");
	}

	@Override
	public void setContent(ChannelBuffer content) {
		if (content != null) {
			_content = content;
		} else _content = ChannelBuffers.EMPTY_BUFFER;
	}

	@Override
	public void setHeader(String name, Object value) {
		if (value != null) {
			wrapping.setHeader(value.toString(), name);
		} else wrapping.setHeader(null, name);
	}

	@Override
	public void setHeader(String name, Iterable<?> values) {
		if (values != null) {
			NSArray<String> value = new NSArray(values);
			wrapping.setHeaders(value, name);
		} else wrapping.setHeader(null, name);
	}

	@Override
	public void setProtocolVersion(HttpVersion version) {
		wrapping.setHTTPVersion(version.getText());
	}
}
