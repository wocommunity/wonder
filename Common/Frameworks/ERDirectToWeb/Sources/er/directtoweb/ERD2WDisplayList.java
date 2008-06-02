/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXArrayUtilities;

/**
 * Used to display a an NSArray of the form "A, B and C", useful for toMany relationships  or propertyKeys that return arrays.<br />
 * RENAMEME: ERDDisplayList
 */

public class ERD2WDisplayList extends ERDCustomEditComponent {

    public ERD2WDisplayList(WOContext context) { super(context); }
    
    public boolean isStateless() { return true; }
    public boolean synchronizesVariablesWithBindings() { return false; }

    public NSArray listToDisplay() {
        NSArray objects = (NSArray)(object() instanceof NSArray ? object() : objectKeyPathValue());
        if(objects != null) {
            String sortKey = (String)valueForBinding("sortKey");
            if(sortKey != null) {
                objects = ERXArrayUtilities.sortedArraySortedWithKey(objects, sortKey);
            }
        } else {
            objects = NSArray.EmptyArray;
        }
        return objects;
    }
}
