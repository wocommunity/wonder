//
// Application.java
// Project ERD2WTemplate
//
// Created by ak on Sun Apr 21 2002
//
package ag.kcmedia;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import er.extensions.*;

public class Application extends ERXApplication {

    static ERXLogger log = ERXLogger.getERXLogger(Application.class);
    
    public static void main(String argv[]) {
        WOApplication.main(argv, Application.class);
    }

    public void didFinishedLaunching(NSNotification n) {
    }
    
    public Application() {
        super();
        setPageRefreshOnBacktrackEnabled(true);
        EOEventCenter.setPassword("4Events");
        setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
        statisticsStore().setPassword("4Stats");
        NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("didFinishedLaunching", ERXConstant.NotificationClassArray), WOApplication.ApplicationDidFinishLaunchingNotification, null);
        /* ** Put your application initialization code here ** */
        System.out.println("Welcome to " + this.name() + "!");
    }
    
}
