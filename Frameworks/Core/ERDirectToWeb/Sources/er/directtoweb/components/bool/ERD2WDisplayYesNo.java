package er.directtoweb.components.bool;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

/**
 * Displays a boolean localized as Yes or No.<br />
 * You should use ERD2WCustomDisplayBoolean with the choicesNames d2w key instead.
 */
@Deprecated 
public class ERD2WDisplayYesNo extends ERD2WCustomDisplayBoolean {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private  NSArray<String> choicesNames = new NSArray(new String[] {"Yes" , "No"});
    
    public ERD2WDisplayYesNo(WOContext context) {
        super(context);
    }
    
    @Override
    public NSArray<String> choicesNames() {
        return choicesNames;
    }
}
