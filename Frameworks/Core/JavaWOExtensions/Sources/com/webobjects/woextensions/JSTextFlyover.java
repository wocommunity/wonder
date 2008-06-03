/*
 * JSTextFlyover.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOContext;


public class JSTextFlyover extends JSComponent {
    public JSTextFlyover(WOContext aContext)  {
        super(aContext);
    }

    public String mouseOver() {
        // Return the highlighted color when the mouse is over the text
        return "window.event.srcElement.style.color = '"+valueForBinding("selectedColor")+"'";
    }


    public String mouseOut() {
        // Return the un-highlighted color when the mouse leaves
        return "window.event.srcElement.style.color = '"+valueForBinding("unselectedColor")+"'";
    }
}
