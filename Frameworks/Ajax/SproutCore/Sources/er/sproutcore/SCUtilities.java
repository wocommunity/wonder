package er.sproutcore;

import java.util.LinkedHashMap;
import java.util.Stack;

import com.webobjects.appserver.WOContext;

import er.extensions.appserver.ERXResponse;
import er.extensions.appserver.ERXWOContext;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXThreadStorage;

public class SCUtilities {
    /**
     * Holds the external URL for the sproutcore libs via the property <code>er.sproutcore.base</code>.
     */
    private static String scBase;

    /**
     * Returns an external URL for the sproutcore libs to prepend via the property <code>er.sproutcore.base</code> or the empty String. MUST end with "/" if set.
     * @return
     */
    public static String scBase() {
        if(scBase == null) {
            scBase = ERXProperties.stringForKeyWithDefault("er.sproutcore.base","");
        }
        return scBase;
    }
    
    public static String staticUrl(String asset) {
        return "blank";
    }
}
