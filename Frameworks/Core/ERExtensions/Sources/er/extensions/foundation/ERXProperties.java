/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.foundation;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSProperties;
import com.webobjects.foundation.NSPropertyListSerialization;

import er.extensions.appserver.ERXApplication;
import er.extensions.crypting.ERXCrypto;
import er.extensions.net.ERXTcpIp;

/**
 * <span class="en">
 * Collection of simple utility methods used to get and set properties
 * in the system properties. The only reason this class is needed is
 * because all of the methods in NSProperties have been deprecated.
 * This is a wee bit annoying. The usual method is to have a method
 * like <code>getBoolean</code> off of Boolean which would resolve
 * the System property as a Boolean object.
 * 
 * Properties can be set in all the following places:
 * <ul>
 * <li>Properties in a bundle Resources directory</li>
 * <li>Properties.dev in a bundle Resources directory</li>
 * <li>Properties.username in a bundle Resources directory </li>
 * <li>~/Library/WebObjects.properties file</li>
 * <li>in the eclipse launcher or on the command-line</li>
 * </ul>
 * 
 * @property er.extensions.ERXProperties.RetainDefaultsEnabled
 * </span>
 * 
 * <span class="ja">
 * 	システム・プロパティーのセットや取得するユーティリティー・メソッド集です。
 * 	このクラスが作成されている理由は、NSPropertiesの全メソッドが廃止になったからです。
 * 	普通はシステム・プロパティーの Boolean オブジェクトを扱う為に <code>getBoolean</code> などがあればいいのです。
 * 
 * @property er.extensions.ERXProperties.RetainDefaultsEnabled
 * </span>
 * 
 * @property NSProperties.useLoadtimeAppSpecifics Default is true.
 * 
 * TODO - Neither of these property names are standard. Should be camel-case and proper prefix.
 * 
 * TODO - What character sets can you use in property names? Only ISO-8859-1? UTF-8?
 * 
 * TODO - If this would fallback to calling the System getProperty, we could ask that Project Wonder frameworks only use this class.
 * 
 */
public class ERXProperties extends Properties implements NSKeyValueCoding {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** default string */
    public static final String DefaultString = "Default";
    
    private static Boolean RetainDefaultsEnabled;
    private static String UndefinedMarker = "-undefined-";

    /** logging support */
    public final static Logger log = Logger.getLogger(ERXProperties.class);
    private static final Map AppSpecificPropertyNames = new HashMap(128);

    /** WebObjects version number as string */
    private static String _webObjectsVersion;
    
    /** WebObjects version number as double */ 
    private static double _webObjectsVersionDouble;

    /** 
    * <span class="en">Internal cache of type converted values to avoid reconverting attributes that are asked for frequently</span> 
    * 
    * <span class="ja">タイプ変換されている値を内部でキャシュし、何回も同じ変換をする必要なくなります</span>
    */
    private static Map _cache = Collections.synchronizedMap(new HashMap());

    
    /**
     * This boolean controls the behavior of application specific properties. Setting this to
     * false makes the old behavior active, true activates the new behavior. The value of this
     * boolean is controlled by the property "NSProperties.useLoadtimeAppSpecifics" and defaults
     * to true. Please note this property MUST be defined with a -D argument on the application
     * launch command.
     * <p>
     * The old behavior will retain the original property names (including the application name),
     * and will search for an application specific version of each property every time someone
     * reads a property.
     * </p>
     * <p>
     * The new behavior will analyze all properties after being loaded from their source, and create
     * (or update) generic properties for each application specific one. So, if we are MyApp,
     * foo.bar.MyApp=4 will originate a new property foo.bar=4 (or, if foo.bar already exists,
     * update its value to 4). foo.bar.MyApp is also kept, because we cannot be sure foo.bar.MyApp
     * is an app specific property or a regular property with an ambiguous name.
     * </p>
     * <p>
     * The advantage of the new method is getting rid of the performance hit each time an app
     * accesses a property, and making application specific properties work for code that uses
     * the native Java System.getProperty call.
     * </p> 
     */
    public static final boolean _useLoadtimeAppSpecifics;

    /**
     * Set in flattenPropertyNames().
     *
     * The flattenPropertyNames() method is called from ERXSystem.updateProperties(), 
     *     which is called from ERXConfigurationManager.loadConfiguration(),
     *         which is called from ERXExtensions.finishInitialization(),
     *             which is registered to be called at ApplicationDidFinishLaunching-time by ERXApplication.
     */
    private String _appNameSuffix;

    static {
       _useLoadtimeAppSpecifics = ERXValueUtilities.booleanValueWithDefault(System.getProperty("NSProperties.useLoadtimeAppSpecifics"), true);
    }

    /**
     * <span class="ja">
     * 	デフォルト値を保持するかどかをセットします
     * 
     * 	@return boolean - true の場合には保持する
     * </span>
     */
    private static boolean retainDefaultsEnabled() {
        if (RetainDefaultsEnabled == null) {
            final String propertyValue = ERXSystem.getProperty("er.extensions.ERXProperties.RetainDefaultsEnabled", "false");
            final boolean isEnabled = "true".equals(propertyValue);
            RetainDefaultsEnabled = Boolean.valueOf(isEnabled);
        }
        return RetainDefaultsEnabled.booleanValue();
    }

    /**
     * <span class="en">
     * Puts handy properties such as <code>com.webobjects.version</code> 
     * into the system properties. This method is called when 
     * the framework is initialized  
     * (when WOApplication.ApplicationWillFinishLaunchingNotification 
     * is posted.)
     * </span>
     * 
     * <span class="ja">
     * <code>com.webobjects.version</code> をシステム・プロパティーへセットします。
     * フレームワークが初期化される時に呼ばれます。
     * 
     * (WOApplication.ApplicationWillFinishLaunchingNotification 発行時)
     * </span>
     */
    public static void populateSystemProperties() {
        System.setProperty("com.webobjects.version", webObjectsVersion());
    }

    /** 
     * <span class="en">
     * Returns the version string of the application.  
     * It checks <code>CFBundleShortVersionString</code> property 
     * in the <code>info.plist</code> resource and returns 
     * a trimmed version of the value. 
     * 
     * @return version number as string; can be a null-string when 
     *          the application doesn't have the value of
     *          <code>CFBundleShortVersionString</code>
     *                  in its <code>info.plist</code> resource. 
     *                  
     * @see #versionStringForFrameworkNamed
     * @see #webObjectsVersion
     * </span>
     * 
     * <span class="ja">
     * アプリケーションのバージョンを取得します
     * 
     * CustomInfo.plist 内の CFBundleShortVersionString プロパティーを調べ、Trimした結果を戻します
     * 
     * @return バージョン番号を文字列として戻ります。見つからない場合には null-string が戻ります。
     * 
     * @see #versionStringForFrameworkNamed
     * @see #webObjectsVersion
     * </span>
     */ 
    @SuppressWarnings("javadoc")
	public static String versionStringForApplication() {
        return valueFromPlistBundleWithKey(NSBundle.mainBundle(), "../Info.plist", "CFBundleShortVersionString");
    }

    /** 
     * <span class="en">
     * Returns the version string of the given framework.
     * It checks <code>CFBundleShortVersionString</code> property 
     * in the <code>info.plist</code> resource and returns 
     * a trimmed version of the value. 
     * 
     * @param frameworkName - name
     * 
     * @return version number as string; can be null-string when 
     *          the framework is not found or the framework
     *          doesn't have the value of
     *                  <code>CFBundleShortVersionString</code> in its
     *                  <code>info.plist</code> resource.
     *                  
     * @see #versionStringForApplication()
     * @see #webObjectsVersion()
     * @see ERXStringUtilities#removeExtraDotsFromVersionString(String)
     * </span>
     * 
     * <span class="ja">
     * 指定されたフレームワークのバージョンを取得します
     * 
     * CustomInfo.plist 内の CFBundleShortVersionString プロパティーを調べ、Trimした結果を戻します
     * 
     * @param frameworkName - フレームワーク名
     * 
     * @return バージョン番号を文字列として戻ります。フレームワークが見つからない場合には null-string が戻ります。
     * 
     * @see #versionStringForApplication()
     * @see #webObjectsVersion()
     * @see ERXStringUtilities#removeExtraDotsFromVersionString(String)
     * </span>
     */ 
    @SuppressWarnings("javadoc")
	public static String versionStringForFrameworkNamed(String frameworkName) {
        return valueFromPlistBundleWithKey(NSBundle.bundleForName(frameworkName), "Info.plist", "CFBundleShortVersionString");
    }

    /**
     * <span class="en">
     * Returns the version string of the given framework.
     * It checks <code>SourceVersion</code> property
     * in the <code>version.plist</code> resource and returns
     * a trimmed version of the value.
     *
     * @return version number as string; can be null-string when
     *          the framework is not found or the framework
     *          doesn't have the value of
     *                  <code>SourceVersion</code> in its
     *                  <code>info.plist</code> resource.
     *                  
     * @see #versionStringForApplication
     * @see #webObjectsVersion
     * </span>
     * 
     * <span class="ja">
     * WebObjectsフレームワークのバージョンを取得します
     * 
     * version.plist内の <code>SourceVersion</code> プロパティーを調べ、Trimした結果を戻します
     * 
     * @return バージョン番号をStringとして戻します。フレームワークが見つからなければ、null-stringが戻ります。
     * 
     * @see #versionStringForApplication
     * @see #webObjectsVersion
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static String sourceVersionString() {
        return valueFromPlistBundleWithKey(NSBundle.bundleForName("JavaWebObjects"), "version.plist", "SourceVersion");
    }

    /**
     * <span class="en">
     * Returns the key in an plist of the given framework.
     * 
     * @param bundle - bundle name
     * @param plist - plist Filename
     * @param key - key
     * 
     * @return Result
     * </span>
     * <span class="ja">
     * 対象バンドル内のplistファイル内のキーを使って、結果を戻します。
     * 
     * @param bundle - 対象するバンドル名
     * @param plist - plist ファイル名
     * @param key - plist 内に調べるキー
     * 
     * @return 結果文字列
     * </span>
     */
    @SuppressWarnings("javadoc")
    public static String valueFromPlistBundleWithKey(NSBundle bundle, String plist, String key) {
    	if (bundle == null)
    		return "";

    	String dictString = new String(bundle.bytesForResourcePath(plist));
    	NSDictionary versionDictionary = NSPropertyListSerialization.dictionaryForString(dictString);

    	String versionString = (String) versionDictionary.objectForKey(key);
    	return versionString == null  ?  ""  :  versionString.trim(); // trim() removes the line ending char
    }
    
    /** 
     * <span class="en">
     * Returns WebObjects version as string. If it's one of those 
     * version 5.1s (5.1, 5.1.1, 5.1.2...), this method will only 
     * return 5.1. If it's 5.2s, this method will return more precise 
     * version numbers such as 5.2.1. Note that version 5.0 series 
     * is not supported and may return incorrect version numbers 
     * (it will return 5.1). 
     * 
     * @return WebObjects version number as string
     * 
     * @see #webObjectsVersionAsDouble()
     * @see ERXStringUtilities#removeExtraDotsFromVersionString(String)
     * </span>
     * 
     * <span class="ja">
     * WebObjectsのバージョンをStringとして戻します。
     * ただし、5.1x (5.1, 5.1.1, 5.1.2...)の場合は単に5.1が戻ります。
     * 5.2以上では正しく5.2.1の様な番号が戻ります。5.1以下はサポート対象外で正しい番号が戻りません。
     * 
     * @return WebObjects バージョン番号を String として戻ります。
     * 
     * @see #webObjectsVersionAsDouble()
     * @see ERXStringUtilities#removeExtraDotsFromVersionString(String)
     * </span>
     * @deprecated Wonder is used with WO 5.4 only
     */ 
    @SuppressWarnings("javadoc")
    @Deprecated
	public static String webObjectsVersion() {
        if (_webObjectsVersion == null) {
            _webObjectsVersion = versionStringForFrameworkNamed("JavaWebObjects");
            
            // if _webObjectsVersion is a null-string, we assume it's WebObjects 5.1.x
            if (_webObjectsVersion.equals("")) 
                _webObjectsVersion = "5.1";
        }
        return _webObjectsVersion;
    }

