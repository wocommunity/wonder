package com.webobjects.appserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;

public class WOCookie implements NSKeyValueCoding, com.webobjects.foundation.NSKeyValueCoding.ErrorHandling,
		NSKeyValueCodingAdditions, Serializable {

	static final long serialVersionUID = 310727495L;
	String _name;
	String _value;
	String _domain;
	String _path;
	boolean _isSecure;
	NSTimestamp _expires;
	int _timeout;
	boolean _isHttpOnly;
	@Deprecated
	static final SimpleDateFormat TheDateFormat;

	static {
		TheDateFormat = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss 'GMT'", new DateFormatSymbols(Locale.US));
		TheDateFormat.setTimeZone(NSTimeZone.timeZoneWithName("GMT", true));
	}
	
	/**
	 * Formatter to use when handling timestamp columns. Each thread has its own
	 * copy.
	 */
	private static final ThreadLocal<SimpleDateFormat> TIMESTAMP_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss 'GMT'", new DateFormatSymbols(Locale.US));
			formatter.setTimeZone(NSTimeZone.timeZoneWithName("GMT", true));
			return formatter;
		}
	};

	@Deprecated
	public static WOCookie cookieWithName(final String name, final String value, final String path,
			final String domain, final NSTimestamp expires, final boolean isSecure) {
		return new WOCookie(name, value, path, domain, expires, isSecure);
	}

	@Deprecated
	public static WOCookie cookieWithName(final String name, final String value, final String path,
			final String domain, final int timeout, final boolean isSecure) {
		return new WOCookie(name, value, path, domain, timeout, isSecure);
	}

	@Deprecated
	public static WOCookie cookieWithName(final String name, final String value) {
		return new WOCookie(name, value);
	}

	public WOCookie(final String name, final String value, final String path, final String domain,
			final NSTimestamp expires, final boolean isSecure) {
		this(name, value, path, domain, expires, isSecure, false);
	}

	public WOCookie(final String name, final String value, final String path, final String domain,
			final NSTimestamp expires, final boolean isSecure, final boolean httpOnly) {
		if (name == null) {
			throw new IllegalArgumentException("Cookie may not have null name.");
		}
		_name = name;
		_value = value;
		_path = path;
		_domain = domain;
		_expires = expires;
		_isSecure = isSecure;
		_isHttpOnly = httpOnly;
		setTimeOut(-1);
		return;
	}

	public WOCookie(final String name, final String value, final String path, final String domain, final int timeout,
			final boolean isSecure) {
		this(name, value, path, domain, timeout, isSecure, false);
	}

	public WOCookie(final String name, final String value, final String path, final String domain, final int timeout,
			final boolean isSecure, final boolean httpOnly) {
		if (name == null) {
			throw new IllegalArgumentException("Cookie may not have null name.");
		}
		_name = name;
		_value = value;
		_path = path;
		_domain = domain;
		setTimeOut(timeout);
		_isSecure = isSecure;
		_isHttpOnly = httpOnly;
		return;
	}

	public WOCookie(final String name, final String value) {
		this(name, value, null, null, -1, false);
	}

	@Override
	public String toString() {
		String expiresString = _expires != null ? new StringBuilder().append(" expires=")
				.append(TIMESTAMP_FORMATTER.get().format(_expires)).toString() : "";
		String expires = _timeout < 0 ? "" : new StringBuilder().append(" max-age=").append(_timeout).toString();

		return new StringBuilder().append('<').append(getClass().getName()).append(" name=").append(_name)
				.append(" value=").append(_value).append(" path=").append(_path).append(" domain=").append(_domain)
				.append(expiresString).append(expires).append(" isSecure=").append(_isSecure)
				.append(" isHttpOnly=").append(_isHttpOnly).append('>').toString();
	}

	public String headerString() {
		return _headerString(false);
	}

	String _headerString(final boolean isRequest) {
		StringBuilder header = new StringBuilder(140);
		header.append(_name);
		header.append('=');
		if (_value != null && _value.indexOf(' ') != -1 && (!_value.startsWith("\"") || !_value.endsWith("\""))) {
			header.append("\"");
			header.append(_value);
			header.append("\"");
		} else if (_value == null) {
			header.append(' ');
		} else {
			header.append(_value);
		}
		if (!isRequest) {
			NSTimestamp localExpires = _expires;

			header.append("; version=\"1\"");
			if (_timeout >= 0) {
				header.append("; max-age=");
				header.append(_timeout);
				if (_timeout == 0) {
					localExpires = new NSTimestamp(0L);
				} else {
					localExpires = new NSTimestamp(System.currentTimeMillis() + (_timeout * 1000));
				}
			}
			if (_expires != null) {
				header.append("; expires=");
				header.append(TIMESTAMP_FORMATTER.get().format(localExpires));
			}
			if (_path != null) {
				header.append("; path=");
				header.append(_path);
			}
			if (_domain != null) {
				header.append("; domain=");
				header.append(_domain);
			}
			if (_isSecure) {
				header.append("; secure");
			}
			if (_isHttpOnly) {
				header.append("; HttpOnly");
			}
		}
		return header.toString();
	}

	public String name() {
		return _name;
	}

	public void setName(final String name) {
		_name = name;
	}

	public String value() {
		return _value;
	}

	public void setValue(final String value) {
		_value = value;
	}

	public String domain() {
		return _domain;
	}

	public void setDomain(final String domain) {
		_domain = domain;
	}

	public String path() {
		return _path;
	}

	public void setPath(final String path) {
		_path = path;
	}

	public NSTimestamp expires() {
		return _expires;
	}

	public void setExpires(final NSTimestamp expires) {
		_expires = expires;
	}

	public void setTimeOut(final int timeout) {
		_timeout = timeout;
	}

	public int timeOut() {
		return _timeout;
	}

	public boolean isSecure() {
		return _isSecure;
	}

	public void setIsSecure(final boolean isSecure) {
		_isSecure = isSecure;
	}

	public boolean isHttpOnly() {
		return _isHttpOnly;
	}

	public void setIsHttpOnly(final boolean isHttpOnly) {
		_isHttpOnly = isHttpOnly;
	}

	public static boolean canAccessFieldsDirectly() {
		return true;
	}

	public Object valueForKey(final String key) {
		return com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
	}

	public void takeValueForKey(final Object value, final String key) {
		com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
	}

	public Object handleQueryWithUnboundKey(final String key) {
		return com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.handleQueryWithUnboundKey(this, key);
	}

	public void handleTakeValueForUnboundKey(final Object value, final String key) {
		com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.handleTakeValueForUnboundKey(this, value, key);
	}

	public void unableToSetNullForKey(final String key) {
		com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.unableToSetNullForKey(this, key);
	}

	public Object valueForKeyPath(final String key) {
		return com.webobjects.foundation.NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, key);
	}

	public void takeValueForKeyPath(final Object value, final String key) {
		com.webobjects.foundation.NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, key);
	}

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.writeInt(_timeout);
		out.writeUTF(_name);
		out.writeUTF(_value);
		out.writeUTF(_domain);
		out.writeUTF(_path);
		out.writeBoolean(_isSecure);
		out.writeObject(_expires);
		out.writeBoolean(_isHttpOnly);
	}

	private void readObject(final ObjectInputStream out) throws IOException, ClassNotFoundException {
		_timeout = out.readInt();
		_name = out.readUTF();
		_value = out.readUTF();
		_domain = out.readUTF();
		_path = out.readUTF();
		_isSecure = out.readBoolean();
		_expires = (NSTimestamp) out.readObject();
		_isHttpOnly = out.readBoolean();
	}
}
