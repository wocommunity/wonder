package org.svgobjects.examples.componenttour;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.io.*;

public class Application extends WOApplication {
    protected NSDictionary defaults;

    /*
    * main
    */
    public static void main(String argv[]) {
        WOApplication.main(argv, Application.class);
    }

    /*
    * accessors
    */
    public NSDictionary defaults() {
	if (defaults == null) {	
	    InputStream inputStream = resourceManager().inputStreamForResourceNamed("Application.defaults", null, null);
	    NSData defaultsData = null;

	    // create the data, log exceptions
	    try { 
		defaultsData = new NSData(inputStream, inputStream.available()); 
		defaults = (NSDictionary) NSPropertyListSerialization.propertyListFromData(defaultsData);
	    } catch (Exception exception) {
		NSLog.out.appendln("Application:  failed to read in defaults");
	    }
	} return defaults;
    }
}