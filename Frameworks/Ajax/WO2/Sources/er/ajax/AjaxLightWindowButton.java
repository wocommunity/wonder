package er.ajax;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

import er.ajax.AjaxLightWindowLink.Bindings;
import er.extensions.appserver.ERXWOContext;

/**
 * Encapsulates http://www.stickmanlabs.com/lightwindow 2.0
 *
 * Extending the api of WOSubmitButton
 *
 * 
 * @author mendis
 */

public class AjaxLightWindowButton extends AjaxLightWindow {

    public AjaxLightWindowButton(WOContext context) {
        super(context);
    }
    
    @Override
    public boolean synchronizesVariablesWithBindings() {
    	return false;
    }
    
    @Override
    public boolean isStateless() {
    	return true;
    }
    
    // accessors
    public String onClick() {
    	return "myLightWindow.activateWindow({ " + options() + " }); return false;";
    }
    
    private NSArray<String> _options() {
    	NSMutableArray<String> _options = new NSMutableArray<String>();
    	
    	_options.add("href: '" + href() + "'");
    	if (hasBinding(Bindings.formID)) _options.add("form: '" + formID() + "'");
    	_options.add("type: '" + type + "'");
    	_options.add("rel: 'submitForm'");
    	if (hasBinding(Bindings.height) && valueForBinding(Bindings.height) != null) _options.add("height: " + valueForBinding(Bindings.height));
    	if (hasBinding(Bindings.width) && valueForBinding(Bindings.width) != null) _options.add("width: " + valueForBinding(Bindings.width));
    	if (hasBinding(Bindings.top) && valueForBinding(Bindings.top) != null) _options.add("top: " + valueForBinding(Bindings.top));
    	if (hasBinding(Bindings.left) && valueForBinding(Bindings.left) != null) _options.add("left: " + valueForBinding(Bindings.left));
    	if (hasBinding(Bindings.title) && valueForBinding(Bindings.left) != null) _options.add("title: '" + valueForBinding(Bindings.title) + "'");
    	
    	return _options.immutableClone();
    }
    
    public String options() {
    	return _options().componentsJoinedByString(",");
    }
    
    public String href() {
    	if (hasBinding(Bindings.action))
    		return ERXWOContext.ajaxActionUrl(context());
    	else if (hasBinding(Bindings.directActionName)) {
    		String directActionName = (String) valueForBinding(Bindings.directActionName);
    		NSDictionary queryDictionary = (NSDictionary) valueForBinding(Bindings.queryDictionary);
    		
    		return context().directActionURLForActionNamed(directActionName, queryDictionary);
    	} else return null;
    }
    
    // R/R
	@Override
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
    	if (aContext.senderID().equals(aContext.elementID())) {		// check to see if the request is coming from lightwindow
    		if (hasBinding(Bindings.action)) {
        		aContext._setActionInvoked(true);
    			return (WOComponent) valueForBinding(Bindings.action);
    		}
    	} return null;
    }
    
    @Override
    public void awake() {
    	super.awake();
    	context()._setFormSubmitted(true);
    }
    
    @Override
    public void sleep() {
    	super.sleep();
    	context()._setFormSubmitted(false);
    }
}