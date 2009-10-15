package er.extensions.qualifiers;

import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSSelector;

import er.extensions.ERXStringUtilities;

/**
 * ERXQuicksilverQualifier is a KeyValueQualifier that compares values with
 * ERXStringUtilities.quicksilverContains.  This only works as an in-memory
 * qualifier and DOES NOT WORK if you try to use it with EOF.
 * 
 * @author mschrag
 */
public class ERXQuicksilverQualifier extends ERXKeyValueQualifier {
	public ERXQuicksilverQualifier(String key, Object value) {
		super(key, new NSSelector("quicksilverContains:"), value);
	}

	@Override
	public boolean evaluateWithObject(Object object) {
		String stringValue = (String) NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, key());
		return ERXStringUtilities.quicksilverContains(stringValue, (String) value());
	}
}
