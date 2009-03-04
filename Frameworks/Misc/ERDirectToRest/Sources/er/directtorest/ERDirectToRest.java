package er.directtorest;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver._private.WODirectActionRequestHandler;
import com.webobjects.foundation.NSArray;

import er.directtoweb.ERDirectToWeb;
import er.extensions.ERXExtensions;
import er.extensions.ERXFrameworkPrincipal;

public class ERDirectToRest extends ERXFrameworkPrincipal {
    
    public static Class[] REQUIRES = {ERXExtensions.class, ERDirectToWeb.class};

    static {
        setUpFrameworkPrincipalClass(ERDirectToRest.class);
    }

    @Override
    public void finishInitialization() {
        WOApplication.application().registerRequestHandler(new WODirectActionRequestHandler() {
            public NSArray getRequestHandlerPathForRequest(WORequest worequest) {
                NSArray nsarray = new NSArray(ERD2RestAction.class.getName());
                return nsarray.arrayByAddingObject(worequest.requestHandlerPath());
            }

        }, "d2r");

    }

}
