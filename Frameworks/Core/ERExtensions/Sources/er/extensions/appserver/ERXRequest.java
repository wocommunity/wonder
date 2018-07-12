package er.extensions.appserver;

import java.net.HttpCookie;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WOProperties;
import com.webobjects.appserver._private.WOShared;
import com.webobjects.appserver._private.WOURLFormatException;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.foundation.ERXProperties;
import er.extensions.localization.ERXLocalizer;

/**
 * Subclass of WORequest that fixes several Bugs.
 * The ID's are #2924761 and #2961017. It can also be extended to handle
 * #2957558 ("de-at" is converted to "German" instead of "German_Austria").
 * The request is created via {@link ERXApplication#createRequest(String, String, String, Map, NSData, Map)}.
 */
public  class ERXRequest extends WORequest {
    private static final Logger log = LoggerFactory.getLogger(ERXRequest.class);

    public static final String UNKNOWN_HOST = "UNKNOWN";

    public static final String X_FORWARDED_PROTO_FOR_SSL = ERXProperties.stringForKeyWithDefault("er.extensions.appserver.ERXRequest.xForwardedProtoForSsl", "https");
    public static final String X_FORWARDED_PROTO_HEADER_KEY_FOR_SSL = ERXProperties.stringForKeyWithDefault("er.extensions.appserver.ERXRequest.xForwardedProtoHeaderKeyForSsl", "x-forwarded-proto");

    protected static Boolean isBrowserFormValueEncodingOverrideEnabled;

    protected static final NSArray<String> HOST_ADDRESS_KEYS = new NSArray<>(new String[]{"x-forwarded-for", "pc-remote-addr", "remote_host", "remote_addr", "remote_user", "x-webobjects-remote-addr"});

    // 'Host' is the official HTTP 1.1 header for the host name in the request URL, so this should be checked first.
    // @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.23
    // when the app is behind a reverse proxy 'Host' will contain the proxy address instead of the requested one so check first for 'x-forwarded-host'
    // @see http://httpd.apache.org/docs/2.2/mod/mod_proxy.html#x-headers
    // Fallback headers such as server_name will screw up your complete URL generation for secure domains that have wildcard subdomains since it returns sth like *.domain.com for host name
    protected static final NSArray<String> HOST_NAME_KEYS = new NSArray<>(new String[]{"x-forwarded-host", "Host", "x-webobjects-server-name", "server_name", "http_host"});
    
    /** NSArray to keep browserLanguages in. */
    protected NSArray<String> _browserLanguages;

    /** holds a reference to the browser object */
    protected ERXBrowser _browser;

    /**
     * Specifies whether https should be overridden to be enabled or disabled app-wide. This is 
     * useful if you are developing with DirectConnect and you want to be able to specify secure 
     * forms and links, but you want to be able to continue testing them without setting up SSL.
     * 
     * Defaults to <code>false</code>, set er.extensions.ERXRequest.secureDisabled=true to turn it off.
     */
    protected boolean _secureDisabled;
    
    /**
     * Holds the cookies in a NSDictionary.
     */
    protected NSDictionary<String, NSArray<String>> _cookieDictionary;
    
    /**
     * Returns a ERXRequest object initialized with the specified parameters.
     * 
     * @param aMethod a "GET", "POST" or "HEAD", may not be <code>null</code>. If <code>null</code>, or not one of the allowed methods, an IllegalArgumentException will be thrown
     * @param aURL a URL, may not be null or an IllegalArgumentException will be thrown
     * @param anHTTPVersion  the version of HTTP used when sending the message, may not be <code>null</code> or an IllegalArgumentException will be thrown
     * @param someHeaders  a dictionary whose String keys correspond to header names and whose values are arrays of one or more strings corresponding to the values of each header
     * @param aContent the HTML content
     * @param aUserInfoDictionary java.util.Map that contains any information that the WORequest object wants to pass along to other objects
     */
    public ERXRequest(String aMethod, String aURL, String anHTTPVersion,
                      Map someHeaders, NSData aContent, Map aUserInfoDictionary) {
        super(aMethod, aURL, anHTTPVersion, someHeaders, aContent, aUserInfoDictionary);
        if (isBrowserFormValueEncodingOverrideEnabled() && browser().formValueEncoding() != null) {
            setDefaultFormValueEncoding(browser().formValueEncoding());
        }
        _secureDisabled = ERXRequest._isSecureDisabled();
    }
    
