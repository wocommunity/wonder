package er.extensions;

import com.webobjects.foundation.NSArray;

/**
 * A class to bridge the migration of this class to the eof.qualifiers sub-package.
 * @deprecated use er.extensions.eof.qualifiers.ERXPrimaryKeyListQualifier
 */
public class ERXPrimaryKeyListQualifier extends er.extensions.eof.qualifiers.ERXPrimaryKeyListQualifier {

    public ERXPrimaryKeyListQualifier(NSArray eos) {
        super(eos);
    }

    public ERXPrimaryKeyListQualifier(String key, NSArray eos) {
        super(key, eos);
    }

    public ERXPrimaryKeyListQualifier(String key, String foreignKey, NSArray eos) {
        super(key, foreignKey, eos);
    }
    
}
