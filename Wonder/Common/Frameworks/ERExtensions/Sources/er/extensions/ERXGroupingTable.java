/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOComponent;

public class ERXGroupingTable extends ERXGroupingRepetition {

    public ERXGroupingTable(WOContext context) {
        super(context);
    }
    
    public boolean synchronizesVariablesWithBindings() { return false; }
    public boolean isStateless() { return true; }
}
