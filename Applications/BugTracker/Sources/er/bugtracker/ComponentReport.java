/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.bugtracker.Component;

public class ComponentReport extends WOComponent {

    public ComponentReport(WOContext c) {
        super(c);
    }
    
    public Component component;
    
    public NSArray componentList() {
        return Component.orderedComponents(session().defaultEditingContext());        
    }    
}
