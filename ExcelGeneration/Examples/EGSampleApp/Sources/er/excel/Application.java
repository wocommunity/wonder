//
// Application.java
// Project ERD2WTemplate
//
// Created by ak on Sun Apr 21 2002
//
package er.excel;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.extensions.*;

public class Application extends ERXApplication {

    static ERXLogger log = ERXLogger.getERXLogger(Application.class);
    
    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
    }
    
    public NSDictionary data() {
    	NSDictionary dict = ERXDictionaryUtilities.dictionaryFromPropertyList("Styles", NSBundle.mainBundle());
    	return dict;
    }

    public Application() {
        super();
        setPageRefreshOnBacktrackEnabled(true);
        System.out.println("Welcome to " + this.name() + "!");
    }
}
