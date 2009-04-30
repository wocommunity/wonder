package er.prototaculous;

import com.webobjects.appserver.*;

import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXProperties;

/**
 * Abstract superclass that encapsulates http://www.stickmanlabs.com/lightwindow 2.0
 * 
 * @author mendis
 *
 */
public abstract class AjaxLightWindow extends WOComponent {
	public AjaxLightWindow(WOContext context) {
		super(context);
	}

	public final static String type = ERXProperties.stringForKeyWithDefault("er.ajax.AjaxLightWindow.type", "external");		// Default is to treat all Window content as external

    /*
     * API or bindings common to light window subcomponents
     */
    public static interface Bindings {
    	public static final String directActionName = "directActionName";
    	public static final String action = "action";
    	public static final String queryDictionary = "queryDictionary";
    	public static final String formID = "formID";
    	public static final String type = "type";
    	public static final String height = "height";
    	public static final String width = "width";
    	public static final String top = "top";
    	public static final String left = "left";
		public static final String title = "title";
    }
    
    // accessors
    public String formID() {
    	return (String) valueForBinding(Bindings.formID);
    }
    
    // R/R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
        AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
        AjaxUtils.addScriptResourceInHead(context, response, "scriptaculous.js");
        AjaxUtils.addScriptResourceInHead(context, response, "effects.js");
        AjaxUtils.addScriptResourceInHead(context, response, "WO2", "lightwindow.js");
        AjaxUtils.addStylesheetResourceInHead(context, response, "WO2", "lightwindow.css");
    }
}
