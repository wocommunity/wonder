package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

import er.extensions.eof.ERXQ;

/**
 * ERXKeyComparisonQualifier is a chainable extension of
 * EOKeyComparisonQualifier.
 * 
 * @author mschrag
 */
public class ERXKeyComparisonQualifier extends EOKeyComparisonQualifier implements IERXChainableQualifier {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
		return ERXQ.not(this);
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
