//
//  ERXPathDirectActionRequestHandler.java
//  ERExtensions
//
//  Created by Max Muller on Thu May 08 2003.
//
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
 * The path direct action request handler allows for storing information
 * in the request path. For instance you could have the request path:
 * /WebObjects/MyApp.woa/wpa/com.foo.SomeActionClass/bar/gee/wiz=neat/actionName
 * This action is treated just like:
 * /WebObjects/MyApp.woa/wpa/com.foo.SomeActionClass/actionName
 * 
 */
public class ERXPathDirectActionRequestHandler extends ERXDirectActionRequestHandler {

    //	===========================================================================
    //	Instance Constructor(s)
    //	---------------------------------------------------------------------------
    
    /**
     * Public constructor, just calls super
     */
    public ERXPathDirectActionRequestHandler() {
        super();
    }

    //	===========================================================================
    //	Instance Method(s)
    //	---------------------------------------------------------------------------
    
    /**
     * Public constructor, just calls super
     * @param actionClassName action class name
     * @param defaultActionName action name
     * @param shouldAddToStatistics boolean to add to stats
     */    
    public ERXPathDirectActionRequestHandler(String actionClassName,
                                         String defaultActionName,
                                         boolean shouldAddToStatistics) {
        super(actionClassName, defaultActionName, shouldAddToStatistics);
    }

    /**
     * Modified version for getting the request handler path for a given
     * request.
     * @param aRequest a given request
     * @return array of request handler paths for a given request, truncates
     * 		to the first and last component if the number of components
     * 		is greater than 2.
     */
    public NSArray getRequestHandlerPathForRequest(WORequest aRequest) {
        NSArray paths = super.getRequestHandlerPathForRequest(aRequest);
        if (paths.count() > 2) {
            NSMutableArray temp = new NSMutableArray(paths.objectAtIndex(0));
            temp.addObject(paths.lastObject());
            paths = temp.immutableClone();
        }
        return paths;
    }
}
