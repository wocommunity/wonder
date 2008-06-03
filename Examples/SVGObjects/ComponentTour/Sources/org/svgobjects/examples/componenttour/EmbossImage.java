//
// EmbossImage.java
// Project ComponentTour
//
// Created by ravi on Sat Jun 16 2001
//
package org.svgobjects.examples.componenttour;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class EmbossImage extends WOComponent {

    public EmbossImage(WOContext context) {
        super(context);
    }

    /*
    * request/response
    */
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        response.setHeader("image/svg-xml", "Content-Type");
    }
}
