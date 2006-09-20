//
// ERXFrameworkPrincipal.java
// Project ERExtensions
//
// Created by ak on Sat May 04 2002
//
package er.extensions;

import java.lang.reflect.*;
import java.util.*;

import org.apache.log4j.*;

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
    private static final NSMutableDictionary  initializedFrameworks = new NSMutableDictionary();
    private static final NSMutableArray  launchingFrameworks = new NSMutableArray();

    public static class Observer {
        
        /**
         * Notification method called when the WOApplication posts
         * the notification 'ApplicationWillFinishLaunching'. This
         * method handles de-registering for notifications and releasing
         * any references to observer so that it can be released for
         * garbage collection.
         * @param n notification that is posted after the WOApplication
         *      has been constructed, but before the application is
         *      ready for accepting requests.
         */
        public final void finishInitialization(NSNotification n) {
            NSNotificationCenter.defaultCenter().removeObserver(this);
            for (Enumeration enumerator = launchingFrameworks.objectEnumerator(); enumerator.hasMoreElements();) {
                ERXFrameworkPrincipal principal = (ERXFrameworkPrincipal) enumerator.nextElement();
                principal.finishInitialization();
                NSLog.debug.appendln("Finished initialization after launch: " + principal);
            }
        }

    }
    
    private static Observer observer;
    
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
            NSLog.debug.appendln("Loaded items: " + initializedFrameworks);
            if(observer == null) {
                observer = new Observer();
                NSNotificationCenter center = NSNotificationCenter.defaultCenter();
                center.addObserver(observer,
                        new NSSelector("finishInitialization",  ERXConstant.NotificationClassArray),
                        WOApplication.ApplicationWillFinishLaunchingNotification,
                        null);
               
            }
            if (initializedFrameworks.objectForKey(c.getName()) == null) {
                NSLog.debug.appendln("Starting up: " + c.getName());
                try {
                    Field f = c.getField("REQUIRES");
                    Class requires[] = (Class[]) f.get(c);
                    for (int i = 0; i < requires.length; i++) {
                        Class requirement = requires[i];
                        setUpFrameworkPrincipalClass(requirement);
                    }
                } catch (NoSuchFieldException e) {
                    // nothing
                    // NSLog.debug.appendln("No requirements: " + c.getName());
                } catch (IllegalAccessException e) {
                    NSLog.err.appendln("Can't read field REQUIRES from " + c.getName() + ", check if it is 'public static Class[] REQUIRES= new Class[] {...}' in this class");
                    NSForwardException._runtimeExceptionForThrowable(e);
                }
                ERXFrameworkPrincipal principal = (ERXFrameworkPrincipal)c.newInstance();
                initializedFrameworks.setObjectForKey(principal,c.getName());
                launchingFrameworks.addObject(principal);
                principal.initialize();
                NSLog.debug.appendln("Initialized : " + c.getName());

            } else {
                NSLog.debug.appendln("Was already inited: " + c.getName());
            }
        } catch (InstantiationException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        } catch (IllegalAccessException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    protected void initialize() {
        // empty
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
