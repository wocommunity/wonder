package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierVariable;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSStringUtilities;

import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXProperties;

/**
 * ERXKeyValueQualifier is a chainable extension of EOKeyValueQualifier.
 * 
 * @author mschrag
 */
public class ERXKeyValueQualifier extends EOKeyValueQualifier implements IERXChainableQualifier {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
	
	// Lazy static initialization
	private static class PROPERTIES {
		static boolean shouldFlattenValueObject = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXKeyValueQualifier.Contains.flatten", true);
	}

	public ERXKeyValueQualifier(String key, NSSelector selector, Object value) {
		super(key, selector, value);
		if (key == null) {
			throw new IllegalArgumentException("A KeyQualifierQualifier must have a key.");
		}
		if (selector == null) {
			throw new IllegalArgumentException("A KeyQualifierQualifier must have a selector.");
		}
	}

	public ERXAndQualifier and(EOQualifier... qualifiers) {
		return ERXChainedQualifierUtils.and(this, qualifiers);
	}

	public ERXNotQualifier not() {
		return ERXQ.not(this);
	}

	public ERXOrQualifier or(EOQualifier... qualifiers) {
		return ERXChainedQualifierUtils.or(this, qualifiers);
	}

	public void filter(NSMutableArray<?> array) {
		ERXQ.filter(array, this);
	}

	public <T> NSArray<T> filtered(NSArray<T> array) {
		return ERXQ.filtered(array, this);
	}

	public <T> T first(NSArray<T> array) {
		return ERXQ.first(array, this);
	}

	public <T> T one(NSArray<T> array) {
		return ERXQ.one(array, this);
	}

	public <T> T requiredOne(NSArray<T> array) {
		return ERXQ.requiredOne(array, this);
	}
	
	/**
	 * Overridden to handle case of in-memory evaluation of QualifierOperatorContains selector and a keyPath that has multiple toMany and/or manyToMany-flattened relationships resulting in arrays of arrays rather than
	 * an array of discrete objects. In that case the object is evaluated against a flattened array which gives the same result as SQL evaluation.
	 * 
	 * Since legacy code may depend on workarounds to the incorrect behavior, this patch can be disabled by setting the property <code>er.extensions.ERXKeyValueQualifier.Contains.flatten</code> to <code>false</code>
	 */
	@Override
	public boolean evaluateWithObject(Object object) {
		Object objectValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, _key);

		if (_value instanceof EOQualifierVariable) {
			throw new IllegalStateException("Error evaluating qualifier with key " + _key + ", selector " + _selector + ", value " + _value + " - value must be substitued for variable before evaluating");
		}

		if (_selector.equals(EOQualifier.QualifierOperatorCaseInsensitiveLike)) {
			if (_lowercaseCache == null) {
				_lowercaseCache = (_value != NSKeyValueCoding.NullValue) ? (_value.toString()).toLowerCase() : "";
			}
			return _NSStringUtilities.stringMatchesPattern(((objectValue != null) && (objectValue != NSKeyValueCoding.NullValue)) ? objectValue.toString() : "", _lowercaseCache, true);
		}
		
		// Flatten in case we have array of arrays
		if (_selector.equals(EOQualifier.QualifierOperatorContains) && PROPERTIES.shouldFlattenValueObject && objectValue != null && objectValue instanceof NSArray) {
			objectValue = ERXArrayUtilities.flatten((NSArray<?>) objectValue);
		}
		return ComparisonSupport.compareValues((objectValue != null) ? objectValue : NSKeyValueCoding.NullValue, _value, _selector);
	}
}
