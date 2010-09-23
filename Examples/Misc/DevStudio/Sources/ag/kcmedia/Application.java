//
// Application.java
// Project ERD2WTemplate
//
// Created by ak on Sun Apr 21 2002
//
package ag.kcmedia;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eocontrol.EOEventCenter;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXConstant;

public class Application extends ERXApplication {

    static Logger log = Logger.getLogger(Application.class);
    
    public static void main(String argv[]) {
        ERXApplication.main(argv, Application.class);
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
