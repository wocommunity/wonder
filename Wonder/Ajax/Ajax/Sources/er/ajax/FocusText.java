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
 * Focus text is a convenience version of ERXWOText that provides
 * support for grabbing default focus via javascript.
 * 
 * @author mschrag
 * 
 * @binding id the id the textfield
 * @binding selectAll if true, the text will default to be selected
 */
public class FocusText extends ERXWOText {
	protected WOAssociation _selectAll;

	public FocusText(String tagname, NSDictionary nsdictionary, WOElement woelement) {
		super(tagname, nsdictionary, woelement);

		if (_id == null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> id is a required binding.");
		}
		_selectAll = (WOAssociation) _associations.removeObjectForKey("selectAll");
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");

		super.appendToResponse(response, context);

		WOComponent component = context.component();
		String id = (String) _id.valueInComponent(component);
		boolean selectAll = (_selectAll != null && _selectAll.booleanValueInComponent(component));
		FocusTextField.appendFocusAndSelectToResponse(response, context, id, selectAll);
	}
}
