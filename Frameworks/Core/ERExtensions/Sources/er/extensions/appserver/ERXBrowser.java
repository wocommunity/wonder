//
// ERXBrowser.java
// Project ERExtensions
//
// Created by tatsuya on Mon Jul 22 2002
//
package er.extensions.appserver;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;

/** 
 * <span class="en">
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
 * </span>
 * 
 * <span class="ja">
 * ERXBrowser はブラウザ・オブジェクトを表現する抽象的なクラスです。
 * ブラウザ・オブジェクトはカレント・リクエスト・レスポンス・ループの Webブラウザを表現しています。
 * HTTPリクエスト "user-agent" より情報を取得し、 Webブラウザ名、バージョン番号、プラットフォームと
 * Mozilla バージョン番号等々を含みます。他には boolean で <code>isIE</code>,
 * <code>isOmniWeb</code>, <code>isVersion5</code> と 
 * <code>isMozilla40Compatible</code>の情報を簡単にアクセスできます。 <br>
 * 
 * ERXBrowser は不変で、他のセッションとダイレクト・アクションで共有されています。
 * 共有インスタンスは ERXBrowserFactory で管理されています。他には ERXBrowserFactory が
 * WORequest の "user-agent" パースとブラウザ・オブジェクトの作成を担当しています。<br>
 * 
 * ERExtensions フレームワークには ERXBasicBrowser ブラウザ・オブジェクトが明確にされている。
 * ERXBrowser の基本な調査メソッドのみではなく、もっと確実な <code>isIFrameSupported</code>
 * と <code>willRenderNestedTablesFast</code> を回答します。<br>
 * 
 * 自分のアプリケーションの為に ERXBrowser や ERXBasicBrowser のサブクラスをつくることができます。
 * 例：アプリケーションでサポートされているブラウザかどうかの <code>isSupportedBrowser</code> を追加できます。<br>
 * 
 * ERXSession はブラウザ・オブジェクトを保持し、セッションにアクセスしている Web Browser の情報を持っている。
 * <code>browser</code> メソッドでオブジェクトを取得できます。<br>
 * 
 * コンポーネント内の WOConditionals より ERXBrowser の boolean を問い合わせにアクセスする時、
 * 次のようなキーパス "session.brower.isIFrameSupported" をバインディングします。<br>
 * 
 * ERXDirectAction もカレント・リクエストのブラウザ・オブジェクトを保持します。
 * オブジェクトをアクセスするには <code>browser</code> メソッドを使用します。<br>
 * </span>
 */  
public abstract class ERXBrowser implements NSKeyValueCoding {

    public static final String UNKNOWN_BROWSER = "Unknown Browser";

    public static final String ROBOT 	= "robot";
    public static final String ICAB 	= "iCab";
    public static final String IE		= "IE";
    public static final String NETSCAPE = "Netscape";
    public static final String OMNIWEB 	= "OmniWeb";
    public static final String OPERA 	= "Opera";
    public static final String SAFARI	= "Safari";
    public static final String MOZILLA	= "Mozilla";
    public static final String CHROME	= "Chrome";
    public static final String FIREFOX	= "Firefox";

    public static final String UNKNOWN_VERSION = "Unknown Version";

    public static final String UNKNOWN_PLATFORM = "Unknown Platform";

    public static final String MACOS	= "MacOS";
    public static final String WINDOWS 	= "Windows";
    public static final String LINUX	= "Linux";
    public static final String IPHONE	= "iPhone";
    public static final String IPAD     = "iPad";

    public static final String POWER_PC	= "PowerPC";
    public static final String UNKNOWN_CPU = "Unknown CPU";
    public static final String NO_GECKO = "No Gecko";

    /**
     * <span class="en">
     * Browser name string
     * 
     * @return what type of browser
     * </span>
     * 
     * <span class="ja">
     * ブラウザ名を戻します
     * 
     * @return ブラウザ名
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract String browserName();

    /**
     * <span class="en">
     * Version string
     * 
     * @return what version of browser
     * </span>
     * 
     * <span class="ja">
     * ブラウザのバージョンアップを戻します
     * 
     * @return ブラウザのバージョンアップ
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract String version();
    
    /**
     * Major version
     * 
     * @return what major version of browser
     */
    public abstract Integer majorVersion();

