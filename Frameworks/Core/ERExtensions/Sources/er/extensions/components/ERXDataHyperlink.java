package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.components._private.ERXHyperlink;

/**
 * ERXDataHyperlink works like a WOHyperlink except that instead of turning
 * unknown bindings into tag attributes, it instead passes them to the page
 * specified in pageName. For instance, if you have a pageName = "PersonPage"
 * and person = "$currentPerson", when the link is clicked, the next page will
 * be a PersonPage with setPerson(currentPerson) passed into it.
 * 
 * You can pass in as many bindings as you need, but there are some bindings
 * that are used by this element by itself and thus are not passed to the page:
 * The ones defined by WOHyperlink and additionally "id", "class", "onclick" and
 * "otherTagString", which are handled like bindings on a stock WOHyperlink.
 * 
 * If the "pageName" binding is not set, ERXDataHyperlink will behave like a
 * regular WOHyperlink.
 * 
 * @author mschrag
 * @author timo
 */
public class ERXDataHyperlink extends ERXHyperlink {
	private final static NSArray<String> BINDINGS_HANDLED_BY_SUPER_CLASS = new NSArray<>(new String[] {"id", "class", "onclick", "otherTagString"});
	private NSMutableDictionary<String, WOAssociation> _bindingAssociations;
	
	@SuppressWarnings("unchecked")
	public ERXDataHyperlink(String s, NSDictionary associations, WOElement woelement) {
		// The WOHyperlink's constructor will remove all associations from
		// super._associations that directly affect WOHyperlink
		super(s, associations, woelement);
		if (_pageName != null) {
			_bindingAssociations = new NSMutableDictionary<>();
			NSMutableDictionary<String, WOAssociation> superAssociations = super._associations;
			for (String key : superAssociations.allKeys()) {
				// leave anything listed in BINDINGS_HANDLED_BY_SUPERCLASS in
				// super._associations to be handled by the superclass.
				if (!BINDINGS_HANDLED_BY_SUPER_CLASS.containsObject(key)) {
					// Move all other associations to our own _bindingAssociations,
					// so they can later be used as bindings for the page specified
					// in pageName.
					_bindingAssociations.setObjectForKey(superAssociations.removeObjectForKey(key), key);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		WOActionResults results = super.invokeAction(worequest, wocontext);
		if (results != null && _pageName != null) {
			WOComponent component = wocontext.component();
			for (String bindingName : _bindingAssociations.allKeys()) {
				WOAssociation association = _bindingAssociations.objectForKey(bindingName);
				Object value = association.valueInComponent(component);
				NSKeyValueCoding.Utility.takeValueForKey(results, value, bindingName);
			}
		}
		return results;
	}
}
