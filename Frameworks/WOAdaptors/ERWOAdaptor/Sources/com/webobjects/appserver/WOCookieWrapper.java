package com.webobjects.appserver;

import java.util.Set;

import org.jboss.netty.handler.codec.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for WOCookie to pass to Netty HttpResponseEncoder
 * 
 * @author ravim
 */
public class WOCookieWrapper implements Cookie {	
    private static final Logger log = LoggerFactory.getLogger(WOCookieWrapper.class);

	private WOCookie wrapping;
	
	public WOCookieWrapper(WOCookie cookie) {
		super();
		wrapping = cookie;
	}

	public String getComment() {
		return (String) wrapping.valueForKey("comment");
	}

	public String getCommentUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDomain() {
		return wrapping.domain();
	}

	public int getMaxAge() {
		return wrapping.timeOut();
	}

	public String getName() {
		return wrapping.name();
	}

	public String getPath() {
		return wrapping.path();
	}

	public Set<Integer> getPorts() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getValue() {
		return wrapping.value();
	}

	public int getVersion() {
		return 1;
	}

	public boolean isDiscard() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isHttpOnly() {
		return wrapping.isHttpOnly();
	}

	public void setComment(String comment) {
		wrapping.takeValueForKey(comment, "comment");
	}

	public void setCommentUrl(String commentUrl) {
		// TODO Auto-generated method stub
		
	}

	public void setDiscard(boolean discard) {
		if (discard) wrapping.setTimeOut(0);
	}

	public void setHttpOnly(boolean httpOnly) {
		wrapping.setIsHttpOnly(httpOnly);
	}

	public void setMaxAge(int maxAge) {
		wrapping.setTimeOut(maxAge);
	}

	public void setPorts(int... ports) {
		// TODO Auto-generated method stub
		
	}

	public void setPorts(Iterable<Integer> ports) {
		// TODO Auto-generated method stub
		
	}

	public void setSecure(boolean secure) {
		wrapping.setIsSecure(secure);
	}

	public void setVersion(int version) {
		log.warn("Illegally trying to set cookie version: {}", version);
	}

	public int compareTo(Cookie o) {
		return -1 * o.compareTo(this);
	}

	public boolean isSecure() {
		return wrapping.isSecure();
	}

	public void setDomain(String domain) {
		wrapping.setDomain(domain);
	}

	public void setPath(String path) {
		wrapping.setPath(path);
	}

	public void setValue(String value) {
		wrapping.setValue(value);
	}
}
