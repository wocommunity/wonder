package er.extensions;
import java.util.*;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/**
 * JavaScript remote execution.
 * See: http://developer.apple.com/internet/javascript/iframe.html for details
 * @binding sample sample binding explanation
 *
 * @created ak on Fri May 02 2003
 * @project ERExtensions
 */

public class ERXJSRemoteCall extends WOComponent {

    /** logging support */
    private static final ERXLogger log = ERXLogger.getLogger(ERXJSRemoteCall.class,"components");

    protected NSDictionary _arguments;
    protected NSArray _parameters;
    protected String _url;

    public Object currentValue;
    public Object currentKey;
         
    /**
     * Public constructor
     * @param context the context
     */
    public ERXJSRemoteCall(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }
    public String name() { return (String)valueForBinding("name");}
    public String frameName() { return name() + "Frame";}
    public String actionClass() { return (String)valueForBinding("actionClass");}
    public String url() {
        if(_url == null)
            _url = context().directActionURLForActionNamed((actionClass() == null ? "" : actionClass() + "/") + name(), arguments());
        return _url;
    }
    
    public NSDictionary arguments() {
        if(_arguments == null) {
            _arguments = new NSMutableDictionary();
            for(Enumeration e = bindingKeys().objectEnumerator(); e.hasMoreElements();) {
                String s = (String)e.nextElement();
                if(s.indexOf("?") == 0) {
                    Object o = valueForBinding(s);
                    if(o != null)
                        ((NSMutableDictionary)_arguments).setObjectForKey(s, s.substring(1));
                }
            }
        }
        return _arguments;
    }
    public NSArray parameters() {
        if(_parameters == null) {
            _parameters = ERXValueUtilities.arrayValue(valueForBinding("parameters"));
        }
        return _parameters;
    }
    
    public void reset() {
        _arguments = null;
        _parameters = null;
        super.reset();
    }
}
