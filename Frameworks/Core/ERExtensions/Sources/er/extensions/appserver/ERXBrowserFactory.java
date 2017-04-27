//
// ERXBrowserFactory.java
// Project ERExtensions
//
// Created by tatsuya on Mon Jul 22 2002
//
package er.extensions.appserver;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXMutableDictionary;
import er.extensions.foundation.ERXMutableInteger;
import er.extensions.foundation.ERXStringUtilities;

/**
 * <div class="en">
 * All WebObjects applications have exactly one <code>ERXBrowserFactory</code> 
 * instance. Its primary role is to manage {@link ERXBrowser} objects. 
 * It provides facility to parse <code>"user-agent"</code> HTTP header and to 
 * create an appropriate browser object. It also maintains the 
 * browser pool to store shared <code>ERXBrowser</code> objects. 
 * Since <code>ERXBrowser</code> object is immutable, it can be 
 * safely shared between sessions and <code>ERXBrowserFactory</code> 
 * tries to have only one instance of <code>ERXBrowser</code> for 
 * each kind of web browsers.
 * <p>
 * The primary method called by {@link ERXSession} and {@link ERXDirectAction} 
 * is {@link #browserMatchingRequest browserMatchingRequest} 
 * which takes a {@link com.webobjects.appserver.WORequest WORequest} 
 * as the parameter and returns a shared instance of browser object. 
 * You actually wouldn't have to call this function by yourself 
 * because <code>ERXSession</code> and <code>ERXDirectAction</code> 
 * provide {@link ERXSession#browser() browser} method 
 * that returns a browser object for the current request for you.
 * <p>
 * Note that <code>ERXSession</code> and <code>ERXDirectAction</code> 
 * call <code>ERXBrowserFactory</code>'s 
 * {@link #retainBrowser retainBrowser} and {@link #releaseBrowser releaseBrowser}  
 * to put the browser object to the browser pool when it is 
 * created and to remove the browser object from the pool when 
 * it is no longer referred from sessions and direct actions. 
 * <code>ERXSession</code> and <code>ERXDirectAction</code> 
 * automatically handle this and you do not have to call these 
 * methods from your code.<br>
 * <p>
 * The current implementation of the parsers support variety of 
 * Web browsers in the market such as Internet Explorer (IE), 
 * OmniWeb, Netscape, iCab and Opera, versions between 2.0 and 7.0. <br>
 * <p>
 * To customize the parsers for <code>"user-agent"</code> HTTP header, 
 * subclass <code>ERXBrowserFactory</code> and override methods 
 * like {@link #parseBrowserName parseBrowserName}, 
 * {@link #parseVersion parseVersion}, 
 * {@link #parseMozillaVersion parseMozillaVersion} and 
 * {@link #parsePlatform parsePlatForm}. 
 * Then put the following statement into the application's 
 * constructor. 
 * <p>
 * <code>ERXBrowserFactory.{@link #setFactory 
 * setFactory(new SubClassOfERXBrowserFactory())};</code>
 * <p>
 * If you want to use your own subclass of <code>ERXBrowser</code>, 
 * put the following statement into the application's constructor.
 * <p>
 * <code>ERXBrowserFactory.factory().{@link #setBrowserClassName 
 * setBrowserClassName("NameOfTheSubClassOfERXBrowser")}</code>
 *
 * <p>
 * <pre>
 * This implementation is tested with the following browsers (or "user-agent" strings)
 * Please ask the guy (tatsuyak@mac.com) for WOUnitTest test cases. 
 * 
 * Mac OS X 
 * ----------------------------------------------------------------------------------
 * iCab 2.8.1       Mozilla/4.5 (compatible; iCab 2.8.1; Macintosh; I; PPC)
 * IE 5.21          Mozilla/4.0 (compatible; MSIE 5.21; Mac_PowerPC)
 * Netscape 7.0b1   Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:1.0rc2) Gecko/20020512 Netscape/7.0b1
 * Netscape 6.2.3   Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:0.9.4.1) Gecko/20020508 Netscape6/6.2.3
 * OmniWeb 5.11.1   Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_7_3; en-US) AppleWebKit/533.21.1+(KHTML, like Gecko, Safari/533.19.4) Version/5.11.1 OmniWeb/622.18.0
 * Safari 1.0b(v48) Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/48 (like Gecko) Safari/48
 * iPhone 1.0       Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3
 * 
 * Windows 2000
 * ----------------------------------------------------------------------------------
 * IE 6.0           Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)
 * IE 5.5           Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)
 * Netscape 6.2.3   Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:0.9.4.1) Gecko/20020508 Netscape6/6.2.3
 * Netscape 4.79    Mozilla/4.79 [en] (Windows NT 5.0; U)
 * Opera 6.04       Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.04  [en]
 * </pre>
 * </div>
 *
 * <div class="ja">
 * 全 WebObjects アプリケーションは一つの <code>ERXBrowserFactory</code> インスタンスを持っている。
 * このメソッドは {@link ERXBrowser} オブジェクトを管理する責任があります。
 * 
 * HTTP ヘッダーの <code>"user-agent"</code> をパース、及び適切なブラウザ・オブジェクトを作成する機能がある。
 * さらに共有 <code>ERXBrowser</code> オブジェクトのブラウザ・プールをメンテナンスします。
 * 
 * <code>ERXBrowser</code> オブジェクトは不可変で、セッション間での共有も安全です。
 * <code>ERXBrowserFactory</code> は各ブラウザ種類に一つのオブジェクトのみを作成することを試します。
 * 
 * {@link ERXSession} と {@link ERXDirectAction} より呼び出されるメイン・メソッドは
 * {@link #browserMatchingRequest browserMatchingRequest} で、引数として
 * {@link com.webobjects.appserver.WORequest WORequest} を受け取り、
 * ブラウザ・オブジェクトの共有インスタンスを戻します。
 * 
 * 実際には直接呼ぶ必要はないでしょう。なぜなら、<code>ERXSession</code> と <code>ERXDirectAction</code>
 * は適切なブラウザ・オブジェクトをカレント・リクエストの為に戻す {@link ERXSession#browser browser} メソッドを用意しています。
 * 
 * メモ：
 * <code>ERXSession</code> と <code>ERXDirectAction</code> は <code>ERXBrowserFactory</code> の
 * {@link #retainBrowser retainBrowser} と {@link #releaseBrowser releaseBrowser} を呼び出し、
 * ブラウザ・オブジェクトが作成される時にブラウザ・プールに追加し、ダイレクト・アクションやセッションよりの参照が
 * なくなるとブラウザ・プールから削除します。
 * 
 * <code>ERXSession</code> と <code>ERXDirectAction</code> は自動的に処理しますので、
 * 自分でこれらのメソッドを呼ぶことはありません。
 * 
 * カレント実装では多数の Webブラウザをサポートしています。例えば、 Internet Explorer (IE), 
 * OmniWeb, Netscape, iCab と Opera, バージョン番号は 2.0 から 7.0 まで <br>
 * 
 * HTTP ヘッダーの <code>"user-agent"</code> パースを拡張する場合には <code>ERXBrowserFactory</code>
 * をサブクラス化し、次のメソッド {@link #parseBrowserName parseBrowserName}, 
 * {@link #parseVersion parseVersion}, {@link #parseMozillaVersion parseMozillaVersion}
 * と {@link #parsePlatform parsePlatForm} をオーバライドすると良いです。
 * 
 * 後は次のステートメントをコンストラクタにセットします。
 * <code>ERXBrowserFactory.{@link #setFactory setFactory(new SubClassOfERXBrowserFactory())};</code>
 * 
 * <code>ERXBrowser</code> の独自のサブクラスを作成されたい場合は次のステートメントをコンストラクタにセットします。
 * <code>ERXBrowserFactory.factory().{@link #setBrowserClassName setBrowserClassName("NameOfTheSubClassOfERXBrowser")}</code>
 * 
 * <pre>
 * この実装は次のブラウザ "user-agent" でテストされています。
 * 
 * Mac OS X 
 * ----------------------------------------------------------------------------------
 * iCab 2.8.1       Mozilla/4.5 (compatible; iCab 2.8.1; Macintosh; I; PPC)
 * IE 5.21          Mozilla/4.0 (compatible; MSIE 5.21; Mac_PowerPC)
 * Netscape 7.0b1   Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:1.0rc2) Gecko/20020512 Netscape/7.0b1
 * Netscape 6.2.3   Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:0.9.4.1) Gecko/20020508 Netscape6/6.2.3
 * OmniWeb 5.11.1   Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_7_3; en-US) AppleWebKit/533.21.1+(KHTML, like Gecko, Safari/533.19.4) Version/5.11.1 OmniWeb/622.18.0
 * Safari 1.0b(v48) Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/48 (like Gecko) Safari/48
 * iPhone 1.0       Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3
 * 
 * Windows 2000
 * ----------------------------------------------------------------------------------
 * IE 6.0           Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)
 * IE 5.5           Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)
 * Netscape 6.2.3   Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:0.9.4.1) Gecko/20020508 Netscape6/6.2.3
 * Netscape 4.79    Mozilla/4.79 [en] (Windows NT 5.0; U)
 * Opera 6.04       Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.04  [en]
 * </pre>
 * </div>
 *
 * @property er.extensions.ERXBrowserFactory.FactoryClassName
 * @property er.extensions.ERXBrowserFactory.BrowserClassName (default ERXBasicBrowser)
 */
