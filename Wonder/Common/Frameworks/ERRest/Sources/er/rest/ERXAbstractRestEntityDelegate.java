package er.rest;

import java.math.BigDecimal;
import java.text.ParseException;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.ERXGuardedObjectInterface;

public abstract class ERXAbstractRestEntityDelegate implements IERXRestEntityDelegate {
	public Object valueForKey(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return NSKeyValueCoding.Utility.valueForKey(obj, propertyName);
	}

	public void takeValueForKey(EOEntity entity, Object obj, String propertyName, String value, ERXRestContext context) throws ParseException, ERXRestException {
		Object parsedAttributeValue = parseAttributeValue(entity, obj, propertyName, value);
		EOKeyValueCoding.Utility.takeStoredValueForKey(obj, parsedAttributeValue, propertyName);
	}

	public void delete(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		if (eo instanceof ERXGuardedObjectInterface) {
			((ERXGuardedObjectInterface) eo).delete();
		}
		else {
			eo.editingContext().deleteObject(eo);
		}
	}

	public String formatAttributeValue(EOEntity entity, Object object, String attributeName, Object attributeValue) throws ParseException, ERXRestException {
		String formattedValue;
		if (attributeValue == null) {
			formattedValue = null;
		}
		else {
			formattedValue = attributeValue.toString();
		}
		return formattedValue;
	}
	
	public Object parseAttributeValue(EOEntity entity, Object object, String attributeName, String attributeValue) throws ParseException, ERXRestException {
		NSKeyValueCoding._KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(object, attributeName);
		Class valueType = binding.valueType();

		Object parsedValue;
		if (attributeValue == null || attributeValue.length() == 0) {
			EOAttribute attribute = entity.attributeNamed(attributeName);
			if (attribute != null && !attribute.allowsNull() && String.class.isAssignableFrom(valueType)) {
				parsedValue = "";
			}
			else {
				parsedValue = EOKeyValueCoding.NullValue;
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
				parsedValue = new NSTimestampFormatter().parseObject(attributeValue);
			}
			else {
				throw new ERXRestException("Unable to parse the value '" + attributeValue + "' into a " + valueType.getName() + ".");
			}
		}
		return parsedValue;
	}

}
