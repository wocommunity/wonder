package com.webobjects.appserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.util.Locale;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;


public class WOCookie
        implements
        NSKeyValueCoding,
        com.webobjects.foundation.NSKeyValueCoding.ErrorHandling,
        NSKeyValueCodingAdditions,
        Serializable {

    static final long                 serialVersionUID = 310727495L;
    String                            _name;
    String                            _value;
    String                            _domain;
    String                            _path;
    boolean                           _isSecure;
    NSTimestamp                       _expires;
    int                               _timeout;
    boolean                           _isHttpOnly;
    static final NSTimestampFormatter TheDateFormat;

    static {
        DateFormatSymbols dateformatsymbols = new DateFormatSymbols(Locale.US);
        dateformatsymbols.setZoneStrings(NSTimestampFormatter._getDefaultZoneStrings());
        TheDateFormat = new NSTimestampFormatter("%a, %d-%b-%Y %H:%M:%S GMT", dateformatsymbols);
        TheDateFormat.setDefaultFormatTimeZone(NSTimeZone.timeZoneWithName("GMT", true));
    }

    /**
     * @deprecated Method cookieWithName is deprecated
     */

    @Deprecated
    public static WOCookie cookieWithName(final String name, final String value, final String path, final String domain, final NSTimestamp expires, final boolean isSecure) {
        return new WOCookie(name, value, path, domain, expires, isSecure);
    }

    /**
     * @deprecated Method cookieWithName is deprecated
     */

    @Deprecated
    public static WOCookie cookieWithName(final String name, final String value, final String path, final String domain, final int timeout, final boolean isSecure) {
        return new WOCookie(name, value, path, domain, timeout, isSecure);
    }

    /**
     * @deprecated Method cookieWithName is deprecated
     */

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
        } else {
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
    }

    public WOCookie(final String name, final String value, final String path, final String domain, final int timeout,
            final boolean isSecure) {
        this(name, value, path, domain, timeout, isSecure, false);
    }

    public WOCookie(final String name, final String value, final String path, final String domain, final int timeout,
            final boolean isSecure, final boolean httpOnly) {
        if (name == null) {
            throw new IllegalArgumentException("Cookie may not have null name.");
        } else {
            _name = name;
            _value = value;
            _path = path;
            _domain = domain;
            setTimeOut(timeout);
            _isSecure = isSecure;
            _isHttpOnly = httpOnly;
            return;
        }
    }

    public WOCookie(final String name, final String value) {
        this(name, value, null, null, -1, false);
    }

    @Override
    public String toString() {
        String s = _expires != null ? " expires=" + TheDateFormat.format(_expires) : "";
        String s1 = _timeout < 0 ? "" : " max-age=" + _timeout;
        return "<" + getClass().getName() + " name=" + _name + " value=" + _value + " path=" + _path + " domain="
                + _domain + s + s1 + " isSecure=" + (_isSecure ? "true" : "false") + " isHttpOnly="
                + (_isHttpOnly ? "true" : "false") + ">";
    }

    public String headerString() {
        return _headerString(false);
    }

    String _headerString(final boolean flag) {
        StringBuffer header = new StringBuffer(140);
        header.append(_name);
        header.append('=');
        if (_value != null && _value.indexOf(' ') != -1 && (!_value.startsWith("\"") || !_value.endsWith("\""))) {
            header.append("\"");
            header.append(_value);
            header.append("\"");
        } else
            if (_value == null) {
                header.append(" ");
            } else {
                header.append(_value);
            }
        if (!flag) {
            header.append("; version=\"1\"");
            if (_timeout >= 0) {
                header.append("; max-age=");
                header.append(_timeout);
            }
            if (_expires != null) {
                header.append("; expires=");
                header.append(TheDateFormat.format(_expires));
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
        return new String(header);
    }

    public String name() {
        return _name;
    }

    public void setName(final String s) {
        _name = s;
    }

    public String value() {
        return _value;
    }

    public void setValue(final String s) {
        _value = s;
    }

    public String domain() {
        return _domain;
    }

    public void setDomain(final String s) {
        _domain = s;
    }

    public String path() {
        return _path;
    }

    public void setPath(final String s) {
        _path = s;
    }

    public NSTimestamp expires() {
        return _expires;
    }

    public void setExpires(final NSTimestamp nstimestamp) {
        _expires = nstimestamp;
    }

    public void setTimeOut(final int i) {
        _timeout = i;
    }

    public int timeOut() {
        return _timeout;
    }

    public boolean isSecure() {
        return _isSecure;
    }

    public void setIsSecure(final boolean flag) {
        _isSecure = flag;
    }

    public boolean isHttpOnly() {
        return _isHttpOnly;
    }

    public void setIsHttpOnly(final boolean flag) {
        _isHttpOnly = flag;
    }

    public static boolean canAccessFieldsDirectly() {
        return true;
    }

    public Object valueForKey(final String s) {
        return com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.valueForKey(this, s);
    }

    public void takeValueForKey(final Object obj, final String s) {
        com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, obj, s);
    }

    public Object handleQueryWithUnboundKey(final String s) {
        return com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.handleQueryWithUnboundKey(this, s);
    }

    public void handleTakeValueForUnboundKey(final Object obj, final String s) {
        com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.handleTakeValueForUnboundKey(this, obj, s);
    }

    public void unableToSetNullForKey(final String s) {
        com.webobjects.foundation.NSKeyValueCoding.DefaultImplementation.unableToSetNullForKey(this, s);
    }

    public Object valueForKeyPath(final String s) {
        return com.webobjects.foundation.NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, s);
    }

    public void takeValueForKeyPath(final Object obj, final String s) {
        com.webobjects.foundation.NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, obj, s);
    }

    public void writeObject(final ObjectOutputStream objectoutputstream) throws IOException {
        objectoutputstream.writeInt(_timeout);
        objectoutputstream.writeUTF(_name);
        objectoutputstream.writeUTF(_value);
        objectoutputstream.writeUTF(_domain);
        objectoutputstream.writeUTF(_path);
        objectoutputstream.writeBoolean(_isSecure);
        objectoutputstream.writeObject(_expires);
        objectoutputstream.writeBoolean(_isHttpOnly);
    }

    public void readObject(final ObjectInputStream objectinputstream) throws IOException, ClassNotFoundException {
        _timeout = objectinputstream.readInt();
        _name = objectinputstream.readUTF();
        _value = objectinputstream.readUTF();
        _domain = objectinputstream.readUTF();
        _path = objectinputstream.readUTF();
        _isSecure = objectinputstream.readBoolean();
        _expires = (NSTimestamp) objectinputstream.readObject();
        _isHttpOnly = objectinputstream.readBoolean();
    }

}
