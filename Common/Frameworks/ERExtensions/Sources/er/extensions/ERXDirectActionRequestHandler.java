//
// ERXDirectActionRequestHandler.java
// Project ERExtensions
//
// Created by tatsuya on Thu Aug 15 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.appserver._private.WODirectActionRequestHandler;

/**
 * 
 * NOTE: This class is multi thread safe. 
 */
public class ERXDirectActionRequestHandler extends WODirectActionRequestHandler {

    public ERXDirectActionRequestHandler() {
        super();
    }
    
    public ERXDirectActionRequestHandler(String actionClassName, String defaultActionName,
					boolean shouldAddToStatistics) {
        super(actionClassName, defaultActionName, shouldAddToStatistics);
    }

    public WOResponse handleRequest(WORequest request) {
        WOResponse response = super.handleRequest(request);
        
        ERXMessageEncoding messageEncoding = null;
        
        // This should retrieve the session object belonging to the same 
        // worker thread that's been calling the current handleRequest method. 
        WOSession session = ERXExtensions.session();   // get it from the thread specific storage 
        if (session != null  &&  session instanceof ERXSession) {
            ERXSession erxSession = (ERXSession)session;
            messageEncoding = erxSession.messageEncoding();
            erxSession.lastActionWasDA = true;
        } else {
            messageEncoding = new ERXMessageEncoding(request.browserLanguages());
        }
        messageEncoding.setEncodingToResponse(response);
        return response;
    }

}
