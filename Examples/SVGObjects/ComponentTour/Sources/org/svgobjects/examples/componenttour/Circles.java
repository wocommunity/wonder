//
// Circles.java
// Project ComponentTour
//
// Created by ravi on Sat Jun 16 2001
//
package org.svgobjects.examples.componenttour;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class Circles extends WOComponent {

    public Circles(WOContext context) {
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