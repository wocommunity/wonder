/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOApplication;
import java.util.*;
import java.math.*;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import com.webobjects.appserver._private.WOProjectBundle;
import com.webobjects.foundation.*;

/**
 * Collection of simple utility methods used to get and set properties
 * in the system properties. The only reason this class is needed is
 * because all of the methods in NSProperties have been deprecated.
 * This is a wee bit annoying. The usual method is to have a method
 * like <code>getBoolean</code> off of Boolean which would resolve
 * the System property as a Boolean object.
 */
public class ERXProperties {
    private static Boolean RetainDefaultsEnabled;
    private static String UndefinedMarker = "-undefined-";
    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERXProperties.class);
    private static final Map AppSpecificPropertyNames = new HashMap(128);

    /** WebObjects version number as string */
    private static String _webObjectsVersion;
    
    /** WebObjects version number as double */ 
    private static double _webObjectsVersionDouble;

    /** Internal cache of type converted values to avoid reconverting attributes that are asked for frequently */
    private static Map _cache = Collections.synchronizedMap(new HashMap());

    private static boolean retainDefaultsEnabled() {
        if (RetainDefaultsEnabled == null) {
            final String propertyValue = System.getProperty("er.extensions.ERXProperties.RetainDefaultsEnabled", "false");
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
     * in the <code>info.plist</code> resource and returns
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
        NSDictionary versionDictionary = (NSDictionary)ERXFileUtilities.readPropertyListFromFileInFramework("version.plist", "JavaWebObjects", null);

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
        return webObjectsVersionAsDouble() >= 5.2d;
    }

    /**
     * Quick convience method used to determine if the current
     * webobjects version is 5.22 or higher.
     * @return if the version of webobjects is 5.22 or better
     */
    public static boolean webObjectsVersionIs522OrHigher() {
        String webObjectsVersion = webObjectsVersion();
        if(log.isDebugEnabled()){
            log.debug("webObjectsVersion:"+webObjectsVersion());
        }
        if("5.2".equals(webObjectsVersion)) {
            String sourceVersion = sourceVersionString();
            if(log.isDebugEnabled()){
                log.debug("sourceVersionString:"+sourceVersionString());
            }
            if("9260000".equals(sourceVersion)) {
                return true;
            }
        }
        return webObjectsVersionAsDouble() >= 5.22d;
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
                final String propertyValue = System.getProperty(appSpecificPropertyName);
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
        
        final String propertyValue = System.getProperty(propertyName);
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
        
        String propertyValue = System.getProperty(propertyName);
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
        
        final String propertyValue = System.getProperty(propertyName);
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
        
        String propertyValue = System.getProperty(propertyName);
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
        
        String propertyValue = System.getProperty(propertyName);
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

        String propertyValue = System.getProperty(propertyName);
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
        final String propertyValue = System.getProperty(propertyName);
        final String stringValue = propertyValue == null ? defaultValue : propertyValue;
        if (retainDefaultsEnabled() && propertyValue == null) {
            System.setProperty(propertyName, stringValue == null ? UndefinedMarker : stringValue);
        }
        return stringValue == UndefinedMarker ? null : stringValue;
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
                projectPath = System.getProperty("projects." + frameworkName);
            
            if (projectPath != null) {
                aPropertiesPath = pathForPropertiesUnderProjectPath(projectPath);
                if (aPropertiesPath != null) {
                    projectsInfo.addObject("Framework:   " + frameworkName 
                            + " (opened, development-mode) " + aPropertiesPath);
                }
            }
            
            if (aPropertiesPath == null) {
                // The framework project is not opened from PBX, use the one in the bundle. 
                aPropertiesPath = ERXFileUtilities.pathForResourceNamed("Properties", frameworkName, null);
                if (aPropertiesPath != null) {
                    aPropertiesPath = getActualPath(aPropertiesPath);
                    projectsInfo.addObject("Framework:   " + frameworkName 
                            + " (not opened, installed) " + aPropertiesPath); 
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
                projectPath = System.getProperty("projects." + mainBundleName);
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
        String userHome = System.getProperty("user.home");
        if (userHome != null  &&  userHome.length() > 0) { 
            File file = new File(userHome, "WebObjects.properties");
            if (file.exists()  &&  file.isFile()  &&  file.canRead()) {
                try {
                    aPropertiesPath = file.getCanonicalPath();
                    projectsInfo.addObject("User:        WebObjects.properties " + aPropertiesPath);  
                    propertiesPaths.addObject(aPropertiesPath);
                } catch (java.io.IOException ex) {
                    ;
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
                        ;
                    }                    
                }
            }
        }
        
        /* *** Report the result *** */ 
        if (reportLoggingEnabled  &&  projectsInfo.count() > 0) {
            StringBuffer message = new StringBuffer();
            message.append("\n\n")
                    .append("ERXProperties has found the following Properties files: \n");
            message.append(projectsInfo.componentsJoinedByString("\n"));
            message.append("\n");
            log.info(message.toString());
        }

        return propertiesPaths.immutableClone();
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

    /**
     * Stores the mapping between operator keys and operators
     */
    private static final NSMutableDictionary operators = new NSMutableDictionary();

    /**
     * Registers a property operator for a particular key.
     * 
     * @param operator
     *            the operator to register
     * @param key
     *            the key name of the operator
     */
    public static void setOperatorForKey(ERXProperties.Operator operator, String key) {
      ERXProperties.operators.setObjectForKey(operator, key);
    }

    /**
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
     * 
     * @author mschrag
     */
    public static interface Operator {
      /**
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
       * @return a dictionary of properties (or null to use the original key
       *         and value)
       */
      public NSDictionary compute(String key, String value, String parameters);
    }

    /**
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
     * 
     * @author mschrag
     */
    public static class InRangeOperator implements ERXProperties.Operator {
      /**
       * The default key name of the ForInstance variant of the InRange operator.
       */
      public static final String ForInstanceKey = "forInstance";

      private int _instanceNumber;

      /**
       * Constructs a new InRangeOperator.
       * 
       * @param value
       *            the instance number of this application
       */
      public InRangeOperator(int value) {
        _instanceNumber = value;
      }

      public NSDictionary compute(String key, String value, String parameters) {
        NSDictionary computedProperties = null;
        if (parameters != null && parameters.length() > 0) {
          boolean instanceNumberMatches = false;
          String[] ranges = parameters.split(",");
          for (int rangeNum = 0; rangeNum < ranges.length; rangeNum ++) {
            String range = ranges[rangeNum];
            range = range.trim();
            int dashIndex = range.indexOf('-');
            if (dashIndex == -1) {
              int singleValue = Integer.parseInt(range);
              if (_instanceNumber == singleValue) {
                instanceNumberMatches = true;
                break;
              }
            }
            else {
              int lowValue = Integer.parseInt(range.substring(0, dashIndex).trim());
              int highValue = Integer.parseInt(range.substring(dashIndex + 1).trim());
              if (_instanceNumber >= lowValue && _instanceNumber <= highValue) {
                instanceNumberMatches = true;
                break;
              }
            }
          }
          if (instanceNumberMatches) {
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
     * For each property in originalProperties, process the keys and avlues with
     * the registered property operators and stores the converted value into
     * destinationProperties.
     * 
     * @param originalProperties
     *            the properties to convert
     * @param destinationProperties
     *            the properties to copy into
     */
    public static void evaluatePropertyOperators(Properties originalProperties, Properties destinationProperties) {
      NSArray operatorKeys = ERXProperties.operators.allKeys();
      Set originalKeys = new TreeSet(originalProperties.keySet());
      Iterator originalKeysIter = originalKeys.iterator();
      while (originalKeysIter.hasNext()) {
        String key = (String) originalKeysIter.next();
        if (key != null && key.length() > 0) {
          String value = originalProperties.getProperty(key);
          if (operatorKeys.count() > 0 && key.indexOf(".@") != -1) {
            ERXProperties.Operator operator = null;
            NSDictionary computedProperties = null;
            Enumeration operatorKeyEnum = operatorKeys.objectEnumerator();
            while (operatorKeyEnum.hasMoreElements()) {
              String operatorKey = (String)operatorKeyEnum.nextElement();
              String operatorKeyWithAt = ".@" + operatorKey;
              if (key.endsWith(operatorKeyWithAt)) {
                operator = (ERXProperties.Operator) ERXProperties.operators.objectForKey(operatorKey);
                computedProperties = operator.compute(key.substring(0, key.length() - operatorKeyWithAt.length()), value, null);
                break;
              }
              else {
                int keyIndex = key.indexOf(operatorKeyWithAt + ".");
                if (keyIndex != -1) {
                  operator = (ERXProperties.Operator) ERXProperties.operators.objectForKey(operatorKey);
                  computedProperties = operator.compute(key.substring(0, keyIndex), value, key.substring(keyIndex + operatorKeyWithAt.length() + 1));
                  break;
                }
              }
            }

            if (computedProperties == null) {
              destinationProperties.put(key, value);
            }
            else {
              originalProperties.remove(key);
              Enumeration computedPropertyKeysEnum = computedProperties.keyEnumerator();
              while (computedPropertyKeysEnum.hasMoreElements()) {
                Object computedKey = computedPropertyKeysEnum.nextElement();
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

}
