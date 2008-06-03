/*
 * JSImageFlyover.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

public class JSImageFlyover extends JSComponent {
    public String uniqueID;
   
    public JSImageFlyover(WOContext aContext)  {
        super(aContext);
    }

    public void appendToResponse(WOResponse response, WOContext context) {
        // We need to give each image a unique name, with considerations that there might be
        // more than one ImageFlyover per page.

        StringBuffer uniqueIDBuffer = new StringBuffer("ImageFlyover");
        uniqueIDBuffer.append(context.contextID());
        uniqueIDBuffer.append("_");
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

    public String imageLocation() {
        // Return the base image (what the hyperlink starts with) to the WOImage
        return _url("unselectedImage");
    }
}
