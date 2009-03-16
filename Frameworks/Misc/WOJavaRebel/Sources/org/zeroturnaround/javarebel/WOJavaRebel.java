package org.zeroturnaround.javarebel;

/**
 * WOJavaRebel framework principal class. Initialises WOJavaRebelSupport if
 * the application is running with JavaRebel activated.
 * 
 * Note: This is a separate class with minimal dependencies to help isolate class 
 * loading failures when the framework is present but javarebel is not running.
 * 
 * @author q
 *
 */
public class WOJavaRebel {	
	public WOJavaRebel() {
		// Do nothing
	}
	
	static {
		try {
			if (ReloaderFactory.getInstance().isReloadEnabled()) {
				new WOJavaRebelSupport();
			}
		} catch (NoClassDefFoundError e) {
			System.err.println("WOJavaRebel failed to start: JavaRebel was not found");
		}
	}
}