    /**
     * This method is used by WOContext when generating full URLs for form actions in secure mode, etc.
     *
     * Overriding this because WORequest checks 'server_name' before 'Host' by default and it does not cut it for generating full secure
     * urls in the case of a hostname that uses a wildcard SSL certificate allowing infinite secure subdomains.
     *
     * For example, if we have a wildcard ssl cert for https://*.mydomain.com (where * = wildcard subdomain), and we use
     * subdomains to implement CSS skinning for different customers that are all using the
     * same WO app while using subdomains to get their "custom" site, with host names such as
     * acmesandwiches.mydomain.com, apple.mydomain.com, kfc.mydomain.com, etc., and we configure apache with one virtual host config for
     * *.mydomain.com, then the stupid 'server_name' header will return *.mydomain.com INSTEAD OF the host name
     * used in the request URL, and thus all https urls in forms, links etc will be broken.
     *
     * @return the server name, which happens to be used by WOContext for generating full URLs.
     * @see com.webobjects.appserver.WORequest#_serverName()
     * @see WOContext#completeURLWithRequestHandlerKey(String, String, String, String, boolean, int)
     * @see WORequest#_completeURLPrefix(StringBuffer, boolean, int)
     */
	@Override
	public String _serverName() {
		String serverName = headerForKey("x-webobjects-servlet-server-name");

		if ((serverName == null) || (serverName.length() == 0)) {
			if (isUsingWebServer()) {
				// Check our host name keys in our preferred order instead of Apple WO 5.4.3 default header check logic.
				serverName = remoteHostName();

				if ((serverName == null) || (serverName.length() == 0) || serverName.equals(UNKNOWN_HOST))
					throw new NSForwardException(new WOURLFormatException("<" + super.getClass().getName() + ">: Unable to build complete url as no server name was provided in the headers of the request."));
			}
			else {
				serverName = ERXApplication.erxApplication().publicHost();
			}
		}
		return serverName;
	}

    /**
     * Returns <code>true</code> if er.extensions.ERXRequest.secureDisabled is true.
     * Defaults to <code>false</code>.
     * 
     * @return <code>true</code> if er.extensions.ERXRequest.secureDisabled is true
     */
    public static boolean _isSecureDisabled() {
        return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXRequest.secureDisabled", false);
    }
    
    /**
     * Returns <code>true</code> if er.extensions.ERXRequest.secureDisabled is true.
     * 
     * @return <code>true</code> if er.extensions.ERXRequest.secureDisabled is true
     */
    public boolean isSecureDisabled() {
    	return _secureDisabled;
    }
    
    public boolean isBrowserFormValueEncodingOverrideEnabled() {
        if (isBrowserFormValueEncodingOverrideEnabled == null) {
            isBrowserFormValueEncodingOverrideEnabled = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXRequest.BrowserFormValueEncodingOverrideEnabled", false) ? Boolean.TRUE : Boolean.FALSE;
        }
        return isBrowserFormValueEncodingOverrideEnabled.booleanValue();
    }

    @Override
    public WOContext context() {
    	return _context();
    }
    
