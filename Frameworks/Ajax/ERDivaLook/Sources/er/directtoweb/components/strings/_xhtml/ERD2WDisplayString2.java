package er.directtoweb.components.strings._xhtml;

import com.webobjects.appserver.WOContext;

import er.directtoweb.components.strings.ERD2WDisplayString;
import er.extensions.foundation.ERXStringUtilities;

public class ERD2WDisplayString2 extends ERD2WDisplayString {
    public ERD2WDisplayString2(WOContext context) {
        super(context);
    }
    
    // accessors 
    public String classString() {
    	Object classValue = d2wContext().valueForKey("class");
    	return classValue != null ? ERXStringUtilities.safeIdentifierName(classValue.toString()) : null;
    }
}