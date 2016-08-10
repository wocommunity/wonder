/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.relationships;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.foundation.ERXArrayUtilities;

/**
 * Used to display a an NSArray of the form "A, B and C", useful for toMany relationships  or propertyKeys that return arrays.
 */
// TODO rename to ERDDisplayList
public class ERD2WDisplayList extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERD2WDisplayList(WOContext context) { super(context); }

    @Override
    public boolean isStateless() { return true; }

    @Override
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
