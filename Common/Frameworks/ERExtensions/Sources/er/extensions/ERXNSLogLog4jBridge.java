//
//  ERXNSLogLog4jBridge.java
//
//  Created and contributed by David Teran on Mon Oct 21 2002.
//
package er.extensions;

import com.webobjects.foundation.NSLog;
import org.apache.log4j.Level;

public class ERXNSLogLog4jBridge extends NSLog.Logger {

    public static final ERXLogger log = ERXLogger.getERXLogger(ERXNSLogLog4jBridge.class);
    public static final int OUT = 1;
    public static final int ERR = 2;
    public static final int DEBUG = 3;
    private final int type;
    
    public ERXNSLogLog4jBridge(int type) {
        super();
        this.type = type;
        setIsEnabled(true);
    }

    public void appendln(Object obj) {
        if (isEnabled()) {
            if (obj == null)   obj = "";
            if (type == OUT) {
                log.info(obj.toString());
            } else if (type == ERR) {
                log.info(obj.toString());
            } else if (type == DEBUG) {
                log.debug(obj.toString());
            }
        }
    }
    
    public void setIsEnabled(boolean enabled) {
        super.setIsEnabled(enabled);
        if (type == DEBUG) {
            if (enabled) {
                log.setLevel(Level.DEBUG);
            } else {
                log.setLevel(Level.INFO);
            }
        }
    }

    public void appendln() {
        log.info(""); // Assuming people will always put "%n" at the end of the layout pattern.  
    }

    public void flush() {
    }
}

