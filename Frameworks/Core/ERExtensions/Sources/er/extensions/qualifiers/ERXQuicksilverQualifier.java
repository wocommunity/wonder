package er.extensions.qualifiers;

import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSSelector;

import er.extensions.foundation.ERXStringUtilities;

/**
 * ERXQuicksilverQualifier is a KeyValueQualifier that compares values with
 * ERXStringUtilities.quicksilverContains.  This only works as an in-memory
 * qualifier and DOES NOT WORK if you try to use it with EOF.
 * 
 * @author mschrag
 */
public class ERXQuicksilverQualifier extends ERXKeyValueQualifier {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERXQuicksilverQualifier(String key, Object value) {
		super(key, new NSSelector("quicksilverContains:"), value);
	}

	@Override
	public boolean evaluateWithObject(Object object) {
		String stringValue = (String) NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, key());
		return ERXStringUtilities.quicksilverContains(stringValue, (String) value());
	}
}
