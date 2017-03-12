package com.webobjects.appserver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSSet;

/**
 * Wrapper for WOResponse to pass to Netty HttpResponseEncoder
 * 
 * @author ravim
 */
public class WOResponseWrapper implements HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(WOResponseWrapper.class);

	private WOResponse wrapping;
	private ChannelBuffer _content;
	
	/**
	 * Converts a WOCookie to a Netty cookie
	 * 
	 * @param wocookie
	 * @return A Netty cookie
	 */
	private static Cookie asCookie(WOCookie wocookie) {
		return new WOCookieWrapper(wocookie);
	}
	
	public WOResponseWrapper(WOResponse response) {
		super();
		wrapping = response;
	}

	public HttpResponseStatus getStatus() {
		return HttpResponseStatus.valueOf(wrapping.status());
	}

	public void setStatus(HttpResponseStatus status) {
		wrapping.setStatus(status.getCode());
	}

	public void addHeader(String name, Object value) {
		if (value != null) {
			if (value instanceof String) {
				wrapping.appendHeader((String) value, name);
			}
		}
	}

	public void clearHeaders() {
		wrapping._httpHeaders = null;
	}

	public boolean containsHeader(String name) {
		return wrapping.hasHeaderForKey(name);
	}

	public ChannelBuffer getContent() {
		if(_content == null) {
			// set content string
			if (wrapping._contentLength() > 0) {
				if (wrapping._content.length() > 0) {
					Charset charset = Charset.forName(wrapping.contentEncoding());
					_content = ChannelBuffers.copiedBuffer(wrapping._content.toString(), charset);
					wrapping._content = null;
				} else {
					int _length = wrapping._contentData.length();
					this.setHeader(Names.CONTENT_LENGTH, _length);
					_content = ChannelBuffers.copiedBuffer(wrapping._contentData._bytesNoCopy());
					wrapping._contentData = null;
				}
			} else if (wrapping.contentInputStream() != null) {
				try {
					int length = (int)wrapping.contentInputStreamLength();
					_content = ChannelBuffers.buffer(length);
					InputStream stream = null;
					try {
						stream = wrapping.contentInputStream();
						_content.writeBytes(stream, length);
						wrapping.setContentStream(null, 0, 0l);
					} finally {
						try { if(stream != null) { stream.close();} } catch(IOException e) { log.error("Could not close stream.", e); }
					}
				} catch (IOException exception) {
					throw NSForwardException._runtimeExceptionForThrowable(exception);
				}
			} else {
				_content = ChannelBuffers.EMPTY_BUFFER;
			}
		}
		return _content;
	}

	@Deprecated
	public long getContentLength() {
		return HttpHeaders.getContentLength(this);
	}

	@Deprecated
	public long getContentLength(long defaultValue) {
		return HttpHeaders.getContentLength(this, defaultValue);
	}

	public String getHeader(String name) {
		if (name.equals(Names.COOKIE)) {
			// Encode the cookie.
			NSArray<WOCookie> wocookies = wrapping.cookies();
			if(!wocookies.isEmpty()) {
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				for (WOCookie wocookie : wocookies) {
					Cookie cookie = asCookie(wocookie);
					cookieEncoder.addCookie(cookie);
				} return cookieEncoder.encode();
			} else return null;
		} else return wrapping.headerForKey(name);
	}

	public Set<String> getHeaderNames() {
		return new NSSet<>(wrapping.headerKeys());
	}
    
	public List<Map.Entry<String, String>> getHeaders() {
		List<Map.Entry<String, String>> headers = new LinkedList<Map.Entry<String, String>>();
		
		for(String headerKey: wrapping.headerKeys()) {
			String value = wrapping.headerForKey(headerKey);
			if (value != null) {
				headers.add(new SimpleEntry<>(headerKey, value));
			}
		}
		
		return headers;
	}

	public List<String> getHeaders(String name) {
		return wrapping.headersForKey(name);
	}

	public HttpVersion getProtocolVersion() {
		return HttpVersion.valueOf(wrapping.httpVersion());
	}

	public boolean isChunked() {
		return false;
	}

	public boolean isKeepAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeHeader(String name) {
		wrapping.removeHeadersForKey(name);
	}

	public void setChunked(boolean chunked) {
		// TODO Checkme - irrelevant for http response
		log.error("Trying to set http response content");
	}

	public void setContent(ChannelBuffer content) {
		if (content != null) {
			_content = content;
		} else _content = ChannelBuffers.EMPTY_BUFFER;
	}

	public void setHeader(String name, Object value) {
		if (value != null) {
			wrapping.setHeader(value.toString(), name);
		} else wrapping.setHeader(null, name);
	}

	public void setHeader(String name, Iterable<?> values) {
		if (values != null) {
			NSArray<String> value = new NSArray(values);
			wrapping.setHeaders(value, name);
		} else wrapping.setHeader(null, name);
	}

	public void setProtocolVersion(HttpVersion version) {
		wrapping.setHTTPVersion(version.getText());
	}
}
