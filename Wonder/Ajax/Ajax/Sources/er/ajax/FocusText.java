package er.ajax;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;

import er.extensions.ERXWOText;

/**
 * Focus text is a convenience version of ERXWOText that provides support for grabbing default focus via javascript.
 * 
 * @author mschrag
 * 
 * @binding id the id the textfield
 * @binding selectAll if true, the text will default to be selected
 * @binding focus if false, focus will not be grabbed
 * @binding onEnter javascript to execute when the enter key is pressed
 */
public class FocusText extends ERXWOText {
	protected WOAssociation _selectAll;
	protected WOAssociation _focus;
	protected WOAssociation _onEnter;
	protected WOAssociation _onKeyPress;

	public FocusText(String tagname, NSDictionary nsdictionary, WOElement woelement) {
		super(tagname, nsdictionary, woelement);

		if (_id == null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> id is a required binding.");
		}
		_selectAll = (WOAssociation) _associations.removeObjectForKey("selectAll");
		_focus = (WOAssociation) _associations.removeObjectForKey("focus");
		_onEnter = (WOAssociation) _associations.removeObjectForKey("onEnter");
		_onKeyPress = (WOAssociation) _associations.removeObjectForKey("onkeypress");
	}

	protected void _appendAttributesFromAssociationsToResponse(WOResponse woresponse, WOContext wocontext, NSDictionary nsdictionary) {
		super._appendAttributesFromAssociationsToResponse(woresponse, wocontext, nsdictionary);
		WOComponent component = wocontext.component();
		String onKeyPress = (_onKeyPress != null) ? (String) _onKeyPress.valueInComponent(component) : null;
		String onEnterScript = (_onEnter != null) ? (String) _onEnter.valueInComponent(component) : null;
		String id = (String) _id.valueInComponent(component);
		FocusTextField._appendAttributesFromAssociationsToResponse(woresponse, wocontext, id, onKeyPress, onEnterScript);
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");

		super.appendToResponse(response, context);

		WOComponent component = context.component();
		boolean focus = (_focus == null || _focus.booleanValueInComponent(component));
		String id = (String) _id.valueInComponent(component);
		boolean selectAll = (_selectAll != null && _selectAll.booleanValueInComponent(component));
		String onEnterScript = (_onEnter != null) ? (String) _onEnter.valueInComponent(component) : null;
		FocusTextField.appendJavascriptToResponse(response, context, id, focus, selectAll, onEnterScript);
	}
}
