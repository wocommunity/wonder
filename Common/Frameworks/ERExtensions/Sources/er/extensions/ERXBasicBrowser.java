//
// ERXBasicBrowser.java
// Project ERExtensions
//
// Created by tatsuya on Tue Jul 23 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import java.util.*;

/**
 * ERXBasicBrowser is a concrete subclass of ERXBrowser that 
 * defines browser object. A browser object represents the web 
 * browser that the current request-response loop is dealing 
 * with. It holds the information retrieved from HTTP request's 
 * "user-agent" header, and such information includes web 
 * browser's name, version, Mozilla compatible version and 
 * platform (OS). Also, a browser object can answer boolean 
 * questions such as <code>isIE</code>, <code>isOmniWeb</code>, 
 * <code>isVersion5</code> and <code>isMozilla40Compatible</code>, 
 * and even more specific questions like <code>isIFrameSupported</code> 
 * and <code>willRenderNestedTablesFast</code>. <br>
 * 
 * ERXBrowser is immutable and shared by different sessions and
 * direct actions. 
 * The shared instances are managed by ERXBrowserFactory which 
 * is also responsible to parse "user-agent" header in a WORequest 
 * object and to get an appropriate browser object. <br>
 * 
 * You can extends ERXBrowser or its concrete subclass ERXBasicBrowser 
 * to implement more specific questions for your application. 
 * One good example will be to have a question <code>isSupportedBrowser</code> 
 * that checks if the client is using one of the supported 
 * browsers for your application. <br>
 * 
 * ERXSession holds a browser object that represent the web 
 * browser for that session and <code>browser</code> method 
 * returns the object. 
 * 
 * To access ERXBasicBrowser's boolean questions from WOConditionals 
 * on a web component, set the path like "session.brower.isIFrameSupported" 
 * to their condition bindings. <br>
 * 
 * ERXDirectAction also holds a browser object for the current request. 
 * Use its <code>browser</code> method to access the object from a 
 * session-less direct action. <br>
 */ 
public class ERXBasicBrowser extends ERXBrowser {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getLogger(ERXBasicBrowser.class);

    private final String _browserName;
    private final String _version;
    private final String _mozillaVersion;
    private final String _platform;
    private final String _cpu;
    private final NSDictionary _userInfo;

    private final boolean _isICab;
    private final boolean _isIE;
    private final boolean _isNetscape;
    private final boolean _isOmniweb;
    private final boolean _isOpera;
    private final boolean _isUnknownBrowser;

    private final boolean _isMozillaVersion50;
    private final boolean _isMozillaVersion45;
    private final boolean _isMozillaVersion40;

    private final boolean _isVersion9;
    private final boolean _isVersion8;
    private final boolean _isVersion7;
    private final boolean _isVersion6;
    private final boolean _isVersion5;

    private final boolean _isVersion45;  
    private final boolean _isVersion41;  
    private final boolean _isVersion40;

    private final boolean _isVersion4;
    private final boolean _isVersion3;
    private final boolean _isVersion2;
    
    private final boolean _isMacOS;
    private final boolean _isWindows;
    private final boolean _isLinux;
    private final boolean _isUnknownPlatform;

