/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;

import com.webobjects.appserver.*;

/**
 * The regular NextPageDelegate interface from
 * d2w has hard coded the return type of WOComponent.
 * Sometimes you need to return a WOResponse instead
 * of a component. This interface solves this problem.
 */
// MOVEME: Might want to move this to ERD2W
// ENHANCEME: Might want this interface to extend NextPageDelegate, so that casting wise things would be fine.
public interface ERXNextPageDelegate  {
    public WOActionResults erNextPage(WOComponent sender);
}

// MOVEME: Should move to it's own class, most likely in ERD2W
abstract class ERXDictNextPageDelegate implements NextPageDelegate {
    private NSMutableDictionary _data = new NSMutableDictionary();
    public void takeValueForKey(Object value, Object key) { _data.setObjectForKey(value, key); }
    public Object valueForKey(Object key) { return _data.objectForKey(key); }
} 