public class ERXBrowserFactory {

    /** logging support */
    protected static final Logger log = Logger.getLogger(ERXBrowserFactory.class);

    /** 
     * <div class="en">holds the default browser class name</div>
     * <div class="ja">デフォルト・ブラウザ・クラス名を保持</div>
     */
    private static final String _DEFAULT_BROWSER_CLASS_NAME = ERXBasicBrowser.class.getName();

    /** 
     * <div class="en">Caches a reference to the browser factory</div>
     * <div class="ja">ブラウザ・ファクトリーへのリファレンス：キャシュ用</div>
     */
    private static ERXBrowserFactory _factory;

    /** 
     * <div class="en">Expressions that define a robot</div>
     * <div class="ja">ロボットを認識できる定義：キャシュ用</div>
     */
    private static final NSMutableArray<Pattern> robotExpressions = new NSMutableArray();

    /** 
     * <div class="en">Mapping of UAs to browsers</div>
     * <div class="ja">ブラウザと user-agent マップ：キャシュ用</div>
     */
    private static final NSMutableDictionary _cache = ERXMutableDictionary.synchronizedDictionary();

    /**
     * <div class="en">
     * Gets the singleton browser factory object.
     * </div>
     * 
     * <div class="ja">
     * singleton ブラウザ・ファクトリー・オブジェクトを取得します
     * </div>
     * 
     * @return <div class="en">browser factory</div>
     *         <div class="ja">ブラウザ・ファクトリー</div>
     */
    public static ERXBrowserFactory factory() {
        if (_factory == null) {
            String browserFactoryClass = System.getProperty("er.extensions.ERXBrowserFactory.FactoryClassName");
            if (browserFactoryClass != null && !browserFactoryClass.equals(ERXBrowserFactory.class.getName())) {
                log.debug("Creating browser factory for class name: " + browserFactoryClass);
                try {
                    Class browserClass = Class.forName(browserFactoryClass);
                    _factory = (ERXBrowserFactory)browserClass.newInstance();
                } catch (Exception e) {
                    log.error("Unable to create browser factory for class name \"" + browserFactoryClass + "\"", e);
                }
            }
            if (_factory == null) {
                log.debug("Factory null creating default browser factory. " + browserFactoryClass);
                _factory = new ERXBrowserFactory();
            }
        }
        return _factory;
    }

