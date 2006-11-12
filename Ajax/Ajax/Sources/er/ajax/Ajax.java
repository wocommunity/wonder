package er.ajax;

import com.webobjects.appserver.WOApplication;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

public class Ajax extends ERXFrameworkPrincipal {
	
	public static Class[] REQUIRES;

    static {
    	try {
    		REQUIRES = new Class[] { ERXExtensions.class };
    	}
    	catch (Throwable t) {
    		// If you don't have ERExtesnsions
    		REQUIRES = new Class[0];
    	}
        setUpFrameworkPrincipalClass(Ajax.class);
    }

    public Ajax() {
	}
	
	public void finishInitialization() {
		WOApplication.application().registerRequestHandler(new AjaxRequestHandler(), AjaxRequestHandler.AjaxRequestHandlerKey);
	}
}
