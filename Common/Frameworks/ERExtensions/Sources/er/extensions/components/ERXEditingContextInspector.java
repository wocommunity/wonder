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
 * Inspects an eo's editing context.*<br />
 * 
 * @binding object
 */

public class ERXEditingContextInspector extends WOComponent {

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
