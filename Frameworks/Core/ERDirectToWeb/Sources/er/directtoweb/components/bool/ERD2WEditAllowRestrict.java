package er.directtoweb.components.bool;

import com.webobjects.appserver.WOContext;

/**
 * Edits a boolean with the string Allow/Restrict. <br />
 * You should use ERD2WCustomEditBoolean with the choicesNames d2w key instead.
 * 
 */
@Deprecated
public class ERD2WEditAllowRestrict extends ERD2WEditYesNo {

    public ERD2WEditAllowRestrict(WOContext context) {
        super(context);
    }
}
