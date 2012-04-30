/*
 * JSConfirmPanel.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

public class JSConfirmPanel extends JSAlertPanel {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public JSConfirmPanel(WOContext aContext)  {
        super(aContext);
    }

    public String confirmJSMessage() {

        String theMessage = (String)_WOJExtensionsUtil.valueForBindingOrNull("confirmMessage",this);

            // Put in a default message if one was not provided
        if (theMessage==null) {
            theMessage = "Are you sure you want to do this?";

        } else {
            // Strip out the tags in the message that will mess things up - like apostrophes and quotes
    theMessage = NSArray.componentsSeparatedByString(theMessage, "'").componentsJoinedByString("");
    theMessage = NSArray.componentsSeparatedByString(theMessage, "\"").componentsJoinedByString("");
        }	

        // Return the opening string for the Javascript function
        return "return confirm('"+theMessage+"')";

    }
}
