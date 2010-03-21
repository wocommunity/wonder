package er.wojrebel;

import java.util.concurrent.locks.ReentrantLock;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

/**
 * WOJRebelSupport loads the ClassReloadHandler once the app has started
 * 
 * @author q
 *
 */
public class WOJRebelSupport {
	public static final Observer observer = new Observer();
  private static final ReentrantLock lock = new ReentrantLock();
  
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
				WOJRebelEOModelReloadHandler.getInstance().initialize();
			} catch (NoClassDefFoundError e) {
				/* JRebel isn't in the classpath so we do nothing */
				return;
			}
		}
	}
	
	static {
		try {
			NSNotificationCenter.defaultCenter().addObserver(observer,
				new NSSelector("finishedLaunchingApp", new Class[] { com.webobjects.foundation.NSNotification.class }),
				WOApplication.ApplicationWillFinishLaunchingNotification, null);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
}
