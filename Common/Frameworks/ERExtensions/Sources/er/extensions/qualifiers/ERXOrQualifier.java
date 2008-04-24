package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXQ;

/**
 * ERXOrQualifier is a chainable extension of EOOrQualifier.
 * 
 * @author mschrag
 */
public class ERXOrQualifier extends EOOrQualifier implements IERXChainableQualifier {
	@SuppressWarnings("cast")
	public ERXOrQualifier(NSArray<? extends EOQualifier> qualifiers) {
		super((NSArray) qualifiers);
	}

	public ERXAndQualifier and(EOQualifier... qualifiers) {
		return ERXChainedQualifierUtils.and(this, qualifiers);
	}

	public ERXNotQualifier not() {
		return ERXChainedQualifierUtils.not(this);
	}

	@SuppressWarnings("unchecked")
	public ERXOrQualifier or(EOQualifier... qualifiers) {
		NSMutableArray<EOQualifier> newQualifiers = qualifiers().mutableClone();
		for (EOQualifier qualifier : qualifiers) {
			if (qualifier != null) {
				newQualifiers.addObject(qualifier);
			}
		}
		return new ERXOrQualifier(newQualifiers);
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
