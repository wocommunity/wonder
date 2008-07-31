package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXQ;

import java.util.Enumeration;

/**
 * ERXAndQualifier is a chainable extension of EOAndQualifier.
 * 
 * @author mschrag
 */
public class ERXAndQualifier extends EOAndQualifier implements IERXChainableQualifier {
	@SuppressWarnings("cast")
	public ERXAndQualifier(NSArray qualifiers) {
		super((NSArray) qualifiers);
	}

	@SuppressWarnings("unchecked")
	public ERXAndQualifier and(EOQualifier... qualifiers) {
		NSMutableArray newQualifiers = qualifiers().mutableClone();
		for (EOQualifier qualifier : qualifiers) {
			if (qualifier != null) {
				newQualifiers.addObject(qualifier);
			}
		}
		return new ERXAndQualifier(newQualifiers);
	}

	public ERXNotQualifier not() {
		return ERXChainedQualifierUtils.not(this);
	}

	public ERXOrQualifier or(EOQualifier... qualifiers) {
		return ERXChainedQualifierUtils.or(this, qualifiers);
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
