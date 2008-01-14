package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * ERXOrQualifier is a chainable extension of EOOrQualifier.
 * 
 * @author mschrag
 */
public class ERXOrQualifier extends EOOrQualifier implements IERXChainableQualifier {
	public ERXOrQualifier(NSArray<? extends EOQualifier> qualifiers) {
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
		NSMutableArray<EOQualifier> newQualifiers = qualifiers().mutableClone();
		for (EOQualifier qualifier : qualifiers) {
			if (qualifier != null) {
				newQualifiers.addObject(qualifier);
			}
		}
		return new ERXOrQualifier(newQualifiers);
	}
}