    /**
     * <div class="en">
     * Sets the browser factory used to create browser objects.
     * </div>
     * 
     * <div class="ja">
     * ブラウザ・オブジェクトを作成するブラウザ・ファクトリーをセットします
     * </div>
     * 
     * @param newFactory <div class="en">new browser factory</div>
     *                   <div class="ja">新しいブラウザ・ファクトリー</div>
     */
    public static void setFactory(ERXBrowserFactory newFactory) { _factory = newFactory; }

    /** 
     * <div class="en">Caches the browser class name</div>
     * <div class="ja">ブラウザ・クラス名：キャシュ用</div>
     */
    protected String _browserClassName;

    /**
     * <div class="en">
     * Returns the name of the {@link ERXBrowser} subclass. 
     * The default value is <code>"er.extensions.appserver.ERXBasicBrowser"</code>.
     * </div>
     * 
     * <div class="ja">
     * {@link ERXBrowser} サブクラスの名前を戻します。
     * 
     * デフォルト値は <code>"er.extensions.appserver.ERXBasicBrowser"</code>.
     * </div>
     * 
     * @see	#setBrowserClassName
     * 
     * @return <div class="en">the name of the ERXBrowser subclass; default to 
     *          <code>"er.extensions.appserver.ERXBasicBrowser"</code></div>
     *         <div class="ja">ERXBrowser サブクラスの名前; デフォルト <code>"er.extensions.appserver.ERXBasicBrowser"</code></div>
     */
    public String browserClassName() { return _browserClassName; }
    
    /**
     * <div class="en">
     * Sets the name of the {@link ERXBrowser} subclass.
     * </div>
     * 
     * <div class="ja">
     * ERXBrowser サブクラスの名前をセットします
     * </div>
     * 
     * @param name <div class="en">the name of the ERXBrowser subclass; ignored if null</div>
     *             <div class="ja">ERXBrowser サブクラスの名前; null の場合は無視</div>
     * 
     * @see #browserClassName
     * @see #createBrowser
     */
    public void setBrowserClassName(String name) { 
        if (name != null  &&  name.length() > 0) 
            _browserClassName = name; 
    }

    /**
     * Public browser constructor.
     */
    public ERXBrowserFactory() {
        // ENHANCEME: (tk) to arrow to set the class name from property files and launch arguments. 
        setBrowserClassName(System.getProperty("er.extensions.ERXBrowserFactory.BrowserClassName",
                                               _DEFAULT_BROWSER_CLASS_NAME));
    }

