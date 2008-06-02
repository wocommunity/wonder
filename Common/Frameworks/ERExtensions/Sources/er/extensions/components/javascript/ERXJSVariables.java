package er.extensions.components.javascript;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Class for Wonder Component ERXJSVariables.
 *
 * @binding sample sample binding explanation
 *
 * @created ak on Sat May 03 2003
 * @project ERExtensions
 */

public class ERXJSVariables extends WOComponent {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERXJSVariables.class);

    public NSDictionary _arguments;
        protected String currentKey;

    /**
     * Public constructor
     * @param context the context
     */
    public ERXJSVariables(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    public NSDictionary arguments() {
        if(_arguments == null) {
            Object o = valueForBinding("arguments");
            if(o != null)
                _arguments = ((NSDictionary)o).mutableClone();
            else
                _arguments = new NSMutableDictionary();
                
            for(Enumeration e = bindingKeys().objectEnumerator(); e.hasMoreElements();) {
                String s = (String)e.nextElement();
                if(s.indexOf("?") == 0) {
                    Object obj = valueForBinding(s);
                    if(obj != null)
                        ((NSMutableDictionary)_arguments).setObjectForKey(obj, s.substring(1));
                }
            }
        }
        return _arguments;
    }

    public String currentKey() {
        return currentKey;
    }
    
    public String currentValue() {
        Object s = arguments().valueForKeyPath(currentKey());
        s = s == null ? "null" : s;
        return s.toString();
    }
}
