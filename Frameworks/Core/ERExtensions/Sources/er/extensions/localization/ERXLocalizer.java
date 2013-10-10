//
// ERXLocalizer.java
// Project armehaut
//
// Created by ak on Sun Apr 14 2002
//
package er.extensions.localization;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.Format;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.appserver.ERXWOContext;
import er.extensions.eof.ERXConstant;
import er.extensions.formatters.ERXNumberFormatter;
import er.extensions.formatters.ERXTimestampFormatter;
import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXFileNotificationCenter;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSimpleTemplateParser;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXThreadStorage;
import er.extensions.validation.ERXValidationFactory;

/**
 * <span class="en">
 * Provides KVC access to localization.
 * 
 * Monitors a set of files in all loaded frameworks and returns a string given a key for a language. These types of keys
 * are acceptable in the monitored files:
 * 
 * <pre><code>
 *   &quot;this is a test&quot; = &quot;some test&quot;;
 *   &quot;unittest.key.path.as.string&quot; = &quot;some test&quot;;
 *   &quot;unittest&quot; = {
 *      &quot;key&quot; = { 
 *          &quot;path&quot; = { 
 *              &quot;as&quot; = {
 *                  &quot;dict&quot;=&quot;some test&quot;;
 *               };
 *          };
 *      };
 *   };
 * </code></pre>
 * 
 * Note that if you only call for <code>unittest</code>, you'll get a dictionary not a string. So you can localize
 * more complex objects than strings. <br />
 * If you set the base class of your session to ERXSession, you can then use this code in your components:
 * 
 * <pre><code>
 *  valueForKeyPath(&quot;session.localizer.this is a test&quot;)
 *  valueForKeyPath(&quot;session.localizer.unittest.key.path.as.string&quot;)
 *  valueForKeyPath(&quot;session.localizer.unittest.key.path.as.dict&quot;)
 * </code></pre>
 * 
 * For sessionless Apps, you must use another method to get at the requested language and then call the localizer via:
 * 
 * <pre><code>
 *  ERXLocalizer l = ERXLocalizer.localizerForLanguages(languagesThisUserCanHandle) or
 *  ERXLocalizer l = ERXLocalizer.localizerForLanguage(&quot;German&quot;)
 * </code></pre>
 * 
 * These are the defaults can be set (listed with their current defaults):
 * 
 * <pre><code>
 *  er.extensions.ERXLocalizer.defaultLanguage=English
 *  er.extensions.ERXLocalizer.fileNamesToWatch=(&quot;Localizable.strings&quot;,&quot;ValidationTemplate.strings&quot;)
 *  er.extensions.ERXLocalizer.availableLanguages=(English,German)
 *  er.extensions.ERXLocalizer.frameworkSearchPath=(app,ERDirectToWeb,ERExtensions)
 * </code></pre>
 * 
 * There are also methods that pluralize using normal english pluralizing rules (y->ies, x -> xes etc). You can provide
 * your own plural strings by using a dict entry:
 * 
 * <pre><code>
 *  localizerExceptions = {
 *      &quot;Table.0&quot; = &quot;Table&quot;; 
 *      &quot;Table&quot; = &quot;Tables&quot;;
 *      ...
 *  };
 * </code></pre>
 * 
 * in your Localizable.strings. <code>Table.0</code> meaning no "Table", <code>Table.1</code> one table and
 * <code>Table</code> any other number. <b>Note:</b> unlike all other keys, you need to give the translated value
 * ("Tisch" for "Table" in German) as the key, not the untranslated one. This is because this method is mainly called
 * via d2wContext.displayNameForProperty which is already localized. <br />
 * </span>
 * 
 * <span class="ja">
 * ローカライズ用の KVC アクセスを実現します。
 * 
 * すべてのフレームワーク内のファイルセットをモニタし、指定キーと言語に従って String を戻します。
 * 次のキーがファイル内で可能です。
 * 
 * <pre><code>
 *   "this is a test"; = "some test";
 *   "unittest.key.path.as.string" = "some test";
 *   "unittest" = {
 *      "key" = { 
 *          "path" = { 
 *              "as" = {
 *                  "dict"="some test";
 *               };
 *          };
 *      };
 *   };
 * </code></pre>
 * 
 * メモ： <code>unittest</code> のみを呼ぶと、String の替わりに dictionary を取得します。
 * Stringよりも複雑なオブジェクトをローカライズが可能です。<br>
 * 
 * session のベースクラスが ERXSession であれば、コンポーネント内に次のコードを実行できます。
 * 
 * <pre><code>
 *  valueForKeyPath("session.localizer.this is a test")
 *  valueForKeyPath("session.localizer.unittest.key.path.as.string")
 *  valueForKeyPath("session.localizer.unittest.key.path.as.dict")
 * </code></pre>
 * 
 * sessionの無いアプリケーションの場合は他の方法を使用することでリクエスト言語を取得します。
 * 
 * <pre><code>
 *  ERXLocalizer l = ERXLocalizer.localizerForLanguages(languagesThisUserCanHandle) or
 *  ERXLocalizer l = ERXLocalizer.localizerForLanguage("German")
 * </code></pre>
 * 
 * 次のデフォルト情報をセットできます (カレント・デフォルト値含むでリストされています):
 * 
 * <pre><code>
 *  er.extensions.ERXLocalizer.defaultLanguage=English
 *  er.extensions.ERXLocalizer.fileNamesToWatch=("Localizable.strings","ValidationTemplate.strings")
 *  er.extensions.ERXLocalizer.availableLanguages=(English,German)
 *  er.extensions.ERXLocalizer.frameworkSearchPath=(app,ERDirectToWeb,ERExtensions)
 * </code></pre>
 * 
 * 言語体系の英語複数形処理のメソッドもあります。(y->ies, x -> xes など)
 * dict に追加することで、自分の複数形処理ルールを設定できます。
 * 
 * <pre><code>
 *  localizerExceptions = {
 *      "Table.0" = "Table"; 
 *      "Table" = "Tables";
 *      ...
 *  };
 * </code></pre>
 * 
 * Localizable.strings ファイル内 <code>Table.0</code> は"Table"がない、 <code>Table.1</code> は"Table"が一つと
 * <code>Table</code> はそれ以外の数のことです。 <b>メモ:</b> 他のすべてのキーと違って、翻訳済み値をキーとして使用します。
 * (ドイツ語では "Table" の替わりに "Tisch" を使います)
 * なぜなら、このメソッドは良くd2wContext.displayNameForPropertyのようなメソッドより呼ばれ、既に翻訳済みなのです。
 * </span>
 */
public class ERXLocalizer implements NSKeyValueCoding, NSKeyValueCodingAdditions {

	public static final String KEY_LOCALIZER_EXCEPTIONS = "localizerExceptions";

	protected static final Logger log = Logger.getLogger(ERXLocalizer.class);

	protected static final Logger createdKeysLog = Logger.getLogger(ERXLocalizer.class.getName() + ".createdKeys");

	private static boolean isLocalizationEnabled = true;

	private static boolean isInitialized = false;

	private static Boolean _useLocalizedFormatters;

	private static Boolean _fallbackToDefaultLanguage;

	public static final String LocalizationDidResetNotification = "LocalizationDidReset";

	private static Observer observer = new Observer();

  /** <span class="ja">モニタ中のファイル・リスト</span> */
	private static NSMutableArray<URL> monitoredFiles = new NSMutableArray<URL>();
	
	private static final char _localizerMethodIndicatorCharacter = '@';

	static NSArray<String> fileNamesToWatch;
	static NSArray<String> frameworkSearchPath;
	static NSArray<String> availableLanguages;
	static String defaultLanguage;

	static NSMutableDictionary<String, ERXLocalizer> localizers = new NSMutableDictionary<String, ERXLocalizer>();

	public static class Observer {
		public void fileDidChange(NSNotification n) {
			ERXLocalizer.resetCache();
			NSNotificationCenter.defaultCenter().postNotification(LocalizationDidResetNotification, null);
		}
	}

