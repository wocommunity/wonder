package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Needed when you have a component content and the state of the wrapper changes.
 * In that case, you might get the same element id and then switch components will get the
 * wrong value from their cache. It's very hard to explain, take a look at the ERXTabPanel :)
 * @binding id is to append to the element ID
 * @author ak
 */
public class ERXUniquingWrapper extends WODynamicGroup {
	private WOAssociation _id;

	public ERXUniquingWrapper(String name, NSDictionary associations, WOElement template) {
		super(name, associations, template);
		_id = (WOAssociation) ((NSMutableDictionary) associations).removeObjectForKey("id");
		if(_id == null) {
			throw new WODynamicElementCreationException("ERXUniquingWrapper needs an 'id' binding." );
		}
	}

	private String id(WOComponent component) {
		Object value = _id.valueInComponent(component);
		return value != null ? value.toString() : "" + hashCode();
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		context.appendElementIDComponent(id(context.component()));
		super.appendToResponse(response, context);
		context.deleteLastElementIDComponent();
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		context.appendElementIDComponent(id(context.component()));
		WOActionResults result = super.invokeAction(request, context);
		context.deleteLastElementIDComponent();
		return result;
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		context.appendElementIDComponent(id(context.component()));
		super.takeValuesFromRequest(request, context);
		context.deleteLastElementIDComponent();
	}

}
