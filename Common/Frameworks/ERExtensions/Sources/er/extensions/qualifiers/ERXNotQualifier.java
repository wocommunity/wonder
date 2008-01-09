package er.extensions.qualifiers;

import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOQualifier;

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
}
