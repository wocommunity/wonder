/*
 * Created on Jan 27, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.*;
import com.webobjects.foundation.*;


/**
 * @author david
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ERXHyperlink extends WOHyperlink {
    public static ERXLogger log = ERXLogger.getERXLogger(ERXHyperlink.class);
    
    /**
     * @param arg0
     * @param arg1
     * @param arg2
     */
    public ERXHyperlink(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
    }

    
    /* logs the action name into session's dictionary with a key = ERXActionLogging 
     */
    public WOActionResults invokeAction(WORequest arg0, WOContext arg1) {
        WOActionResults result = super.invokeAction(arg0, arg1);
        if (result != null && ERXSession.session() != null) {
            ERXSession.session().setObjectForKey(this.toString(), "ERXActionLogging");
        }
        return result;
    }
}
