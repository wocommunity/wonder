package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

/**
 * Sets a keypath to a value. This is useful when you want a part of the page
 * rendered with different settings. The value is changed before its children
 * will be processed and is re-set to the old value after their processing.<br>
 * One way to use it would be in a D2W app, where <code>source="edit"</code>
 * and <code>destination=d2wContext.task</code> when you are on a list page.
 * Another would be the typical scenario where you iterate over keys of a
 * dictionary and you want the corresponding entry. Normally you'd need an
 * accessor for that, but using this component you would set
 * <code>source=myDict</code>, <code>sourceKeypath=currentyKey</code> and
 * <code>destination=currentEntry</code>.
 * 
 * @binding source the source object
 * @binding sourceKeypath (optional) the key path off the source object. If
 *          omitted, the source itself is used.
 * @binding destination the destination of the value
 * @binding destinationKeypath (optional) the key path off the destination
 *          object. If omitted, the source itself is used.
 * @author ak
 */
public class ERXKeyValueSetter extends ERXDynamicElement {
	public ERXKeyValueSetter(String name, NSDictionary<String, WOAssociation> associations, NSMutableArray<WOElement> children) {
		super(name, associations, children);
	}

	private Object setNewValue(WOContext context) {
		WOComponent component = context.component();
		Object newValue = valueForBinding("source", component);
		String sourceKey = stringValueForBinding("sourceKeypath", component);
		if (sourceKey != null) {
			newValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(newValue, sourceKey);
		}

		Object oldValue = valueForBinding("destination", component);
		String destinationKey = stringValueForBinding("destinationKeypath", component);
		if (destinationKey != null) {
			Object destinationTarget = oldValue;
			oldValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(destinationTarget, destinationKey);
			NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(destinationTarget, newValue, destinationKey);
		} else {
			setValueForBinding(newValue, "destination", component);
		}
		return oldValue;
	}

	private void restoreOldValue(Object oldValue, WOContext context) {
		WOComponent component = context.component();
		String destinationKey = stringValueForBinding("destinationKeypath", component);
		if (destinationKey != null) {
			Object destinationTarget = oldValue;
			oldValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(destinationTarget, destinationKey);
			NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(destinationTarget, oldValue, destinationKey);
		} else {
			setValueForBinding(oldValue, "destination", component);
		}
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		Object oldValue = setNewValue(context);
		super.takeValuesFromRequest(request, context);
		restoreOldValue(oldValue, context);
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		Object oldValue = setNewValue(context);
		WOActionResults result = super.invokeAction(request, context);
		restoreOldValue(oldValue, context);
		return result;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		Object oldValue = setNewValue(context);
		super.appendToResponse(response, context);
		restoreOldValue(oldValue, context);
	}
}
