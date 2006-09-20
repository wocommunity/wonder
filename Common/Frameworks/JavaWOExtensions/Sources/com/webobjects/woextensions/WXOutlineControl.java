/*
 * WXOutlineControl.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WXOutlineControl extends WOComponent {
    protected int _anchor;

    protected static int _counter = 0;

    public WXOutlineControl(WOContext aContext)  {
        super(aContext);
        // just a hack to get a unique anchor in a thread safe manner.
        synchronized(WOApplication.application()) {
            _counter++;
            _anchor = _counter;
        }
    }

    /////////////
    // No-Sync
    ////////////
    public boolean synchronizesVariablesWithBindings() {
        return false;
    }
    
    public int fragmentIdentifier() { return _anchor; }

    public WXOutlineEntry currentEntry() {
        return (WXOutlineEntry)session().objectForKey("_outlineEntry");
    }

    public int indentation() {
        return currentEntry().nestingLevel() * 20;
    }

    public String currentToggleImageName() {
        return (currentEntry().isExpanded()) ? "DownTriangle.gif": "RightTriangle.gif";
    }
}
