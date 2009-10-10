package er.extensions;

import com.webobjects.eocontrol.EOQualifier;

/**
 * A class to bridge the migration of this class to the eof.qualifiers sub-package.
 * @deprecated use er.extensions.eof.qualifiers.ERXExistsQualifier
 */
public class ERXExistsQualifier extends er.extensions.eof.qualifiers.ERXExistsQualifier {

    public ERXExistsQualifier(EOQualifier subqualifier) {
        super(subqualifier);
    }

    public ERXExistsQualifier(EOQualifier subqualifier, String baseKeyPath) {
        super(subqualifier, baseKeyPath);
    }
    
}
