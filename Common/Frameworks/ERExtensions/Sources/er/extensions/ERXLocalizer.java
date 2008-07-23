//
// ERXLocalizer.java
// Project armehaut
//
// Created by ak on Sun Apr 14 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

import java.text.Format;
import java.util.*;
import java.lang.reflect.Constructor;

/** KVC access to localization.
Monitors a set of files in all frameworks and returns a string given a key for a language.
In the current state, it's more a stub for things to come.

These types of keys are acceptable in the monitored files:

    "this is a test" = "some test";
    "unittest.key.path.as.string" = "some test";
    "unittest" = {"key" = { "path" = { "as" = {"dict"="some test";};};};};

Note that if you only call for "unittest", you'll get a dictionary. So you can localize more complex objects than strings.

If you set the base class of your session to ERXSession, you can then use this code in your components:

   valueForKeyPath("session.localizer.this is a test")
   valueForKeyPath("session.localizer.unittest.key.path.as.string")
   valueForKeyPath("session.localizer.unittest.key.path.as.dict")

For sessionless Apps, you must use another method to get at the requested language and then call the localizer via

  ERXLocalizer l = ERXLocalizer.localizerForLanguages(languagesThisUserCanHandle) or
  ERXLocalizer l = ERXLocalizer.localizerForLanguage("German")

These defaults can be set (listed with their current defaults):

er.extensions.ERXLocalizer.defaultLanguage=English
er.extensions.ERXLocalizer.fileNamesToWatch=("Localizable.strings","ValidationTemplate.strings")
er.extensions.ERXLocalizer.availableLanguages=(English,German)
er.extensions.ERXLocalizer.frameworkSearchPath=(app,ERDirectToWeb,ERExtensions)

TODO: chaining of Localizers
*/

public class ERXLocalizer implements NSKeyValueCoding, NSKeyValueCodingAdditions  {
    
	protected static final ERXLogger log = ERXLogger.getERXLogger(ERXLocalizer.class);
    
    protected static final ERXLogger createdKeysLog = (ERXLogger)ERXLogger.getLogger(ERXLocalizer.class.getName() + ".createdKeys");

    private static boolean isLocalizationEnabled = true;
    
    private static boolean isInitialized = false;
    
    public static final String LocalizationDidResetNotification = "LocalizationDidReset";
    
    private static Observer observer;
    
    private static NSMutableArray monitoredFiles;
    
    static NSArray fileNamesToWatch;
    static NSArray frameworkSearchPath;
    static NSArray availableLanguages;
    static String defaultLanguage;
    
    static NSMutableDictionary localizers = new NSMutableDictionary();
    
    public static class Observer {
        public void fileDidChange(NSNotification n) {
            ERXLocalizer.resetCache();
            NSNotificationCenter.defaultCenter().postNotification(LocalizationDidResetNotification, null);
        }
    }

    public static void initialize() {
        if (!isInitialized) {
            observer = new Observer();
            monitoredFiles = new NSMutableArray();
            isLocalizationEnabled = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXLocalizer.isLocalizationEnabled", true);
            isInitialized = true;
        }
    }
    
    public static boolean isLocalizationEnabled() { 
    	return isLocalizationEnabled; 
    }
    public static void setIsLocalizationEnabled(boolean value) { 
    	isLocalizationEnabled = value; 
    }

    // CHECKME: Don't think that we need these any more now that we can have access to
    //	        the current localizer via thread storage
    public static NSDictionary fakeSessionForLanguage(String language) {
        ERXLocalizer localizer = localizerForLanguage(language);
        return new NSDictionary(new Object[] {localizer,language}, new Object[] {"localizer", "language"} );
    }

    // CHECKME: Don't think that we need these any more now that we can have access to
    //	        the current localizer via thread storage
    public static NSDictionary fakeSessionForSession(Object session) {
        ERXLocalizer localizer = localizerForSession(session);
        return new NSDictionary(new Object[] {localizer,localizer.language()}, new Object[] {"localizer", "language"} );
    }

    /**
     * Returns the current localizer for the current thread.
     * Note that the localizer for a given session is pushed
     * onto the thread when a session awakes and is nulled out
     * when a session sleeps.
     * @return the current localizer that has been pushed into
     * 		thread storage.
     */
    public static ERXLocalizer currentLocalizer() {
        ERXLocalizer current = (ERXLocalizer)ERXThreadStorage.valueForKey("localizer");
        if(current == null) {
        	if(!isInitialized) {
            	initialize();
            }
        	current = defaultLocalizer();
        }
        return current;
    }

