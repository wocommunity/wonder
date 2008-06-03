//
// RecipientEmail.java: Class file for WO Component 'RecipientEmail'
// Project eCards
//
// Created by ravi on Wed Aug 29 2001
//
package org.svgobjects.examples.ecard;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class RecipientEmail extends WOComponent {
    public String from;
    public String subject;
    public String message;
    public String image;

    public RecipientEmail(WOContext context) {
        super(context);
    }

}
