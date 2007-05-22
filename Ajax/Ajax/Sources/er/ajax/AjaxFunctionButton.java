package er.ajax;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * AjaxFunctionButton is a convenience for calling javascript functions in response to an onclick on a button. When the
 * link is inside of an AjaxUpdateContainer, you do not need to specify the ID of the container.
 * 
 * So for instance, if you are inside of an AjaxInPlace with the id "blogForm" you can do
 * 
 * <pre>
 * &amp;lt:wo:AjaxFunctionButton action = &quot;edit&quot;&gt;
 * </pre>
 * 
 * to go into edit mode.
 * 
 * @binding disabled if true, the button is disabled
 * @binding onclick the javascript to execute when the button is clicked.
 * @binding action the type of event to fire ("update", "save", "edit", "cancel")
 * @binding updateContainerID the id of the container to fire the event to (optional if inside of the container)
 * @binding value the text of the button
 * 
 * @author mschrag
 */
public class AjaxFunctionButton extends WOHTMLDynamicElement {
	private WOAssociation _disabled;
	private WOAssociation _action;
	private WOAssociation _updateContainerID;

	public AjaxFunctionButton(String aName, NSDictionary associations, WOElement template) {
		super("input", AjaxFunctionButton.processAssociations(associations), template);
		_disabled = (WOAssociation) _associations.removeObjectForKey("disabled");
		_action = (WOAssociation) _associations.removeObjectForKey("action");
		_updateContainerID = (WOAssociation) _associations.removeObjectForKey("updateContainerID");
		if (_associations.objectForKey("onclick") != null && _action != null) {
			throw new WODynamicElementCreationException("You cannot bind both 'action' and 'onclick' at the same time.");
		}
		if (_updateContainerID != null && _action == null) {
			throw new WODynamicElementCreationException("If you bind 'updateContainerID', you must also bind 'action'.");
		}
	}

	private boolean isDisabled(WOContext context) {
		return _disabled != null && _disabled.booleanValueInComponent(context.component());
	}

	public void appendAttributesToResponse(WOResponse response, WOContext context) {
		super.appendAttributesToResponse(response, context);
		if (!isDisabled(context)) {
			AjaxFunctionLink._appendAttributesToResponse(response, context, _action, _updateContainerID);
		}
		else {
			response.appendContentString(" disabled");
		}
	}

	protected static NSDictionary processAssociations(NSDictionary associations) {
		NSMutableDictionary mutableAssociations = (NSMutableDictionary) associations;
		mutableAssociations.setObjectForKey(new WOConstantValueAssociation("button"), "type");
		return mutableAssociations;
	}
}
