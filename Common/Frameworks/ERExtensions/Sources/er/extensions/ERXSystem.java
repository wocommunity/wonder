/*
 * Created on 28.09.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;


/**
 * @author david teran
 *
 */
public class ERXSystem implements NSKeyValueCoding, NSKeyValueCodingAdditions {

    /**
     * 
     */
    private ERXSystem() {
        super();
    }
    private static ERXSystem sharedInstance = new ERXSystem();
    
    public static String getProperty(String key) {
        String oriValue = (String) sharedInstance.valueForKey(key);
        String convertedValue = oriValue;
        String lastConvertedValue = null;
        while (convertedValue != lastConvertedValue && convertedValue.indexOf("@@") > -1) {
            lastConvertedValue = convertedValue;
            convertedValue = ERXSimpleTemplateParser.sharedInstance().parseTemplateWithObject(convertedValue, "@@", sharedInstance, WOApplication.application());
        }
        return convertedValue;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    /* (non-Javadoc)
     * @see com.webobjects.foundation.NSKeyValueCoding#valueForKey(java.lang.String)
     */
    public Object valueForKey(String key) {
        return System.getProperty(key);
    }

    /* (non-Javadoc)
     * @see com.webobjects.foundation.NSKeyValueCoding#takeValueForKey(java.lang.Object, java.lang.String)
     */
    public void takeValueForKey(Object arg0, String arg1) {
        throw new RuntimeException("not implemented");
    }

    /* (non-Javadoc)
     * @see com.webobjects.foundation.NSKeyValueCodingAdditions#valueForKeyPath(java.lang.String)
     */
    public Object valueForKeyPath(String key) {
        return System.getProperty(key);
    }

    /* (non-Javadoc)
     * @see com.webobjects.foundation.NSKeyValueCodingAdditions#takeValueForKeyPath(java.lang.Object, java.lang.String)
     */
    public void takeValueForKeyPath(Object arg0, String arg1) {
        throw new RuntimeException("not implemented");
    }
}
