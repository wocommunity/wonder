/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import er.extensions.ERXLogger;

/**
 * Superclass for all message pages.<br />
 * 
 */

public abstract class ERD2WMessagePage extends ERD2WPage implements ERDMessagePageInterface, ERDErrorPageInterface {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger(ERD2WMessagePage.class);
    
    protected String _message;
    protected String _title;
    protected WOComponent _cancelPage;
    protected WOComponent _nextPage;
    protected NextPageDelegate _cancelDelegate;
    protected NextPageDelegate _nextDelegate;
    protected Exception _exception;

    /**
     * Public constructor
     * @param c current context
     */
    public ERD2WMessagePage(WOContext c) {
        super(c);
    }
    
    public WOComponent cancelAction() { return (cancelDelegate() != null) ? cancelDelegate().nextPage(this) : cancelPage(); }
    public WOComponent confirmAction() { return errorMessages.count()==0 ? nextPageAction() : null; }
    public WOComponent nextPageAction() { return nextPageDelegate() != null ? nextPageDelegate().nextPage(this) : nextPage(); }

    public void setException(Exception exception) { _exception=exception; }
    public Exception exception() {return _exception; }

    public void setMessage(String message) { _message = message; }
    public String message() { return _message; }

    public String formattedMessage() {
        return WOMessage.stringByEscapingHTMLString(message());
    }
    
    public String title() {
        if(_title == null) {
            _title = (String) d2wContext().valueForKey("displayNameForPageConfiguration");
        }
        return _title;
    }
    public void setTitle(String title) {
        _title = title;
    }
    
    public void setCancelPage(WOComponent cancelPage) { _cancelPage = cancelPage; }
    public WOComponent cancelPage() { return _cancelPage; }

    public void setCancelDelegate(NextPageDelegate cancelDelegate) { _cancelDelegate = cancelDelegate; }
    public NextPageDelegate cancelDelegate() { return _cancelDelegate; }

    public void setConfirmPage(WOComponent confirmPage) { setNextPage(confirmPage); }
    public WOComponent confirmPage() { return nextPage(); }

    public void setConfirmDelegate(NextPageDelegate confirmPageDelegate) { setNextPageDelegate(confirmPageDelegate); }
    public NextPageDelegate confirmDelegate() { return nextPageDelegate(); }

    public void setNextPageDelegate(NextPageDelegate nextDelegate) { _nextDelegate = nextDelegate; }
    public NextPageDelegate nextPageDelegate() { return _nextDelegate; }    
    
    public void setNextPage(WOComponent nextPage) { _nextPage = nextPage; }
    public WOComponent nextPage() { return _nextPage; }

    // CHECKME ak: do we really need this? It's never referenced in the templates? 
    public String titleForPage() {
        return title() != null ? title() : (String)d2wContext().valueForKey("messageTitleForPage");
    }

    public boolean hasNextPage() {
        return !(nextPage() == null && nextPageDelegate() == null);
    }
    public boolean hasCancelPage() {
        return !(cancelPage() == null && cancelDelegate() == null);
    }
}
