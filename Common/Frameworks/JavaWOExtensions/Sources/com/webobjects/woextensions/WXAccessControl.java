/*
 * WXAccessControl.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This code not the original code. */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

public class WXAccessControl extends WOComponent {

    public WXAccessControl(WOContext aContext)  {
        super(aContext);
    }

    /////////////
    // No-Sync
    ////////////
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }

    public boolean shouldShow() {
        NSDictionary permissions = (NSDictionary)session().valueForKey("permissions");
        if (permissions!=null) {
            return ((Boolean)permissions.valueForKey((String)valueForBinding("key"))).booleanValue();
        }
        return true;
    }
}