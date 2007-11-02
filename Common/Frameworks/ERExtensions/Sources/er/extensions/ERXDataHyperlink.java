package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * ERXDataHyperlink works like a WOHyperlink except that instead of turning
 * unknown bindings into tag attributes, it instead passes them to the page
 * specified in pageName. For instance, if you have a pageName = "PersonPage"
 * and person = "$currentPerson", when the link is clicked, the next page will
 * be a PersonPage with setPerson(currentPerson) passed into it. You can pass in
 * as many bindings as you need.
 * 
 * @author mschrag
 */
public class ERXDataHyperlink extends ERXHyperlink {
	public ERXDataHyperlink(String s, NSDictionary nsdictionary, WOElement woelement) {
		super(s, nsdictionary, woelement);
	}

	@Override
	@SuppressWarnings("unchecked")
	public NSMutableDictionary<String, WOAssociation> nonUrlAttributeAssociations() {
		NSMutableDictionary<String, WOAssociation> nonUrlAttributeAssociations = super.nonUrlAttributeAssociations();
		if (_pageName != null) {
			nonUrlAttributeAssociations = new NSMutableDictionary();
		}
		return nonUrlAttributeAssociations;
	}

	@Override
	@SuppressWarnings("unchecked")
	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		WOActionResults results = super.invokeAction(worequest, wocontext);
		NSMutableDictionary<String, WOAssociation> nonUrlAttributeAssociations = super.nonUrlAttributeAssociations();
		if (results != null && _pageName != null) {
			WOComponent component = wocontext.component();
			for (String bindingName : nonUrlAttributeAssociations.allKeys()) {
				WOAssociation association = nonUrlAttributeAssociations.objectForKey(bindingName);
				Object value = association.valueInComponent(component);
				NSKeyValueCoding.Utility.takeValueForKey(results, value, bindingName);
			}
		}
		return results;
	}
}
