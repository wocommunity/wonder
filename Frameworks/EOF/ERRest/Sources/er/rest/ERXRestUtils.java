package er.rest;

import java.math.BigDecimal;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;
import com.webobjects.foundation._NSUtilities;

import er.extensions.crypting.ERXCryptoString;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Miscellaneous rest-related utility methods.
 * 
 * @property er.rest.dateFormat
 * @property er.rest.timestampFormat
 * @property er.rest.rfcDateFormat (default "rfc822")
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
		return obj == null || ((obj instanceof Class) ? ERXRestUtils.isPrimitive((Class<?>) obj) : ERXRestUtils.isPrimitive(obj.getClass()));
	}

	/**
	 * Returns whether or not the given class represents a primitive in REST.
	 * 
	 * @param valueType the class to check
	 * @return whether or not the given class represents a primitive in REST
	 */
	public static boolean isPrimitive(Class<?> valueType) {
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
		else if (LocalDateTime.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (LocalDate.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (Enum.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (NSKeyValueCoding.Null.class.isAssignableFrom(valueType)) {
			primitive = true;
		}
		else if (ERXCryptoString.class.isAssignableFrom(valueType)) {
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
	public static String coerceValueToString(Object value, ERXRestContext context) {
		String formattedValue;
		if (value == null || value instanceof NSKeyValueCoding.Null) {
			formattedValue = null;
		}
		else if (value instanceof NSTimestamp) {
			NSTimestamp timestamp = (NSTimestamp) value;
			String rfcFormat = ERXProperties.stringForKeyWithDefault("er.rest.rfcDateFormat", "rfc822");
			if ("rfc3339".equals(rfcFormat)) {
				formattedValue = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date(timestamp.getTime()));
				formattedValue = formattedValue.substring(0, formattedValue.length()-2) + ":" + formattedValue.substring(formattedValue.length()-2);  				
			} else {
				formattedValue = ERXRestUtils.timestampFormat(false, context).format(timestamp);
			}
		}
		else if (value instanceof Date) {
			formattedValue = ERXRestUtils.dateFormat(false, context).format(value);
		}
		else if (value instanceof LocalDateTime) {
			formattedValue = ERXRestUtils.jodaLocalDateTimeFormat(false, context).print((LocalDateTime)value);
		}
		else if (value instanceof LocalDate) {
			formattedValue = ERXRestUtils.jodaLocalDateFormat(false, context).print((LocalDate)value);
		}
		else if (value instanceof NSData && ((NSData)value).length() == 24) {
			formattedValue = NSPropertyListSerialization.stringFromPropertyList(value);
		}
		else {
			formattedValue = value.toString();
		}
		return formattedValue;

	}

	// this "spaces" attribute is stupid, i know ... this whole api is stupid.  it's a quick hack for now to accommodate someone very near and dear to my heart ... yes i'm talking to you.
	protected static Format timestampFormat(boolean spaces, ERXRestContext context) {
		Format timestampFormatter = (Format)context.userInfoForKey("er.rest.timestampFormatter");
		if (timestampFormatter == null) {
			String timestampFormat = (String)context.userInfoForKey("er.rest.timestampFormat");
			if (timestampFormat == null) {
				timestampFormat = ERXProperties.stringForKey("er.rest.timestampFormat");
				if (timestampFormat == null) {
					if (spaces) {
						timestampFormat = ERXProperties.stringForKeyWithDefault("er.rest.timestampFormat.secondary", "%Y-%m-%d %H:%M:%S %Z");
					}
					else {
						timestampFormat = ERXProperties.stringForKeyWithDefault("er.rest.timestampFormat.primary", "%Y-%m-%dT%H:%M:%SZ");
					}
				}
			}
			timestampFormatter = new NSTimestampFormatter(timestampFormat);
		}
		return timestampFormatter;
	}

	protected static Format dateFormat(boolean spaces, ERXRestContext context) {
		Format dateFormatter = (Format)context.userInfoForKey("er.rest.dateFormatter");
		if (dateFormatter == null) {
			String dateFormat = (String)context.userInfoForKey("er.rest.dateFormat");
			if (dateFormat == null) {
				dateFormat = ERXProperties.stringForKey("er.rest.dateFormat");
				if (dateFormat == null) {
					if (spaces) {
						dateFormat = ERXProperties.stringForKeyWithDefault("er.rest.dateFormat.secondary", "yyyy-MM-dd HH:mm:ss z");
					}
					else {
						dateFormat = ERXProperties.stringForKeyWithDefault("er.rest.dateFormat.primary", "yyyy-MM-dd'T'HH:mm:ss'Z'");
					}
				}
			}
			dateFormatter = new SimpleDateFormat(dateFormat);
		}
		return dateFormatter;
	}
	
	protected static DateTimeFormatter jodaLocalDateFormat(boolean spaces, ERXRestContext context) {
		DateTimeFormatter dateFormatter = (DateTimeFormatter)context.userInfoForKey("er.rest.jodaFormatter");
		if (dateFormatter == null) {
			String dateFormat = (String)context.userInfoForKey("er.rest.jodaFormat");
			if (dateFormat == null) {
				dateFormat = ERXProperties.stringForKey("er.rest.jodaFormat");
				if (dateFormat == null) {
					if (spaces) {
						dateFormat = ERXProperties.stringForKeyWithDefault("er.rest.jodaFormat.secondary", "yyyy-MM-dd HH:mm:ss z");
					}
					else {
						dateFormat = ERXProperties.stringForKeyWithDefault("er.rest.jodaFormat.primary", "yyyy-MM-dd'T'HH:mm:ss'Z'");
					}
				}
			}
			dateFormatter = DateTimeFormat.forPattern(dateFormat);
		}
		return dateFormatter;
	}
	
	protected static DateTimeFormatter jodaLocalDateTimeFormat(boolean spaces, ERXRestContext context) {
		DateTimeFormatter dateFormatter = (DateTimeFormatter)context.userInfoForKey("er.rest.jodaTimeFormatter");
		if (dateFormatter == null) {
			String dateFormat = (String)context.userInfoForKey("er.rest.jodaFormatTime");
			if (dateFormat == null) {
				dateFormat = ERXProperties.stringForKey("er.rest.jodaFormatTime");
				if (dateFormat == null) {
					if (spaces) {
						dateFormat = ERXProperties.stringForKeyWithDefault("er.rest.jodaFormat.secondary", "yyyy-MM-dd HH:mm:ss z");
					}
					else {
						dateFormat = ERXProperties.stringForKeyWithDefault("er.rest.jodaFormat.primary", "yyyy-MM-dd'T'HH:mm:ss'Z'");
					}
				}
			}
			dateFormatter = DateTimeFormat.forPattern(dateFormat);
		}
		return dateFormatter;
	}
	
	@SuppressWarnings("unchecked")
	public static Object coerceValueToTypeNamed(Object value, String valueTypeName, ERXRestContext context, boolean resolveEntities) {
		Object parsedValue;
		Class<?> valueType = _NSUtilities.classWithName(valueTypeName);
		// test primitives first, since we can't return a null for them
		if (valueType != null && int.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.intValueWithDefault(value, 0);
		}
		else if (valueType != null && boolean.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.booleanValueWithDefault(value, false);
		}
		else if (valueType != null && char.class.isAssignableFrom(valueType)) {
			parsedValue = (char)ERXValueUtilities.intValueWithDefault(value, 0);
		}
		else if (valueType != null && byte.class.isAssignableFrom(valueType)) {
			parsedValue = (byte)ERXValueUtilities.intValueWithDefault(value, 0);
		}
		else if (valueType != null && long.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.longValueWithDefault(value, 0);
		}
		else if (valueType != null && float.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.floatValueWithDefault(value, 0);
		}
		else if (valueType != null && double.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.doubleValueWithDefault(value, 0);
		}
		else if (valueType != null && short.class.isAssignableFrom(valueType)) {
			parsedValue = (short)ERXValueUtilities.intValueWithDefault(value, 0);
		}
		else if (ERXValueUtilities.isNull(value)) {
			parsedValue = null;
		}
		else if (valueType != null && String.class.isAssignableFrom(valueType)) {
			parsedValue = String.valueOf(value);
		}
		else if (valueType != null && Boolean.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.BooleanValueWithDefault(value, null);
		}
		else if (valueType != null && Character.class.isAssignableFrom(valueType)) {
			parsedValue = Character.valueOf(((String) value).charAt(0)); // MS: Presumes String
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
		else if (valueType != null && NSData.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.dataValueWithDefault(value, null);
		}		
		else if (valueType != null && NSTimestamp.class.isAssignableFrom(valueType)) {
			if (value instanceof NSTimestamp) {
				parsedValue = value;
			}
			else {
				String strValue = (String) value;
				boolean spaces = strValue.indexOf(' ') != -1;
				String rfcFormat = ERXProperties.stringForKeyWithDefault("er.rest.rfcDateFormat", "rfc822");					
				if ("rfc3339".equals(rfcFormat)) {
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
					java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(.*[\\-,\\+]{1}[0-9]{1,2}):([0-9]{1,2})");
					java.util.regex.Matcher matcher = pattern.matcher(strValue);
					if (matcher.matches()) {
						try {
							strValue = matcher.group(1) + matcher.group(2);
							parsedValue = formatter.parseObject(strValue);
							if (parsedValue instanceof java.util.Date) {
								parsedValue = new NSTimestamp((Date)parsedValue);
							} 
						} catch (Throwable t) {
							String msg = "Failed to parse '" + strValue + "' as a timestamp";
							if (formatter != null) {
								msg += " (example: " + formatter.format(new NSTimestamp()) + ")";
							}
							msg += ".";
							throw new IllegalArgumentException(msg, t);
						}
					} else {
						strValue  = null;
						throw new IllegalArgumentException(strValue + " didn't match the " + pattern.pattern() + " pattern", new Throwable());
					}
				} else {
					Format formatter = null;
					try {
						formatter = ERXRestUtils.timestampFormat(spaces, context);
						parsedValue = formatter.parseObject(strValue);		
					}
					catch (Throwable t) {
						String msg = "Failed to parse '" + strValue + "' as a timestamp";
						if (formatter != null) {
							msg += " (example: " + formatter.format(new NSTimestamp()) + ")";
						}
						msg += ".";
						throw new IllegalArgumentException(msg, t);
					}
				}
			}
		}
		else if (valueType != null && Date.class.isAssignableFrom(valueType)) {
			if (value instanceof NSTimestamp) {
				parsedValue = value;
			}
			else {
				String strValue = (String) value;
				Format formatter = null;
				try {
					boolean spaces = strValue.indexOf(' ') != -1;
					formatter = ERXRestUtils.dateFormat(spaces, context);
					parsedValue = formatter.parseObject(strValue);
				}
				catch (Throwable t) {
					String msg = "Failed to parse '" + strValue + "' as a timestamp";
					if (formatter != null) {
						msg += " (example: " + formatter.format(new Date()) + ")";
					}
					msg += ".";
					throw new IllegalArgumentException(msg, t);
				}
			}
		}
		else if (valueType != null && LocalDateTime.class.isAssignableFrom(valueType)) {
			if (value instanceof NSTimestamp) {
				parsedValue = value;
			}
			else {
				String strValue = (String) value;
				DateTimeFormatter formatter = null;
				try {
					boolean spaces = strValue.indexOf(' ') != -1;
					formatter = ERXRestUtils.jodaLocalDateTimeFormat(spaces, context);
					parsedValue = new LocalDateTime(formatter.parseDateTime(strValue));
				}
				catch (Throwable t) {
					String msg = "Failed to parse '" + strValue + "' as a timestamp";
					if (formatter != null) {
						msg += " (example: " + formatter.print(new LocalDateTime()) + ")";
					}
					msg += ".";
					throw new IllegalArgumentException(msg, t);
				}
			}
		}
		else if (valueType != null && LocalDate.class.isAssignableFrom(valueType)) {
			if (value instanceof NSTimestamp) {
				parsedValue = value;
			}
			else {
				String strValue = (String) value;
				DateTimeFormatter formatter = null;
				try {
					boolean spaces = strValue.indexOf(' ') != -1;
					formatter = ERXRestUtils.jodaLocalDateFormat(spaces, context);
					parsedValue = new LocalDate(formatter.parseDateTime(strValue));
				}
				catch (Throwable t) {
					String msg = "Failed to parse '" + strValue + "' as a timestamp";
					if (formatter != null) {
						msg += " (example: " + formatter.print(new LocalDate()) + ")";
					}
					msg += ".";
					throw new IllegalArgumentException(msg, t);
				}
			}
		}
		else if (valueType != null && Enum.class.isAssignableFrom(valueType)) {
			parsedValue = ERXValueUtilities.enumValueWithDefault(value, (Class<? extends Enum>) valueType, null);
		}
		else if (valueType != null && ERXCryptoString.class.isAssignableFrom(valueType)) {
			parsedValue = new ERXCryptoString(value.toString());
		}
		else if (resolveEntities) {
			EOClassDescription entity = ERXRestClassDescriptionFactory.classDescriptionForEntityName(valueTypeName);
			if (entity != null) {
			  parsedValue = IERXRestDelegate.Factory.delegateForClassDescription(entity).objectOfEntityWithID(entity, value, context);
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
	public static Object coerceValueToAttributeType(Object value, EOClassDescription parentEntity, Object parentObject, String attributeName, ERXRestContext context) {
		NSKeyValueCoding._KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(parentObject, attributeName);
		Class<?> valueType = binding.valueType();
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
			else if (valueType == Object.class) {
				parsedValue = value;
			}
			else {
				parsedValue = ERXRestUtils.coerceValueToTypeNamed(value, valueType.getName(), context, false);
			}
			return parsedValue;
		}
		catch (Throwable e) {
			throw new IllegalArgumentException("Failed to parse attribute " + attributeName + " for entity " + ((parentEntity == null) ? "unknown" : parentEntity.entityName()), e);
		}
	}
}
