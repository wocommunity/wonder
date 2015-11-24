package er.jquery;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;

/**
 * A jQuery.get() as a link
 * @see jQueryGet
 * 
 * @binding queryDictionary		The form values used in the directAction that replaced $('container')
 * @binding confirmMessage		The message you wish to display in a confirm panel before executing the update
 * 
 * @author mendis
 *
 * TODO: Convert to dynamic element (for DA form elements support)
 */
public class jQueryGetLink extends jQueryGet {
    public jQueryGetLink(WOContext context) {
        super(context);
    }
    
    /*
     * API/Bindings
     */
    public static interface Bindings extends jQueryGet.Bindings {
    	public static final String queryDictionary = "queryDictionary";
    	public static final String confirmMessage = "confirmMessage";
    }
    
    @Override
    public boolean isStateless() {
    	return true;
    }

    // accessors
    @Override
    public String onClick() {
    	if (hasBinding(Bindings.confirmMessage)) {
    		return "if (confirm('" + confirmMessage() + "')) {" + _onClick() + " } return false;";
    	} else return super.onClick();
    }
    
    private String _onClick() {
    	String onClick = "$.get(" + url();
    	if (data() != null) onClick += ", " + data();
    	if (callback() != null) onClick += ", " + callback();
    	onClick += ");";
    	
    	return onClick;
    }
    
    public String confirmMessage() {
    	return (String) valueForBinding(Bindings.confirmMessage);
    }
    
    @Override
    protected String url() {
    	if (hasBinding(Bindings.action) || hasBinding(Bindings.directActionName))
    		return "this.href";
    	else throw new WODynamicElementCreationException("Action or directActionName is a required binding");
    }
    
    public String href() {
    	if (hasBinding(Bindings.action)) {
    		return context().componentActionURL(WOApplication.application().componentRequestHandlerKey());
    	} else if (hasBinding(Bindings.directActionName)) {
    		NSDictionary queryDictionary = hasBinding(Bindings.queryDictionary) ? queryDictionary() : null;
    		return context().directActionURLForActionNamed(directActionName(), queryDictionary);
    	} else return "#";
    }

    public NSDictionary queryDictionary() {
    	return (NSDictionary) valueForBinding(Bindings.queryDictionary);
    }
    
    // actions
    public WOActionResults invokeAction() {
    	context().setActionInvoked(true);
		if (hasBinding(Bindings.action))  {
			WOActionResults action = action();
			if (action instanceof WOComponent)  ((WOComponent) action)._setIsPage(true);	// cache is pageFrag cache
			return action;
		} else return context().page();
    }
}
