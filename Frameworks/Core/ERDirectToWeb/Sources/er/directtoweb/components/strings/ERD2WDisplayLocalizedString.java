package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.strings.ERD2WDisplayString;
import er.extensions.foundation.ERXStringUtilities;

public class ERD2WDisplayLocalizedString extends ERD2WDisplayString {

    public ERD2WDisplayLocalizedString(WOContext context) {
        super(context);
    }

    @Override
    public Object objectPropertyValue() {
        Object object = super.objectPropertyValue();
        String prefix = null;
        if (d2wContext().valueForKey("localizationPrefix") != null) {
            prefix = (String) d2wContext().valueForKey("localizationPrefix");
        }
        if (object instanceof Enum) {
            if (ERXStringUtilities.stringIsNullOrEmpty(prefix)) {
                return object.toString();
            } else {
                return prefix + object.toString();
            }
        } else if (super.objectPropertyValue() instanceof String
                && !ERXStringUtilities.stringIsNullOrEmpty(prefix)) {
            return prefix + object.toString();
        }
        return super.objectPropertyValue();
    }
}