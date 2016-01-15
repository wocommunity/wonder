/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.navigation.ERXNavigationManager;
import er.extensions.appserver.navigation.ERXNavigationState;
import er.extensions.components._private.ERXSubmitButton;
import er.extensions.foundation.ERXPatcher;

public class PageWrapper extends WOComponent {
    private static final Logger log = LoggerFactory.getLogger(PageWrapper.class);

    public PageWrapper(WOContext aContext) {
        super(aContext);
        ERXPatcher.setClassForName(ERXSubmitButton.class, "WOSubmitButton");
    }

    public String pageName() {
    	String pageName = (String) valueForBinding("pageName");
    	if(pageName == null && d2wContext() != null) {
    		pageName = d2wContext().dynamicPage();
    	}
    	if(pageName == null) {
    		pageName = context().page().name();
    	}
    	return pageName;
    }
    
    public D2WContext d2wContext() {
    	if (context().page() instanceof D2WPage) {
			D2WPage d2wPage = (D2WPage) context().page();
			return d2wPage.d2wContext();
		}
    	return null;
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
        log.debug("NavigationState: {}, {}, {}", state, state.state(), state.stateAsString());
        //log.info("navigationContext: {}", session().objectForKey("navigationContext"));
        return context;
    }
}
