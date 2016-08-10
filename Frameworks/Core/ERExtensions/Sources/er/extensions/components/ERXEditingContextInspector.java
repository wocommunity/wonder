/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

import er.extensions.foundation.ERXStringUtilities;

/**
 * Inspects an eo's editing context.
 * 
 * @binding object The EOEditingContext to inspect
 * @binding item The EOEnterpriseObject to inspect
 * @binding key A string
 * @binding debugPageProvider
 */

public class ERXEditingContextInspector extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXEditingContextInspector(WOContext aContext) {
        super(aContext);
    }

    protected static NSArray _keys = new NSArray(new Object[] {"insertedObjects", "updatedObjects","deletedObjects","registeredObjects",});
    
    public NSArray keys() {
    	return _keys;
    }
  
    public EOEditingContext editingContext() {
    	return object;
    }
    public NSArray objectsForKey() {
    	return (NSArray)NSKeyValueCoding.Utility.valueForKey(editingContext(), key);
    }
    
    public String labelForKey() {
    	return ERXStringUtilities.displayNameForKey(key) + " (" + objectsForKey().count() + ")";
    }
    public Object extraInfo() {
    	return ("updatedObjects".equals(key) ? item.changesFromSnapshot(editingContext().committedSnapshotForObject(item)) : null);
    }
    
    public boolean showInitially() {
    	return !"registeredObjects".equals(key);
    }

    public String key;
    public EOEditingContext object;
    public EOEnterpriseObject item;
    public Object debugPageProvider;
}
