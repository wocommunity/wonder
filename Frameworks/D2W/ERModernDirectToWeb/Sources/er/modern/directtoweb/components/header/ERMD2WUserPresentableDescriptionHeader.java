package er.modern.directtoweb.components.header;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.foundation.ERXStringUtilities;
import er.modern.directtoweb.components.header.ERMD2WHeader;
import er.modern.directtoweb.components.header.ERMD2WSimpleHeader;

/**
 * Simple h1 header that defaults to displaying the
 * displayNameForPageConfiguration and appends the object's
 * userPresentableDescription if one is available.
 * 
 * @author fpeters
 * 
 */
public class ERMD2WUserPresentableDescriptionHeader extends ERMD2WSimpleHeader {

    private static final long serialVersionUID = 1L;

    public interface Keys extends ERMD2WHeader.Keys {
        public static String displayNameForPageConfiguration = "displayNameForPageConfiguration";
    }

    protected String _headerString;

    public ERMD2WUserPresentableDescriptionHeader(WOContext context) {
        super(context);
    }

    public String headerString() {
        String headerString = super.headerString();
        if (object() != null) {
            String userPresentableDescription = (String) object().valueForKey(
                    "userPresentableDescription");
            if (!ERXStringUtilities.stringIsNullOrEmpty(userPresentableDescription)) {
                // is displayNameForPageConfiguration null?
                if (ERXStringUtilities.stringIsNullOrEmpty(headerString)) {
                    headerString = userPresentableDescription;
                } else {
                    headerString = headerString + ": " + userPresentableDescription;
                }
            }
        }
        return headerString;
    }

    public EOEnterpriseObject object() {
        EOEnterpriseObject _object = (EOEnterpriseObject) valueForBinding("object");
        return _object;
    }

}
