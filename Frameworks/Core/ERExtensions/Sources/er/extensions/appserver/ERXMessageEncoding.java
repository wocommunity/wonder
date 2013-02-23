//
// ERXMessageEncoding.java
// Project ERExtensions
//
// Created by tatsuya on Wed Jun 05 2002
//
package er.extensions.appserver;

import java.io.Serializable;
import java.util.Enumeration;

import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXSimpleTemplateParser;

/**
 * Holds encoding related settings and methods for {@link WOMessage} 
 * and its subclasses {@link WORequest} and {@link WOResponse}. 
 */
public class ERXMessageEncoding implements Serializable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private String _encoding; 
    public String encoding() { return _encoding; }
    
    public ERXMessageEncoding(String languageOrEncoding) {
        if (availableEncodings().containsObject(languageOrEncoding)) {
            _encoding = languageOrEncoding;
        } else if (availableLanguages().containsObject(languageOrEncoding)) {
            _encoding = defaultEncodingForLanguage(languageOrEncoding);
        } else {
            _encoding = defaultEncoding();
        }
    }
    
    public ERXMessageEncoding(NSArray preferedLanguages) {
        _encoding = null;
        NSArray availableLanguages = availableLanguages();
        
        Enumeration e = preferedLanguages.objectEnumerator();
        while (e.hasMoreElements()) {
            String aPreferedLanguage = (String)e.nextElement();
            if (availableLanguages.containsObject(aPreferedLanguage)) {
                _encoding = defaultEncodingForLanguage(aPreferedLanguage);
                break;
            }
        }
        if (_encoding == null) 
            _encoding = defaultEncoding();
    }

    public static void resetToFactoryDefault() { 
        _encodings = null;
        _languagesAndDefaultEncodings = null;
    }   

    public static NSArray availableEncodings() { 
        return _encodings().allKeys();
    }

    public static NSArray availableLanguages() { 
        return _languagesAndDefaultEncodings().allKeys();
    }

    private static NSDictionary _encodings;
    private static NSDictionary _encodings() { 
        if (_encodings == null) {
            _encodings = ERXDictionaryUtilities.dictionaryWithObjectsAndKeys(
                new Object [] { "ISO-8859-1",  "ISO8859_1",
                                "ISO-8859-1",  "ISO-8859-1",
                                "Shift_JIS",   "SJIS", 
                                "Shift_JIS",   "SHIFT_JIS", 
                                "EUC-JP",      "EUC_JP", 	//Note: dash and underscore
                                "EUC-JP",      "EUC-JP",
                                "iso-2022-jp", "ISO2022JP", 
                                "iso-2022-jp", "ISO-2022-JP", 
                                "UTF-8",       "UTF8",
                                "UTF-8",       "UTF-8" });
        }
        return _encodings;
    }

    private static NSDictionary _languagesAndDefaultEncodings;
    private static NSDictionary _languagesAndDefaultEncodings() {
        if (_languagesAndDefaultEncodings == null) {
            _languagesAndDefaultEncodings = ERXDictionaryUtilities.dictionaryWithObjectsAndKeys(
                new Object [] { "ISO8859_1", "English", 
                                "ISO8859_1", "German", 
                                "SJIS",      "Japanese" });
        }
        return _languagesAndDefaultEncodings;
    }
    private static void _setLanguagesAndDefaultEncodings(NSDictionary newLanguagesAndDefaultEncodings) {
        _languagesAndDefaultEncodings = newLanguagesAndDefaultEncodings;
    }

    private static String _defaultEncoding;
    public static String defaultEncoding() {
        if (_defaultEncoding == null) 
            _defaultEncoding = "ISO8859_1";
        return _defaultEncoding;
    }
    public static void setDefaultEncoding(String newDefaultEncoding) {
        if (! availableEncodings().containsObject(newDefaultEncoding.toUpperCase())) 
            throw createIllegalArgumentException(newDefaultEncoding, "encoding", "availableEncodings()");

        _defaultEncoding = newDefaultEncoding;
    }

    public static void setDefaultEncodingForAllLanguages(String newDefaultEncoding) {
        // This statement may throw an IllegalArgumentException when newDefaultEncoding isn't supported.  
        setDefaultEncoding(newDefaultEncoding); 

        NSMutableDictionary d = new NSMutableDictionary(_languagesAndDefaultEncodings());
        Enumeration e = d.keyEnumerator();
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            d.setObjectForKey(newDefaultEncoding, key);
        }
        _setLanguagesAndDefaultEncodings(d);
    }

    public static String defaultEncodingForLanguage(String language) {
        String defaultEncoding = null;
        if (availableLanguages().containsObject(language)) 
            defaultEncoding = (String)_languagesAndDefaultEncodings.objectForKey(language);
        if (defaultEncoding == null) 
            defaultEncoding = defaultEncoding();
        return defaultEncoding; 
    }
    public static void setDefaultEncodingForLanguage(String encoding, String language) {
        if (! availableLanguages().containsObject(language)) 
            throw createIllegalArgumentException(language, "language", "availableLanguages()");
        if (! availableEncodings().containsObject(encoding)) 
            throw createIllegalArgumentException(encoding, "encoding", "availableEncodings()");
        
        NSMutableDictionary d = new NSMutableDictionary(_languagesAndDefaultEncodings);
        d.setObjectForKey(encoding, language);
        _languagesAndDefaultEncodings = d;
    }

    public static void setEncodingToResponse(WOResponse response, String encoding) {
    	encoding = encoding.toUpperCase();
        if (! availableEncodings().containsObject(encoding)) 
            throw createIllegalArgumentException(encoding, "encoding", "availableEncodings()");

        String mimeType = response.headerForKey("Content-Type");
        if (mimeType != null && (mimeType.equals("text/html") || mimeType.equals("text/xml"))) {
            response.setContentEncoding (encoding); 
            response.setHeader(mimeType + "; charset=" + _encodings().objectForKey(encoding), "Content-Type");
        }
    }

    public void setEncodingToResponse(WOResponse response) {
        setEncodingToResponse(response, encoding());
    }
    
    public static void setDefaultFormValueEncodingToRequest(WORequest request, String encoding) {
    	encoding = encoding.toUpperCase();
        if (! availableEncodings().containsObject(encoding)) 
            throw createIllegalArgumentException(encoding, "encoding", "availableEncodings()");

        request.setDefaultFormValueEncoding (encoding); 
        // request.setFormValueEncodingDetectionEnabled (true);
    }

    public void setDefaultFormValueEncodingToRequest(WORequest request) {
        setDefaultFormValueEncodingToRequest(request, encoding());
    }
    
    protected static IllegalArgumentException createIllegalArgumentException(String value, String target, String listingMethod) {
        NSDictionary d = ERXDictionaryUtilities.dictionaryWithObjectsAndKeys(
                new Object [] { value,		"value", 
                                target, 	"target", 
                                listingMethod,	"listingMethod" });
        ERXSimpleTemplateParser parser = ERXSimpleTemplateParser.sharedInstance();
        String message = parser.parseTemplateWithObject(
                    "@@value@@ isn't a supported @@target@@. (Not listed under @@listingMethod@@)", null, d, null);
        return new IllegalArgumentException(message);
    }

    private String _toString;
    @Override
    public String toString() {
        if (_toString == null) {
            _toString = "<" + getClass().getName() 
                        + " encoding: " + _encoding
                        + ">";
        }
        return _toString;
    }
}
