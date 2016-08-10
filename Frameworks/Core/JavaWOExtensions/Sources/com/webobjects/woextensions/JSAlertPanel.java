/*
 * JSAlertPanel.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

public class JSAlertPanel extends JSComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
    
    public JSAlertPanel(WOContext aContext)  {
        super(aContext);
    }

    /**
     * <span class="ja">alert メッセージ</span>
     */
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

    /**
     * <span class="ja">イメージの指定があれば YES が戻ります。</span>
     */
    public boolean isImage() {

           // If the user specified an image name, return YES
        return (valueForBinding("filename")!=null);
    }

    /** 
     * <span class="ja">テキストの指定があれば YES が戻ります。 </span>
     */
    public boolean isText() {

           // If the user specified a hyperlink string, return YES
        return (valueForBinding("string")!=null);
    }

    /**
     * <span class="ja">イメージ＆テキストの指定があれば YES が戻ります。</span>
     */
    public boolean isImageAndText() {

           // If the user specified a hyperlink string AND and image, return YES
        return isImage() && isText();
    }
}
