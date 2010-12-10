package er.extensions.qualifiers;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableSet;

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
