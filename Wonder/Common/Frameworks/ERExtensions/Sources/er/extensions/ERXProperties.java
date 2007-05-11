/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver._private.WOProjectBundle;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSProperties;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * Collection of simple utility methods used to get and set properties
 * in the system properties. The only reason this class is needed is
 * because all of the methods in NSProperties have been deprecated.
 * This is a wee bit annoying. The usual method is to have a method
 * like <code>getBoolean</code> off of Boolean which would resolve
 * the System property as a Boolean object.
 */
public class ERXProperties extends Properties implements NSKeyValueCoding {

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

    /** Internal cache of type converted values to avoid reconverting attributes that are asked for frequently */
    private static Map _cache = Collections.synchronizedMap(new HashMap());

    private static boolean retainDefaultsEnabled() {
        if (RetainDefaultsEnabled == null) {
            final String propertyValue = ERXSystem.getProperty("er.extensions.ERXProperties.RetainDefaultsEnabled", "false");
            final boolean isEnabled = "true".equals(propertyValue);
            RetainDefaultsEnabled = Boolean.valueOf(isEnabled);
        }
        return RetainDefaultsEnabled.booleanValue();
    }

    /**
     * Puts handy properties such as <code>com.webobjects.version</code> 
     * into the system properties. This method is called when 
     * the framework is initialized  
     * (when WOApplication.ApplicationWillFinishLaunchingNotification 
     * is posted.)
     */
    public static void populateSystemProperties() {
        System.setProperty("com.webobjects.version", webObjectsVersion());
    }

    /** 
     * Returns the version string of the application.  
     * It checks <code>CFBundleShortVersionString</code> property 
     * in the <code>info.plist</code> resource and returns 
     * a trimmed version of the value. 
     * 
     * @return version number as string; can be a null-string when 
     *          the applicaiton doesn't have the value of
     *          <code>CFBundleShortVersionString</code>
     *                  in its <code>info.plist</code> resource. 
     * @see #versionStringForFrameworkNamed
     * @see #webObjectsVersion
     */ 
    public static String versionStringForApplication() {
        NSBundle bundle = NSBundle.mainBundle();
        String versionString = (String) bundle.infoDictionary()
                                            .objectForKey("CFBundleShortVersionString");
        return versionString == null  ?  ""  :  versionString.trim(); // remove the line ending char
    }

    /** 
     * Returns the version string of the given framework.
     * It checks <code>CFBundleShortVersionString</code> property 
     * in the <code>info.plist</code> resource and returns 
     * a trimmed version of the value. 
     * 
     * @param  frameworkName name
     * @return version number as string; can be null-string when 
     *          the framework is not found or the framework
     *          doesn't have the value of
     *                  <code>CFBundleShortVersionString</code> in its
     *                  <code>info.plist</code> resource.
     * @see #versionStringForApplication
     * @see #webObjectsVersion
     * @see ERXStringUtilities#removeExtraDotsFromVersionString
     */ 
    public static String versionStringForFrameworkNamed(String frameworkName) {
        NSBundle bundle = NSBundle.bundleForName(frameworkName);
        if (bundle == null)  return "";
        
        String versionString = (String) bundle.infoDictionary()
                                            .objectForKey("CFBundleShortVersionString");
        return versionString == null  ?  ""  :  versionString.trim(); // trim() removes the line ending char
    }

    /**
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
     * @see #versionStringForApplication
     * @see #webObjectsVersion
     */
    public static String sourceVersionString() {
        NSBundle bundle = NSBundle.bundleForName("JavaWebObjects");
        if (bundle == null)  return "";
        String dictString = new String(bundle.bytesForResourcePath("version.plist"));
        NSDictionary versionDictionary = NSPropertyListSerialization.dictionaryForString(dictString);

        String versionString = (String) versionDictionary.objectForKey("SourceVersion");
        return versionString == null  ?  ""  :  versionString.trim(); // trim() removes the line ending char
    }

