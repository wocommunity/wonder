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

public class ERXStringUtilities extends Object {
    private static final String DEFAULT_TARGET_DISPLAY_LANGUAGE = "English";
    private static NSArray _defaultTargetDisplayLanguages = new NSArray(DEFAULT_TARGET_DISPLAY_LANGUAGE);

    public static String localizedStringForKey(String key) {
        return localizedStringForKey(key, null, null);
    }

    public static String localizedStringForKey(String key, String framework) {
        return localizedStringForKey(key, framework, null);
    }

    public static String localizedStringForKey(String key, String framework, NSArray languages) {
        languages = languages != null && languages.count() > 0 ? languages : _defaultTargetDisplayLanguages;
        String result = WOApplication.application().resourceManager().stringForKey( key, "Localizable", key, framework, languages);
        return result;
    }

    public static String localizedTemplateStringWithObjectForKey(Object o, String key, String framework, NSArray languages) {
        String template = localizedStringForKey(key, framework, languages);
        return ERXSimpleTemplateParser.sharedInstance().parseTemplateWithObject(template, null, o);
    }

    public static String stringWithContentsOfFile(String path) {
        try {
            InputStream in = new FileInputStream(path);
            
            if(null == in)
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

    public static Integer integerWithString(String s) {
        try {
            return new Integer(Integer.parseInt(s));
        } catch (Exception e) {
        }
        return null;
    } 

    public static String stringFromResource(String name, String extension, NSBundle bundle) {
         return stringWithContentsOfFile(WOApplication.application().resourceManager().pathForResourceNamed(name +"." + extension, null, null));
    }

}
