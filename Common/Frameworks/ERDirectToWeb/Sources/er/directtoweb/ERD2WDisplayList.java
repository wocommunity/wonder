/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import ERDCustomEditComponent;

public class ERD2WDisplayList extends ERDCustomEditComponent {

    public ERD2WDisplayList(WOContext context) {super(context);}
    
    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public NSArray listToDisplay() {
        return (NSArray)(object() instanceof NSArray ? object() : objectKeyPathValue());
    }
    
}