    /**
     * Sets a localizer for the current thread. This is accomplished
     * by using the object {@link ERXThreadStorage}
     * @param currentLocalizer to set in thread storage for the current
     *		thread.
     */
    public static void setCurrentLocalizer(ERXLocalizer currentLocalizer) {
        ERXThreadStorage.takeValueForKey(currentLocalizer, "localizer");
    }

    /**
     * Gets the localizer for the default language.
     * @return localizer for the default language.
     */
    public static ERXLocalizer defaultLocalizer() {
        return localizerForLanguage(defaultLanguage());
    }
    
    public static ERXLocalizer localizerForSession(Object session) {
        if(session instanceof ERXSession) return ((ERXSession)session).localizer();
        if(session instanceof WOSession) return localizerForLanguages(((WOSession)session).languages());
        if(session instanceof NSDictionary) {
            NSDictionary dict = ((NSDictionary)session);
            Object l = dict.valueForKey("localizer");
            if(l != null)
                return (ERXLocalizer)l;
            Object language = dict.valueForKey("language");
            if(language != null)
                return localizerForLanguage((String)language);
        }
        return localizerForLanguage(defaultLanguage());
    }

    public static ERXLocalizer localizerForRequest(WORequest request) {
        return localizerForLanguages(request.browserLanguages());
    }    
    
    /**
     * Resets the localizer cache. If WOCaching is
     * enabled then after being reinitialize all of
     * the localizers will be reloaded.
     */
    public static void resetCache() {
        initialize();
        if (WOApplication.application().isCachingEnabled()) {
            Enumeration e = localizers.objectEnumerator();
            while (e.hasMoreElements()) {
                ((ERXLocalizer)e.nextElement()).load();
            }
        } else {
            localizers = new NSMutableDictionary();
        }
    }
    
    public static ERXLocalizer localizerForLanguages(NSArray languages) {
        if (! isLocalizationEnabled)   
            return createLocalizerForLanguage("Nonlocalized", false);

        if (languages == null || languages.count() == 0)  return localizerForLanguage(defaultLanguage());
        ERXLocalizer l = null;
        Enumeration e = languages.objectEnumerator();
        while(e.hasMoreElements()) {
            String language = (String)e.nextElement();
            l = (ERXLocalizer)localizers.objectForKey(language);
            if(l != null) {
                return l;
            }
            if(availableLanguages().containsObject(language)) {
                return localizerForLanguage(language);
            }
        }
        return localizerForLanguage((String)languages.objectAtIndex(0));
    }
    
    private static NSArray _languagesWithoutPluralForm = new NSArray(new Object [] {"Japanese"});
    