    /** 
     * <span class="en">
     * Returns WebObjects version as double. If it's one of those 
     * version 5.1s (5.1, 5.1.1, 5.1.2...), this method will only 
     * return 5.1. If it's 5.2s, this method will return more precise 
     * version numbers such as 5.2.1. Note that version 5.0 series 
     * is not supported and may return incorrect version numbers 
     * (it will return 5.1). 
     * 
     * @return WebObjects version number as double
     * 
     * @see #webObjectsVersion
     * </span>
     * 
     * <span class="ja">
     * WebObjectsのバージョンをdoubleとして戻します。
     * ただし、5.1x (5.1, 5.1.1, 5.1.2...)の場合は単に5.1が戻ります。
     * 5.2以上では正しく5.2.1の様な番号が戻ります。5.1以下はサポート対象外で正しい番号が戻りません。
     * 
     * @return WebObjects バージョン番号を double として戻ります。
     * 
     * @see #webObjectsVersion
     * </span>
     * @deprecated Wonder is used with WO 5.4 only
     */
    @SuppressWarnings("javadoc")
    @Deprecated
	public static double webObjectsVersionAsDouble() {
        if (_webObjectsVersionDouble == 0.0d) {
            String woVersionString = ERXStringUtilities.removeExtraDotsFromVersionString(webObjectsVersion());
            int cutoffIndex = woVersionString.indexOf(' ');
            if (cutoffIndex == -1) {
            	cutoffIndex = woVersionString.indexOf('-');
            }
            if (cutoffIndex != -1) {
            	woVersionString = woVersionString.substring(0, cutoffIndex);
            }
            try {
                _webObjectsVersionDouble = Double.parseDouble(woVersionString);
            } catch (NumberFormatException ex) {
                log.error("An exception occurred while parsing webObjectVersion " + woVersionString 
                    + " as a double value: " + ex.getClass().getName() + " " + ex.getMessage());
            }
        }
        return _webObjectsVersionDouble;
    }

    /**
     * <span class="en">
     * Quick convience method used to determine if the current
     * WebObjects version is 5.2 or higher.
     * @return if the version of WebObjects is 5.2 or better
     * </span>
     * 
     * <span class="ja">
     *  WebObjectsのバージョン番号が5.2以上の場合にはtrueが戻ります
     *  
     *  @return true もし、バージョン番号が5.2以上であれば
     * </span>
     * @deprecated Wonder is used with WO 5.4 only
     */
    @SuppressWarnings("javadoc")
    @Deprecated
	public static boolean webObjectsVersionIs52OrHigher() {
        if(ERXProperties.booleanForKey("er.extensions.ERXProperties.checkOldVersions")) {
            return webObjectsVersionAsDouble() >= 5.2d;
        }
        return true;
    }

    /**
     * <span class="en">
     * Quick convience method used to determine if the current
     * WebObjects version is 5.22 or higher.
     * 
     * @return if the version of WebObjects is 5.22 or better
     * </span>
     * 
     * <span class="ja">
     * WebObjectsのバージョン番号が5.22以上の場合にはtrueが戻ります
     * 
     * @return true もし、バージョン番号が5.22以上であれば
     * </span>
     * @deprecated Wonder is used with WO 5.4 only
     */
    @SuppressWarnings("javadoc")
    @Deprecated
	public static boolean webObjectsVersionIs522OrHigher() {
        if(ERXProperties.booleanForKey("er.extensions.ERXProperties.checkOldVersions")) {
            String webObjectsVersion = webObjectsVersion();
            if("5.2".equals(webObjectsVersion)) {
                String sourceVersion = sourceVersionString();
                if("9260000".equals(sourceVersion)) {
                    return true;
                }
            }
            return webObjectsVersionAsDouble() >= 5.22d;
        }
        return true;
    }

    
    /**
     * <span class="en">
     * Cover method for returning an NSArray for a
     * given system property.
     * 
     * @param s - system property
     * 
     * @return array de-serialized from the string in
     *      the system properties
     * </span>
     * 
     * <span class="ja">
     * 	システム・プロパティーの結果を NSArray で戻します
     * 
     * 	@param s - キー（システム・プロパティー内）
     * 
     * 	@return システム・プロパティー内の連結した String を NSArray に変換した配列
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static NSArray arrayForKey(String s) {
        return arrayForKeyWithDefault(s, null);
    }

    /**
     * <span class="en">
     * Converts the standard propertyName into one with a .&lt;AppName&gt; on the end, if the property is defined with
     * that suffix.  If not, then this caches the standard propertyName.  A cache is maintained to avoid concatenating
     * strings frequently, but may be overkill since most usage of this system doesn't involve frequent access.
     * 
     * @param propertyName
     * 
     * @return
     * </span>
     * <span class="ja">
     * 標準プロパティー名の右側にアプリケーション名を追加し、プロパティーを使用します。なければ、標準プロパティーを使用します。
     * 毎回の文字列処理を防ぐ為にはキャシュを使用します。
     * 
     * @param propertyName - プロパティー名
     * 
     * @return アプリケーション専用のプロパティー名
     * </span>
     */
    @SuppressWarnings("javadoc")
	private static String getApplicationSpecificPropertyName(final String propertyName) {
    	if (_useLoadtimeAppSpecifics) {
    		return propertyName;
    	}
    		
    	synchronized(AppSpecificPropertyNames) {
            // only keep 128 of these around
            if (AppSpecificPropertyNames.size() > 128) {
                AppSpecificPropertyNames.clear();
            }
            String appSpecificPropertyName = (String)AppSpecificPropertyNames.get(propertyName);
            if (appSpecificPropertyName == null) {
                final WOApplication application = WOApplication.application();
                if (application != null) {
                    final String appName = application.name();
                    appSpecificPropertyName = propertyName + "." + appName;
                }
                else {
                    appSpecificPropertyName = propertyName;
                }
                final String propertyValue = ERXSystem.getProperty(appSpecificPropertyName);
                if (propertyValue == null) {
                    appSpecificPropertyName = propertyName;
                }
                AppSpecificPropertyNames.put(propertyName, appSpecificPropertyName);
            }
            return appSpecificPropertyName;
        }
    }

    /**
     * <span class="en">
     * Cover method for returning an NSArray for a
     * given system property and set a default value if not given.
     * @param s - system property
     * @param defaultValue - default value
     * 
     * @return array de-serialized from the string in
     *      the system properties or default value
     * </span>
     * 
     * <span class="ja">
     * 	システム・プロパティーの結果を NSArray で戻します
     * 
     * 	@param s - キー（システム・プロパティー内）
     * 	@param defaultValue - デフォルト値
     * 
     * 	@return システム・プロパティー内の連結した String を NSArray に変換した配列
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static NSArray arrayForKeyWithDefault(final String s, final NSArray defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);
		NSArray value;
		Object cachedValue = _cache.get(propertyName);
		if (UndefinedMarker.equals(cachedValue)) {
			value = defaultValue;
		} else if (cachedValue instanceof NSArray) {
			value = (NSArray) cachedValue;
		} else {
			value = ERXValueUtilities.arrayValueWithDefault(ERXSystem.getProperty(propertyName), null);
			_cache.put(s, value == null ? (Object)UndefinedMarker : value);
			if (value == null) {
				value = defaultValue;
			}
	        if (retainDefaultsEnabled() && value == null && defaultValue != null) {
	            setArrayForKey(defaultValue, propertyName);
	        }
		}
		return value;
    }
    
    /**
     * <span class="en">
     * 	Cover method for returning a boolean for a
     * 	given system property. This method uses the
     * 	method <code>booleanValue</code> from
     * 	{@link ERXUtilities}.
     * 	@param s - system property
     * 	@return boolean value of the string in the
     *      system properties.
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果を boolean で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * 
     * @return boolean 値、デフォルトはfalse
     * </span>
     */    
    @SuppressWarnings("javadoc")
	public static boolean booleanForKey(String s) {
        return booleanForKeyWithDefault(s, false);
    }

    /**
     * <span class="en">
     * Cover method for returning a boolean for a
     * given system property or a default value. This method uses the
     * method <code>booleanValue</code> from
     * {@link ERXUtilities}.
     * 
     * @param s - system property
     * @param defaultValue - default value
     * 
     * @return boolean value of the string in the
     *      system properties.
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果やデフォルト値を boolean で戻します<br>
     * 
     * @param s - キー（システム・プロパティー内）
     * @param defaultValue - デフォルト値
     * 
     * @return boolean 値
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static boolean booleanForKeyWithDefault(final String s, final boolean defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);
        boolean value;
		Object cachedValue = _cache.get(propertyName);
		if (UndefinedMarker.equals(cachedValue)) {
			value = defaultValue;
		} else if (cachedValue instanceof Boolean) {
			value = ((Boolean) cachedValue).booleanValue();
		} else {
			Boolean objValue = ERXValueUtilities.BooleanValueWithDefault(ERXSystem.getProperty(propertyName), null);
			_cache.put(propertyName, objValue == null ? (Object)UndefinedMarker : objValue);
			if (objValue == null) {
				value = defaultValue;
			} else {
				value = objValue.booleanValue();
			}
	        if (retainDefaultsEnabled() && objValue == null) {
	            System.setProperty(propertyName, Boolean.toString(defaultValue));
	        }
		}
		return value;
    }
    
    /**
     * <span class="en">
     * Cover method for returning an NSDictionary for a
     * given system property.
     * 
     * @param s - system property
     * 
     * @return dictionary de-serialized from the string in
     *      the system properties
     * </span>
     * 
     * <span class="ja">
     * 	システム・プロパティーの結果を NSDictionary で戻します
     * 
     * 	@param s - キー（システム・プロパティー内）
     * 
     * 	@return システム・プロパティー内の NSDictionary に変換
     * </span>
     */    
    @SuppressWarnings("javadoc")
	public static NSDictionary dictionaryForKey(String s) {
        return dictionaryForKeyWithDefault(s, null);
    }

    /**
     * <span class="en">
     * dictionaryForKeyWithDefault
     * Cover method for returning an NSDictionary for a
     * given system property or the default value.
     * 
     * @param s  - system property
     * @param defaultValue - default value
     * 
     * @return dictionary de-serialized from the string in
     *      the system properties
     * </span>
     * 
     * <span class="ja">
     * 	システム・プロパティーの結果を NSDictionary で戻します
     * 
     * 	@param s - キー（システム・プロパティー内）
     * 	@param defaultValue - デフォルト値
     * 
     * 	@return システム・プロパティーの結果を NSDictionary
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static NSDictionary dictionaryForKeyWithDefault(final String s, final NSDictionary defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);
		NSDictionary value;
		Object cachedValue = _cache.get(propertyName);
		if (UndefinedMarker.equals(cachedValue)) {
			value = defaultValue;
		} else if (cachedValue instanceof NSDictionary) {
			value = (NSDictionary) cachedValue;
		} else {
			value = ERXValueUtilities.dictionaryValueWithDefault(ERXSystem.getProperty(propertyName), null);
			_cache.put(s, value == null ? (Object)UndefinedMarker : value);
			if (value == null) {
				value = defaultValue;
			}
	        if (retainDefaultsEnabled() && value == null && defaultValue != null) {
	            setDictionaryForKey(defaultValue, propertyName);
	        }
		}
		return value;
    }
    
    /**
     * Checks if a property for the given key exists.
     * @param key name of the property
     * @return <code>true</code> if a property for key exists
     */
    public static boolean hasKey(String key) {
		return hasKey(key, false);
    }
    
    /**
     * Checks if a property for the given key exists. If you want to
     * ignore properties that have an empty value pass <code>true</code>
     * as parameter (i.e. '<code>my.property=</code>').
     * @param key name of the property
     * @param ignoreEmptyValue <code>true</code> if you want to ignore
     *            properties with empty values
     * @return <code>true</code> if a property exists
     */
    public static boolean hasKey(String key, boolean ignoreEmptyValue) {
    	final String propertyName = getApplicationSpecificPropertyName(key);
    	Object cachedValue = _cache.get(propertyName);
    	if (cachedValue == null || UndefinedMarker.equals(cachedValue)) {
    		String value = ERXSystem.getProperty(key);
    		return value != null && !(ignoreEmptyValue && value.length() == 0);
    	}
		return true;
    }

