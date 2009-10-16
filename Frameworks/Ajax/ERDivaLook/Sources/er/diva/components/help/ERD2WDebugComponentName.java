package er.diva.components.help;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.D2WContext;

public class ERD2WDebugComponentName extends WOComponent {
    public ERD2WDebugComponentName(WOContext context) {
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