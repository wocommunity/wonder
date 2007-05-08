/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.components;
import com.webobjects.appserver.WOContext;

import er.bugtracker.Bug;
import er.directtoweb.ERDCustomComponent;

public class StatusComponent extends ERDCustomComponent {

    public StatusComponent(WOContext aContext) {
        super(aContext);
    }

    public Bug object;
    public String key;

    public String[] bugIcons=new String[] { "spider.gif", "closed.gif", "check.gif", "molette.gif", "document.gif" };
    public String[] requirementsIcons=new String[] { "spider.gif", "molette.gif", "check.gif", "closed.gif" };
    
    public String filename() {
        String result="closed.gif";
        if (object!=null) {
            Number pkNum=object.state().sortOrder();
            if (pkNum!=null) {
                int pk=pkNum.intValue();
                if (object instanceof Bug) result=bugIcons[pk-1];
            }
        }
        return result;
    }
}
