//
// Application.java
// Project Feedback
//
// Created by ravi on Fri Aug 10 2001
//
package org.svgobjects.examples.ecard;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;

public class Application extends WOApplication {
    
    public static void main(String argv[]) {
        WOApplication.main(argv, Application.class);
    }

    public Application() {
        super();
        System.out.println("Welcome to " + this.name() + "!");
        
        /* ** Put your application initialization code here ** */
	WORequestHandler directActionRequestHandler = requestHandlerForKey("wa");
	setDefaultRequestHandler(directActionRequestHandler);
    }
    
}