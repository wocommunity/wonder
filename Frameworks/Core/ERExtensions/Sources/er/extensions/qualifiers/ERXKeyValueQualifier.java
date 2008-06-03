package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXQ;

/**
 * ERXKeyValueQualifier is a chainable extension of EOKeyValueQualifier.
 * 
 * @author mschrag
 */
public class ERXKeyValueQualifier extends EOKeyValueQualifier implements IERXChainableQualifier {
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
		return ERXChainedQualifierUtils.not(this);
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
}