    /**
     * <span class="en">
     * Cover method for returning an int for a
     * given system property.
     * 
     * @param s - system property
     * 
     * @return int value of the system property or 0
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果を int で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * 
     * @return int 値、デフォルトは 0
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static int intForKey(String s) {
        return intForKeyWithDefault(s, 0);
    }

    /**
     * <span class="en">
     * Cover method for returning a long for a
     * given system property.
     * 
     * @param s - system property
     * 
     * @return long value of the system property or 0
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果を long で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * 
     * @return long 値、デフォルトは 0
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static long longForKey(String s) {
        return longForKeyWithDefault(s, 0);
    }

    /**
     * <span class="en">
     * Cover method for returning a float for a
     * given system property.
     * 
     * @param s - system property
     * 
     * @return float value of the system property or 0
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果を float で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * 
     * @return float 値、デフォルトは 0
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static float floatForKey(String s) {
        return floatForKeyWithDefault(s, 0);
    }

    /**
     * <span class="en">
     * Cover method for returning a double for a
     * given system property.
     * 
     * @param s - system property
     * 
     * @return double value of the system property or 0
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果を double で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * 
     * @return double 値、デフォルトは 0
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static double doubleForKey(String s) {
        return doubleForKeyWithDefault(s, 0);
    }

    /**
     * <span class="en">
     * Cover method for returning a BigDecimal for a
     * given system property. This method uses the
     * method <code>bigDecimalValueWithDefault</code> from
     * {@link ERXValueUtilities}.
     * 
     * @param s - system property
     * 
     * @return bigDecimal value of the string in the
     *      system properties.  Scale is controlled by the string, ie "4.400" will have a scale of 3.
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果を BigDecimal で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * 
     * @return BigDecimal 値、デフォルトは null
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static BigDecimal bigDecimalForKey(String s) {
        return bigDecimalForKeyWithDefault(s,null);
    }

    /**
     * <span class="en">
     * Cover method for returning a BigDecimal for a
     * given system property or a default value. This method uses the
     * method <code>bigDecimalValueWithDefault</code> from
     * {@link ERXValueUtilities}.
     * 
     * @param s - system property
     * @param defaultValue - default value
     * 
     * @return BigDecimal value of the string in the
     *      system properties. Scale is controlled by the string, ie "4.400" will have a scale of 3.
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果を BigDecimal で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * @param defaultValue - デフォルト値
     * 
     * @return BigDecimal 値、又はデフォルト値
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static BigDecimal bigDecimalForKeyWithDefault(String s, BigDecimal defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);

        Object value = _cache.get(propertyName);
        if (UndefinedMarker.equals(value)) {
            return defaultValue;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal)value;
        }
        
        String propertyValue = ERXSystem.getProperty(propertyName);
        final BigDecimal bigDecimal = ERXValueUtilities.bigDecimalValueWithDefault(propertyValue, defaultValue);
        if (retainDefaultsEnabled() && propertyValue == null && bigDecimal != null) {
            propertyValue = bigDecimal.toString();
            System.setProperty(propertyName, propertyValue);
        }
        _cache.put(propertyName, propertyValue == null ? (Object)UndefinedMarker : bigDecimal);
        return bigDecimal;
    }

    /**
     * <span class="en">
     * Cover method for returning an int for a
     * given system property with a default value.
     * 
     * @param s - system property
     * @param defaultValue - default value
     * 
     * @return int value of the system property or the default value
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果を int で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * @param defaultValue - デフォルト値
     * 
     * @return int 値、又はデフォルト値
     * </span>
     */    
    @SuppressWarnings("javadoc")
	public static int intForKeyWithDefault(final String s, final int defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);
		int value;
		Object cachedValue = _cache.get(propertyName);
		if (UndefinedMarker.equals(cachedValue)) {
			value = defaultValue;
		} else if (cachedValue instanceof Integer) {
			value = ((Integer) cachedValue).intValue();
		} else {
			Integer objValue = ERXValueUtilities.IntegerValueWithDefault(ERXSystem.getProperty(propertyName), null);
			_cache.put(s, objValue == null ? (Object)UndefinedMarker : objValue);
			if (objValue == null) {
				value = defaultValue;
			} else {
				value = objValue.intValue();
			}
	        if (retainDefaultsEnabled() && objValue == null) {
	            System.setProperty(propertyName, Integer.toString(defaultValue));
	        }
		}
		return value;
    }

    /**
     * <span class="en">
     * Cover method for returning a long for a
     * given system property with a default value.
     * 
     * @param s - system property
     * @param defaultValue - default value
     * 
     * @return long value of the system property or the default value
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果を long で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * @param defaultValue - デフォルト値
     * 
     * @return long 値、又はデフォルト値
     * </span>
     */    
    @SuppressWarnings("javadoc")
	public static long longForKeyWithDefault(final String s, final long defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);
		long value;
		Object cachedValue = _cache.get(propertyName);
		if (UndefinedMarker.equals(cachedValue)) {
			value = defaultValue;
		} else if (cachedValue instanceof Long) {
			value = ((Long) cachedValue).longValue();
		} else {
			Long objValue = ERXValueUtilities.LongValueWithDefault(ERXSystem.getProperty(propertyName), null);
			_cache.put(s, objValue == null ? (Object)UndefinedMarker : objValue);
			if (objValue == null) {
				value = defaultValue;
			} else {
				value = objValue.longValue();
			}
	        if (retainDefaultsEnabled() && objValue == null) {
	            System.setProperty(propertyName, Long.toString(defaultValue));
	        }
		}
		return value;
    }

    /**
     * <span class="en">
     * Cover method for returning a float for a
     * given system property with a default value.
     * 
     * @param s - system property
     * @param defaultValue - default value
     * 
     * @return float value of the system property or the default value
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果やデフォルト値を float で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * @param defaultValue - デフォルト値
     * 
     * @return float 値
     * </span>
     */    
    @SuppressWarnings("javadoc")
	public static float floatForKeyWithDefault(final String s, final float defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);

		float value;
		Object cachedValue = _cache.get(propertyName);
		if (UndefinedMarker.equals(cachedValue)) {
			value = defaultValue;
		} else if (cachedValue instanceof Float) {
			value = ((Float) cachedValue).floatValue();
		} else {
			Float objValue = ERXValueUtilities.FloatValueWithDefault(ERXSystem.getProperty(propertyName), null);
			_cache.put(s, objValue == null ? (Object)UndefinedMarker : objValue);
			if (objValue == null) {
				value = defaultValue;
			} else {
				value = objValue.floatValue();
			}
	        if (retainDefaultsEnabled() && objValue == null) {
	            System.setProperty(propertyName, Float.toString(defaultValue));
	        }
		}
		return value;
    }

    /**
     * <span class="en">
     * Cover method for returning a double for a
     * given system property with a default value.
     * 
     * @param s - system property
     * @param defaultValue - default value
     * 
     * @return double value of the system property or the default value
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果やデフォルト値を double で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * @param defaultValue - デフォルト値
     * 
     * @return double 値
     * </span>
     */    
    @SuppressWarnings("javadoc")
	public static double doubleForKeyWithDefault(final String s, final double defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);

		double value;
		Object cachedValue = _cache.get(propertyName);
		if (UndefinedMarker.equals(cachedValue)) {
			value = defaultValue;
		} else if (cachedValue instanceof Double) {
			value = ((Double) cachedValue).doubleValue();
		} else {
			Double objValue = ERXValueUtilities.DoubleValueWithDefault(ERXSystem.getProperty(propertyName), null);
			_cache.put(s, objValue == null ? (Object)UndefinedMarker : objValue);
			if (objValue == null) {
				value = defaultValue;
			} else {
				value = objValue.doubleValue();
			}
	        if (retainDefaultsEnabled() && objValue == null) {
	            System.setProperty(propertyName, Double.toString(defaultValue));
	        }
		}
		return value;
    }
    
    /**
     * <span class="en">
     * Returning an string for a given system 
     * property. This is a cover method of 
     * {@link java.lang.System#getProperty}
     * 
     * @param s - system property
     * 
     * @return string value of the system property or null
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果を String で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * 
     * @return String 値、デフォルトはnull
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static String stringForKey(String s) {
        return stringForKeyWithDefault(s, null);
    }

    /**
     * <span class="en">
     * Returning an string for a given system
     * property. This is a cover method of
     * {@link java.lang.System#getProperty}
     * 
     * @param s - system property
     * @param defaultValue - default Value
     * 
     * @return string value of the system property or null
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの結果やデフォルト値を String で戻します
     * 
     * @param s - キー（システム・プロパティー内）
     * @param defaultValue - デフォルト値
     * 
     * @return String 値、又はデフォルト値
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static String stringForKeyWithDefault(final String s, final String defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);
        final String propertyValue = ERXSystem.getProperty(propertyName);
        final String stringValue = propertyValue == null ? defaultValue : propertyValue;
        if (retainDefaultsEnabled() && propertyValue == null) {
            System.setProperty(propertyName, stringValue == null ? UndefinedMarker : stringValue);
        }
        return stringValue == UndefinedMarker ? null : stringValue;
    }
    
    /**
     * <span class="en">
     * Returns the decrypted value for the given property name using
     * the default crypter if the property propertyName.encrypted=true.  For
     * instance, if you are requesting my.password, if my.password.encrypted=true
     * the value of my.password will be passed to the default crypter's decrypt
     * method.
     * 
     * @param propertyName - the property name to retrieve and optionally decrypt
     * 
     * @return the decrypted property value
     * </span>
     * 
     * <span class="ja">
     * 	指定プロパティー名とデフォルト暗号化方法 (propertyName.encrypted=true) を使って復元されている値を戻します。
     * 	例えば、my.password を取得する場合、my.password.encrypted=true も設定されていれば、
     * 	my.password は復元する時にデフォルト暗号化方法 {@link er.extensions.crypting.ERXCrypto#defaultCrypter()} を使用します。
     * 
     * 	@param propertyName - プロパティー名
     * 
     * 	@return 復元されている値
     * </span>
     */
    @SuppressWarnings("javadoc")
    public static String decryptedStringForKey(String propertyName) {
    	return ERXProperties.decryptedStringForKeyWithDefault(propertyName, null);
    }
    
    /**
     * <span class="en">
     * If the <code>propertyName.encrypted</code> property is set to true, returns
     * the plain text value of the given property name, after decrypting it with the
     * {@link er.extensions.crypting.ERXCrypto#defaultCrypter()}. For instance, if you are requesting
     * my.password and <code>my.password.encrypted</code> is set to true,
     * the value of <code>my.password</code> will be sent to the default crypter's
     * decrypt() method.
     * 
     * @param propertyName - the property name to retrieve and optionally decrypt
     * @param defaultValue - the default value to return if there is no password
     * 
     * @return the decrypted property value
     * </span>
     * 
     * <span class="ja">
     * 	指定プロパティー名とデフォルト暗号化方法 (propertyName.encrypted=true) を使って復元されている値を戻します。
     * 	例えば、my.password を取得する場合、my.password.encrypted=true も設定されていれば、
     * 	my.password は復元する時にデフォルト暗号化方法 {@link er.extensions.crypting.ERXCrypto#defaultCrypter()} を使用します。
     * 
     * 	@param propertyName - プロパティー名
     * 	@param defaultValue - プロパティーが無ければ、デフォルト値
     * 
     * 	@return 復元されている値
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static String decryptedStringForKeyWithDefault(String propertyName, String defaultValue) {
		boolean propertyNameEncrypted = ERXProperties.booleanForKeyWithDefault(propertyName + ".encrypted", false);
		String decryptedPassword;
		if (propertyNameEncrypted) {
			String encryptedPassword = ERXProperties.stringForKey(propertyName);
			decryptedPassword = ERXCrypto.defaultCrypter().decrypt(encryptedPassword);
		}
		else {
			decryptedPassword = ERXProperties.stringForKey(propertyName);
		}
		if (decryptedPassword == null) {
			decryptedPassword = defaultValue;
		}
		return decryptedPassword;
    }

    /**
     * <span class="en">
     * Returns the decrypted value for the given property name using the
     * {@link er.extensions.crypting.ERXCrypto#defaultCrypter()}. This is slightly different than
     * decryptedStringWithKeyWithDefault in that it does not require  the encrypted
     * property to be set.
     *  
     * @param propertyName - the name of the property to decrypt
     * @param defaultValue - the default encrypted value
     * 
     * @return the decrypted value
     * </span>
     * 
     * <span class="ja">
     * 	指定プロパティー名とデフォルト暗号化方法 (propertyName.encrypted=true) を使って復元されている値を戻します。
     * 	例えば、my.password を取得する場合、my.password.encrypted=true も設定されていれば、
     * 	my.password は復元する時にデフォルト暗号化方法 {@link er.extensions.crypting.ERXCrypto#defaultCrypter()} を使用します。
     *  
     * 	@param propertyName - プロパティー名
     * 	@param defaultValue - プロパティーが無ければ、暗号化されているデフォルト値
     * 
     * 	@return 復元されている値
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static String decryptedStringForKeyWithEncryptedDefault(String propertyName, String defaultValue) {
    	String encryptedPassword = ERXProperties.stringForKeyWithDefault(propertyName, defaultValue);
    	return ERXCrypto.defaultCrypter().decrypt(encryptedPassword);
    }

    /**
     * <span class="en">
     * Returns an array of strings separated with the given separator string.
     * 
     * @param key - the key to lookup
     * @param separator - the separator (",")
     * 
     * @return the array of strings or NSArray.EmptyArray if not found
     * </span>
     * <span class="ja">
     * 	システム・プロパティーの結果を指定 key と指定 separator より配列を戻します。
     * 	取得文字列は句切れ文字で配列に切り出します。
     * 
     * 	@param key - キー（システム・プロパティー内）
     * 	@param separator - 句切れ文字 (",")
     * 
     * 	@return 文字列よりの配列、無ければ NSArray.EmptyArray が戻ります
     * </span>
     */
    @SuppressWarnings({ "unchecked", "javadoc" })
    public static NSArray<String> componentsSeparatedByString(String key, String separator) {
    	return ERXProperties.componentsSeparatedByStringWithDefault(key, separator, NSArray.EmptyArray);
    }

    /**
     * <span class="en">
     * Returns an array of strings separated with the given separator string.
     * 
     * @param key - the key to lookup
     * @param separator - the separator (",")
     * @param defaultValue - the default array to return if there is no value
     * 
     * @return the array of strings
     * </span>
     * 
     * <span class="ja">
     * 	システム・プロパティーの結果を指定 key と指定 separator より配列を戻します。
     * 	取得文字列は句切れ文字で配列に切り出します。
     * 
     * 	@param key - キー（システム・プロパティー内）
     * 	@param separator - 句切れ文字 (",")
     * 	@param defaultValue - 値が無ければ、デフォルト値
     * 
     * 	@return 文字列よりの配列、無ければ defaultValue が戻ります
     * </span>
     */
    @SuppressWarnings({ "unchecked", "javadoc" })
	public static NSArray<String> componentsSeparatedByStringWithDefault(String key, String separator, NSArray<String> defaultValue) {
    	NSArray<String> array;
    	String str = stringForKeyWithDefault(key, null);
    	if (str == null) {
    		array = defaultValue;
    	}
    	else {
    		array = NSArray.componentsSeparatedByString(str, separator);
    	}
    	return array;
    }
    
    /**
     * <span class="en">
     * Sets an array in the System properties for
     * a particular key.
     * @param array - to be set in the System properties
     * @param key - to be used to get the value
     * </span>
     * 
     * <span class="ja">
     * 	システム・プロパティーに NSArray をセットします
     * 
     * 	@param array - システム・プロパティーにセットする配列
     * 	@param key - セットする為のキー
     * </span>
     */
    public static void setArrayForKey(NSArray array, String key) {
        setStringForKey(NSPropertyListSerialization.stringFromPropertyList(array), key);
    }

    /**
     * <span class="en">
     * Sets a dictionary in the System properties for
     * a particular key.
     * 
     * @param dictionary - to be set in the System properties
     * @param key - to be used to get the value
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーの指定キーをディクショナリー内にセットします
     * 
     * @param dictionary - システム・プロパティーをセットするディクショナリー
     * @param key - 値をアクセスするキー
     * </span>
     */    
    public static void setDictionaryForKey(NSDictionary dictionary, String key) {
        setStringForKey(NSPropertyListSerialization.stringFromPropertyList(dictionary), key);
    }

    /**
     * <span class="en">
     * Sets a string in the System properties for
     * another string.
     * 
     * @param string - to be set in the System properties
     * @param key - to be used to get the value
     * </span>
     * 
     * <span class="ja">
     * システム・プロパティーに String をセットします
     * 
     * @param string - システム・プロパティーにセットするString
     * @param key - セットする為のキー
     * </span>
     */
    // DELETEME: Really not needed anymore -- MS: Why?  We need the cache clearing.
    public static void setStringForKey(String string, String key) {
        System.setProperty(key, string);
        _cache.remove(key);
    }
    
    /**
     * <span class="ja">
     * 	キーを削除します
     * 
     * 	@param key - キー
     * </span>
     */
    public static void removeKey(String key) {
    	System.getProperties().remove(key);
    	_cache.remove(key);
    }
    
    /** 
     * <span class="en">
     * Copies all properties from source to dest. 
     * 
     * @param source  properties copied from 
     * @param dest  properties copied to
     * </span>
     * 
     * <span class="ja">
     * 	全てのプロパティーをコピー元からコピー先へコピーします。
     * 	
     * 	@param source - コピー元するプロパティー
     * 	@param dest - コピー先のプロパティー
     * </span>
     */
    public static void transferPropertiesFromSourceToDest(Properties source, Properties dest) {
        if (source != null) {
            dest.putAll(source);
            if (dest == System.getProperties()) {
                systemPropertiesChanged();
            }
        }
    }
    

    
    /**
     * <span class="en">
     * Reads a Java properties file at the given path 
     * and returns a {@link java.util.Properties Properties} object 
     * as the result. If the file does not exist, returns 
     * an empty properties object. 
     * 
     * @param path  file path to the properties file
     * 
     * @return properties object with the values from the file
     *      specified.
     * </span>
     * 
     * <span class="ja">
     * 	指定パスを使って、 Java プロパティー・ファイルを読込み、
     * 	{@link java.util.Properties Properties} オブジェクトとして戻します。
     * 	ファイルが存在していなければ、empty プロパティー・オブジェクトが戻ります。
     * 
     * 	@param path - プロパティー・ファイルへのパス
     * 
     * 	@return ファイルの内容を持つプロパティー・オブジェクト
     * </span>
     */
    // FIXME: This shouldn't eat the exception
    @SuppressWarnings("javadoc")
	public static Properties propertiesFromPath(String path) {
    	ERXProperties._Properties prop = new ERXProperties._Properties();

        if (path == null  ||  path.length() == 0) {
            log.warn("Attempting to read property file for null file path");
            return prop;
        }

        File file = new File(path);
        if (! file.exists()  ||  ! file.isFile()  ||  ! file.canRead()) {
            log.warn("File " + path + " doesn't exist or can't be read.");
            return prop;
        }

        try {
        	prop.load(file);
            log.debug("Loaded configuration file at path: "+ path);
        } catch (IOException e) {
            log.error("Unable to initialize properties from file \"" + path + "\"", e);
        }
        return prop;
    }

    /**
     * <span class="en">
     * Gets the properties for a given file.
     * 
     * @param file the properties file
     * 
     * @return properties from the given file
     * 
     * @throws java.io.IOException if the file is not found or cannot be read
     * </span>
     * 
     * <span class="ja">
     * 	指定ファイルのプロパティーをプロパティー・オブジェクトへロードします。
     * 
     * 	@param file - プロパティー・ファイル
     * 
     * 	@return ファイルの内容を持つプロパティー・オブジェクト
     * 
     * 	@throws java.io.IOException if the file is not found or cannot be read
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static Properties propertiesFromFile(File file) throws java.io.IOException {
        if (file == null)
            throw new IllegalStateException("Attempting to get properties for a null file!");
        ERXProperties._Properties prop = new ERXProperties._Properties();
        prop.load(file);
        return prop;
    }
    
    /**
     * <span class="en">
     * Sets and returns properties object with the values from 
     * the given command line arguments string array. 
     * 
     * @param argv - string array typically provided by 
     *               the command line arguments
     * @return properties object with the values from 
     *          the argv
     * </span>
     * 	コマンドライン等で渡されている文字列配列をプロパティー・オブジェクトへロードします。
     * 
     * 	@param argv - コマンドライン等で渡されている文字列配列
     * 
     * 	@return argv の内容を持つプロパティー・オブジェクト
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static Properties propertiesFromArgv(String[] argv) {
    	ERXProperties._Properties properties = new ERXProperties._Properties();
        NSDictionary argvDict = NSProperties.valuesFromArgv(argv);
        Enumeration e = argvDict.allKeys().objectEnumerator();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            properties.put(key, argvDict.objectForKey(key));
        }
        return properties;
    }

    /** 
     * <span class="en">
     * Returns an array of paths to the <code>Properties</code> and 
     * <code>WebObjects.properties</code> files contained in the 
     * application/framework bundles and home directory. 
     * <p>
     * If ProjectBuilder (for Mac OS X) has the project opened, 
     * it will attempt to get the path to the one in the project 
     * directory instead of the one in the bundle. 
     * <p>
     * This opened project detection feature is pretty fragile and 
     * will change between versions of the dev-tools.
     * 
     * @return paths to Properties files
     * </span>
     * <span class="ja">
     * 	application/framework バンドルとホーム・ダイレクトリーに含む <code>Properties</code> と
     *  <code>WebObjects.properties</code> ファイルへのファイル・パス配列を戻します
     *  <p>
     *  プロジェクト・ビルダーがプロジェクトを開いていれば、バンドルではなく、プロジェクトへのパスが戻されます。
     *  <p>
     *  開いているプロジェクト問題は開発ツールによって違っています。
     *  
     *  @return プロパティー・ファイルパス配列
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static NSArray pathsForUserAndBundleProperties() {
        return pathsForUserAndBundleProperties(false);
    }

    private static void addIfPresent(String info, String path, NSMutableArray<String> propertiesPaths, NSMutableArray<String> projectsInfo) {
    	if(path != null && path.length() > 0) {
    		path = getActualPath(path);
    		if(propertiesPaths.containsObject(path)) {
    			log.error("Path was already included: " + path + "");
    		}
    		projectsInfo.addObject("  " + info +" -> " + path);
    		propertiesPaths.addObject(path);
    	}
    }
    
    public static NSArray<String> pathsForUserAndBundleProperties(boolean reportLoggingEnabled) {
        NSMutableArray<String> propertiesPaths = new NSMutableArray();
        NSMutableArray<String> projectsInfo = new NSMutableArray();

        /*  Properties for frameworks */
        NSArray frameworkNames = (NSArray) NSBundle.frameworkBundles().valueForKey("name");
        Enumeration e = frameworkNames.reverseObjectEnumerator();
        while (e.hasMoreElements()) {
        	String frameworkName = (String) e.nextElement();

        	String propertyPath = ERXFileUtilities.pathForResourceNamed("Properties", frameworkName, null);
        	addIfPresent(frameworkName + ".framework", propertyPath, propertiesPaths, projectsInfo);

        	/** Properties.dev -- per-Framework-dev properties 
        	 * This adds support for Properties.dev in your Frameworks new load order will be
        	 */
        	String devPropertiesPath = ERXApplication.isDevelopmentModeSafe() ? ERXProperties.variantPropertiesInBundle("dev", frameworkName) : null;
        	addIfPresent(frameworkName + ".framework.dev", devPropertiesPath, propertiesPaths, projectsInfo);
        	
        	/** Properties.<userName> -- per-Framework-per-User properties */
        	String userPropertiesPath = ERXProperties.variantPropertiesInBundle(ERXSystem.getProperty("user.name"), frameworkName);
        	addIfPresent(frameworkName + ".framework.user", userPropertiesPath, propertiesPaths, projectsInfo);
        }

		NSBundle mainBundle = NSBundle.mainBundle();
		
		if( mainBundle != null ) {
	        String mainBundleName = mainBundle.name();
	
	        String appPath = ERXFileUtilities.pathForResourceNamed("Properties", "app", null);
	    	addIfPresent(mainBundleName + ".app", appPath, propertiesPaths, projectsInfo);
		}

		/*  WebObjects.properties in the user home directory */
		String userHome = ERXSystem.getProperty("user.home");
		if (userHome != null && userHome.length() > 0) {
			File file = new File(userHome, "WebObjects.properties");
			if (file.exists() && file.isFile() && file.canRead()) {
				try {
					String userHomePath = file.getCanonicalPath();
			    	addIfPresent("{$user.home}/WebObjects.properties", userHomePath, propertiesPaths, projectsInfo);
				}
				catch (java.io.IOException ex) {
					ERXProperties.log.error("Failed to load the configuration file '" + file.getAbsolutePath() + "'.", ex);
				}
			}
        }

		/*  Optional properties files */
		if (optionalConfigurationFiles() != null && optionalConfigurationFiles().count() > 0) {
			for (Enumeration configEnumerator = optionalConfigurationFiles().objectEnumerator(); configEnumerator.hasMoreElements();) {
				String configFile = (String) configEnumerator.nextElement();
				File file = new File(configFile);
				if (file.exists() && file.isFile() && file.canRead()) {
					try {
						String optionalPath = file.getCanonicalPath();
				    	addIfPresent("Optional Configuration", optionalPath, propertiesPaths, projectsInfo);
					}
					catch (java.io.IOException ex) {
						ERXProperties.log.error("Failed to load configuration file '" + file.getAbsolutePath() + "'.", ex);
					}
				}
				else {
					ERXProperties.log.error("The optional configuration file '" + file.getAbsolutePath() + "' either does not exist or could not be read.");
				}
			}
		}

		optionalPropertiesLoader(ERXSystem.getProperty("user.name"), propertiesPaths, projectsInfo);
		
        /** /etc/WebObjects/AppName/Properties -- per-Application-per-Machine properties */
        String applicationMachinePropertiesPath = ERXProperties.applicationMachinePropertiesPath("Properties");
    	addIfPresent("Application-Machine Properties", applicationMachinePropertiesPath, propertiesPaths, projectsInfo);

        /** Properties.dev -- per-Application-dev properties */
        String applicationDeveloperPropertiesPath = ERXProperties.applicationDeveloperProperties();
    	addIfPresent("Application-Developer Properties", applicationDeveloperPropertiesPath, propertiesPaths, projectsInfo);

        /** Properties.<userName> -- per-Application-per-User properties */
        String applicationUserPropertiesPath = ERXProperties.applicationUserProperties();
    	addIfPresent("Application-User Properties", applicationUserPropertiesPath, propertiesPaths, projectsInfo);

        /*  Report the result */
		if (reportLoggingEnabled && projectsInfo.count() > 0 && log.isInfoEnabled()) {
			StringBuffer message = new StringBuffer();
			message.append("\n\n").append("ERXProperties has found the following Properties files: \n");
			message.append(projectsInfo.componentsJoinedByString("\n"));
			message.append("\n");
			message.append("ERXProperties currently has the following properties:\n");
			message.append(ERXProperties.logString(ERXSystem.getProperties()));
			// ERXLogger.configureLoggingWithSystemProperties();
			log.info(message.toString());
		}

    	return propertiesPaths.immutableClone();
    }

    /** 
     * <span class="en">
     * 	Making it possible to use Properties File in the Application more
     * 	powerful, specially for newcomers.
     * 	For every Framework it will try to call also following 
     * 		Properties.[Framework] and Properties.[Framework].[Username]
     * 	Also there is a Propertie for
     * 		Properties.log4j, Properties.log4j.[Username] for logging
     * 		Properties.database, Properties.database.[Username] for database infos
     * 		Properties.multilanguage, Properties.multilanguage.[Username] for Encoding
     * 		Properties.migration, Properties.migration.[Username] for Migration
     * 
     * 	@param userName - Username
     * 	@param propertiesPaths - Properites Path
     * 		{@link ERXProperties#pathsForUserAndBundleProperties}
     *  @param projectsInfo - Project Info
     * 		{@link ERXProperties#pathsForUserAndBundleProperties}
     * </span>
     * 
     * <span class="ja">
     * 	アプリケーション内でプロパティーをもっと活躍させる為に種類別おプロパティーが追加されました。
     * 	初心者を含めてとても使いやすくなります。
     * 	各フレームワークへのプロパティー・ファイルを読み込むことを試します 
     * 		Properties.[Framework] と Properties.[Framework].[Username]
     * 	他にも次のプロパティーがあります
     * 		Properties.log4j, Properties.log4j.[Username] ログ専用
     * 		Properties.database, Properties.database.[Username] データベース情報
     * 		Properties.multilanguage, Properties.multilanguage.[Username] エンコーディング
     * 		Properties.migration, Properties.migration.[Username] マイグレーション
     * 
     * 	@param userName - ユーザ名
     * 	@param propertiesPaths - プロパティー・パス
     * 		{@link ERXProperties#pathsForUserAndBundleProperties}
     *  @param projectsInfo - プロジェクト情報
     * 		{@link ERXProperties#pathsForUserAndBundleProperties}
     * </span>
     */
    private static void optionalPropertiesLoader(String userName, NSMutableArray<String> propertiesPaths, NSMutableArray<String> projectsInfo) {

    	/** Properties.log4j.<userName> -- per-Application-per-User properties */
        String logPropertiesPath;
        logPropertiesPath = ERXProperties.variantPropertiesInBundle("log4j", "app");
        if(logPropertiesPath != null) {
        	addIfPresent("Application-User Log4j Properties", logPropertiesPath, propertiesPaths, projectsInfo);
        }
        logPropertiesPath = ERXProperties.variantPropertiesInBundle("log4j." + userName, "app");
        if(logPropertiesPath != null) {
        	addIfPresent("Application-User Log4j Properties", logPropertiesPath, propertiesPaths, projectsInfo);
        }

        /** Properties.database.<userName> -- per-Application-per-User properties */
        String databasePropertiesPath;
        databasePropertiesPath = ERXProperties.variantPropertiesInBundle("database", "app");
        if(databasePropertiesPath != null) {
        	addIfPresent("Application-User Database Properties", databasePropertiesPath, propertiesPaths, projectsInfo);
        }
        databasePropertiesPath = ERXProperties.variantPropertiesInBundle("database." + userName, "app");
        if(databasePropertiesPath != null) {
        	addIfPresent("Application-User Database Properties", databasePropertiesPath, propertiesPaths, projectsInfo);
        }
   	
        /** Properties.multilanguage.<userName> -- per-Application-per-User properties */
        String multilanguagePath;
        multilanguagePath = ERXProperties.variantPropertiesInBundle("multilanguage", "app");
        if(multilanguagePath != null) {
        	addIfPresent("Application-User Multilanguage Properties", multilanguagePath, propertiesPaths, projectsInfo);
        }
        multilanguagePath = ERXProperties.variantPropertiesInBundle("multilanguage." + userName, "app");
        if(multilanguagePath != null) {
        	addIfPresent("Application-User Multilanguage Properties", multilanguagePath, propertiesPaths, projectsInfo);
        }
    	
        /** Properties.migration -- per-Application properties */
        String migrationPath;
        migrationPath = ERXProperties.variantPropertiesInBundle("migration", "app");
        if(migrationPath != null) {
        	addIfPresent("Application-User Migration Properties", migrationPath, propertiesPaths, projectsInfo);
        }
        migrationPath = ERXProperties.variantPropertiesInBundle("migration." + userName, "app");
        if(migrationPath != null) {
        	addIfPresent("Application-User Migration Properties", migrationPath, propertiesPaths, projectsInfo);
        }
    	
        /** Properties.<frameworkName>.<userName> -- per-Application-per-User properties */
        @SuppressWarnings("unchecked")
        NSArray<String> frameworkNames = (NSArray<String>) NSBundle.frameworkBundles().valueForKey("name");
        Enumeration<String> e = frameworkNames.reverseObjectEnumerator();
        while (e.hasMoreElements()) {
          String frameworkName = e.nextElement();
          String userPropertiesPath;
          userPropertiesPath = ERXProperties.variantPropertiesInBundle(frameworkName, "app");
          if(userPropertiesPath != null) {
        	  addIfPresent(frameworkName + ".framework.common", userPropertiesPath, propertiesPaths, projectsInfo);
          }
          userPropertiesPath = ERXProperties.variantPropertiesInBundle(frameworkName + "." + userName, "app");
          if(userPropertiesPath != null) {
        	  addIfPresent(frameworkName + ".framework.user", userPropertiesPath, propertiesPaths, projectsInfo);
          }
        }
    }

    /**
     * Apply the current configuration to the supplied properties.
     * @param source
     * @param commandLine
     * @return the applied properties
     */
    public static Properties applyConfiguration(Properties source, Properties commandLine) {

    	Properties dest = source != null ? (Properties) source.clone() : new Properties();
    	NSArray additionalConfigurationFiles = ERXProperties.pathsForUserAndBundleProperties(false);

    	if (additionalConfigurationFiles.count() > 0) {
    		for (Enumeration configEnumerator = additionalConfigurationFiles.objectEnumerator(); configEnumerator.hasMoreElements();) {
    			String configFile = (String)configEnumerator.nextElement();
    			File file = new File(configFile);
    			if (file.exists() && file.isFile() && file.canRead()) {
    				try {
    					Properties props = ERXProperties.propertiesFromFile(file);
    					if(log.isDebugEnabled()) {
    						log.debug("Loaded: " + file + "\n" + ERXProperties.logString(props));
    					}
    					ERXProperties.transferPropertiesFromSourceToDest(props, dest);
    				} catch (java.io.IOException ex) {
    					log.error("Unable to load optional configuration file: " + configFile, ex);
    				}
    			}
    			else {
    				ERXConfigurationManager.log.error("The optional configuration file '" + file.getAbsolutePath() + "' either does not exist or cannot be read.");
    			}
    		}
    	}

    	if(commandLine != null) {
    		ERXProperties.transferPropertiesFromSourceToDest(commandLine, dest);
    	}
		return dest;
    	
    }

    /**
     * <span class="en">
     * Returns all of the properties in the system mapped to their evaluated values, sorted by key.
     * 
     * @param protectValues - if true, keys with the word "password" in them will have their values removed 
     * 
     * @return all of the properties in the system mapped to their evaluated values, sorted by key
     * </span>
     * 
     * <span class="ja">
     * 値にマップされているシステム内の全プロパティーをキーでソート済みとして戻します。
     * 
     * @param protectValues - true の場合にはパスワードを含むキーが出力されません。
     * 
     * @return 値にマップされているシステム内の全プロパティーをキーでソート済み
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static Map<String, String> allPropertiesMap(boolean protectValues) {
    	return propertiesMap(ERXSystem.getProperties(), protectValues);
    }

    /**
     * Returns all of the properties in the system mapped to their evaluated values, sorted by key.
     * 
     * @param properties
     * @param protectValues if <code>true</code>, keys with the word "password" in them will have their values removed 
     * @return all of the properties in the system mapped to their evaluated values, sorted by key
     */
    public static Map<String, String> propertiesMap(Properties properties, boolean protectValues) {
    	Map<String, String> props = new TreeMap<String, String>();
    	for (Enumeration e = properties.keys(); e.hasMoreElements();) {
    		String key = (String) e.nextElement();
    		if (protectValues && key.toLowerCase().contains("password")) {
    			props.put(key, "<deleted for log>");
    		}
    		else {
    			props.put(key, String.valueOf(properties.getProperty(key)));
    		}
    	}
    	return props;
    }
    
    /**
     * Returns a string suitable for logging.
     * @param properties
     * @return string for logging
     */
    public static String logString(Properties properties) {
    	StringBuilder message = new StringBuilder();
        for (Map.Entry<String, String> entry : propertiesMap(properties, true).entrySet()) {
        	message.append("  " + entry.getKey() + "=" + entry.getValue() + "\n");
        }
        return message.toString();
    }
    
    public static class Property {
    	public String key, value;
    	public Property(String key, String value) {
    		this.key = key;
    		this.value = value;
    	}
    	@Override
    	public String toString() {
    		return key + " = " + value;
    	}
    }

    /**
     * <span class="ja">
     * 	全プロパティーの配列を戻します
     * 
     * 	@return 全プロパティーの配列
     * </span>
     */
    public static NSArray<Property> allProperties() {
    	NSMutableArray props = new NSMutableArray();
    	for (Enumeration e = ERXSystem.getProperties().keys(); e.hasMoreElements();) {
    		String key = (String) e.nextElement();
    		String object = "" + ERXSystem.getProperty(key);
    		props.addObject(new Property(key, object));
    	}
    	return (NSArray) props.valueForKey("@sortAsc.key");
     }

    /** 
     * <span class="en">
     * Returns the full path to the Properties file under the 
     * given project path. At the current implementation, 
     * it looks for /Properties and /Resources/Properties. 
     * If the Properties file doesn't exist, returns null.  
     * 
     * @param projectPath - string to the project root directory
     * 
     * @return  the path to the Properties file if it exists
     * </span>
     * 
     * <span class="ja">
     * 指定プロジェクト・パスのプロパティー・ファイルの完全パスを戻します。
     * カレントの実装では、 /Properties と /Resources/Properties を参照します。
     * プロパティー・ファイルが存在していなければ、 null が戻ります。
     * 
     * @param projectPath - プロジェクトのルート・ダイレクトリーへのパス
     * 
     * @return プロパティー・ファイルへのパス
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static String pathForPropertiesUnderProjectPath(String projectPath) {
        String path = null; 
        final NSArray supportedPropertiesPaths = new NSArray(new Object[] 
                                        {"/Properties", "/Resources/Properties"});
        Enumeration e = supportedPropertiesPaths.objectEnumerator();
        while (e.hasMoreElements()) {
            File file = new File(projectPath + (String) e.nextElement());
            if (file.exists()  &&  file.isFile()  &&  file.canRead()) {
                try {
                    path = file.getCanonicalPath();
                } catch (IOException ex) {
                    log.error(ex.getClass().getName() + ": " + ex.getMessage());
                }
                break;
            }
        }
        return path;
    }
    
    /**
     * <span class="en">
     * Returns the application-specific user properties.
     * 
     * @return application-specific user properties
     * </span>
     * 
     * <span class="ja">
     * アプリケーション開発者プロパティーへのパスを戻します。
     * 
     * @return 開発者プロパティーへのパス
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static String applicationDeveloperProperties() {
    	String applicationDeveloperPropertiesPath = null;
    	if (ERXApplication.isDevelopmentModeSafe()) {
	        String devName = ERXSystem.getProperty("er.extensions.ERXProperties.devPropertiesName", "dev");
	        applicationDeveloperPropertiesPath = variantPropertiesInBundle(devName, "app");
    	}
        return applicationDeveloperPropertiesPath;
    }
    
    /**
     * Returns the application-specific variant properties for the given bundle.
     * @param userName 
     * @param bundleName 
     * @return the application-specific variant properties for the given bundle.
     */
    public static String variantPropertiesInBundle(String userName, String bundleName) {
    	String applicationUserPropertiesPath = null;
        if (userName != null  &&  userName.length() > 0) { 
        	String resourceApplicationUserPropertiesPath = ERXFileUtilities.pathForResourceNamed("Properties." + userName, bundleName, null);
            if (resourceApplicationUserPropertiesPath != null) {
            	applicationUserPropertiesPath = ERXProperties.getActualPath(resourceApplicationUserPropertiesPath);
            }
        }
        return applicationUserPropertiesPath;
    }

    /**
     * <span class="en">
     * Returns the application-specific user properties.
     * 
     * @return the application-specific user properties
     * </span>
     * 
     * <span class="ja">
     * アプリケーション特定のユーザ・プロパティーへのパスを戻します。
     * 
     * @return ユーザ・プロパティーへのパス
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static String applicationUserProperties() {
    	return variantPropertiesInBundle(ERXSystem.getProperty("user.name"), "app");
    }
    
    /**
     * <span class="en">
     * Returns the path to the application-specific system-wide file "fileName".  By default this path is /etc/WebObjects, 
     * and the application name will be appended.  For instance, if you are asking for the MyApp Properties file for the
     * system, it would go in /etc/WebObjects/MyApp/Properties.
     * 
     * @param fileName - the Filename
     * 
     * @return the path, or null if the path does not exist
     * </span>
     * 
     * <span class="ja">
     * アプリケーション特定の "fileName" へのパスを戻します。
     * デフォルトでは /etc/WebObjects とアプリケーション名です。
     * 例えば、MyApp プロパティー・ファイルへの問い合わせをすると、/etc/WebObjects/MyApp/Properties が戻ります。
     * 
     * @param fileName - ファイル名
     * 
     * @return プロパティーへのパス、無ければ null
     * </span>
     */
	@SuppressWarnings("javadoc")
	public static String applicationMachinePropertiesPath(String fileName) {
    	String applicationMachinePropertiesPath = null;
    	String machinePropertiesPath = ERXSystem.getProperty("er.extensions.ERXProperties.machinePropertiesPath", "/etc/WebObjects");
    	WOApplication application = WOApplication.application();
    	String applicationName;
    	if (application != null) {
    		applicationName = application.name();
    	}
    	else {
    		applicationName = ERXSystem.getProperty("WOApplicationName");
    		if (applicationName == null) {
    			NSBundle mainBundle = NSBundle.mainBundle();
    			if (mainBundle != null) {
    				applicationName = mainBundle.name();
    			}
    			if (applicationName == null) {
    				applicationName = "Unknown";
    			}
    		}
    	}
    	File applicationPropertiesFile = new File(machinePropertiesPath + File.separator + fileName);
    	if (!applicationPropertiesFile.exists()) {
    		applicationPropertiesFile = new File(machinePropertiesPath + File.separator + applicationName + File.separator + fileName);
    	}
    	if (applicationPropertiesFile.exists()) {
    		try {
    			applicationMachinePropertiesPath = applicationPropertiesFile.getCanonicalPath();
    		}
    		catch (IOException e) {
    			ERXProperties.log.error("Failed to load machine Properties file '" + fileName + "'.", e);
    		}
    	}
    	return applicationMachinePropertiesPath;
    }

    /**
     * <span class="en">
     * Gets an array of optionally defined configuration files.  For each file, if it does not
     * exist as an absolute path, ERXProperties will attempt to resolve it as an application resource
     * and use that instead.
     *  
     * @return array of configuration file names
     * </span>
     * 
     * <span class="ja">
     * 	オプションで定義セットアップファイルの配列を取得します。各ファイル、完全パスとして存在していなければ、
     * 	ERXProperties はアプリケーション・リソースとして処理します。
     *  
     * 	@return オプションで定義セットアップファイルの配列
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static NSArray optionalConfigurationFiles() {
    	NSArray immutableOptionalConfigurationFiles = arrayForKey("er.extensions.ERXProperties.OptionalConfigurationFiles");
    	NSMutableArray optionalConfigurationFiles = null;
    	if (immutableOptionalConfigurationFiles != null) {
    		optionalConfigurationFiles = immutableOptionalConfigurationFiles.mutableClone();
	    	for (int i = 0; i < optionalConfigurationFiles.count(); i ++) {
	    		String optionalConfigurationFile = (String)optionalConfigurationFiles.objectAtIndex(i);
	    		if (!new File(optionalConfigurationFile).exists()) {
		        	String resourcePropertiesPath = ERXFileUtilities.pathForResourceNamed(optionalConfigurationFile, "app", null);
		        	if (resourcePropertiesPath != null) {
		            	optionalConfigurationFiles.replaceObjectAtIndex(ERXProperties.getActualPath(resourcePropertiesPath), i);
		        	}
	    		}
	    	}
    	}
    	return optionalConfigurationFiles;
    }
    
    /**
     * <span class="en">
     * Returns actual full path to the given file system path  
     * that could contain symbolic links. For example: 
     * /Resources will be converted to /Versions/A/Resources
     * when /Resources is a symbolic link.
     * 
     * @param path - path string to a resource that could 
     *               contain symbolic links
     *               
     * @return actual path to the resource
     * </span>
     * 
     * <span class="ja">
     * 指定ファイル・システム・パスへの完全パスを戻します。（シンボリックリンク可）
     * 
     * 例えば、/Resources がシンボリックであれば、 /Resources は /Versions/A/Resources へコンバートされる
     * 
     * @param path - リソースへの文字列パス（シンボリックリンクである可能性があります）
     * 
     * @return リソースへの実際のパス
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static String getActualPath(String path) {
        String actualPath = null;
        File file = new File(path);
        try {
            actualPath = file.getCanonicalPath();
        } catch (Exception ex) {
            log.warn("The file at " + path + " does not seem to exist: " 
                + ex.getClass().getName() + ": " + ex.getMessage());
        }
        return actualPath;
    }
    
    /**
     * <span class="ja">システム・プロパティーが変更されたので、キャシュをクリアする</span>
     */
    public static void systemPropertiesChanged() {
        synchronized (AppSpecificPropertyNames) {
            AppSpecificPropertyNames.clear();
        }
        _cache.clear();
        // MS: Leave for future WO support ...
        NSNotificationCenter.defaultCenter().postNotification("PropertiesDidChange", null, null);
    }

    //	===========================================================================
    //	Instance Variable(s)
    //	---------------------------------------------------------------------------

    /** caches the application name that is appended to the key for lookup */
    protected String applicationNameForAppending;

    //	===========================================================================
    //	Instance Method(s)
    //	---------------------------------------------------------------------------

    /**
     * <span class="en">
     * Caches the application name for appending to the key.
     * Note that for a period when the application is starting up
     * application() will be null and name() will be null.
     * 
     * @return application name used for appending, for example ".ERMailer"
     * 
     * Note: this is redundant with the scheme checked in on March 21, 2005 by clloyd (ben holt did checkin).
     * This scheme requires the user to swizzle the existing properties file with a new one of this type.
     * </span>
     * 
     * <span class="ja">
     * キーに追加する為のアプリケーション名を戻します。
     * 注意：起動時には少しの間 application() と name() がまだ null のままのです。
     * 
     * @return キーに追加する為のアプリケーション名、例えば ".ERMailer"
     * </span>
     */
    @SuppressWarnings("javadoc")
	protected String applicationNameForAppending() {
        if (applicationNameForAppending == null) {
            applicationNameForAppending = WOApplication.application() != null ? WOApplication.application().name() : null;
            if (applicationNameForAppending != null) {
                applicationNameForAppending = "." + applicationNameForAppending;
            }
        }
        return applicationNameForAppending;
    }

    /**
     * <span class="en">
     * Overriding the default getProperty method to first check:
     * key.&lt;ApplicationName&gt; before checking for key. If nothing
     * is found then key.Default is checked.
     * @param key - to check
     * 
     * @return property value
     * </span>
     * 
     * <span class="ja">
     * 	デフォルト・プロパティーの getProperty メソッドをオーバライドします。
     * 
     * 	キー &lt;ApplicationName&gt; を先にチェックします。
     * 	何も見つからなければ、キーを使用します。
     * 
     * 	@param key - チェックするキー
     * 
     * 	@return プロパティー値
     * </span>
     */
    @SuppressWarnings("javadoc")
	@Override
    public String getProperty(String key) {
        String property = null;
        String application = applicationNameForAppending();
        if (application != null) {
            property = super.getProperty(key + application);
        }
        if (property == null) {
            property = super.getProperty(key);
            if (property == null) {
                property = super.getProperty(key + DefaultString);
            }
            // We go ahead and set the value to increase the lookup the next time the
            // property is accessed.
            if (property != null && application != null) {
                setProperty(key + application, property);
            }
        }
        return property;
    }

    /**
     * <span class="en">
     * Returns the properties as a String in Property file format. Useful when you use them 
     * as custom value types, you would set this as the conversion method name.
     * 
     * @return Returns the properties as a String in Property file format
     *
     * @throws IOException
     * </span>
     * 
     * <span class="ja">
     * プロパティーをプロパティー・ファイル・フォーマットの文字列として戻します。
     * カスタム値タイプとして使用する時に便利です。
     * コンバーション・メソッド名として使用するといい
     * 
     * @return プロパティーをプロパティー・ファイル・フォーマットの文字列
     * 
     * @throws IOException
     * </span>
     */
    // TODO The result isn't a Object it is a String
    @SuppressWarnings("javadoc")
	public Object toExternalForm() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        store(os, null);
        return new String(os.toByteArray());
    }
    
    /**
     * <span class="en">
     * Load the properties from a String in Property file format. Useful when you use them 
     * as custom value types, you would set this as the factory method name.
     * 
     * @param string
     * 
     * @return 
     * </span>
     * <span class="ja">
     * プロパティー・ファイル・フォーマットの文字列からプロパティーを読込みます。
     * カスタム値タイプとして使用する時に便利です。
     * ファクトリー・メソッド名として使用するといい
     * 
     * @param string - プロパティー・ファイル・フォーマットの文字列
     * 
     * @return 
     * </span>
     */
    @SuppressWarnings("javadoc")
	public static ERXProperties fromExternalForm(String string) {
        ERXProperties result = new ERXProperties();
        try {
			result.load(new ByteArrayInputStream(string.getBytes()));
		}
		catch (IOException e) {
			// AK: shouldn't ever happen...
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
        return result;
    }

    /**
     * KVC implementation.
     * @param anObject
     * @param aKey
     */
    public void takeValueForKey(Object anObject, String aKey) {
         setProperty(aKey, (anObject != null ? anObject.toString() : null));
    }

    /**
     * KVC implementation.
     *
     * @param aKey
     */
    public Object valueForKey(String aKey) {
         return getProperty(aKey);
    }

	/**
	 * <span class="en">Stores the mapping between operator keys and operators</span>
	 * 
	 * <span class="ja">オペレータ処理とオペレータ・キーのマップを保持しま</span>
	 */
	private static final NSMutableDictionary<String, ERXProperties.Operator> operators = new NSMutableDictionary<String, ERXProperties.Operator>();

	/**
	 * <span class="en">
	 * Registers a property operator for a particular key.
	 * 
	 * @param operator
	 *            the operator to register
	 * @param key
	 *            the key name of the operator
	 * </span>
	 * 
	 * <span class="ja">
	 * あるキーへのプロパティー・オペレータを登録します。
	 * 
	 * @param operator - 登録するオペレータ
	 * @param key - オペレータのキー名
	 * </span>
	 */
	public static void setOperatorForKey(ERXProperties.Operator operator, String key) {
		ERXProperties.operators.setObjectForKey(operator, key);
	}

	/**
	 * <span class="en">
	 * <p>
	 * Property operators work like array operators. In your properties, you can
	 * define keys like:
	 * </p>
	 * 
	 * <code>
	 * er.extensions.akey.@someOperatorKey.aparameter=somevalue
	 * </code>
	 * 
	 * <p>
	 * Which will be processed by the someOperatorKey operator. Because
	 * properties get handled very early in the startup process, you should
	 * register operators somewhere like a static block in your Application
	 * class. For instance, if you wanted to register the forInstance operator,
	 * you might put the following your Application class:
	 * </p>
	 * 
	 * <code>
	 * static {
	 *   ERXProperties.setOperatorForKey(new ERXProperties.InRangeOperator(100), ERXProperties.InRangeOperator.ForInstanceKey);
	 * }
	 * </code>
	 * 
	 * <p>
	 * It's important to note that property operators evaluate at load time, not
	 * access time, so the compute function should not depend on any runtime
	 * state to execute. Additionally, access to other properties inside the
	 * compute method should be very carefully considered because it's possible
	 * that the operators are evaluated before all of the properties in the
	 * system are loaded.
	 * </p>
	 * </span>
	 * 
	 * <span class="ja">
	 * <p>
	 * プロパティー・オペレータは配列オペレータと同じ様に動作します。
	 * あなたのプロパティー内には次のようにキーを作成が可能です：
	 * </p>
	 * 
	 * <code>er.extensions.akey.@someOperatorKey.aparameter=somevalue</code>
	 * 
	 * <p>
	 * someOperatorKey オペレータより処理されます。アプリケーション起動時に、
	 * プロパティーが早い段階で処理されるので、オペレータをアプリケーションクラスのスタティックで宣言するといいのです。
	 * </p>
	 * 
	 * <code>
	 * static {
	 *   ERXProperties.setOperatorForKey(new ERXProperties.InRangeOperator(100), ERXProperties.InRangeOperator.ForInstanceKey);
	 * }
	 * </code>
	 * 
	 * <p>
	 * 大変重要はプロパティー・オペレータがロード時で処理されるので、アクセス時ではありません。
	 * 計算されている関数ステータスがランタイム状況に左右されないようにする必要がある。
	 * 
	 * 追伸：計算オペレータ・メソッド内で他のプロパティーをアクセスする時にも注意が必要です。
	 * なぜなら、計算時にまだシステムにロードされていない可能性があるからです。
	 * </p>
	 * </span>
	 * 
	 * @author mschrag
	 */
	public static interface Operator {
		/**
		 * <span class="en">
		 * Performs some computation on the key, value, and parameters and
		 * returns a dictionary of new properties. If this method returns null,
		 * the original key and value will be used. If any other dictionary is
		 * returned, the properties in the dictionary will be copied into the
		 * destination properties.
		 * 
		 * @param key
		 *            the key ("er.extensions.akey" in
		 *            "er.extensions.akey.@someOperatorKey.aparameter=somevalue")
		 * @param value
		 *            ("somevalue" in
		 *            "er.extensions.akey.@someOperatorKey.aparameter=somevalue")
		 * @param parameters
		 *            ("aparameter" in
		 *            "er.extensions.akey.@someOperatorKey.aparameter=somevalue")
		 *            
		 * @return a dictionary of properties (or null to use the original key
		 *         and value)
		 * </span>
		 * <span class="ja">
		 * キー、値とパラメータを処理し、新プロパティーのディクショナリーを戻します。
		 * もし、このメソッドが null を戻すなら、オリジナル・キーと値が使用されます。
		 * 
		 * 他のディクショナリーが戻される、ディクショナリーのプロパティーは
		 * ターゲット・プロパティーにコピーされます。
		 * 
		 * @param key - キー　
		 * 		("er.extensions.akey.@someOperatorKey.aparameter=somevalue" 内の "er.extensions.akey")
		 * @param value - 値 
		 * 		("er.extensions.akey.@someOperatorKey.aparameter=somevalue" 内の "somevalue")
		 * @param parameters - パラメータ 
		 * 		("er.extensions.akey.@someOperatorKey.aparameter=somevalue" 内の "aparameter")
		 * 
		 * @return プロパティーのディクショナリー (null の場合にはオリジナルのキーと値が使用)
		 * </span>
		 */
		@SuppressWarnings("javadoc")
		public NSDictionary<String, String> compute(String key, String value, String parameters);
	}

	/**
	 * <span class="en">
	 * <p>
	 * InRangeOperator provides support for defining properties that only
	 * get set if a value falls within a specific range of values.
	 * </p>
	 * 
	 * <p>
	 * An example of this is instance-number-based properties, where you want to 
	 * only set a specific value if the instance number of the application falls
	 * within a certain value. In this example, because instance number is 
	 * something that is associated with a request rather than the application 
	 * itself, it is up to the class registering this operator to specify which 
	 * instance number this application is (via, for instance, a custom system property).
	 * </p>
	 * 
	 * <p>
	 * InRangeOperator supports specifying keys like:
	 * </p>
	 * 
	 * <code>er.extensions.akey.@forInstance.50=avalue</code>
	 * <p>
	 * which would set the value of "er.extensions.akey" to "avalue" if this
	 * instance is 50.
	 * </p>
	 * 
	 * <code>er.extensions.akey.@forInstance.60,70=avalue</code>
	 * <p>
	 * which would set the value of "er.extensions.akey" to "avalue" if this
	 * instance is 60 or 70.
	 * </p>
	 * 
	 * <code>er.extensions.akey.@forInstance.100-300=avalue</code>
	 * <p>
	 * which would set the value of "er.extensions.akey" to "avalue" if this
	 * instance is between 100 and 300 (inclusive).
	 * </p>
	 * 
	 * <code>er.extensions.akey.@forInstance.20-30,500=avalue</code>
	 * <p>
	 * which would set the value of "er.extensions.akey" to "avalue" if this
	 * instance is between 20 and 30 (inclusive), or if the instance is 50.
	 * </p>
	 * 
	 * <p>
	 * If there are multiple inRange operators that match for the same key,
	 * the last property (when sorted alphabetically by key name) will win. As a
	 * result, it's important to not define overlapping ranges, or you
	 * may get unexpected results.
	 * </p>
	 * </span>
	 * 
	 * <span class="ja">
	 * <p>InRangeOperator は指定レンジ内のあるプロパティーをセットする処理をサポートします。</p>
	 * 
	 * <p>このメソッドのサンプルとして、インスタンス番号ベース・プロパティーです。
	 * 指定値をプロパティーとしてセットするのはインスタンス番号がある値の間にある時だけ。
	 * 例えば、
	 * インスタンス番号はアプリケーションよりも、リクエストと関連されています。
	 * クラス登録によって、このオペレータがインスタンス番号がわかるのです。</p>
	 * 
	 * <p>InRangeOperator は次のようなキーをサポートしています：</p>
	 * 
	 * <code>er.extensions.akey.@forInstance.50=avalue</code>
	 * <p>インスタンスが50になると "er.extensions.akey" の値を "avalue" にセットします。</p>
	 * 
	 * <code>er.extensions.akey.@forInstance.60,70=avalue</code>
	 * <p>インスタンスが60又は70の場合 "er.extensions.akey" の値を "avalue" にセットします。</p>
	 * 
	 * <code>er.extensions.akey.@forInstance.100-300=avalue</code>
	 * <p>インスタンスが100から300の間にある場合 "er.extensions.akey" の値を "avalue" にセットします。</p>
	 * 
	 * <code>er.extensions.akey.@forInstance.20-30,50=avalue</code>
	 * <p>インスタンスが20から30の間、又は50の場合 "er.extensions.akey" の値を "avalue" にセットします。</p>
	 * 
	 * <p>複数の処理が同じキーでヒットすると、最後のプロパティー（キー名でソートされて）が採用されます。
	 * 結果として、オーバラップされる値をセットしないように、そうしないと思わない結果が得られます。</p>
	 * </span>
	 * 
	 * @author mschrag
	 */
	public static class InRangeOperator implements ERXProperties.Operator {
		/**
		 * <span class="en">The default key name of the ForInstance variant of the InRange operator.</span>
		 * <span class="ja">InRangeオペレータのインスタンス・デフォルト・キー名</span>
		 */
		public static final String ForInstanceKey = "forInstance";

		private int _instanceNumber;

		/**
		 * <span class="en">
		 * Constructs a new InRangeOperator.
		 * 
		 * @param value
		 *            the instance number of this application
		 * </span>
		 * 
		 * <span class="ja">
	     * 新規 InRangeOperator を作成します。「コンストラクタ」
	     * 
	     * @param value - このアプリケーションのインスタンス番号
	     * </span>
		 */
		public InRangeOperator(int value) {
			_instanceNumber = value;
		}

		public NSDictionary<String, String> compute(String key, String value, String parameters) {
			NSDictionary computedProperties = null;
			if (parameters != null && parameters.length() > 0) {
				if (ERXStringUtilities.isValueInRange(_instanceNumber, parameters)) {
					computedProperties = new NSDictionary(value, key);
				}
				else {
					computedProperties = NSDictionary.EmptyDictionary;
				}
			}
			return computedProperties;
		}
	}

	/**
	 * <span class="en">
	 * <p>
	 * Encrypted operator supports decrypting values using the default crypter. To register this
	 * operator, add the following static block to your Application class:
	 * </p>
	 * 
	 * <code>
	 * static {
	 *   ERXProperties.setOperatorForKey(new ERXProperties.EncryptedOperator(), ERXProperties.EncryptedOperator.Key);
	 * }
	 * </code>
	 * 
	 * Call er.extensions.ERXProperties.EncryptedOperator.register() in an Application static
	 * block to register this operator.
	 * </span>
	 * 
	 * <span class="ja">
	 * <p>暗号オペレータ・サポート。デフォルト暗号化処理が使用されます。</p>
	 * 
	 * <code>
	 * static {
	 *   ERXProperties.setOperatorForKey(new ERXProperties.EncryptedOperator(), ERXProperties.EncryptedOperator.Key);
	 * }
	 * </code>
	 * 
	 * er.extensions.ERXProperties.EncryptedOperator.register() をスタティック・ブロック内で呼ぶことで登録できます。
	 * </span>
	 * 
	 * @author mschrag
	 */
	public static class EncryptedOperator implements ERXProperties.Operator {
		public static final String Key = "encrypted";

		public static void register() {
			ERXProperties.setOperatorForKey(new ERXProperties.EncryptedOperator(), ERXProperties.EncryptedOperator.Key);
		}

		public NSDictionary<String, String> compute(String key, String value, String parameters) {
			String decryptedValue = ERXCrypto.defaultCrypter().decrypt(value);
			return new NSDictionary<String, String>(decryptedValue, key);
		}
	}

	/**
	 * <span class="ja">
	 * 	<p>InIpRangeOperator は指定IPレンジ内のあるプロパティーをセットする処理をサポートします。</p>
	 * 
	 * 	各 IP アドレスの優先順の設定できます。
	 * 	<code>er.erxtensions.ERXTcpIp.IpPriority.[[ip address]]</code>
	 * 	優先順の設定 0 - 9999
	 * 	例：er.erxtensions.ERXTcpIp.IpPriority.10.0.1.97 = 5
	 * 
	 * 	動作マシンのIPアドレス
	 * 	<code>er.erxtensions.ERXTcpIp.UseThisIp</code>
	 * 	直接IPを記述するとマシンのIPは読み取らず、記述されたIPを使用し、各設定値を読み込みます。
	 * 	この指定が無ければ、自動設定が行われます。
	 * 	例：er.erxtensions.ERXTcpIp.UseThisIp = 192.168.1.68
	 * 
	 * 	er.erxtensions.ERXTcpIp.UseThisIp アドレスが設定され、自動で IP 特定できない場合
	 * 	<code>er.erxtensions.ERXTcpIp.NoIpAndNoNetwork</code>
	 * 	（ネットワークに接続されていない状態）にこのIPをMachineIpとして使用します。
	 * 	もし、このプロパティーも設定していなければ、ローカル・アドレスが使用されます：127.0.0.1
	 * 	例：er.erxtensions.ERXTcpIp.NoIpAndNoNetwork = 192.168.1.220
	 * 
	 * 
	 * 	<p>InIpRangeOperator は次のようなキーをサポートしています：</p>
	 * 
	 * 	<code>sampleip1.@forIP.192.168.1.68 = avalue</code>
	 * 	<p>IPアドレスが 192.168.1.68 になると "sampleip1" の値を "avalue" にセットします。</p>
	 * 
	 * 	<code>test.sampleip2.@forIP.192.168.1.67,192.168.1.68 = avalue</code>
	 * 	<p>IPアドレスが 192.168.1.67 又は 192.168.1.68 の場合 "test.sampleip2" の値を "avalue" にセットします。</p>
	 * 
	 * 	<code>test.sampleip3.@forIP.192.168.1.50-192.168.1.90 = avalue</code>
	 * 	<p>IPアドレスが 192.168.1.50 から 192.168.1.90 の間にある場合 "test.sampleip3" の値を "avalue" にセットします。</p>
	 * 
	 * 	<code>test.sampleip4.@forIP.192.168.1.50-192.168.1.90,127.0.0.1 = avalue</code>
	 * 	<p>IPアドレスが 192.168.1.50 から 192.168.1.90 の間、又は 127.0.0.1 の場合 "test.sampleip4" の値を "avalue" にセットします。</p>
	 * 
	 * 	<p>複数の処理が同じキーでヒットすると、最後のプロパティー（キー名でソートされて）が採用されます。
	 * 	結果として、オーバラップされる値をセットしないように、そうしないと思わない結果が得られます。</p>
	 * 
	 *  @property er.erxtensions.ERXTcpIp.UseThisIp
	 *  @property er.erxtensions.ERXTcpIp.NoIpAndNoNetwork
	 *  @property er.erxtensions.ERXTcpIp.IpPriority.{IP Address}
	 *  
	 * 	@author tani & fukui & ishimoto 
	 * </span>
	 */
	public static class InIpRangeOperator implements ERXProperties.Operator {

		/** 
		 * <span class="ja">InIpRangeオペレータのインスタンス・デフォルト・キー名</span>
		 */
		public static final String ForInstanceKey = "forIP";

		/** 
		 * <span class="ja">IP 配列：キャシュ用</span> 
		 */
		private NSArray<String> _ipAddress;

		/**
		 * <span class="ja">
		 * 	新規 InIpRangeOperator を作成します。「コンストラクタ」
		 * 
		 * 	@param ipList - このマシンの IP 配列
		 * </span>
		 */
		public InIpRangeOperator(NSArray<String> ipList) {
			_ipAddress = ipList;
		}

		/**
		 * この InIpRangeOperator の使用を登録します。A10Application 内より実行されます
		 */
		public static void register() {
			ERXProperties.setOperatorForKey(new ERXProperties.InIpRangeOperator(ERXTcpIp.machineIpList()), ERXProperties.InIpRangeOperator.ForInstanceKey);
		}

		public NSDictionary<String, String> compute(String key, String value, String parameters) {
			NSDictionary<String, String> computedProperties = null;

			if (parameters != null && parameters.length() > 0) {
				boolean ipNumberMatches = false;
				String[] ranges = parameters.split(",");

				for (String range : ranges) {
					range = range.trim();
					int dashIndex = range.indexOf('-');

					if (dashIndex == -1) {
						String singleIP = range;
						if (_ipAddress.contains(singleIP)){
							ipNumberMatches = true;
							break;
						}
					}
					else {
						String lowValue = range.substring(0, dashIndex).trim();
						String highValue = range.substring(dashIndex + 1).trim();

						for(String obj: _ipAddress) {
							if(ERXTcpIp.isInet4IPAddressWithinRange(lowValue, obj, highValue)) {
								ipNumberMatches = true;
								break;
							}
						}
					}
				}

				if (ipNumberMatches) {
					computedProperties = new NSDictionary<String, String>(value, key);
				}
				else {
					computedProperties = new NSDictionary<String, String>();
				}
			}
			return computedProperties;
		}
	}

	/**
	 * <span class="en">
	 * For each property in originalProperties, process the keys and values with
	 * the registered property operators and stores the converted value into
	 * destinationProperties.
	 * 
	 * @param originalProperties
	 *            the properties to convert
	 * @param destinationProperties
	 *            the properties to copy into
	 * </span>
	 * 
	 * <span class="ja">
	 * オリジナル・プロパティーの各プロパティーのキーと値を登録されているオペレータで処理し、
	 * 変換されている結果をターゲット・プロパティーに記録します。
	 * ※ ERXSystemより呼ばれる ※
	 * 
	 * @param originalProperties - オリジナル・プロパティー
	 * @param destinationProperties - 変換済みのターゲット・プロパティー
	 * </span>
	 */
	public static void evaluatePropertyOperators(Properties originalProperties, Properties destinationProperties) {
		NSArray<String> operatorKeys = ERXProperties.operators.allKeys();
		for (Object keyObj : new TreeSet<Object>(originalProperties.keySet())) {
			String key = (String) keyObj;
			if (key != null && key.length() > 0) {
				String value = originalProperties.getProperty(key);
				if (operatorKeys.count() > 0 && key.indexOf(".@") != -1) {
					ERXProperties.Operator operator = null;
					NSDictionary<String, String> computedProperties = null;
					for (String operatorKey : operatorKeys) {
						String operatorKeyWithAt = ".@" + operatorKey;
						if (key.endsWith(operatorKeyWithAt)) {
							operator = ERXProperties.operators.objectForKey(operatorKey);
							computedProperties = operator.compute(key.substring(0, key.length() - operatorKeyWithAt.length()), value, null);
							break;
						}
						int keyIndex = key.indexOf(operatorKeyWithAt + ".");
						if (keyIndex != -1) {
							operator = ERXProperties.operators.objectForKey(operatorKey);
							computedProperties = operator.compute(key.substring(0, keyIndex), value, key.substring(keyIndex + operatorKeyWithAt.length() + 1));
							break;
						}
					}

					if (computedProperties == null) {
						destinationProperties.put(key, value);
					}
					else {
						originalProperties.remove(key);
						
						// If the key exists in the System properties' defaults with a different value, we must reinsert
						// the property so it doesn't get overwritten with the default value when we evaluate again.
						// This happens because ERXConfigurationManager processes the properties after a configuration
						// change in multiple passes and each calls this method.
						if (System.getProperty(key) != null && !System.getProperty(key).equals(value)) {
							originalProperties.put(key, value);
						}
						
						for (String computedKey : computedProperties.allKeys()) {
							destinationProperties.put(computedKey, computedProperties.objectForKey(computedKey));
						}
					}
				}
				else {
					destinationProperties.put(key, value);
				}
			}
		}
	}

	/**
	 * For every application specific property, this method generates a similar property without
	 * the application name in the property key. The original property is not removed.
	 * <p>
	 * Ex: if current application is MyApp, for a property foo.bar.MyApp=true a new property
	 * foo.bar=true is generated.
	 * </p>
	 * 
	 * @param properties Properties to update
	 */
// xxxxxxxxxxxxxxxxxxx
// This is more complex than it needs to be. Can just use endsWith....
//
	public static void flattenPropertyNames(Properties properties) {
	    if (_useLoadtimeAppSpecifics == false) {
	        return;
	    }
	    
	    WOApplication application = WOApplication.application();
	    if (application == null) {
	        return;
	    }
	    String applicationName = application.name();
	    for (Object keyObj : new TreeSet<Object>(properties.keySet())) {
	        String key = (String) keyObj;
	        if (key != null && key.length() > 0) {
	            String value = properties.getProperty(key);
	            int lastDotPosition = key.lastIndexOf(".");
	            if (lastDotPosition != -1) {
	                String lastElement = key.substring(lastDotPosition + 1);
	                if (lastElement.equals(applicationName)) {
	                    properties.put(key.substring(0, lastDotPosition), value);
	                }
	            }
	        }
	    }
	}

	/**
	 * <span class="en">
	 * _Properties is a subclass of Properties that provides support for including other
	 * Properties files on the fly.  If you create a property named .includeProps, the value
	 * will be interpreted as a file to load.  If the path is absolute, it will just load it
	 * directly.  If it's relative, the path will be loaded relative to the current user's
	 * home directory.  Multiple .includeProps can be included in a Properties file and they
	 * will be loaded in the order they appear within the file.
	 * </span>
	 * 
	 * <span class="ja">
     * 	_Properties は Properties のサブクラスでプロパティ・ファイルのオンザフライ読み込みを可能にします。
     * 	.includeProps のようなプロパティ名を作成するとファイルとして認識され、読み込まれます。
     * 	完全パスの場合には直接読み込みを行います。相違パスの場合はカレント・ユーザのホームフォルダ対象になります。
     * 	プロパティ・ファイルには複数の .includeProps を含むことができ、発生される順番でロードされます。
     * </span>
	 *  
	 * @author mschrag
	 */
	public static class _Properties extends Properties {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		public static final String IncludePropsKey = ".includeProps";
		
		private Stack<File> _files = new Stack<File>();
		
		@Override
		public synchronized Object put(Object key, Object value) {
			if (_Properties.IncludePropsKey.equals(key)) {
				String propsFileName = (String)value;
                File propsFile = new File(propsFileName);
                if (!propsFile.isAbsolute()) {
                    // if we don't have any context for a relative (non-absolute) props file,
                    // we presume that it's relative to the user's home directory
    				File cwd = null;
    				if (_files.size() > 0) {
    					cwd = _files.peek();
    				}
    				else {
    					cwd = new File(System.getProperty("user.home"));
                	}
                    propsFile = new File(cwd, propsFileName);
                }

                // Detect mutually recursing props files by tracking what we've already loaded:
                String existingIncludeProps = getProperty(_Properties.IncludePropsKey);
                if (existingIncludeProps == null) {
                	existingIncludeProps = "";
                }
                if (existingIncludeProps.indexOf(propsFile.getPath()) > -1) {
                    log.error("_Properties.load(): recursive includeProps detected! " + propsFile + " in " + existingIncludeProps);
                    log.error("_Properties.load() cannot proceed - QUITTING!");
                    System.exit(1);
                }
                if (existingIncludeProps.length() > 0) {
                	existingIncludeProps += ", ";
                }
                existingIncludeProps += propsFile;
                super.put(_Properties.IncludePropsKey, existingIncludeProps);

                try {
                    log.info("_Properties.load(): Including props file: " + propsFile);
					load(propsFile);
				} catch (IOException e) {
					throw new RuntimeException("Failed to load the property file '" + value + "'.", e);
				}
				return null;
			}
			return super.put(key, value);
		}

		public synchronized void load(File propsFile) throws IOException {
			_files.push(propsFile.getParentFile());
			try {
	            BufferedInputStream is = new BufferedInputStream(new FileInputStream(propsFile));
	            try {
	            	load(is);
	            }
	            finally {
	            	is.close();
	            }
			}
			finally {
				_files.pop();
			}
		}
	}

	@Deprecated
	public static void setCommandLineArguments(String[] argv) {
		
	}
	
	/**
	 * <span class="ja">
	 * 	システム・プロパティーの結果を String で戻します。
	 * 	最大2つのキーを指定可能で、最初に見つかったキーを使用します。
	 * 
	 * 	@param s1 - キー1（システム・プロパティー内）
	 * 	@param s2 - キー2（システム・プロパティー内）
	 * 
	 * 	@return String 値、デフォルトはnull
	 * 
	 * 	@author A10 nettani
	 * </span>
	 */    
	public static String stringFor2Keys(String s1, String s2) {
		return stringForManyKeys(ERXValueUtilities.stringsToStringArray(s1, s2));
	}
	
	/**
	 * <span class="ja">
	 * 	システム・プロパティーの結果を String で戻します。
	 * 	最大2つのキーを指定可能で、最初に見つかったキーを使用します。
	 * 
	 * 	@param s1 - キー1（システム・プロパティー内）
	 * 	@param s2 - キー2（システム・プロパティー内）
	 * 	@param defaultValue - デフォルト値
	 * 
	 * 	@return String 値、デフォルトはdefaultValue
	 * 
	 * 	@author A10 nettani
	 * </span>
	 */    
	public static String stringFor2KeysWithDefault(String s1, String s2, final String defaultValue) {
		return stringForManyKeysWithDefault(ERXValueUtilities.stringsToStringArray(s1, s2), defaultValue);
	}

	/**
	 * <span class="ja">
	 * 	システム・プロパティーの結果を String で戻します。
	 * 	最大3つのキーを指定可能で、最初に見つかったキーを使用します。
	 * 
	 * 	@param s1 - キー1（システム・プロパティー内）
	 * 	@param s2 - キー2（システム・プロパティー内）
	 * 	@param s3 - キー3（システム・プロパティー内）
	 * 
	 * 	@return String 値、デフォルトは null
	 * 
	 * 	@author A10 nettani
	 * </span>
	 */    
	public static String stringFor3Keys(String s1, String s2, String s3) {
		return stringForManyKeys(ERXValueUtilities.stringsToStringArray(s1, s2, s3));
	}

	/**
	 * <span class="ja">
	 * 	システム・プロパティーの結果を String で戻します。
	 * 	最大3つのキーを指定可能で、最初に見つかったキーを使用します。
	 * 
	 * 	@param s1 - キー1（システム・プロパティー内）
	 * 	@param s2 - キー2（システム・プロパティー内）
	 * 	@param s3 - キー3（システム・プロパティー内）
	 * 	@param defaultValue - デフォルト値
	 * 
	 * 	@return String 値、デフォルトはdefaultValue
	 * 
	 * 	@author A10 nettani
	 * </span>
	 */    
	public static String stringFor3KeysWithDefault(String s1, String s2, String s3, final String defaultValue) {
		return stringForManyKeysWithDefault(ERXValueUtilities.stringsToStringArray(s1, s2, s3), defaultValue);
	}

	/**
	 * <span class="ja">
	 * 	システム・プロパティーの結果を String で戻します。
	 * 	キーは文字列配列で渡され配列要素位置0から開始し最初に見つかったキーを使用します。
	 * 
	 * 	@param ss - キーs（システム・プロパティー内）
	 * 
	 * 	@return String 値、デフォルトはnull
	 * 
	 * 	@author A10 nettani
	 * </span>
	 */    
	public static String stringForManyKeys(String[] ss) {
		return stringForManyKeysWithDefault(ss,null);
	}

	/**
	 * <span class="ja">
	 * 	システム・プロパティーの結果やデフォルト値を String で戻します。
	 * 	キーは文字列配列で渡され配列要素位置0から開始し最初に見つかったキーを使用します。
	 * 
	 * 	@param ss - キーs（システム・プロパティー内）
	 * 	@param defaultValue - デフォルト値
	 * 
	 * 	@return String 値
	 * 
	 * 	@author A10 nettani
	 * </span>
	 */
	public static String stringForManyKeysWithDefault(final String[] ss, final String defaultValue) {
		if((ss == null) || (ss.length == 0)) return defaultValue;	// 文字列配列が無いならdefaultValue
		int count = ss.length;
		String value = null;
		for(int loop = 0; loop < count; loop++){
			if((ss[loop] != null) && (ss[loop].length() > 0)){
				value = stringForKey(ss[loop]);
				if(!ERXStringUtilities.stringIsNullOrEmpty(value))
					return stringForKey(ss[loop]);
			}
		}
		return defaultValue;
	}

}
