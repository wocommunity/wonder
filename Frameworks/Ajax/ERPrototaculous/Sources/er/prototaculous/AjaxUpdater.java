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
import er.prototaculous.AjaxRequestButton.Bindings;

/**
 * Wrapper of Prototype's Ajax.Updater
 * @see <a href="http://www.prototypejs.org/api/ajax/updater">Prototype's Ajax.Updater Reference</a>
 * 
 * @binding containe          The id of the container to be updated
 * @binding action            When bound $('container') is replaced with the results of the action
 * @binding directActionName  When bound $('container') is replaced with the results of the direct action
 * @binding onComplete        Callback @see Prototype Ajax.options
 * @binding onSuccess         Callback @see Prototype Ajax.options
 * @binding onCreate          Callback @see Prototype Ajax.options
 * @binding onException       Callback @see Prototype Ajax.options
 * @binding evalScripts       @see Prototype Ajax.Updater
 * @binding method            @see Prototype Ajax.Updater
 *  
 * @property er.prototaculous.useUnobtrusively Support for Unobtrusive Javascript programming. (Turned on by default).
 *  
 * @author mendis
 *
 */
public abstract class AjaxUpdater extends WOComponent {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.prototaculous.useUnobtrusively", true);
	
    public AjaxUpdater(WOContext context) {
		super(context);
	}

	/*
     * API/Bindings
     */
    public static interface Bindings {
    	public static final String container = "container";
    	public static final String action = "action";
    	public static final String directActionName = "directActionName";
    	public static final String evalScripts = "evalScripts";
    	public static final String name = "name";
    	public static final String onSuccess = "onSuccess";
    	public static final String onComplete = "onComplete";
    	public static final String onCreate = "onCreate";
    	public static final String onException = "onException";
    	public static final String method = "method";
    }
    
    // accessors
    public String directActionName() {
    	return (String) valueForBinding(Bindings.directActionName);
    }
    
    public WOActionResults action() {
    	return (WOActionResults) valueForBinding(Bindings.action);
    }
    
    public String container() {
    	return (String) valueForBinding(Bindings.container);
    }
    
    private String _method() {
    	return (String) valueForBinding(Bindings.method);
    }
    
    public String method() {
    	return (_method() != null) ? _method() : "post";
    }
        
    /*
     * The url of the Ajax.Updater. Defaults to the ref of the AjaxGenericContainer
     * @see AjaxGenericContainer
     */
    protected String url() {
    	return "$('" + container() + "').readAttribute('ref')";
    }
    
    /*
     * An array of options for Ajax.Updater
     */
    protected NSArray<String> _options() {
    	NSMutableArray _options = new NSMutableArray();
    	
    	// add options
    	if (hasBinding(Bindings.method)) _options.add("method: '" + valueForBinding(Bindings.method) + "'");
    	if (hasBinding(Bindings.evalScripts)) _options.add("evalScripts: " + evalScripts());
    	if (hasBinding(Bindings.onSuccess)) _options.add("onSuccess: " + valueForBinding(Bindings.onSuccess));
    	if (hasBinding(Bindings.onComplete)) _options.add("onComplete: " + valueForBinding(Bindings.onComplete));
    	if (hasBinding(Bindings.onCreate)) _options.add("onCreate: " + valueForBinding(Bindings.onCreate));
    	if (hasBinding(Bindings.onException)) _options.add("onException: " + valueForBinding(Bindings.onException));

    	return _options.immutableClone();
    }
    
    public String options() {
    	return _options().componentsJoinedByString(",");
    }
    
    public String onClick() {
    	return "new Ajax.Updater('" + container() + "', " + url() + ", {" + options() + "}); return false;";
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
