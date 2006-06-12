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
import er.extensions.ERXUtilities;
import er.extensions.ERXExtensions;
import org.apache.log4j.Logger;

public class Home extends WOComponent {

    public Home(WOContext context) {
        super(context);
    }
    
    public WOComponent editMyInformation() {
        // This will be a peer of the session's default editingContext.
        EOEditingContext peer = ERXEC.newEditingContext();
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed("EditUser", session());
        epi.setObject(ERXUtilities.localInstanceOfObject(peer, ((Session)session()).user()));
        epi.setNextPage(context().page());
        return (WOComponent)epi;        
    }
    
    public WOComponent createUser() {
        // This will be a peer of the session's default editingContext.
        EOEditingContext peer = ERXEC.newEditingContext();
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed("EditUser", session());
        epi.setObject(ERXUtilities.createEO("User", peer));
        epi.setNextPage(context().page());
        return (WOComponent)epi;
    }

    public WOComponent createGroup() {
        // This will be a peer of the session's default editingContext.
        EOEditingContext peer = ERXEC.newEditingContext();
        EditPageInterface epi = (EditPageInterface)D2W.factory().pageForConfigurationNamed("EditGroup", session());
        epi.setObject(ERXUtilities.createEO("Group", peer));
        epi.setNextPage(context().page());
        return (WOComponent)epi;
    }

    public WOComponent listUsers() {
        ListPageInterface lpi = (ListPageInterface)D2W.factory().pageForConfigurationNamed("ListAllUsers", session());
        lpi.setDataSource(((Session)session()).usersDataSource());
        return (WOComponent)lpi;
    }

    public WOComponent listGroups() {
        ListPageInterface lpi = (ListPageInterface)D2W.factory().pageForConfigurationNamed("ListAllGroups", session());
        lpi.setDataSource(((Session)session()).groupsDataSource());
        return (WOComponent)lpi;
    }

    public WOComponent throwNiceRuntimeException() {
        ((Session)session()).user().valueForKey("foo").toString();
        return null;
    }

    public WOComponent log4jExample() {
        return null;
    }
}