    /**
     * <span class="en">
     * MozillaVersion string
     * 
     * @return what Mozilla version equivement to the browser's version
     * </span>
     * 
     * <span class="ja">
     * ブラウザの Mozilla バージョンを戻します
     * 
     * @return ブラウザの Mozilla バージョン
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract String mozillaVersion();

    /**
     * <span class="en">
     * The revision of the gecko rendering engine. 1.0.2 and up support xslt.
     * 
     * @return what gecko revision equivement to the browser's version
     * </span>
     * 
     * <span class="ja">
     * gecko レンダリング・エンジンのバージョンを戻します
     * 
     * @return gecko レンダリング・エンジンのバージョンを戻します
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract String geckoRevision();

    /**
     * <span class="en">
     * Platform string
     * 
     * @return what platform that the browser is running on
     * </span>
     * 
     * <span class="ja">
     * プラットフォームを戻します
     * 
     * @return プラットフォーム
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract String platform();

    /**
     * <span class="en">
     * UserInfo dictionary
     * 
     * @return what type of browser
     * </span>
     * 
     * <span class="ja">
     * UserInfo ディクショナリーを戻します
     * 
     * @return UserInfo ディクショナリー
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract NSDictionary userInfo();

    /**
     * <span class="ja">
     * 未知のブラウザ？
     * 
     * @return 未知のブラウザの場合は true が戻ります
     * </span>
     */
    public abstract boolean isUnknownBrowser();

    /**
     * <span class="en">
     * Browser is isRobot?
     * 
     * @return true if browser is robot.
     * </span>
     * 
     * <span class="ja">
     * ブラウザはロボットですか？
     * 
     * @return ロボットの場合には true が戻ります
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract boolean isRobot();

    /**
     * <span class="en">
     * Browser is iCab?
     * 
     * @return true if browser is iCab.
     * </span>
     
     * <span class="ja">
     * ブラウザは iCab ですか？
     * 
     * @return iCab の場合には true が戻ります
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract boolean isICab();

    /**
     * <span class="en">
     * Browser is Internet Explorer?
     * 
     * @return true if browser is IE.
     * </span>
     * 
     * <span class="ja">
     * ブラウザは Internet Explorer ですか？
     * 
     * @return IE の場合には true が戻ります
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract boolean isIE();

    /**
     * <span class="en">
     * Browser is Netscape?
     * 
     * @return true if browser is Netscape.
     * </span>
     * 
     * <span class="ja">
     * ブラウザは Netscape ですか？
     * 
     * @return Netscape の場合には true が戻ります
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract boolean isNetscape();

    /**
     * <span class="en">
     * Browser is not Netscape?
     * 
     * @return true if browser is not Netscape.
     * </span>
     * 
     * <span class="ja">
     * ブラウザは Netscape ではないか？
     * 
     * @return Netscape でない場合には true が戻ります
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract boolean isNotNetscape();

    /**
     * <span class="en">
     * Browser is OmniWeb?
     * 
     * @return true if browser is OmniWeb.
     * </span>
     * 
     * <span class="ja">
     * ブラウザは OmniWeb ですか？
     * 
     * @return OmniWeb の場合には true が戻ります
     * </span>
     */
     @SuppressWarnings("javadoc")
   public abstract boolean isOmniWeb();

    /**
     * <span class="en">
     * Browser is Opera?
     * 
     * @return true if browser is Opera.
     * </span>
     * 
     * <span class="ja">
     * ブラウザは Opera ですか？
     * 
     * @return Opera の場合には true が戻ります
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract boolean isOpera();

    /**
     * <span class="en">
     * Browser is Safari?
     * 
     * @return true if browser is Safari.
     * </span>
     * 
     * <span class="ja">
     * ブラウザは Safari ですか？
     * 
     * @return Safari の場合には true が戻ります
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract boolean isSafari();
    
    /**
     * <span class="en">
     * Browser is Firefox?
     * 
     * @return true if browser is Firefox.
     * </span>
     * 
     * <span class="ja">
     * ブラウザは Firefox ですか？
     * 
     * @return Firefox の場合には true が戻ります
     * </span>
     */
     @SuppressWarnings("javadoc")
   public abstract boolean isFirefox();
    
