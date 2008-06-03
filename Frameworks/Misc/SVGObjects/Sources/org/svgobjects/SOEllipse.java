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
 * SVG <ellipse...></ellipse>
 * additional bindings of the form "$key" are placed in the style string.
 */
public class SOEllipse extends SODynamicElement {
    /*
     * constructors
     */
    public SOEllipse(String name, NSDictionary associations, WOElement element) {
        super(name, associations, element);

        // set the tag
        super.elementName = "ellipse";
    }
}