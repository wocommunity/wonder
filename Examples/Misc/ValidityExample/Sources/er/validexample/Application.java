package er.validexample;
//
// Application.java
// Project ValidityExample
//
// Created by msacket on Mon Jun 11 2001
//

import er.extensions.appserver.ERXApplication;

public class Application extends ERXApplication {
    
    public static void main(String argv[]) {
    	ERXApplication.main(argv, Application.class);
    }

    public Application() {
        super();
        System.out.println("Welcome to " + this.name() + "!");
        
        /* ** Put your application initialization code here ** */
    }    
}
