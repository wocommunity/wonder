package er.directtoweb.components.bool;

import com.webobjects.appserver.WOContext;

/**
 * Edits a boolean with the string Allow/Restrict. <br />
 * You should use ERD2WCustomEditBoolean with the choicesNames d2w key instead.
 * 
 */
@Deprecated
public class ERD2WEditAllowRestrict extends ERD2WEditYesNo {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WEditAllowRestrict(WOContext context) {
        super(context);
    }
}