  /**
   * <span class="ja">初期化を行います</span>
   */
	public static void initialize() {
		if (!isInitialized) {
			isLocalizationEnabled = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXLocalizer.isLocalizationEnabled", true);
			isInitialized = true;
		}
	}

  /**
   * <span class="ja">
   * ローカライズ可能かどうかを戻します
   * 
   * @return boolean - true 可能
   * </span>
   */
	public static boolean isLocalizationEnabled() {
		return isLocalizationEnabled;
	}

  /**
   * <span class="ja">
   * ローカライズ可能かどうかをセットします
   * 
   * @param value - true 可能
   * </span>
   */
	public static void setIsLocalizationEnabled(boolean value) {
		isLocalizationEnabled = value;
	}

	/**
	 * <span class="en">
	 * Returns the current localizer for the current thread. Note that the localizer for a given session is pushed onto
	 * the thread when a session awakes and is nulled out when a session sleeps. In case there is no localizer set, it tries to
	 * pull it from the current WOContext or the default language.
	 * 
	 * @return the current localizer that has been pushed into thread storage.
	 * </span>
	 * 
	 * <span class="ja">
   * カレント・スレッドのカレント・ローカライザーを戻します。
   * メモ：指定 session が awake 時にローカライザーをスレッドにプッシュし、session が sleep 時に null されます
   * 
   * ローカライザーが設定されていない場合、カレント WOContext より取得を試し、駄目の場合はデフォルト言語を使用する
   * 
   * @return カレント・ローカライザー
   * </span>
	 */
	public static ERXLocalizer currentLocalizer() {
		ERXLocalizer current = (ERXLocalizer) ERXThreadStorage.valueForKey("localizer");
		if (current == null) {
			if (!isInitialized) {
				initialize();
			}
			WOContext context = ERXWOContext.currentContext();
			// set the current localizer
			if (context != null && context.request() != null && context.request().browserLanguages() != null) {
				current = ERXLocalizer.localizerForLanguages(context.request().browserLanguages());
				ERXLocalizer.setCurrentLocalizer(current);
			}
			else {
				current = defaultLocalizer();
			}
		}
		return current;
	}

	/**
	 * <span class="en">
	 * Sets a localizer for the current thread. This is accomplished by using the object {@link ERXThreadStorage}
	 * 
	 * @param currentLocalizer
	 *            to set in thread storage for the current thread.
	 * </span>
	 * 
	 * <span class="ja">
   * カレント・スレッドのローカライザーをセットします。 {@link ERXThreadStorage} を参照
   * 
   * @param currentLocalizer - スレッドにセットするローカライザー
   * </span>
	 */
	public static void setCurrentLocalizer(ERXLocalizer currentLocalizer) {
		ERXThreadStorage.takeValueForKey(currentLocalizer, "localizer");
	}

	/**
	 * <span class="en">
	 * Gets the localizer for the default language.
	 * 
	 * @return localizer for the default language
	 * </span>
	 * 
	 * <span class="ja">
   * デフォルト言語のローカライザーを戻します
   * 
   * @return ローカライザー
   * </span>
	 */
	public static ERXLocalizer defaultLocalizer() {
		return localizerForLanguage(defaultLanguage());
	}
	
  /**
   * <span class="ja">
   * 英語のローカライザーを戻します
   * 
   * @return 英語のローカライザー
   * </span>
   */
	public static ERXLocalizer englishLocalizer() {
		return localizerForLanguage("English");
	}

  /**
   * <span class="ja">
   * ブラウザー・リクエスト言語のローカライザーを戻します
   * 
   * @param request - WORequest
   * 
   * @return ローカライザー
   * </span>
   */
	public static ERXLocalizer localizerForRequest(WORequest request) {
		return localizerForLanguages(request.browserLanguages());
	}

	/**
	 * <span class="en">
	 * Resets the localizer cache. If WOCaching is enabled then after being reinitialize all of the localizers will be
	 * reloaded.
	 * </span>
	 * 
   * <span class="ja">
   * ローカライザー・キャシュをリセットする
   * WOCaching が使用可能な場合、リセットする時にすべてのローカライザーをリロードします
   * </span>
	 */
	public static void resetCache() {
		initialize();
		if (WOApplication.application().isCachingEnabled()) {
			Enumeration<ERXLocalizer> e = localizers.objectEnumerator();
			while (e.hasMoreElements()) {
				e.nextElement().load();
			}
		}
		else {
			localizers = new NSMutableDictionary<String, ERXLocalizer>();
		}
	}

  /**
   * <span class="ja">
   * キー・リストに追加します
   * 
   * @param value - オブジェクト
   * @param key - キー
   * </span>
   */
	protected void addToCreatedKeys(Object value, String key) {
		if (key != null && value != null) {
			createdKeys.takeValueForKey(value, key);
			if (key.indexOf(" ") > 0) {
				log.info("Value added: " + key + "->" + value + " in " + NSPropertyListSerialization.stringFromPropertyList(ERXWOContext.componentPath(ERXWOContext.currentContext())));
			}
		}
	}

	/**
	 * <span class="en">
	 * Gets the best localizer for a set of languages.
	 * 
	 * @param languages
	 * @return localizer
	 * </span>
	 * 
   * <span class="ja">
   * 言語配列に一番適用ローカライザーを戻します
   * 
   * @param languages - 言語配列セット
   * 
   * @return ローカライザー
   * </span>
	 */
	public static ERXLocalizer localizerForLanguages(NSArray<String> languages) {
		if (!isLocalizationEnabled)
			return createLocalizerForLanguage("Nonlocalized", false);

		if (languages == null || languages.isEmpty())
			return localizerForLanguage(defaultLanguage());
		
		ERXLocalizer l = null;
		Enumeration<String> e = languages.objectEnumerator();
		while (e.hasMoreElements()) {
			String language = e.nextElement();
			l = localizers.objectForKey(language);
			if (l != null) {
				return l;
			}
			if (availableLanguages().containsObject(language)) {
				return localizerForLanguage(language);
			}
			// try to do a fallback to the base language if this was regionalized
			int index = language.indexOf('_');
			if (index > 0) {
				language = language.substring(0, index);
				if (availableLanguages().containsObject(language)) {
					return localizerForLanguage(language);
				}
			}
		}
		return localizerForLanguage(languages.objectAtIndex(0));
	}

	private static NSArray<String> _languagesWithoutPluralForm = new NSArray<String>(new String[] { "Japanese" });

	/**
	 * <span class="en">
	 * Get a localizer for a specific language. If none could be found or language
	 * is <code>null</code> a localizer for the {@link #defaultLanguage()} is returned.
	 * 
	 * @param language name of the requested language
	 * @return localizer
	 * </span>
	 * 
   * <span class="ja">
   * 指定言語のローカライザーを戻します。language が null 又は見つからない場合には
   * {@link #defaultLanguage()} が戻ります。
   * 
   * @param language - 言語
   * 
   * @return ローカライザー
   * </span>
	 */
	public static ERXLocalizer localizerForLanguage(String language) {
		if (!isLocalizationEnabled)
			return createLocalizerForLanguage("Nonlocalized", false);

		if (language == null) {
			language = defaultLanguage();
		}
		ERXLocalizer l = null;
		l = localizers.objectForKey(language);
		if (l == null) {
			if (availableLanguages().containsObject(language)) {
				if (_languagesWithoutPluralForm.containsObject(language))
					l = createLocalizerForLanguage(language, false);
				else
					l = createLocalizerForLanguage(language, true);
			}
			else {
				l = localizers.objectForKey(defaultLanguage());
				if (l == null) {
					if (_languagesWithoutPluralForm.containsObject(defaultLanguage()))
						l = createLocalizerForLanguage(defaultLanguage(), false);
					else
						l = createLocalizerForLanguage(defaultLanguage(), true);
					localizers.setObjectForKey(l, defaultLanguage());
				}
			}
			localizers.setObjectForKey(l, language);
		}
		return l;
	}

