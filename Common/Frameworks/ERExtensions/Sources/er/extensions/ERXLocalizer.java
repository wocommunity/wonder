//
// ERXLocalizer.java
// Project armehaut
//
// Created by ak on Sun Apr 14 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import java.util.*;

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
er.extensions.ERXLocalizer.fileNamesToWatch=Localizable.strings,ValidationTemplate.strings
er.extensions.ERXLocalizer.availableLanguages=English,German
er.extensions.ERXLocalizer.frameworkSearchPath=app,ERDirectToWeb,ERExtensions

TODO: chaining of Localizers
*/

public class ERXLocalizer implements NSKeyValueCoding, NSKeyValueCodingAdditions  {
    static final ERXLogger cat = ERXLogger.getLogger(ERXLocalizer.class);
    private static boolean isLocalizationEnabled = false;
    public static final String LocalizationDidResetNotification = "LocalizationDidReset";
    
    public static class Observer {
        public void fileDidChange(NSNotification n) {
            ERXLocalizer.resetCache();
            NSNotificationCenter.defaultCenter().postNotification(LocalizationDidResetNotification, null);
        }
    }

    private static Observer observer;
    private static NSMutableArray monitoredFiles;
    
    public static void initialize() {
        observer = new Observer();
        monitoredFiles = new NSMutableArray();
        isLocalizationEnabled = ERXUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXLocalizer.isLocalizationEnabled"), true);
    }

    public static boolean isLocalizationEnabled() { return isLocalizationEnabled; }
    public static void setIsLocalizationEnabled(boolean value) { isLocalizationEnabled = value; }
    
    static NSArray fileNamesToWatch;
    static NSArray frameworkSearchPath;
    static NSArray availableLanguages;
    static String defaultLanguage;

    
    static NSMutableDictionary localizers = new NSMutableDictionary();
    
    private NSMutableDictionary cache;
    private String NOT_FOUND = "**NOT_FOUND**";

