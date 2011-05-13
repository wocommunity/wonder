package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableSet;

/**
 * A qualifier that always evaluates to false, and thus <strong>never</strong> qualifies an objects.
 * For example if you qualify an array or a fetch with this qualifier, the result will be an empty array.
 */
public class ERXFalseQualifier extends EOQualifier {
	@Override
	public void addQualifierKeysToSet(NSMutableSet keys) {
	}

	@Override
	public EOQualifier qualifierWithBindings(NSDictionary bindings, boolean requireAll) {
		return this;
	}

	@Override
	public void validateKeysWithRootClassDescription(EOClassDescription classDescription) {
	}

	@Override
	public boolean evaluateWithObject(Object object) {
		return false;
	}
}