    /** Returns a cooked version of the languages the user has set in his Browser.
     * Adds "Nonlocalized" and {@link er.extensions.localization.ERXLocalizer#defaultLanguage()} if not
     * already present. Transforms regionalized en_us to English_US as a key.
     * @return cooked version of user's languages
     */
	@Override
    @SuppressWarnings("unchecked")
	public NSArray<String> browserLanguages() {
        if (_browserLanguages == null) {
        	NSMutableArray<String> languageKeys = new NSMutableArray<>();
            NSArray<String> fixedLanguages = null;
            String string = headerForKey("accept-language");
            if (string != null) {
                NSArray<String> rawLanguages = NSArray.componentsSeparatedByString(string, ",");
                fixedLanguages = fixAbbreviationArray(rawLanguages);
                for (Enumeration<String> e = fixedLanguages.objectEnumerator(); e.hasMoreElements();) {
					String languageKey = e.nextElement();
					String language = WOProperties.TheLanguageDictionary.objectForKey(languageKey);
					if(language == null) {
						int index = languageKey.indexOf('_');
						if(index > 0) {
							String mainLanguageKey = languageKey.substring(0, index);
							String region = languageKey.substring(index);
							language = WOProperties.TheLanguageDictionary.objectForKey(mainLanguageKey);
							if(language != null) {
								language = language + region.toUpperCase();
							}
						}
					}
					if(language != null) {
						languageKeys.addObject(language);
					}
				}
            }
            languageKeys.addObject("Nonlocalized");
            if(!languageKeys.containsObject(ERXLocalizer.defaultLanguage())) {
                languageKeys.addObject(ERXLocalizer.defaultLanguage());
            }
            _browserLanguages = languageKeys.immutableClone();
        }
        return _browserLanguages;
    }
    
    @Override
	public String stringFormValueForKey(String key) {
    	String result = super.stringFormValueForKey(key);
    	if (result == null && "wodata".equals(key)) {
    	    // AK: yet another crappy 5.4 fix, WODynamicURL changed packages
    		String requestHandlerKey = (String)valueForKeyPath("_uriDecomposed.requestHandlerKey");
    		if (WOApplication.application().resourceRequestHandlerKey().equals(requestHandlerKey)) {
    			String requestHandlerPath = (String)valueForKeyPath("_uriDecomposed.requestHandlerPath");
    			if(requestHandlerPath != null) {
    				requestHandlerPath = "file:/" +  requestHandlerPath.substring("wodata=/".length());
    				result = requestHandlerPath.replace('+', ' ');
    			}
    		}
    	}

		return result;
	}

    @Override
    public NSTimestamp dateFormValueForKey(String aKey, SimpleDateFormat dateFormatter) {

        String aDateString = stringFormValueForKey(aKey);
        java.util.Date aDate = null;
        if (aDateString != null && dateFormatter != null) {
            try {
                aDate = dateFormatter.parse(aDateString);
            } catch (java.text.ParseException e) {
               log.error("Could not parse date '{}'.", aDateString, e);
            }
        }
        return aDate == null ? null : new NSTimestamp(aDate);
    }


	/**
     * Gets the ERXBrowser associated with the user-agent of
     * the request.
     * @return browser object for the request
     */
    public ERXBrowser browser() {
        if (_browser == null) {
            ERXBrowserFactory browserFactory = ERXBrowserFactory.factory();
            _browser = browserFactory.browserMatchingRequest(this);
            browserFactory.retainBrowser(_browser);            
        }
        return _browser;
    }

    /**
     * Cleaning up retain count on the browser.
     */
    @Override
	public void finalize() throws Throwable {
        if (_browser != null) {
            ERXBrowserFactory.factory().releaseBrowser(_browser);
        }
        super.finalize();
    }
    
    /**
     * Returns whether or not this request is secure.
     * 
     * @return whether or not this request is secure
     */
    @Override
    public boolean isSecure() {
    	return ERXRequest.isRequestSecure(this);
    }
    
    /**
     * Add the protocol, server and port parts of this request to a StringBuffer.
     * @param stringbuffer 
     * 
     */
	public void _completeURLPrefix(StringBuffer stringbuffer) {
		_completeURLPrefix(stringbuffer, isSecure(), 0);
	}
	
