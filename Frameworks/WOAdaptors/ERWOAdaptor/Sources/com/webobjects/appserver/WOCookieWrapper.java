package com.webobjects.appserver;

import java.util.Set;

import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.Cookie;

/**
 * Wrapper for WOCookie to pass to Netty HttpResponseEncoder
 * 
 * @author ravim
 */
public class WOCookieWrapper implements Cookie {	
    private static final Logger log = Logger.getLogger(WOCookieWrapper.class);

	private WOCookie wrapping;
	
	public WOCookieWrapper(WOCookie cookie) {
		super();
		wrapping = cookie;
	}

	@Override
	public String getComment() {
		return (String) wrapping.valueForKey("comment");
	}

	@Override
	public String getCommentUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDomain() {
		return wrapping.domain();
	}

	@Override
	public int getMaxAge() {
		return wrapping.timeOut();
	}

	@Override
	public String getName() {
		return wrapping.name();
	}

	@Override
	public String getPath() {
		return wrapping.path();
	}

	@Override
	public Set<Integer> getPorts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValue() {
		return wrapping.value();
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public boolean isDiscard() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHttpOnly() {
		return wrapping.isHttpOnly();
	}

	@Override
	public void setComment(String comment) {
		wrapping.takeValueForKey(comment, "comment");
	}

	@Override
	public void setCommentUrl(String commentUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDiscard(boolean discard) {
		if (discard) wrapping.setTimeOut(0);
	}

	@Override
	public void setHttpOnly(boolean httpOnly) {
		wrapping.setIsHttpOnly(httpOnly);
	}

	@Override
	public void setMaxAge(int maxAge) {
		wrapping.setTimeOut(maxAge);
	}

	@Override
	public void setPorts(int... ports) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPorts(Iterable<Integer> ports) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSecure(boolean secure) {
		wrapping.setIsSecure(secure);
	}

	@Override
	public void setVersion(int version) {
		log.warn("Illegally trying to set cookie version: " + version);
	}

	@Override
	public int compareTo(Cookie o) {
		return -1 * o.compareTo(this);
	}

	@Override
	public boolean isSecure() {
		return wrapping.isSecure();
	}

	@Override
	public void setDomain(String domain) {
		wrapping.setDomain(domain);
	}

	@Override
	public void setPath(String path) {
		wrapping.setPath(path);
	}

	@Override
	public void setValue(String value) {
		wrapping.setValue(value);
	}
}
