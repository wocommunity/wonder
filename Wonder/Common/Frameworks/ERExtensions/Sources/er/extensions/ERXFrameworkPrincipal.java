//
// ERXFrameworkPrincipal.java
// Project ERExtensions
//
// Created by ak on Sat May 04 2002
//

package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import org.apache.log4j.Category;

public abstract class ERXFrameworkPrincipal {
    public static final Category cat = Category.getInstance(ERXFrameworkPrincipal.class);

    private static NSMutableDictionary initializedFrameworks = new NSMutableDictionary();

    public static ERXFrameworkPrincipal sharedInstance(Class c) {
        return (ERXFrameworkPrincipal)initializedFrameworks.objectForKey(c.getName());
    }
    
    public static void setUpFrameworkPrincipalClass(Class c) {
        try {
            if (initializedFrameworks.objectForKey(c.getName()) != null) {
                cat.debug("Starting up: " + c.getName());
                ERXFrameworkPrincipal principal = (ERXFrameworkPrincipal)c.newInstance();
                ERXRetainer.retain(principal);
                NSNotificationCenter center = NSNotificationCenter.defaultCenter();
                center.addObserver(principal,
                                   new NSSelector("finishInitialization",  ERXConstant.NotificationClassArray),
                                   WOApplication.ApplicationWillFinishLaunchingNotification,
                                   null);
                initializedFrameworks.setObjectForKey(principal,c.getName());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void finishInitialization(NSNotification n) {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        ERXRetainer.release(this);
        finishInitialization();
    }

    public abstract void finishInitialization();
    
    public Category log() {
        return cat;
    }
}
