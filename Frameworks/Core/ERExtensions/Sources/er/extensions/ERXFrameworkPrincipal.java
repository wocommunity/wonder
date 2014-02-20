//
// ERXFrameworkPrincipal.java
// Project ERExtensions
//
// Created by ak on Sat May 04 2002
//
package er.extensions;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXConstant;
import er.extensions.foundation.ERXStringUtilities;

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
    protected final Logger log = Logger.getLogger(getClass());

    /** holds the mapping between framework principals classes and ERXFrameworkPrincipal objects */
    protected static final NSMutableDictionary<String, ERXFrameworkPrincipal> initializedFrameworks = new NSMutableDictionary<String, ERXFrameworkPrincipal>();
    protected static final NSMutableArray<ERXFrameworkPrincipal> launchingFrameworks = new NSMutableArray<ERXFrameworkPrincipal>();

    public static class Observer {
        
        /**
         * Notification method called when the WOApplication posts
         * the notification 'ApplicationDidCreateNotification'. This
         * method handles de-registering for notifications and releasing
         * any references to observer so that it can be released for
         * garbage collection.
         * @param n notification that is posted after the WOApplication
         *      has been constructed, but before the application is
         *      ready for accepting requests.
         */
        public final void willFinishInitialization(NSNotification n) {
            NSNotificationCenter.defaultCenter().removeObserver(this, ERXApplication.ApplicationDidCreateNotification, null);
            for (ERXFrameworkPrincipal principal : launchingFrameworks) {
                principal.finishInitialization();
                ERXApplication.log.debug("Finished initialization after launch: " + principal);
            }
        }
        
        /**
         * Notification method called when the WOApplication posts
         * the notification 'ApplicationDidFinishInitializationNotification'. This
         * method handles de-registering for notifications and releasing
         * any references to observer so that it can be released for
         * garbage collection.
         * @param n notification that is posted after the WOApplication
         *      has been constructed, but before the application is
         *      ready for accepting requests.
         */
        public final void didFinishInitialization(NSNotification n) {
            NSNotificationCenter.defaultCenter().removeObserver(this);
            for (ERXFrameworkPrincipal principal : launchingFrameworks) {
                principal.didFinishInitialization();
            }
        }
    }
    
    private static Observer observer;
    
    /**
     * Gets the shared framework principal instance for a given
     * class.
     * @param c principal class for a given framework
     * @return framework principal initializer
     */
    public static<T extends ERXFrameworkPrincipal> T sharedInstance(Class<T> c) {
        return (T)initializedFrameworks.objectForKey(c.getName());
    }
    
    /**
     * Sets up a given framework principal class to receive notification
     * when it is safe for the framework to be initialized.
     * @param c principal class
     */
    public static void setUpFrameworkPrincipalClass(Class c) {
        if (initializedFrameworks.objectForKey(c.getName()) != null) {
        	return;
        }
        try {
        	// NSLog.debug.appendln("Loaded items: " + initializedFrameworks);
            if(observer == null) {
                observer = new Observer();
                NSNotificationCenter center = NSNotificationCenter.defaultCenter();
                center.addObserver(observer,
                        new NSSelector("willFinishInitialization",  ERXConstant.NotificationClassArray),
                        // WOApplication.ApplicationWillFinishLaunchingNotification,
                        ERXApplication.ApplicationDidCreateNotification,
                        null);
                center.addObserver(observer,
                        new NSSelector("didFinishInitialization",  ERXConstant.NotificationClassArray),
                        // WOApplication.ApplicationWillFinishLaunchingNotification,
                        ERXApplication.ApplicationDidFinishInitializationNotification,
                        null);
            }
            if (initializedFrameworks.objectForKey(c.getName()) == null) {
            	// NSLog.debug.appendln("Starting up: " + c.getName());
                try {
                    Field f = c.getField("REQUIRES");
                    Class requires[] = (Class[]) f.get(c);
                    for (int i = 0; i < requires.length; i++) {
                    	Class requirement = requires[i];
                    	if(initializedFrameworks.objectForKey(requirement.getName()) == null) {
                    		// NSLog.debug.appendln("Loading required: " + requirement.getName());
                    		setUpFrameworkPrincipalClass(requirement);
                    	}
                    }
                } catch (NoSuchFieldException e) {
                    // nothing
                    // NSLog.debug.appendln("No requirements: " + c.getName());
                } catch (IllegalAccessException e) {
                    ERXApplication.log.error("Can't read field REQUIRES from " + c.getName() + ", check if it is 'public static Class[] REQUIRES= new Class[] {...}' in this class");
                    throw NSForwardException._runtimeExceptionForThrowable(e);
                }
                if(initializedFrameworks.objectForKey(c.getName()) == null) {
                	ERXFrameworkPrincipal principal = (ERXFrameworkPrincipal)c.newInstance();
                	initializedFrameworks.setObjectForKey(principal,c.getName());
                	principal.initialize();
                	launchingFrameworks.addObject(principal);
                	ERXApplication.log.debug("Initialized : " + c.getName());
                }

            } else {
            	ERXApplication.log.debug("Was already inited: " + c.getName());
            }
        } catch (InstantiationException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        } catch (IllegalAccessException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    /**
     * Called directly after the constructor.
     */
    protected void initialize() {
        // empty
    }

    public ERXFrameworkPrincipal() {
        // NSLog.debug.appendln("Started initialization: " + getClass().getName());
    }
    
    /**
     * Overridden by subclasses to provide framework initialization.
     */
    public abstract void finishInitialization();
    
    /**
     * Overridden by subclasses to finalize framework initialization.
     */
    public void didFinishInitialization() {
    	// Do nothing
    }
    
    @Override
    public String toString() {
      return ERXStringUtilities.lastPropertyKeyInKeyPath(getClass().getName());
    }
    
  /**
   * <span class="ja">
   * 指定フレームワークがインストールされているかどうかを確認します。
   * 
   * @return ある場合には true が戻ります。
   * </span>
   */
  public static boolean hasFrameworkInstalled(String frameworkName) {
    if(ERXStringUtilities.stringIsNullOrEmpty(frameworkName)) {
      return false;
    }

    for (ERXFrameworkPrincipal frameworkPrincipal : ERXFrameworkPrincipal.launchingFrameworks) {
      String s = frameworkPrincipal.toString();
      if(frameworkName.equalsIgnoreCase(s)) {
        return true;
      }
    }

    return false;
  }

}
