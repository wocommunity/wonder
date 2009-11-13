package er.directtoweb.components;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.D2WContext;

/**
 * 
 * @author mschrag
 * @d2wKey propertyKey
 */
public class ERD2WDebugPropertyName extends WOComponent {
    public ERD2WDebugPropertyName(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    
    // accessors
    public D2WContext d2wContext() {
    	return (D2WContext) valueForBinding("d2wContext");
    }
}