    /** 
     * <div class="en">
     * Gets a shared browser object for given request. 
     * Parses <code>"user-agent"</code> string in the request and gets 
     * the appropriate browser object. 
     * <p>
     * This is the primary method to call from application logics, and 
     * once you get a browser object, you are responsible to call 
     * {@link #retainBrowser retainBrowser} to keep the browser 
     * object in the browser pool. 
     * <p>
     * You are also required to call {@link #releaseBrowser releaseBrowser} 
     * to release the browser from the pool when it is no longer needed. 
     * </div>
     * 
     * <div class="ja">
     * 指定 WORequest より共有ブラウザ・オブジェクトを取得します。
     * リクエスト内の <code>"user-agent"</code> 文字列をパースし、適切なブラウザ・オブジェクトを取得します。
     * <p>
     * アプリケーション・ロジックより呼ばれるメイン・メソッドになります。
     * ブラウザ・オブジェクトを取得した後、ブラウザ・オブジェクトをブラウザ・プールに登録する
     * retainBrowser メソッドの呼び出しは開発者の責任です。
     * <p>
     * 他にもオブジェクトが不必要になった場合には releaseBrowser を呼ばなければなりません。
     * </div>
     * 
     * @param request - WORequest
     * @return <div class="en">a shared browser object</div>
     *         <div class="ja">共有ブラウザ・オブジェクト</div>
     */
    public ERXBrowser browserMatchingRequest(WORequest request) {
        if (request == null) {
        	throw new IllegalArgumentException("Request can't be null.");
        }

        String ua = request.headerForKey("user-agent");
        return browserMatchingUserAgent(ua);
    }

    /** 
     * <div class="en">
     * Returns a shared browser object for a given <code>user-agent</code>
     * string by parsing the string and retrieving the appropriate browser 
     * object, creating it if necessary. 
     * <p>
     * Use this method to retrieve a browser instance from an existing
     * user-agent string rather than a request object (e.g. you're 
     * recreating a browser instance from a past user-agent string). Once 
     * you get the browser object, you are responsible for calling {@link 
     * #retainBrowser retainBrowser} to keep it in the browser pool. 
     * <p>
     * You are also required to call {@link #releaseBrowser releaseBrowser} 
     * to release the browser from the pool when it is no longer needed. 
     * </div>
     * 
     * @param ua - user agent string (e.g. from request headers)
     * @return <div class="en">a shared browser object</div>
     *         <div class="ja">共有ブラウザ・オブジェクト</div>
     */
    public ERXBrowser browserMatchingUserAgent(String ua) {
        if (ua == null) {
            return getBrowserInstance(ERXBrowser.UNKNOWN_BROWSER, ERXBrowser.UNKNOWN_VERSION, 
            		ERXBrowser.UNKNOWN_VERSION, ERXBrowser.UNKNOWN_PLATFORM, null);
        }
        
       	ERXBrowser result = (ERXBrowser) _cache.objectForKey(ua);
       	if (result == null) {
       		String browserName 		= parseBrowserName(ua);
       		String version 			= parseVersion(ua);
       		String mozillaVersion	= parseMozillaVersion(ua);
       		String platform 		= parsePlatform(ua);
       		NSDictionary userInfo 	= new NSDictionary(
       				new Object[] {parseCPU(ua), parseGeckoVersion(ua)},
       				new Object[] {"cpu", "geckoRevision"});
       		
        	result = getBrowserInstance(browserName, version, mozillaVersion, platform, userInfo);
        	_cache.setObjectForKey(result, ua);
        }
        return result;
    }

