package er.extensions.components.conditionals;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.woextensions.WOKeyValueConditional;

import er.extensions.eof.ERXEOControlUtilities;


/**
 * Same as the {@link WOKeyValueConditional}, but as a dynamic element. Matches if
 * a given key from the component matches the given value. Compares EOs by global ID.
 *
 * @author ak
 * @binding key
 * @binding value
 */
public class ERXKeyValueConditional extends ERXWOConditional {

	protected WOAssociation _key;
	protected WOAssociation _value;

	public ERXKeyValueConditional(String aName, NSDictionary aDict, WOElement aElement) {
		super(aName, aDict, aElement);
	}


	@Override
	protected void pullAssociations(NSDictionary<String, ? extends WOAssociation> dict) {
		_key = dict.objectForKey("key");
		_value = dict.objectForKey("value");
		if (_key == null || _value == null) {
			throw new WODynamicElementCreationException("key and value must be bound");
		}
	}

	@Override
	protected boolean conditionInComponent(WOComponent component) {
		String key = (String) _key.valueInComponent(component);
		Object v1 = (key != null ? component.valueForKeyPath(key) : null);
		Object v2 = _value.valueInComponent(component);
		boolean result;
		if ((v1 instanceof EOEnterpriseObject) && (v2 instanceof EOEnterpriseObject)) {
			result = ERXEOControlUtilities.eoEquals((EOEnterpriseObject) v1, (EOEnterpriseObject) v2);
		}
		else {
			result = (v1 == v2 || (v1 != null && v2 != null && v1.equals(v2)));
		}
		return result;
	}
}
