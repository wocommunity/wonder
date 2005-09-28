package er.extensions;

import com.webobjects.eoaccess.EOAdaptorContext;

public class ERXAdaptorContextDelegate {
    private ERXLogger log = ERXLogger.getERXLogger(ERXAdaptorContextDelegate.class);
 
    private static ERXAdaptorContextDelegate _delegate;

    public static void setupDelegate() {
        _delegate = new ERXAdaptorContextDelegate();
        EOAdaptorContext.setDefaultDelegate(_delegate);
    }

    public static ERXAdaptorContextDelegate delegate() {
        return _delegate;
    }
    
    public void adaptorContextDidCommit(EOAdaptorContext adaptorContext) {
    		if (log.isDebugEnabled()) {
    			log.debug("adaptorContextDidCommit "+adaptorContext);
    		}
    }
}
