/*
 * JSModalWindow.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOContext;


public class JSModalWindow extends JSComponent {
    public JSModalWindow(WOContext aContext)  {
        super(aContext);
    }


    public String contextComponentActionURL(){
        // Return the javascript function to the HREF, getting the URL for the action
        // from the invokeAction setting in the context
        return "window.open('"+context().componentActionURL()+"','"+valueForBinding("windowName")+"','"+windowInfo()+"'); return false";
    }

    public String windowInfo() {

            // Generate the javascript window details
        return "toolbar="+((null!=valueForBinding("showToolbar")) ? "yes" : "no")+
        ",location="+((null!=valueForBinding("showLocation")) ? "yes" : "no")+
        ",status="+((null!=valueForBinding("showStatus")) ? "yes" : "no")+
        ",menubar="+((null!=valueForBinding("showMenubar")) ? "yes" : "no")+
        ",resizable="+((null!=valueForBinding("isResizable")) ? "yes" : "no")+
        ",scrollbars="+((null!=valueForBinding("showScrollbars")) ? "yes" : "no")+
        ",width="+valueForBinding("width")+
        ",height="+valueForBinding("height");
    }
}