    /** 
     * <div class="en">
     * Gets a shared browser object from browser pool. If such browser 
     * object does not exist, this method will create one by using 
     * {@link #createBrowser createBrowser} method.
     * </div>
     * 
     * <div class="en">
     * ブラウザ・プールより共有ブラウザ・オブジェクトを戻します。
     * このようなブラウザ・オブジェクトがなければ、
     * このメソッドは {@link #createBrowser createBrowser} メソッドを使って作成します。
     * </div>
     * 
     * @param browserName <div class="en">string</div>
     *                    <div class="ja">ブラウザ名</div>
     * @param version <div class="en">string</div>
     *                <div class="ja">バージョン</div>
     * @param mozillaVersion <div class="en">string</div>
     *                       <div class="ja">mozillaの対応バージョン</div>
     * @param platform <div class="en">string</div>
     *                 <div class="ja">プラットフォーム</div>
     * @param userInfo <div class="en">string</div>
     *                 <div class="ja">ユーザ情報を持つディクショナリー</div>
     * 
     * @return <div class="en">a shared browser object</div>
     *         <div class="ja">共有ブラウザ・オブジェクト</div>
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
     * <div class="en">
     * Creates a new browser object for given parameters. Override this 
     * method if you need to provide your own subclass of {@link ERXBrowser}. 
     * If you override it, your implementation should not call <code>super</code>.
     * <p>
     * Alternatively, use {@link #setBrowserClassName} and {@link #browserClassName}.
     * </div>
     * 
     * <div class="ja">
     * 指定パラメータを使って、新しいブラウザ・オブジェクトを作成します。
     * 独自のサブクラスが必要な場合にはこのメソッドをオーバライドすると良いのです。
     * オーバライドをした場合の実装が <code>super</code> を呼ばないこと。
     * <p>
     * 他には {@link #setBrowserClassName} と {@link #browserClassName} を使用できます。
     * </div>
     * 
     * @see	#setBrowserClassName
     * @see	#browserClassName
     * 
     * @param browserName <div class="en">string</div>
     *                    <div class="ja">ブラウザ名</div>
     * @param version <div class="en">string</div>
     *                <div class="ja">バージョン</div>
     * @param mozillaVersion <div class="en">string</div>
     *                       <div class="ja">mozillaの対応バージョン</div>
     * @param platform <div class="en">string</div>
     *                 <div class="ja">プラットフォーム</div>
     * @param userInfo <div class="en">string</div>
     *                 <div class="ja">ユーザ情報を持つディクショナリー</div>
     * 
     * @return <div class="en">new browser object that is a concrete subclass of <code>ERXBrowser</code></div>
     *         <div class="ja"><code>ERXBrowser</code> を明確なサブクラスとして持つ新規ブラウザ・オブジェクト</div>
     */
    public synchronized ERXBrowser createBrowser(String browserName, String version, String mozillaVersion,
                                                String platform, NSDictionary userInfo) {
        ERXBrowser browser = null;
        try {
            browser = _createBrowserWithClassName(browserClassNameForBrowserNamed(browserName),
                                        browserName, version, mozillaVersion, platform, userInfo);
        } catch (Exception ex) {
            log.error("Unable to create a browser for class name: " + browserClassNameForBrowserNamed(browserName) 
                            + " with exception: " + ex.getMessage() + ".  Will use default classes."
                            + " Please ensure that the fully-qualified name for the class is specified"
                            + " if it is in a different package.", ex);
        }
        if (browser == null) {
            try {
                browser = _createBrowserWithClassName(_DEFAULT_BROWSER_CLASS_NAME, 
                                        browserName, version, mozillaVersion, platform, userInfo);
            } catch (Exception ex) {
                log.error("Unable to create even a default browser for class name: " + _DEFAULT_BROWSER_CLASS_NAME
                            + " with exception: " + ex.getMessage()
                            + "  Will instanciate a browser with regular" 
                            + " new " + _DEFAULT_BROWSER_CLASS_NAME + "(...) statement.", ex);
                browser = new ERXBasicBrowser(browserName, version, mozillaVersion, platform, userInfo);
            }
        }
        return browser;
    }

    /** 
     * <div class="ja">
     * クラス名を使って、ERXBrowserオブジェクトを作成します 
     * 
     * @param className - クラス名
     * @param browserName - ブラウザ名
     * @param version - バージョン情報
     * @param mozillaVersion - Mozilla バージョン
     * @param platform - プラットフォーム
     * @param userInfo - ユーザ・ディクショナリー
     * </div>
     * 
     * @return <div class="en"></div>
     *         <div class="ja">ERXBrowser オブジェクト</div>
     * 
     * @throws java.lang.reflect.InvocationTargetException 
     * @throws ClassNotFoundException 
     * @throws NoSuchMethodException 
     * @throws InstantiationException 
     * @throws IllegalAccessException 
     */
    private ERXBrowser _createBrowserWithClassName(String className, String browserName, String version, 
                                            String mozillaVersion, String platform, NSDictionary userInfo) 

                                            throws  ClassNotFoundException, 
                                                    NoSuchMethodException, 
                                                    InstantiationException, 
                                                    IllegalAccessException, 
                                                    java.lang.reflect.InvocationTargetException 
    {
                            
        Class browserClass = Class.forName(className);
        Class[] paramArray = new Class[] { String.class, String.class, String.class, 
                                                                String.class, NSDictionary.class };
        java.lang.reflect.Constructor constructor = browserClass.getConstructor(paramArray);
        return (ERXBrowser)constructor.newInstance(new Object[] {
                    browserName, version, mozillaVersion, platform, userInfo } );
    }
        

