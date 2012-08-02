/*
 * JSTextFlyover.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOContext;


public class JSTextFlyover extends JSComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
