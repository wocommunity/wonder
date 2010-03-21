package er.wojrebel;

import org.zeroturnaround.javarebel.ReloaderFactory;

/**
 * WOJRebel framework principal class. Initialises WOJRebelSupport if
 * the application is running with JRebel activated.
 * 
 * Note: This is a separate class with minimal dependencies to help isolate class 
 * loading failures when the framework is present but JRebel is not running.
 * 
 * @author q
 *
 */
public class WOJRebel {	
	public WOJRebel() {
		// Do nothing
	}
	
	static {
		try {
			if (ReloaderFactory.getInstance().isReloadEnabled() && WOJRebelIntegrationPlugin.isEnabled()) {
				new WOJRebelSupport();
			}
		} catch (NoClassDefFoundError e) {
			System.err.println("WOJRebel failed to start: JRebel was not found");
		}
	}
}
