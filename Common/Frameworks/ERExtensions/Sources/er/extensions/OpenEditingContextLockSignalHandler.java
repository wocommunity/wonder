package er.extensions;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

/**
 * OpenEditingContextLockSignalHandler provides a signal handler that
 * prints out open editing context locks.  By default, the handler attaches
 * to SIGHUP.
 * <p> 
 * OpenEditingContextLockSignalHandler.register();
 * <p>
 * Note that this would normally be in ERXEC as an inner class, but it uses 
 * a sun.misc API, so I didn't want to potentially break the class loading 
 * of ERXEC if a VM didn't happen to have it.
 * 
 * @author mschrag
 */
public class OpenEditingContextLockSignalHandler implements SignalHandler {
	public static final Logger log = Logger.getLogger(OpenEditingContextLockSignalHandler.class);

	/**
	 * Register the signal handle on the HUP signal. 
	 */
	public static void register() {
		OpenEditingContextLockSignalHandler.register("HUP");
	}

	/**
	 * Register the signal handle on the named signal.
	 * 
	 * @param signalName the name of the signal to handle
	 */
	public static void register(String signalName) {
		Signal.handle(new Signal(signalName), new OpenEditingContextLockSignalHandler());
	}

	public void handle(Signal signal) {
		ERXEC.Factory ecFactory = ERXEC.factory();
		if (ecFactory instanceof ERXEC.DefaultFactory) {
			NSArray lockedEditingContexts = ((ERXEC.DefaultFactory) ecFactory).lockedEditingContexts();
			if (lockedEditingContexts.count() != 0) {
				log.info(lockedEditingContexts.count() + " open EC locks:");
			}
			else {
				log.info("No open editing contexts.");
			}
			Enumeration lockedEditingContextEnum = lockedEditingContexts.objectEnumerator();
			while (lockedEditingContextEnum.hasMoreElements()) {
				EOEditingContext lockedEditingContext = (EOEditingContext) lockedEditingContextEnum.nextElement();
				log.info("   Editing Context " + lockedEditingContext);
				Exception creationTrace = ((ERXEC) lockedEditingContext).creationTrace();
				if (creationTrace != null) {
					log.info("  Created:");
					log.info("", creationTrace);
				}
				NSArray openLockTraces = ((ERXEC) lockedEditingContext).openLockTraces();
				if (openLockTraces != null) {
					log.info("  Locks:");
					Enumeration openLockTracesEnum = openLockTraces.objectEnumerator();
					while (openLockTracesEnum.hasMoreElements()) {
						Exception ecOpenLockTrace = (Exception) openLockTracesEnum.nextElement();
						log.info("", ecOpenLockTrace);
						log.info("");
					}
				}
			}
		}
		else {
			log.info("OpenEditingContextLockSignalHandler is only available for ERXEC.DefaultFactory.");
		}
	}
}
