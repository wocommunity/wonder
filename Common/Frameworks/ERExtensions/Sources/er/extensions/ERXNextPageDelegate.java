/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERNextPageDelegate.java created by patrice on Fri 17-Nov-2000 */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;

public interface ERXNextPageDelegate  {
    public WOActionResults erNextPage(WOComponent sender);
}

abstract class ERXDictNextPageDelegate implements NextPageDelegate {
    private NSMutableDictionary _data = new NSMutableDictionary();
    public void takeValueForKey(Object value, Object key) { _data.setObjectForKey(value, key); }
    public Object valueForKey(Object key) { return _data.objectForKey(key); }
} 
