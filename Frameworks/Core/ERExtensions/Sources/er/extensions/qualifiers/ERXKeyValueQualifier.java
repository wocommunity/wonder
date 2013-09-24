package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierVariable;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
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
		static boolean enableArrayElementComparison = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXKeyValueQualifier.arrayElementComparison", true);
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
	 *
	 * Legacy WebObjects code is not handling comparison of NSArray with _value except over ualifierOperatorContains selector correctly. This is a fix for legacy code to correctly filter a NSArray with _value.
	 * This implementation can be disabled by setting the property <code>er.extensions.ERXKeyValueQualifier.arrayElementComparison</code> to <code>false</code>
	 * 
	 */
	@Override
	public boolean evaluateWithObject(Object object) {
		boolean retVal = false;

		if (_value instanceof EOQualifierVariable) {
			throw new IllegalStateException(new StringBuffer("Error evaluating qualifier with key ").append(_key).append(", selector ").append(_selector).append(", value ").append(_value).append(" - value must be substitued for variable before evaluating").toString());
		}

		if(object == null){
			throw new IllegalArgumentException(new StringBuffer("Error evaluating qualifier with key ").append(_key).append(", selector ").append(_selector).append(", value ").append(_value).append(" - object cannot be null").toString());
		}

		Object objectValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(object, _key);
		// Flatten in case we have array of arrays
		if(objectValue != null && objectValue instanceof NSArray && EOQualifier.QualifierOperatorContains.equals(_selector) && PROPERTIES.shouldFlattenValueObject){
			objectValue = ERXArrayUtilities.flatten((NSArray<?>) objectValue);
			retVal = EOQualifier.ComparisonSupport.compareValues(objectValue, _value, _selector);
		} else if(objectValue != null && objectValue instanceof NSArray && PROPERTIES.enableArrayElementComparison){
			//flatten the array
			objectValue = ERXArrayUtilities.flatten((NSArray<?>) objectValue);
			//loop through the flattened array and compare each element with _value
			for(int i = 0; i < ((NSArray)objectValue).count() && !retVal; i++){
				NSDictionary<String, Object> newObject = new NSDictionary<String, Object>(((NSArray)objectValue).get(i), _key);
				retVal = super.evaluateWithObject(newObject);
			}
		} else {
			retVal = super.evaluateWithObject(object);
		}
		return retVal;
	}
}
