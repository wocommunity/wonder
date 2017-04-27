//
// ERXBasicBrowser.java
// Project ERExtensions
//
// Created by tatsuya on Tue Jul 23 2002
//
package er.extensions.appserver;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXStringUtilities;

/**
 * <div class="en">
 * <code>ERXBasicBrowser</code> is a concrete subclass of {@link ERXBrowser}
 * that defines browser object. A browser object represents the web browser
 * that the current request-response cycle is dealing with. It holds the
 * information retrieved from HTTP request's <code>"user-agent"</code>
 * header, and such information includes web browser's name, version, Mozilla
 * compatible version and platform (OS). Also, a browser object can answer
 * boolean questions such as {@link #isIE}, {@link #isOmniWeb},
 * {@link #isVersion5} and {@link #isMozilla40Compatible}, and even more
 * specific questions like {@link #isIFrameSupported} and
 * {@link #willRenderNestedTablesFast}.
 * <p>
 * <code>ERXBasicBrowser</code> is immutable and shared by different sessions
 * and direct actions. The shared instances are managed by
 * {@link ERXBrowserFactory} which is also responsible to parse <code>"user-agent"</code>
 * header in a {@link com.webobjects.appserver.WORequest WORequest} object and
 * to get an appropriate browser object.
 * <p>
 * You can extends <code>ERXBasicBrowser</code> or its abstract parent <code>ERXBrowser</code>
 * to implement more specific questions for your application. One potential
 * example will be to have a question <code>isSupportedBrowser</code> that
 * checks if the client is using one of the supported browsers for your
 * application.
 * <p>
 * {@link ERXSession} holds a browser object that represent the web browser for
 * that session and {@link ERXSession#browser() browser()} method returns the
 * object.
 * <p>
 * To access <code>ERXBasicBrowser</code>'s boolean questions from <code>WOConditionals</code>
 * on a web component, set the key path like <code>"session.brower.isNetscape"</code>
 * to their condition bindings.
 * <p>
 * {@link ERXDirectAction} also holds a browser object for the current request.
 * Use its {@link ERXDirectAction#browser() browser()} method to access the
 * object from a session-less direct action.
 * 
 * 
 * <h3>Some browser user-agents</h3>
 * 
 * <p><strong>IE 5.17 OS 9</strong><br>
 * user-agent = (Mozilla/4.0 (compatible; MSIE 5.17; Mac_PowerPC)); ua-os = (MacOS); ua-cpu = (PPC);
 * 
 * IE 5.0 OS 9: user-agent = (Mozilla/4.0 (compatible; MSIE 5.0; Mac_PowerPC));
 * ua-os = (MacOS); ua-cpu = (PPC);
 * 
 * <p><strong>FireFox OS X 10.3.3</strong><br>
 * user-agent = (Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.6) Gecko/20040206 Firefox/0.8);
 * 
 * <p><strong>IE 5.2 MacOS X</strong><br>
 * user-agent = (Mozilla/4.0 (compatible; MSIE 5.23; Mac_PowerPC)); ua-os = (MacOS); ua-cpu = (PPC);
 * 
 * <p><strong>Safari</strong><br>
 * user-agent = ("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/124 (KHTML, like Gecko) Safari/125.1");
 * 
 * <p><strong>IE WIndows 6.02</strong><br>
 * user-agent = (Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0));
 * </div>
 *  
 * <div class="ja">
 * <code>ERXBasicBrowser</code> はブラウザ・オブジェクトを定義する {@link ERXBrowser} の明確なサブクラスです。
 * ブラウザ・オブジェクトはカレント・リクエスト・レスポンス・ループの Webブラウザを表します。
 * HTTP リクエスト・ヘッダー <code>"user-agent"</code> の情報を保持し、さらに
 * Webブラウザ名、プラットフォームや Mozilla互換性の情報を含みます。
 * ブラウザ・オブジェクトは基本なメソッド <code>isIE</code> や <code>isVersion5</code> のみではなく、
 * もっと確実な <code>isIFrameSupported</code>と <code>willRenderNestedTablesFast</code> を boolean で
 * 回答します。<br>
 * 
 * ERXBasicBrowser は不変で、他のセッションとダイレクト・アクションで共有されています。
 * 共有インスタンスは ERXBrowserFactory で管理されています。他には ERXBrowserFactory が
 * WORequest の "user-agent" パースとブラウザ・オブジェクトの作成を担当しています。<br>
 * 
 * 自分のアプリケーションの為に ERXBrowser や ERXBasicBrowser のサブクラスをつくることができます。
 * 例：アプリケーションでサポートされているブラウザかどうかの <code>isSupportedBrowser</code> を追加できます。<br>
 * 
 * ERXSession はブラウザ・オブジェクトを保持し、セッションにアクセスしている Web Browser の情報を持っている。
 * <code>browser</code> メソッドでオブジェクトを取得できます。<br>
 * 
 * コンポーネント内の WOConditionals より ERXBasicBrowser の boolean を問い合わせにアクセスする時、
 * 次のようなキーパス "session.brower.isNetscape" をバインディングします。<br>
 * 
 * ERXDirectAction もカレント・リクエストのブラウザ・オブジェクトを保持します。
 * オブジェクトをアクセスするには <code>browser</code> メソッドを使用します。<br>
 * 
 * <h3>ブラウザの user-agents 一部: </h3>
 * 
 * <p><strong>IE 5.17 OS 9: </strong><br>
 * user-agent = (Mozilla/4.0 (compatible; MSIE 5.17; Mac_PowerPC)); ua-os = (MacOS); ua-cpu = (PPC);
 * 
 * IE 5.0 OS 9: user-agent = (Mozilla/4.0 (compatible; MSIE 5.0; Mac_PowerPC));
 * ua-os = (MacOS); ua-cpu = (PPC);
 * 
 * <p><strong>FireFox OS X 10.3.3: </strong><br>
 * user-agent = (Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.6) Gecko/20040206 Firefox/0.8);
 * 
 * <p><strong>IE 5.2 MacOS X: </strong><br>
 * user-agent = (Mozilla/4.0 (compatible; MSIE 5.23; Mac_PowerPC)); ua-os = (MacOS); ua-cpu = (PPC);
 * 
 * <p><strong>Safari: </strong><br>
 * user-agent = ("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/124 (KHTML, like Gecko) Safari/125.1");
 * 
 * <p><strong>IE WIndows 6.02: </strong><br>
 * user-agent = (Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0));
 * </div>
 */
