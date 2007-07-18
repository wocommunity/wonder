//
// Application.java
// Project ERD2WTemplate
//
// Created by ak on Sun Apr 21 2002
//
package er.excel;

import org.apache.log4j.*;

import com.webobjects.foundation.*;

import er.extensions.*;

public class Application extends ERXApplication {

    static Logger log = Logger.getLogger(Application.class);
    
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
