/*
 * WXOutlineRepetition.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WXOutlineRepetition extends WOComponent {

    public WXOutlineRepetition(WOContext aContext)  {
        super(aContext);
    }

    /////////////
    // No-Sync
    ////////////
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
}