    /**
     * <div class="en">
     * Retains a given browser object.
     * </div>
     * 
     * <div class="ja">
     * 指定ブラウザ・オブジェクトをブラウザ・プールに登録します
     * </div>
     * 
     * @param browser <div class="en">to be retained</div>
     *                <div class="ja">ブラウザ・プールに登録するブラウザ・オブジェクト</div>
     */
    public synchronized void retainBrowser(ERXBrowser browser) {
        String key = _computeKey(browser);
        _browserPool().setObjectForKey(browser, key);
        _incrementReferenceCounterForKey(key);
    }

    /**
     * <div class="en">
     * Decrements the retain count for a given
     * browser object.
     * </div>
     * 
     * <div class="ja">
     * 指定ブラウザ・オブジェクトをブラウザ・プールの登録から解除します
     * </div>
     * 
     * @param browser <div class="en">to be released</div>
     *                <div class="ja">ブラウザ・プールの登録から解除するブラウザ・オブジェクト</div>
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
     * <div class="en">
     * Adds the option to use multiple different ERXBrowser subclasses
     * depending on the name of the browser.
     * </div>
     * 
     * <div class="ja">
     * 複数の ERXBrowser サブクラスを使用できる様にブラウザ名を渡します
     * </div>
     * 
     * @param browserName <div class="en">name of the browser</div>
     *                    <div class="ja">ブラウザ名</div>
     * @return <div class="en">ERXBrowser subclass class name</div>
     *         <div class="ja">ERXBrowser サブクラス名</div>
     */
    public String browserClassNameForBrowserNamed(String browserName) {
        return browserClassName();
    }

    /**
     * <div class="ja">
     * ブラウザ名をパースします
     * </div>
     * 
     * @param userAgent
     * @return <div class="en"></div>
     *         <div class="ja">ブラウザ名</div>
     */
    public String parseBrowserName(String userAgent) {
        String browserString = _browserString(userAgent);
        String browser  = ERXBrowser.UNKNOWN_BROWSER;
        if (isRobot(browserString)) 						browser  = ERXBrowser.ROBOT;
        else if (browserString.indexOf("Edge") > -1) 		browser  = ERXBrowser.EDGE;
        else if (browserString.indexOf("Chrome") > -1) 		browser  = ERXBrowser.CHROME;
        else if (browserString.indexOf("MSIE") > -1 || browserString.indexOf("Trident") > -1)	browser  = ERXBrowser.IE;
        else if (browserString.indexOf("Safari") > -1) 		browser  = ERXBrowser.SAFARI;
        else if (browserString.indexOf("Firefox") > -1) 	browser  = ERXBrowser.FIREFOX;
        else if (browserString.indexOf("OmniWeb") > -1)		browser  = ERXBrowser.OMNIWEB;
        else if (browserString.indexOf("iCab") > -1)		browser  = ERXBrowser.ICAB;
        else if (browserString.indexOf("Opera") > -1)		browser  = ERXBrowser.OPERA;
        else if (browserString.indexOf("Netscape") > -1)	browser  = ERXBrowser.NETSCAPE;
        else if (browserString.startsWith("Mozilla") && (browserString.indexOf("compatible") == -1))	browser  = ERXBrowser.MOZILLA;

        // This condition should always come last because *all* browsers have 
        // the word Mozilla at the beginning of their user-agent string. 
        else if (browserString.indexOf("Mozilla") > -1)		browser  = ERXBrowser.NETSCAPE;

        return browser;
    }
    
    /**
     * <div class="ja">
     * ロボットかどうかを調べる
     * [Resources内：robots.txtを必須]
     * </div>
     * 
     * @param userAgent
     * @return <div class="en"></div>
     *         <div class="ja">ロボットの場合は true が戻ります</div>
     */
    private boolean isRobot(String userAgent) {
    	synchronized (robotExpressions) {
			if(robotExpressions.count()==0) {
				String strings = ERXStringUtilities.stringFromResource("robots", "txt", NSBundle.bundleForName("ERExtensions"));
				for (String item : NSArray.componentsSeparatedByString(strings, "\n")) {
					if(item.trim().length() > 0 && item.charAt(0) != '#') {
						robotExpressions.addObject(Pattern.compile(item));
					}
				}
			}
			userAgent = userAgent.toLowerCase();
			for (Pattern pattern : robotExpressions) {
				if (pattern.matcher(userAgent).find()) {
					log.debug(pattern + " matches  " + userAgent);
					return true;
				}
			}
		}
		
    	return false;
    }
    
    /**
     * <div class="ja">
     * GeckoVersionをパースします
     * </div>
     * 
     * @param userAgent
     * @return GeckoVersion
     */
    public String parseGeckoVersion(String userAgent) {
    	if (userAgent.indexOf("Gecko") >= 0) {
    		final String revString = "; rv:";
    		int startPos = userAgent.indexOf(revString) + revString.length();
            if (startPos > revString.length()) {
            	
            	int endPos = userAgent.indexOf(")", startPos);
            	
            	if (endPos > startPos) {
            		return userAgent.substring(startPos, endPos);
            	} 	
            }
        }
        return ERXBrowser.NO_GECKO;
    }

