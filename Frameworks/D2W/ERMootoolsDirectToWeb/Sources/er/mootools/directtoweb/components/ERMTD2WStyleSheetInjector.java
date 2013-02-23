package er.mootools.directtoweb.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.ERD2WStatelessComponent;
import er.extensions.foundation.ERXProperties;

public class ERMTD2WStyleSheetInjector extends ERD2WStatelessComponent {
	
	public String _stylesheetName;
	
    public ERMTD2WStyleSheetInjector(WOContext context) {
        super(context);
    }
    
    public String resourceFrameworkName() {
        
    	String resourceFrameworkName = (String)stylesheet().valueForKey("framework");
    	
    	if (resourceFrameworkName == null) {
    		resourceFrameworkName = ERXProperties.stringForKeyWithDefault("er.mootools.look.skinframework", "app");
    	}
    	
    	return resourceFrameworkName;

    }

    @SuppressWarnings("unchecked")
	public NSDictionary<String, String> stylesheet() {
    	return (NSDictionary<String, String>)d2wContext().valueForKey(_stylesheetName);
    }

	public String stylesheetFileName() {
		return (String)stylesheet().valueForKey("fileName");
	}

	public String stylesheetMedia() {
		return (String)stylesheet().valueForKey("media");
	}    

}