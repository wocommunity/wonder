/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
 * Conditional component that tests if a given item is contained
 * in an {@link com.webobjects.foundation.NSArray}.
 * <br/>
 * Synopsis:<br/>
 * item=<i>anItem</i>;list=<i>aList</i>;[negate=<i>aBoolean</i>;]
 * 
 * @binding list array of objects
 * @binding item object to test inclusion in the list
 * @binding negate inverts the sense of the conditional.
 */
// ENHANCEME: Should support java.util.List interface
public class ERXListContainsItemConditional extends WOComponent {

    /**
     * Public constructor.
     * @param aContext a context
     */
    public ERXListContainsItemConditional(WOContext aContext) {
        super(aContext);
    }

    /**
     * Component is stateless
     * @return true
     */
    public boolean isStateless() { return true; }

    /**
     * Tests if the bound item is contained within the
     * bound list.
     * @return result of comparision
     */
    // ENHANCEME: Should support the List interface
    public boolean listContainsItem() {
        NSArray list=(NSArray)valueForBinding("list");
        Object item=valueForBinding("item");
        return item != null && list != null && list.containsObject(item);
    }
}
