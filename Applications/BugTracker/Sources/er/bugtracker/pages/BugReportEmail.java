/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.pages;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.bugtracker.Bug;
import er.bugtracker.People;

public class BugReportEmail extends WOComponent {

    public BugReportEmail(WOContext aContext) {
        super(aContext);
    }

    public NSArray unreadBugs;
    public People owner;
    public Bug bug;   
}
