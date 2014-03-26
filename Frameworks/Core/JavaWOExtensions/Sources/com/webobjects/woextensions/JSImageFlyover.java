/*
 * JSImageFlyover.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class JSImageFlyover extends JSComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public String uniqueID;
   
    public JSImageFlyover(WOContext aContext)  {
        super(aContext);
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        // We need to give each image a unique name, with considerations that there might be
        // more than one ImageFlyover per page.

    	StringBuilder uniqueIDBuffer = new StringBuilder("ImageFlyover");
        uniqueIDBuffer.append(context.contextID());
        uniqueIDBuffer.append('_');
        uniqueIDBuffer.append(context.elementID().replace('.', '_'));
        uniqueID = uniqueIDBuffer.toString();
        super.appendToResponse(response, context);

    }

    protected String _url(String binding) {
        String aFilename = (String)_WOJExtensionsUtil.valueForBindingOrNull(binding,this);
        String source = application().resourceManager().urlForResourceNamed(aFilename, framework(), session().languages(), context().request());
        return source;
    }
    
    public String mouseOver() {
        // What to do when the mouse moves over the image ...
        // Return the image source name
        return uniqueID+".src='"+_url("selectedImage")+"'";
    }

    public String mouseOut() {
        // What to do when the mouse moves off the image ...
        // Return the image source name
        return uniqueID+".src='"+_url("unselectedImage")+"'";
    }

    @Override
    public String imageLocation() {
        // Return the base image (what the hyperlink starts with) to the WOImage
        return _url("unselectedImage");
    }
}
