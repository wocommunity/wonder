//
// ERXBrowserFactory.java
// Project ERExtensions
//
// Created by tatsuya on Mon Jul 22 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import java.util.*;

/**
 * All WebObjects applications have exactly one ERXBrowserFactory 
 * instance. Its primary role is to manage ERXBrowser objects. 
 * It provides facility to parse "user-agent" HTTP header and to 
 * create an appropriate browser object. It also maintains the 
 * browser pool to store shared ERXBrowser objects. Since ERXBrowser 
 * object is immutable, it can be safely shared between sessions 
 * and ERXBrowserFactory tries to have only one instance of 
 * ERXBrowser for each kind of web browsers.<br>
 * 
 * The primary method called by ERXSession and ERXDirectAction is 
 * <code>browserMatchingRequest</code> which takes a WORequest as 
 * the parameter and returns a shared instance of browser object. 
 * You actually wouldn't have to call this function by yourself 
 * because ERXSession and ERXDirectAction provide <code>broser</code> 
 * method that returns a browser object for the current request
 * for you.<br>
 * 
 * Note that ERXSession and ERXDirectAction call ERXBrowserFactory's 
 * <code>retainBrowser</code> and <code>releaseBrowser</code> 
 * to put the browser object to the browser pool when it is 
 * created and to remove the browser object from the pool when 
 * it is no longer referred from sessions and direct actions. 
 * ERXSession and ERXDirectAction automatically handle this and 
 * you do not have to call these methods from your code.<br>
 * 
 * The current implementation of the parsers support variety of 
 * Web browsers in the market such as Internet Explorer (IE), 
 * OmniWeb, Netscape, iCab and Opera, versions between 2.0 and 7.0. <br>
 * 
 * To customize the parsers for "user-agent" HTTP header, subclass  
 * ERXBrowserFactory and override methods like <code>parseBrowserName</code>, 
 * <code>parseVersion</code>, <code>parseMozillaVersion</code> 
 * and <code>parsePlatform</code>. Then put the following statement 
 * into the application's constructor. <br>
 * 
 * <code>ERXBrowserFactory.setFactory(new SubClassOfERXBrowserFactory());</code><br>
 * 
 * If you want to use your own subclass of ERXBrowser, extend 
 * ERXBrowserFactory and override <code>createBrowser</code> method. 
 * This method will only have to contain <code>return new SubClassOfERXBrowser();</code> 
 * statement and should not call super. Then, again, put 
 * <code>ERXBrowserFactory.setFactory(new SubClassOfERXBrowserFactory)</code> 
 * statement into the application's constructor. <br>
 */ 

// This implementation is tested with the following browsers (or "user-agent" strings)
// Please ask the guy (tatsuyak@mac.com) for WOUnitTest test cases. 
//
// Mac OS X 
// ----------------------------------------------------------------------------------
// iCab 2.8.1       Mozilla/4.5 (compatible; iCab 2.8.1; Macintosh; I; PPC)
// IE 5.21          Mozilla/4.0 (compatible; MSIE 5.21; Mac_PowerPC)
// Netscape 7.0b1   Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:1.0rc2) Gecko/20020512 Netscape/7.0b1
// Netscape 6.2.3   Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:0.9.4.1) Gecko/20020508 Netscape6/6.2.3
// OmniWeb 4.1-v422 Mozilla/4.5 (compatible; OmniWeb/4.1-v422; Mac_PowerPC)
//
// Windows 2000
// ----------------------------------------------------------------------------------
// IE 6.0           Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)
// IE 5.5           Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)
// Netscape 6.2.3   Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:0.9.4.1) Gecko/20020508 Netscape6/6.2.3
// Netscape 4.79    Mozilla/4.79 [en] (Windows NT 5.0; U)
// Opera 6.04       Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.04  [en]

public class ERXBrowserFactory {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getLogger(ERXBrowserFactory.class);
    
    private static ERXBrowserFactory _factory = new ERXBrowserFactory();
    public static ERXBrowserFactory factory() { return _factory; }
    public static void setFactory(ERXBrowserFactory newFactory) { _factory = newFactory; }

