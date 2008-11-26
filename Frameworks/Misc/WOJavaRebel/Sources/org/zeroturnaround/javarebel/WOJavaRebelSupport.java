package org.zeroturnaround.javarebel;

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
	public final static Observer observer = new Observer();

	public WOJavaRebelSupport() {
		//Do nothing
	}
	
	public static class Observer {
		public void finishedLaunchingApp(NSNotification n) {
			try {
				WOJavaRebelClassReloadHandler.getClassHandler().initialize();
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
