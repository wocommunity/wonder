package er.jquery;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXProperties;

/**
 * Wrapper of jQuery.load()
 * @see <a href="http://api.jquery.com/load/">jQuery .load() Reference</a>
 * 
 * @binding action      When bound the action is performed
 * @binding container   The id of the container to that performs the load
 * @binding complete  	A callback executed when the request completes
 *  
 * @property er.jquery.useUnobtrusively Support for Unobtrusive Javascript programming. (Turned on by default).
 *  
 * @author mendis
 */
public abstract class jQueryLoad extends WOComponent {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.jquery.useUnobtrusively", true);

    public jQueryLoad(WOContext context) {
		super(context);
	}
    
	/*
     * API/Bindings
     */
    public static interface Bindings {
    	public static final String action = "action";
    	public static final String directActionName = "directActionName";
    	public static final String name = "name";
    	public static final String container = "container";
    	public static final String complete = "complete";
    }
    
	// accessors    
    public WOActionResults action() {
    	return (WOActionResults) valueForBinding(Bindings.action);
    }
    
    public String directActionName() {
    	return (String) valueForBinding(Bindings.directActionName);
    }
    
    public String container() {
    	return (String) valueForBinding(Bindings.container);
    }
    
    public String complete() {
    	return (String) valueForBinding(Bindings.complete);
    }
    
    /*
     * The url of the jQuery .load(). Defaults to the href
     */
    protected abstract String url();
     
    public String onClick() {
    	String onClick = "$('#" + container() + "').load(" + url();
    	if (complete() != null) onClick += ", " + complete();
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