public class ERXBasicBrowser extends ERXBrowser {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXBasicBrowser.class);

    private final String          _browserName;
    private final String          _version;
    private final Integer         _majorVersion;
    private final String          _mozillaVersion;
    private final String          _platform;
    private final String          _cpu;
    private final String          _geckoRevision;
    private final NSDictionary    _userInfo;

    private final boolean         _isRobot;
    private final boolean         _isICab;
    private final boolean         _isEdge;
    private final boolean         _isIE;
    private final boolean         _isNetscape;
    private final boolean         _isOmniWeb;
    private final boolean         _isOpera;
    private final boolean         _isSafari;
    private final boolean         _isFirefox;
    private final boolean         _isMozilla;
    private final boolean         _isChrome;
    private final boolean         _isUnknownBrowser;

    private final boolean         _isMozillaVersion50;
    private final boolean         _isMozillaVersion45;
    private final boolean         _isMozillaVersion40;

    private final boolean         _isVersion13;
    private final boolean         _isVersion12;
    private final boolean         _isVersion11;
    private final boolean         _isVersion10;
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
    private final boolean         _isIPhone;
    private final boolean         _isIPad;
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
        _isEdge = _browserName.equals(EDGE);
        _isNetscape = _browserName.equals(NETSCAPE);
        _isOmniWeb = _browserName.equals(OMNIWEB);
        _isOpera = _browserName.equals(OPERA);
        _isSafari = _browserName.equals(SAFARI);
        _isFirefox = _browserName.equals(FIREFOX);
        _isMozilla = (_browserName.equals(MOZILLA) || _browserName.equals(FIREFOX));
        _isChrome = _browserName.equals(CHROME);
        _isUnknownBrowser = _browserName.equals(UNKNOWN_BROWSER);

        _isMozillaVersion50 = -1 < _mozillaVersion.indexOf("5.0");

        _isMozillaVersion45 = (-1 < _mozillaVersion.indexOf("4.5")) || (-1 < _mozillaVersion.indexOf("4.6"))
                || (-1 < _mozillaVersion.indexOf("4.7"));

        _isMozillaVersion40 = -1 < _mozillaVersion.indexOf("4.0");

        
        String normalizedVersion = ERXStringUtilities.removeExtraDotsFromVersionString(_version);
        _isVersion13 = normalizedVersion.startsWith("13.");
        _isVersion12 = normalizedVersion.startsWith("12.");
        _isVersion11 = normalizedVersion.startsWith("11.");
        _isVersion10 = normalizedVersion.startsWith("10.");
        _isVersion9 = normalizedVersion.startsWith("9.");
        _isVersion8 = normalizedVersion.startsWith("8.");
        _isVersion7 = normalizedVersion.startsWith("7.");
        _isVersion6 = normalizedVersion.startsWith("6.");
        _isVersion5 = normalizedVersion.startsWith("5.");
        _isVersion51 = normalizedVersion.startsWith("5.1");

        _isVersion45 = normalizedVersion.startsWith("4.5") || normalizedVersion.startsWith("4.6") || normalizedVersion.startsWith("4.7");

        _isVersion41 = normalizedVersion.startsWith("4.1");
        _isVersion40 = normalizedVersion.startsWith("4.0");

        _isVersion4 = normalizedVersion.startsWith("4.");
        _isVersion3 = normalizedVersion.startsWith("3.");
        _isVersion2 = normalizedVersion.startsWith("2.");
       
        _isMacOS = _platform.equals(MACOS);
        _isWindows = _platform.equals(WINDOWS);
        _isLinux = _platform.equals(LINUX);
        _isIPhone = _platform.equals(IPHONE);
        _isIPad = _platform.equals(IPAD);
        _isUnknownPlatform = _platform.equals(UNKNOWN_PLATFORM);
        
        if (_version.equals(UNKNOWN_VERSION)) {
        	_majorVersion = Integer.valueOf(0);
        } else {
	        String majorVersion = normalizedVersion;
	        if (majorVersion.indexOf(".") != -1) {
	        	majorVersion = majorVersion.substring(0, majorVersion.indexOf("."));
	        }
	        Integer mj = -1;
	        try {
	        	mj = Integer.valueOf(majorVersion);
	        } catch (NumberFormatException e) {
	        	log.info("could not determine major version from '" + majorVersion + "'", e);
			}
			_majorVersion = mj;
        }
    }

    @Override
    public String browserName() {
        return _browserName;
    }

    @Override
    public String version() {
        return _version;
    }
    
    @Override
    public Integer majorVersion() {
    	return _majorVersion;
    }

    @Override
    public String mozillaVersion() {
        return _mozillaVersion;
    }

    @Override
    public String platform() {
        return _platform;
    }

    /**
     * <div class="en">
     * CPU string
     * </div>
     * 
     * <div class="ja">
     * ブラウザが動作している CPU を戻します
     * </div>
     * 
     * @return <div class="en">what processor that the browser is running on</div>
     *         <div class="ja">ブラウザが動作している CPU</div>
     */
    public String cpu() {
        return _cpu;
    }

    @Override
    public NSDictionary userInfo() {
        return _userInfo;
    }

    @Override
    public boolean isUnknownBrowser() {
        return _isUnknownBrowser;
    }

    @Override
    public boolean isRobot() {
        return _isRobot;
    }

    @Override
    public boolean isICab() {
        return _isICab;
    }

    @Override
    public boolean isEdge() {
        return _isEdge;
    }

    @Override
    public boolean isIE() {
        return _isIE;
    }

    @Override
    public boolean isNetscape() {
        return _isNetscape;
    }

    @Override
    public boolean isNotNetscape() {
        return !_isNetscape;
    }

    @Override
    public boolean isOmniWeb() {
        return _isOmniWeb;
    }

    @Override
    public boolean isOpera() {
        return _isOpera;
    }

    @Override
    public boolean isSafari() {
        return _isSafari;
    }

    @Override
    public boolean isFirefox() {
        return _isFirefox;
    }

    @Override
    public boolean isChrome() {
        return _isChrome;
    }

    /**
     * <div class="ja">
     * Mozilla ですか？
     * 
     * @return Mozilla の場合には true を戻します
     * </div>
     */
    public boolean isMozilla() {
        return _isMozilla;
    }

    @Override
    public boolean isMozilla50Compatible() {
        return _isMozillaVersion50;
    }

    @Override
    public boolean isMozilla45Compatible() {
        return _isMozillaVersion45;
    }

    @Override
    public boolean isMozilla40Compatible() {
        return _isMozillaVersion40 || _isMozillaVersion45;
    }

    @Override
    public boolean isVersion9() {
        return _isVersion9;
    }

    @Override
    public boolean isVersion8() {
        return _isVersion8;
    }

    @Override
    public boolean isVersion7() {
        return _isVersion7;
    }

    @Override
    public boolean isVersion6() {
        return _isVersion6;
    }

    @Override
    public boolean isVersion5() {
        return _isVersion5;
    }

    @Override
    public boolean isVersion51() {
        return _isVersion51;
    }

    // Netscape 4.5 to 4.7 is very different from 4.0
    // NOTE: 4.6 and 4.7 fell into this group
    @Override
    public boolean isVersion45() {
        return _isVersion45;
    }

    // IE 4.1 for Mac is somewhat different from 4.0
    @Override
    public boolean isVersion41() {
        return _isVersion41;
    }

    @Override
    public boolean isVersion40() {
        return _isVersion40;
    }

    @Override
    public boolean isVersion4() {
        return _isVersion4;
    }

    @Override
    public boolean isVersion3() {
        return _isVersion3;
    }

    @Override
    public boolean isVersion2() {
        return _isVersion2;
    }

    @Override
    public boolean isUnknownPlatform() {
        return _isUnknownPlatform;
    }

    @Override
    public boolean isMacOS() {
        return _isMacOS;
    }

    @Override
    public boolean isWindows() {
        return _isWindows;
    }

    @Override
    public boolean isLinux() {
        return _isLinux;
    }

    @Override
    public boolean isIPhone() {
        return _isIPhone;
    }
    
    @Override
    public boolean isIPad() {
		return _isIPad;
	}

    /**
     * Returns the gecko revision of the browser or {@link ERXBrowser#NO_GECKO}.
     * 
     * @return the gecko revision of the browser or {@link ERXBrowser#NO_GECKO}.
     */
    @Override
    public String geckoRevision() {
        return _geckoRevision;
    }

    /**
     * <div class="en">
     * Does the browser support IFrames?
     * </div>
     * 
     * <div class="ja">
     * ブラウザが iFrames をサポートしていますか？
     * </div>
     * 
     * @return <div class="en">true if the browser is IE.</div>
     *         <div class="ja">iFrames サポートの場合には true を戻します</div>
     */
    public boolean isIFrameSupported() {
        return isIE();
    }

    /**
     * <div class="en">
     * Browser is not netscape or is a version 5 browser.
     * </div>
     * 
     * <div class="ja">
     * ネストされているテーブルを高速でレンダリング可能？
     * Browser is not netscape or is a version 5 browser.
     * </div>
     * 
     * @return <div class="en">true if this browser can handle nested tables</div>
     *         <div class="ja">ネストされているテーブルを高速でレンダリング可能の場合には true を戻します</div>
     */
    public boolean willRenderNestedTablesFast() {
        return isNotNetscape() || isMozilla50Compatible();
    }

    /**
     * <div class="ja">
     * Javascript OnImage ボタンがサポートされていますか？
     * 
     * @return Javascript OnImage ボタンがサポートされている場合には true を戻します
     * </div>
     */
    public boolean isJavaScriptOnImageButtonSupported() {
        return isNotNetscape() || isMozilla50Compatible();
    }

}