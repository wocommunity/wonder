//
// Preview.java: Class file for WO Component 'Preview'
// Project eCards
//
// Created by ravi on Wed Aug 29 2001
//
package org.svgobjects.examples.ecard;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class Preview extends WOComponent {
    public String image;
    public String subject;
    public String message;

    public Preview(WOContext context) {
        super(context);
    }
    
    public NSDictionary queryDictionary() {
	return new NSDictionary(image, "image");
    }
}