package er.r2d2w.components.misc;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.navigation.ERXNavigationMenuItem;
import er.extensions.components.ERXComponentUtilities;

public class R2DNavigationMenuItem extends ERXNavigationMenuItem {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public R2DNavigationMenuItem(WOContext context) {
        super(context);
    }

	@SuppressWarnings("unchecked")
	public NSDictionary<String, String> queryDictionary() {
		NSMutableDictionary<String, String> queryDictionary = 
			navigationItem().queryBindings().mutableClone();
		queryDictionary.setObjectForKey(context().contextID(), "__cid");
		return queryDictionary;
	}

	public WOActionResults action() {
		WOActionResults result = 
			(WOActionResults)valueForKeyPath(navigationItem().action());
		return result;
	}

	public boolean hasDirectAction() {
		String actionClass = navigationItem().directActionClass();
		String directActionName = navigationItem().directActionName();
		return (directActionName != null && directActionName.trim().length() > 0) 
			|| (actionClass != null && actionClass.trim().length() > 0) ;
	}
	
	public String itemClass() {
		StringBuilder sb = new StringBuilder(20);
		if(isSelected()) {
			sb.append(sb.length() == 0?"selected":" selected");
		}
		if(isDisabled()) {
			sb.append(sb.length() == 0?"disabled":" disabled");
		}
		if(hasActivity()) {
			sb.append(sb.length() == 0?"activity":" activity");
		}
		return sb.toString();
	}
	
	public Boolean secureInContext() {
		Boolean secure = navigationItem().secureInContext(this);
		if(secure == null) {
			boolean defaultValue = context().request().isSecure();
			secure = ERXComponentUtilities.booleanValueForBinding(this, "secure", defaultValue);
		}
		return secure;
	}
}