	/**
	 * <span class="en">
	 * Returns the default language (English) or the contents of the
	 * <code>er.extensions.ERXLocalizer.defaultLanguage</code> property.
	 * 
	 * @return default language name
	 * </span>
	 * 
   * <span class="ja">
   * デフォルト言語(English) や
   * <code>er.extensions.ERXLocalizer.defaultLanguage</code>プロパティー
   * を戻します
   * 
   * @return デフォルト言語
   * </span>
	 */
	public static String defaultLanguage() {
		if (defaultLanguage == null) {
			defaultLanguage = ERXProperties.stringForKeyWithDefault("er.extensions.ERXLocalizer.defaultLanguage", "English");
		}
		return defaultLanguage;
	}

	/**
	 * <span class="en">
	 * Sets the default language.
	 * 
	 * @param value
	 * </span>
	 * 
   * <span class="ja">
   * デフォルト言語をセットします
   * 
   * @param value - デフォルト言語
   * </span>
	 */
	public static void setDefaultLanguage(String value) {
		defaultLanguage = value;
		resetCache();
	}

  /**
   * <span class="ja">
   * ウォッチするファイル名配列を戻します
   * 
   * @return NSAray - ファイル名配列
   * </span>
   */
	public static NSArray<String> fileNamesToWatch() {
		if (fileNamesToWatch == null) {
			fileNamesToWatch = ERXProperties.arrayForKeyWithDefault("er.extensions.ERXLocalizer.fileNamesToWatch", new NSArray<String>(new String[] { "Localizable.strings", "ValidationTemplate.strings" }));
			if (log.isDebugEnabled())
        log.debug("FileNamesToWatch: " + fileNamesToWatch.componentsJoinedByString(" / "));
		}
		return fileNamesToWatch;
	}

  /**
   * <span class="ja">
   * ウォッチするファイル名をセットします
   * 
   * @param value - ファイル配列
   * </span>
   */
	public static void setFileNamesToWatch(NSArray<String> value) {
		fileNamesToWatch = value;
		resetCache();
	}

  /**
   * <span class="ja">
   * 使用可能な言語を配列として戻します
   * 
   * @return 使用可能な言語のNSArray
   * </span>
   */
	public static NSArray<String> availableLanguages() {
		if (availableLanguages == null) {
			availableLanguages = ERXProperties.arrayForKeyWithDefault("er.extensions.ERXLocalizer.availableLanguages", new NSArray(new String[] { "English", "German", "Japanese" }));
			if (log.isDebugEnabled())
        log.debug("AvailableLanguages: " + availableLanguages.componentsJoinedByString(" / "));
		}
		return availableLanguages;
	}

  /**
   * <span class="ja">
   * 使用可能な言語を配列でセットする
   * 
   * @param value - 使用可能な言語のNSArray
   * </span>
   */
	public static void setAvailableLanguages(NSArray<String> value) {
		availableLanguages = value;
		resetCache();
	}

  /**
   * <span class="ja">
   * フレームワーク検索パスの配列を戻します
   * 
   * @return フレームワーク検索パスの配列
   * </span>
   */
	public static NSArray<String> frameworkSearchPath() {
		if (frameworkSearchPath == null) {
		  frameworkSearchPath = ERXProperties.arrayForKey("er.extensions.ERXLocalizer.frameworkSearchPath");
		  if(frameworkSearchPath == null) {
		    NSMutableArray<String> defaultValue = new NSMutableArray<String>();
		    for (Enumeration<NSBundle> e = NSBundle.frameworkBundles().objectEnumerator(); e.hasMoreElements();) {
		      NSBundle bundle = e.nextElement();
		      String name = bundle.name();
		  
		      // Check the Properties and Add it Automatically
		      String propertyName = "er.extensions." + name + ".hasLocalization";
		      boolean hasLocalization = ERXProperties.booleanForKeyWithDefault(propertyName, true);
		  
          if(name.equals("ERCoreBusinessLogic") || name.equals("ERDirectToWeb") || name.equals("ERExtensions")){ //|| name.startsWith("Java")
            // do nothing yet, because will add later
          } else if(hasLocalization) { 
            defaultValue.addObject(name);
          }
				}
				if(NSBundle.bundleForName("ERCoreBusinessLogic") != null) 
					defaultValue.addObject("ERCoreBusinessLogic");
				if(NSBundle.bundleForName("ERDirectToWeb") != null) 
					defaultValue.addObject("ERDirectToWeb");
				if(NSBundle.bundleForName("ERExtensions") != null) 
					defaultValue.addObject("ERExtensions");
				defaultValue.insertObjectAtIndex("app", 0);
				frameworkSearchPath = defaultValue;
			}
			if (log.isDebugEnabled())
				log.debug("FrameworkSearchPath: " + frameworkSearchPath.componentsJoinedByString(" / "));
		}
		return frameworkSearchPath;
	}

  /**
   * <span class="ja">
   * フレームワーク検索パスをセットします
   * 
   * @param value - フレームワーク検索
   * </span>
   */
	public static void setFrameworkSearchPath(NSArray<String> value) {
		frameworkSearchPath = value;
		resetCache();
	}

	/**
	 * <span class="en">
	 * Creates a localizer for a given language and with an indication if the language supports plural forms. To provide
	 * your own subclass of an ERXLocalizer you can set the system property
	 * <code>er.extensions.ERXLocalizer.pluralFormClassName</code> or
	 * <code>er.extensions.ERXLocalizer.nonPluralFormClassName</code>.
	 * 
	 * @param language
	 *            name to construct the localizer for
	 * @param pluralForm
	 *            denotes if the language supports the plural form
	 * @return a localizer for the given language
	 * </span>
	 * 
   * <span class="ja">
   * 指定言語のローカライザーを作成します。さらに言語体系が複数を持つかどうかのフラグを持っています。
   * 自分専用のクラスを作成する為にプロパティーで設定が可能です。
   * 
   * <code>er.extensions.ERXLocalizer.pluralFormClassName</code> or
   * <code>er.extensions.ERXLocalizer.nonPluralFormClassName</code>.
   * 
   * @param language - 言語
   * @param pluralForm - true 言語体系が複数を使用している場合
   * 
   * @return ローカライザー
   * </span>
	 */
	protected static ERXLocalizer createLocalizerForLanguage(String language, boolean pluralForm) {
		ERXLocalizer localizer = null;
		String className = null;
		if (pluralForm) {
			className = ERXProperties.stringForKeyWithDefault("er.extensions.ERXLocalizer.pluralFormClassName", ERXLocalizer.class.getName());
		}
		else {
			className = ERXProperties.stringForKeyWithDefault("er.extensions.ERXLocalizer.nonPluralFormClassName", ERXNonPluralFormLocalizer.class.getName());
		}
		try {
			Class localizerClass = Class.forName(className);
			Constructor constructor = localizerClass.getConstructor(ERXConstant.StringClassArray);
			localizer = (ERXLocalizer) constructor.newInstance(new Object[] { language });
		}
		catch (Exception e) {
			log.error("Unable to create localizer for language \"" + language + "\" class name: " + className + " exception: " + e.getMessage() + ", will use default classes", e);
		}
		if (localizer == null) {
			if (pluralForm)
				localizer = new ERXLocalizer(language);
			else
				localizer = new ERXNonPluralFormLocalizer(language);
		}
		return localizer;
	}

  /**
   * <span class="ja">
   * ローカライザーをセットします
   * 
   * @param l - ERXLocalizer ローカライザー
   * @param language - 言語
   * </span>
   */
	public static void setLocalizerForLanguage(ERXLocalizer l, String language) {
		localizers.setObjectForKey(l, language);
	}

	protected NSMutableDictionary<String, Object> cache;
	private NSMutableDictionary<String, Object> createdKeys;
	private String NOT_FOUND = "**NOT_FOUND**";
	protected Hashtable<String, Format> _dateFormatters = new Hashtable<String, Format>();
	protected Hashtable<String, Format> _numberFormatters = new Hashtable<String, Format>();
	protected String language;
	protected Locale locale;
	
	private Map<Pattern, String> _plurifyRules;
	private Map<Pattern, String> _singularifyRules;

