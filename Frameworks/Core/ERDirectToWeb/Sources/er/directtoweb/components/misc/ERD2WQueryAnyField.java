package er.directtoweb.components.misc;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WQueryAnyField;

/**
 * Localized QueryAnyField for the query all pages.
 * 
 *
 * @author giorgio
 */
public class ERD2WQueryAnyField extends D2WQueryAnyField {

    public ERD2WQueryAnyField(WOContext context) {
        super(context);
    }

    public String relationshipKey() {
        String result = propertyKey();
        // AK: for whatever reason, the relationship key is "()" in this component and as
        // I couldn't find the cause, I fixed it here
        if("()".equals(result)) {
            result = null;
        }
        return result;
    }

}
