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
import er.extensions.ERXEC;
import er.extensions.ERXExtensions;
import er.wrox.Group;

public class LinkToEditGroup extends WOComponent {

    public LinkToEditGroup(WOContext context) {
        super(context);
    }
    
    public boolean isStateless() { return true; }
    public EOEnterpriseObject object() { return (EOEnterpriseObject)valueForBinding("object"); }

    public Group group() { return (Group)object(); }

    public WOComponent editGroup() {
        // This will be a peer of the session's default editingContext.  We want to edit in a peer so in case they hit
        // the back button the ec won't be dirty
        EOEditingContext peer = ERXEC.newEditingContext();
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed("EditGroup", session());
        epi.setObject(EOUtilities.localInstanceOfObject(peer, group()));
        epi.setNextPage(context().page());
        return (WOComponent)epi;
    }    
}
