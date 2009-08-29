package er.directtoweb.components.bool;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2WEditString;
import com.webobjects.foundation.NSValidation;


/**
 * Edits a boolean with radio buttons and Yes/No<br />
 * You should use ERD2WCustomEditBoolean with the choicesNames d2w key instead.
 */
@Deprecated
public class ERD2WEditFlag extends ERD2WCustomEditBoolean {

    public ERD2WEditFlag(WOContext context) {
        super(context);
    }
}