	public ERXLocalizer(String aLanguage) {
		_plurifyRules = new HashMap<Pattern, String>();
		_singularifyRules = new HashMap<Pattern, String>();
		
		language = aLanguage;
		cache = new NSMutableDictionary<String, Object>();
		createdKeys = new NSMutableDictionary<String, Object>();

		// We first check to see if we have a locale register for the language name
		String shortLanguage = ERXProperties.stringForKey("er.extensions.ERXLocalizer." + aLanguage + ".locale");

		// Let's go fishing
		if (shortLanguage == null) {
			NSDictionary<String, Object> dict = ERXDictionaryUtilities.dictionaryFromPropertyList("Languages", NSBundle.bundleForName("JavaWebObjects"));
			if (dict != null) {
				NSArray<String> keys = dict.allKeysForObject(aLanguage);
				if (keys.count() > 0) {
					shortLanguage = keys.objectAtIndex(0);
					if (keys.count() > 1) {
						log.info("Found multiple entries for language \"" + aLanguage + "\" in Language.plist file! Found keys: " + keys);
					}
				}
			}
			else {
				log.info("No Languages.plist found in JavaWebObjects bundle.");
			}
		}
		if (shortLanguage != null) {
			locale = new Locale(shortLanguage);
		}
		else {
			log.info("Locale for " + aLanguage + " not found! Using default locale: " + Locale.getDefault());
			locale = Locale.getDefault();
		}
		load();
	}

  /**
   * <span class="ja">
   * キャシュを戻します
   * 
   * @return NSDictionaryキャシュ
   * </span>
   */
	public NSDictionary<String, Object> cache() {
		return cache;
	}

  /** <span class="ja">ローカライザーのロード作業</span> */
	public void load() {
		cache.removeAllObjects();
		createdKeys.removeAllObjects();

		if (log.isDebugEnabled())
		  log.debug("Loading templates for language: " + language + " for files: " + fileNamesToWatch().componentsJoinedByString(" / ") + " with search path: " + frameworkSearchPath().componentsJoinedByString(" / "));

		NSArray<String> languages = new NSArray<String>(language);
		Enumeration<String> fn = fileNamesToWatch().objectEnumerator();
		while (fn.hasMoreElements()) {
			String fileName = fn.nextElement();
			Enumeration<String> fr = frameworkSearchPath().reverseObjectEnumerator();
			while (fr.hasMoreElements()) {
				String framework = fr.nextElement();

				URL path = ERXFileUtilities.pathURLForResourceNamed(fileName, framework, languages);
				if (path != null) {
					try {
						framework = "app".equals(framework) ? null : framework;
						if(log.isDebugEnabled())
						  log.debug("Loading: " + fileName + " - " + (framework == null ? "app" : framework) + " - " + languages.componentsJoinedByString(" / ") + " " + path);
						
						NSDictionary<String, Object> dict = (NSDictionary<String, Object>) ERXFileUtilities.readPropertyListFromFileInFramework(fileName, framework, languages);
						// HACK: ak we have could have a collision between the search path for validation strings and
						// the normal localized strings.
						if (fileName.indexOf(ERXValidationFactory.VALIDATION_TEMPLATE_PREFIX) == 0) {
							NSMutableDictionary<String, Object> newDict = new NSMutableDictionary<String, Object>();
							for (Enumeration<String> keys = dict.keyEnumerator(); keys.hasMoreElements();) {
								String key = keys.nextElement();
								newDict.setObjectForKey(dict.objectForKey(key), ERXValidationFactory.VALIDATION_TEMPLATE_PREFIX + key);
							}
							dict = newDict;
						}
						addEntriesToCache(dict);
						if (!WOApplication.application().isCachingEnabled()) {
							synchronized (monitoredFiles) {
								if (!monitoredFiles.containsObject(path)) {
									ERXFileNotificationCenter.defaultCenter().addObserver(observer, new NSSelector("fileDidChange", ERXConstant.NotificationClassArray), path.getFile());
									monitoredFiles.addObject(path);
								}
							}
						}
					}
					catch (Exception ex) {
            log.warn("Exception loading: " + fileName + " - " + (framework == null ? "app" : framework) + " - " + languages.componentsJoinedByString(" / ") + ":" + ex, ex);
					}
				}
				else {
				  if(log.isDebugEnabled())
				    log.debug("Unable to create path for resource named: " + fileName + " framework: " + (framework == null ? "app" : framework) + " languages: " + languages.componentsJoinedByString(" / "));
				}
			}
		}
		
		_plurifyRules = plurifyRules();
		_singularifyRules = singularifyRules();
	}

	/**
	 * <span class="en">
	 * Returns the plurify rules for the current language.  This first checks for a property of the form:
	 * 
	 * <code>
	 * er.extensions.ERXLocalizer.en.plurifyRules=(.*)person$=$1people:(.*)man$=$1men
	 * </code>
	 * 
	 * which is
	 * 
	 * <code>
	 * er.extensions.ERXLocalizer.en.plurifyRules=pattern1=replacement1:pattern2=replacement2:etc
	 * </code>
	 * 
	 * In the absence of a rule set for a particular language, the default rules are English and ported
	 * from the pluralizer in Rails.
	 * 
	 * @return the plurify rules for the current language
	 * </span>
	 * 
   * <span class="ja">
   * カレント言語の複数形ルールを戻します。
   * 最初はプロパティーをチェックします。
   * 
   * <code>
   * er.extensions.ERXLocalizer.en.plurifyRules=(.*)person$=$1people:(.*)man$=$1men
   * </code>
   * 
   * が次のように
   * 
   * <code>
   * er.extensions.ERXLocalizerr.en.plurifyRules=pattern1=replacement1:pattern2=replacement2:etc
   * </code>
   * 
   * 各言語のルール不足分で、デフォルト・ルールは英語です。
   * 
   * @return カレント言語の複数形ルールを戻します。
   * </span>
	 */
	protected Map<Pattern, String> plurifyRules() {
		Map<Pattern, String> plurifyRules;
		String plurifyRulesStr = ERXProperties.stringForKeyWithDefault("er.extensions.ERXLocalizer." + language + ".plurifyRules", null);
		if (plurifyRulesStr == null) {
			plurifyRules = defaultPlurifyRules();
		}
		else {
			plurifyRules = new LinkedHashMap<Pattern, String>();
			String[] rulePairs = plurifyRulesStr.split(":");
			for (int i = 0; i < rulePairs.length; i ++) {
				String[] rulePair = rulePairs[i].split("=");
				Pattern pattern = Pattern.compile(rulePair[0], Pattern.CASE_INSENSITIVE);
				plurifyRules.put(pattern, rulePair[1]);
			}
		}
		return plurifyRules;
	}
	
