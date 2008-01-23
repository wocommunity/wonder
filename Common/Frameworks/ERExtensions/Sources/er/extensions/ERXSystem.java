/*
 * Created on 28.09.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package er.extensions;

import java.util.Enumeration;
import java.util.Properties;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSProperties;


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
        if (oriValue == null || oriValue.indexOf("@@") == -1) return oriValue;
        String lastConvertedValue = null;
        while (convertedValue != lastConvertedValue && convertedValue.indexOf("@@") > -1) {
            lastConvertedValue = convertedValue;
            convertedValue = new ERXSimpleTemplateParser("ERXSystem:KEY_NOT_FOUND").parseTemplateWithObject(convertedValue, "@@", sharedInstance, WOApplication.application());
        }
        if (convertedValue.indexOf("ERXSystem:KEY_NOT_FOUND") > -1) return oriValue;//not all keys are present
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
        return NSProperties.getProperty(key);
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
        return NSProperties.getProperty(key);
    }

    /* (non-Javadoc)
     * @see com.webobjects.foundation.NSKeyValueCodingAdditions#takeValueForKeyPath(java.lang.Object, java.lang.String)
     */
    public void takeValueForKeyPath(Object arg0, String arg1) {
        throw new RuntimeException("not implemented");
    }

    /**
     */
    public static Properties getProperties() {
        Properties ori = NSProperties._getProperties();
        Properties converted = new Properties();
        for (Enumeration e = ori.propertyNames(); e.hasMoreElements();) {
        	String key = (String) e.nextElement();
        	if(key != null && key.length() > 0) {
        		String value = getProperty(key);
        		converted.put(key, value);
        	}
        }
        return converted;
    }

    public static void updateProperties() {
    	Properties ori = NSProperties._getProperties();
    	for (Enumeration e = ori.propertyNames(); e.hasMoreElements();) {
    		String key = (String) e.nextElement();
    		if(key != null && key.length() > 0) {
    			String value = getProperty(key);
    			ori.put(key, value);
    		}
    	}
    }
}
