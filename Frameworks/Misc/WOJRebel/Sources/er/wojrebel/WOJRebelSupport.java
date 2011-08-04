package er.wojrebel;

import java.util.concurrent.locks.ReentrantLock;

import org.zeroturnaround.javarebel.webobjects.WebObjectsPlugin;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

/**
 * WOJRebelSupport loads the ClassReloadHandler once the app has started
 * 
 * @author qdolan
 *
 */
public class WOJRebelSupport {
	public static final Observer observer = new Observer();
  private static final ReentrantLock lock = new ReentrantLock();
  private static final Class<?>[] NotificationClassArray = new Class[] { NSNotification.class };
  
  private static final long MIN_ELAPSED_TIME = 2000;
  private static long lastRunTimestamp = System.currentTimeMillis();

	public WOJRebelSupport() {
		//Do nothing
	}
	
	public static void run() {
	  long currentTime = System.currentTimeMillis();
	  if (currentTime - lastRunTimestamp > MIN_ELAPSED_TIME) {
	    lock.lock();
	    try {
	      lastRunTimestamp = System.currentTimeMillis();
	      WOJRebelClassReloadHandler.getInstance().updateLoadedClasses(null);
	      WOJRebelEOModelReloadHandler.getInstance().updateLoadedModels(null);
	    } finally {
	      lock.unlock();
	    }
	  }
	}
	
	public static class Observer {
		public void finishedLaunchingApp(NSNotification n) {
			try {
				WOJRebelClassReloadHandler.getInstance().initialize();
				NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("run", NotificationClassArray), WebObjectsPlugin.JREBEL_EVENT, null);
			} catch (NoClassDefFoundError e) {
				/* JRebel isn't in the classpath so we do nothing */
				return;
			}
		}
		
	  public static void run(NSNotification notification) {
	    WOJRebelSupport.run();
	  }
	}
	
	static {
		try {
			NSNotificationCenter.defaultCenter().addObserver(observer,
				new NSSelector("finishedLaunchingApp", NotificationClassArray), WOApplication.ApplicationWillFinishLaunchingNotification, null);
			if (WOJRebelClassReloadHandler.getInstance().isReloadEnabled()) {
				// We need to initialize this early or we won't see the models load.
				WOJRebelEOModelReloadHandler.getInstance().initialize();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
}
