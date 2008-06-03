package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;

/**
 * Class for DirectToWeb Component ERDQueryPageRepetition.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Mon Sep 01 2003
 * @project ERDirectToWeb
 */

public class ERDQueryPageRepetition extends ERDAttributeRepetition {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDQueryPageRepetition.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDQueryPageRepetition(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }


    public WODisplayGroup displayGroup() {
        return (WODisplayGroup)valueForBinding("displayGroup");
    }
}
