//
// SenderEmail.java: Class file for WO Component 'SenderEmail'
// Project eCards
//
// Created by ravi on Wed Aug 29 2001
//
package org.svgobjects.examples.ecard;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class SenderEmail extends WOComponent {
    public String to;
    public String subject;
    public String message;
    public String image;

    public SenderEmail(WOContext context) {
        super(context);
    }
}