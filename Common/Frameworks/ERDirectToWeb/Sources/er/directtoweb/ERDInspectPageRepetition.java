package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

import er.extensions.*;

/**
 * Class for DirectToWeb Component ERDInspectPageRepetition.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Mon Sep 01 2003
 * @project ERDirectToWeb
 */

public class ERDInspectPageRepetition extends ERDAttributeRepetition {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERDInspectPageRepetition.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDInspectPageRepetition(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    public EOEnterpriseObject object() {
        return (EOEnterpriseObject)valueForBinding("object");
    }
}
