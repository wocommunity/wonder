package er.extensions.qualifiers;

import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.ERXQ;

/**
 * ERXNotQualifier is a chainable extension of EONotQualifier.
 * 
 * @author mschrag
 */
public class ERXNotQualifier extends EONotQualifier implements IERXChainableQualifier {
	public ERXNotQualifier(EOQualifier qualifier) {
		super(qualifier);
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
