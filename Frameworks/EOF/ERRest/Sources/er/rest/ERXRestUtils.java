package er.rest;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.foundation._NSUtilities;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Miscellaneous rest-related utility methods.
 * 
 * @author mschrag
 */
public class ERXRestUtils {
	/**
	 * Returns whether or not the given object represents a primitive in REST.
	 * 
	 * @param obj the object to check
	 * @return whether or not the given object represents a primitive in REST
	 */
	public static boolean isPrimitive(Object obj) {
		return obj == null || ((obj instanceof Class) ? ERXRestUtils.isPrimitive((Class) obj) : ERXRestUtils.isPrimitive(obj.getClass()));
	}

	/**
	 * Returns whether or not the given class represents a primitive in REST.
	 * 
	 * @param valueType the class to check
	 * @return whether or not the given class represents a primitive in REST
	 */
	public static boolean isPrimitive(Class valueType) {
		boolean primitive = false;
		if (String.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Boolean.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Character.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Byte.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (BigDecimal.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Integer.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Short.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Long.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Float.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Double.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Date.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Calendar.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Enum.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		return primitive;
	}

	/**
	 * Convert the given object to a String (using REST formats).
	 * 
	 * @param value the value to convert
	 * @return the REST-formatted string
	 */
	public static String coerceValueToString(Object value) {
		String formattedValue;
		if (value == null) {
			formattedValue = null;
		}
		else if (value instanceof NSTimestamp) {
			NSTimestamp timestamp = (NSTimestamp) value;
			formattedValue = new NSTimestampFormatter(ERXRestUtils.timestampFormat()).format(timestamp);
		}
		else if (value instanceof Date) {
			Date date = (Date) value;
			formattedValue = new SimpleDateFormat(ERXRestUtils.dateFormat(false)).format(value);
		}
		else {
			formattedValue = value.toString();
		}
		return formattedValue;

	}

	protected static String timestampFormat() {
		return ERXProperties.stringForKeyWithDefault("er.rest.timestampFormat", "%Y-%m-%dT%H:%M:%SZ");
	}

	// this "spaces" attribtue is stupid, i know ... this whole api is stupid.  it's a quick hack for now
	protected static String dateFormat(boolean spaces) {
		if (spaces) {
			return ERXProperties.stringForKeyWithDefault("er.rest.dateFormat", "YYYY-MM-dd HH:mm:ss z");
		}
		else {
			return ERXProperties.stringForKeyWithDefault("er.rest.dateFormat", "YYYY-MM-dd\\THH:mm:ss\\Z");
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Object coerceValueToTypeNamed(Object value, String valueTypeName, IERXRestDelegate delegate) {
		Object parsedValue;
		Class<?> valueType = _NSUtilities.classWithName(valueTypeName);
		if (ERXValueUtilities.isNull(value)) {
			parsedValue = null;
		}
		else if (valueType != null && String.class.isAssignableFrom(valueType)) {
			parsedValue = String.valueOf(value);
		}
		else if (valueType != null && Boolean.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.BooleanValueWithDefault(value, null);
		}
		else if (valueType != null && Character.class.isAssignableFrom(valueType)) {
			parsedValue = new Character(((String) value).charAt(0)); // MS: Presumes String
		}
		else if (valueType != null && Byte.class.isAssignableFrom(valueType)) {
			parsedValue = Byte.valueOf((String) value); // MS: Presumes String
		}
		else if (valueType != null && BigDecimal.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.DoubleValueWithDefault(value, null);
		}
		else if (valueType != null && Integer.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.IntegerValueWithDefault(value, null);
		}
		else if (valueType != null && Short.class.isAssignableFrom(valueType)) {
			parsedValue = Short.valueOf((String) value); // MS: Presumes String
		}
		else if (valueType != null && Long.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.LongValueWithDefault(value, null);
		}
		else if (valueType != null && Float.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.FloatValueWithDefault(value, null);
		}
		else if (valueType != null && Double.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.DoubleValueWithDefault(value, null);
		}
		else if (valueType != null && NSTimestamp.class.isAssignableFrom(valueType)) {
			if (value instanceof NSTimestamp) {
				parsedValue = value;
			}
			else {
				String strValue = (String) value;
				NSTimestampFormatter formatter = null;
				try {
					if (strValue.indexOf(' ') == -1) {
						formatter = new NSTimestampFormatter(ERXRestUtils.timestampFormat());
					}
					else {
						formatter = new NSTimestampFormatter();
					}
					parsedValue = formatter.parseObject(strValue);
				}
				catch (Throwable t) {
					String msg = "Failed to parse '" + strValue + "' as a timestamp";
					if (formatter != null) {
						msg += " (example: " + formatter.format(new NSTimestamp()) + ")";
					}
					msg += ".";
					throw new IllegalArgumentException(msg);
				}
			}
		}
		else if (valueType != null && Date.class.isAssignableFrom(valueType)) {
			if (value instanceof NSTimestamp) {
				parsedValue = value;
			}
			else {
				String strValue = (String) value;
				SimpleDateFormat formatter = null;
				try {
					if (strValue.indexOf(' ') == -1) {
						formatter = new SimpleDateFormat(ERXRestUtils.dateFormat(false));
					}
					else {
						formatter = new SimpleDateFormat(ERXRestUtils.dateFormat(true));
					}
					parsedValue = formatter.parseObject(strValue);
				}
				catch (Throwable t) {
					String msg = "Failed to parse '" + strValue + "' as a timestamp";
					if (formatter != null) {
						msg += " (example: " + formatter.format(new Date()) + ")";
					}
					msg += ".";
					throw new IllegalArgumentException(msg);
				}
			}
		}
		else if (valueType != null && Enum.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.enumValueWithDefault(value, (Class<? extends Enum>) valueType, null);
		}
		else if (delegate != null) {
			EOClassDescription entity = ERXRestClassDescriptionFactory.classDescriptionForEntityName(valueTypeName);
			if (entity != null) {
			  parsedValue = delegate.objectOfEntityWithID(entity, value);
			}
			else {
				throw new IllegalArgumentException("Unknown value type '" + valueTypeName + "'.");
			}
		}
		else {
			throw new IllegalArgumentException("Unable to parse the value '" + value + "' into a '" + valueTypeName + "'.");
		}
		return parsedValue;
	}

	/**
	 * Parses the given String and returns an object.
	 * 
	 * @param value
	 *            the value of the attribute
	 * @param parentEntity
	 *            the entity
	 * @param attributeName
	 *            the name of the property
	 * @param parentObject
	 *            the parent object
	 * @return a parsed version of the String
	 */
	public static Object coerceValueToAttributeType(Object value, EOClassDescription parentEntity, Object parentObject, String attributeName) {
		NSKeyValueCoding._KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(parentObject, attributeName);
		Class valueType = binding.valueType();

		try {
			Object parsedValue;
			if (value == null || ERXValueUtilities.isNull(value) || (value instanceof String && ((String) value).length() == 0)) {
				if (parentEntity != null) {
					if (parentEntity instanceof EOEntityClassDescription) {
						EOAttribute attribute = ((EOEntityClassDescription)parentEntity).entity().attributeNamed(attributeName);
						if (attribute != null && !attribute.allowsNull() && String.class.isAssignableFrom(valueType)) {
							parsedValue = "";
						}
						else {
							parsedValue = EOKeyValueCoding.NullValue;
						}
					}
					else {
						parsedValue = NSKeyValueCoding.NullValue;
					}
				}
				else {
					parsedValue = NSKeyValueCoding.NullValue;
				}
			}
			else {
				parsedValue = ERXRestUtils.coerceValueToTypeNamed(value, valueType.getName(), null);
			}
			return parsedValue;
		}
		catch (Throwable e) {
			throw new IllegalArgumentException("Failed to parse attribute " + attributeName + " for entity " + ((parentEntity == null) ? "unknown" : parentEntity.entityName()), e);
		}
	}
}