    /** 
     * Get a shared browser object for given request. 
     * Parses "user-agent" string in the request and gets the appropiate 
     * browser object. <br>
     * This is the primary method to call from application logics, and 
     * once you get a browser object, you are responsible to call 
     * <code>retainBrowser</code> to keep the browser object in the browser pool. 
     * You are also required to call <code>releaseBrowser</code> to release the 
     * browser from the pool when it is no longer needed. 
     * 
     * @param request WORequest
     * @return a shared browser object
     */
    public ERXBrowser browserMatchingRequest(WORequest request) {
        if (request == null)   throw new IllegalArgumentException("Request can't be null.");

        String ua = (String)request.headerForKey("user-agent");
        if (ua == null) {
            return getBrowserInstance(ERXBrowser.UNKNOWN_BROWSER, ERXBrowser.UNKNOWN_VERSION, 
                                        ERXBrowser.UNKNOWN_VERSION, ERXBrowser.UNKNOWN_PLATFORM, null);
        } else {
            String browserName 		= parseBrowserName(ua);
            String version 		= parseVersion(ua);
            String mozillaVersion	= parseMozillaVersion(ua);
            String platform 		= parsePlatform(ua);
            NSDictionary userInfo 	= new NSDictionary(parseCPU(ua), "cpu");
            
            return getBrowserInstance(browserName, version, mozillaVersion, platform, userInfo);
        }
    }

    /** 
     * Get a shared browser object from browser pool. If such browser object 
     * does not exist, this method will create one by using 
     * <code>createBrowser</code> method.
     * 
     * @param browserName string
     * @param version string
     * @param platform string
     * @userInfo dictionary
     * @return a shared browser object
     */
    public synchronized ERXBrowser getBrowserInstance(String browserName, String version, String mozillaVersion, 
                                                String platform, NSDictionary userInfo) {
        String key = _computeKey(browserName, version, mozillaVersion, platform, userInfo);
        ERXBrowser browser = (ERXBrowser)_browserPool().objectForKey(key);
        if (browser == null) 
            browser = createBrowser(browserName, version, mozillaVersion, platform, userInfo);
        return browser;
    }

    /** 
     * Creates a new browser object for given parameters. Override this method 
     * if you need to provide your own subclass of ERXBrowser. 
     * If you override it, your implementation should not call super.
     * 
     * @param browserName string
     * @param version string
     * @param mozillaVersion string
     * @param platform string
     * @userInfo dictionary
     * @return new browser object that is a concrete subclass of ERXBrowser
     */
    public synchronized ERXBrowser createBrowser(String browserName, String version, String mozillaVersion,
                                                String platform, NSDictionary userInfo) {
        return new ERXBasicBrowser(browserName, version, mozillaVersion, platform, userInfo);
    }

    /** 
     * 
     * @param
     */ 
    public synchronized void retainBrowser(ERXBrowser browser) {
        String key = _computeKey(browser);
	_browserPool().setObjectForKey(browser, key);
        _incrementReferenceCounterForKey(key);
    }

    /** 
     * 
     * @param
     */ 
    public synchronized void releaseBrowser(ERXBrowser browser) {
        String key = _computeKey(browser);
        ERXMutableInteger count = _decrementReferenceCounterForKey(key);
        if (count == null) {
            // Perhaps forgot to call registerBrowser() but try to remove the browser for sure
            _browserPool().removeObjectForKey(key);
        } else if (count.intValue() <= 0) {
            _browserPool().removeObjectForKey(key);
            _referenceCounters().removeObjectForKey(key);
        } 
    }

    /** 
     * 
     * @param
     */ 
    public String parseBrowserName(String userAgent) {
        String browserString = _browserString(userAgent);

        String browser  = ERXBrowser.UNKNOWN_BROWSER;
        if      (browserString.indexOf("MSIE") > -1) 		browser  = ERXBrowser.IE;
        else if (browserString.indexOf("OmniWeb") > -1)		browser  = ERXBrowser.OMNIWEB;
        else if (browserString.indexOf("iCab") > -1)		browser  = ERXBrowser.ICAB;
        else if (browserString.indexOf("Opera") > -1)		browser  = ERXBrowser.OPERA;
        else if (browserString.indexOf("Netscape") > -1)	browser  = ERXBrowser.NETSCAPE;

        // This condition should always come last because *all* browsers have 
        // the word Mozilla at the beginning of their user-agent string. 
        else if (browserString.indexOf("Mozilla") > -1)		browser  = ERXBrowser.NETSCAPE;

        return browser;
    }

    /** 
     * 
     * @param
     */ 
    public String parseVersion(String userAgent) {
        String browserString = _browserString(userAgent);
        int startpos;
        String version = ERXBrowser.UNKNOWN_VERSION;
        
        // Remove "Netscape6" from string such as "Netscape6/6.2.3", 
        // otherwise this method will produce wrong result "6/6.2.3" as the version
        final String netscape6 = "Netscape6";
        startpos = browserString.indexOf(netscape6);
        if (startpos > -1) 
            browserString = browserString.substring(startpos + netscape6.length());
        
        // Find first numeric in the string such as "MSIE 5.21; Mac_PowerPC)"
        startpos = ERXStringUtilities.indexOfNumericInString(browserString);

        if (startpos > -1) {
            StringTokenizer st = new StringTokenizer(browserString.substring(startpos), " ;"); 
            if (st.hasMoreTokens()) 
                version = st.nextToken();  // Will return "5.21" portion of "5.21; Mac_PowerPC)"
        }
        return version;
    }

