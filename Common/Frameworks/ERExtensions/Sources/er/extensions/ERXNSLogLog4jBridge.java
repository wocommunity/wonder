//
//  ERXNSLogLog4jBridge.java
//
//  Created and contributed by David Teran on Mon Oct 21 2002.
//
package er.extensions;

import org.apache.log4j.*;

import com.webobjects.foundation.*;

// CHECKME: A quick workaround for WO 5.1.x WOOutputPath issue. 
//          Subclassing PrintStreamLogger instead of Logger to prevent 
//          a ClassCastException. (WOApplication tries to cast the logger  
//          as PrintStreamLogger when WOOutputPath is specified.) 
//          Note that ERXNSLogLog4jBridge simply ignores the parameter 
//          of setPrintStream(PrintStream stream) method. 

public class ERXNSLogLog4jBridge extends /* NSLog.Logger */ NSLog.PrintStreamLogger {

    public static final ERXLogger log = ERXLogger.getERXLogger("NSLog");
    public static final int OUT = 1;
    public static final int ERR = 2;
    public static final int DEBUG = 3;
    private final int type;
    
    public ERXNSLogLog4jBridge(int type) {
        super();
        this.type = type;
       // setIsEnabled(true);
    }

    public void appendln(Object obj) {
        if (isEnabled()) {
            if (obj == null)   obj = "";
            switch (type) {
                case OUT:
                    log.info(obj.toString());
                    break;
                case ERR:
                    log.error(obj.toString());
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
    
    public void setIsEnabled(boolean enabled) {
        super.setIsEnabled(enabled);
        if (type == DEBUG) {
            log.setLevel(enabled ? Level.DEBUG : Level.INFO);
        }
    }

    public void setAllowedDebugLevel(int debugLevel) {
        super.setAllowedDebugLevel(debugLevel);

        if (type == DEBUG) {
            log.setLevel(debugLevel != NSLog.DebugLevelOff ? Level.DEBUG : Level.INFO);
        }
    }

    public void appendln() {
        appendln(""); // Assuming people will always put "%n" at the end of the layout pattern.  
    }

    public void flush() {
    }
}

