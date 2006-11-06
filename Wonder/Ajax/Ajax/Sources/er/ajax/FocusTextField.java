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
 */
public class FocusTextField extends ERXWOTextField {
	protected WOAssociation _id;
	protected WOAssociation _selectAll;
	protected WOAssociation _focus;

	public FocusTextField(String tagname, NSDictionary nsdictionary, WOElement woelement) {
		super(tagname, nsdictionary, woelement);

		_id = (WOAssociation) _associations.valueForKey("id");
		if (_id == null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> id is a required binding.");
		}
		_selectAll = (WOAssociation) _associations.removeObjectForKey("selectAll");
		_focus = (WOAssociation) _associations.removeObjectForKey("focus");
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");

		super.appendToResponse(response, context);

		WOComponent component = context.component();
		boolean focus = (_focus == null || _focus.booleanValueInComponent(component));
		if (focus) {
			String id = (String) _id.valueInComponent(component);
			boolean selectAll = (_selectAll != null && _selectAll.booleanValueInComponent(component));
			FocusTextField.appendFocusAndSelectToResponse(response, context, id, selectAll);
		}
	}

	public static void appendFocusAndSelectToResponse(WOResponse response, WOContext context, String id, boolean selectAll) {
		WOComponent component = context.component();
		AjaxUtils.appendScriptHeader(response);
		response.appendContentString("$('" + id + "').focus();");
		if (selectAll) {
			response.appendContentString("$('" + id + "').select();");
		}
		AjaxUtils.appendScriptFooter(response);
	}
}
