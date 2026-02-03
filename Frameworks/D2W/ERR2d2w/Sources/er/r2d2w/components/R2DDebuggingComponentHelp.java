package er.r2d2w.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.ERD2WContextDictionary;
import er.directtoweb.ERDirectToWeb;
import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.appserver.ERXWOContext;

public class R2DDebuggingComponentHelp extends ERD2WStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 2L;
    public transient String currentKey;
    protected transient NSDictionary _contextDictionary;

    public R2DDebuggingComponentHelp(WOContext context) {
        super(context);
    }

    public boolean d2wDebuggingEnabled() {
        return ERDirectToWeb.d2wDebuggingEnabled(session());
    }

    public Object currentValue() {
        return contextDictionaryForPropertyKey().valueForKey(currentKey);
    }

    public NSDictionary contextDictionary() {
        if(_contextDictionary == null) {
            String key = "contextDictionary." + d2wContext().dynamicPage();
            _contextDictionary = (NSDictionary)ERXWOContext.contextDictionary().objectForKey(key);
            if(_contextDictionary == null) {
            	ERD2WContextDictionary dict = new ERD2WContextDictionary(d2wContext().dynamicPage(), null, null);
            	_contextDictionary = dict.dictionary();
            	ERXWOContext.contextDictionary().setObjectForKey(_contextDictionary, key);
            }
        }
        return _contextDictionary;
    }
    
    public NSDictionary contextDictionaryForPropertyKey() {
        Object o = contextDictionary().valueForKeyPath("componentLevelKeys." + propertyKey());
        if(o instanceof NSDictionary) {
            return (NSDictionary)o;
        }
        return NSDictionary.EmptyDictionary;
    }
}