    // non-public but default-access browser; prevents direct instantiation from outside the package
    ERXBasicBrowser(String browserName, String version, String mozillaVersion, 
                                                        String platform, NSDictionary userInfo) {
        if (userInfo instanceof NSMutableDictionary)
            userInfo = new NSDictionary(userInfo);
        
        _userInfo 		= userInfo != null ? userInfo : null; 
        
        _browserName	 	= browserName	 != null ?  browserName    : UNKNOWN_BROWSER;
        _version  		= version 	 != null ?  version        : UNKNOWN_VERSION;
        _mozillaVersion		= mozillaVersion != null ?  mozillaVersion : UNKNOWN_VERSION;
        _platform 		= platform	 != null ?  platform       : UNKNOWN_PLATFORM;

        String tempCpu 		= userInfo	!= null ?  (String)userInfo.objectForKey("cpu") : UNKNOWN_CPU;
        _cpu      		= tempCpu	!= null ?  tempCpu : UNKNOWN_CPU;
        
        _isICab			= _browserName.equals(ICAB);
        _isIE			= _browserName.equals(IE);
        _isNetscape		= _browserName.equals(NETSCAPE);
        _isOmniweb		= _browserName.equals(OMNIWEB);
        _isOpera		= _browserName.equals(OPERA);
        _isUnknownBrowser	= _browserName.equals(UNKNOWN_BROWSER);

        _isMozillaVersion50	= -1 < _mozillaVersion.indexOf("5.0");

        _isMozillaVersion45	= (-1 < _mozillaVersion.indexOf("4.5"))
                                        || (-1 < _mozillaVersion.indexOf("4.6"))
                                        || (-1 < _mozillaVersion.indexOf("4.7"));

        _isMozillaVersion40	= -1 < _mozillaVersion.indexOf("4.0");
        
        _isVersion9		= -1 < _version.indexOf("9.");
        _isVersion8		= -1 < _version.indexOf("8.");
        _isVersion7		= -1 < _version.indexOf("7.");
        _isVersion6		= -1 < _version.indexOf("6.");
        _isVersion5		= -1 < _version.indexOf("5.");

        _isVersion45		= (-1 < _version.indexOf("4.5")) 
                                        || (-1 < _version.indexOf("4.6"))
                                        || (-1 < _version.indexOf("4.7"));

        _isVersion41		= -1 < _version.indexOf("4.1");
        _isVersion40		= -1 < _version.indexOf("4.0");

        _isVersion4		= -1 < _version.indexOf("4.");
        _isVersion3		= -1 < _version.indexOf("3.");
        _isVersion2		= -1 < _version.indexOf("2.");

        _isMacOS		= _platform.equals(MACOS);
        _isWindows		= _platform.equals(WINDOWS);
        _isLinux		= _platform.equals(LINUX);
        _isUnknownPlatform	= _platform.equals(UNKNOWN_PLATFORM);
    }

    /**
     * Browser name string
     * @return what type of browser
     */
    public String browserName() { return _browserName; }

    /**
     * Version string
     * @return what version of browser
     */
    public String version() { return _version; }

    /**
     * MozillaVersion string
     * @return what Mozilla version equivement to the browser's version
     */
    public String mozillaVersion() { return _mozillaVersion; }

    /**
     * Platform string
     * @return what platform that the browser is running on
     */
    public String platform() { return _platform; }

    /**
     * CPU string
     * @return what processor that the browser is running on
     */
    public String cpu() { return _cpu; }

    /**
     * UserInfo dictionary
     * @return userInfo dictionary
     */
    public NSDictionary userInfo() { return _userInfo; }

    /**
     * Browser is iCab?
     * @return true if browser is iCab.
     */
    public boolean isICab() { return _isICab; }

    /**
     * Browser is Internet Explorer?
     * @return true if browser is IE.
     */
    public boolean isIE() { return _isIE; }

    /**
     * Browser is Netscape?
     * @return true if browser is Netscape.
     */
    public boolean isNetscape() { return _isNetscape;  }

    /**
     * Browser is not Netscape?
     * @return true if browser is not Netscape.
     */
    public boolean isNotNetscape() { return ! _isNetscape; }

    /**
     * Browser is Omniweb?
     * @return true if browser is Omniweb.
     */
    public boolean isOmniweb() { return _isOmniweb; }

    /**
     * Browser is Opera?
     * @return true if browser is Opera.
     */
    public boolean isOpera() { return _isOpera; }


    public boolean isMozilla50Compatible() { return _isMozillaVersion50; }

    public boolean isMozilla45Compatible() { return _isMozillaVersion45; }

    public boolean isMozilla40Compatible() { 
        return _isMozillaVersion40  ||  _isMozillaVersion45; 
    }

    public boolean isVersion7() { return _isVersion7; }
    public boolean isVersion6() { return _isVersion6; }
    public boolean isVersion5() { return _isVersion5; }

    // Netscape 4.5 to 4.7 is very different from 4.0 
    // NOTE: 4.6 and 4.7 is fell into this group
    public boolean isVersion45() { return _isVersion45; }

    // IE 4.1 for Mac is somewhat different from 4.0
    public boolean isVersion41() { return _isVersion41; }

    public boolean isVersion40() { return _isVersion40; }

    public boolean isVersion4() { return _isVersion4; }
    public boolean isVersion3() { return _isVersion3; }
    public boolean isVersion2() { return _isVersion2; }


    /**
     * Does the browser support IFrames?
     * @return true if the browser is IE.
     */
    public boolean isIFrameSupported() { return isIE(); }

    /**
     * Browser is not netscape or is a version 5 browser.
     * @return true if this browser can handle nested tables
     */
    public boolean willRenderNestedTablesFast() {
        return isNotNetscape()  ||  isMozilla50Compatible();
    }

    public boolean isJavaScriptOnImageButtonSupported() { 
        return isNotNetscape()  ||  isMozilla50Compatible();
    }

}