    public static ERXLocalizer localizerForLanguage(String language) {
        if (! isLocalizationEnabled)   
            return createLocalizerForLanguage("Nonlocalized", false);
            
        ERXLocalizer l = null;
        l = (ERXLocalizer)localizers.objectForKey(language);
        if(l == null) {
            if(availableLanguages().containsObject(language)) {
                if (_languagesWithoutPluralForm.containsObject(language))
                    l = createLocalizerForLanguage(language, false);
                else
                    l = createLocalizerForLanguage(language, true);
            } else {
                l = (ERXLocalizer)localizers.objectForKey(defaultLanguage());
                if(l == null) {
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


    public static String defaultLanguage() {
        if (defaultLanguage == null) {
            defaultLanguage = ERXProperties.stringForKeyWithDefault("er.extensions.ERXLocalizer.defaultLanguage", "English");
        }
        return defaultLanguage;
    }
    public static void setDefaultLanguage(String value) {
        defaultLanguage = value;
        resetCache();
    }

    public static NSArray fileNamesToWatch() {
        if (fileNamesToWatch == null) {
            fileNamesToWatch = ERXProperties.arrayForKeyWithDefault("er.extensions.ERXLocalizer.fileNamesToWatch", new NSArray(new Object [] {"Localizable.strings", "ValidationTemplate.strings"}));
            if (log.isDebugEnabled())
                log.debug("FileNamesToWatch: " + fileNamesToWatch);
        }
        return fileNamesToWatch;
    }
    public static void setFileNamesToWatch(NSArray value) {
        fileNamesToWatch = value;
        resetCache();
    }

    public static NSArray availableLanguages() {
        if(availableLanguages == null) {
            availableLanguages = ERXProperties.arrayForKeyWithDefault("er.extensions.ERXLocalizer.availableLanguages", new NSArray(new Object [] {"English", "German", "Japanese"}));
            if (log.isDebugEnabled())
                log.debug("AvailableLanguages: " + availableLanguages);
        }
        return availableLanguages;
    }
    public static void setAvailableLanguages(NSArray value) {
        availableLanguages = value;
        resetCache();
    }

    public static NSArray frameworkSearchPath() {
        if (frameworkSearchPath == null) {
            frameworkSearchPath = ERXProperties.arrayForKeyWithDefault("er.extensions.ERXLocalizer.frameworkSearchPath", new NSArray(new Object [] {"app", "ERDirectToWeb", "ERExtensions"}));
            if (log.isDebugEnabled())
                log.debug("FrameworkSearchPath: " + frameworkSearchPath);
        }
        return frameworkSearchPath;
    }
    public static void setFrameworkSearchPath(NSArray value) {
        frameworkSearchPath = value;
        resetCache();
    }

    /**
     * Creates a localizer for a given language and with an
     * indication if the language supports plural forms. To provide
     * your own subclass of an ERXLocalizer you can set the system
     * property <code>er.extensions.ERXLocalizer.pluralFormClassName</code>
     * or <code>er.extensions.ERXLocalizer.nonPluralFormClassName</code>.
     * @param language name to construct the localizer for
     * @param pluralForm denotes if the language supports the plural form
     * @return a localizer for the given language
     */
    protected static ERXLocalizer createLocalizerForLanguage(String language, boolean pluralForm) {
        ERXLocalizer localizer = null;
        String className = null;
        if (pluralForm) {
            className = ERXProperties.stringForKeyWithDefault("er.extensions.ERXLocalizer.pluralFormClassName", "er.extensions.ERXLocalizer");
        } else {
            className = ERXProperties.stringForKeyWithDefault("er.extensions.ERXLocalizer.nonPluralFormClassName", "er.extensions.ERXNonPluralFormLocalizer");            
        }
        try {
            Class localizerClass = Class.forName(className);
            Constructor constructor = localizerClass.getConstructor(ERXConstant.StringClassArray);
            localizer = (ERXLocalizer)constructor.newInstance(new Object[] {language});
        } catch (Exception e) {
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
    
    public static void setLocalizerForLanguage(ERXLocalizer l, String language) {
        localizers.setObjectForKey(l, language);
    }

    
    protected NSMutableDictionary cache;
    private NSMutableDictionary createdKeys;
    private String NOT_FOUND = "**NOT_FOUND**";
    protected Hashtable _dateFormatters = new Hashtable();
    protected Hashtable _numberFormatters = new Hashtable();
    protected String language;
    protected Locale locale;
    

    public ERXLocalizer(String aLanguage) {
        language = aLanguage;
        cache = new NSMutableDictionary();
        createdKeys = new NSMutableDictionary();

        // We first check to see if we have a locale register for the language name
        String shortLanguage = System.getProperty("er.extensions.ERXLocalizer." + aLanguage + ".locale");

        // Let's go fishing
        if (shortLanguage == null) {
            NSDictionary dict = ERXDictionaryUtilities.dictionaryFromPropertyList("Languages",
                                                                                  NSBundle.bundleForName("JavaWebObjects"));
            NSArray keys = dict.allKeysForObject(aLanguage);
            if (keys.count() > 0) {
                shortLanguage = (String)keys.objectAtIndex(0);
                if (keys.count() > 1) {
                    log.error("Found multiple entries for language \"" + aLanguage
                              + "\" in Language.plist file! Found keys: " + keys);
                }
            }
        }
        if (shortLanguage != null) {
            locale = new Locale(shortLanguage);
        } else {
            log.info("Locale for " + aLanguage + " not found! Using default locale: " + Locale.getDefault());
            locale = Locale.getDefault();
        }
        load();
    }
    
    public void load() {
        cache.removeAllObjects();
        createdKeys.removeAllObjects();

        if (log.isDebugEnabled())
            log.debug("Loading templates for language: " + language + " for files: "
                      + fileNamesToWatch() + " with search path: " + frameworkSearchPath());
        
        NSArray languages = new NSArray(language);
        Enumeration fn = fileNamesToWatch().objectEnumerator();
        while(fn.hasMoreElements()) {
            String fileName = (String)fn.nextElement();
            Enumeration fr = frameworkSearchPath().reverseObjectEnumerator();
            while(fr.hasMoreElements()) {
                String framework = (String)fr.nextElement();
                
                String path = ERXFileUtilities.pathForResourceNamed(fileName, framework, languages);
                if(path != null) {
                    try {
                        framework = "app".equals(framework) ? null : framework;
                        log.debug("Loading: " + fileName + " - " 
                            + (framework == null ? "app" : framework) + " - " 
                            + languages + ERXFileUtilities.pathForResourceNamed(fileName, framework, languages));
                        String encoding = ERXProperties.stringForKey("er.extensions.ERXLocalizer.encodingForLocalizationFiles");
                       NSDictionary dict = (NSDictionary)ERXFileUtilities.readPropertyListFromFileInFramework(fileName, framework, languages,encoding);
                       // HACK: ak we have could have a collision between the search path for validation strings and
                       // the normal localized strings.
                       if(fileName.indexOf(ERXValidationFactory.VALIDATION_TEMPLATE_PREFIX) == 0) {
                           NSMutableDictionary newDict = new NSMutableDictionary();
                           for(Enumeration keys = dict.keyEnumerator(); keys.hasMoreElements(); ) {
                               String key = (String)keys.nextElement();
                               newDict.setObjectForKey(dict.objectForKey(key), ERXValidationFactory.VALIDATION_TEMPLATE_PREFIX + key);
                           }
                           dict = newDict;
                       }
                        cache.addEntriesFromDictionary(dict);
                        if(!monitoredFiles.containsObject(path)) {
                            ERXFileNotificationCenter.defaultCenter().addObserver(observer,
                                                                                  new NSSelector("fileDidChange",
                                                                                                 ERXConstant.NotificationClassArray),
                                                                                  path);
                            monitoredFiles.addObject(path);
                        }
                    } catch(Exception ex) {
                        log.warn("Exception loading: " + fileName + " - " 
                            + (framework == null ? "app" : framework) + " - " 
                            + languages + ":" + ex, ex);
                    }
                } else  {
                    log.debug("Unable to create path for resource named: " + fileName 
                        + " framework: " + (framework == null ? "app" : framework)
                        + " languages: " + languages);
                }
            }
        }
    }

    protected NSDictionary readPropertyListFromFileInFramework(String fileName, String framework, NSArray languages){
        NSDictionary dict = (NSDictionary)ERXExtensions.readPropertyListFromFileInFramework(fileName, framework, languages);
        return dict;
    }
    
    /**
     * Cover method that calls <code>localizedStringForKey</code>.
     * @param key to resolve a localized varient of
     * @return localized string for the given key
     */
    public Object valueForKey(String key) {
        return valueForKeyPath(key);
    }
    
    public Object valueForKeyPath(String key) {
        Object result = localizedValueForKey(key);
        if(result == null) {
            int indexOfDot = key.indexOf(".");
            if(indexOfDot > 0) {
                String firstComponent = key.substring(0, indexOfDot);
                String otherComponents = key.substring(indexOfDot+1, key.length());
                result = cache.objectForKey(firstComponent);
                if(log.isDebugEnabled())
                    log.debug("Trying " + firstComponent + " . " + otherComponents);
                if(result != null) {
                    try {
                        result = NSKeyValueCodingAdditions.Utility.valueForKeyPath(result, otherComponents);
                        if(result != null) {
                            cache.setObjectForKey(result, key);
                        } else {
                            cache.setObjectForKey(NOT_FOUND, key);
                        }
                    } catch (NSKeyValueCoding.UnknownKeyException e) {
                        if (log.isDebugEnabled()) {
                            log.debug(e.getMessage());
                        }
                        cache.setObjectForKey(NOT_FOUND, key);
                    }
                }
            }
        }
        return result;
    }
    public void takeValueForKey(Object value, String key) {
        cache.setObjectForKey(value, key);
    }
    public void takeValueForKeyPath(Object value, String key) {
        cache.setObjectForKey(value, key);
    }

    public String language() { return language; }
    public NSDictionary createdKeys() { return createdKeys; }

    public Object localizedValueForKeyWithDefault(String key) {
        if(key == null) {
            log.warn("Attempt to insert null key!");
            return null;
        }
        Object result = localizedValueForKey(key);
        if(result == null) {
            if(createdKeysLog.isDebugEnabled())
                createdKeysLog.debug("Default key inserted: '"+key+"'/"+language);
            cache.setObjectForKey(key, key);
            createdKeys.setObjectForKey(key, key);
            result = key;
        }
        return result;
    }

    public Object localizedValueForKey(String key) {
        Object result = cache.objectForKey(key);
        if(key == null || result == NOT_FOUND) return null;
        if(result != null) return result;

        if(createdKeysLog.isDebugEnabled())
            log.debug("Key not found: '"+key+"'/"+language);
        cache.setObjectForKey(NOT_FOUND, key);
        return null;
    }

    public String localizedStringForKeyWithDefault(String key) {
        return (String)localizedValueForKeyWithDefault(key);
    }
    public String localizedStringForKey(String key) {
        return (String)localizedValueForKey(key);
    }

    public String localizedTemplateStringForKeyWithObject(String key, Object o1) {
        return localizedTemplateStringForKeyWithObjectOtherObject(key, o1, null);
    }

    public String localizedTemplateStringForKeyWithObjectOtherObject(String key, Object o1, Object o2) {
        String template = (String)valueForKeyPath(key);
        if (template != null)
            return ERXSimpleTemplateParser.sharedInstance().parseTemplateWithObject(template, null, o1, o2);
        return key;
    }

    private String _plurify(String s, int howMany) {
        String result=s;
        if (s!=null && howMany!=1) {
            if (s.endsWith("y"))
                result=s.substring(0,s.length()-1)+"ies";
            else if (s.endsWith("s") && ! s.endsWith("ss")) {
                // we assume it's already plural. There are a few words this will break this heuristic
                // e.g. gas --> gases
                // but otherwise for Documents we get Documentses..
            } else if (s.endsWith("s") || s.endsWith("ch") || s.endsWith("sh") || s.endsWith("x"))
                result+="es";
            else
                result+= "s";
        }
        return result;
    }

    private String _singularify(String value) {
        String result = value;
        if (value!=null) {
            if (value.endsWith("ies"))
                result = value.substring(0,value.length()-3)+"y";
            else if (value.endsWith("hes"))
                result = value.substring(0,value.length()-2);
            else if (!value.endsWith("ss") && (value.endsWith("s") || value.endsWith("ses")))
                result = value.substring(0,value.length()-1);
        }
        return result;
    }
    
    // name is already localized!
    // subclasses can override for more sensible behaviour
    public String plurifiedStringWithTemplateForKey(String key, String name, int count, Object helper) {
        NSDictionary dict = new NSDictionary( new Object[] {plurifiedString(name, count), new Integer(count)},
                                              new Object[] {"pluralString", "pluralCount"});
        return localizedTemplateStringForKeyWithObjectOtherObject(key, dict, helper);
    }

    public String plurifiedString(String name, int count) {
        return _plurify(name, count);
    }
    
    public String toString() { return "<" + getClass().getName() + " " + language + ">"; }

    /**
	 * Returns a localized date formatter for the given key.
	 * @param formatString
	 * @return
	 */
	
    public Format localizedDateFormatForKey(String formatString) {
    	formatString = formatString == null ? "%Y/%m/%d" : formatString;
		Format result = (Format)_dateFormatters.get(formatString);
		if(result == null) {
			// HACK ak
			// we need to sync on the localizer and hold our breath that no one else relies on Locale.getDefault()
			// at this time. All of this because the NSTimestampFormatter doesn't have a setLocale() method...
			synchronized(ERXLocalizer.class) {
				Locale old = Locale.getDefault();
				Locale.setDefault(locale());
				NSTimestampFormatter formatter = new NSTimestampFormatter(formatString);
				result = formatter;
				_dateFormatters.put(formatString, result);
				Locale.setDefault(old);
			}
		}
		return result;
	}

	/**
	 * Returns a localized number formatter for the given key.
	 * @param formatString
	 * @return
	 */
    public Format localizedNumberFormatForKey(String formatString) {
    	formatString = formatString == null ? "#,##0.00;-(#,##0.00)" : formatString;
		Format result = (Format)_numberFormatters.get(formatString);
		if(result == null) {
			synchronized(_numberFormatters) {
				NSNumberFormatter formatter = new ERXNumberFormatter();
				formatter.setLocale(locale()); 
				formatter.setLocalizesPattern(true);
				formatter.setPattern(formatString);
				result = formatter;
				_numberFormatters.put(formatString, result);
			}
		}
		return result;
	}


	/**
	 * @param formatter
	 * @param pattern
	 * @return
	 */
	public void setLocalizedNumberFormatForKey(Format formatter, String pattern) {
		_numberFormatters.put(pattern, formatter);
	}

	/**
	 * @return
	 */
	public Locale locale() {
		return locale;
	}
	
	public void setLocale(Locale value) {
		locale = value;
	}

	/**
	 * @param formatter
	 * @param pattern
	 */
	public void setLocalizedDateFormatForKey(NSTimestampFormatter formatter, String pattern) {
		_dateFormatters.put(pattern, formatter);
	}
}
