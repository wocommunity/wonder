package er.extensions;
import java.net.InetAddress;

import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WOProperties;
import com.webobjects.appserver._private.WOShared;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/** Subclass of WORequest that fixes several Bugs.
 * The ID's are #2924761 and #2961017. It can also be extended to handle
 * #2957558 ("de-at" is converted to "German" instead of "German_Austria").
 * The request is created via {@link ERXApplication#createRequest(String,String,String, NSDictionary,NSData,NSDictionary)}.
 */
public  class ERXRequest extends WORequest {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXRequest.class);

    protected static Boolean isBrowserFormValueEncodingOverrideEnabled;
    public boolean isBrowserFormValueEncodingOverrideEnabled() {
        if (isBrowserFormValueEncodingOverrideEnabled == null) {
            isBrowserFormValueEncodingOverrideEnabled = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXRequest.BrowserFormValueEncodingOverrideEnabled", false) ? Boolean.TRUE : Boolean.FALSE;
        }
        return isBrowserFormValueEncodingOverrideEnabled.booleanValue();
    }
    
    public WOContext context() {
    	return _context();
    }
    
    /** Simply call superclass constructor */
    public ERXRequest(String string, String string0, String string1,
                      NSDictionary nsdictionary, NSData nsdata,
                      NSDictionary nsdictionary2) {
        super(string, string0, string1, nsdictionary,
              nsdata, nsdictionary2);
        if (isBrowserFormValueEncodingOverrideEnabled() && browser().formValueEncoding() != null) {
            setDefaultFormValueEncoding(browser().formValueEncoding());
        }
    }

    /** NSArray to keep browserLanguages in. */
    protected  NSArray _browserLanguages;

    /** holds a reference to the browser object */
    protected ERXBrowser _browser;
    
    /** Returns a cooked version of the languages the user has set in his Browser.
     * Adds "Nonlocalized" and {@link ERXLocalizer#defaultLanguage()} if not
     * already present.
     * @return cooked version of user's languages
     */
    public NSArray browserLanguages() {
        if (_browserLanguages == null) {
            NSArray fixedLanguages = null;
            String string = this.headerForKey("accept-language");
            if (string != null) {
                NSArray rawLanguages
                = NSArray.componentsSeparatedByString(string, ",");
                fixedLanguages = fixAbbreviationArray(rawLanguages);
            }
            NSMutableArray languageKeys =
                WOProperties.TheLanguageDictionary.objectsForKeys(fixedLanguages,
                                                                  null).mutableClone();
            languageKeys.addObject("Nonlocalized");
            if(!languageKeys.containsObject(ERXLocalizer.defaultLanguage()))
                languageKeys.addObject(ERXLocalizer.defaultLanguage());
            _browserLanguages = languageKeys.immutableClone();
        }
        return _browserLanguages;
    }
    
    public String stringFormValueForKey(String key) {
    	String result = super.stringFormValueForKey(key);
    	if (result == null && "wodata".equals(key)) {
    	    // AK: yet another crappy 5.4 fix, WODynamicURL changed packages
    		String requestHandlerKey = (String)valueForKeyPath("_uriDecomposed.requestHandlerKey");
    		if (WOApplication.application().resourceRequestHandlerKey().equals(requestHandlerKey)) {
    			String requestHandlerPath = (String)valueForKeyPath("_uriDecomposed.requestHandlerPath");
        		requestHandlerPath = "file:/" +  requestHandlerPath.substring("wodata=/".length());
    			result = requestHandlerPath.replace('+', ' ');
    		}
    	}

		return result;
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
     * Cleaning up retian count on the browser.
     */
    public void finalize() throws Throwable {
        if (_browser != null)
            ERXBrowserFactory.factory().releaseBrowser(_browser);
        super.finalize();
    }
    
    /**
     * Returns whether or not this request is secure.
     * 
     * @return whether or not this request is secure
     */
    public boolean isSecure() {
    	return ERXRequest.isRequestSecure(this);
    }
    
    public void _completeURLPrefix(StringBuffer stringbuffer, boolean secure, int port) {
    	String serverName = _serverName();
        String portStr;
        if (port == 0) {
        	portStr = secure ? "443" : _serverPort();
        } else {
        	portStr = WOShared.unsignedIntString(port);
        }
        if (secure) {
        	stringbuffer.append("https://");
        } else {
        	stringbuffer.append("http://");
        }
   		stringbuffer.append(serverName);
   		if(portStr != null && ((secure && !"443".equals(portStr)) || (!secure && !"80".equals(portStr)))) {
   			stringbuffer.append(':');
   			stringbuffer = stringbuffer.append(portStr);
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
	          serverPort = request.headerForKey("x-webobjects-server-port");
	        }
	
	        // Apache and some other web servers use this to indicate HTTPS mode.
	        String httpsMode = request.headerForKey("https");
	
	        // If either the https header is 'on' or the server port is 443 then we
	        // consider this to be an HTTP request.
	        isRequestSecure = ((httpsMode != null && httpsMode.equalsIgnoreCase("on")) || (serverPort != null && "443".equals(serverPort)));
        }

        return isRequestSecure;
    }

    private static class _LanguageComparator extends NSComparator {
        
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
        public int compare(Object o1, Object o2) {
            float f1=quality((String)o1);
            float f2=quality((String)o2);
            return f1<f2 ? OrderedDescending : ( f1==f2 ? OrderedSame : OrderedAscending ); // we want DESCENDING SORT!!
        }
        
    }

    
    /** Translates ("de", "en-us;q=0.33", "en", "en-gb;q=0.66") to ("de", "en_gb", "en-us", "en").
     * @param languages NSArray of Strings
        * @return sorted NSArray of normalized Strings
        */
    private final static NSComparator COMPARE_Qs=new _LanguageComparator();
    protected NSArray fixAbbreviationArray(NSArray languages) {
        try {
            languages=languages.sortedArrayUsingComparator(COMPARE_Qs);
        } catch (NSComparator.ComparisonException e) {
            log.warn("Couldn't sort language array "+languages+": "+e);
        } catch (NumberFormatException e2) {
            log.warn("Couldn't sort language array "+languages+": "+e2);
        }
        NSMutableArray nsmutablearray = new NSMutableArray(languages.count());
        int cnt = languages.count();
        for (int i = cnt - 1; i >= 0; i--) {
            String string = (String) languages.objectAtIndex(i);
            int offset;
            string = string.trim();
            offset = string.indexOf(';');
            if (offset > 0)
                string = string.substring(0, offset);
            offset = string.indexOf('-');
            if (offset > 0) {
                String langPrefix = string.substring(0, 2);  //  "en" part of "en-us"
                if (!nsmutablearray.containsObject(langPrefix)) 
                    nsmutablearray.insertObjectAtIndex(langPrefix, 0);
                // converts "en-us" into "en_us";
                StringBuffer cooked = new StringBuffer(string.length());
                cooked.append(langPrefix)
                    .append("_")
                    .append(string.substring(offset+1, offset+3));
                string = cooked.toString();
            }
            nsmutablearray.insertObjectAtIndex(string, 0);
        }
        return nsmutablearray;
    }

    /**
     * Overridden because malformed cookie to return an empty dictionary
     * if the super implementation throws an exception. This will happen
     * if the request contains malformed cookie values.
     */
    public NSDictionary cookieValues() {
        try {
            return super.cookieValues();
        } catch (Throwable t) {
            log.warn(t + ":" + this);
            log.warn(t);
            return NSDictionary.EmptyDictionary;
        }
    }    

    /**
     * Overridden because the super implementation would pull in all 
     * content even if the request is supposed to be streaming and thus 
     * very large. Will now return <code>false</code> if the request
     * handler is streaming.
     */
    public boolean isSessionIDInRequest() {
        ERXApplication app = (ERXApplication)WOApplication.application();
        
        if (app.isStreamingRequestHandlerKey(requestHandlerKey())) {
            return false;
        } else {
            return super.isSessionIDInRequest();
        }
    }

    /**
     * Overridden because the super implementation would pull in all 
     * content even if the request is supposed to be streaming and thus 
     * very large. Will now look for the session ID only in the cookie
     * values.
     */

    protected String _getSessionIDFromValuesOrCookie(boolean inCookiesFirst) {
        ERXApplication app = (ERXApplication)WOApplication.application();

        boolean wis = WOApplication.application().streamActionRequestHandlerKey().equals(requestHandlerKey());
        boolean alternateStreaming = app.isStreamingRequestHandlerKey(requestHandlerKey());
        boolean streaming = wis || alternateStreaming;
        
        String sessionID = null;
        if(inCookiesFirst) {
            sessionID = cookieValueForKey("wosid");
            if(sessionID == null && !streaming) {
                sessionID = stringFormValueForKey("wosid");
            }
        } else {
            if(!streaming) {
                sessionID = stringFormValueForKey("wosid");
            }
            if(sessionID == null) {
                sessionID = cookieValueForKey("wosid");
            }
        }
        return sessionID;
    }
    
    /**
     * Utility method to set credentials for basic authorization.
     * 
     */
    public void setCredentials(String userName, String password) {
        String up = userName + ":" + password;
        BASE64Encoder coder = new BASE64Encoder();
        byte[] bytes = up.getBytes();
        String encodedString = coder.encode(bytes);
        setHeader("Basic " +  encodedString, "authorization");
    }
    
    /**
     * Returns the remote host name. Works in various setups, like
     * direct connect, deployed etc. If no host name can be found,
     * returns "UNKNOWN".
     */
    public String remoteHost() {
        String host = null;
        if (WOApplication.application().isDirectConnectEnabled()) {
            InetAddress hostAdd = _originatingAddress();
            if (hostAdd != null) {
                host = hostAdd.getHostAddress();
                return host;
            }
        }
        if (host == null) {
            host = headerForKey("remote_host");
            if (host != null) return host;
            
            host= headerForKey("remote_addr");
            if (host != null) return host;
            
            host = headerForKey("remote_user");
            if (host != null) return host;
            
            host = headerForKey("x-webobjects-remote-addr");
            if (host != null) return host;
        }
        return "UNKNOWN";
    }

	public NSMutableDictionary mutableUserInfo() {
		NSDictionary userInfo = userInfo();
		NSMutableDictionary mutableUserInfo;
		if (userInfo == null) {
			mutableUserInfo = new NSMutableDictionary();
			setUserInfo(mutableUserInfo);
		}
		else if (userInfo instanceof NSMutableDictionary) {
			mutableUserInfo = (NSMutableDictionary) userInfo;
		}
		else {
			mutableUserInfo = userInfo.mutableClone();
			setUserInfo(mutableUserInfo);
		}
		return mutableUserInfo;
	}
}
