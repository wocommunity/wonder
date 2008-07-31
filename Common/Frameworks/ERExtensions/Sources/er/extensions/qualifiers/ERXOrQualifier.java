package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXQ;

import java.util.Enumeration;

/**
 * ERXOrQualifier is a chainable extension of EOOrQualifier.
 * 
 * @author mschrag
 */
public class ERXOrQualifier extends EOOrQualifier implements IERXChainableQualifier {
	@SuppressWarnings("cast")
	public ERXOrQualifier(NSArray qualifiers) {
		super(qualifiers);
	}

	public ERXAndQualifier and(EOQualifier... qualifiers) {
		return ERXChainedQualifierUtils.and(this, qualifiers);
	}

	public ERXNotQualifier not() {
		return ERXChainedQualifierUtils.not(this);
	}

	@SuppressWarnings("unchecked")
	public ERXOrQualifier or(EOQualifier... qualifiers) {
		NSMutableArray newQualifiers = qualifiers().mutableClone();
		for (EOQualifier qualifier : qualifiers) {
			if (qualifier != null) {
				newQualifiers.addObject(qualifier);
			}
		}
		return new ERXOrQualifier(newQualifiers);
	}

	public void filter(NSMutableArray array) {
		ERXQ.filter(array, this);
	}

	public NSArray filtered(NSArray array) {
		return ERXQ.filtered(array, this);
	}

	public <T> T first(NSArray array) {
		return (T)ERXQ.first(array, this);
	}

	public <T> T one(NSArray array) {
		return (T)ERXQ.one(array, this);
	}

	public <T> T requiredOne(NSArray array) {
		return (T)ERXQ.requiredOne(array, this);
	}
}