	/**
	 * <span class="en">
	 * Returns the default plurify rules for this language.  The default implementation is 
	 * English and ported from the plurify code in Ruby on Rails.  The returned Map should
	 * have regex Pattern objects as keys mapping to the replacement String to apply to
	 * that pattern. 
	 * 
	 * @return the default plurify rules
	 * </span>
	 * 
   * <span class="ja">
   * デフォルト複数形ルールを戻します。
   * デフォルト実装は英語です。 戻り値のマップは regex パターンのキー・マッピングです。
   * 
   * @return デフォルト複数形ルールを戻します
   * </span>
	 */
	protected Map<Pattern, String> defaultPlurifyRules() {
		Map<Pattern, String> defaultPlurifyRules = new LinkedHashMap<Pattern, String>();

		defaultPlurifyRules.put(Pattern.compile("^equipment$", Pattern.CASE_INSENSITIVE), "equipment");
		defaultPlurifyRules.put(Pattern.compile("^information$", Pattern.CASE_INSENSITIVE), "information");
		defaultPlurifyRules.put(Pattern.compile("^rice$", Pattern.CASE_INSENSITIVE), "rice");
		defaultPlurifyRules.put(Pattern.compile("^money$", Pattern.CASE_INSENSITIVE), "money");
		defaultPlurifyRules.put(Pattern.compile("^species$", Pattern.CASE_INSENSITIVE), "species");
		defaultPlurifyRules.put(Pattern.compile("^series$", Pattern.CASE_INSENSITIVE), "series");
		defaultPlurifyRules.put(Pattern.compile("^fish$", Pattern.CASE_INSENSITIVE), "fish");
		defaultPlurifyRules.put(Pattern.compile("^sheep$", Pattern.CASE_INSENSITIVE), "sheep");

		defaultPlurifyRules.put(Pattern.compile("(.*)person$", Pattern.CASE_INSENSITIVE), "$1people");
		defaultPlurifyRules.put(Pattern.compile("(.*)man$", Pattern.CASE_INSENSITIVE), "$1men");
		defaultPlurifyRules.put(Pattern.compile("(.*)child$", Pattern.CASE_INSENSITIVE), "$1children");
		defaultPlurifyRules.put(Pattern.compile("(.*)sex$", Pattern.CASE_INSENSITIVE), "$1sexes");
		defaultPlurifyRules.put(Pattern.compile("(.*)move$", Pattern.CASE_INSENSITIVE), "$1moves");
		
		defaultPlurifyRules.put(Pattern.compile("(.*)(quiz)$", Pattern.CASE_INSENSITIVE), "$1$2zes");
		defaultPlurifyRules.put(Pattern.compile("(.*)^(ox)$", Pattern.CASE_INSENSITIVE), "$1$2en");
		defaultPlurifyRules.put(Pattern.compile("(.*)([m|l])ouse$", Pattern.CASE_INSENSITIVE), "$1$2ice");
		defaultPlurifyRules.put(Pattern.compile("(.*)(matr|vert|ind)ix|ex$", Pattern.CASE_INSENSITIVE), "$1$2ices");
		defaultPlurifyRules.put(Pattern.compile("(.*)(x|ch|ss|sh)$", Pattern.CASE_INSENSITIVE), "$1$2es");
		defaultPlurifyRules.put(Pattern.compile("(.*)([^aeiouy]|qu)y$", Pattern.CASE_INSENSITIVE), "$1$2ies");
		defaultPlurifyRules.put(Pattern.compile("(.*)(hive)$", Pattern.CASE_INSENSITIVE), "$1$2s");
		defaultPlurifyRules.put(Pattern.compile("(.*)(?:([^f])fe|([lr])f)$", Pattern.CASE_INSENSITIVE), "$1$2$3ves");
		defaultPlurifyRules.put(Pattern.compile("(.*)sis$", Pattern.CASE_INSENSITIVE), "$1ses");
		defaultPlurifyRules.put(Pattern.compile("(.*)([ti])um$", Pattern.CASE_INSENSITIVE), "$1$2a");
		defaultPlurifyRules.put(Pattern.compile("(.*)(buffal|tomat)o$", Pattern.CASE_INSENSITIVE), "$1$2oes");
		defaultPlurifyRules.put(Pattern.compile("(.*)(bu)s$", Pattern.CASE_INSENSITIVE), "$1$2ses");
		defaultPlurifyRules.put(Pattern.compile("(.*)(alias|status)$", Pattern.CASE_INSENSITIVE), "$1$2es");
		defaultPlurifyRules.put(Pattern.compile("(.*)(octop|vir)us$", Pattern.CASE_INSENSITIVE), "$1$2i");
		defaultPlurifyRules.put(Pattern.compile("(.*)(ax|test)is$", Pattern.CASE_INSENSITIVE), "$1$2es");
		defaultPlurifyRules.put(Pattern.compile("(.*)s$", Pattern.CASE_INSENSITIVE), "$1s");
		defaultPlurifyRules.put(Pattern.compile("(.*)$", Pattern.CASE_INSENSITIVE), "$1s");

		return defaultPlurifyRules;
	}

	/**
	 * <span class="en">
	 * Returns the singularify rules for the current language.  This first checks for a property of the form:
	 * 
	 * <code>
	 * er.extensions.ERXLocalizer.en.singularifyRules=(.*)person$=$1people:(.*)man$=$1men
	 * </code>
	 * 
	 * which is
	 * 
	 * <code>
	 * er.extensions.ERXLocalizer.en.singularifyRules=pattern1=replacement1:pattern2=replacement2:etc
	 * </code>
	 * 
	 * In the absence of a rule set for a particular language, the default rules are English and ported
	 * from the singularizer in Rails.
	 * 
	 * @return the singularify rules for the current language
	 * </span>
	 * 
   * <span class="ja">
   * カレント言語の単数形ルールを戻します。
   * 最初はプロパティーをチェックします。
   * 
   * <code>
   * er.extensions.ERXLocalizer.en.singularifyRules=(.*)person$=$1people:(.*)man$=$1men
   * </code>
   * 
   * が次のように
   * 
   * <code>
   * er.extensions.ERXLocalizer.en.singularifyRules=pattern1=replacement1:pattern2=replacement2:etc
   * </code>
   * 
   * 各言語のルール不足分で、デフォルト・ルールは英語です。
   * 
   * @return カレント言語の単数形ルールを戻します。
   * </span>
	 */
	protected Map<Pattern, String> singularifyRules() {
		Map<Pattern, String> singularifyRules;
		String plurifyRulesStr = ERXProperties.stringForKeyWithDefault("er.extensions.ERXLocalizer." + language + ".singularifyRules", null);
		if (plurifyRulesStr == null) {
			singularifyRules = defaultSingularifyRules();
		}
		else {
			singularifyRules = new LinkedHashMap<Pattern, String>();
			String[] rulePairs = plurifyRulesStr.split(":");
			for (int i = 0; i < rulePairs.length; i ++) {
				String[] rulePair = rulePairs[i].split("=");
				Pattern pattern = Pattern.compile(rulePair[0], Pattern.CASE_INSENSITIVE);
				singularifyRules.put(pattern, rulePair[1]);
			}
		}
		return singularifyRules;
	}
	
