/*
 * JSImageFlyover.java
 * [JavaWOExtensions Project]
 *
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This code not the original code. */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import java.util.Random;

public class JSImageFlyover extends JSComponent {
    public String uniqueID;
   
    public JSImageFlyover(WOContext aContext)  {
        super(aContext);
    }

    public void awake() {
        // We need to give each image a unique name, with considerations that there might be
        // more than ImageFlyover per page.Be sure it is apositive number otherwise
        // we could create var names with "-" on them. 
        uniqueID = "Image"+Math.abs((new Random()).nextInt());
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
