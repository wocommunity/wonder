//
// StringUtilities.java
// Project linksadmin
//
// Created by ak on Mon Nov 05 2001
//
package er.extensions;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.io.*;

/**
 * Collection of {@lin String} utilities. Contains
 * the base localization support.
 */
public class ERXStringUtilities extends Object {

    /** Holds the default display language, which is English */
    private static final String DEFAULT_TARGET_DISPLAY_LANGUAGE = "English";
    /** 
     * Holds the array of default display languages. Holds
     * a single entry for English.
     */
    private static NSArray _defaultTargetDisplayLanguages = new NSArray(DEFAULT_TARGET_DISPLAY_LANGUAGE);

    /**
     * Gets a localized string for a given key in the application's
     * Localizable strings file for the default language (English).
     * @param key to be lookup in the strings file
     * @return string value of the looked up key
     */
    // CHECKME: Should this be using the framework search order?
    public static String localizedStringForKey(String key) {
        return localizedStringForKey(key, null, null);
    }

    /**
     * Gets a localized string for a given key in a given framework's
     * Localizable strings file for the default language (English).
     * @param key to be lookup in the strings file
     * @param framework name, specify app or null to perform the
     *		lookup in the application's resources.
     * @return string value of the looked up key
     */    
    public static String localizedStringForKey(String key, String framework) {
        return localizedStringForKey(key, framework, null);
    }

    /**
     * Gets a localized string for a given key in a given framework's
     * Localizable strings file using the array of languages as the
     * search order for the key.
     * @param key to be lookup in the strings file
     * @param framework name, specify app or null to perform the
     *		lookup in the application's resources.
     * @param languages array to search for the key in
     * @return string value of the looked up key
     */    
    public static String localizedStringForKey(String key, String framework, NSArray languages) {
        languages = languages != null && languages.count() > 0 ? languages : _defaultTargetDisplayLanguages;
        String result = WOApplication.application().resourceManager().stringForKey( key, "Localizable", key, framework, languages);
        return result;
    }

    /**
     * Uses the method <code>localizedStringForKey</code> to retreive
     * a template that is then parsed using the passed in object to
     * produce a resulting string. The template parser used is
     * {@link ERXSimpleTemplateParser}.
     * @param o object used to resolve keys in the localized template
     * @param key to be lookup in the strings file
     * @param framework name, specify app or null to perform the
     *		lookup in the application's resources.
     * @param languages array to search for the key in
     * @return localized template parsed and resolved with the given
     *		object.
     */    
    public static String localizedTemplateStringWithObjectForKey(Object o, String key, String framework, NSArray languages) {
        String template = localizedStringForKey(key, framework, languages);
        return ERXSimpleTemplateParser.sharedInstance().parseTemplateWithObject(template, null, o);
    }

    /**
     * Reads the contents of a file given by a path
     * into a string.
     * @param path to the file in the file system
     * @return the contents of the file in a string
     */
    public static String stringWithContentsOfFile(String path) {
        try {
            InputStream in = new FileInputStream(path);
            
            if (null == in)
                throw new RuntimeException("The file '"+ path + "' can not be opened.");
            int length = in.available();
            if (length == 0) {
                return "";
            }
            byte buffer[] = new byte[length];
            in.read(buffer);
            in.close();
            return new String(buffer);
        } catch(Throwable t) {
            // cat.debug(t.toString());
        }
        return null;
    }

    /**
     * Calculates an Integer for a given string. The
     * only advantage that this method has is to not
     * throw a number format exception if the string
     * is not correctly formatted.
     * @param s string to caclulate an Integer from
     * @return parsed Integer from the string or null
     *		if the string is not correctly formed.
     */
    public static Integer integerWithString(String s) {
        try {
            return new Integer(Integer.parseInt(s));
        } catch (Exception e) {
        }
        return null;
    } 

    /**
     * Retrives a given string for a given name, extension
     * and bundle.
     * @param name of the resource
     * @param extension of the resource, example: txt or rtf
     * @param bundle to look for the resource in
     * @return string of the given file specified in the bundle
     */
    // CHECKME: The bundle isn't being used. Is this right?
    public static String stringFromResource(String name, String extension, NSBundle bundle) {
         return stringWithContentsOfFile(WOApplication.application().resourceManager().pathForResourceNamed(name +"." + extension, null, null));
    }

    public static final String lastPropertyKeyInKeyPath(String keyPath) {
        String part = null;
        if (keyPath != null) {
            int index = keyPath.lastIndexOf(".");
            if (index != -1)
                part = keyPath.substring(index + 1);
            else
                part = keyPath;
        }
        return part;
    }

    public static final String keyPathWithoutLastProperty(String keyPath) {
        String part = null;
        if(keyPath != null) {
            int index = keyPath.lastIndexOf(".");
            if (index != -1)
                part = keyPath.substring(0, index);
        }
        return part;
    }
}
