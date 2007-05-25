/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.components;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

import er.bugtracker.Factory;

public class FreeQuery extends WOComponent {

    public FreeQuery(WOContext aContext) {
        super(aContext);
    }

    public String string;

    public WOComponent find() {
        return Factory.bugTracker().findBugs(string);            
    }
}
