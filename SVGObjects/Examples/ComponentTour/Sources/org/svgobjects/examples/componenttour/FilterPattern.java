//
// FilterPattern.java
// Project ComponentTour
//
// Created by ravi on Sat Jun 16 2001
//
package org.svgobjects.examples.componenttour;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class FilterPattern extends WOComponent {
    
    public FilterPattern(WOContext context) {
        super(context);
    }

    /*
    * accessors
    */
    public String coloursString() {
        NSArray colours = (NSArray) valueForKeyPath("application.defaults.colours");
	return colours.componentsJoinedByString(";");
    }
    
    /*
    * request/response
    */
    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
        response.setHeader("image/svg-xml", "Content-Type");
    }
}
