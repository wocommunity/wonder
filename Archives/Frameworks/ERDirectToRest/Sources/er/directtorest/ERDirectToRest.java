package er.directtorest;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.ERD2WContext;

import er.directtoweb.ERDirectToWeb;
import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;
import er.extensions.foundation.ERXThreadStorage;

public class ERDirectToRest extends ERXFrameworkPrincipal {
    
    public static Class[] REQUIRES = {ERXExtensions.class, ERDirectToWeb.class};

    static {
        setUpFrameworkPrincipalClass(ERDirectToRest.class);
    }

    /**
     * @return the threads d2w context
     */
    public static D2WContext d2wContext() {
        D2WContext result = (D2WContext) ERXThreadStorage.valueForKey("ERD2Rest.d2wContext");
        if(result == null) {
            result = ERD2WContext.newContext();
            ERXThreadStorage.takeValueForKey(result, "ERD2Rest.d2wContext");
        }
        return result;
    }

    @Override
    public void finishInitialization() {
 
    }

}
