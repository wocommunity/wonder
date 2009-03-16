package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;

/**
 * Sometimes it's handy to be able to print out debug entries during
 * the various stages of the RR loop. This can sometimes help diagnose 
 * when structure is changing on a page.
 * 
 * @author mschrag
 *
 * @binding name the name to print out 
 */
public class ERXRRLoopDebug extends WODynamicElement {
	private WOAssociation _name;

	public ERXRRLoopDebug(String name, NSDictionary associations, WOElement template) {
		super(name, associations, template);
		_name = (WOAssociation) associations.objectForKey("name");
		
		if (_name == null) {
			throw new WODynamicElementCreationException("'name' is a required binding.");
		}
	}

	public String name(WOContext context) {
		return (String) _name.valueInComponent(context.component());
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		System.out.println("ERXRRLoopDebug.takeValuesFromRequest: " + name(context));
		super.takeValuesFromRequest(request, context);
	}

	@Override
	public void appendToResponse(WOResponse request, WOContext context) {
		System.out.println("ERXRRLoopDebug.appendToResponse: " + name(context));
		super.appendToResponse(request, context);
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		System.out.println("ERXRRLoopDebug.invokeAction: " + name(context));
		return super.invokeAction(request, context);
	}

}
