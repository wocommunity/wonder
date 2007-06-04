//
// // ERXBasicBrowser.java
// Project ERExtensions
//
// Created by tatsuya on Tue Jul 23 2002
//
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * <code>ERXBasicBrowser</code> is a concrete subclass of {@link ERXBrowser}
 * that defines browser object. A browser object represents the web browser
 * that the current request-response cycle is dealing with. It holds the
 * information retrieved from HTTP request's <code>"user-agent"</code>
 * header, and such information includes web browser's name, version, Mozilla
 * compatible version and platform (OS). Also, a browser object can answer
 * boolean questions such as {@link #isIE},{@link #isOmniWeb},
 * {@link #isVersion5}and {@link #isMozilla40Compatible}, and even more
 * specific questions like {@link #isIFrameSupported}and
 * {@link #willRenderNestedTablesFast}.
 * <p>
 * <code>ERXBasicBrowser</code> is immutable and shared by different sessions
 * and direct actions. The shared instances are managed by
 * {@link ERXBrowserFactory}which is also responsible to parse <code>"user-agent"</code>
 * header in a {@link com.webobjects.appserver.WORequest WORequest}object and
 * to get an appropriate browser object.
 * <p>
 * You can extends <code>ERXBasicBrowser</code> or its abstract parent <code>ERXBrowser</code>
 * to implement more specific questions for your application. One potencial
 * example will be to have a question <code>isSupportedBrowser</code> that
 * checks if the client is using one of the supported browsers for your
 * application.
 * <p>
 * {@link ERXSession}holds a browser object that represent the web browser for
 * that session and {@link ERXSession#browser browser()}method returns the
 * object.
 * <p>
 * To access <code>ERXBasicBrowser</code>'s boolean questions from <code>WOConditionals</code>
 * on a web component, set the key path like <code>"session.brower.isNetscape"</code>
 * to their condition bindings.
 * <p>
 * {@link ERXDirectAction}also holds a browser object for the current request.
 * Use its {@link ERXDirectAction#browser browser()}method to access the
 * object from a session-less direct action.
 * 
 * 
 * Some browser user-agents: 
 * 
 * IE 5.17 OS 9: 
 * user-agent = (Mozilla/4.0 (compatible; MSIE 5.17; Mac_PowerPC)); ua-os = (MacOS); ua-cpu = (PPC);
 * 
 * IE 5.0 OS 9: user-agent = (Mozilla/4.0 (compatible; MSIE 5.0; Mac_PowerPC));
 * ua-os = (MacOS); ua-cpu = (PPC);
 * 
 * FireFox OS X 10.3.3: 
 * user-agent = (Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.6) Gecko/20040206 Firefox/0.8);
 * 
 * IE 5.2 MacOS X: 
 * user-agent = (Mozilla/4.0 (compatible; MSIE 5.23; Mac_PowerPC)); ua-os = (MacOS); ua-cpu = (PPC);
 * 
 * Safari: 
 * user-agent = ("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/124 (KHTML, like Gecko) Safari/125.1");
 * 
 * IE WIndows 6.02: 
 * user-agent = (Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0));
 *  
 */
public class ERXBasicBrowser extends ERXBrowser {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXBasicBrowser.class);

    private final String          _browserName;
    private final String          _version;
    private final String          _mozillaVersion;
    private final String          _platform;
    private final String          _cpu;
    private final String          _geckoRevision;
    private final NSDictionary    _userInfo;

    private final boolean         _isRobot;
    private final boolean         _isICab;
    private final boolean         _isIE;
    private final boolean         _isNetscape;
    private final boolean         _isOmniWeb;
    private final boolean         _isOpera;
    private final boolean         _isSafari;
    private final boolean         _isMozilla;
    private final boolean         _isUnknownBrowser;

    private final boolean         _isMozillaVersion50;
    private final boolean         _isMozillaVersion45;
    private final boolean         _isMozillaVersion40;

    private final boolean         _isVersion9;
    private final boolean         _isVersion8;
    private final boolean         _isVersion7;
    private final boolean         _isVersion6;
    private final boolean         _isVersion5;
    private final boolean         _isVersion51;

    private final boolean         _isVersion45;
    private final boolean         _isVersion41;
    private final boolean         _isVersion40;

    private final boolean         _isVersion4;
    private final boolean         _isVersion3;
    private final boolean         _isVersion2;

    private final boolean         _isMacOS;
    private final boolean         _isWindows;
    private final boolean         _isLinux;
    private final boolean         _isUnknownPlatform;

    public ERXBasicBrowser(String browserName, String version, String mozillaVersion, String platform, NSDictionary userInfo) {
        if (userInfo instanceof NSMutableDictionary) userInfo = new NSDictionary(userInfo);

        _userInfo = userInfo != null ? userInfo : null;

        _browserName = browserName != null ? browserName : UNKNOWN_BROWSER;
        _version = version != null ? version : UNKNOWN_VERSION;
        _mozillaVersion = mozillaVersion != null ? mozillaVersion : UNKNOWN_VERSION;
        _platform = platform != null ? platform : UNKNOWN_PLATFORM;

        String tempCpu = userInfo != null ? (String) userInfo.objectForKey("cpu") : UNKNOWN_CPU;
        _cpu = tempCpu != null ? tempCpu : UNKNOWN_CPU;

        _geckoRevision = userInfo != null ? (String) userInfo.objectForKey("geckoRevision") : null;

        _isRobot = _browserName.equals(ROBOT);
        _isICab = _browserName.equals(ICAB);
        _isIE = _browserName.equals(IE);
        _isNetscape = _browserName.equals(NETSCAPE);
        _isOmniWeb = _browserName.equals(OMNIWEB);
        _isOpera = _browserName.equals(OPERA);
        _isSafari = _browserName.equals(SAFARI);
        _isMozilla = _browserName.equals(MOZILLA);
        _isUnknownBrowser = _browserName.equals(UNKNOWN_BROWSER);

        _isMozillaVersion50 = -1 < _mozillaVersion.indexOf("5.0");

        _isMozillaVersion45 = (-1 < _mozillaVersion.indexOf("4.5")) || (-1 < _mozillaVersion.indexOf("4.6"))
                || (-1 < _mozillaVersion.indexOf("4.7"));

        _isMozillaVersion40 = -1 < _mozillaVersion.indexOf("4.0");

        _isVersion9 = -1 < _version.indexOf("9.");
        _isVersion8 = -1 < _version.indexOf("8.");
        _isVersion7 = -1 < _version.indexOf("7.");
        _isVersion6 = -1 < _version.indexOf("6.");
        _isVersion5 = -1 < _version.indexOf("5.");
        _isVersion51 = -1 < _version.indexOf("5.1");

        _isVersion45 = (-1 < _version.indexOf("4.5")) || (-1 < _version.indexOf("4.6")) || (-1 < _version.indexOf("4.7"));

        _isVersion41 = -1 < _version.indexOf("4.1");
        _isVersion40 = -1 < _version.indexOf("4.0");

        _isVersion4 = -1 < _version.indexOf("4.");
        _isVersion3 = -1 < _version.indexOf("3.");
        _isVersion2 = -1 < _version.indexOf("2.");
       
        _isMacOS = _platform.equals(MACOS);
        _isWindows = _platform.equals(WINDOWS);
        _isLinux = _platform.equals(LINUX);
        _isUnknownPlatform = _platform.equals(UNKNOWN_PLATFORM);
    }

    public String browserName() {
        return _browserName;
    }

    public String version() {
        return _version;
    }

    public String mozillaVersion() {
        return _mozillaVersion;
    }

    public String platform() {
        return _platform;
    }

    /**
     * CPU string
     * 
     * @return what processor that the browser is running on
     */
    public String cpu() {
        return _cpu;
    }

    public NSDictionary userInfo() {
        return _userInfo;
    }

    public boolean isUnknownBrowser() {
        return _isUnknownBrowser;
    }

    public boolean isRobot() {
        return _isRobot;
    }

    public boolean isICab() {
        return _isICab;
    }

    public boolean isIE() {
        return _isIE;
    }

    public boolean isNetscape() {
        return _isNetscape;
    }

    public boolean isNotNetscape() {
        return !_isNetscape;
    }

    public boolean isOmniWeb() {
        return _isOmniWeb;
    }

    public boolean isOpera() {
        return _isOpera;
    }

    public boolean isSafari() {
        return _isSafari;
    }

    public boolean isMozilla() {
        return _isMozilla;
    }

    public boolean isMozilla50Compatible() {
        return _isMozillaVersion50;
    }

    public boolean isMozilla45Compatible() {
        return _isMozillaVersion45;
    }

    public boolean isMozilla40Compatible() {
        return _isMozillaVersion40 || _isMozillaVersion45;
    }

    public boolean isVersion9() {
        return _isVersion8;
    }

    public boolean isVersion8() {
        return _isVersion8;
    }

    public boolean isVersion7() {
        return _isVersion7;
    }

    public boolean isVersion6() {
        return _isVersion6;
    }

    public boolean isVersion5() {
        return _isVersion5;
    }

    public boolean isVersion51() {
        return _isVersion51;
    }

    // Netscape 4.5 to 4.7 is very different from 4.0
    // NOTE: 4.6 and 4.7 is fell into this group
    public boolean isVersion45() {
        return _isVersion45;
    }

    // IE 4.1 for Mac is somewhat different from 4.0
    public boolean isVersion41() {
        return _isVersion41;
    }

    public boolean isVersion40() {
        return _isVersion40;
    }

    public boolean isVersion4() {
        return _isVersion4;
    }

    public boolean isVersion3() {
        return _isVersion3;
    }

    public boolean isVersion2() {
        return _isVersion2;
    }

    public boolean isUnknownPlatform() {
        return _isUnknownPlatform;
    }

    public boolean isMacOS() {
        return _isMacOS;
    }

    public boolean isWindows() {
        return _isWindows;
    }

    public boolean isLinux() {
        return _isLinux;
    }

    /**
     * Returns the gecko revision of the browser or {@link ERXBrowser#NO_GECKO}.
     * 
     * @return the gecko revision of the browser or {@link ERXBrowser#NO_GECKO}.
     */
    public String geckoRevision() {
        return _geckoRevision;
    }

    /**
     * Does the browser support IFrames?
     * 
     * @return true if the browser is IE.
     */
    public boolean isIFrameSupported() {
        return isIE();
    }

    /**
     * Browser is not netscape or is a version 5 browser.
     * 
     * @return true if this browser can handle nested tables
     */
    public boolean willRenderNestedTablesFast() {
        return isNotNetscape() || isMozilla50Compatible();
    }

    public boolean isJavaScriptOnImageButtonSupported() {
        return isNotNetscape() || isMozilla50Compatible();
    }

}