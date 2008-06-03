//
// Picture.java: Class file for WO Component 'Picture'
// Project eCards
//
// Created by ravi on Thu Aug 30 2001
//
package org.svgobjects.examples.ecard;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class Picture extends WOComponent {
    public String image;

    public Picture(WOContext context) {
        super(context);
    }

    public void appendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);

        response.setHeader("image/svg-xml", "Content-Type");
    }
}
