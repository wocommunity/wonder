//
//  ERXGracefulShutdown.java
//  ERExtensions
//
//  Created by Max Muller III on Thu Nov 06 2003.
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import java.util.*;

public class ERXGracefulShutdown implements SignalHandler {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXGracefulShutdown.class);
    
    public static boolean isEnabled() {
        return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXGracefulShutdown.Enabled",
                                                      false);
    }

    public static interface GracefulApplication {
        public void gracefulTerminate();
    }

    public static void installHandler() {
        // Just the rgular termination request
        NSArray signals = ERXProperties.arrayForKey("er.extensions.ERXGracefulShutdown.SignalsToHandle");
        if (signals != null && signals.count() > 0) {
            for (Enumeration signalsEnumerator = signals.objectEnumerator();
                 signalsEnumerator.hasMoreElements();) {
                Signal signal = new Signal((String)signalsEnumerator.nextElement());
                ERXGracefulShutdown handler = new ERXGracefulShutdown();
                handler.setDefaultHandler(Signal.handle(signal, handler));        
            }
        }
    }

    protected SignalHandler defaultHandler;

    public SignalHandler defaultHandler() { return defaultHandler; }
    public void setDefaultHandler(SignalHandler value) { defaultHandler = value; }

    // Signal handler method
    public void handle(Signal signal) {
        log.info("Received " + signal + ", starting graceful shutdown.");
        try {
            if (WOApplication.application() instanceof GracefulApplication) {
                ((GracefulApplication)WOApplication.application()).gracefulTerminate();
            }
        } catch (RuntimeException e) {
            log.warn("Caught exception when attempting to gracefully shutdown! " + e.getClass().getName() + " stack: " + ERXUtilities.stackTrace(e));
        }
        // Chaining back to original handler
        defaultHandler().handle(signal);
    }
}
