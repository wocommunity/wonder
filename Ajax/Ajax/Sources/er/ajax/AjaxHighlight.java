package er.ajax;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSet;

import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXWOContext;

/**
 * AjaxHighlight provides a convenient way to queue up an object as 
 * highlighted so that it gets a highlight effect when the next 
 * page renders.
 * 
 * In the action prior to returning the page that will show a highlight,
 * you can call AjaxHighlight.highlight(theObject).  The object you 
 * highlight could be a String, an EO, or whatever object makes sense
 * in your context.
 * 
 * Then you simply bind value = .. on this component on the following
 * page.  If the value matches an object that was flagged as highlighted,
 * the container you specify will receive the highlight effect.  This
 * component can also generate its own container if you do not specify
 * another container id. 
 *   
 * @binding value the value to check for highlighting
 * @binding id the optional id to highlight (if blank, a container will be generated)
 * @binding elementName the element name of the generated container (if specified, a container will be generated); defaults to div
 * @binding effect the name of the scriptaculous effect to render (defaults to "Highlight")
 * @binding class the CSS class of the generated container
 * @binding style the CSS style of the generated container
 * @binding duration passed through to Effect.Xxx in the options map
 * 
 * @author mschrag
 */
public class AjaxHighlight extends WODynamicGroup {
	private static final String HIGHLIGHTED_KEY = "er.ajax.AjaxHighlight.highlighted";
	private NSDictionary _associations;
	private WOAssociation _value;
	private WOAssociation _id;
	private WOAssociation _elementName;
	private WOAssociation _effect;

	public AjaxHighlight(String name, NSDictionary associations, WOElement template) {
		super(name, associations, template);
		_associations = associations;
		_value = (WOAssociation) associations.valueForKey("value");
		if (_value == null) {
			throw new WODynamicElementCreationException("'value' is a required binding.");
		}
		_elementName = (WOAssociation) associations.valueForKey("elementName");
		_id = (WOAssociation) associations.valueForKey("id");
		_effect = (WOAssociation) associations.valueForKey("effect");
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
		AjaxUtils.addScriptResourceInHead(context, response, "scriptaculous.js");
		WOComponent component = context.component();
		boolean generateContainer = (_id == null || _elementName != null);
		String elementName;
		if (_elementName == null) {
			elementName = "div";
		}
		else {
			elementName = (String) _elementName.valueInComponent(component);
		}
		String id;
		if (_id == null) {
			id = AjaxUtils.toSafeElementID(context.elementID());
		}
		else {
			id = (String) _id.valueInComponent(component);
		}
		if (generateContainer) {
			response.appendContentString("<");
			response.appendContentString(elementName);
			response._appendTagAttributeAndValue("id", id, true);
			AjaxUtils.appendTagAttributeAndValue(response, context, component, _associations, "class");
			AjaxUtils.appendTagAttributeAndValue(response, context, component, _associations, "style");
			response.appendContentString(">");
		}
		super.appendToResponse(response, context);
		if (generateContainer) {
			response.appendContentString("</");
			response.appendContentString(elementName);
			response.appendContentString(">");
		}

		if (_value != null) {
			Object value = _value.valueInComponent(component);
			if (value != null) {
				boolean highlighted = isHighlighted(value);
				if (highlighted) {
					AjaxUtils.appendScriptHeader(response);
					String effect;
					if (_effect == null) {
						effect = "Highlight";
					}
					else {
						effect = (String) _effect.valueInComponent(component);
					}
					response.appendContentString("new Effect.");
					response.appendContentString(effect);
					response.appendContentString("('");
					response.appendContentString(id);
					response.appendContentString("',");

					NSMutableArray ajaxOptionsArray = new NSMutableArray();
					ajaxOptionsArray.addObject(new AjaxOption("duration", AjaxOption.NUMBER));

					NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, _associations);
					options.setObjectForKey("'end'", "queue");

					AjaxOptions.appendToResponse(options, response, context);

					response.appendContentString(");");

					AjaxUtils.appendScriptFooter(response);
				}
			}
		}
	}

	protected static Object highlightedValue(Object obj) {
		Object highlightedValue = ERXEOControlUtilities.convertEOtoGID(obj);
		return highlightedValue;
	}

	public static boolean isHighlighted(Object obj) {
		boolean highlighted = false;
		if (obj != null) {
			NSSet highlightedObjects = (NSSet) ERXWOContext.contextDictionary().valueForKey(AjaxHighlight.HIGHLIGHTED_KEY);
			if (highlightedObjects != null) {
				highlighted = highlightedObjects.containsObject(highlightedValue(obj));
			}
		}
		return highlighted;
	}

	public static final void highlight(Object obj) {
		if (obj != null) {
			NSMutableSet highlightedObjects = (NSMutableSet) ERXWOContext.contextDictionary().valueForKey(AjaxHighlight.HIGHLIGHTED_KEY);
			if (highlightedObjects == null) {
				highlightedObjects = new NSMutableSet();
				ERXWOContext.contextDictionary().takeValueForKey(highlightedObjects, AjaxHighlight.HIGHLIGHTED_KEY);
			}
			highlightedObjects.addObject(highlightedValue(obj));
		}
	}
}
