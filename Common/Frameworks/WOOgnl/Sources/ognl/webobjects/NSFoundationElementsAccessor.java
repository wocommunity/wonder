/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* NSFoundationElementsAccessor.java created by max on Fri 28-Sep-2001 */
package ognl.webobjects;

import com.webobjects.foundation.*;
import java.util.Enumeration;
import ognl.ElementsAccessor;

public class NSFoundationElementsAccessor implements ElementsAccessor {

    // FIXME: Should instead use a slightly more dynamic dispatch so that any object that implements
    //	the method objectEnumerator() can use this elements accessor.
    public Enumeration getElements(Object target) {
        if (target instanceof NSArray)
            return ((NSArray)target).objectEnumerator();
        if (target instanceof NSDictionary)
            return ((NSDictionary)target).objectEnumerator();
        if (target instanceof NSSet)
            return ((NSSet)target).objectEnumerator();
        throw new RuntimeException("NSFoundationElementsAccessor being used with a non-foundation class: " + target.getClass().getName());
    }
}
