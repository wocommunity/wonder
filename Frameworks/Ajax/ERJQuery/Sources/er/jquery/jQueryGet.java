package er.jquery;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXProperties;

/**
 * Wrapper of jQuery.get()
 * @see <a href="http://api.jquery.com/jQuery.get/">jQuery.get() Reference</a>
 * 
 * @binding action      When bound the action is performed
 * @binding callback  	Callback @see jQuery.ajax() success
 *  
 * @property er.jquery.useUnobtrusively Support for Unobtrusive Javascript programming. (Turned on by default).
 *  
 * @author mendis
 */
public abstract class jQueryGet extends WOComponent {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.jquery.useUnobtrusively", true);

    public jQueryGet(WOContext context) {
		super(context);
	}
    
	/*
     * API/Bindings
     */
    public static interface Bindings {
    	public static final String action = "action";
    	public static final String directActionName = "directActionName";
    	public static final String name = "name";
    	public static final String callback = "callback";
    	public static final String data = "data";
    	public static final String dataType = "dataType";
    }

	// accessors    
    public WOActionResults action() {
    	return (WOActionResults) valueForBinding(Bindings.action);
    }
    
    public String directActionName() {
    	return (String) valueForBinding(Bindings.directActionName);
    }
    
    public String data() {
    	return (String) valueForBinding(Bindings.data);
    }
    
    public String callback() {
    	return (String) valueForBinding(Bindings.callback);
    }
    
    /*
     * The url of the jQuery.get(). Defaults to the href
     */
    protected abstract String url();
     
    public String onClick() {
    	String onClick = "$.get(" + url();
    	if (data() != null) onClick += ", " + data();
    	if (callback() != null) onClick += ", " + callback();
    	onClick += "); return false;";
    	
    	return onClick;
    }
	
    // R&R
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	if (!useUnobtrusively) {
    		ERXResponseRewriter.addScriptResourceInHead(response, context, "ERJQuery", "jquery-1.4.js");
    	} super.appendToResponse(response, context);
    }
}