    /** 
     * Returns WebObjects version as string. If it's one of those 
     * version 5.1s (5.1, 5.1.1, 5.1.2...), this method will only 
     * return 5.1. If it's 5.2s, this mothod will return more precise 
     * version numbers such as 5.2.1. Note that version 5.0 series 
     * is not supported and may return incorrect version numbers 
     * (it will return 5.1). 
     * 
     * @return WebObjects version number as string
     * @see #webObjectsVersionAsDouble
     * @see ERXStringUtilities#removeExtraDotsFromVersionString
     */ 
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
     * Returns WebObjects version as double. If it's one of those 
     * version 5.1s (5.1, 5.1.1, 5.1.2...), this method will only 
     * return 5.1. If it's 5.2s, this mothod will return more precise 
     * version numbers such as 5.2.1. Note that version 5.0 series 
     * is not supported and may return incorrect version numbers 
     * (it will return 5.1). 
     * 
     * @return WebObjects version number as double
     * @see #webObjectsVersion
     */
    public static double webObjectsVersionAsDouble() {
        if (_webObjectsVersionDouble == 0.0d) {
            String woVersionString = ERXStringUtilities.removeExtraDotsFromVersionString(webObjectsVersion());
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
     * Quick convience method used to determine if the current
     * webobjects version is 5.2 or higher.
     * @return if the version of webobjects is 5.2 or better
     */
    public static boolean webObjectsVersionIs52OrHigher() {
        if(ERXProperties.booleanForKey("er.extensions.ERXProperties.checkOldVersions")) {
            return webObjectsVersionAsDouble() >= 5.2d;
        }
        return true;
    }

    /**
     * Quick convience method used to determine if the current
     * webobjects version is 5.22 or higher.
     * @return if the version of webobjects is 5.22 or better
     */
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
     * Cover method for returning an NSArray for a
     * given system property.
     * @param s system property
     * @return array de-serialized from the string in
     *      the system properties
     */
    public static NSArray arrayForKey(String s) {
        return arrayForKeyWithDefault(s, null);
    }

    /**
     * Converts the standard propertyName into one with a .&lt;AppName> on the end, iff the property is defined with
     * that suffix.  If not, then this caches the standard propertyName.  A cache is maintained to avoid concatenating
     * strings frequently, but may be overkill since most usage of this system doesn't involve frequent access.
     * @param propertyName
     * @return
     */
    private static String getApplicationSpecificPropertyName(final String propertyName) {
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
     * Cover method for returning an NSArray for a
     * given system property and set a default value if not given.
     * @param s system property
     * @param defaultValue default value
     * @return array de-serialized from the string in
     *      the system properties or default value
     */
    public static NSArray arrayForKeyWithDefault(final String s, final NSArray defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);

        Object value = _cache.get(propertyName);
        if (value == UndefinedMarker) {
            return defaultValue;
        }
        if (value instanceof NSArray) {
            return (NSArray)value;
        }
        
        final String propertyValue = ERXSystem.getProperty(propertyName);
        final NSArray array = ERXValueUtilities.arrayValueWithDefault(propertyValue, defaultValue);
        if (retainDefaultsEnabled() && propertyValue == null && array != null) {
            setArrayForKey(array, propertyName);
        }
        _cache.put(propertyName, propertyValue == null ? (Object)UndefinedMarker : array);
        return array;
    }
    
    /**
     * Cover method for returning a boolean for a
     * given system property. This method uses the
     * method <code>booleanValue</code> from
     * {@link ERXUtilities}.
     * @param s system property
     * @return boolean value of the string in the
     *      system properties.
     */    
    public static boolean booleanForKey(String s) {
        return booleanForKeyWithDefault(s, false);
    }

    /**
     * Cover method for returning a boolean for a
     * given system property or a default value. This method uses the
     * method <code>booleanValue</code> from
     * {@link ERXUtilities}.
     * @param s system property
     * @param defaultValue default value
     * @return boolean value of the string in the
     *      system properties.
     */
    public static boolean booleanForKeyWithDefault(final String s, final boolean defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);
        
        Object value = _cache.get(propertyName);
        if (value == UndefinedMarker) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        }
        
        String propertyValue = ERXSystem.getProperty(propertyName);
        final boolean booleanValue = ERXValueUtilities.booleanValueWithDefault(propertyValue, defaultValue);
        if (retainDefaultsEnabled() && propertyValue == null) {
            propertyValue = Boolean.toString(booleanValue);
            System.setProperty(propertyName, propertyValue);
        }
        _cache.put(propertyName, propertyValue == null ? (Object)UndefinedMarker : Boolean.valueOf(booleanValue));
        return booleanValue;
    }
    
    /**
     * Cover method for returning an NSDictionary for a
     * given system property.
     * @param s system property
     * @return dictionary de-serialized from the string in
     *      the system properties
     */    
    public static NSDictionary dictionaryForKey(String s) {
        return dictionaryForKeyWithDefault(s, null);
    }

