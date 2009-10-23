package er.prototaculous;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxUtils;
import er.extensions.components.ERXComponentUtilities;
import er.extensions.foundation.ERXProperties;

/**
 * Wrapper of Prototype's Ajax.Request
 * @see <a href="http://www.prototypejs.org/api/ajax/request">Prototype's Ajax.Request Reference</a>
 * 
 * @binding action      When bound the action is performed
 * @binding onComplete  Callback @see Prototype Ajax.options
 * @binding onSuccess   Callback @see Prototype Ajax.options
 * @binding onCreate    Callback @see Prototype Ajax.options
 * 
 * @binding evalScripts @see Prototype Ajax.Request
 *  
 * @property er.prototaculous.useUnobtrusively Support for Unobtrusive Javascript programming. (Turned on by default).
 *  
 * @author mendis
 *
 */
public abstract class AjaxRequest extends WOComponent{
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.prototaculous.useUnobtrusively", true);
	
    public AjaxRequest(WOContext context) {
		super(context);
	}

	/*
     * API/Bindings
     */
    public static interface Bindings {
    	public static final String action = "action";
    	public static final String directActionName = "directActionName";
    	public static final String evalScripts = "evalScripts";
    	public static final String name = "name";
    	public static final String onSuccess = "onSuccess";
    	public static final String onComplete = "onComplete";
    	public static final String onCreate = "onCreate";
    }
    
    // accessors    
    public WOActionResults action() {
    	return (WOActionResults) valueForBinding(Bindings.action);
    }
    
    public String directActionName() {
    	return (String) valueForBinding(Bindings.directActionName);
    }
    
    /*
     * An array of options for Ajax.Updater
     */
    protected NSArray<String> _options() {
    	NSMutableArray _options = new NSMutableArray();
    	
    	// add options
    	if (hasBinding(Bindings.evalScripts)) _options.add("evalScripts: " + evalScripts());
    	if (hasBinding(Bindings.onSuccess)) _options.add("onSuccess: " + valueForBinding(Bindings.onSuccess));
    	if (hasBinding(Bindings.onComplete)) _options.add("onComplete: " + valueForBinding(Bindings.onComplete));
    	if (hasBinding(Bindings.onCreate)) _options.add("onCreate: " + valueForBinding(Bindings.onCreate));

    	return _options.immutableClone();
    }
    
    public String options() {
    	return _options().componentsJoinedByString(",");
    }
    
    /*
     * The url of the Ajax.Request. Defaults to the href
     */
    protected abstract String url();
     
    public String onClick() {
    	return "new Ajax.Request(" + url() + ", {" + options() + "}); return false;";
    }
    
    protected boolean evalScripts() {
    	return ERXComponentUtilities.booleanValueForBinding(this, Bindings.evalScripts);
    }
    
    public String elementName() {
    	return (hasBinding(Bindings.name)) ? _elementName() : context().elementID();
    }
    
    private String _elementName() {
    	return (String) valueForBinding(Bindings.name);
    }
    
    // R&R
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	if (!useUnobtrusively) {
    		AjaxUtils.addScriptResourceInHead(context, response, "Ajax", "prototype.js");
    	} super.appendToResponse(response, context);
    }
}
