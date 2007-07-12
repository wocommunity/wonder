/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.bugtracker.components;
import com.webobjects.appserver.WOContext;

import er.bugtracker.Bug;
import er.directtoweb.ERDCustomEditComponent;
import er.extensions.ERXLocalizer;

public class PriorityComponent extends ERDCustomEditComponent {

    public PriorityComponent(WOContext aContext) {
        super(aContext);
    }

    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public boolean isStateless() {
        return true;
    }
    
    private Bug bug() {
        return (Bug)object();
    }
    
    public String name() {
        return ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(bug().priority().textDescription());
    }

    public boolean showText() {
        return !"list".equals(valueForBinding("task"));
    }

    /*
bug=# select * from priority;;
  id  | sort_order | description 
------+------------+-------------
 crtl |          1 | Critical
 high |          2 | High
 medm |          3 | Medium
 low  |          4 | Low
(4 rows)

     */
    
    private String img[] = {"fire.gif", "gyrophare.gif", "doctor.gif", "bandaid.gif"};
    
    public String filename() {
        Number priority=bug().priority().sortOrder();
        String result=null;
        if (priority!=null) {
            int c=priority.intValue();
            return img[c-1];
        }
        return result;
    }
}
