/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components.javascript;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Useful for creating a javascript window for a form submit.
 * @binding multipleSubmit true the form multiple submit
 * @binding targetDictionary dictionary (optionally) containing
 *  <li>width - width of the window
 *  <li>targetName - name of the target window
 *  <li>height - height of the target window
 *  <li>scrollbars - NO/false if you don't want scrollbars 
 */
public class ERXJSFormForTarget extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXJSFormForTarget(WOContext aContext) {
        super(aContext);
    }

    public Boolean multipleSubmit;
    public NSDictionary targetDictionary;
    
    public String targetString(){
        String result = "";
        if(targetDictionary != null){
            StringBuilder buffer = new StringBuilder();
            buffer.append( targetDictionary.valueForKey("targetName")!=null ?
                           targetDictionary.valueForKey("targetName") : "foobar");
            buffer.append(":width=");
            buffer.append( targetDictionary.valueForKey("width")!=null ?
                           targetDictionary.valueForKey("width") : "{window.screen.width/2}");
            buffer.append(", height=");
            buffer.append( targetDictionary.valueForKey("height")!=null ?
                           targetDictionary.valueForKey("height") : "{myHeight}");
            buffer.append(',');
            buffer.append( ERXValueUtilities.booleanValueWithDefault(targetDictionary.valueForKey("scrollbars"), true) ? " " : "scrollbars");
            buffer.append(", {(isResizable)?'resizable':''}, status");
            //System.out.println("buffer = "+buffer.toString());
            result = buffer.toString();
        }else{
            result = "foobar:width={window.screen.width/2}, height={myHeight}, scrollbars, {(isResizable)?'resizable':''}, status";
        }
        return result;
    }
}
