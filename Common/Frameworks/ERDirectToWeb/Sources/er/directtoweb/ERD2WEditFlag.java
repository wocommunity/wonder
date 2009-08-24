package er.directtoweb;

import com.webobjects.appserver.WOContext;


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
