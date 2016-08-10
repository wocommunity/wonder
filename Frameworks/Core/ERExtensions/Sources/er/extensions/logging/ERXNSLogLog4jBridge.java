//
//  ERXNSLogLog4jBridge.java
//
//  Created and contributed by David Teran on Mon Oct 21 2002.
//
package er.extensions.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.webobjects.foundation.NSLog;

import er.extensions.foundation.ERXProperties;

// CHECKME: A quick workaround for WO 5.1.x WOOutputPath issue. 
//          Subclassing PrintStreamLogger instead of Logger to prevent 
//          a ClassCastException. (WOApplication tries to cast the logger  
//          as PrintStreamLogger when WOOutputPath is specified.) 
//          Note that ERXNSLogLog4jBridge simply ignores the parameter 
//          of setPrintStream(PrintStream stream) method. 
// @property er.extensions.ERXNSLogLog4jBridge.ignoreNSLogSettings if true, NSLog's settings will not affect log4j and the log4j.logger.NSLog setting will be used instead.
public class ERXNSLogLog4jBridge extends /* NSLog.Logger */ NSLog.PrintStreamLogger {
    public static final Logger log = Logger.getLogger("NSLog");
    public static final int OUT = 1;
    public static final int ERR = 2;
    public static final int DEBUG = 3;
    private final int type;
    
    public ERXNSLogLog4jBridge(int type) {
        super();
        this.type = type;
       // setIsEnabled(true);
    }

    @Override
    public void appendln(Object obj) {
        if (isEnabled()) {
            if (obj == null)   obj = "";
            switch (type) {
                case OUT:
                    log.info(obj.toString());
                    break;
                case ERR:
                    log.warn(obj.toString());
                    break;
                case DEBUG:
                    log.debug(obj.toString());
                    break;
            }
        } else {
            if(type == ERR)
                log.warn(obj != null ? obj.toString() : "");
        }
    }
    
    @Override
    public void setIsEnabled(boolean enabled) {
        super.setIsEnabled(enabled);
        if (type == DEBUG && !ERXProperties.booleanForKeyWithDefault("er.extensions.ERXNSLogLog4jBridge.ignoreNSLogSettings", false)) {
            log.setLevel(enabled ? Level.DEBUG : Level.INFO);
        }
    }

    @Override
    public void setAllowedDebugLevel(int debugLevel) {
        super.setAllowedDebugLevel(debugLevel);

        if (type == DEBUG && !ERXProperties.booleanForKeyWithDefault("er.extensions.ERXNSLogLog4jBridge.ignoreNSLogSettings", false)) {
            log.setLevel(debugLevel != NSLog.DebugLevelOff ? Level.DEBUG : Level.INFO);
        }
    }

    @Override
    public void appendln() {
        appendln(""); // Assuming people will always put "%n" at the end of the layout pattern.  
    }

    @Override
    public void flush() {
    }
}

