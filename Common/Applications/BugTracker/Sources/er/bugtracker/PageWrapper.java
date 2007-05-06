/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXNavigationManager;
import er.extensions.ERXNavigationState;

public class PageWrapper extends WOComponent {
    static final Logger log = Logger.getLogger(PageWrapper.class);

    public PageWrapper(WOContext aContext) {
        super(aContext);
    }

    public ERXNavigationState navigationState() {
        return ERXNavigationManager.manager().navigationStateForSession(session());
    }
    
    public String bodyIdentifier() {
        if(context().page() instanceof D2WPage) {
            return ((D2WPage)context().page()).d2wContext().dynamicPage();
        }
        return context().page().name();
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
        log.debug("NavigationState:" + state + "," + state.state() + "," + state.stateAsString());
        //log.info("navigationContext:" + session().objectForKey("navigationContext"));
        return context;
    }
}


