/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.InspectPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.eocontrol.EOCooperatingObjectStore;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.*;

/**
 * Little help component useful for debugging.<br />
 * 
 * @binding d2wContext
 * @binding condition" defaults="Boolean
 */

public class ERDDebuggingHelp extends WOComponent {

    public ERDDebuggingHelp(WOContext context) { super(context); }

    public boolean synchronizesVariablesWithBindings() { return false; }
    
    public boolean showHelp() {
        return ERDirectToWeb.d2wDebuggingEnabled(session()) || ERXValueUtilities.booleanValue(valueForBinding("condition"));
    }
    public boolean d2wComponentNameDebuggingEnabled() {
        return ERDirectToWeb.d2wComponentNameDebuggingEnabled(session());
    }
    public WOComponent toggleComponentNameDebugging() {
        ERDirectToWeb.setD2wComponentNameDebuggingEnabled(session(),
                                                          !ERDirectToWeb.d2wComponentNameDebuggingEnabled(session()));
        return null;
    }

    public String key;
    protected EOEditingContext editingContext;
    protected boolean didSearchEditingContext;
    
    public EOEditingContext editingContext() {
    	if(editingContext == null && !didSearchEditingContext) {
    		WOComponent parent = parent();
    		while(parent != null && editingContext == null) {
    			if(parent instanceof ERD2WPage) {
    				editingContext = ((ERD2WPage)parent).editingContext();
    			}
    			parent = parent.parent();
    		}
    		didSearchEditingContext = true;
    	}
    	return editingContext;
    }
    
    public WOComponent showEditingContext() {
    	WOComponent nextPage = pageWithName("ERXEditingContextInspector");
    	nextPage.takeValueForKey(editingContext(), "object");
    	return nextPage;
    }
    
    public boolean hasEditingContext() {
    	return editingContext() != null;
    }
    
    public Object debugValueForKey() {
        if(key != null && !"".equals(key))
            return parent().valueForKeyPath("d2wContext."+key);
        return null;
    }
}