    public static void resetCache() {
        if(WOApplication.application().isCachingEnabled()) {
            Enumeration e = localizers.objectEnumerator();
            while(e.hasMoreElements()) {
                ((ERXLocalizer)e.nextElement()).load();
            }
        } else {
            localizers = new NSMutableDictionary();
        }
    }
    public static ERXLocalizer localizerForLanguages(NSArray languages) {
        if(languages == null || languages.count() == 0) return localizerForLanguage(defaultLanguage());
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
    
    public static ERXLocalizer localizerForLanguage(String language) {
        ERXLocalizer l = null;
        l = (ERXLocalizer)localizers.objectForKey(language);
        if(l == null) {
            if(availableLanguages().containsObject(language)) {
                l = new ERXLocalizer(language);
            } else {
                l = (ERXLocalizer)localizers.objectForKey(defaultLanguage());
            }
            localizers.setObjectForKey(l, language);
        }
        return l;
    }

    public Object valueForKey(String key) {
        return localizedStringForKey(key);
    }
    public Object valueForKeyPath(String key) {
        if("localizerLanguage".equals(key)) return localizerLanguage();
        Object result = valueForKey(key);
        if(result == null) {
            int indexOfDot = key.indexOf(".");
            if(indexOfDot > 0) {
                String firstComponent = key.substring(0, indexOfDot);
                String otherComponents = key.substring(indexOfDot+1, key.length());
                result = cache.objectForKey(firstComponent);
                cat.info("Trying " + firstComponent + " . " + otherComponents);
                if(result != null) {
                    result = NSKeyValueCodingAdditions.Utility.valueForKeyPath(result, otherComponents);
                    if(result != null) {
                        cache.setObjectForKey(result, key);
                    } else {
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
    
    String language;
    public ERXLocalizer(String aLanguage) {
        language = aLanguage;
        cache = new NSMutableDictionary();
        load();
    }

    public void load() {
        cache.removeAllObjects();
        
        NSArray languages = new NSArray(language);
        WOResourceManager rm = WOApplication.application().resourceManager();
        Enumeration fn = fileNamesToWatch().objectEnumerator();
        while(fn.hasMoreElements()) {
            String fileName = (String)fn.nextElement();
            Enumeration fr = frameworkSearchPath().reverseObjectEnumerator();
            while(fr.hasMoreElements()) {
                String framework = (String)fr.nextElement();
                
                String path = rm.pathForResourceNamed(fileName, framework, languages);
                if(path != null) {
                    if(!monitoredFiles.containsObject(path)) {
                        ERXFileNotificationCenter.defaultCenter().addObserver(observer, new NSSelector("fileDidChange", ERXConstant.NotificationClassArray), path);
                        monitoredFiles.addObject(path);
                    }
                    try {
                        NSDictionary dict = (NSDictionary)ERXExtensions.readPropertyListFromFileInFramework(fileName, framework, languages);
                        cache.addEntriesFromDictionary(dict);
                    } catch(Exception ex) {
                        cat.warn("Exception loading: " + fileName + " - " + framework + " - " + languages + ":" + ex);
                    }
                }
            }
        }
    }

    public String localizedStringForKeyWithDefault(String key) {
        String result = localizedStringForKey(key);
        if(result == null) {
            cat.info("Default key inserted: '"+key+"'/"+language);
            cache.setObjectForKey(key, key);
            result = key;
        }
        return result;
    }
    public String localizerLanguage() {return language;}
    
    public String localizedStringForKey(String key) {
        String result = (String)cache.objectForKey(key);
        if(result == NOT_FOUND) return null;
        if(result != null) return result;

        cat.warn("Key not found: '"+key+"'/"+language);
        cache.setObjectForKey(NOT_FOUND, key);
        return null;
    }

    public String localizedTemplateStringForKeyWithObject(String key, Object o1) {
        return localizedTemplateStringForKeyWithObjectOtherObject(key, o1, null);
    }

    public String localizedTemplateStringForKeyWithObjectOtherObject(String key, Object o1, Object o2) {
        String template = localizedStringForKey(key);
        return ERXSimpleTemplateParser.sharedInstance().parseTemplateWithObject(template, null, o1, o2);
    }

    public String toString() {return "<ERXLocalizer "+language+">";}


    public static String defaultLanguage() {
        if(defaultLanguage == null) {
            defaultLanguage = System.getProperty("er.extensions.ERXLocalizer.defaultLanguage");
            if(defaultLanguage == null) {
                defaultLanguage = "English";
            }
        }
        return defaultLanguage;
    }
    public static void setDefaultLanguage(String value) {
        defaultLanguage = value;
        resetCache();
    }

    public static NSArray fileNamesToWatch() {
        if(fileNamesToWatch == null) {
            String fileNamesToWatchString = System.getProperty("er.extensions.ERXLocalizer.fileNamesToWatch");
            if(fileNamesToWatchString == null) {
                fileNamesToWatch = new NSArray(new Object [] {"Localizable.strings", "ValidationTemplate.strings"});
            } else {
                fileNamesToWatch = NSArray.componentsSeparatedByString(fileNamesToWatchString, ",");
            }
        }
        return fileNamesToWatch;
    }
    public static void setFileNamesToWatch(NSArray value) {
        fileNamesToWatch = value;
        resetCache();
    }

    public static NSArray availableLanguages() {
        if(availableLanguages == null) {
            String availableLanguagesString = System.getProperty("er.extensions.ERXLocalizer.availableLanguages");
            if(availableLanguages == null) {
                availableLanguages = new NSArray(new Object [] {"English", "German"});
            } else {
                availableLanguages = NSArray.componentsSeparatedByString(availableLanguagesString, ",");
            }
        }
        return availableLanguages;
    }
    public static void setAvailableLanguages(NSArray value) {
        availableLanguages = value;
        resetCache();
    }

    public static NSArray frameworkSearchPath() {
        if(frameworkSearchPath == null) {
            String frameworkSearchPathString = System.getProperty("er.extensions.ERXLocalizer.frameworkSearchPath");
            if(frameworkSearchPath == null) {
                frameworkSearchPath = new NSArray(new Object [] {"app", "ERDirectToWeb", "ERExtensions"});
            } else {
                frameworkSearchPath = NSArray.componentsSeparatedByString(frameworkSearchPathString, ",");
            }
        }
        return frameworkSearchPath;
    }
    public static void setFrameworkSearchPath(NSArray value) {
        frameworkSearchPath = value;
        resetCache();
    }
}