    /**
     * Cover method for returning an NSDictionary for a
     * given system property or the default value.
     * @param s system property
     * @param defaultValue default value
     * @return dictionary de-serialized from the string in
     *      the system properties
     */
    public static NSDictionary dictionaryForKeyWithDefault(final String s, final NSDictionary defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);

        Object value = _cache.get(propertyName);
        if (value == UndefinedMarker) {
            return defaultValue;
        }
        if (value instanceof NSDictionary) {
            return (NSDictionary)value;
        }
        
        final String propertyValue = ERXSystem.getProperty(propertyName);
        final NSDictionary dictionary = ERXValueUtilities.dictionaryValueWithDefault(propertyValue, defaultValue);
        if (retainDefaultsEnabled() && propertyValue == null && dictionary != null) {
            setDictionaryForKey(dictionary, propertyName);
        }
        _cache.put(propertyName, propertyValue == null ? (Object)UndefinedMarker : dictionary);
        return dictionary;
    }

    /**
     * Cover method for returning an int for a
     * given system property.
     * @param s system property
     * @return int value of the system property or 0
     */
    public static int intForKey(String s) {
        return intForKeyWithDefault(s, 0);
    }

    /**
     * Cover method for returning a long for a
     * given system property.
     * @param s system property
     * @return long value of the system property or 0
     */
    public static long longForKey(String s) {
        return longForKeyWithDefault(s, 0);
    }

    /**
     * Cover method for returning a BigDecimal for a
     * given system property. This method uses the
     * method <code>bigDecimalValueWithDefault</code> from
     * {@link ERXValueUtilities}.
     * @param s system property
     * @return bigDecimal value of the string in the
     *      system properties.  Scale is controlled by the string, ie "4.400" will have a scale of 3.
     */
    public static BigDecimal bigDecimalForKey(String s) {
        return bigDecimalForKeyWithDefault(s,null);
    }

    /**
     * Cover method for returning a BigDecimal for a
     * given system property or a default value. This method uses the
     * method <code>bigDecimalValueWithDefault</code> from
     * {@link ERXValueUtilities}.
     * @param s system property
     * @param defaultValue default value
     * @return BigDecimal value of the string in the
     *      system properties. Scale is controlled by the string, ie "4.400" will have a scale of 3.
     */
    public static BigDecimal bigDecimalForKeyWithDefault(String s, BigDecimal defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);

        Object value = _cache.get(propertyName);
        if (value == UndefinedMarker) {
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
     * Cover method for returning an int for a
     * given system property with a default value.
     * @param s system property
     * @param defaultValue default value
     * @return int value of the system property or the default value
     */    
    public static int intForKeyWithDefault(final String s, final int defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);

        Object value = _cache.get(propertyName);
        if (value == UndefinedMarker) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return ((Integer)value).intValue();
        }
        
        String propertyValue = ERXSystem.getProperty(propertyName);
        final int intValue = ERXValueUtilities.intValueWithDefault(propertyValue, defaultValue);
        if (retainDefaultsEnabled() && propertyValue == null) {
            propertyValue = Integer.toString(intValue);
            System.setProperty(propertyName, propertyValue);
        }
        _cache.put(propertyName, propertyValue == null ? (Object)UndefinedMarker : ERXConstant.integerForInt(intValue));
        return intValue;
    }

    /**
     * Cover method for returning a long for a
     * given system property with a default value.
     * @param s system property
     * @param defaultValue default value
     * @return long value of the system property or the default value
     */    
    public static long longForKeyWithDefault(final String s, final long defaultValue) {
        final String propertyName = getApplicationSpecificPropertyName(s);
        
        Object value = _cache.get(propertyName);
        if (value == UndefinedMarker) {
            return defaultValue;
        }
        if (value instanceof Long) {
            return ((Long)value).longValue();
        }

        String propertyValue = ERXSystem.getProperty(propertyName);
        final long longValue = ERXValueUtilities.longValueWithDefault(propertyValue, defaultValue);
        if (retainDefaultsEnabled() && propertyValue == null) {
            propertyValue = Long.toString(longValue);
            System.setProperty(propertyName, propertyValue);
        }
        _cache.put(propertyName, propertyValue == null ? (Object)UndefinedMarker : new Long(longValue));
        return longValue;
    }
    
    /**
     * Returning an string for a given system 
     * property. This is a cover method of 
     * {@link java.lang.System#getProperty}
     * @param s system property
     * @return string value of the system propery or null
     */
    public static String stringForKey(String s) {
        return stringForKeyWithDefault(s, null);
    }

    /**
     * Returning an string for a given system
     * property. This is a cover method of
     * {@link java.lang.System#getProperty}
     * @param s system property
     * @return string value of the system propery or null
     */
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
     * Returns an array of strings separated with the given separator string.
     * 
     * @param key the key to lookup
     * @param separator the separator (",")
     * @return the array of strings or NSArray.EmptyArray if not found
     */
    public static NSArray componentsSeparatedByString(String key, String separator) {
    	return ERXProperties.componentsSeparatedByStringWithDefault(key, separator, NSArray.EmptyArray);
    }

    /**
     * Returns an array of strings separated with the given separator string.
     * 
     * @param key the key to lookup
     * @param separator the separator (",")
     * @param defaultValue the default array to return if there is no value
     * @return the array of strings
     */
    public static NSArray componentsSeparatedByStringWithDefault(String key, String separator, NSArray defaultValue) {
    	NSArray array;
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
     * Sets an array in the System properties for
     * a particular key.
     * @param array to be set in the System properties
     * @param key to be used to get the value
     */
    public static void setArrayForKey(NSArray array, String key) {
        setStringForKey(NSPropertyListSerialization.stringFromPropertyList(array), key);
    }

    /**
     * Sets a dictionary in the System properties for
     * a particular key.
     * @param dictionary to be set in the System properties
     * @param key to be used to get the value
     */    
    public static void setDictionaryForKey(NSDictionary dictionary, String key) {
        setStringForKey(NSPropertyListSerialization.stringFromPropertyList(dictionary), key);
    }

    /**
     * Sets a string in the System properties for
     * another string.
     * @param string to be set in the System properties
     * @param key to be used to get the value
     */
    // DELETEME: Really not needed anymore
    public static void setStringForKey(String string, String key) {
        System.setProperty(key, string);
        _cache.remove(key);
    }
    
    /** 
     * Copies all properties from source to dest. 
     * 
     * @param source  proeprties copied from 
     * @param dest  properties copied to
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
     * Reads a Java properties file at the given path 
     * and returns a {@link java.util.Properties Properties} object 
     * as the result. If the file does not exist, returns 
     * an empty properties object. 
     * 
     * @param path  file path to the properties file
     * @return properties object with the values from the file
     *      specified.
     */
    // FIXME: This shouldn't eat the exception
    public static Properties propertiesFromPath(String path) {
        Properties prop = new Properties();

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
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));
            prop.load(in);
            in.close();
            log.debug("Loaded configuration file at path: "+ path);
        } catch (IOException e) {
            log.error("Unable to initialize properties from file \"" + path + "\"", e);
        }
        return prop;
    }

    /**
     * Gets the properties for a given file.
     * @param file the properties file
     * @return properties from the given file
     */
    public static Properties propertiesFromFile(File file) throws IOException {
        if (file == null)
            throw new IllegalStateException("Attempting to get properties for a null file!");
        Properties prop = new Properties();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        prop.load(in);
        in.close();
        return prop;
    }
    
    /**
     * Sets and returns properties object with the values from 
     * the given command line arguments string array. 
     * 
     * @param argv  string array typically provided by 
     *               the command line arguments
     * @return properties object with the values from 
     *          the argv
     */
    public static Properties propertiesFromArgv(String[] argv) {
        Properties properties = new Properties();
        NSDictionary argvDict = NSProperties.valuesFromArgv(argv);
        Enumeration e = argvDict.allKeys().objectEnumerator();
        while (e.hasMoreElements()) {
            Object key = e.nextElement();
            properties.put(key, argvDict.objectForKey(key));
        }
        return properties;
    }

    /** 
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
     */
    public static NSArray pathsForUserAndBundleProperties() {
        return pathsForUserAndBundleProperties(false);
    }

    public static NSArray pathsForUserAndBundleProperties(boolean reportLoggingEnabled) {
        NSMutableArray propertiesPaths = new NSMutableArray();
        NSMutableArray projectsInfo = new NSMutableArray();
        String projectPath, aPropertiesPath;
        WOApplication application = WOApplication.application();
        if (application == null) {
            log.warn("The application is not yet initialized. Returning an empty array.");
            return NSArray.EmptyArray;
        }
                
        /* *** Properties for frameworks *** */
        NSArray frameworkNames = (NSArray)NSBundle.frameworkBundles().valueForKey("name");
        Enumeration e = frameworkNames.objectEnumerator();
        while (e.hasMoreElements()) {
            String frameworkName = (String) e.nextElement();
            projectPath = aPropertiesPath = null;
            
            // Check if the framework project is opened from PBX
            WOProjectBundle bundle = WOProjectBundle.projectBundleForProject(frameworkName, true);
            if (bundle != null) 
                projectPath = bundle.projectPath();
            else 
                projectPath = ERXSystem.getProperty("projects." + frameworkName);
            
            if (projectPath != null) {
                aPropertiesPath = pathForPropertiesUnderProjectPath(projectPath);
                if (aPropertiesPath != null) {
                    projectsInfo.addObject("Framework:   " + frameworkName 
                            + " (opened, development-mode) " + aPropertiesPath);
                }
            }
            
            if (aPropertiesPath == null) {
                // The framework project is not opened from PBX, use the one in the bundle. 
                URL url =  ERXFileUtilities.pathURLForResourceNamed("Properties", frameworkName, null);
                if(url != null) {
                    aPropertiesPath = url.getFile();
                    if (aPropertiesPath != null) {
                        aPropertiesPath = getActualPath(aPropertiesPath);
                        projectsInfo.addObject("Framework:   " + frameworkName 
                                + " (not opened, installed) " + aPropertiesPath); 
                    }
                }
            }
            
            if (aPropertiesPath != null) 
                    propertiesPaths.addObject(aPropertiesPath);
        } 
        
        /* *** Properties for the application (mainBundle) *** */
        
        // Check if the application project is opened from PBXs
        projectPath = aPropertiesPath = null;
        String mainBundleName = NSBundle.mainBundle().name();
        // horrendous hack to avoid having to set the NSProjectPath manually.
            WOProjectBundle mainBundle = WOProjectBundle.projectBundleForProject(mainBundleName, false);
            if (mainBundle == null) {
                projectPath = ERXSystem.getProperty("projects." + mainBundleName);
                if (projectPath == null)
                    projectPath = "../..";
            } else {
                projectPath = mainBundle.projectPath();
            }
            
        if (projectPath != null) {
            aPropertiesPath = pathForPropertiesUnderProjectPath(projectPath);
            if (aPropertiesPath != null) {
                projectsInfo.addObject("Application: " + mainBundleName 
                            + " (opened, development-mode) " + aPropertiesPath); 
            }
        }

        if (aPropertiesPath == null) {
            // The application project is not opened from PBX, use the one in the bundle. 
            aPropertiesPath = ERXFileUtilities.pathForResourceNamed("Properties", "app", null);
            if (aPropertiesPath != null) {
               aPropertiesPath = getActualPath(aPropertiesPath);
               projectsInfo.addObject("Application: " + mainBundleName 
                            + " (not opened, installed) " + aPropertiesPath);  
            }
        }

        if (aPropertiesPath != null) 
            propertiesPaths.addObject(aPropertiesPath);


        /* *** WebObjects.properties in the user home directory *** */
        String userHome = ERXSystem.getProperty("user.home");
        if (userHome != null  &&  userHome.length() > 0) { 
            File file = new File(userHome, "WebObjects.properties");
            if (file.exists()  &&  file.isFile()  &&  file.canRead()) {
                try {
                    aPropertiesPath = file.getCanonicalPath();
                    projectsInfo.addObject("User:        WebObjects.properties " + aPropertiesPath);  
                    propertiesPaths.addObject(aPropertiesPath);
                } catch (java.io.IOException ex) {
                    // do nothing
                }
            }
        }

        /* **** Optional properties files **** */
        if (optionalConfigurationFiles() != null &&
            optionalConfigurationFiles().count() > 0) {
            for (Enumeration configEnumerator = optionalConfigurationFiles().objectEnumerator();
                 configEnumerator.hasMoreElements();) {
                String configFile = (String)configEnumerator.nextElement();
                File file = new File(configFile);
                if (file.exists()  &&  file.isFile()  &&  file.canRead()) {
                    try {
                        aPropertiesPath = file.getCanonicalPath();
                        projectsInfo.addObject("Optional Configuration:    " + aPropertiesPath);
                        propertiesPaths.addObject(aPropertiesPath);
                    } catch (java.io.IOException ex) {
                        // do nothing
                    }                    
                }
            }
        }

        /** Properties.<userName> -- per-Application-per-User properties */
        String applicationUserPropertiesPath = ERXProperties.applicationUserProperties();
        if (applicationUserPropertiesPath != null) {
           projectsInfo.addObject("Application " + mainBundleName + "/Application-User Properties: " + aPropertiesPath);
           propertiesPaths.addObject(applicationUserPropertiesPath);
        }
        
        /* *** Report the result *** */ 
        if (reportLoggingEnabled  &&  projectsInfo.count() > 0) {
            StringBuffer message = new StringBuffer();
            message.append("\n\n")
                    .append("ERXProperties has found the following Properties files: \n");
            message.append(projectsInfo.componentsJoinedByString("\n"));
            message.append("\n");
            message.append(NSPropertyListSerialization.stringFromPropertyList(allProperties()));
            log.info(message.toString());
        }

        return propertiesPaths.immutableClone();
    }

    public static class Property {
    	public String key, value;
    	public Property(String key, String value) {
    		this.key = key;
    		this.value = value;
    	}
    	public String toString() {
    		return key + " = " + value;
    	}
    }

    private static NSArray allProperties() {
    	NSMutableArray props = new NSMutableArray();
    	for (Enumeration e = ERXSystem.getProperties().keys(); e.hasMoreElements();) {
    		String key = (String) e.nextElement();
    		String object = "" + ERXSystem.getProperty(key);
    		props.addObject(new Property(key, object));
    	}
    	return (NSArray) props.valueForKey("@sortAsc.key");
     }

    /** 
     * Returns the full path to the Properties file under the 
     * given project path. At the current implementation, 
     * it looks for /Properties and /Resources/Properties. 
     * If the Properties file doesn't exist, returns null.  
     * 
     * @param projectPath  string to the project root directory
     * @return  the path to the Properties file if it exists
     */
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
     * Returns the application-specific user properties.
     */
    public static String applicationUserProperties() {
    	String applicationUserPropertiesPath = null;
        String userName = ERXSystem.getProperty("user.name");
        if (userName != null  &&  userName.length() > 0) { 
        	String resourceApplicationUserPropertiesPath = ERXFileUtilities.pathForResourceNamed("Properties." + userName, "app", null);
            if (resourceApplicationUserPropertiesPath != null) {
            	applicationUserPropertiesPath = getActualPath(resourceApplicationUserPropertiesPath);
            }
        }
        return applicationUserPropertiesPath;
    }

    /**
     * Gets an array of optionally defined configuration
     * files. 
     * @return array of configuration file names (absolute paths)
     */
    public static NSArray optionalConfigurationFiles() {
        return arrayForKey("er.extensions.ERXProperties.OptionalConfigurationFiles");
    }
    
    /**
     * Returns actual full path to the given file system path  
     * that could contain symbolic links. For example: 
     * /Resources will be converted to /Versions/A/Resources
     * when /Resources is a symbolic link.
     * 
     * @param path  path string to a resource that could 
     *               contain symbolic links
     * @return actual path to the resource
     */
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
    
    public static void systemPropertiesChanged() {
        synchronized (AppSpecificPropertyNames) {
            AppSpecificPropertyNames.clear();
        }
        _cache.clear();
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
     * Caches the application name for appending to the key.
     * Note that for a period when the application is starting up
     * application() will be null and name() will be null.
     * @return application name used for appending, for example ".ERMailer"
     * Note: this is redundant with the scheme checked in on March 21, 2005 by clloyd (ben holt did checkin).
     * This scheme requires the user to swizzle the existing properties file with a new one of this type.
     */
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
     * Overriding the default getProperty method to first check:
     * key.&lt;ApplicationName> before checking for key. If nothing
     * is found then key.Default is checked.
     * @param key to check
     * @return property value
     */
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
     * Returns the properties as a String in Property file format. Useful when you use them 
     * as custom value types, you would set this as the conversion method name.
     * @return
     * @throws IOException
     */
    public Object toExternalForm() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        store(os, null);
        return new String(os.toByteArray());
    }
    
    /**
     * Load the properties from a String in Property file format. Useful when you use them 
     * as custom value types, you would set this as the factory method name.
     * @param string
     * @return
     * @throws IOException
     */
    public static ERXProperties fromExternalForm(String string) throws IOException {
        ERXProperties result = new ERXProperties();
        result.load(new ByteArrayInputStream(string.getBytes()));
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
     * @param anObject
     * @param aKey
     */
    public Object valueForKey(String aKey) {
         return getProperty(aKey);
    }
}