    /**
     * <div class="ja">
     * バージョン番号をパースします
     * </div>
     * 
     * @param userAgent
     * @return <div class="en"></div>
     *         <div class="ja">バージョン番号</div>
     */
    public String parseVersion(String userAgent) {
        String versionString = _versionString(userAgent);
        int startpos;
        String version = ERXBrowser.UNKNOWN_VERSION;
        
        // Remove "Netscape6" from string such as "Netscape6/6.2.3", 
        // otherwise this method will produce wrong result "6/6.2.3" as the version
        final String netscape6 = "Netscape6";
        startpos = versionString.indexOf(netscape6);
        if (startpos > -1) 
            versionString = versionString.substring(startpos + netscape6.length());
        
        // Find first numeric in the string such as "MSIE 5.21; Mac_PowerPC)"
        startpos = ERXStringUtilities.indexOfNumericInString(versionString);

        if (startpos > -1) {
            StringTokenizer st = new StringTokenizer(versionString.substring(startpos), " ;"); 
            if (st.hasMoreTokens()) 
                version = st.nextToken();  // Will return "5.21" portion of "5.21; Mac_PowerPC)"
        }
		// Test if we got a real number
		try {
	        String normalizedVersion = ERXStringUtilities.removeExtraDotsFromVersionString(version);
			Double.parseDouble(normalizedVersion);
		}
		catch (NumberFormatException e) {
			version = ERXBrowser.UNKNOWN_VERSION;
		}
        return version;
    }

    /**
     * <div class="ja">
     * Mozillaバージョンをパースします
     * </div>
     * 
     * @param userAgent
     * @return <div class="en"></div>
     *         <div class="ja">Mozillaバージョン</div>
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
     * <div class="ja">
     * プラットフォームをパースします
     * </div>
     * 
     * @param userAgent
     * @return <div class="en"></div>
     *         <div class="ja">プラットフォーム</div>
     */
    public String parsePlatform(String userAgent) {
        String platform = ERXBrowser.UNKNOWN_PLATFORM;
        if      (userAgent.indexOf("Win") > -1) 	platform = ERXBrowser.WINDOWS;
        else if ((userAgent.indexOf("iPhone") > -1) || (userAgent.indexOf("iPod") > -1)) 	platform = ERXBrowser.IPHONE;
        else if (userAgent.indexOf("iPad") > -1) platform = ERXBrowser.IPAD;
        else if (userAgent.indexOf("Mac") > -1) 	platform = ERXBrowser.MACOS;
        else if (userAgent.indexOf("Linux") > -1) 	platform = ERXBrowser.LINUX;
        return platform;
    }

    /**
     * <div class="ja">
     * CPUをパースします
     * </div>
     * 
     * @param userAgent
     * @return CPU
     */
    public String parseCPU(String userAgent) {
        String cpu = ERXBrowser.UNKNOWN_CPU;
        if      (userAgent.indexOf("PowerPC") > -1) 	cpu = ERXBrowser.POWER_PC;
        else if (userAgent.indexOf("PPC") > -1) 	cpu = ERXBrowser.POWER_PC;
        return cpu;
    }

    /**
     * <div class="ja">
     * userAgent よりブラウザ文字列を戻します
     * </div>
     * 
     * @param userAgent
     * @return <div class="en"></div>
     *         <div class="ja">ブラウザ文字列</div>
     */
    private String _browserString(String userAgent) {
        int startpos;

        // Get substring "Chrome/0.X.Y.Z Safari/525.13"
        // from          "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) "
        //               "AppleWebKit/525.13 (KHTML, wie z. B. Gecko) Chrome/0.X.Y.Z Safari/525.13"
        final String chrome = "Chrome";
        startpos = userAgent.indexOf(chrome);
        if (startpos > -1)
            return userAgent.substring(startpos);
        
        // Get substring "OmniWeb/622.18.0"
        // from          "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_7_3; en-US)"
        //               "AppleWebKit/533.21.1+(KHTML, like Gecko, Safari/533.19.4) "
        //               "Version/5.11.1 OmniWeb/622.18.0"
        final String omniWeb = "OmniWeb";
        startpos = userAgent.indexOf(omniWeb);
        if (startpos > -1)
          return userAgent.substring(startpos);
        
        // Get substring "Safari/48"
        // from          "Safari 1.0b(v48) Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) "
        //               "AppleWebKit/48 (like Gecko) Safari/48"
        final String safari = "Safari";
        startpos = userAgent.indexOf(safari);
        if (startpos > -1)
        	return userAgent.substring(startpos);

        // Get substring "Opera 6.04  [en]" 
        // from          "Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.04  [en]"
        final String opera = "Opera";
        startpos = userAgent.indexOf(opera);
        if (startpos > -1) 
        	return userAgent.substring(startpos);

        // Get substring "MSIE 5.21; Mac_PowerPC)"
        // from          "Mozilla/4.0 (compatible; MSIE 5.21; Mac_PowerPC)" 
        final String compatible = "compatible;";
        startpos = userAgent.indexOf(compatible);
        if (startpos > -1) 
        	return userAgent.substring(startpos + compatible.length());
            
        // Get substring "Netscape6/6.2.3" 
        // from          "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-US; rv:0.9.4.1) 
        //                Gecko/20020508 Netscape6/6.2.3"
        final String netscape = "Netscape";
        startpos = userAgent.indexOf(netscape);
        if (startpos > -1) 
        	return userAgent.substring(startpos);
            
        return userAgent;
    }

