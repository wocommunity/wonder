//
// EZ1040.java: Class file for WO Component 'EZ1040'
// Project EZForms
//
// Created by ravi on Tue Jun 19 2001
//
package org.svgobjects.examples.ezforms;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class EZ1040 extends WOComponent {
    public Applicant contact;
    
    public EZ1040(WOContext context) {
        super(context);
    }

    /*
    * request/response
    */
    public void appendToResponse(WOResponse response,  WOContext context) {
	super.appendToResponse(response, context);
    
	// set the header
	response.setHeader("image/svg-xml", "Content-Type");
    }
}