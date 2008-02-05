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
	public ERXKeyComparisonQualifier(String leftKey, NSSelector selector, String rightKey) {
		super(leftKey, selector, rightKey);
    if (leftKey == null) {
      throw new IllegalArgumentException("A KeyComparisonQualifier must have a left key.");
    }
    if (rightKey == null) {
      throw new IllegalArgumentException("A KeyComparisonQualifier must have a right key.");
    }
    if (selector == null) {
      throw new IllegalArgumentException("A KeyComparisonQualifier must have a selector.");
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
}
