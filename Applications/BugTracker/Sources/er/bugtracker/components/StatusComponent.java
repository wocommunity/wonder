/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.components;

import com.webobjects.appserver.WOContext;

import er.bugtracker.Bug;
import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.localization.ERXLocalizer;

public class StatusComponent extends ERDCustomEditComponent {

    public StatusComponent(WOContext aContext) {
        super(aContext);
    }

    @Override
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    @Override
    public boolean isStateless() {
        return true;
    }
    
    private Bug bug() {
        return (Bug)object();
    }
/*
   bug=# select * from state;
    id  | sort_order | description 
  ------+------------+-------------
   anzl |          1 | Analyze
   buld |          2 | Build
   vrfy |          3 | Verify
   dcmt |          4 | Document
   clsd |          5 | Closed
 */ 

    public boolean showText() {
        return !"list".equals(valueForBinding("task"));
    }

    public String[] bugIcons=new String[] { 
            "spider.gif", "molette.gif", "check.gif", "document.gif", "closed.gif" };

    @Override
    public String name() {
        return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(bug().state().textDescription());
    }
    
    public String filename() {
        String result="closed.gif";
        if (bug()!=null) {
            int img=bug().state().sortOrder()- 1;
            result=bugIcons[img];
        }
        return result;
    }
}