    /** 
     * 
     * @param
     */ 
    public String parseMozillaVersion(String userAgent) {
        final String mozilla = "Mozilla/";
        String mozillaVersion = ERXBrowser.UNKNOWN_VERSION;
        int startpos = userAgent.indexOf(mozilla);
        if (startpos > -1) {
            StringTokenizer st = new StringTokenizer(userAgent.substring(startpos + mozilla.length()), " ;"); 
            if (st.hasMoreTokens()) 
                mozillaVersion = st.nextToken();  // Will return "5.21" portion of "5.21; Mac_PowerPC)"
        }
        return mozillaVersion;
    }

    /** 
     * 
     * @param
     */ 
    public String parsePlatform(String userAgent) {
        String platform = ERXBrowser.UNKNOWN_PLATFORM;
        if      (userAgent.indexOf("Win") > -1) 	platform = ERXBrowser.WINDOWS;
        else if (userAgent.indexOf("Mac") > -1) 	platform = ERXBrowser.MACOS;
        else if (userAgent.indexOf("Linux") > -1) 	platform = ERXBrowser.LINUX;
        return platform;
    }

    /** 
     * 
     * @param
     */ 
    public String parseCPU(String userAgent) {
        String cpu = ERXBrowser.UNKNOWN_CPU;
        if      (userAgent.indexOf("PowerPC") > -1) 	cpu = ERXBrowser.POWER_PC;
        else if (userAgent.indexOf("PPC") > -1) 	cpu = ERXBrowser.POWER_PC;
        return cpu;
    }



    private String _browserString(String userAgent) {
        String browserString = userAgent;
        int startpos;

        // Get substring "Opera 6.04  [en]" 
        // from          "Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.04  [en]"
        final String opera = "Opera";
        startpos = browserString.indexOf(opera);
        if (startpos > -1) 
            browserString = browserString.substring(startpos);

        // Get substring "MSIE 5.21; Mac_PowerPC)"
        // from          "Mozilla/4.0 (compatible; MSIE 5.21; Mac_PowerPC)" 
        final String compatible = "compatible;";
        startpos = browserString.indexOf(compatible);
        if (startpos > -1) 
            browserString = browserString.substring(startpos + compatible.length());
            
        // Get substring "Netscape6/6.2.3" 
        // from          "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:0.9.4.1) 
        //                Gecko/20020508 Netscape6/6.2.3"
        final String netscape = "Netscape";
        startpos = browserString.indexOf(netscape);
        if (startpos > -1) 
            browserString = browserString.substring(startpos);
            
        return browserString;
    }

    private NSMutableDictionary _browserPool;
    private NSMutableDictionary _browserPool() { 
        if (_browserPool == null) 
            _browserPool = new NSMutableDictionary();
        return _browserPool;
    }

    private NSMutableDictionary _referenceCounters;
    private NSMutableDictionary _referenceCounters() {
        if (_referenceCounters == null)
            _referenceCounters = new NSMutableDictionary();
        return _referenceCounters;
    }

    private ERXMutableInteger _incrementReferenceCounterForKey(String key) {
        ERXMutableInteger count = (ERXMutableInteger)_referenceCounters().objectForKey(key);
        if (count != null) 
            count.increment();
        else {
            count = new ERXMutableInteger(1);
            _referenceCounters().setObjectForKey(count, key);
        }
        if (log.isDebugEnabled()) 
            log.debug("_incrementReferenceCounterForKey() - count = " + count + ", key = " + key);
        return count;
    }

    private ERXMutableInteger _decrementReferenceCounterForKey(String key) {
        ERXMutableInteger count = (ERXMutableInteger)_referenceCounters().objectForKey(key);
        if (count != null)  
            count.decrement();
        
        if (log.isDebugEnabled()) 
            log.debug("_decrementReferenceCounterForKey() - count = " + count + ", key = " + key);
        return count;
    }

    private String _computeKey(ERXBrowser browser) {
        return browser.browserName() + "." + browser.version() + "." + browser.mozillaVersion() + "."
                        + browser.platform() + "." + browser.userInfo().toString();
    }

    private String _computeKey(String browserName, String version, String mozillaVersion, 
                                                                String platform, NSDictionary userInfo) {
        return browserName + "." + version + "." + mozillaVersion + "." + platform + "." + userInfo.toString();
    }

}
