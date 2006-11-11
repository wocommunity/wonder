package er.ajax;

import com.webobjects.appserver.WOApplication;

import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

public class Ajax extends ERXFrameworkPrincipal {
	
	public static Class[] REQUIRES = {ERXExtensions.class};

    static {
        setUpFrameworkPrincipalClass(Ajax.class);
    }

    public Ajax() {
	}
	
	public void finishInitialization() {
		WOApplication.application().registerRequestHandler(new AjaxRequestHandler(), AjaxRequestHandler.AjaxRequestHandlerKey);
	}
}
