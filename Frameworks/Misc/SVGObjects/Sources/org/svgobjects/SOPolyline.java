/*
 * Copyright (c) 2001 ravi@svgobjects.com. All rights reserved.
 *
 * The code in this file are subject to the Artistic License:
 * http://www.svgobjects.com/documentation/reference/Copyright.html
 */
package org.svgobjects;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;

/*
 * SVG <polyline...></polyline>
 * additional bindings of the form "$key" are placed in the style string.
 */
public class SOPolyline extends SODynamicElement {
    /*
     * constructors
     */
    public SOPolyline(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);

        // set the tag
        super.elementName = "polyline";
    }
}