package er.extensions.components.javascript;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WOGenericContainer;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * ERXClickableContainer is a WOGenericContainer with easier support for the
 * javascript onclick event. For instance,
 * 
 * &lt;wo:ERXClickableContainer action = "$someAction"&gt;
 * 
 * would create a div (by default) with an onclick action that executes the
 * given component action and replaces the page with the results.
 * 
 * @binding elementName the HTML element name to use (div by default)
 * @binding action the action to perform
 * @binding actionClass the class name that contains the direct action
 * @binding directActionName the name of the direct action to execute
 * @binding disabled if true, the onclick is not added to this container
 * 
 * @author mschrag
 */
public class ERXClickableContainer extends WOGenericContainer {
	private WOAssociation _actionClass;
	private WOAssociation _directActionName;
	private WOAssociation _disabled;
	
	public ERXClickableContainer(String name, NSDictionary associations, WOElement template) {
		super(name, ERXClickableContainer._processAssociations(associations), template);
		_actionClass = _associations.removeObjectForKey("actionClass");
		_directActionName = _associations.removeObjectForKey("directActionName");
		_disabled = _associations.removeObjectForKey("disabled");
	}

	protected static NSDictionary _processAssociations(NSDictionary associations) {
		NSMutableDictionary mutableAssociations = (NSMutableDictionary) associations;
		WOAssociation action = (WOAssociation) mutableAssociations.removeObjectForKey("action");
		if (action != null) {
			mutableAssociations.setObjectForKey(action, "invokeAction");
		}
		if (!mutableAssociations.containsKey("elementName")) {
			mutableAssociations.setObjectForKey(new WOConstantValueAssociation("div"), "elementName");
		}
		if (!mutableAssociations.containsKey("style")) {
			mutableAssociations.setObjectForKey(new WOConstantValueAssociation("cursor: pointer;"), "style");
		}
		return mutableAssociations;
	}

	@Override
	public void appendAttributesToResponse(WOResponse response, WOContext context) {
		super.appendAttributesToResponse(response, context);
		WOComponent component = context.component();
		if (_disabled == null || !_disabled.booleanValueInComponent(component)) {
			response.appendContentString(" onclick = \"location.href='");
			String url;
			if (_directActionName != null) {
				String actionName = (String) _directActionName.valueInComponent(component);
				if (_actionClass != null) {
					actionName = actionName + "/" + (String) _actionClass.valueInComponent(component);
				}
				url = context.directActionURLForActionNamed(actionName, null);
			}
			else {
				url = context.componentActionURL();
			}
			response.appendContentString(url);
			response.appendContentString("'\"");
		}
	}
}