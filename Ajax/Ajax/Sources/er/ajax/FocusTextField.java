package er.ajax;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXWOTextField;

/**
 * Focus text field is a convenience version of ERXWOTextField that provides
 * support for grabbing default focus via javascript.
 * 
 * @author mschrag
 * 
 * @binding id the id the textfield
 * @binding selectAll if true, the text will default to be selected
 * @binding focus if false, focus will not be grabbed
 * @binding onEnter javascript to execute when the enter key is pressed
 */
public class FocusTextField extends ERXWOTextField {
	protected WOAssociation _id;
	protected WOAssociation _selectAll;
	protected WOAssociation _focus;
	protected WOAssociation _onEnter;
	protected WOAssociation _onKeyPress;

	public FocusTextField(String tagname, NSDictionary nsdictionary, WOElement woelement) {
		super(tagname, nsdictionary, woelement);

		_id = (WOAssociation) nsdictionary.valueForKey("id");
		if (_id == null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> id is a required binding.");
		}
		//_associations.setObjectForKey(_id, ")
		_selectAll = (WOAssociation) _associations.removeObjectForKey("selectAll");
		_focus = (WOAssociation) _associations.removeObjectForKey("focus");
		_onEnter = (WOAssociation) _associations.removeObjectForKey("onEnter");
		_onKeyPress = (WOAssociation) _associations.removeObjectForKey("onkeypress");
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");

		super.appendToResponse(response, context);

		WOComponent component = context.component();
		boolean focus = (_focus == null || _focus.booleanValueInComponent(component));
		boolean selectAll = (_selectAll != null && _selectAll.booleanValueInComponent(component));
		String id = (String) _id.valueInComponent(component);
		String onEnterScript = (_onEnter != null) ? (String)_onEnter.valueInComponent(component) : null;
		FocusTextField.appendJavascriptToResponse(response, context, id, focus, selectAll, onEnterScript);
	}

	protected void _appendAttributesFromAssociationsToResponse(WOResponse response, WOContext wocontext, NSDictionary nsdictionary) {
		super._appendAttributesFromAssociationsToResponse(response, wocontext, nsdictionary);
		WOComponent component = wocontext.component();
		String onKeyPress = (_onKeyPress != null) ? (String) _onKeyPress.valueInComponent(component) : null;
		String onEnterScript = (_onEnter != null) ? (String) _onEnter.valueInComponent(component) : null;
		String id = (String) _id.valueInComponent(component);
		FocusTextField._appendAttributesFromAssociationsToResponse(response, wocontext, id, onKeyPress, onEnterScript);
	}

	public static void _appendAttributesFromAssociationsToResponse(WOResponse response, WOContext wocontext, String id, String onKeyPress, String onEnterScript) {
		WOComponent component = wocontext.component();
		if (onKeyPress != null || onEnterScript != null) {
			response.appendContentString(" onkeypress = \"");
			if (onKeyPress != null) {
				response.appendContentString(onKeyPress);
			}
			if (onEnterScript != null) {
				if (onKeyPress != null) {
					response.appendContentString("; ");
				}
				response.appendContentString(id + "SubmitOnEnter(event);");
			}
			response.appendContentString("\"");
		}
	}
	
	public static void appendJavascriptToResponse(WOResponse response, WOContext context, String id, boolean focus, boolean selectAll, String onEnterScript) {
		WOComponent component = context.component();
		AjaxUtils.appendScriptHeader(response);
		if (focus || selectAll) {
			response.appendContentString("setTimeout(function() { ");
		}
		if (focus) {
			response.appendContentString("Field.focus('" + id + "');");
		}
		if (selectAll) {
			response.appendContentString("Field.select('" + id + "');");
		}
		if (focus || selectAll) {
			response.appendContentString(" }, 10);");
		}
		if (onEnterScript != null) {
			response.appendContentString(id + "SubmitOnEnter = function(e) { var keynum = Event.keyValue(e); if (keynum == 13 || keynum == 3) { ");
			response.appendContentString(onEnterScript);
			response.appendContentString("; Event.stop(e); } }");
		}
		AjaxUtils.appendScriptFooter(response);
	}
}
