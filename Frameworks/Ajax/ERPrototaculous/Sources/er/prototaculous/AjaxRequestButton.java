package er.prototaculous;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

/**
 * An Ajax.Request as a button (that also submits the form and form values)
 * 
 * In order to use this, the form elements(i.e WOTextField, etc) need to have their name attribute bound to concrete values.
 * The Prototype Ajax.Request form is parametized using these names. WOElements won't correctly take form values otherwise.
 * Also Prototype/WO integration requires the use of &lt;button&gt; rather than &lt;input&gt; WOSubmitButtons. 
 * So set:		
 * 			 er.extensions.foundation.ERXPatcher.DynamicElementsPatches.SubmitButton.useButtonTag=true
 *  
 * @see AjaxRequest
 *   
 * @author mendis
 */
public class AjaxRequestButton extends AjaxRequest {
    public AjaxRequestButton(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean isStateless() {
    	return true;
    }
    
    /*
     * API/Bindings
     */
    public static interface Bindings extends AjaxRequest.Bindings {
    	public static final String method = "method";
    }
    

    // accessors
    @Override
    protected NSArray<String> _options() {
    	NSMutableArray _options = super._options().mutableClone();
    	
    	if (hasBinding(Bindings.method)) _options.add("method: '" + valueForBinding(Bindings.method) + "'");
    	_options.add("parameters: this.form.serialize(true)");
    	
    	return _options.immutableClone();
    }

	@Override
	protected String url() {
    	if (hasBinding(Bindings.action)) {
    		return "'" + context().componentActionURL(application().ajaxRequestHandlerKey()) + "'";
    	} else throw new WODynamicElementCreationException("Action is a required binding");
	}
	
    // actions
    public WOActionResults invokeAction() {
		if (hasBinding(Bindings.action)) {
			WOActionResults action = (WOActionResults) valueForBinding(Bindings.action);
			if (action instanceof WOComponent)  ((WOComponent) action)._setIsPage(true);	// cache is pageFrag cache
			return action;
		} else throw new WODynamicElementCreationException("Action is not bound");
    }
}