    /**
     * Add the protocol, server and port parts to a StringBuffer to build an URL to this app.
     * if port is set to 0, this request port will be used.
     * @param stringbuffer 
     * @param secure generate a https url
     * @param port the port number to use, 0 this request port  
     * 
     */
	@Override
	public void _completeURLPrefix(StringBuffer stringbuffer, boolean secure, int port) {
    	if (_secureDisabled) {
    		secure = false;
    	}
    	
    	String serverName = _serverName();
        String portStr;
        if (port == 0) {
        	String sslPort = String.valueOf(ERXApplication.erxApplication().sslPort());
        	portStr = secure ? sslPort : _serverPort();
        } else {
        	portStr = WOShared.unsignedIntString(port);
        }
        if (secure) {
        	stringbuffer.append("https://");
        } else {
        	stringbuffer.append("http://");
        }
   		stringbuffer.append(serverName);
   		if(portStr != null && WOApplication.application().isDirectConnectEnabled() && ((secure && !"443".equals(portStr)) || (!secure && !"80".equals(portStr)))) {
   			stringbuffer.append(':');
   			stringbuffer.append(portStr);
        }
    }
    
    /**
     * Returns whether or not the given request is secure.
     * MS: I found this somewhere else a while ago, but I have no idea where or
     * I'd give attribution.
     * 
     * @param request the request to check
     * @return whether or not the given request is secure.
     */
    public static boolean isRequestSecure(WORequest request) {
        boolean isRequestSecure = false;
        
        // Depending on the adaptor the incoming port can be found in one of two
        // places.
        if (request != null) {
        	
	        String serverPort = request.headerForKey("SERVER_PORT");
	        if (serverPort == null) {
	        	serverPort = request.headerForKey("x-webobjects-servlet-server-port");
	        }
	        if (serverPort == null) {
	        	serverPort = request.headerForKey("x-webobjects-server-port");
	        }
	
	        // Apache and some other web servers use this to indicate HTTPS mode.
	        String httpsMode = request.headerForKey("https");
	
	        // If either the https header is 'on' or the server port is 443 then we
	        // consider this to be an HTTP request.
	        if (httpsMode != null && httpsMode.equalsIgnoreCase("on")) {
	        	isRequestSecure = true;
	        }
	        else if (serverPort != null && WOApplication.application() instanceof ERXApplication && String.valueOf(ERXApplication.erxApplication().sslPort()).equals(serverPort)) {
	        	isRequestSecure = true;
	        }
	        // MS: I have no idea how to do this properly ... There doesn't appear to be any way to
	        // determine which adaptor is servicing this request right now, and WOHttpIO only tracks the
	        // the originating port, not the original server port that serviced the request.  
	        else if (!request.isUsingWebServer()) {
	        	// It turns out there appears to always be a "host" header of the format "hostname:port" ... I
	        	// don't believe this is actually secure at ALL, so I'm only enabling it when you're not using
	        	// a webserver (i.e. probably testing).
	        	String hostHeader = request.headerForKey("host");
	        	if (hostHeader != null && WOApplication.application() instanceof ERXApplication && hostHeader.endsWith(":" + ERXApplication.erxApplication().sslPort())) {
	        		isRequestSecure = true;
	        	}
	        }
	        
	        // Check if we've got an x-forwarded-proto header which is typically sent by a load balancer that is 
	        // implementing ssl termination to indicate the request on the public side of the load balancer is secure.
	        else if (X_FORWARDED_PROTO_FOR_SSL.equals(request.headerForKey(X_FORWARDED_PROTO_HEADER_KEY_FOR_SSL))) {
	    		isRequestSecure = true;
	        }
        }

        return isRequestSecure;
    }

    private static class _LanguageComparator extends NSComparator {
        public _LanguageComparator() {}

        private static float quality(String languageString) {
            float result=0f;
            if (languageString!=null) {
                languageString = languageString.trim();
                int semicolon=languageString.indexOf(';');
                if (semicolon!=-1 &&
                    languageString.length()>semicolon+2) {
                    result=Float.parseFloat(languageString.substring(semicolon+1).trim().substring(2));
                } else
                    result=1.0f;
            }
            return result;
        }
        
