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
 * <div class="en">
 * ERXBrowser is an abstract class that defines browser object. 
 * A browser object represents the web browser that the current 
 * request-response loop is dealing with. It holds the information 
 * retrieved from HTTP request's "user-agent" header, and such 
 * information includes web browser's name, version, Mozilla 
 * compatible version and platform (OS). Also, a browser object 
 * can answer boolean questions such as <code>isIE</code>, 
 * <code>isOmniWeb</code>, <code>isVersion5</code> and 
 * <code>isMozilla40Compatible</code>.
 * <p>
 * ERXBrowser is immutable and shared by different sessions and
 * direct actions. 
 * The shared instances are managed by ERXBrowserFactory which 
 * is also responsible to parse "user-agent" header in a WORequest 
 * object and to get an appropriate browser object.
 * <p>
 * One concrete browser, ERXBasicBrowser, is defined in the 
 * ERExtensions framework. It not only implements the basic 
 * questions defined by ERXBrowser, but also more specific 
 * questions like <code>isIFrameSupported</code> and 
 * <code>willRenderNestedTablesFast</code>.
 * <p>
 * You can extend ERXBrowser or its concrete subclass 
 * ERXBasicBrowser to implement more specific questions for 
 * your application. One good example will be to have a question 
 * <code>isSupportedBrowser</code> that checks if the client 
 * is using one of the supported browsers for your application.
 * <p>
 * ERXSession holds a browser object that represent the web 
 * browser for that session and <code>browser</code> method 
 * returns the object. 
 * 
 * To access ERXBrowser's boolean questions from WOConditionals 
 * on a web component, set the key path like "session.brower.isIFrameSupported" 
 * to their condition bindings.
 * <p>
 * ERXDirectAction also holds a browser object for the current request. 
 * Use its <code>browser</code> method to access the object from a 
 * session-less direct action.
 * </div>
 * 
 * <div class="ja">
 * ERXBrowser はブラウザ・オブジェクトを表現する抽象的なクラスです。
 * ブラウザ・オブジェクトはカレント・リクエスト・レスポンス・ループの Webブラウザを表現しています。
 * HTTPリクエスト "user-agent" より情報を取得し、 Webブラウザ名、バージョン番号、プラットフォームと
 * Mozilla バージョン番号等々を含みます。他には boolean で <code>isIE</code>,
 * <code>isOmniWeb</code>, <code>isVersion5</code> と 
 * <code>isMozilla40Compatible</code>の情報を簡単にアクセスできます。
 * <p>
 * ERXBrowser は不変で、他のセッションとダイレクト・アクションで共有されています。
 * 共有インスタンスは ERXBrowserFactory で管理されています。他には ERXBrowserFactory が
 * WORequest の "user-agent" パースとブラウザ・オブジェクトの作成を担当しています。
 * <p>
 * ERExtensions フレームワークには ERXBasicBrowser ブラウザ・オブジェクトが明確にされている。
 * ERXBrowser の基本な調査メソッドのみではなく、もっと確実な <code>isIFrameSupported</code>
 * と <code>willRenderNestedTablesFast</code> を回答します。
 * <p>
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
 * オブジェクトをアクセスするには <code>browser</code> メソッドを使用します。
 * </div>
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
     * <div class="en">
     * Browser name string
     * </div>
     * 
     * <div class="ja">
     * ブラウザ名を戻します
     * </div>
     * 
     * @return <div class="en">what type of browser</div>
     *         <div class="ja">ブラウザ名</div>
     */
    public abstract String browserName();

    /**
     * <div class="en">
     * Version string
     * </div>
     * 
     * <div class="ja">
     * ブラウザのバージョンアップを戻します
     * </div>
     * 
     * @return <div class="en">what version of browser</div>
     *         <div class="ja">ブラウザのバージョンアップ</div>
     */
    public abstract String version();
    
    /**
     * Major version
     * 
     * @return what major version of browser
     */
    public abstract Integer majorVersion();

    /**
     * <div class="en">
     * MozillaVersion string
     * </div>
     * 
     * <div class="ja">
     * ブラウザの Mozilla バージョンを戻します
     * </div>
     * 
     * @return <div class="en">Mozilla version equivalent to the browser's version</div>
     *         <div class="ja">ブラウザの Mozilla バージョン</div>
     */
    public abstract String mozillaVersion();

    /**
     * <div class="en">
     * The revision of the gecko rendering engine. 1.0.2 and up support xslt.
     * </div>
     * 
     * <div class="ja">
     * gecko レンダリング・エンジンのバージョンを戻します
     * </div>
     * 
     * @return <div class="en">gecko revision equivalent to the browser's version</div>
     *         <div class="ja">gecko レンダリング・エンジンのバージョンを戻します</div>
     */
    public abstract String geckoRevision();

    /**
     * <div class="en">
     * Platform string
     * </div>
     * 
     * <div class="ja">
     * プラットフォームを戻します
     * </div>
     * 
     * @return <div class="en">what platform that the browser is running on</div>
     *         <div class="ja">プラットフォーム</div>
     */
    public abstract String platform();

    /**
     * <div class="en">
     * UserInfo dictionary
     * </div>
     * 
     * <div class="ja">
     * UserInfo ディクショナリーを戻します
     * </div>
     * 
     * @return <div class="en">the user info</div>
     *         <div class="ja">UserInfo ディクショナリー</div>
     */
    public abstract NSDictionary userInfo();

    /**
     * <div class="ja">
     * 未知のブラウザ？
     * </div>
     * 
     * @return <div class="en">true if browser type is unknown</div>
     *         <div class="ja">未知のブラウザの場合は true が戻ります</div>
     */
    public abstract boolean isUnknownBrowser();

    /**
     * <div class="en">
     * Browser is isRobot?
     * </div>
     * 
     * <div class="ja">
     * ブラウザはロボットですか？
     * </div>
     * 
     * @return <div class="en">true if browser is robot.</div>
     *         <div class="ja">ロボットの場合には true が戻ります</div>
     */
    public abstract boolean isRobot();

    /**
     * <div class="en">
     * Browser is iCab?
     * </div>
     
     * <div class="ja">
     * ブラウザは iCab ですか？
     * </div>
     * 
     * @return <div class="en">true if browser is iCab.</div>
     *         <div class="ja">iCab の場合には true が戻ります</div>
     */
    public abstract boolean isICab();

    /**
     * <div class="en">
     * Browser is Internet Explorer?
     * </div>
     * 
     * <div class="ja">
     * ブラウザは Internet Explorer ですか？
     * </div>
     * 
     * @return <div class="en">true if browser is IE.</div>
     *         <div class="ja">IE の場合には true が戻ります</div>
     */
    public abstract boolean isIE();

    /**
     * <div class="en">
     * Browser is Netscape?
     * </div>
     * 
     * <div class="ja">
     * ブラウザは Netscape ですか？
     * </div>
     * 
     * @return <div class="en">true if browser is Netscape.</div>
     *         <div class="ja">Netscape の場合には true が戻ります</div>
     */
    public abstract boolean isNetscape();

    /**
     * <div class="en">
     * Browser is not Netscape?
     * </div>
     * 
     * <div class="ja">
     * ブラウザは Netscape ではないか？
     * </div>
     * 
     * @return <div class="en">true if browser is not Netscape.</div>
     *         <div class="ja">Netscape でない場合には true が戻ります</div>
     */
    public abstract boolean isNotNetscape();

    /**
     * <div class="en">
     * Browser is OmniWeb?
     * </div>
     * 
     * <div class="ja">
     * ブラウザは OmniWeb ですか？
     * </div>
     * 
     * @return <div class="en">true if browser is OmniWeb.</div>
     *         <div class="ja">OmniWeb の場合には true が戻ります</div>
     */
   public abstract boolean isOmniWeb();

    /**
     * <div class="en">
     * Browser is Opera?
     * </div>
     * 
     * <div class="ja">
     * ブラウザは Opera ですか？
     * </div>
     * 
     * @return <div class="en">true if browser is Opera.</div>
     *         <div class="ja">Opera の場合には true が戻ります</div>
     */
    public abstract boolean isOpera();

    /**
     * <div class="en">
     * Browser is Safari?
     * </div>
     * 
     * <div class="ja">
     * ブラウザは Safari ですか？
     * </div>
     * 
     * @return <div class="en">true if browser is Safari.</div>
     *         <div class="ja">Safari の場合には true が戻ります</div>
     */
    public abstract boolean isSafari();
    
    /**
     * <div class="en">
     * Browser is Firefox?
     * </div>
     * 
     * <div class="ja">
     * ブラウザは Firefox ですか？
     * </div>
     * 
     * @return <div class="en">true if browser is Firefox.</div>
     *         <div class="ja">Firefox の場合には true が戻ります</div>
     */
   public abstract boolean isFirefox();
    
    /**
     * <div class="en">
     * Browser is Chrome?
     * </div>
     * 
     * <div class="ja">
     * ブラウザは Chrome ですか？
     * </div>
     * 
     * @return <div class="en">true if browser is Chrome.</div>
     *         <div class="ja">Chrome の場合には true が戻ります</div>
     */
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
     * <div class="en">
     * Gets the message encoding for a given request. Default implementation
     * gets the message encoding for all of the browserLanguages off of
     * the request.
     * </div>
     * 
     * <div class="ja">
     * 指定のリクエストのメッセージ・エンコーディング方法を戻します。
     * デフォルト実装ではリクエストの全てのブラウザ言語のエンコーディング方法を取得します。
     * </div>
     * 
     * @param request <div class="en">to get the message encoding for</div>
     *                <div class="ja">リクエスト WORequest</div>
     * @return <div class="en">message encoding</div>
     *         <div class="ja">メッセージ・エンコーディング方法</div>
     */
    public ERXMessageEncoding messageEncodingForRequest(WORequest request) {
        return messageEncodingForLanguages(request.browserLanguages());
    }

    /**
     * <div class="en">
     * Gets the message encoding for a given array of languages.
     * </div>
     * 
     * <div class="ja">
     * 指定の言語配列のメッセージ・エンコーディング方法を戻します。
     * </div>
     * 
     * @param languages <div class="en">array to get the correct encoding for</div>
     *                  <div class="ja">メッセージ・エンコーディング方法を取得したい言語配列</div>
     * @return <div class="en">message encoding</div>
     *         <div class="ja">メッセージ・エンコーディング方法</div>
     */
    public ERXMessageEncoding messageEncodingForLanguages(NSArray languages) {
        return new ERXMessageEncoding(languages);
    }
    
    /**
     * <div class="en">
     * Gets the message encoding for a given language.
     * </div>
     * 
     * <div class="en">
     * 指定の言語のメッセージ・エンコーディング方法を戻します。
     * </div>
     * 
     * @param language <div class="en">to get the encoding for</div>
     *                 <div class="ja">メッセージ・エンコーディング方法を取得したい言語</div>
     * @return <div class="en">message encoding</div>
     *         <div class="ja">メッセージ・エンコーディング方法</div>
     */
    public ERXMessageEncoding messageEncodingForLanguage(String language) {
        return new ERXMessageEncoding(language);
    }    

    /**
     * <div class="en">
     * If using ERXRequest objects allows one to override on a per browser basis
     * what form value encoding to use. Default implementation defaults to null
     * Note that you will need to enable the property:
     * er.extensions.ERXRequest.BrowserFormValueEncodingOverrideEnabled=true
     * in order for the actual over ride to happen.
     * </div>
     * 
     * <div class="ja">
     * ERXRequest オブジェクトを使用すると、各ブラウザのエンコーディング方法をオーバライドすることができます。
     * デフォルト実装では null を戻します。
     * オーバライドを使用する為には、次のプロパティーをセットする必要があります。
     * er.extensions.ERXRequest.BrowserFormValueEncodingOverrideEnabled=true
     * </div>
     * 
     * @return <div class="en">form value encoding to use for this particular user-agent.</div>
     *         <div class="ja">指定 user-agent のフォーム・バリューのエンコーディング方法</div>
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
