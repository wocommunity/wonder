/*
 * Created on Feb 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package er.jdbcadaptor;

import com.webobjects.eoaccess.EOAdaptorContext;


/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PrincipalClass {

    static {
        EOAdaptorContext.setDefaultDelegate(ERAdaptorContextDelegate.defaultDelegate());
    }
    
    /**
     * 
     */
    private PrincipalClass() {
        super();
    }

}
