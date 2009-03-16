package er.directtoweb.components.repetitions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

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
    private static final Logger log = Logger.getLogger(ERDInspectPageRepetition.class);
	
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
