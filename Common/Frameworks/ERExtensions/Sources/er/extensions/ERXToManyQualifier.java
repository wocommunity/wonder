package er.extensions;

import com.webobjects.foundation.NSArray;

/**
 * A class to bridge the migration of this class to the eof.qualifiers sub-package.
 * @deprecated use er.extensions.eof.qualifiers.ERXToManyQualifier
 */
public class ERXToManyQualifier extends er.extensions.eof.qualifiers.ERXToManyQualifier {

    public ERXToManyQualifier(String toManyKey, NSArray elements) {
        super(toManyKey, elements);
    }

    public ERXToManyQualifier(String toManyKey, NSArray elements, int minCount) {
        super(toManyKey, elements, minCount);
    }
}
