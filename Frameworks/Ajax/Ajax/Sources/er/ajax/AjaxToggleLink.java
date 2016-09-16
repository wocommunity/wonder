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
 * AjaxToggleLink provides a wrapper for calling Effect.toggle when clicking a link.
 * 
 * @binding disabled if true, disables the link
 * @binding effect the name of the effect to use (defaults to "blind")
 * @binding duration the duration of the effect
 * @binding toggleID the id of the container to toggle
 * 
 * @author mschrag
 */
public class AjaxToggleLink extends WOHTMLDynamicElement {
	private WOAssociation _disabled;
	private WOAssociation _effect;
	private WOAssociation _toggleID;
	private WOAssociation _duration;

	public AjaxToggleLink(String aName, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super("a", AjaxToggleLink.processAssociations(associations), template);
		_effect = _associations.removeObjectForKey("effect");
		_duration = _associations.removeObjectForKey("duration");
		_toggleID = _associations.removeObjectForKey("toggleID");
		if (_associations.objectForKey("onclick") != null) {
			throw new WODynamicElementCreationException("You cannot bind 'onclick' on AjaxToggleLink.");
		}
		if (_toggleID == null) {
			throw new WODynamicElementCreationException("You must bind 'toggleID'.");
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
			AjaxToggleLink._appendAttributesToResponse(response, context, _toggleID, _effect, _duration);
		}
	}

	public static void _appendAttributesToResponse(WOResponse response, WOContext context, WOAssociation toggleIDAssociation, WOAssociation effectAssociation, WOAssociation durationAssociation) {
		WOComponent component = context.component();
		String effect = null;
		if (effectAssociation != null) {
			effect = (String) effectAssociation.valueInComponent(component);
		}
		if (effect == null) {
			effect = "blind";
		}

		String toggleID = (String) toggleIDAssociation.valueInComponent(component);

		// PROTOTYPE EFFECTS
		response.appendContentString(" onclick = \"Effect.toggle($wi('");
		response.appendContentString(toggleID);
		response.appendContentString("'), '");
		response.appendContentString(effect);
		response.appendContentString("', ");
		
		NSMutableDictionary<String, WOAssociation> options = new NSMutableDictionary<>();
		if (durationAssociation != null) {
			options.setObjectForKey(durationAssociation, "duration");
		}

		AjaxOptions.appendToResponse(options, response, context);
		response.appendContentString(")\"");
	}

	protected static NSDictionary<String, WOAssociation> processAssociations(NSDictionary<String, WOAssociation> associations) {
		NSMutableDictionary<String, WOAssociation> mutableAssociations = (NSMutableDictionary<String, WOAssociation>) associations;
		mutableAssociations.setObjectForKey(new WOConstantValueAssociation("javascript:void(0)"), "href");
		return mutableAssociations;
	}
}
