package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXQ;

/**
 * ERXOrQualifier is a chainable extension of EOOrQualifier.
 * 
 * @author mschrag
 */
public class ERXOrQualifier extends EOOrQualifier implements IERXChainableQualifier {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public ERXOrQualifier(NSArray<? extends EOQualifier> qualifiers) {
		super((NSArray<EOQualifier>) qualifiers);
	}
	
	public ERXOrQualifier(EOQualifier... qualifiers) {
		super(new NSArray<EOQualifier>(qualifiers));
	}
	
	public ERXAndQualifier and(EOQualifier... qualifiers) {
		return ERXChainedQualifierUtils.and(this, qualifiers);
	}

	public ERXNotQualifier not() {
		return ERXQ.not(this);
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
