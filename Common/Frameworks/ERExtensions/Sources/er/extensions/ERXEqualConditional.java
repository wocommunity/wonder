/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class ERXEqualConditional extends WOComponent {

    public ERXEqualConditional(WOContext aContext) {
        super(aContext);
    }
    
    public boolean synchronizesBindingsWithVariables() { return false; }
    public boolean isStateless() { return true; }
    
    public boolean areEqual() {
        Object v1=valueForBinding("value1");
        Object v2=valueForBinding("value2");
        return v1==v2 || (v1!=null && v2!=null && v1.equals(v2));
    }
}
