package er.extensions.components;

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
 * ERXSwitchEmbeddedPage allows you to wrap a section of your page and treat return
 * values from invokeAction as a replacement only for the this element and not
 * for the entire page. This allows you to write components that operate like a
 * sequence of top level elements, yet actually they live within a larger page.  This
 * is very similar to ERXEmbeddedPage except in it comes in the form of a switch
 * component.
 * 
 * @binding initialComponentName the name of the component to display
 * 
 * @author mschrag
 */
public class ERXSwitchEmbeddedPage extends WODynamicElement {
	private WOAssociation _initialComponentName;
	private NSMutableDictionary<String, WOAssociation> _componentAttributes;
	private NSMutableDictionary<String, WOElement> _componentCache;
	private WOElement _template;

	public ERXSwitchEmbeddedPage(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(null, null, null);
		_initialComponentName = associations.objectForKey("initialComponentName");
		if (_initialComponentName == null) {
			throw new WODynamicElementCreationException("initialComponentName is a required attribute.");
		}
		_componentAttributes = associations.mutableClone();
		_componentAttributes.removeObjectForKey("initialComponentName");

		_componentCache = new NSMutableDictionary<String, WOElement>();

		_template = template;
	}

	public String _elementNameInContext(WOContext wocontext) {
		com.webobjects.appserver.WOComponent wocomponent = wocontext.component();
		String s = null;
		Object obj = _initialComponentName.valueInComponent(wocomponent);
		if (obj != null) {
			s = obj.toString();
		}
		if (s == null || s.length() == 0) {
			throw new IllegalStateException("<" + getClass().getName() + "> : componentName not specified or initialComponentName association evaluated to null.");
		}
		return s;
	}

	public WOElement _realComponentWithName(String s, WOContext wocontext) {
		WOElement woelement;
		synchronized (this) {
			woelement = _componentCache.objectForKey(s);
			if (woelement == null) {
				woelement = WOApplication.application().dynamicElementWithName(s, _componentAttributes, _template, wocontext._languages());
				if (woelement == null) {
					throw new WODynamicElementCreationException("<" + getClass().getName() + "> : cannot find component or dynamic element named " + s);
				}
				_componentCache.setObjectForKey(woelement, s);
			}
		}
		return woelement;
	}

	public void _setRealComponentWithName(WOComponent realComponent, String name, WOContext context) {
		synchronized (this) {
			WOElement dynamicElement = WOApplication.application().dynamicElementWithName(realComponent.getClass().getName(), _componentAttributes, _template, context._languages());
			_componentCache.setObjectForKey(dynamicElement, name);
			realComponent._setParent(context.component(), _componentAttributes, _template);
			context.component()._setSubcomponent(realComponent, context.elementID());
		}
	}

	@Override
	public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		String s = _elementNameInContext(wocontext);
		wocontext.appendElementIDComponent(s.replace('.', '_'));
		WOElement woelement = _realComponentWithName(s, wocontext);
		woelement.takeValuesFromRequest(worequest, wocontext);
		wocontext.deleteLastElementIDComponent();
	}

	@Override
	public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
		String s = _elementNameInContext(wocontext);
		wocontext.appendElementIDComponent(s.replace('.', '_'));
		WOElement woelement = _realComponentWithName(s, wocontext);
		WOActionResults woactionresults = woelement.invokeAction(worequest, wocontext);
		if (woactionresults != null) {
			WOComponent nextComponent;
			if (woactionresults instanceof WOComponent) {
				nextComponent = (WOComponent) woactionresults;
			}
			else {
				ERXResponseComponent responseComponent = new ERXResponseComponent(wocontext);
				responseComponent.setActionResults(woactionresults);
				nextComponent = responseComponent;
			}
			_setRealComponentWithName(nextComponent, s, wocontext);
			woactionresults = wocontext.page();
		}
		wocontext.deleteLastElementIDComponent();
		return woactionresults;
	}

	@Override
	public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		String s = _elementNameInContext(wocontext);
		wocontext.appendElementIDComponent(s.replace('.', '_'));
		WOElement woelement = _realComponentWithName(s, wocontext);
		woelement.appendToResponse(woresponse, wocontext);
		wocontext.deleteLastElementIDComponent();
	}
}