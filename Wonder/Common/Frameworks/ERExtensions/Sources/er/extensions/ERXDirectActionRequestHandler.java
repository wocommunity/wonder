//
// ERXDirectActionRequestHandler.java
// Project ERExtensions
//
// Created by tatsuya on Thu Aug 15 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

/**
 * 
 * NOTE: This class is multi thread safe. 
 */
public class ERXDirectActionRequestHandler extends com.webobjects.appserver._private.WODirectActionRequestHandler {

    public WOResponse handleRequest(WORequest request) {
        WOResponse response = super.handleRequest(request);
        
        ERXMessageEncoding messageEncoding = null;
        
        // Remember request handler should be on the same thread to the session it handles. 
        WOSession session = ERXExtensions.session();   // get it from the thread local 
        if (session != null  &&  session instanceof ERXSession) 
            messageEncoding = ((ERXSession)session).messageEncoding();
        else 
            messageEncoding = new ERXMessageEncoding(request.browserLanguages());
        
        messageEncoding.setEncodingToResponse(response);
        return response;
    }

}
