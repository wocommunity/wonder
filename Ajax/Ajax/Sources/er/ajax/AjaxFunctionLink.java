package er.ajax;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXHyperlink;

/**
 * AjaxFunctionLink is a convenience for calling javascript functions in response to
 * an onclick.  When the link is inside of an AjaxUpdateContainer, you do not need to
 * specify the ID of the container.
 * 
 * So for instance, if you are inside of an AjaxInPlace with the id "blogForm" you can do
 * 
 * <pre>
 * &lt:wo:AjaxFunctionLink type = "edit"&gt;
 * </pre>
 * 
 * to go into edit mode.
 * 
 * @binding onclick the javascript to execute when the link is clicked.
 * @binding type the type of event to fire ("update", "save", "edit", "cancel")
 * @binding updateContainerID the id of the container to fire the event to (optional if inside of the container)
 * 
 * @author mschrag
 */
public class AjaxFunctionLink extends ERXHyperlink {
	private WOAssociation _type;
	private WOAssociation _updateContainerID;

	public AjaxFunctionLink(String aName, NSDictionary associations, WOElement template) {
		super(aName, AjaxFunctionLink.processAssociations(associations), template);
		_type = (WOAssociation) _associations.removeObjectForKey("type");
		_updateContainerID = (WOAssociation) _associations.removeObjectForKey("updateContainerID");
		if (_associations.objectForKey("onclick") != null && _type != null) {
			throw new WODynamicElementCreationException("You cannot bind both 'type' and 'onclick' at the same time.");
		}
		if (_updateContainerID != null && _type == null) {
			throw new WODynamicElementCreationException("If you bind 'updateContainerID', you must also bind 'type'.");
		}
	}

	public void appendAttributesToResponse(WOResponse response, WOContext context) {
		super.appendAttributesToResponse(response, context);
		AjaxFunctionLink._appendAttributesToResponse(response, context, _type, _updateContainerID);
	}
	
	public static void _appendAttributesToResponse(WOResponse response, WOContext context, WOAssociation typeAssociation, WOAssociation updateContainerIDAssociation) {
		WOComponent component = context.component();
		String type = (String) typeAssociation.valueInComponent(component);
		if (type != null) {
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
			if ("edit".equalsIgnoreCase(type)) {
				response.appendContentString("Edit");
			}
			else if ("cancel".equalsIgnoreCase(type)) {
				response.appendContentString("Cancel");
			}
			else if ("save".equalsIgnoreCase(type)) {
				response.appendContentString("Save");
			}
			else if ("update".equalsIgnoreCase(type)) {
				response.appendContentString("Update");
			}
			else {
				throw new WODynamicElementCreationException("Unknown AjaxInPlace type '" + type + "'.  Must be one of 'edit', 'cancel', 'save', or 'Update'.");
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
