/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class ERXNonZeroConditional extends WOComponent {

    public ERXNonZeroConditional(WOContext aContext) {
        super(aContext);
    }
    
    public boolean synchronizesBindingsWithVariables() { return false; }
    public boolean isStateless() { return true; }
    
    public boolean isNonZero() {
        Object binding=valueForBinding("condition");
        return binding==null || !binding.equals(ERXConstant.ZeroInteger);
    }
}
