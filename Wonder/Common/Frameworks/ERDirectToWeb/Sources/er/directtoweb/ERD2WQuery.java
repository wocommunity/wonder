package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

import er.extensions.*;

/**
 * Same as D2WQuery, except that you can specify the queryBindings in advance.
 * 
 * @created ak on Fri Jan 09 2004
 * @project ERDirectToWeb
 */

public class ERD2WQuery extends D2WQuery {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERD2WQuery.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERD2WQuery(WOContext context) {
        super(context);
    }
    
    static {
    	D2WSwitchComponent.addToPossibleBindings("queryBindings");
    }
}
