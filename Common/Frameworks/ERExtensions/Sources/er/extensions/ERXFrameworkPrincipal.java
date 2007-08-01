//
// ERXFrameworkPrincipal.java
// Project ERExtensions
//
// Created by ak on Sat May 04 2002
//
package er.extensions;

import java.lang.reflect.Field;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.appserver.WOApplication;

/** 
 * Designated starter class for frameworks, adds support for dependency management.<br />
 * Allows you to disregard your framework order in the class path (at least where 
 * startup is concerned, if you override actual classes you still need to take care.)<br /><br />
 * The <code>initialize()</code> method will be called directly after your principal
 * is instantiated.<br />
 * The <code>finishInitialization()</code> method will be called when the app finishes 
 * startup but before it will begin to process requests.<br />
 * 
 * If you define <pre><code>public static Class[] REQUIRES = Class[] {...}</code></pre>
 * all the classes (which must be assignable from this class) will get 
 * loaded before your principal.<br />
 * 
 * NOTE: try to avoid putting code in static initializers. These may lead to 
 * unpredictable behaviour when launching. Use one of the methods above
 * to do what you need to do.<br /><br />
 * Here is an example:<pre><code>
 * public class ExampleFrameworkPrincipal extends ERXFrameworkPrincipal {
 * 
 *     public static final Logger log = Logger.getLogger(ExampleFrameworkPrincipal.class);
 * 
 *     protected static ExampleFrameworkPrincipal sharedInstance;
 *     
 *     public final static Class REQUIRES[] = new Class[] {ERXExtensions.class, ERDirectToWeb.class, ERJavaMail.class};
 * 
 *     // Registers the class as the framework principal
 *     static {
 *         setUpFrameworkPrincipalClass(ExampleFrameworkPrincipal.class);
 *     }
 * 
 *     public static ExampleFrameworkPrincipal sharedInstance() {
 *         if (sharedInstance == null) {
 *             sharedInstance = (ExampleFrameworkPrincipal)sharedInstance(ExampleFrameworkPrincipal.class);
 *         }
 *         return sharedInstance;
 *     }
 * 
 *     public void initialize() {
 *         // code during startup
 *     }
 * 
 *     public void finishInitialization() {
 *         // Initialized shared data
 *     }
 * }</code></pre>
 */
public abstract class ERXFrameworkPrincipal {

    /** logging support */
    //protected final Logger log = Logger.getLogger(getClass());
    private static final Logger log = Logger.getLogger(ERXFrameworkPrincipal.class);

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
                log.debug("Finished initialization after launch: " + principal);
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
            log.debug("Loaded items: " + initializedFrameworks);
            if(observer == null) {
                observer = new Observer();
                NSNotificationCenter center = NSNotificationCenter.defaultCenter();
                center.addObserver(observer,
                        new NSSelector("finishInitialization",  ERXConstant.NotificationClassArray),
                        WOApplication.ApplicationWillFinishLaunchingNotification,
                        //ERXApplication.ApplicationDidCreateNotification,
                        null);
               
            }
            if (initializedFrameworks.objectForKey(c.getName()) == null) {
                log.debug("Starting up: " + c.getName());
                try {
                    Field f = c.getField("REQUIRES");
                    Class requires[] = (Class[]) f.get(c);
                    for (int i = 0; i < requires.length; i++) {
                        Class requirement = requires[i];
                        setUpFrameworkPrincipalClass(requirement);
                    }
                } catch (NoSuchFieldException e) {
                    // nothing
                    // log.debug("No requirements: " + c.getName());
                } catch (IllegalAccessException e) {
                    log.error("Can't read field REQUIRES from " + c.getName() + ", check if it is 'public static Class[] REQUIRES= new Class[] {...}' in this class");
                    throw NSForwardException._runtimeExceptionForThrowable(e);
                }
                ERXFrameworkPrincipal principal = (ERXFrameworkPrincipal)c.newInstance();
                initializedFrameworks.setObjectForKey(principal,c.getName());
                principal.initialize();
                launchingFrameworks.addObject(principal);
                log.debug("Initialized : " + c.getName());

            } else {
                log.debug("Was already inited: " + c.getName());
            }
        } catch (InstantiationException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        } catch (IllegalAccessException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    /**
     * Called directly after the contructor.
     *
     */
    protected void initialize() {
        // empty
    }

    public ERXFrameworkPrincipal() {
        log.debug("Started initialization: " + getClass().getName());
    }
    
    /**
     * Overridden by subclasses to provide framework initialization.
     */
    public abstract void finishInitialization();
    
    public String toString() {
      return ERXStringUtilities.lastPropertyKeyInKeyPath(getClass().getName());
    }
}
