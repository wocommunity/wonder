
package er.extensions.appserver;

import java.util.HashSet;
import java.util.Set;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;


/**
 * Use this to register shutdown hooks instead of directly using Runtime.addShutdownHook().
 * The net effect is that there will be a specific log file entry AFTER all other shutdown
 * hooks have completed, notifying a watching user that shutdown of the application has
 * indeed completed, which says:
 * <p>
 * <tt>APPLICATION SHUTDOWN SEQUENCE COMPLETE</tt>
 * <p>
 * on a single line. After you see this line in the application's log file, you can be
 * pretty sure that the process is indeed terminated. This notification works even if
 * there are no other shutdown hooks registered, if you ensure that this class is
 * loaded at all, e.g. by calling the no-op {@link #useMe()} method somewhere
 * ({@link ERXApplication} does this for you if you extend that).
 * <p>
 * Usage (e.g. in your Application class constructor):
 * <pre><code>
 * new ERXShutdownHook() {
 *     {@literal @}Override
 *     public void hook() {
 *         // do something
 *     }
 * };
 * </code></pre>
 *
 * <h3>CAUTION</h3>
 * You should not use this class when deploying the application as a J2EE servlet as it may interfere with other servlets running in the same VM. 
 * To disable this feature you have to provide the following start parameter to the java VM:
 * 
 * <code>
 * -Der.extensions.ERXApplication.enableERXShutdownHook=false
 * </code>
 *
 * @author Maik Musall, maik@selbstdenker.ag
 *
 */
public abstract class ERXShutdownHook extends Thread {

	static final Set<ERXShutdownHook> ALL_HOOKS = new HashSet<ERXShutdownHook>();
	
	public static void initERXShutdownHook() {
		System.out.println( "WILL ADD SHUTDOWNHOOK" );
		Runtime.getRuntime().addShutdownHook( new Thread() {
			@Override
			public void run() {
				try {
					synchronized( ALL_HOOKS ) {
						while( ALL_HOOKS.size() > 0 ) {
							// Use System.out to minimize dependencies
							System.out.println( "ShutdownHook waiting for " + ALL_HOOKS.size() + " hook" + (ALL_HOOKS.size() > 1 ? "s" : "") + " to complete" );
							ALL_HOOKS.wait();
						}

						if ( ! ERXApplication.erxApplication().getIsTerminating()) {
							NSNotificationCenter.defaultCenter().postNotification(new NSNotification(ERXApplication.ApplicationWillTerminateNotification, NSKeyValueCoding.NullValue));
						}

						System.out.println( "APPLICATION SHUTDOWN SEQUENCE COMPLETE" );
					}
				} catch( Exception e ) {
					e.printStackTrace();
				}
			}
		} );
	}
	
	private String name;

	/**
	 * Call this in your app constructor if you have no other shutdown hooks. If you don't call
	 * anything, this class will not be loaded at all and won't work.
	 * 
	 * {@link ERXApplication} calls this, so no need to do this if you're extending that.
	 */
	public static void useMe() {
		// do nothing
	}

	/**
	 * Construct a new nameless shutdown hook and register it.
	 */
	public ERXShutdownHook() {
		Runtime.getRuntime().addShutdownHook( this );
		ALL_HOOKS.add( this );
	}
	
	/**
	 * Construct a new named shutdown hook and register it.
	 * @param hookName hook name
	 */
	public ERXShutdownHook( String hookName ) {
		this();
		name = hookName;
	}

	@Override
	public final void run() {
		try {
			if( name != null ) System.out.println( "ERXShutdownHook " + name + " launched" );
			hook();
			if( name != null ) System.out.println( "ERXShutdownHook " + name + " completed" );
			synchronized( ALL_HOOKS ) {
				ALL_HOOKS.remove( this );
				ALL_HOOKS.notify();
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * This is where you implement what is supposed to be run at shutdown time.
	 */
	abstract public void hook();
}
