/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.delegates;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.NextPageDelegate;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * The regular NextPageDelegate interface from
 * d2w has hard coded the return type of WOComponent.
 * Sometimes you need to return a WOResponse instead
 * of a component. This interface solves this problem.
 * Alternately, you could return a ERXResponseComponent.
 */

public interface ERDNextPageDelegate extends NextPageDelegate {
    public WOActionResults erNextPage(WOComponent sender);
}

// MOVEME: Should move to it's own class.
abstract class ERDDictNextPageDelegate implements NextPageDelegate {
    private NSMutableDictionary _data = new NSMutableDictionary();
    public void takeValueForKey(Object value, Object key) { _data.setObjectForKey(value, key); }
    public Object valueForKey(Object key) { return _data.objectForKey(key); }
} 
