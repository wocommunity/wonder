/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.wrox.User;

public class LinkToInspectUser extends WOComponent {

    public LinkToInspectUser(WOContext context) {
        super(context);
    }
    
    public boolean isStateless() { return true; }
    public EOEnterpriseObject object() { return (EOEnterpriseObject)valueForBinding("object"); }

    public User user() { return (User)object(); }

    public WOComponent inspectUser() {
        InspectPageInterface ipi = (EditPageInterface)D2W.factory().pageForConfigurationNamed("InspectUser", session());
        ipi.setObject(user());
        ipi.setNextPage(context().page());
        return (WOComponent)ipi;
    }    
}
