/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.bugtracker.Factory;
import er.extensions.ERXNavigationManager;
import er.extensions.ERXNavigationState;

public class MenuHeader extends WOComponent {

    public MenuHeader(WOContext aContext) {
        super(aContext);
    }

    public String item;

    public NSArray mainMenuItems() {
        return ERXNavigationManager.manager().navigationItemForName("MainMenu").children();
    }

    public NSKeyValueCoding navigationContext() {
        NSKeyValueCoding context = (NSKeyValueCoding)session().objectForKey("navigationContext");

        if (context().page() instanceof D2WPage) {
            context = ((D2WPage)context().page()).d2wContext();
        }

        //log.debug(ERXNavigationManager.manager().navigationStateForSession(session()));
        if(context == null) {
            context = new NSMutableDictionary();
            session().setObjectForKey(context, "navigationContext");
        }
        ERXNavigationState state = ERXNavigationManager.manager().navigationStateForSession(session());
        // log.debug("NavigationState:" + state + "," + state.state() + "," + state.stateAsString());
        //log.info("navigationContext:" + session().objectForKey("navigationContext"));
        return context;
    }

    public WOComponent homeAction() {
        return pageWithName("HomePage");
    }
    
    public boolean quickSearchDisabled() {
    	return entityName() == null;
    }

    public String entityName() {
        return (String) parent().valueForKeyPath("d2wContext.entity.name");
    }

    public WOComponent freeQuery() {
        return pageWithName("FreeQuery");
    }

    public String bugNumber;

    public WOComponent findBugByNumber() {
        if(bugNumber != null) {
            return Factory.bugTracker().findBugs(bugNumber);
        }
        return context().page();
    }
}
