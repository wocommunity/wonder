/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import er.extensions.ERXSession;

// FIXME: All of the message pages need to be cleaned up.
/**
 * A recoverable error page.<br />
 * 
 */

public class ERDRecoverableErrorPage extends ERD2WPage implements ERDErrorPageInterface {

    public ERDRecoverableErrorPage(WOContext context) { super(context); }
    
    private String _wrapperName=null;
    public void setWrapperName(String wrapperName) { _wrapperName=wrapperName; }

    public String wrapperName() {
        return _wrapperName!=null ? _wrapperName : (String)d2wContext().valueForKey("pageWrapperName");
    }
    
    protected WOComponent _nextPage;
    public void setNextPage(WOComponent page) { _nextPage=page; }
    public WOComponent nextPageClicked() { return _nextPage; }

    /*protected D2WContext _d2wContext;
    public D2WContext d2wContext() {return _d2wContext; }
*/
    protected String _message;
    public void setMessage(String message) { _message=message; }
    public String message() {return _message; }

    protected Exception _exception;
    public void setException(Exception exception) { _exception=exception; }
    public Exception exception() {return _exception; }

    public String formattedMessage() {
        //return Services.breakDown(_message,60);
        return WOMessage.stringByEscapingHTMLString(message());
    }


}