	/**
	 * <span class="en">
	 * Returns the default singularify rules for this language.  The default implementation is 
	 * English and ported from the singularize code in Ruby on Rails.  The returned Map should
	 * have regex Pattern objects as keys mapping to the replacement String to apply to
	 * that pattern. 
	 * 
	 * @return the default singularify rules
	 * </span>
	 * 
   * <span class="ja">
   * デフォルト単数形ルールを戻します。
   * デフォルト実装は英語です。 戻り値のマップは regex パターンのキー・マッピングです。
   * 
   * @return デフォルト単数形ルールを戻します
   * </span>
	 */
	protected Map<Pattern, String> defaultSingularifyRules() {
		Map<Pattern, String> defaultSingularifyRules = new LinkedHashMap<Pattern, String>();

		defaultSingularifyRules.put(Pattern.compile("^equipment$", Pattern.CASE_INSENSITIVE), "equipment");
		defaultSingularifyRules.put(Pattern.compile("^information$", Pattern.CASE_INSENSITIVE), "information");
		defaultSingularifyRules.put(Pattern.compile("^rice$", Pattern.CASE_INSENSITIVE), "rice");
		defaultSingularifyRules.put(Pattern.compile("^money$", Pattern.CASE_INSENSITIVE), "money");
		defaultSingularifyRules.put(Pattern.compile("^species$", Pattern.CASE_INSENSITIVE), "species");
		defaultSingularifyRules.put(Pattern.compile("^series$", Pattern.CASE_INSENSITIVE), "series");
		defaultSingularifyRules.put(Pattern.compile("^fish$", Pattern.CASE_INSENSITIVE), "fish");
		defaultSingularifyRules.put(Pattern.compile("^sheep$", Pattern.CASE_INSENSITIVE), "sheep");

		defaultSingularifyRules.put(Pattern.compile("(.*)people$", Pattern.CASE_INSENSITIVE), "$1person");
		defaultSingularifyRules.put(Pattern.compile("(.*)men$", Pattern.CASE_INSENSITIVE), "$1man");
		defaultSingularifyRules.put(Pattern.compile("(.*)children$", Pattern.CASE_INSENSITIVE), "$1child");
		defaultSingularifyRules.put(Pattern.compile("(.*)sexes$", Pattern.CASE_INSENSITIVE), "$1sex");
		defaultSingularifyRules.put(Pattern.compile("(.*)moves$", Pattern.CASE_INSENSITIVE), "$1move");

		defaultSingularifyRules.put(Pattern.compile("(.*)(quiz)zes$", Pattern.CASE_INSENSITIVE), "$1$2");
		defaultSingularifyRules.put(Pattern.compile("(.*)(matr)ices$", Pattern.CASE_INSENSITIVE), "$1$2ix");
		defaultSingularifyRules.put(Pattern.compile("(.*)(vert|ind)ices$", Pattern.CASE_INSENSITIVE), "$1$2ex");
		defaultSingularifyRules.put(Pattern.compile("(.*)^(ox)en", Pattern.CASE_INSENSITIVE), "$1$2");
		defaultSingularifyRules.put(Pattern.compile("(.*)(alias|status)es$", Pattern.CASE_INSENSITIVE), "$1$2");
		defaultSingularifyRules.put(Pattern.compile("(.*)(octop|vir)i$", Pattern.CASE_INSENSITIVE), "$1$2us");
		defaultSingularifyRules.put(Pattern.compile("(.*)(cris|ax|test)es$", Pattern.CASE_INSENSITIVE), "$1$2is");
		defaultSingularifyRules.put(Pattern.compile("(.*)(shoe)s$", Pattern.CASE_INSENSITIVE), "$1$2");
		defaultSingularifyRules.put(Pattern.compile("(.*)(o)es$", Pattern.CASE_INSENSITIVE), "$1$2");
		defaultSingularifyRules.put(Pattern.compile("(.*)(bus)es$", Pattern.CASE_INSENSITIVE), "$1$2");
		defaultSingularifyRules.put(Pattern.compile("(.*)([m|l])ice$", Pattern.CASE_INSENSITIVE), "$1$2ouse");
		defaultSingularifyRules.put(Pattern.compile("(.*)(x|ch|ss|sh)es$", Pattern.CASE_INSENSITIVE), "$1$2");
		defaultSingularifyRules.put(Pattern.compile("(.*)(m)ovies$", Pattern.CASE_INSENSITIVE), "$1$2ovie");
		defaultSingularifyRules.put(Pattern.compile("(.*)(s)eries$", Pattern.CASE_INSENSITIVE), "$1$2eries");
		defaultSingularifyRules.put(Pattern.compile("(.*)([^aeiouy]|qu)ies$", Pattern.CASE_INSENSITIVE), "$1$2y");
		defaultSingularifyRules.put(Pattern.compile("(.*)([lr])ves$", Pattern.CASE_INSENSITIVE), "$1$2f");
		defaultSingularifyRules.put(Pattern.compile("(.*)(tive)s$", Pattern.CASE_INSENSITIVE), "$1$2");
		defaultSingularifyRules.put(Pattern.compile("(.*)(hive)s$", Pattern.CASE_INSENSITIVE), "$1$2");
		defaultSingularifyRules.put(Pattern.compile("(.*)([^f])ves$", Pattern.CASE_INSENSITIVE), "$1$2fe");
		defaultSingularifyRules.put(Pattern.compile("(.*)(^analy)ses$", Pattern.CASE_INSENSITIVE), "$1$2sis");
		defaultSingularifyRules.put(Pattern.compile("(.*)((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", Pattern.CASE_INSENSITIVE), "$1$2$3sis");
		defaultSingularifyRules.put(Pattern.compile("(.*)([ti])a$", Pattern.CASE_INSENSITIVE), "$1$2um");
		defaultSingularifyRules.put(Pattern.compile("(.*)(n)ews$", Pattern.CASE_INSENSITIVE), "$1$2ews");
		defaultSingularifyRules.put(Pattern.compile("(.*)s$", Pattern.CASE_INSENSITIVE), "$1");

		return defaultSingularifyRules;
	}

	protected void addEntriesToCache(NSDictionary<String, Object> dict) {
		try {
			// try-catch to prevent potential CCE when the value for the key localizerExcepions is not an NSDictionary
			NSDictionary<String, Object> currentLEs = (NSDictionary<String, Object>) cache.valueForKey(KEY_LOCALIZER_EXCEPTIONS);
			NSDictionary<String, Object> newLEs = (NSDictionary<String, Object>) dict.valueForKey(KEY_LOCALIZER_EXCEPTIONS);
			if (currentLEs != null && newLEs != null) {
				if (log.isDebugEnabled())
					log.debug("Merging localizerExceptions " + currentLEs + " with " + newLEs);
				NSMutableDictionary<String, Object> combinedLEs = currentLEs.mutableClone();
				combinedLEs.addEntriesFromDictionary(newLEs);
				NSMutableDictionary<String, Object> replacementDict = dict.mutableClone();
				replacementDict.takeValueForKey(combinedLEs, KEY_LOCALIZER_EXCEPTIONS);
				dict = replacementDict;
				if (log.isDebugEnabled())
					log.debug("Result of merge: " + combinedLEs);
			}
		}
		catch (RuntimeException e) {
			log.error("Error while adding enties to cache", e);
		}

		cache.addEntriesFromDictionary(dict);
	}

	protected NSDictionary readPropertyListFromFileInFramework(String fileName, String framework, NSArray<String> languages) {
		return (NSDictionary) ERXFileUtilities.readPropertyListFromFileInFramework(fileName, framework, languages);
	}

	/**
	 * Cover method that calls <code>localizedStringForKey</code>.
	 * 
	 * @param key
	 *            to resolve a localized varient of
	 * @return localized string for the given key
	 */
	public Object valueForKey(String key) {
		return valueForKeyPath(key);
	}

	protected void setCacheValueForKey(Object value, String key) {
		if (key != null && value != null) {
			cache.setObjectForKey(value, key);
		}
	}

	public Object valueForKeyPath(String key) {
		Object result = localizedValueForKey(key);
		if (result == null) {
			int indexOfDot = key.indexOf(".");
			if (indexOfDot > 0) {
				String firstComponent = key.substring(0, indexOfDot);
				String otherComponents = key.substring(indexOfDot + 1, key.length());
				result = cache.objectForKey(firstComponent);
				if (log.isDebugEnabled()) {
					log.debug("Trying " + firstComponent + " . " + otherComponents);
				}
				if (result != null) {
					try {
						result = NSKeyValueCodingAdditions.Utility.valueForKeyPath(result, otherComponents);
						if (result != null) {
							setCacheValueForKey(result, key);
						}
						else {
							setCacheValueForKey(NOT_FOUND, key);
						}
					}
					catch (NSKeyValueCoding.UnknownKeyException e) {
						if (log.isDebugEnabled()) {
							log.debug(e.getMessage());
						}
						setCacheValueForKey(NOT_FOUND, key);
					}
				}
			}
		}
		return result;
	}

	public void takeValueForKey(Object value, String key) {
		setCacheValueForKey(value, key);
		addToCreatedKeys(value, key);
	}

	public void takeValueForKeyPath(Object value, String key) {
		setCacheValueForKey(value, key);
		addToCreatedKeys(value, key);
	}

  /** 
   * <span class="ja">言語を戻します</span> 
   */
	public String language() {
		return language;
	}

  /**
   * <span class="ja">
   * 作成されているキーをdictionaryとして戻します
   * 
   * @return NSDictionary
   * </span>
   */
	public NSDictionary<String, Object> createdKeys() {
		return createdKeys;
	}

  /**
   * <span class="ja">
   * キー・リストをダンプします
   * </span>
   */
	public void dumpCreatedKeys() {
	  if(log.isInfoEnabled())
	    log.info(NSPropertyListSerialization.stringFromPropertyList(createdKeys()));
	}

