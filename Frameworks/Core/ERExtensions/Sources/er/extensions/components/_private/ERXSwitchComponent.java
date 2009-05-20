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
 * Variant of WOSwitchComponent that replaces the plain-text names in the
 * element ID  with a number, in case you don't like advertising your
 * component names or like shorter URLs.
 * 
 * @author ak
 */
public class ERXSwitchComponent extends WODynamicElement {
	WOAssociation componentName;
	public NSMutableDictionary componentAttributes;
	NSMutableDictionary componentCache;
	WOElement template;

	public ERXSwitchComponent(String paramString, NSDictionary paramNSDictionary, WOElement paramWOElement) {
		super(null, null, null);
		this.componentName = ((WOAssociation) paramNSDictionary.objectForKey("WOComponentName"));
		if (this.componentName == null) {
			this.componentName = ((WOAssociation) paramNSDictionary.objectForKey("_componentName"));
			if (this.componentName == null)
				throw new WODynamicElementCreationException("<" + getClass().getName() + "> : '" + "WOComponentName" + "' attribute missing.");
		}

		if (paramNSDictionary != null) {
			this.componentAttributes = paramNSDictionary.mutableClone();
			this.componentAttributes.removeObjectForKey("WOComponentName");
			this.componentAttributes.removeObjectForKey("_componentName");
		}
		else {
			this.componentAttributes = new NSMutableDictionary();
		}

		this.componentCache = new NSMutableDictionary();

		this.template = paramWOElement;
	}

	public String toString() {
		return "<WOSwitchComponent  componentName: " + ((this.componentName != null) ? this.componentName.toString() : "null") + " componentAttributes: " + ((this.componentAttributes != null) ? this.componentAttributes.toString() : "null") + " componentCache: " + ((this.componentCache != null) ? this.componentCache.toString() : "null") + " children: " + ((this.template != null) ? this.template.toString() : "null") + ">";
	}

	private static NSMutableDictionary<String, String> _namesByID = new NSMutableDictionary<String, String>();
	private static NSMutableDictionary<String, String> _idsByName = new NSMutableDictionary<String, String>();

	public String _elementNameInContext(WOContext paramWOContext) {
		WOComponent localWOComponent = paramWOContext.component();
		String name = null;
		Object localObject = this.componentName.valueInComponent(localWOComponent);
		if (localObject != null)
			name = localObject.toString();

		if ((name == null) || (name.length() == 0)) {
			throw new IllegalStateException("<" + getClass().getName() + "> : componentName not specified or componentName association evaluated to null.");
		}
		synchronized (_namesByID) {
			String id = _idsByName.objectForKey(name);
			if (id == null) {
				id = _namesByID.count() + "";
				_idsByName.setObjectForKey(id, name);
				_namesByID.setObjectForKey(name, id);
			}
			name = id;
		}
		return name;
	}

	public WOElement _realComponentWithName(String paramString, WOContext paramWOContext) {
		WOElement localWOElement;
		synchronized (_namesByID) {
			paramString = _namesByID.objectForKey(paramString);
		}
		synchronized (this) {
			localWOElement = (WOElement) this.componentCache.objectForKey(paramString);

			if (localWOElement == null) {
				localWOElement = WOApplication.application().dynamicElementWithName(paramString, this.componentAttributes, this.template, paramWOContext._languages());

				if (localWOElement == null) {
					throw new WODynamicElementCreationException("<" + getClass().getName() + "> : cannot find component or dynamic element named " + paramString);
				}

				this.componentCache.setObjectForKey(localWOElement, paramString);
			}
		}

		return localWOElement;
	}

	public void takeValuesFromRequest(WORequest paramWORequest, WOContext paramWOContext) {
		String str = _elementNameInContext(paramWOContext);

		paramWOContext.appendElementIDComponent(str);

		WOElement localWOElement = _realComponentWithName(str, paramWOContext);

		localWOElement.takeValuesFromRequest(paramWORequest, paramWOContext);

		paramWOContext.deleteLastElementIDComponent();
	}

	public WOActionResults invokeAction(WORequest paramWORequest, WOContext paramWOContext) {
		String str = _elementNameInContext(paramWOContext);

		paramWOContext.appendElementIDComponent(str);

		WOElement localWOElement = _realComponentWithName(str, paramWOContext);

		WOActionResults localWOActionResults = localWOElement.invokeAction(paramWORequest, paramWOContext);

		paramWOContext.deleteLastElementIDComponent();

		return localWOActionResults;
	}

	public void appendToResponse(WOResponse paramWOResponse, WOContext paramWOContext) {
		String str = _elementNameInContext(paramWOContext);

		paramWOContext.appendElementIDComponent(str);

		WOElement localWOElement = _realComponentWithName(str, paramWOContext);

		localWOElement.appendToResponse(paramWOResponse, paramWOContext);

		paramWOContext.deleteLastElementIDComponent();
	}
}