    /**
     * <span class="en">
     * Browser is Chrome?
     * 
     * @return true if browser is Chrome.
     * </span>
     * 
     * <span class="ja">
     * ブラウザは Chrome ですか？
     * 
     * @return Chrome の場合には true が戻ります
     * </span>
     */
    @SuppressWarnings("javadoc")
    public abstract boolean isChrome();

    public abstract boolean isMozilla50Compatible();
    public abstract boolean isMozilla45Compatible();
    public abstract boolean isMozilla40Compatible();

    public abstract boolean isVersion9();
    public abstract boolean isVersion8();
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
    public abstract boolean isIPhone();
    public abstract boolean isIPad();
    
    /**
     * <span class="en">
     * Gets the message encoding for a given request. Default implementation
     * gets the message encoding for all of the browserLanguages off of
     * the request.
     * @param request - to get the message encoding for
     
     * @return message encoding
     * </span>
     * 
     * <span class="ja">
     * 指定のリクエストのメッセージ・エンコーディング方法を戻します。
     * デフォルト実装ではリクエストの全てのブラウザ言語のエンコーディング方法を取得します。
     * 
     * @param request - リクエスト WORequest
     * 
     * @return メッセージ・エンコーディング方法
     * </span>
     */
    @SuppressWarnings("javadoc")
    public ERXMessageEncoding messageEncodingForRequest(WORequest request) {
        return messageEncodingForLanguages(request.browserLanguages());
    }

    /**
     * <span class="en">
     * Gets the message encoding for a given array of languages.
     
     * @param languages - array to get the correct encoding for
     
     * @return message encoding
     * </span>
     * 
     * <span class="ja">
     * 指定の言語配列のメッセージ・エンコーディング方法を戻します。
     * 
     * @param languages - メッセージ・エンコーディング方法を取得したい言語配列
     * 
     * @return メッセージ・エンコーディング方法
     * </span>
     */    
    @SuppressWarnings("javadoc")
    public ERXMessageEncoding messageEncodingForLanguages(NSArray languages) {
        return new ERXMessageEncoding(languages);
    }
    
    /**
     * <span class="en">
     * Gets the message encoding for a given language.
     * 
     * @param language - to get the encoding for
     * 
     * @return message encoding
     * </span>
     * 
     * <span class="en">
     * 指定の言語のメッセージ・エンコーディング方法を戻します。
     * 
     * @param language - メッセージ・エンコーディング方法を取得したい言語
     * 
     * @return メッセージ・エンコーディング方法
     * </span>
     */    
    @SuppressWarnings("javadoc")
    public ERXMessageEncoding messageEncodingForLanguage(String language) {
        return new ERXMessageEncoding(language);
    }    

    /**
     * <span class="en">
     * If using ERXRequest objects allows one to override on a per browser basis
     * what form value encoding to use. Default implementation defaults to null
     * Note that you will need to enable the property:
     * er.extensions.ERXRequest.BrowserFormValueEncodingOverrideEnabled=true
     * in order for the actual over ride to happen.
     * 
     * @return form value encoding to use for this particular user-agent.
     * <span>
     * 
     * <span class="ja">
     * ERXRequest オブジェクトを使用すると、各ブラウザのエンコーディング方法をオーバライドすることができます。
     * デフォルト実装では null を戻します。
     * オーバライドを使用する為には、次のプロパティーをセットする必要があります。
     * er.extensions.ERXRequest.BrowserFormValueEncodingOverrideEnabled=true
     * 
     * @return 指定 user-agent のフォーム・バリューのエンコーディング方法
     * <span>
     */
    @SuppressWarnings("javadoc")
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
    @Override
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
