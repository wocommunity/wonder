package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

/**
 * Class for DirectToWeb Component ERDActionButton.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @created ak on Mon Sep 01 2003
 * @project ERDirectToWeb
 */

public class ERDActionButton extends ERDCustomComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERDActionButton.class,"components");
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDActionButton(WOContext context) {
        super(context);
    }

    /** action buttons must be stateless. */
    public boolean isStateless() { return true; }

    /** component does not synchronize it's variables. */
    public boolean synchronizesVariablesWithBindings() { return false; }

    public EOEnterpriseObject object() { return (EOEnterpriseObject)valueForBinding("object"); }
    public WODisplayGroup displayGroup() { return (WODisplayGroup)valueForBinding("displayGroup"); }
    public EODataSource dataSource() {return (EODataSource)valueForBinding("dataSource"); }
    public String task() { return (String)valueForBinding("task");  }
    
    /*    public EODataSource dataSource() {
        if(hasBinding("displayGroup"))
        return ((WODisplayGroup)valueForBinding("displayGroup")).dataSource();
    return (EODataSource)valueForBinding("dataSource");
    }*/
    
}
