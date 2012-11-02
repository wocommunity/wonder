/*
 * WXOutlineControl.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;

public class WXOutlineControl extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
    @Override
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
