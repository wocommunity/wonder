//
// ERXBrowser.java
// Project ERExtensions
//
// Created by tatsuya on Mon Jul 22 2002
//
package er.extensions;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;

/** 
 * ERXBrowser is an abstract class that defines browser object. 
 * A browser object represents the web browser that the current 
 * request-response loop is dealing with. It holds the information 
 * retrieved from HTTP request's "user-agent" header, and such 
 * information includes web browser's name, version, Mozilla 
 * compatible version and platform (OS). Also, a browser object 
 * can answer boolean questions such as <code>isIE</code>, 
 * <code>isOmniWeb</code>, <code>isVersion5</code> and 
 * <code>isMozilla40Compatible</code>. <br>
 * 
 * ERXBrowser is immutable and shared by different sessions and
 * direct actions. 
 * The shared instances are managed by ERXBrowserFactory which 
 * is also responsible to parse "user-agent" header in a WORequest 
 * object and to get an appropriate browser object. <br>
 * 
 * One concrete browser, ERXBasicBrowser, is defined in the 
 * ERExtensions framework. It not only implements the basic 
 * questions defined by ERXBrowser, but also more specific 
 * questions like <code>isIFrameSupported</code> and 
 * <code>willRenderNestedTablesFast</code>. <br>
 * 
 * You can extend ERXBrowser or its concrete subclass 
 * ERXBasicBrowser to implement more specific questions for 
 * your application. One good example will be to have a question 
 * <code>isSupportedBrowser</code> that checks if the client 
 * is using one of the supported browsers for your application. <br>
 * 
 * ERXSession holds a browser object that represent the web 
 * browser for that session and <code>browser</code> method 
 * returns the object. 
 * 
 * To access ERXBrowser's boolean questions from WOConditionals 
 * on a web component, set the key path like "session.brower.isIFrameSupported" 
 * to their condition bindings. <br>
 * 
 * ERXDirectAction also holds a browser object for the current request. 
 * Use its <code>browser</code> method to access the object from a 
 * session-less direct action. <br>
 */  
public abstract class ERXBrowser implements NSKeyValueCoding {

    public static final String UNKNOWN_BROWSER = "Unknown Broswer";

    public static final String ROBOT 	= "robot";
    public static final String ICAB 	= "iCab";
    public static final String IE	= "IE";
    public static final String NETSCAPE = "Netscape";
    public static final String OMNIWEB 	= "OmniWeb";
    public static final String OPERA 	= "Opera";
    public static final String SAFARI	= "Safari";
    public static final String MOZILLA	= "Mozilla";

    public static final String UNKNOWN_VERSION = "Unknown Version";

    public static final String UNKNOWN_PLATFORM = "Unknown Platform";

    public static final String MACOS	= "MacOS";
    public static final String WINDOWS 	= "Windows";
    public static final String LINUX	= "Linux";

    public static final String POWER_PC	= "PowerPC";
    public static final String UNKNOWN_CPU = "Unknown CPU";
    public static final String NO_GECKO = "No Gecko";

    /**
     * Browser name string
     * @return what type of browser
     */
    public abstract String browserName();

    /**
     * Version string
     * @return what version of browser
     */
    public abstract String version();

    /**
     * MozillaVersion string
     * @return what Mozilla version equivement to the browser's version
     */
    public abstract String mozillaVersion();

    /**
     * The revision of the gecko rendering engine. 1.0.2 and up support xslt.
     * @return what gecko revision equivement to the browser's version
     */
    public abstract String geckoRevision();

    /**
     * Platform string
     * @return what platform that the browser is running on
     */
    public abstract String platform();

    /**
     * UserInfo dictionary
     * @return what type of browser
     */
    public abstract NSDictionary userInfo();

    public abstract boolean isUnknownBrowser();

    /**
     * Browser is isRobot?
     * @return true if browser is robot.
     */
    public abstract boolean isRobot();

    /**
     * Browser is iCab?
     * @return true if browser is iCab.
     */
    public abstract boolean isICab();

    /**
     * Browser is Ineternet Explorer?
     * @return true if browser is IE.
     */
    public abstract boolean isIE();

    /**
     * Browser is Netscape?
     * @return true if browser is Netscape.
     */
    public abstract boolean isNetscape();

    /**
     * Browser is not Netscape?
     * @return true if browser is not Netscape.
     */
    public abstract boolean isNotNetscape();

    /**
     * Browser is OmniWeb?
     * @return true if browser is OmniWeb.
     */
    public abstract boolean isOmniWeb();

    /**
     * Browser is Opera?
     * @return true if browser is Opera.
     */
    public abstract boolean isOpera();

    /**
     * Browser is Safari?
     * @return true if browser is Safari.
     */
    public abstract boolean isSafari();

    public abstract boolean isMozilla50Compatible();
    public abstract boolean isMozilla45Compatible();
    public abstract boolean isMozilla40Compatible();

    public abstract boolean isVersion7();
    public abstract boolean isVersion6();
    public abstract boolean isVersion5();
    public abstract boolean isVersion51();  // IE 5.0 and IE 5.1 on Mac OS is different

    public abstract boolean isVersion45();  // Netscape 4.5 to 4.7 is very different from 4.0 
                                            // NOTE: 4.6 and 4.7 is fell into this group

    public abstract boolean isVersion41();  // IE 4.1 for Mac is somewhat different from 4.0
    public abstract boolean isVersion40();

    public abstract boolean isVersion4();
    public abstract boolean isVersion3();
    public abstract boolean isVersion2();

    public abstract boolean isUnknownPlatform();
    public abstract boolean isMacOS();
    public abstract boolean isWindows();
    public abstract boolean isLinux();

    /**
     * Gets the message encoding for a given request. Default implementation
     * gets the message encoding for all of the browserLanguages off of
     * the request.
     * @param request to get the message encoding for
     * @return message encoding
     */
    public ERXMessageEncoding messageEncodingForRequest(WORequest request) {
        return messageEncodingForLanguages(request.browserLanguages());
    }

    /**
     * Gets the message encoding for a given array of languages.
     * @param languages array to get the correct encoding for
     * @return message encoding
     */    
    public ERXMessageEncoding messageEncodingForLanguages(NSArray languages) {
        return new ERXMessageEncoding(languages);
    }
    
    /**
     * Gets the message encoding for a given language.
     * @param language to get the encoding for
     * @return message encoding
     */    
    public ERXMessageEncoding messageEncodingForLanguage(String language) {
        return new ERXMessageEncoding(language);
    }    

    /**
     * If using ERXRequest objects allows one to override on a per browser basis
     * what form value encoding to use. Default implementation defaults to null
     * Note that you will need to enable the property:
     * er.extensions.ERXRequest.BrowserFormValueEncodingOverrideEnabled=true
     * in order for the actual over ride to happen.
     * @return form value encoding to use for this particular user-agent.
     */
    public String formValueEncoding() {
        return null;
    }
    
    public Object valueForKey(String key) {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }
    
    public void takeValueForKey(Object value, String key) {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
    }

    private String _toString;
    public String toString() {
        if (_toString == null) {
            _toString = "<" + getClass().getName() 
                        + " browserName: " + browserName()
                        + ", version: " + version()
                        + ", mozillaVersion: " + mozillaVersion()
                        + ", platform: " + platform()
                        + ">";
        }
        return _toString;
    }

}
