package er.ajax;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXEOControlUtilities;
import er.extensions.ERXWOContext;

/**
 * AjaxHighlight provides a convenient way to queue up an object as highlighted so that it gets a highlight effect when
 * the next page renders.
 * 
 * In the action prior to returning the page that will show a highlight, you can call
 * AjaxHighlight.highlight(theObject). The object you highlight could be a String, an EO, or whatever object makes sense
 * in your context.
 * 
 * Then you simply bind value = .. on this component on the following page. If the value matches an object that was
 * flagged as highlighted, the container you specify will receive the highlight effect. This component can also generate
 * its own container if you do not specify another container id.
 * 
 * @binding value the value to check for highlighting
 * @binding id the optional id to highlight (if blank, a container will be generated)
 * @binding elementName the element name of the generated container (if specified, a container will be generated);
 *          defaults to div
 * @binding effect the name of the scriptaculous effect to render (defaults to "Highlight", "none" = no effect)
 * @binding newEffect the name of the scriptaculous effect to render (defaults to "Highlight", "none" = no effect) for new objects
 * @binding updateEffect the name of the scriptaculous effect to render (defaults to "Highlight", "none" = no effect) for updated objects
 * @binding class the CSS class of the generated container
 * @binding style the CSS style of the generated container
 * @binding onMouseOver string with javascript to execute 
 * @binding onMouseOut string with javascript to execute
 * @binding duration passed through to Effect.Xxx in the options map
 * @binding hidden if true, when the value is highlighted, the element will be display: none
 * @binding newHidden if true, when the value is highlighted, the element will be display: none for new objects
 * @binding updateHidden if true, when the value is highlighted, the element will be display: none for updated objects
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
	private WOAssociation _duration;
	private WOAssociation _newEffect;
	private WOAssociation _newDuration;
	private WOAssociation _updateEffect;
	private WOAssociation _updateDuration;
	private WOAssociation _hidden;
	private WOAssociation _newHidden;
	private WOAssociation _updateHidden;

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
		_duration = (WOAssociation) associations.valueForKey("duration");
		_newEffect = (WOAssociation) associations.valueForKey("newEffect");
		_newDuration = (WOAssociation) associations.valueForKey("newDuration");
		_updateEffect = (WOAssociation) associations.valueForKey("updateEffect");
		_updateDuration = (WOAssociation) associations.valueForKey("updateDuration");
		_hidden = (WOAssociation) associations.valueForKey("hidden");
		_newHidden = (WOAssociation) associations.valueForKey("newHidden");
		_updateHidden = (WOAssociation) associations.valueForKey("updateHidden");
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
		AjaxUtils.addScriptResourceInHead(context, response, "effects.js");
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

		HighlightMetadata metadata = null;
		if (_value != null) {
			Object value = _value.valueInComponent(component);
			if (value != null) {
				metadata = highlightMetadataForObject(value);
			}
		}

		if (generateContainer) {
			response.appendContentString("<");
			response.appendContentString(elementName);
			response._appendTagAttributeAndValue("id", id, true);
			AjaxUtils.appendTagAttributeAndValue(response, context, component, _associations, "class");
			String displayStyle = null;
			if (metadata != null) {
				boolean hidden = false;
				if (metadata.isNew() && _newHidden != null) {
					hidden = _newHidden.booleanValueInComponent(component);
				}
				else if (!metadata.isNew() && _updateHidden != null) {
					hidden = _updateHidden.booleanValueInComponent(component);
				}
				else if (_hidden != null) {
					hidden = _hidden.booleanValueInComponent(component);
				}
				if (hidden) {
					displayStyle = "display: none;";
				}
			}
			AjaxUtils.appendTagAttributeAndValue(response, context, component, _associations, "style", displayStyle);
			AjaxUtils.appendTagAttributeAndValue(response, context, component, _associations, "onMouseOver");
            AjaxUtils.appendTagAttributeAndValue(response, context, component, _associations, "onMouseOut");
			response.appendContentString(">");
		}

		super.appendToResponse(response, context);

		if (generateContainer) {
			response.appendContentString("</");
			response.appendContentString(elementName);
			response.appendContentString(">");
		}

		if (metadata != null) {
			String effect;
			if (metadata.isNew() && _newEffect != null) {
				effect = (String) _newEffect.valueInComponent(component);
			}
			else if (!metadata.isNew() && _updateEffect != null) {
				effect = (String) _updateEffect.valueInComponent(component);
			}
			else if (_effect != null) {
				effect = (String) _effect.valueInComponent(component);
			}
			else {
				effect = "Highlight";
			}
			if (!"none".equalsIgnoreCase(effect)) {
				AjaxUtils.appendScriptHeader(response);
				response.appendContentString("new ");
				response.appendContentString(AjaxUpdateLink.fullEffectName(effect));
				response.appendContentString("('");
				response.appendContentString(id);
				response.appendContentString("',");
	
				response.appendContentString("{ queue: 'end'");
				
				if (metadata.isNew() && _newDuration != null) {
					response.appendContentString(", duration: ");
					response.appendContentString((String)_newDuration.valueInComponent(component));
				}
				else if (!metadata.isNew() && _updateDuration != null) {
					response.appendContentString(", duration: ");
					response.appendContentString((String)_updateDuration.valueInComponent(component));
				}
				else if (_duration != null) {
					response.appendContentString(", duration: ");
					response.appendContentString((String)_duration.valueInComponent(component));
					
				}
	
				response.appendContentString("});");
	
				AjaxUtils.appendScriptFooter(response);
			}
		}
	}

	protected static Object highlightedValue(Object obj) {
		Object highlightedValue = ERXEOControlUtilities.convertEOtoGID(obj);
		return highlightedValue;
	}

	public static HighlightMetadata highlightMetadataForObject(Object obj) {
		HighlightMetadata metadata = null;
		if (obj != null) {
			NSMutableDictionary highlightedObjects = (NSMutableDictionary) ERXWOContext.contextDictionary().valueForKey(AjaxHighlight.HIGHLIGHTED_KEY);
			if (highlightedObjects != null) {
				metadata = (HighlightMetadata) highlightedObjects.objectForKey(highlightedValue(obj));
			}
		}
		return metadata;
	}

	public static final void highlight(Object obj) {
		AjaxHighlight.highlightUpdate(obj);
	}

	public static final void highlightNew(Object obj) {
		AjaxHighlight.highlight(obj, true);
	}

	public static final void highlightUpdate(Object obj) {
		AjaxHighlight.highlight(obj, false);
	}

	public static final void highlight(Object obj, boolean isNew) {
		if (obj != null) {
			NSMutableDictionary highlightedObjects = (NSMutableDictionary) ERXWOContext.contextDictionary().valueForKey(AjaxHighlight.HIGHLIGHTED_KEY);
			if (highlightedObjects == null) {
				highlightedObjects = new NSMutableDictionary();
				ERXWOContext.contextDictionary().takeValueForKey(highlightedObjects, AjaxHighlight.HIGHLIGHTED_KEY);
			}
			highlightedObjects.setObjectForKey(new HighlightMetadata(isNew), highlightedValue(obj));
		}
	}

	protected static class HighlightMetadata {
		private boolean _new;

		public HighlightMetadata(boolean isNew) {
			_new = isNew;
		}

		public boolean isNew() {
			return _new;
		}
	}
}
