package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

/**
 * Sets a keypath to a value. This is useful when you want a part of the page rendered with different settings.
 * Think D2W task=inspect vs task=edit...
 * @author ak
 *
 */
public class ERXKeyValueSetter extends WODynamicGroup {

	WOAssociation source;
	WOAssociation destination;

	public ERXKeyValueSetter(String arg0, NSDictionary arg1, NSMutableArray arg2) {
		super(arg0, arg1, arg2);
		source = (WOAssociation) arg1.objectForKey("source");
		destination = (WOAssociation) arg1.objectForKey("destination");
		if (source == null || destination == null) {
			throw new IllegalStateException("source and destination must be bound");
		}
		if (!(source.isValueSettable() && destination.isValueSettable())) {
			throw new IllegalStateException("source and destination must be settable");
		}
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		Object old = destination.valueInComponent(component);
		destination.setValue(source.valueInComponent(component), component);
		try {
			super.appendToResponse(response, context);
		}
		finally {
			destination.setValue(old, component);
		}
	}

	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOComponent component = context.component();
		Object old = destination.valueInComponent(component);
		destination.setValue(source.valueInComponent(component), component);
		try {
			return super.invokeAction(request, context);
		}
		finally {
			destination.setValue(old, component);
		}
	}

	public void takeValuesFromRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		Object old = destination.valueInComponent(component);
		destination.setValue(source.valueInComponent(component), component);
		try {
			super.takeValuesFromRequest(request, context);
		}
		finally {
			destination.setValue(old, component);
		}
	}

}