  /**
   * <span class="ja">
   * キーを使って、ローカライズ・オブジェクトを戻します
   * {@literal @} キーパス <code>session.localizer.{@literal @}locale.getLanguage</code> で
   * キーファイルを探すかわりに ERXLocalizer メソッドを実行します。
   * @param key - キー
   * 
   * @return ローカライズ済みオブジェクト又は @ キーパス
   * </span>
   */
	public Object localizedValueForKeyWithDefault(String key) {
		if (key == null) {
			log.warn("Attempt to insert null key!");
			return null;
		}
		Object result = localizedValueForKey(key);
		if (result == null || NOT_FOUND.equals(result)) {
			if (createdKeysLog.isDebugEnabled()) {
				createdKeysLog.debug("Default key inserted: '" + key + "'/" + language);
			}
			setCacheValueForKey(key, key);
			addToCreatedKeys(key, key);
			result = key;
		}
		return result;
	}

	/**
	 * <span class="en">
	 * Returns the localized value for a key. An {@literal @} keypath such as 
	 * <code>session.localizer.{@literal @}locale.getLanguage</code> indicates that
	 * the methods on ERXLocalizer itself should be called instead of
	 * searching the strings file for a '{@literal @}locale.getLanguage' key.
	 * @param key the keyPath string
	 * @return a localized string value or the object value of the @ keyPath
	 * </span>
	 * 
   * <span class="ja">
   * キーを使って、ローカライズ・オブジェクトを戻します
   * {@literal @} キーパス <code>session.localizer.@locale.getLanguage</code> で
   * キーファイルを探すかわりに ERXLocalicer メソッドを実行します。
   * @param key - キー
   * 
   * @return ローカライズ済みオブジェクト又は @ キーパス
   * </span>
	 */
	public Object localizedValueForKey(String key) {
		if(!ERXStringUtilities.stringIsNullOrEmpty(key) && _localizerMethodIndicatorCharacter == key.charAt(0)) {
			int dotIndex = key.indexOf(NSKeyValueCodingAdditions.KeyPathSeparator);
			String methodKey = (dotIndex>0)?key.substring(1, dotIndex):key.substring(1, key.length());
      
      // KI : This can make bad invoke Errors in D2W Apps, when Rules are like '@count'
      // If the key is one of operatorNames then don't invoke it.
      if(!NSArray.operatorNames().contains(methodKey)) {
			try {
				Method m = ERXLocalizer.class.getMethod(methodKey);
				return m.invoke(this, (Object[])null);
			} catch(NoSuchMethodException nsme) {
				throw NSForwardException._runtimeExceptionForThrowable(nsme);
			} catch(IllegalAccessException iae) {
				throw NSForwardException._runtimeExceptionForThrowable(iae);
			} catch(InvocationTargetException ite) {
				throw NSForwardException._runtimeExceptionForThrowable(ite);
			}
		}
    }
		Object result = cache.objectForKey(key);
		if (key == null || result == NOT_FOUND)
			return null;
		if (result != null)
			return result;

		if (createdKeysLog.isDebugEnabled()) {
		  if(log.isDebugEnabled())
		    log.debug("Key not found: '" + key + "'/" + language);
		}
		if (fallbackToDefaultLanguage() && !defaultLanguage().equals(language)) {
			Object valueInDefaultLanguage = defaultLocalizer().localizedValueForKey(key);
			setCacheValueForKey(valueInDefaultLanguage == null ? NOT_FOUND : valueInDefaultLanguage, key);
			return valueInDefaultLanguage;
		}
		setCacheValueForKey(NOT_FOUND, key);
		return null;
	}

  /**
   * <span class="ja">
   * 指定キーのローカライズ済みの文字列を戻します
   * 
   * @param key - キー
   * 
   * @return ローカライズ済み String
   * </span>
   */
	public String localizedStringForKeyWithDefault(String key) {
		return (String) localizedValueForKeyWithDefault(key);
	}

  /**
   * <span class="ja">
   * 指定キーのローカライズ済みの文字列を戻します
   * 
   * @param key - キー
   * 
   * @return ローカライズ済み String
   * </span>
   */
	public String localizedStringForKey(String key) {
		return (String) localizedValueForKey(key);
	}

	private String displayNameForKey(String key) {
		return ERXStringUtilities.displayNameForKey(key);
	}

	/**
	 * <span class="en">
	 * Returns a localized string for the given prefix and keyPath, inserting it "prefix.keyPath" = "Key Path"; Also
	 * tries to find "Key Path"
	 * 
	 * @param prefix
	 * @param key
	 * 
	 * @return the localized string
	 * </span>
	 * 
   * <span class="ja">
   * 指定の prefix と key のローカライズ済みの文字列を戻します。<br />
   * 例えば、 <br />
   * s = <<ERXLocalizer>>.localizedDisplayNameForKey( "Tester", "samplekey" )<br />
   * <br />
   * Tester.samplekey になります<br />
   * 　　あれば、そのローカライズ済み String が戻ります<br />
   * 　　なければ、ERXStringUtilities.displayNameForKey処理が行われ、<br />
   * Tester.samplekey -> Samplekey になり、再度ローカライザーへ問い合わせが行きます。<br />
   * 　　あれば、そのローカライズ済み String が戻ります<br />
   * 　　なければ、キーが戻ります。<br />
   * 
   * @param prefix - 
   * @param key - キー
   * 
   * @return　ローカライズ済み String
   * </span>
	 */
	public String localizedDisplayNameForKey(String prefix, String key) {
		String localizerKey = prefix + "." + key;
		String result = localizedStringForKey(localizerKey);
		if (result == null) {
			result = displayNameForKey(key);
			String localized = localizedStringForKey(result);
			if (localized != null) {
				result = localized;
				if(log.isInfoEnabled())
				  log.info("Found an old-style entry: " + localizerKey + "->" + result);
			}
			takeValueForKey(result, localizerKey);
		}
		return result;
	}
	
	/**
	 * Returns a localized string for a given entity class description. If the entity
	 * uses inheritance it will traverse the inheritance chain until it finds a
	 * localization.
	 * 
	 * @param classDescription the entity class description
	 * @param key the attribute name
	 * @return the localized string
	 */
	public String localizedDisplayNameForKey(EOClassDescription classDescription, String key) {
		String result = null;
		if (classDescription instanceof EOEntityClassDescription) {
			EOEntity entity = ((EOEntityClassDescription) classDescription).entity();
			while (entity != null && result == null) {
				result = localizedStringForKey(entity.name() + "." + key);
				entity = entity.parentEntity();
			}
		}
		if (result == null) {
			// fallback
			result = localizedDisplayNameForKey(classDescription.entityName(), key);
		}
		return result;
	}

  /**
   * <span class="ja">
   * 指定のキーのローカライズ済み文字列を取得し、ERXSimpleTemplateParserに処理させてから戻します
   * 
   * @param key - キー
   * @param o1 - オブジェクト
   * 
   * @return ERXSimpleTemplateParser処理されているローカライズ済み文字列
   * </span>
   */
	public String localizedTemplateStringForKeyWithObject(String key, Object o1) {
		return localizedTemplateStringForKeyWithObjectOtherObject(key, o1, null);
	}

  /**
   * <span class="ja">
   * 指定のキーのローカライズ済み文字列を取得し、ERXSimpleTemplateParserに処理させてから戻します
   * 
   * @param key - キー
   * @param o1 - オブジェクト
   * @param o2 - オブジェクト
   * 
   * @return ERXSimpleTemplateParser処理されているローカライズ済み文字列
   * </span>
   */
	public String localizedTemplateStringForKeyWithObjectOtherObject(String key, Object o1, Object o2) {
		if (key != null) {
			String template = localizedStringForKeyWithDefault(key);
			if (template != null)
				return ERXSimpleTemplateParser.sharedInstance().parseTemplateWithObject(template, null, o1, o2);
		}
		return key;
	}

	protected String plurify(String str, int howMany) {
		String plurifiedString;
		if (howMany == 1 || howMany == -1) {
			plurifiedString = str;
		}
		else {
			plurifiedString = applyRules(str, _plurifyRules);
		}
		return plurifiedString;
	}

	protected String singularify(String str) {
		return applyRules(str, _singularifyRules);
	}

