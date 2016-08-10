package er.jdbcadaptor;

import com.webobjects.eoaccess.EOAdaptorContext;


/**
 * @author david
 */
public class PrincipalClass {

    static {
        EOAdaptorContext.setDefaultDelegate(ERAdaptorContextDelegate.defaultDelegate());
    }
    
    private PrincipalClass() {
        super();
    }
}
