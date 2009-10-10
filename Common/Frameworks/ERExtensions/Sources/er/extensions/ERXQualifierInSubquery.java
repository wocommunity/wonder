package er.extensions;

import com.webobjects.eocontrol.EOQualifier;

/**
 * A class to bridge the migration of this class to the eof.qualifiers sub-package.
 * @deprecated use er.extensions.eof.qualifiers.ERXQualifierInSubquery
 */
public class ERXQualifierInSubquery extends er.extensions.eof.qualifiers.ERXQualifierInSubquery {

    public ERXQualifierInSubquery(EOQualifier q) {
        super(q);
    }

    public ERXQualifierInSubquery(EOQualifier q, String entityName, String attributeName, String destinationAttName) {
        super(q, entityName, attributeName, destinationAttName);
    }

}