	/**
	 * Apply the set of rules in the given Map to the input String and return
	 * a modified string that matches the case of the input string.  For instance,
	 * if the input string is "Person" and the rules are _plurifyRules, then this
	 * would return "People".
	 *   
	 * @param str the input string
	 * @param rules the rules to apply
	 * @return a case-matched string converted according to the rules
	 */
	protected String applyRules(String str, Map<Pattern, String> rules) {
		String result = str;
		if (str != null) {
			boolean converted = false;
			Iterator<Map.Entry<Pattern, String>> rulesIter = rules.entrySet().iterator();
			while (!converted && rulesIter.hasNext()) {
				Map.Entry<Pattern, String> rule = rulesIter.next();
				Pattern rulePattern = rule.getKey();
				Matcher ruleMatcher = rulePattern.matcher(str);
				if (ruleMatcher.matches()) {
					String ruleReplacement = rule.getValue(); 
					result = ruleMatcher.replaceFirst(ruleReplacement);
					converted = true;
				}
			}
			if (converted) {
				result = ERXStringUtilities.matchCase(str, result);
			}
		}
		return result;
	}

	// name is already localized!
	// subclasses can override for more sensible behaviour
	public String plurifiedStringWithTemplateForKey(String key, String name, int count, Object helper) {
		NSDictionary<String, Object> dict = new NSDictionary<String, Object>(new Object[] { plurifiedString(name, count), Integer.valueOf(count) }, 
				new String[] { "pluralString", "pluralCount" });
		return localizedTemplateStringForKeyWithObjectOtherObject(key, dict, helper);
	}
	
	/**
	 * <span class="en">
	 * Returns a plurified string.
	 * 
	 * @param name the string to plurify
	 * @param count number to determine the plural form
	 * 
	 * @return plurified string
	 * </span>
	 * 
   * <span class="ja">
   * 複数形処理されている文字列を戻します
   * 
   * @param name - 名称
   * @param count - カウント
   * 
   * @return 複数形処理されている文字列
   * </span>
	 */
	public String plurifiedString(String name, int count) {
		if (name != null) {
			NSKeyValueCoding exceptions = (NSKeyValueCoding) valueForKey(KEY_LOCALIZER_EXCEPTIONS);
			if (exceptions != null) {
				String exception = (String) exceptions.valueForKey(name + "." + count);
				if (exception == null) {
					exception = (String) exceptions.valueForKey(name);
				}
				if (exception != null) {
					return exception;
				}
			}
		}
		return plurify(name, count);
	}

	/**
	 * <span class="en">
	 * Returns a singularified string
	 * 
	 * @param value
	 *            the value to singularify
	 * @return a singularified string
	 * </span>
	 * 
   * <span class="ja">
   * 単数形処理されている文字列を戻します
   * 
   * @param value - 単数にする String
   * 
   * @return 単数形処理されている文字列
   * </span>
	 */
	public String singularifiedString(String value) {
		if (value != null) {
			NSKeyValueCoding exceptions = (NSKeyValueCoding) valueForKey(KEY_LOCALIZER_EXCEPTIONS);
			if (exceptions != null) {
				String exception = (String) exceptions.valueForKey(value + ".singular");
				if (exception != null) {
					return exception;
				}
			}
		}
		return singularify(value);
	}

  /**
   * <span class="ja">
   * このクラスの文字列表現
   * 
   * @return 文字列表現
   * </span>
   */
	@Override
	public String toString() {
		return "<" + getClass().getName() + " " + language + ">";
	}

	/**
	 * <span class="en">
	 * Returns a localized date formatter for the given key.
	 * 
	 * @param formatString
	 * @return the formatter object
	 * </span>
	 * 
   * <span class="ja">
   * ローカライズ済み NumberFormatter を指定キーを使って戻します。
   * Localizable.strings 内でキーとパターンを定義することもできます。
   * 
   * @param formatString - フォーマット表記
   * 
   * @return フォーマット・オブジェクト
   * </span>
	 */
	public Format localizedDateFormatForKey(String formatString) {
		formatString = formatString == null ? ERXTimestampFormatter.DEFAULT_PATTERN : formatString;
		formatString = localizedStringForKeyWithDefault(formatString);
		Format result = _dateFormatters.get(formatString);
		if (result == null) {
			Locale current = locale();
			NSTimestampFormatter formatter = new NSTimestampFormatter(formatString, new DateFormatSymbols(current));
			result = formatter;
			_dateFormatters.put(formatString, result);
		}
		return result;
	}

	/**
	 * <span class="en">
	 * Returns a localized number formatter for the given key. Also, can localize units to, just define in your
	 * Localizable.strings a suitable key, with the appropriate pattern.
	 * 
	 * @param formatString
	 * @return the formatter object
	 * </span>
	 * 
   * <span class="ja">
   * ローカライズ済み NumberFormatter を指定キーを使って戻します。
   * Localizable.strings 内でキーとパターンを定義することもできます。
   * 
   * @param formatString - フォーマット表記
   * 
   * @return フォーマット・オブジェクト
   * </span>
	 */
	public Format localizedNumberFormatForKey(String formatString) {
		formatString = formatString == null ? "#,##0.00;-(#,##0.00)" : formatString;
		formatString = localizedStringForKeyWithDefault(formatString);
		Format result = _numberFormatters.get(formatString);
		if (result == null) {
			Locale current = locale();
			NSNumberFormatter formatter = new ERXNumberFormatter();
			formatter.setLocale(current);
			formatter.setLocalizesPattern(true);
			formatter.setPattern(formatString);
			result = formatter;
			_numberFormatters.put(formatString, result);
		}
		return result;
	}

	/**
	 * <span class="en">
	 * @param formatter
	 * @param pattern
	 * </span>
	 * 
   * <span class="ja">
   * ローカライズ済み NumberFormatter を保存します。
   * 
   * @param formatter - フォーマット
   * @param pattern - パターン
   * </span>
	 */
	public void setLocalizedNumberFormatForKey(Format formatter, String pattern) {
		_numberFormatters.put(pattern, formatter);
	}

  /** 
   * <span class="ja">ロケールをセット</span>
   */
	public Locale locale() {
		return locale;
	}

  /** 
   * <span class="ja">ロケールをゲット</span>
   */
	public void setLocale(Locale value) {
		locale = value;
	}

	/**
	 * <span class="en">
	 * @param formatter
	 * @param pattern
	 * </span>
	 * 
   * <span class="ja">
   * ローカライズ済み TimestampFormatter を保存します。
   * 
   * @param formatter - フォーマット
   * @param pattern - パターン
   * </span>
	 */
	public void setLocalizedDateFormatForKey(NSTimestampFormatter formatter, String pattern) {
		_dateFormatters.put(pattern, formatter);
	}

  /**
   * <span class="ja">
   * ローカライズ・フォーマッタを使用するかどうかを戻します
   * 
   * @return <code>true</code> ローカライズ・フォーマッタを使用する場合
   * </span>
   */
	public static boolean useLocalizedFormatters() {
		if (_useLocalizedFormatters == null) {
			_useLocalizedFormatters = ERXProperties.booleanForKey("er.extensions.ERXLocalizer.useLocalizedFormatters") ? Boolean.TRUE : Boolean.FALSE;
		}
		return _useLocalizedFormatters.booleanValue();
	}

  /**
   * <span class="ja">
   * 言語コードを戻します
   * 
   * @return 言語コード
   * </span>
   */
	public String languageCode() {
		return locale().getLanguage();
	}
	
  /**
   * <span class="ja">
   * デフォルト言語に戻ります。
   * 
   * @return true の場合にはデフォルト言語に戻ります、
   * </span>
   */
	public static boolean fallbackToDefaultLanguage() {
		if (_fallbackToDefaultLanguage == null) {
			_fallbackToDefaultLanguage = ERXProperties.booleanForKey("er.extensions.ERXLocalizer.fallbackToDefaultLanguage") ? Boolean.TRUE : Boolean.FALSE;
		}
		return _fallbackToDefaultLanguage.booleanValue();
	}
}
