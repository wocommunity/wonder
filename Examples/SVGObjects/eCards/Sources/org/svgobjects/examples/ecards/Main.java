//
// Main.java: Class file for WO Component 'Main'
// Project Feedback
//
// Created by ravi on Fri Aug 10 2001
//
package org.svgobjects.examples.ecard;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

public class Main extends WOComponent {
    public String from;
    public String subject;
    public String message;
    public NSArray images = new NSArray(new String[]{"leila.jpg", "snowboard.jpg"});
    public String image;
    public String to;

    public Main(WOContext context) {
        super(context);
    }
}