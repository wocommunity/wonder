/*
 * WXAccessControl.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

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