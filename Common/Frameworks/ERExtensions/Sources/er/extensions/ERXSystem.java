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
 * ERXSystem provides support for variable replacement in Properties with
 * 
 * @@key@@ references in values. Additionally, it provides an NSKVC
 *         implementation on top of System properties.
 * 
 * @author david teran
 */
public class ERXSystem implements NSKeyValueCoding, NSKeyValueCodingAdditions {
	/**
	 * The singleton instance to share.
	 */
	private static ERXSystem sharedInstance = new ERXSystem();

	/**
	 * Constructs an ERXSystem
	 */
	private ERXSystem() {
		super();
	}

	/**
	 * Looks up the given key in the ERXSystem properties, converts any property
	 * variables, and returns the converted value.
	 * 
	 * @param key
	 *            the key to lookup
	 * @return the converted value
	 */
	public static String getProperty(String key) {
		String originalValue = (String) ERXSystem.sharedInstance.valueForKey(key);

		String convertedValue = originalValue;
		if (originalValue == null || originalValue.indexOf("@@") == -1) {
			return originalValue;
		}

		String lastConvertedValue = null;
		while (convertedValue != lastConvertedValue && convertedValue.indexOf("@@") > -1) {
			lastConvertedValue = convertedValue;
			convertedValue = new ERXSimpleTemplateParser("ERXSystem:KEY_NOT_FOUND").parseTemplateWithObject(convertedValue, "@@", ERXSystem.sharedInstance, WOApplication.application());
		}

		// MS: Should we warn here? This is awfully quiet ...
		if (convertedValue.indexOf("ERXSystem:KEY_NOT_FOUND") > -1) {
			return originalValue; // not all keys are present
		}

		return convertedValue;
	}

	/**
	 * Retrieves the value of the given key from the ERXSystem properties store,
	 * return defaultValue if the key does not exist.
	 * 
	 * @param key
	 *            the key to lookup
	 * @param defaultValue
	 *            the default value to return
	 * @return the corresponding value or defaultValue if the key doesn't exist
	 */
	public static String getProperty(String key, String defaultValue) {
		String value = ERXSystem.getProperty(key);
		return value == null ? defaultValue : value;
	}

	/**
	 * Converts the property names defined in originalProperties with the
	 * ERXSystem.getProperty(..) method and puts the resulting values into the
	 * destinationProperties.
	 * 
	 * @param originalProperties
	 *            the properties to convert
	 * @param destinationProperties
	 *            the properties to copy into
	 */
	public static void convertProperties(Properties originalProperties, Properties destinationProperties) {
		for (Enumeration e = originalProperties.propertyNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (key != null && key.length() > 0) {
				String value = ERXSystem.getProperty(key);
				destinationProperties.put(key, value);
			}
		}
	}

	/**
	 * Returns a copy of NSProperties._getProperties() that has been passed
	 * variable evaluation.
	 * 
	 * @return a converted copy of NSProperties._getProperties()
	 */
	public static Properties getProperties() {
		Properties convertedProperties = new Properties();
		ERXSystem.convertProperties(NSProperties._getProperties(), convertedProperties);
		return convertedProperties;
	}

	/**
	 * Converts and evaluates the properties from NSProperties._getProperties() and replaces
	 * the converted values in-place.
	 */
	public static void updateProperties() {
		Properties originalProperties = NSProperties._getProperties();
		ERXSystem.convertProperties(originalProperties, originalProperties);
		ERXProperties.evaluatePropertyOperators(originalProperties, originalProperties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.webobjects.foundation.NSKeyValueCoding#valueForKey(java.lang.String)
	 */
	public Object valueForKey(String key) {
		return NSProperties.getProperty(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.webobjects.foundation.NSKeyValueCoding#takeValueForKey(java.lang.Object,
	 *      java.lang.String)
	 */
	public void takeValueForKey(Object value, String key) {
		throw new RuntimeException("not implemented");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.webobjects.foundation.NSKeyValueCodingAdditions#valueForKeyPath(java.lang.String)
	 */
	public Object valueForKeyPath(String key) {
		return NSProperties.getProperty(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.webobjects.foundation.NSKeyValueCodingAdditions#takeValueForKeyPath(java.lang.Object,
	 *      java.lang.String)
	 */
	public void takeValueForKeyPath(Object value, String key) {
		throw new RuntimeException("not implemented");
	}
}