        @Override
		public int compare(Object o1, Object o2) {
            float f1=quality((String)o1);
            float f2=quality((String)o2);
            return f1<f2 ? OrderedDescending : ( f1==f2 ? OrderedSame : OrderedAscending ); // we want DESCENDING SORT!!
        }
        
    }

    private final static NSComparator COMPARE_Qs = new _LanguageComparator();

    /** Translates ("de", "en-us;q=0.33", "en", "en-gb;q=0.66") to ("de", "en_gb", "en-us", "en").
     * @param languages NSArray of Strings
     * @return sorted NSArray of normalized Strings
     */
    protected NSArray<String> fixAbbreviationArray(NSArray<String> languages) {
        try {
            languages=languages.sortedArrayUsingComparator(COMPARE_Qs);
        } catch (NSComparator.ComparisonException e) {
            log.warn("Couldn't sort language array {}.", languages, e);
        } catch (NumberFormatException e2) {
            log.warn("Couldn't sort language array {}.", languages, e2);
        }
        NSMutableArray<String> languagePrefix = new NSMutableArray<>(languages.count());
        for (int languageNum = languages.count() - 1; languageNum >= 0; languageNum--) {
            String language = languages.objectAtIndex(languageNum);
            int offset;
            language = language.trim();
            offset = language.indexOf(';');
            if (offset > 0) {
                language = language.substring(0, offset);
            }
            offset = language.indexOf('-');
            if (offset > 0) {
                String langPrefix = language.substring(0, offset);  //  "en" part of "en-us"
                if (!languagePrefix.containsObject(langPrefix)) { 
                    languagePrefix.insertObjectAtIndex(langPrefix, 0);
                }
                // converts "en-us" into "en_us";
                
                String cooked = language.replace('-', '_');
                language = cooked;
            }
            languagePrefix.insertObjectAtIndex(language, 0);
        }
        return languagePrefix;
    }

    /**
     * Parses all cookies one at a time catch parse exception which just discards
     * that cookie and not all cookies. It uses java.net.HttpCookie as a parser.
     * @return a dictionary of cookies, parsed one cookie at a time
     */
    private NSDictionary _cookieDictionary() {
        if (_cookieDictionary == null) {
        	NSMutableDictionary<String, NSArray<String>> cookieDictionary = new NSMutableDictionary<String, NSArray<String>>();
        	//
        	// from WORequest._cookieDescription()
        	String cookie = headerForKey("cookie");
        	if (cookie == null || cookie.length() == 0)
        		// IIS cookies use a different header
        		cookie = headerForKey("http_cookie");
        	
        	if (cookie != null && cookie.length() > 0) {
        		String[] cookies = cookie.split(";");
        		for (int i = 0; i < cookies.length; i++) {
        			try {
        				//
        				// only parse one cookie at a time => get(0)
        				HttpCookie httpCookie = HttpCookie.parse(cookies[i]).get(0);
        				//
        				// Cookies with longer paths are listed before cookies with shorter paths:
        				// see https://stackoverflow.com/a/24214538
        				// Cookies with longer Patch are more specific than cookies with shorter path 
        				// and should not be replaced by a less specific cookie 
        				// If a cookie with Therfore we do not override cookies if there are already there!
        				String cookieName  = httpCookie.getName();
        				String cookieValue = httpCookie.getValue();
        				log.debug("Cookie: '"+cookieName+"' = '"+cookieValue+"'");
        				NSArray<String> cookieValueArray = cookieDictionary.get(cookieName);
        				if ( cookieValueArray == null ){
        					cookieValueArray = new NSArray<>();
        				}
        				cookieValueArray = cookieValueArray.arrayByAddingObject(cookieValue);
        				cookieDictionary.put( cookieName, cookieValueArray );
        			} catch (Throwable t) {
        				log.warn("Unable to parse cookie '"+cookies[i]+"' : "+t.getMessage());
        			}
        		}
        	}
        	_cookieDictionary = cookieDictionary.immutableClone();
        }
        return _cookieDictionary;
    }

