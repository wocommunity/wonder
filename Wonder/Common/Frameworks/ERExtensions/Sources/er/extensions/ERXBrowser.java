//
// ERXBrowser.java
// Project ERExtensions
//
// Created by tatsuya on Mon Jul 22 2002
//
package er.extensions;

import com.webobjects.foundation.*;

/** 
 * Provides fundermental API for browser object. 
 */  
public abstract class ERXBrowser implements NSKeyValueCoding {

    public static final String UNKNOWN_BROWSER = "Unknown Broswer";

    public static final String ICAB 	= "iCab";
    public static final String IE	= "IE";
    public static final String NETSCAPE = "Netscape";
    public static final String OMNIWEB 	= "OmniWeb";
    public static final String OPERA 	= "Opera";

    public static final String UNKNOWN_VERSION = "Unknown Version";

    public static final String UNKNOWN_PLATFORM = "Unknown Platform";

    public static final String MACOS	= "MacOS";
    public static final String WINDOWS 	= "Windows";
    public static final String LINUX	= "Linux";

    public static final String POWER_PC	= "PowerPC";
    public static final String UNKNOWN_CPU = "Unknown CPU";

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
     * Platform string
     * @return what platform that the browser is running on
     */
    public abstract String platform();

    /**
     * UserInfo dictionary
     * @return what type of browser
     */
    public abstract NSDictionary userInfo();

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
     * Browser is Omniweb?
     * @return true if browser is Omniweb.
     */
    public abstract boolean isOmniweb();

    /**
     * Browser is Opera?
     * @return true if browser is Opera.
     */
    public abstract boolean isOpera();

    public abstract boolean isMozilla50Compatible();
    public abstract boolean isMozilla45Compatible();
    public abstract boolean isMozilla40Compatible();

    public abstract boolean isVersion7();
    public abstract boolean isVersion6();
    public abstract boolean isVersion5();

    public abstract boolean isVersion45();  // Netscape 4.5 to 4.7 is very different from 4.0 
                                            // NOTE: 4.6 and 4.7 is fell into this group

    public abstract boolean isVersion41();  // IE 4.1 for Mac is somewhat different from 4.0
    public abstract boolean isVersion40();

    public abstract boolean isVersion4();
    public abstract boolean isVersion3();
    public abstract boolean isVersion2();

    public Object valueForKey(String key) {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }
    
    public void takeValueForKey(Object value, String key) {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
    }

}
