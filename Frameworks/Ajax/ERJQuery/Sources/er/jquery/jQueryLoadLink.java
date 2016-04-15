package er.jquery;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;

/**
 * A jQuery .load() as a link
 * @see jQueryLoad
 * 
 * @binding queryDictionary		The form values used in the directAction that replaced $('container')
 * 
 * @author mendis
 *
 * TODO: Convert to dynamic element (for DA form elements support)
 */
public class jQueryLoadLink extends jQueryLoad {
	public jQueryLoadLink(WOContext context) {
		super(context);
	}
	
    /*
     * API/Bindings
     */
    public static interface Bindings extends jQueryLoad.Bindings {
    	public static final String queryDictionary = "queryDictionary";
    }
    
    @Override
    public boolean isStateless() {
    	return true;
    }

	// accessors
	@Override
	protected String url() {
		if (hasBinding(Bindings.action) || hasBinding(Bindings.directActionName))
			return "this.href";
		else throw new WODynamicElementCreationException("Action or directActionName is a required binding");
	}

	public String href() {
		if (hasBinding(Bindings.action)) {
    		return context().componentActionURL(application().ajaxRequestHandlerKey());
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