    /**
     * Overridden to call _cookieDictionary() where we parse the cookies one
     * at a time using java.net.HttpCookie so that we don't get an empty cookie
     * dictionary if one cookie is malformed.
     */
    @Override
	public NSDictionary cookieValues() {
    	return _cookieDictionary();
    }    

    /**
     * Overridden because the super implementation would pull in all 
     * content even if the request is supposed to be streaming and thus 
     * very large. Will now return <code>false</code> if the request
     * handler is streaming.
     * 
     * @return <code>true</code> if the session ID can be obtained from the form values or a cookie.
     */
    @Override
	public boolean isSessionIDInRequest() {
        ERXApplication app = (ERXApplication)WOApplication.application();
        
        if (app.isStreamingRequestHandlerKey(requestHandlerKey())) {
            return false;
        }
            return super.isSessionIDInRequest();
        }
    

    /**
     * Overridden because the super implementation would pull in all 
     * content even if the request is supposed to be streaming and thus 
     * very large. Will now look for the session ID only in the cookie
     * values.
     * 
     * @param inCookiesFirst
     *            define if session ID should be searched first in cookie
     */
    @Override
	protected String _getSessionIDFromValuesOrCookie(boolean inCookiesFirst) {
        ERXApplication app = (ERXApplication)WOApplication.application();
        String sessionIdKey = WOApplication.application().sessionIdKey();

        boolean wis = WOApplication.application().streamActionRequestHandlerKey().equals(requestHandlerKey());
        boolean alternateStreaming = app.isStreamingRequestHandlerKey(requestHandlerKey());
        boolean streaming = wis || alternateStreaming;
        
        String sessionID = null;
        if(inCookiesFirst) {
            sessionID = cookieValueForKey(sessionIdKey);
            if(sessionID == null && !streaming) {
                sessionID = stringFormValueForKey(sessionIdKey);
            }
        } else {
            if(!streaming) {
                sessionID = stringFormValueForKey(sessionIdKey);
            }
            if(sessionID == null) {
                sessionID = cookieValueForKey(sessionIdKey);
            }
        }
        return sessionID;
    }
    
    /**
     * Utility method to set credentials for basic authorization.
     * 
     * @param userName
     *            the user name
     * @param password
     *            the password
     */
    public void setCredentials(String userName, String password) {
        String up = userName + ":" + password;
        byte[] bytes = up.getBytes();
        String encodedString = Base64.encodeBase64String(bytes);
        setHeader("Basic " +  encodedString, "authorization");
    }

    /**
     * Returns the remote client host address. Works in various setups, like
     * direct connect, deployed etc. If no host name can be found,
     * returns "UNKNOWN".
     * 
     * @return remote client host address
     */
    public String remoteHostAddress() {
        if (WOApplication.application().isDirectConnectEnabled()) {
            if (_originatingAddress() != null) {
                return _originatingAddress().getHostAddress();
            }
        }
        for (String key : HOST_ADDRESS_KEYS) {
        	String remoteAddressHeaderValue = headerForKey(key); 
			if (remoteAddressHeaderValue != null) {
				return remoteAddressHeaderValue;
			}
		}
        return UNKNOWN_HOST;
    }
    
    /**
     * Returns the remote client host name. If no host name can be found,
     * returns "UNKNOWN".
     * 
     * @return remote client host name
     */
    public String remoteHostName() {
    	for (String key : HOST_NAME_KEYS) {
			if (headerForKey(key) != null) {
				return headerForKey(key);
			}
		}
    	return UNKNOWN_HOST;
    }

	public NSMutableDictionary<String, Object> mutableUserInfo() {
		NSDictionary userInfo = userInfo();
		NSMutableDictionary mutableUserInfo;
		if (userInfo == null) {
			mutableUserInfo = new NSMutableDictionary();
			_userInfo = mutableUserInfo;
		}
		else if (userInfo instanceof NSMutableDictionary) {
			mutableUserInfo = (NSMutableDictionary) userInfo;
		}
		else {
			mutableUserInfo = userInfo.mutableClone();
			_userInfo = mutableUserInfo;
		}
		return mutableUserInfo;
	}
}
