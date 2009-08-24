package er.directtoweb.components.bool;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

/**
 * Displays a boolean localized as Yes or No.<br />
 * You should use ERD2WCustomDisplayBoolean with the choicesNames d2w key instead.
 */
@Deprecated 
public class ERD2WDisplayYesNo extends ERD2WCustomDisplayBoolean {

    private  NSArray<String> choicesNames = new NSArray(new String[] {"Yes" , "No"});
    
    public ERD2WDisplayYesNo(WOContext context) {
        super(context);
    }
    
    public NSArray<String> choicesNames() {
        return choicesNames;
    }
}
