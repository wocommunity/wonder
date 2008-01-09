package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSSelector;

/**
 * ERXKeyComparisonQualifier is a chainable extension of EOKeyComparisonQualifier.
 * 
 * @author mschrag
 */
public class ERXKeyComparisonQualifier extends EOKeyComparisonQualifier implements IERXChainableQualifier {
	public ERXKeyComparisonQualifier(String rightKey, NSSelector selector, String leftKey) {
		super(leftKey, selector, rightKey);
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
