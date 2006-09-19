//
// ERXFrameworkPrincipal.java
// Project ERExtensions
//
// Created by ak on Sat May 04 2002
//
package er.extensions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/** 
 * Designated starter class for frameworks.
 * the <code>finishInitialization</code> will be called when the app finishes startup.
 * To use, subclass it and set the NSPrincipalClass of your framework to the subclass. 
 * See the ERXCoreBusinessLogic and BTBusinessLogic frameworks for an example of usage.
 */
public abstract class ERXFrameworkPrincipal {

    /** logging support */
    private final Logger log = Logger.getLogger(getClass());

    /** holds the mapping between framework principals classes and ERXFrameworkPrincipal objects */
    private static NSMutableDictionary initializedFrameworks = new NSMutableDictionary();

    /**
     * Gets the shared framework principal instance for a given
     * class.
     * @param principal class for a given framework
     * @return framework principal initializer
     */
    public static ERXFrameworkPrincipal sharedInstance(Class c) {
        return (ERXFrameworkPrincipal)initializedFrameworks.objectForKey(c.getName());
    }
    
    /**
     * Sets up a given framework principal class to recieve notification
     * when it is safe for the framework to be initialized.
     * @param c principal class
     */
    public static void setUpFrameworkPrincipalClass(Class c) {
        try {
            if (initializedFrameworks.objectForKey(c.getName()) == null) {
                NSLog.out.appendln("Starting up: " + c.getName());
                ERXFrameworkPrincipal principal = (ERXFrameworkPrincipal)c.newInstance();
                //ERXRetainer.retain(principal);
                NSNotificationCenter center = NSNotificationCenter.defaultCenter();
                center.addObserver(principal,
                                   new NSSelector("finishInitialization",  ERXConstant.NotificationClassArray),
                                   WOApplication.ApplicationWillFinishLaunchingNotification,
                                   null);
                initializedFrameworks.setObjectForKey(principal,c.getName());
            } else {
                NSLog.out.appendln("Skipped: " + c.getName());
            }
        } catch (Throwable e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    /**
     * Notification method called when the WOApplication posts
     * the notification 'ApplicationWillFinishLaunching'. This
     * method handles de-registering for notifications and releasing
     * any references to observer so that it can be released for
     * garbage collection.
     * @param n notification that is posted after the WOApplication
     * 		has been constructed, but before the application is
     *		ready for accepting requests.
     */
    public final void finishInitialization(NSNotification n) {
        NSNotificationCenter.defaultCenter().removeObserver(this);
        //ERXRetainer.release(this);
        finishInitialization();
        log().info("Finished initialization after launch: " + getClass().getName());
    }

    public ERXFrameworkPrincipal() {
        NSLog.out.appendln("Started initialization: " + getClass().getName());
    }
    
    /**
     * Overridden by subclasses to provide framework initialization.
     */
    public abstract void finishInitialization();
    
    /**
     * Access to the logging mechanism
     * @return shared logging instance
     */
    public Logger log() {
        return log;
    }
}
