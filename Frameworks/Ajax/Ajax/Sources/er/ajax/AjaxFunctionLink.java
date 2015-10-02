package er.ajax;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * AjaxFunctionLink is a convenience for calling javascript functions in response to
 * an onclick.  When the link is inside of an AjaxUpdateContainer, you do not need to
 * specify the ID of the container.
 * 
 * So for instance, if you are inside of an AjaxInPlace with the id "blogForm" you can do
 * 
 * <pre>
 * &lt;wo:AjaxFunctionLink action = "edit"&gt;
 * </pre>
 * 
 * to go into edit mode.
 * 
 * @binding disabled if true, disables the link
 * @binding onclick the javascript to execute when the link is clicked.
 * @binding onClick synonym of onclick
 * @binding action the type of event to fire ("update", "save", "edit", "cancel")
 * @binding updateContainerID the id of the container to fire the event to (optional if inside of the container)
 * 
 * @author mschrag
 */
public class AjaxFunctionLink extends WOHTMLDynamicElement {
	private WOAssociation _disabled;
	private WOAssociation _action;
	private WOAssociation _updateContainerID;

	public AjaxFunctionLink(String aName, NSDictionary associations, WOElement template) {
		super("a", AjaxFunctionLink.processAssociations(associations), template);
		_action = _associations.removeObjectForKey("action");
		_updateContainerID = _associations.removeObjectForKey("updateContainerID");
		WOAssociation onclick = _associations.objectForKey("onclick");
		if (onclick == null) {
			onclick = _associations.objectForKey("onClick");
		}
		if (onclick != null && _action != null) {
			throw new WODynamicElementCreationException("You cannot bind both 'action' and 'onclick' at the same time.");
		}
		if (_updateContainerID != null && _action == null) {
			throw new WODynamicElementCreationException("If you bind 'updateContainerID', you must also bind 'action'.");
		}
	}

	private boolean isDisabled(WOContext context) {
		return _disabled != null && _disabled.booleanValueInComponent(context.component());
	}

	@Override
	protected void _appendOpenTagToResponse(WOResponse response, WOContext context) {
		if (!isDisabled(context)) {
			super._appendOpenTagToResponse(response, context);
		}
	}

	@Override
	protected void _appendCloseTagToResponse(WOResponse response, WOContext context) {
		if (!isDisabled(context)) {
			super._appendCloseTagToResponse(response, context);
		}
	}

	@Override
	public void appendAttributesToResponse(WOResponse response, WOContext context) {
		super.appendAttributesToResponse(response, context);
		if (!isDisabled(context)) {
			AjaxFunctionLink._appendAttributesToResponse(response, context, _action, _updateContainerID);
		}
	}
	
	public static void _appendAttributesToResponse(WOResponse response, WOContext context, WOAssociation actionAssociation, WOAssociation updateContainerIDAssociation) {
		WOComponent component = context.component();
		if (actionAssociation != null) {
			String action = (String) actionAssociation.valueInComponent(component);
			String updateContainerID;
			if (updateContainerIDAssociation != null) {
				updateContainerID = (String) updateContainerIDAssociation.valueInComponent(component);
			}
			else {
				updateContainerID = AjaxUpdateContainer.currentUpdateContainerID();
			}

			if (updateContainerID == null) {
				throw new WODynamicElementCreationException("You must either set the 'updateContainerID' binding or the link must be contained inside of an AjaxUpdateContainer.");
			}

			response.appendContentString(" onclick = \"");
			response.appendContentString(updateContainerID);
			if ("edit".equalsIgnoreCase(action)) {
				response.appendContentString("Edit");
			}
			else if ("cancel".equalsIgnoreCase(action)) {
				response.appendContentString("Cancel");
			}
			else if ("save".equalsIgnoreCase(action)) {
				response.appendContentString("Save");
			}
			else if ("update".equalsIgnoreCase(action)) {
				response.appendContentString("Update");
			}
			else {
				throw new WODynamicElementCreationException("Unknown AjaxInPlace action '" + action + "'.  Must be one of 'edit', 'cancel', 'save', or 'Update'.");
			}
			response.appendContentString("()\"");
		}
	}

	protected static NSDictionary processAssociations(NSDictionary associations) {
		NSMutableDictionary mutableAssociations = (NSMutableDictionary) associations;
		mutableAssociations.setObjectForKey(new WOConstantValueAssociation("javascript:void(0)"), "href");
		return mutableAssociations;
	}
}
