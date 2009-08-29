package org.zeroturnaround.javarebel;

import java.util.concurrent.locks.ReentrantLock;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

/**
 * WOJavaRebelSupport loads the ClassReloadHandler once the app has started
 * 
 * @author q
 *
 */
public class WOJavaRebelSupport {
	public static final Observer observer = new Observer();
  private static final ReentrantLock lock = new ReentrantLock();
  
  private static final long MIN_ELAPSED_TIME = 2000;
  private static long lastRunTimestamp = System.currentTimeMillis();

	public WOJavaRebelSupport() {
		//Do nothing
	}
	
	public static void run() {
	  long currentTime = System.currentTimeMillis();
	  if (currentTime - lastRunTimestamp > MIN_ELAPSED_TIME) {
	    lock.lock();
	    try {
	      lastRunTimestamp = System.currentTimeMillis();
	      WOJavaRebelClassReloadHandler.getInstance().updateLoadedClasses(null);
	      WOJavaRebelModelReloadHandler.getInstance().updateLoadedModels(null);
	    } finally {
	      lock.unlock();
	    }
	  }
	}
	
	public static class Observer {
		public void finishedLaunchingApp(NSNotification n) {
			try {
				WOJavaRebelClassReloadHandler.getInstance().initialize();
				WOJavaRebelModelReloadHandler.getInstance().initialize();
			} catch (NoClassDefFoundError e) {
				/* JavaRebel isn't in the classpath so we do nothing */
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
