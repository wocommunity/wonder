/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.directtoweb;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;


public class ERD2WConfirmPageTemplate extends ERD2WInspectPage implements ConfirmPageInterface {

    public ERD2WConfirmPageTemplate(WOContext context) {super(context);}
    
    protected String _message;
    protected WOComponent _cancelPage;
    protected NextPageDelegate _cancelDelegate;

    public WOComponent cancelAction() { return (cancelDelegate() != null) ? cancelDelegate().nextPage(this) : cancelPage(); }
    public WOComponent confirmAction() { return errorMessages.count()==0 ? nextPage() : null; }
    
    public void setMessage(String message) { _message = message; }
    public String message() { return _message; }
    
    public void setCancelPage(WOComponent cancelPage) { _cancelPage = cancelPage; }
    public WOComponent cancelPage() { return _cancelPage; }

    public void setCancelDelegate(NextPageDelegate cancelDelegate) { _cancelDelegate = cancelDelegate; }
    public NextPageDelegate cancelDelegate() { return _cancelDelegate; }

    // These just remap to the nextPage* of EditPageInterface
    public void setConfirmDelegate(NextPageDelegate confirmPageDelegate) { setNextPageDelegate(confirmPageDelegate); }
    public NextPageDelegate confirmDelegate() { return nextPageDelegate(); }

    public void setConfirmPage(WOComponent confirmPage) { setNextPage(confirmPage); }
    public WOComponent confirmPage() { return nextPage(); }
}
