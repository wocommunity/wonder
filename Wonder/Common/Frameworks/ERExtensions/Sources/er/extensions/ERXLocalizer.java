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

/** KVC access to String localization.
Monitors a set of files in all frameworks and returns a string given a key for a language.
In the current state, it's more a stub for things to come.
To be used in components with:
   valueForKey("session.localizer." + whichKey)
  
TODO: FileObserver, Session integration, chaining of Localizers, API to add files and frameworks
*/

public class ERXLocalizer implements NSKeyValueCoding {
    static final ERXLogger cat = ERXLogger.getLogger(ERXLocalizer.class);

    // these will eventually become defaults
    static NSArray fileNamesToWatch = new NSArray(new Object [] {"Localizable.strings", "ValidationTemplate.strings"});
    static NSArray frameworkSearchPath = new NSArray(new Object [] {"app", "ERDirectToWeb", "ERExtensions"});
    static NSArray availableLanguages = new NSArray(new Object [] {"English", "German"});
    static String defaultLanguage = "English";


    static NSMutableDictionary localizers = new NSMutableDictionary();
    
    private NSMutableDictionary cache;
    private String NOT_FOUND = "**NOT_FOUND**";

    public static void resetCache() {
        Enumeration e = localizers.objectEnumerator();
        while(e.hasMoreElements()) {
            ((ERXLocalizer)e.nextElement()).load();
        }
    }
    public static ERXLocalizer localizerForLanguages(NSArray languages) {
        if(languages == null || languages.count() == 0) return localizerForLanguage(defaultLanguage);
        ERXLocalizer l = null;
        Enumeration e = localizers.objectEnumerator();
        while(e.hasMoreElements()) {
            String language = (String)e.nextElement();
            l = (ERXLocalizer)localizers.objectForKey(language);
            if(l != null) {
                return l;
            }
            if(availableLanguages.containsObject(language)) {
                return localizerForLanguage(language);
            }
        }
        return localizerForLanguage((String)languages.objectAtIndex(0));
    }
    
    public static ERXLocalizer localizerForLanguage(String language) {
        ERXLocalizer l = null;
        l = (ERXLocalizer)localizers.objectForKey(language);
        if(l == null) {
            if(availableLanguages.containsObject(language)) {
                l = new ERXLocalizer(language);
            } else {
                l = (ERXLocalizer)localizers.objectForKey(defaultLanguage);
            }
            localizers.setObjectForKey(l, language);
        }
        return l;
    }

    public Object valueForKey(String key) {
        return localizedStringForKey(key);
    }
    public void takeValueForKey(Object value, String key) {
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
        Enumeration fn = fileNamesToWatch.objectEnumerator();
        while(fn.hasMoreElements()) {
            String fileName = (String)fn.nextElement();
            Enumeration fr = frameworkSearchPath.reverseObjectEnumerator();
            while(fr.hasMoreElements()) {
                String framework = (String)fr.nextElement();
                String path = rm.pathForResourceNamed(fileName, framework, languages);
                if(path != null) {
                    NSDictionary dict = (NSDictionary)NSPropertyListSerialization.propertyListFromString(ERXStringUtilities.stringWithContentsOfFile(path));
                    cache.addEntriesFromDictionary(dict);
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
}
