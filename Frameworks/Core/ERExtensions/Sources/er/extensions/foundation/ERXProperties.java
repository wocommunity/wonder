/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.foundation;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSProperties;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation._NSFileUtilities;
import com.webobjects.foundation.properties.NSPropertiesCoordinator;

import er.extensions.crypting.ERXCrypto;

/**
 * Collection of simple utility methods used to get and set properties
 * in the system properties. The only reason this class is needed is
 * because all of the methods in NSProperties have been deprecated.
 * This is a wee bit annoying. The usual method is to have a method
 * like <code>getBoolean</code> off of Boolean which would resolve
 * the System property as a Boolean object.
 */
public class ERXProperties extends Properties implements NSKeyValueCoding {

    /** logging support */
    public final static Logger log = Logger.getLogger(ERXProperties.class);

    /** WebObjects version number as string */
    private static String _webObjectsVersion;
    
    /** WebObjects version number as double */ 
    private static double _webObjectsVersionDouble;

    /**
     * Puts handy properties such as <code>com.webobjects.version</code> 
     * into the system properties. This method is called when 
     * the framework is initialized  
     * (when WOApplication.ApplicationWillFinishLaunchingNotification 
     * is posted.)
     */
    public static void populateSystemProperties() {
        NSProperties.setStringForKey(webObjectsVersion(), "com.webobjects.version");
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
    @Deprecated
    public static NSArray arrayForKey(String s) {
        return NSProperties.arrayForKey(s);
    }

    /**
     * Cover method for returning an NSArray for a
     * given system property and set a default value if not given.
     * @param s system property
     * @param defaultValue default value
     * @return array de-serialized from the string in
     *      the system properties or default value
     */
    @Deprecated
    public static NSArray arrayForKeyWithDefault(final String s, final NSArray defaultValue) {
        return NSProperties.arrayForKeyWithDefault(s, defaultValue);
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
    @Deprecated
    public static boolean booleanForKey(String s) {
        return NSProperties.booleanForKey(s);
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
    @Deprecated
    public static boolean booleanForKeyWithDefault(final String s, final boolean defaultValue) {
        return NSProperties.booleanForKeyWithDefault(s, defaultValue);
    }
    
    /**
     * Cover method for returning an NSDictionary for a
     * given system property.
     * @param s system property
     * @return dictionary de-serialized from the string in
     *      the system properties
     */    
    @Deprecated
    public static NSDictionary dictionaryForKey(String s) {
        return NSProperties.dictionaryForKey(s);
    }

    /**
     * Cover method for returning an NSDictionary for a
     * given system property or the default value.
     * @param s system property
     * @param defaultValue default value
     * @return dictionary de-serialized from the string in
     *      the system properties
     */
    @Deprecated
    public static NSDictionary dictionaryForKeyWithDefault(final String s, final NSDictionary defaultValue) {
        return NSProperties.dictionaryForKeyWithDefault(s, defaultValue);
    }

    /**
     * Cover method for returning an int for a
     * given system property.
     * @param s system property
     * @return int value of the system property or 0
     */
    @Deprecated
    public static int intForKey(String s) {
        return NSProperties.intForKey(s);
    }

    /**
     * Cover method for returning a long for a
     * given system property.
     * @param s system property
     * @return long value of the system property or 0
     */
    @Deprecated
    public static long longForKey(String s) {
        return NSProperties.longForKey(s);
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
    @Deprecated
    public static BigDecimal bigDecimalForKey(String s) {
        return NSProperties.bigDecimalForKey(s);
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
    @Deprecated
    public static BigDecimal bigDecimalForKeyWithDefault(String s, BigDecimal defaultValue) {
        return NSProperties.bigDecimalForKeyWithDefault(s, defaultValue);
    }

    /**
     * Cover method for returning an int for a
     * given system property with a default value.
     * @param s system property
     * @param defaultValue default value
     * @return int value of the system property or the default value
     */    
    @Deprecated
    public static int intForKeyWithDefault(final String s, final int defaultValue) {
        return NSProperties.intForKeyWithDefault(s, defaultValue);
    }

    /**
     * Cover method for returning a long for a
     * given system property with a default value.
     * @param s system property
     * @param defaultValue default value
     * @return long value of the system property or the default value
     */    
    @Deprecated
    public static long longForKeyWithDefault(final String s, final long defaultValue) {
        return NSProperties.longForKeyWithDefault(s, defaultValue);
    }
    
    /**
     * Returning an string for a given system 
     * property. This is a cover method of 
     * {@link java.lang.System#getProperty}
     * @param s system property
     * @return string value of the system propery or null
     */
    @Deprecated
    public static String stringForKey(String s) {
        return NSProperties.stringForKey(s);
    }

    /**
     * Returning an string for a given system
     * property. This is a cover method of
     * {@link java.lang.System#getProperty}
     * @param s system property
     * @return string value of the system propery or null
     */
    @Deprecated
    public static String stringForKeyWithDefault(final String s, final String defaultValue) {
        return NSProperties.stringForKeyWithDefault(s, defaultValue);
    }

    /**
     * Returns the decrypted value for the given property name using
     * the default crypter if the property propertyName.encrypted=true.  For
     * instance, if you are requesting my.password, if my.password.encrypted=true
     * the value of my.password will be passed to the default crypter's decrypt
     * method.
     * 
     * @param propertyName the property name to retrieve and optionally decrypt
     * @return the decrypted property value
     */
    @Deprecated
    public static String decryptedStringForKey(String propertyName) {
        // Arroz: value is decrypted at properties load time
        return NSProperties.stringForKey(propertyName);
    }
    
    /**
     * Returns the decrypted value for the given property name using
     * the default crypter if the property propertyName.encrypted=true.  For
     * instance, if you are requesting my.password, if my.password.encrypted=true
     * the value of my.password will be passed to the default crypter's decrypt
     * method.
     * 
     * @param propertyName the property name to retrieve and optionally decrypt
     * @param defaultValue the default value to return if there is no password
     * @return the decrypted property value
     */
    @Deprecated
    public static String decryptedStringForKeyWithDefault(String propertyName, String defaultValue) {
        // Arroz: value is decrypted at properties load time
		return NSProperties.stringForKeyWithDefault(propertyName, defaultValue);
    }

    /**
     * Returns the decrypted value for the given property name using the default crypter. This is
     * slightly different than decryptedStringWithKeyWithDefault in that it does not require  the .encrypted
     * property to be set.
     *  
     * @param propertyName the name of the property to decrypt
     * @param defaultValue the default encrypted value
     * @return the decrypted value
     */
    public static String decryptedStringForKeyWithEncryptedDefault(String propertyName, String defaultValue) {
        // Arroz: value is decrypted at properties load time
        return NSProperties.stringForKeyWithDefault(propertyName, ERXCrypto.defaultCrypter().decrypt(defaultValue));
    }

    /**
     * Returns an array of strings separated with the given separator string.
     * 
     * @param key the key to lookup
     * @param separator the separator (",")
     * @return the array of strings or NSArray.EmptyArray if not found
     */
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
	public static NSArray componentsSeparatedByStringWithDefault(String key, String separator, NSArray defaultValue) {
    	NSArray array;
    	String str = NSProperties.stringForKey(key);
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
    @Deprecated
    public static void setArrayForKey(NSArray array, String key) {
        NSProperties.setArrayForKey(array, key);
    }

    /**
     * Sets a dictionary in the System properties for
     * a particular key.
     * @param dictionary to be set in the System properties
     * @param key to be used to get the value
     */    
    @Deprecated
    public static void setDictionaryForKey(NSDictionary dictionary, String key) {
        NSProperties.setDictionaryForKey(dictionary, key);
    }
    
    public static NSSet setForKeyWithDefault(String key, NSSet defaultValue) {
        return NSProperties.setForKeyWithDefault(key, defaultValue);
    }
    
    public static NSSet setForKey(String aKey) {
        return NSProperties.setForKey(aKey);
    }

    /**
     * Sets a string in the System properties for
     * another string.
     * @param string to be set in the System properties
     * @param key to be used to get the value
     */
    // DELETEME: Really not needed anymore
    @Deprecated
    public static void setStringForKey(String string, String key) {
        NSProperties.setStringForKey(string, key);
    }
    
    /**
     * Reads a Java properties file at the given path 
     * and returns a {@link java.util.Properties Properties} object 
     * as the result. If the file does not exist, returns 
     * an empty properties object. 
     * 
     * @param path  file path to the properties file
	 * @param requireSymlink whether or not to require a symlink (and block) before loading
     * @return properties object with the values from the file
     *      specified.
     */
    public static Properties propertiesFromPath(String path, boolean requireSymlink) {
    	ERXProperties.NestedProperties prop = new ERXProperties.NestedProperties();

        if (path == null  ||  path.length() == 0) {
            log.warn("Attempting to read property file for null file path");
            return prop;
        }

        File file = new File(path);
        try {
        	prop.load(file, requireSymlink);
            log.debug("Loaded configuration file at path: "+ path);
        } catch (IOException e) {
        	if (ERXProperties.shouldValidateProperties()) {
        		throw new RuntimeException("File '" + path + "' could not be read.", e);
        	}
            log.error("Unable to initialize properties from file \"" + path + "\"", e);
        }
        return prop;
    }

    /**
     * KVC implementation.
     * @param anObject
     * @param aKey
     */
    @Deprecated
    public void takeValueForKey(Object anObject, String aKey) {
        NSPropertiesCoordinator.sharedInstance().takeValueForKey(anObject, aKey);
    }

    /**
     * KVC implementation.
     *
     * @param aKey
     */
    @Deprecated
    public Object valueForKey(String aKey) {
        return NSPropertiesCoordinator.sharedInstance().valueForKey(aKey);
    }
	
	/**
	 * NestedProperties is a subclass of Properties that provides support for including other
	 * Properties files on the fly.  If you create a property named .includeProps, the value
	 * will be interpreted as a file to load.  If the path is absolute, it will just load it
	 * directly.  If it's relative, the path will be loaded relative to the current user's
	 * home directory.  Multiple .includeProps can be included in a Properties file and they
	 * will be loaded in the order they appear within the file.
	 *  
	 * @author mschrag
	 */
	// @WOHack(why="This is a copy of NSProperties.NestedProperties, but Wonder doesn't build against the custom WO 5.2.3.")
	public static class NestedProperties extends Properties {
		public static final String IncludePropsSoFarKey = ".includePropsSoFar";
		public static final String IncludePropsKey = ".includeProps";
		
		private Stack<File> _files = new Stack<File>();
		
		public NestedProperties() {
		}

		public NestedProperties(Properties defaults) {
			super(defaults);
		}
		
		@Override
		public synchronized Object put(Object key, Object value) {
			if (NestedProperties.IncludePropsKey.equals(key)) {
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
                // MS: Canonicalize the includeProps path so that we get more reliable duplicate checks. Otherwise, depending on 
                // exactly how the include path was constructed (with .., absolute path, etc) you might get the same props
                // file included twice but with two different names. It would likely get caught and explode later on, anyway,
                // but failing fast is always desirable.
                String propsPath;
                try {
                	propsPath = propsFile.getCanonicalPath();
                }
                catch (IOException e) {
					throw new RuntimeException("Failed to canonicalize the property file '" + propsFile + "'.", e);
                }

                // Detect mutually recursing props files by tracking what we've already loaded:
                String existingIncludeProps = this.getProperty(NestedProperties.IncludePropsSoFarKey);
                if (existingIncludeProps == null) {
                	existingIncludeProps = "";
                }
                if (existingIncludeProps.indexOf(propsPath) > -1) {
                	log.info("ERXProperties.NestedProperties.load(): Possible recursive includeProps detected. '" + propsPath + "' was included in more than one of the following files: " + existingIncludeProps);
                	log.info("ERXProperties.NestedProperties.load() cannot proceed - QUITTING!");
                    System.exit(1);
                }
                if (existingIncludeProps.length() > 0) {
                	existingIncludeProps += ", ";
                }
                existingIncludeProps += propsPath;
                super.put(NestedProperties.IncludePropsSoFarKey, existingIncludeProps);

                try {
                    log.info("ERXProperties.NestedProperties.load(): Including props file: " + propsFile);
					this.load(propsFile, ERXProperties._shouldRequireSymlinkedGlobalAndIncludeProperties());
				} catch (IOException e) {
					throw new RuntimeException("Failed to load the property file '" + value + "'.", e);
				}
				return null;
			}
			else {
				return super.put(key, value);
			}
		}

		/**
		 * Loads the properties from the given file, optionally blocking until the symlink is resolved.
		 * 
		 * @param propsFile the properties file to load from
		 * @param requireSymlink whether or not to require a symlink (and block) before loading
		 * @throws IOException if properties loading fails
		 */
		public synchronized void load(File propsFile, boolean requireSymlink) throws IOException {
			// MS: We pull the canonical path here in case you symlink to a Properties file
			// that has relative .includeProps paths in it (that you expect to load from the same
			// folder as the symlink target). It's debatable whether this is actually the desired 
			// behavior all the time, but until I hear someone who needs the old way, I'm not
			// going to increase the complexity by adding a config flag that has to propagate all over.
			File canonicalPropsFile;
			if (requireSymlink) {
            	canonicalPropsFile = _NSFileUtilities.resolveLink(propsFile.getPath(), propsFile.getName());
			}
			else {
				canonicalPropsFile = propsFile.getCanonicalFile();
			}

	        if (!canonicalPropsFile.exists() || !canonicalPropsFile.isFile() || !canonicalPropsFile.canRead()) {
	            if (ERXProperties.shouldValidateProperties()) {
	                throw new RuntimeException("File " + propsFile.getPath() + " (resolved to " + canonicalPropsFile.getPath() + ") doesn't exist or can't be read.");
	            }
	            log.warn("File " + propsFile.getPath() + " doesn't exist or can't be read.");
	            return;
	        }

			_files.push(canonicalPropsFile.getParentFile());
			try {
	            BufferedInputStream is = new BufferedInputStream(new FileInputStream(canonicalPropsFile));
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

	// MS: NSValidateProperties specifies whether we should more strictly validate properties files and kill startup if there is a failure. Setting
	// this property to false will switch everything back to the previous behavior.
	private static boolean shouldValidateProperties() {
		return Boolean.valueOf(System.getProperty("NSValidateProperties", "true"));
	}

	// MS: NSRequireSymlinkedGlobalAndIncludeProperties specifies whether we should require root and included properties files to be symlinked. This does not include
	// the root Properties files loaded from frameworks.
	private static boolean _shouldRequireSymlinkedGlobalAndIncludeProperties() {
		return Boolean.valueOf(System.getProperty("NSRequireSymlinkedGlobalAndIncludeProperties", "false"));
	}
	
	public static void registerForInstanceOperator() {
	    NSPropertiesCoordinator.registerProcessorClass(ERXInstanceRangeProcessor.class);
	}
	
	@Deprecated
	public static void systemPropertiesChanged() {
	}
}
