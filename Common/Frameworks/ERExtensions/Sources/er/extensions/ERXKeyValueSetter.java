package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;

/**
 * Sets a keypath to a value. This is useful when you want a part of the page
 * rendered with different settings. The value is re-set to the old value
 * outside of the component.<br>
 * One way to use it would be in a D2W app, where <code>source="edit"</code>
 * and <code>destination=d2wContext.task</code> when you are on a list page.
 * Another would the the typical scenario where you iterate over keys of
 * dictionary, and you want the corresponding entry. Normally you'd need an
 * accessor for that, but using this component, you would set
 * <code>source=myDict</code>, <code>sourceKeypath=currentyKey</code> and
 * <code>destination=currentEntry</code>
 * 
 * @binding source the source object
 * @binding sourceKeypath (optional) the key path off the source object. If
 *          omitted, the source itself is used.
 * @binding destination the destination of the value
 * @binding destinationKeypath (optional) the key path off the destination
 *          object. If omitted, the source itself is used.
 * @author ak
 * 
 */
public class ERXKeyValueSetter extends WODynamicGroup {

	WOAssociation source;
	WOAssociation destination;
	WOAssociation sourceKeypath;
	WOAssociation destinationKeypath;

	public ERXKeyValueSetter(String arg0, NSDictionary arg1, NSMutableArray arg2) {
		super(arg0, arg1, arg2);
		source = (WOAssociation) arg1.objectForKey("source");
		destination = (WOAssociation) arg1.objectForKey("destination");
		sourceKeypath = (WOAssociation) arg1.objectForKey("sourceKeypath");
		destinationKeypath = (WOAssociation) arg1.objectForKey("destinationKeypath");
		if (source == null || destination == null) {
			throw new IllegalStateException("source and destination must be bound");
		}
	}

	private Object setup(WOComponent component) {
		Object newValue = source.valueInComponent(component);
		String sourceKey = null;
		if (sourceKeypath != null) {
			sourceKey = (String) sourceKeypath.valueInComponent(component);
		}
		if (sourceKey != null) {
			newValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(newValue, sourceKey);
		}

		String destinationKey = null;
		Object oldValue = destination.valueInComponent(component);
		if (destinationKeypath != null) {
			destinationKey = (String) destinationKeypath.valueInComponent(component);
		}
		if (destinationKey != null) {
			Object destinationTarget = oldValue;
			oldValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(destinationTarget, destinationKey);
			NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(destinationTarget, newValue, destinationKey);
		}
		else {
			destination.setValue(newValue, component);
		}
		return oldValue;
	}

	private void cleanup(Object oldValue, WOComponent component) {
		String destinationKey = null;
		if (destinationKeypath != null) {
			destinationKey = (String) destinationKeypath.valueInComponent(component);
		}
		if (destinationKey != null) {
			Object destinationTarget = oldValue;
			oldValue = NSKeyValueCodingAdditions.Utility.valueForKeyPath(destinationTarget, destinationKey);
			NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(destinationTarget, oldValue, destinationKey);
		}
		else {
			destination.setValue(oldValue, component);
		}
	}

	public void takeValuesFromRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		Object old = setup(component);
		super.takeValuesFromRequest(request, context);
		cleanup(old, component);
	}

	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOComponent component = context.component();
		Object old = setup(component);
		WOActionResults result = super.invokeAction(request, context);
		cleanup(old, component);
		return result;
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		Object old = setup(component);
		super.appendToResponse(response, context);
		cleanup(old, component);
	}

}
