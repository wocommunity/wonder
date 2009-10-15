package er.extensions;

import com.webobjects.foundation.NSArray;

/**
 * A class to bridge the migration of this class to the eof.qualifiers sub-package.
 * @deprecated use er.extensions.eof.qualifiers.ERXInQualifier
 */
public class ERXInQualifier extends er.extensions.eof.qualifiers.ERXInQualifier {

    public ERXInQualifier(String key, NSArray values) {
        super(key, values);
    }

    public ERXInQualifier(String key, NSArray values, int padToSize) {
        super(key, values, padToSize);
    }
    
}
