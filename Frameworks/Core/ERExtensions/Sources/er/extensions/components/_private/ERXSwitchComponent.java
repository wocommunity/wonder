package er.extensions.components._private;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * Variant of WOSwitchComponent that replaces the plain-text component names in
 * the element ID with a number, in case you don't like advertising your
 * component names or just like shorter URLs.<br>
 * Gets installed automatically by ERXPatcher.
 * @author ak
 */
public class ERXSwitchComponent extends WODynamicElement {
	WOAssociation componentName;
	public NSMutableDictionary componentAttributes;
	NSMutableDictionary componentCache;
	private NSMutableDictionary<String, String> elementIDByName;
	WOElement template;

	public ERXSwitchComponent(String paramString, NSDictionary paramNSDictionary, WOElement paramWOElement) {
		super(null, null, null);
		componentName = ((WOAssociation) paramNSDictionary.objectForKey("WOComponentName"));
		if (componentName == null) {
			componentName = ((WOAssociation) paramNSDictionary.objectForKey("_componentName"));
			if (componentName == null)
				throw new WODynamicElementCreationException("<" + getClass().getName() + "> : '" + "WOComponentName" + "' attribute missing.");
		}

		componentAttributes = paramNSDictionary.mutableClone();
		componentAttributes.removeObjectForKey("WOComponentName");
		componentAttributes.removeObjectForKey("_componentName");
		componentCache = new NSMutableDictionary();
		template = paramWOElement;
		elementIDByName = new NSMutableDictionary<>();
	}

	@Override
	public String toString() {
		return "<" + getClass().getName() + " componentName: " + componentName 
		+ " componentAttributes: " + componentAttributes 
		+ " componentCache: " + componentCache + " children: " + template + ">";
	}

	private String componentNameInContext(WOComponent localWOComponent) {
		String name = null;
		Object localObject = componentName.valueInComponent(localWOComponent);
		if (localObject != null)
			name = localObject.toString();

		if ((name == null) || (name.length() == 0)) {
			throw new IllegalStateException("<" + getClass().getName() + "> : componentName not specified or componentName association evaluated to null.");
		}
		return name;
	}

	public String _elementNameInContext(String name, WOContext paramWOContext) {
		synchronized (this) {
			String id = elementIDByName.objectForKey(name);
			if (id == null) {
				id = elementIDByName.count() + "";
				elementIDByName.setObjectForKey(id, name);
			}
			name = id;
		}
		return name;
	}

	public WOElement _realComponentWithName(String name, String elementID, WOContext paramWOContext) {
		WOElement localWOElement;
		synchronized (this) {
			localWOElement = (WOElement) componentCache.objectForKey(elementID);

			if (localWOElement == null) {
				localWOElement = WOApplication.application().dynamicElementWithName(name, componentAttributes, template, paramWOContext._languages());

				if (localWOElement == null) {
					throw new WODynamicElementCreationException("<" + getClass().getName() + "> : cannot find component or dynamic element named " + name);
				}

				componentCache.setObjectForKey(localWOElement, elementID);
			}
		}

		return localWOElement;
	}

	@Override
	public void takeValuesFromRequest(WORequest paramWORequest, WOContext paramWOContext) {
		String name = componentNameInContext(paramWOContext.component());
		String id = _elementNameInContext(name, paramWOContext);

		paramWOContext.appendElementIDComponent(id);

		WOElement localWOElement = _realComponentWithName(name, id, paramWOContext);

		localWOElement.takeValuesFromRequest(paramWORequest, paramWOContext);

		paramWOContext.deleteLastElementIDComponent();
	}

	@Override
	public WOActionResults invokeAction(WORequest paramWORequest, WOContext paramWOContext) {
		String name = componentNameInContext(paramWOContext.component());
		String id = _elementNameInContext(name, paramWOContext);

		paramWOContext.appendElementIDComponent(id);

		WOElement localWOElement = _realComponentWithName(name, id, paramWOContext);

		WOActionResults localWOActionResults = localWOElement.invokeAction(paramWORequest, paramWOContext);

		paramWOContext.deleteLastElementIDComponent();

		return localWOActionResults;
	}

	@Override
	public void appendToResponse(WOResponse paramWOResponse, WOContext paramWOContext) {
		String name = componentNameInContext(paramWOContext.component());
		String id = _elementNameInContext(name, paramWOContext);

		paramWOContext.appendElementIDComponent(id);

		WOElement localWOElement = _realComponentWithName(name, id, paramWOContext);

		localWOElement.appendToResponse(paramWOResponse, paramWOContext);

		paramWOContext.deleteLastElementIDComponent();
	}
}
