package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

/**
 * Class for DirectToWeb Component ERDQueryIsContainedInArray.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Wed Apr 07 2004
 * @project ERDirectToWeb
 */

public class ERDQueryIsContainedInArray extends ERDCustomQueryComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERDQueryIsContainedInArray.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDQueryIsContainedInArray(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }
    
    public void awake() {
        super.awake();
        displayGroup().queryOperator().setObjectForKey(ERXPrimaryKeyListQualifier.IsContainedInArraySelectorName, key());
    }
    
    public Object restrictedChoiceList() {
        String restrictedChoiceKey=(String)valueForBinding("restrictedChoiceKey");
        if( restrictedChoiceKey!=null &&  restrictedChoiceKey.length()>0 )
            return valueForKeyPath(restrictedChoiceKey);
        String fetchSpecName=(String)valueForBinding("restrictingFetchSpecification");
        if(fetchSpecName != null &&  fetchSpecName.length()>0) {
            EOEditingContext ec = ERXEC.newEditingContext();
            ec.lock();
            try {
                return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, (String)valueForBinding("destinationEntityName"),fetchSpecName,null);
            } finally {
                ec.unlock();
            }
        }
        return null;
    }
}
