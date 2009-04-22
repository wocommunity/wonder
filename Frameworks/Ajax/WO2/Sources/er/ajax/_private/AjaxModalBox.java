package er.ajax._private;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXApplication;

/**
 * Encapsulation of http://www.wildbit.com/labs/modalbox/ (a re-implementation of AjaxModalDialog)  
 * 
 * @author mendis
 *
 */
public abstract class AjaxModalBox extends WOComponent {
	
    /*
     * API or bindings common to light window subcomponents
     */
    public static interface Bindings {
    	public static final String directActionName = "directActionName";
    	public static final String action = "action";
    	public static final String queryDictionary = "queryDictionary";
    	public static final String params = "params";
    	public static final String width = "width";
    	public static final String title = "title";
    	public static final String left = "left";
    }
    
    public AjaxModalBox(WOContext context) {
		super(context);
	}
    
    // accessors
    protected NSArray<String> _options() {
    	NSMutableArray<String> params = new NSMutableArray<String>();
    	
    	if (hasBinding(Bindings.width)) params.add("width: " + width());
    	if (hasBinding(Bindings.left)) params.add("left: " + left());
    	
    	return params.immutableClone();
    }
    
    public String title() {
    	return (String) valueForBinding(Bindings.title);
    }
    
    public String width() {
    	return (String) valueForBinding(Bindings.width);
    }
    
    public String left() {
    	return (String) valueForBinding(Bindings.left);
    }
    
    public String options() {
    	return "{" + _options().componentsJoinedByString(",") + "}";
    }

	// R/R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
        AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
        AjaxUtils.addScriptResourceInHead(context, response, "scriptaculous.js");
        AjaxUtils.addScriptResourceInHead(context, response, "effects.js");
        AjaxUtils.addScriptResourceInHead(context, response, "modalbox.js");
        AjaxUtils.addScriptResourceInHead(context, response, "wonder.js");		// RM: only necessary for ajax updates from dialog
        AjaxUtils.addStylesheetResourceInHead(context, response, "modalbox.css");
    }
}
