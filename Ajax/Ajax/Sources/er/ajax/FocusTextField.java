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
 */
public class FocusTextField extends ERXWOTextField {
	protected WOAssociation _id;
	protected WOAssociation _selectAll;

	public FocusTextField(String tagname, NSDictionary nsdictionary, WOElement woelement) {
		super("input", nsdictionary, woelement);

		_id = (WOAssociation) _associations.valueForKey("id");
		if (_id == null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> id is a required binding.");
		}
		_selectAll = (WOAssociation)_associations.removeObjectForKey("selectAll");
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");

		super.appendToResponse(response, context);

		WOComponent component = context.component();
		String id = (String) _id.valueInComponent(component);
		if (id != null) {
			AjaxUtils.appendScriptHeader(response);
			response.appendContentString("$('" + id + "').focus();");
			if (_selectAll != null && _selectAll.booleanValueInComponent(component)) {
				response.appendContentString("$('" + id + "').select();");
			}
			AjaxUtils.appendScriptFooter(response);
		}
	}
}
