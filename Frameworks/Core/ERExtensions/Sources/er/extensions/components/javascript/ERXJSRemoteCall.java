package er.extensions.components.javascript;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXValueUtilities;

/**
 * JavaScript remote execution.
 * See: http://developer.apple.com/internet/javascript/iframe.html for details
 * @binding sample sample binding explanation
 *
 * @author ak on Fri May 02 2003
 */
public class ERXJSRemoteCall extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
    @Override
    public boolean synchronizesVariablesWithBindings() { return false; }
    
    @Override
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
    
    @Override
    public void reset() {
        _arguments = null;
        _parameters = null;
        super.reset();
    }
}
