//
//  ERXGracefulShutdown.java
//  ERExtensions
//
//  Created by Max Muller III on Thu Nov 06 2003.
//
package er.extensions.components;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.webobjects.appserver.WOApplication;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXUtilities;

/**
 * Graceful shutdown adds signal handling support for gracefully
 * terminating a WOApplication. The default implementation in
 * ERXApplication simply calls the terminate method. By default
 * only the TERM and INT signals are registered to handle.
 */
public class ERXGracefulShutdown implements SignalHandler {

    //	===========================================================================
    //	Class Constant(s)
    //	---------------------------------------------------------------------------    
    
    private static final Logger log = LoggerFactory.getLogger(ERXGracefulShutdown.class);

    //	===========================================================================
    //	Interfaces(s)
    //	---------------------------------------------------------------------------

    /**
     * Interface to be implemented by the WOApplication subclass to gracefully
     * handle termination. Implemented by ERXApplication.
     */
    public static interface GracefulApplication {
        public void gracefulTerminate();
    }    
    
    //	===========================================================================
    //	Class Method(s)
    //	---------------------------------------------------------------------------

    /**
     * Determines if signal handling is enabled. Defaults to false.
     * @return if signal handling is enabled
     */
    public static boolean isEnabled() {
        return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXGracefulShutdown.Enabled",
                                                      false);
    }

    /**
     * Installs signal handlers for the given array of signals. Default signals
     * to catch are TERM and INT. The previous handler is saved to chain back to
     * if anything goes wrong with the graceful termination method.
     */
    public static void installHandler() {
        if (isEnabled()) {
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
    }

    //	===========================================================================
    //	Instance Variable(s)
    //	---------------------------------------------------------------------------

    /** caches the previous handler for chaining */
    protected SignalHandler defaultHandler;

    //	===========================================================================
    //	Instance Method(s)
    //	---------------------------------------------------------------------------

    /**
     * Signal handling method. Gracefully terminates the currently running
     * WOApplication.
     * @param signal to be handled
     */
    public void handle(Signal signal) {
        log.info("Received {}, starting graceful shutdown.", signal);
        try {
            if (WOApplication.application() instanceof GracefulApplication) {
                ((GracefulApplication)WOApplication.application()).gracefulTerminate();
            }
        } catch (RuntimeException e) {
            log.warn("Caught exception when attempting to gracefully shutdown! {} stack: {}", e.getClass().getName(), ERXUtilities.stackTrace(e));
        }
        // Chaining back to original handler
        defaultHandler().handle(signal);
    }

    //	===========================================================================
    //	Instance Accessor Method(s)
    //	---------------------------------------------------------------------------    

    /**
     * @return the default handler
     */
    public SignalHandler defaultHandler() { return defaultHandler; }

    /**
     */
    public void setDefaultHandler(SignalHandler value) { defaultHandler = value; }

}
