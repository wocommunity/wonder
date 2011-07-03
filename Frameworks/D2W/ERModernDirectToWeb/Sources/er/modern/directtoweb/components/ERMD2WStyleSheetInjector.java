package er.modern.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXProperties;

/**
 * Component to inject the rule defined stylesheets. Place at the end of a page
 * to make sure the stylesheets take presidence over any other injected style resources
 * 
 * @property er.modern.look.skinframework
 *
 * @author davidleber
 */
public class ERMD2WStyleSheetInjector extends ERD2WStatelessComponent {
	
	public String stylesheetName;
	
    public ERMD2WStyleSheetInjector(WOContext context) {
        super(context);
    }
    
    
    @SuppressWarnings("unchecked")
	public NSDictionary<String, String> stylesheet() {
    	return (NSDictionary<String, String>)d2wContext().valueForKey(stylesheetName);
    }
    
    /**
     * The name of the stylsheet framework name.
     * <p>
     * Looks for a property named er.modern.look.skinframework (which should be supplied by
     * the skin framework, otherwise defaults to 'app'
     */
    public String resourceFrameworkName() {
    	String fn = (String)stylesheet().valueForKey("framework");
    	if (fn == null) {
    		fn = ERXProperties.stringForKeyWithDefault("er.modern.look.skinframework", "app");
    	}
    	return fn;
    }
}
