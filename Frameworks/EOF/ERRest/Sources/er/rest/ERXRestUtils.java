package er.rest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXValueUtilities;

/**
 * Miscellaneous rest-related utility methods.
 * 
 * @author mschrag
 */
public class ERXRestUtils {
	// MS: Yes, this is wrong, but I'll fix it later ...
	public static EOEntity getEntityNamed(ERXRestContext context, String name) {
		EOEntity e = ERXEOAccessUtilities.entityNamed(context.editingContext(), name);
		if (e == null) {
			throw new RuntimeException("Could not find entity named '" + name + "'");
		}
		return e;
	}

	/**
	 * Returns a String form of the given object using the given delegate.
	 * 
	 * @param context
	 *            the context to write within
	 * @param writer
	 *            the writer to write with
	 * @param value
	 *            the value to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(ERXRestContext context, IERXRestResponseWriter writer, EOEnterpriseObject value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		ERXStringBufferResponseWriter responseWriter = new ERXStringBufferResponseWriter();
		if (value != null) {
			writer.appendToResponse(context, responseWriter, new ERXRestKey(context, ERXEOAccessUtilities.entityForEo(value), null, value));
		}
		return responseWriter.toString();
	}

	/**
	 * Returns a String form of the given objects using the given delegate.
	 * 
	 * @param context
	 *            the context to write within
	 * @param writer
	 *            the writer to write with
	 * @param values
	 *            the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(ERXRestContext context, IERXRestResponseWriter writer, EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		ERXStringBufferResponseWriter responseWriter = new ERXStringBufferResponseWriter();
		writer.appendToResponse(context, responseWriter, new ERXRestKey(context, entity, null, values));
		return responseWriter.toString();
	}

	/**
	 * Returns a String form of the given object using the unsafe delegate.
	 * 
	 * @param writer
	 *            the writer to write with
	 * @param value
	 *            the value to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(IERXRestResponseWriter writer, EOEnterpriseObject value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), writer, value);
	}

	/**
	 * Returns a String form of the given objects using the unsafe delegate.
	 * 
	 * @param writer
	 *            the writer to write with
	 * @param values
	 *            the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(IERXRestResponseWriter writer, EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), writer, entity, values);
	}

	public static boolean isPrimitive(Object obj) {
		return obj == null || (obj instanceof Class) ? ERXRestUtils.isPrimitive((Class) obj) : ERXRestUtils.isPrimitive(obj.getClass());
	}

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
	 * Returns the given object coerced into the desired value as defined by the value type of the given route key --
	 * this should be merged with the method below ...
	 * 
	 * @param obj
	 *            the object to convert
	 * @param valueType
	 *            the destination type to convert to
	 * @param editingContext
	 *            the editing context to fault EO's in from (or null to not fault EO's)
	 * @return the coerced value
	 */
	public static Object coerceValueType(Object obj, String valueType, EOEditingContext editingContext) {
		Object value;
		if (ERXValueUtilities.isNull(obj)) {
			value = null;
		}
		else if ("String".equals(valueType) || java.lang.String.class.getName().equals(valueType)) {
			value = obj.toString();
		}
		else if ("Boolean".equals(valueType) || java.lang.Boolean.class.getName().equals(valueType)) {
			value = ERXValueUtilities.BooleanValueWithDefault(obj, null);
		}
		// else if ("Byte".equals(valueType) || java.lang.Byte.class.getName().equals(valueType)) {
		// value = ERXValueUtilities.ByteValueWithDefault(obj, null);
		// }
		// else if ("Short".equals(valueType) || java.lang.Short.class.getName().equals(valueType)) {
		// value = ERXValueUtilities.ShortValueWithDefault(obj, null);
		// }
		else if ("Integer".equals(valueType) || java.lang.Integer.class.getName().equals(valueType)) {
			value = ERXValueUtilities.IntegerValueWithDefault(obj, null);
		}
		else if ("Long".equals(valueType) || java.lang.Long.class.getName().equals(valueType)) {
			value = ERXValueUtilities.LongValueWithDefault(obj, null);
		}
		else if ("Float".equals(valueType) || java.lang.Float.class.getName().equals(valueType)) {
			value = ERXValueUtilities.FloatValueWithDefault(obj, null);
		}
		else if ("Double".equals(valueType) || java.lang.Double.class.getName().equals(valueType)) {
			value = ERXValueUtilities.DoubleValueWithDefault(obj, null);
		}
		else if ("BigDecimal".equals(valueType) || java.math.BigDecimal.class.getName().equals(valueType)) {
			value = ERXValueUtilities.DoubleValueWithDefault(obj, null);
		}
		else if ("Date".equals(valueType) || "NSTimestamp".equals(valueType) || Date.class.getName().equals(valueType) || NSTimestamp.class.getName().equals(valueType)) {
			if (obj instanceof Date) {
				value = obj;
			}
			else {
				// MS: the copy-and-pasteness of this whole method disgusts me ... I'll resolve it, just give me a little bit
				String strValue = (String)obj;
				NSTimestampFormatter formatter = null;
				try {
					if (strValue.indexOf(' ') == -1) {
						formatter = new NSTimestampFormatter("%Y-%m-%dT%H:%M:%SZ");
					}
					else {
						formatter = new NSTimestampFormatter();
					}
					value = formatter.parseObject(strValue);
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
		else if (editingContext != null) {
			EOEntity entity = ERXEOAccessUtilities.entityNamed(editingContext, valueType);
			if (entity != null) {
				Object pkValue = ((EOAttribute) entity.primaryKeyAttributes().objectAtIndex(0)).validateValue(obj);
				value = ERXEOControlUtilities.objectWithPrimaryKeyValue(editingContext, entity.name(), pkValue, null, false);
			}
			else {
				throw new IllegalArgumentException("Unknown value type '" + valueType + "'.");
			}
		}
		else {
			value = obj;
		}
		return value;
	}

	/**
	 * Parses the given String and returns an object.
	 * 
	 * @param entity
	 *            the entity
	 * @param object
	 *            the object
	 * @param attributeName
	 *            the name of the property
	 * @param attributeValue
	 *            the value of the property
	 * @return a parsed version of the String
	 * @throws ParseException
	 *             if a parse failure occurs
	 * @throws ERXRestException
	 *             if a general failure occurs
	 */
	public static Object coerceValueType(EOEntity entity, Object object, String attributeName, String attributeValue) throws ParseException, ERXRestException {
		try {
			NSKeyValueCoding._KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(object, attributeName);
			Class valueType = binding.valueType();

			Object parsedValue;
			if (attributeValue == null || attributeValue.length() == 0) {
				if (entity != null) {
					EOAttribute attribute = entity.attributeNamed(attributeName);
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
				if (String.class.isAssignableFrom(valueType)) {
					parsedValue = attributeValue;
				}
				else if (Boolean.class.isAssignableFrom(valueType)) {
					parsedValue = Boolean.valueOf(attributeValue);
				}
				else if (Character.class.isAssignableFrom(valueType)) {
					parsedValue = new Character(attributeValue.charAt(0));
				}
				else if (Byte.class.isAssignableFrom(valueType)) {
					parsedValue = Byte.valueOf(attributeValue);
				}
				else if (BigDecimal.class.isAssignableFrom(valueType)) {
					parsedValue = new BigDecimal(attributeValue);
				}
				else if (Integer.class.isAssignableFrom(valueType)) {
					parsedValue = Integer.valueOf(attributeValue);
				}
				else if (Short.class.isAssignableFrom(valueType)) {
					parsedValue = Short.valueOf(attributeValue);
				}
				else if (Long.class.isAssignableFrom(valueType)) {
					parsedValue = Long.valueOf(attributeValue);
				}
				else if (Float.class.isAssignableFrom(valueType)) {
					parsedValue = Float.valueOf(attributeValue);
				}
				else if (Double.class.isAssignableFrom(valueType)) {
					parsedValue = Double.valueOf(attributeValue);
				}
				else if (NSTimestamp.class.isAssignableFrom(valueType)) {
					NSTimestampFormatter formatter = null;
					try {
						if (attributeValue.indexOf(' ') == -1) {
							formatter = new NSTimestampFormatter("%Y-%m-%dT%H:%M:%SZ");
						}
						else {
							formatter = new NSTimestampFormatter();
						}
						parsedValue = formatter.parseObject(attributeValue);
					}
					catch (Throwable t) {
						String msg = "Failed to parse '" + attributeValue + "' as a timestamp";
						if (formatter != null) {
							msg += " (example: " + formatter.format(new NSTimestamp()) + ")";
						}
						msg += ".";
						throw new ERXRestException(msg);
					}
				}
				else if (Enum.class.isAssignableFrom(valueType)) {
					try {
						parsedValue = valueType.getMethod("valueOf", String.class).invoke(null, attributeValue);
					}
					catch (Throwable e) {
						throw new ERXRestException("Failed to parse " + attributeValue + " as enum member of " + valueType);
					}
				}
				else {
					throw new ERXRestException("Unable to parse the value '" + attributeValue + "' into a " + valueType.getName() + ".");
				}
			}
			return parsedValue;
		}
		catch (Throwable e) {
			throw new ERXRestException("Failed to parse attribute " + attributeName + " for entity " + ((entity == null) ? "unknown" : entity.name()), e);
		}
	}
}
