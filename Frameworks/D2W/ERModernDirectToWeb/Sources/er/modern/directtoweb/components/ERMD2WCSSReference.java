package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXProperties;

/**
 * Component to go in the &lt;head&gt; of a page. Injects the standandard set of css stylesheets
 * and the set defined in the rules
 * 
 * @property er.modern.look.skinframework
 *
 * @author davidleber
 */
public class ERMD2WCSSReference extends ERD2WStatelessComponent {
	
	public String stylesheetName;
	
    public ERMD2WCSSReference(WOContext context) {
        super(context);
    }
    
    @SuppressWarnings("unchecked")
	public NSDictionary<String, String> stylesheet() {
    	return (NSDictionary<String, String>)d2wContext().valueForKey(stylesheetName);
    }
    
    @Override
    public D2WContext d2wContext() {
//    	NSLog.out.appendln("ERD2WCSSReference.d2wContext: " + super.d2wContext());
    	return super.d2wContext();
    }
    
    public String resourceFrameworkName() {
    	String fn = (String)stylesheet().valueForKey("framework");
    	if (fn == null) {
    		fn = ERXProperties.stringForKeyWithDefault("er.modern.look.skinframework", "app");
    	}
    	return fn;
    }

}
