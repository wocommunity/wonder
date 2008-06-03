package org.svgobjects.examples.ezforms;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.appserver.*;

public class Application extends WOApplication {
    public NSArray accountTypes = Account.Types;

    /*
    * main
    */
    public static void main(String argv[]) {
	WOApplication.main(argv, Application.class);
    }
}