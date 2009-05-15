package er.extensions.components.partials;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXResponse;

/**
 * Marks a place in the current response.
 * 
 * @author ak
 * 
 */
public class ERXPartialMarker extends WODynamicElement {

	WOAssociation _key;

	public ERXPartialMarker(String s, NSDictionary nsdictionary, WOElement woelement) {
		super(s, nsdictionary, woelement);
		_key = (WOAssociation) nsdictionary.objectForKey("key");
	}

	@Override
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		String key = (String) _key.valueInComponent(wocontext.component());
		((ERXResponse) woresponse).mark(key);
	}
}
