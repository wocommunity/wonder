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
import er.extensions.ERXUtilities;
import er.wrox.eo.Group;

public class LinkToViewGroupUsers extends WOComponent {

    public LinkToViewGroupUsers(WOContext context) {
        super(context);
    }
    
    public boolean isStateless() { return true; }
    public EOEnterpriseObject object() { return (EOEnterpriseObject)valueForBinding("object"); }

    public Group group() { return (Group)object(); }

    public boolean showListOption() { return group().users().count() != 0; }
    
    public WOComponent listGroupUsers() {
        ListPageInterface lpi = (ListPageInterface)D2W.factory().pageForConfigurationNamed("ListUsers", session());
        lpi.setDataSource(ERXUtilities.dataSourceForArray(group().users()));
        lpi.setNextPage(context().page());
        return (WOComponent)lpi;
    }    

}