    /** <div class="ja">ブラウザ・プール</div> */
    private NSMutableDictionary _browserPool;
    private NSMutableDictionary _browserPool() { 
        if (_browserPool == null) 
            _browserPool = new NSMutableDictionary();
        return _browserPool;
    }

    /** <div class="ja">リファレンス・カウント・プール</div> */
    private NSMutableDictionary _referenceCounters;
    private NSMutableDictionary _referenceCounters() {
        if (_referenceCounters == null)
            _referenceCounters = new NSMutableDictionary();
        return _referenceCounters;
    }

    /**
     * <div class="ja">
     * key を使って、_referenceCounters ディクショナリー内のカウンターに１を足すこと
     * 
     * @param key - カウンターを足す key
     * </div>
     * 
     * @return <div class="en"></div>
     *         <div class="ja">新しいカウンターの値を戻します</div>
     */
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

    /**
     * <div class="ja">
     * key を使って、_referenceCounters ディクショナリー内のカウンターに１を減らすこと
     * 
     * @param key - カウンターを減らす key
     * <div>
     * 
     * @return <div class="en"></div>
     *         <div class="ja">新しいカウンターの値を戻します</div>
     */
    private ERXMutableInteger _decrementReferenceCounterForKey(String key) {
        ERXMutableInteger count = (ERXMutableInteger)_referenceCounters().objectForKey(key);
        if (count != null)  
            count.decrement();
        
        if (log.isDebugEnabled()) 
            log.debug("_decrementReferenceCounterForKey() - count = " + count + ", key = " + key);
        return count;
    }

    /**
     * <div class="ja">
     * 下記の引数を使って、合計されている文字列キーを戻します。
     * 
     * @param browser - ブラウザ・オブジェクト
     * </div>
     * 
     * @return <div class="en"></div>
     *         <div class="ja">合計されている文字列キー</div>
     */
    private String _computeKey(ERXBrowser browser) {
        return browser.browserName() + "." + browser.version() + "." + browser.mozillaVersion() + "."
                        + browser.platform() + "." + browser.userInfo();
    }

    /**
     * <div class="ja">
     * 下記の引数を使って、合計されている文字列キーを戻します。
     * 
     * @param browserName - ブラウザ名
     * @param version - バージョン
     * @param mozillaVersion - mozillaバージョン
     * @param platform - プラットフォーム
     * @param userInfo - ユーザ情報ディクショナリー
     * </div>
     * 
     * @return <div class="en"></div>
     *         <div class="ja">合計されている文字列キー</div>
     */
    private String _computeKey(String browserName, String version, String mozillaVersion, 
                                                                String platform, NSDictionary userInfo) {
        return browserName + "." + version + "." + mozillaVersion + "." + platform + "." + userInfo;
    }
    
    private String _versionString(String userAgent) {
    	String versionString;

    	int startpos = userAgent.indexOf("Version/");
    	if (startpos > -1)  {
    		versionString = userAgent.substring(startpos);
    	} else {
    		startpos = userAgent.indexOf("Firefox/");
        	if (startpos > -1)  {
        		versionString = userAgent.substring(startpos);
        	} else {
        		startpos = userAgent.indexOf("rv:");
            	if (startpos > -1)  {
            		versionString = userAgent.substring(startpos);
            		int endpos = versionString.indexOf(')');
            		if (endpos > -1) {
            			versionString = versionString.substring(0, endpos);
            		}
            	} else {
            		startpos = userAgent.indexOf("Edge/");
                	if (startpos > -1)  {
                		versionString = userAgent.substring(startpos);
                	} else {
                		versionString = _browserString(userAgent);
                	}
            	}
        	}
    	}

    	return versionString;
    }

}
