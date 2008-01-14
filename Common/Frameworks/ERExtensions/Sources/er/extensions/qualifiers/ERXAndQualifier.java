package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * ERXAndQualifier is a chainable extension of EOAndQualifier.
 * 
 * @author mschrag
 */
public class ERXAndQualifier extends EOAndQualifier implements IERXChainableQualifier {
	public ERXAndQualifier(NSArray<? extends EOQualifier> qualifiers) {
		super(qualifiers);
	}

	@SuppressWarnings("unchecked")
	public ERXAndQualifier and(EOQualifier... qualifiers) {
		NSMutableArray<EOQualifier> newQualifiers = qualifiers().mutableClone();
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
}
