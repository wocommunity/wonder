/*
 * JSAlertPanel.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

public class JSAlertPanel extends JSComponent {
    
    public JSAlertPanel(WOContext aContext)  {
        super(aContext);
    }

    public String alertJSMessage() {

        String theMessage = (String)_WOJExtensionsUtil.valueForBindingOrNull("alertMessage",this);

            // Put in a default message if one was not provided
        if (theMessage==null) {

                    theMessage = "Done.";

            } else {

                    // Strip out the tags in the message that will mess things up - like apostrophes and quotes
            theMessage = NSArray.componentsSeparatedByString(theMessage, "'").componentsJoinedByString("");
            theMessage = NSArray.componentsSeparatedByString(theMessage, "\"").componentsJoinedByString("");
            }	

            // Return the opening string for the Javascript function
        return "return alert('"+theMessage+"')";

    }

    public boolean isImage() {

           // If the user specified an image name, return YES
        return (valueForBinding("filename")!=null);
    }

    public boolean isText() {

           // If the user specified a hyperlink string, return YES
        return (valueForBinding("string")!=null);
    }

    public boolean isImageAndText() {

           // If the user specified a hyperlink string AND and image, return YES
        return isImage() && isText();
    }
}
