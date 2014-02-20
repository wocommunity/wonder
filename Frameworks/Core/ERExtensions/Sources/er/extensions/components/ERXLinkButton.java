package er.extensions.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOPageNotFoundException;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOCGIFormValues;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.appserver._private.WONoContentElement;
import com.webobjects.appserver._private.WOStaticURLUtilities;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation._NSDictionaryUtilities;

import er.extensions.appserver.ERXSession;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXValueUtilities;

/**
 * This dynamic element produces markup like 
 * &lt;a&gt;&lt;button&gt;...&lt;/button&gt;&lt;/a&gt;.
 * This allows for consistent markup for all links and buttons that make 
 * use of this element. Outside of forms, it uses the combo like a link, 
 * populating the href attribute of the anchor element. Inside of forms, 
 * it defaults to using the combo as a button instead. Since IE is unable
 * to correctly use the &lt;button&gt; element, the default behavior is
 * to generate IE conditionals around the element and use simple &lt;a&gt;
 * tags for links and &lt;input&gt; for buttons.  Any unnamed bindings are
 * passed through to the anchor element. (eg. title, onclick, etc.) 
 * 
 * 
 * @binding action Action method to invoke when this element is activated.
 * 
 * @binding actionClass The name of the class in which the method
 * designated in 'directActionName' can be found. Defaults to DirectAction.
 * 
 * @binding class A class attribute to be bound to the anchor tag.
 * 
 * @binding directActionName The name of the direct action method 
 * (minus the "Action" suffix) to invoke when this element is activated. 
 * Defaults to “default”.
 * 
 * @binding disabled If evaluates to true, the button is in active.
 * 
 * @binding escapeHTML If escapeHTML evaluates to true, the string rendered 
 * by the 'string' or 'value' binding is converted so that characters which 
 * would be interpreted as HTML control characters become their escaped 
 * equivalent (this is the default).
 * 
 * @binding fragmentIdentifier Named location to display in the destination page. 
 * 
 * @binding href URL to direct the browser to when the button is clicked.
 * 
 * @binding id An id attribute to be bound to the anchor tag.
 * 
 * @binding name A name attribute to be bound to the button tag. This is ignored
 * if 'actionClass' or 'directActionName' is bound. It will not appear if the 
 * button is disabled.
 * 
 * @binding pageName Name of WebObjects page to display when the link is clicked.
 * 
 * @binding queryDictionary Takes a dictionary that should be appended to the 
 * hyperlink’s URL after a question mark character. The dictionary must be 
 * correctly encoded and will be merged with any existing query dictionary 
 * for a particular session ID.
 * 
 * @binding rel The rel attribute of the anchor tag. This binding has no effect
 * when this element is used as a button. If the property
 * er.extensions.ERXHyperlink.defaultNoFollow=true is used and 'action' is bound,
 * then "nofollow" will automatically be appended.
 * 
 * @binding secure Changes the URL prefix from http to https when WebObjects 
 * generates URLs for component actions and direct actions for this element. 
 * For this attribute to have any effect, you must provide bindings either for 
 * the 'action,' 'directAction,' 'actionClass,' or 'pageName' attribute (respecting 
 * the valid combinations). This binding has no effect when this element is
 * used as a button.
 * 
 * @binding string This binding provides the string value of the link when
 * submit is false. If no string is provided, it falls back to the value
 * binding. If no value is provided, no string is used. If the element has
 * child elements, the string is ignored except when using IE conditionals.
 * 
 * @binding submit This binding is ignored and the default is false if the 
 * this element is used outside of a form, 'pageName' is bound, or 'href' is bound. 
 * Otherwise, inside of a form, the default is true. Using buttons as links will
 * not submit form values, but can be more efficient.
 * 
 * @binding useIEConditionals When true, the element generates simple input or
 * hyperlink elements using IE conditional comments, since IE is unable to
 * handle button elements correctly. The default is true.
 * 
 * @binding value This binding provides the title of the button when submit
 * is true. If no value is provided, the value falls back to the string binding.
 * If no string binding is provided, a default value of "Submit" is used. If the
 * element has child elements, the value is ignored except when using IE conditionals.
 * 
 * @author Ramsey Gurley
 *
 */
public class ERXLinkButton extends WOHTMLDynamicElement {
		
	/**
	 * Defines if the hyperlink adds a default <code>rel="nofollow"</code> if an action is bound.
	 */
	private static boolean defaultNoFollow = ERXProperties.booleanForKey("er.extensions.ERXHyperlink.defaultNoFollow");

	/**
	 * Defines if the anchor adds a default <code>class="button"</code>
	 */
	private static boolean defaultButtonClass = ERXProperties.booleanForKeyWithDefault("er.r2d2w.components.misc.R2DLinkButton.defaultClass", true);

	protected NSDictionary<String, WOAssociation> _otherQueryAssociations;
	
	protected WOAssociation _action;
	protected WOAssociation _actionClass;
	protected WOAssociation _directActionName;
	protected WOAssociation _disabled;
	protected WOAssociation _escapeHTML;
	protected WOAssociation _fragmentIdentifier;
	protected WOAssociation _href;
	protected WOAssociation _name;
	protected WOAssociation _pageName;
	protected WOAssociation _queryDictionary;
	protected WOAssociation _rel;
	protected WOAssociation _string;
	protected WOAssociation _submit;
	protected WOAssociation _useIEConditionals;
	protected WOAssociation _value;
	
	@SuppressWarnings("unchecked")
	public ERXLinkButton(String aName, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super("a", associations, template);
		
		_otherQueryAssociations = _NSDictionaryUtilities.extractObjectsForKeysWithPrefix(_associations, "?", true);
		_otherQueryAssociations = _otherQueryAssociations == null || _otherQueryAssociations.count() <= 0 ? null : _otherQueryAssociations;
		
		_action = _associations.removeObjectForKey("action");
		_actionClass = _associations.removeObjectForKey("actionClass");
		_directActionName = _associations.removeObjectForKey("directActionName");
		_disabled = _associations.removeObjectForKey("disabled");
		_escapeHTML = _associations.removeObjectForKey("escapeHTML");
		_fragmentIdentifier = _associations.removeObjectForKey("fragmentIdentifier");
		_href = _associations.removeObjectForKey("href");
		_name = _associations.removeObjectForKey("name");
		_pageName = _associations.removeObjectForKey("pageName");
		_queryDictionary = _associations.removeObjectForKey("queryDictionary");
		_rel = _associations.removeObjectForKey("rel");
		_string = _associations.removeObjectForKey("string");
		_submit = _associations.removeObjectForKey("submit");
		_useIEConditionals = _associations.removeObjectForKey("useIEConditionals");
		_value = _associations.removeObjectForKey("value");
		
		if(_action == null && _href == null && _pageName == null && _directActionName == null && _actionClass == null) {
			throw new WODynamicElementCreationException(new StringBuilder().append('<').append(getClass().getName()).append("> Missing required attribute: 'action' or 'href' or 'pageName' or 'directActionName' or 'actionClass'").toString());
		}
		if(_action != null && _href != null || _action != null && _pageName != null || _href != null && _pageName != null || _action != null && _directActionName != null || _href != null && _directActionName != null || _pageName != null && _directActionName != null || _action != null && _actionClass != null) {
			throw new WODynamicElementCreationException(new StringBuilder().append('<').append(getClass().getName()).append("> At least two of these conflicting attributes are present: 'action', 'href', 'pageName', 'directActionName', 'actionClass'.").toString());
		}
		if(_action != null && _action.isValueConstant()) {
			throw new WODynamicElementCreationException(new StringBuilder().append('<').append(getClass().getName()).append("> 'action' is a constant.").toString());
		}	
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		//Do nothing
	}
	
	/**
	 * Overridden to perform the logging, propagating the action to sub-elements and returning the
	 * current page if an empty page is returned from super.
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		boolean submit = submitInContext(context);
		WOActionResults result = submit?invokeButtonAction(request, context):invokeLinkAction(request, context);
		if(!submit) {
			if(result != null && (result instanceof WONoContentElement)) {
				result = context.page();
			}
			if(result == null) {
				String sender = context.senderID();
				String element = context.elementID();
				if(sender.startsWith(element) && !element.equals(sender)) {
					result = invokeChildrenAction(request, context);
				}
			}
			if (result != null && ERXSession.anySession() != null) {
				ERXSession.anySession().setObjectForKey(toString(), "ERXActionLogging");
			}
		}
		return result;
	}
	
	public WOActionResults invokeButtonAction(WORequest request, WOContext context) {
		WOActionResults anActionResult = null;
		WOComponent aComponent = context.component();
		if(!isDisabledInContext(context) && context.wasFormSubmitted()) {
			if(context.isMultipleSubmitForm()) {
				if(request.formValueForKey(nameInContext(context)) != null) {
					context.setActionInvoked(true);
					if(_action != null) {
						anActionResult = (WOActionResults)_action.valueInComponent(aComponent);
					}
					if(anActionResult == null) {
						anActionResult = context.page();
					}
				}
			} else {
				context.setActionInvoked(true);
				if(_action != null) {
					anActionResult = (WOActionResults)_action.valueInComponent(aComponent);
				}
				if(anActionResult == null) {
					anActionResult = context.page();
				}
			}
		}
		return anActionResult;
	}
	
	public WOActionResults invokeLinkAction(WORequest request, WOContext context) {
		String nextPageName = null;
		WOActionResults invokedElement = null;
		WOComponent component = context.component();
		if(context.elementID().equals(context.senderID())) {
			if(_disabled == null || !_disabled.booleanValueInComponent(component)) {
				if(_pageName != null) {
					Object nextPageValue = _pageName.valueInComponent(component);
					if(nextPageValue != null) {
						nextPageName = nextPageValue.toString();
					}
				}
				if(_action != null) {
					invokedElement = (WOActionResults)_action.valueInComponent(component);
				} else {
					if(_pageName == null) {
						throw new IllegalStateException(new StringBuilder().append('<').append(getClass().getName()).append("> : Missing page name.").toString());
					}
					if(nextPageName != null) {
						invokedElement = WOApplication.application().pageWithName(nextPageName, context);
					} else {
						throw new WOPageNotFoundException(new StringBuilder().append('<').append(getClass().getName()).append("> : cannot find page.").toString());
					}
				}
			} else {
				invokedElement = new WONoContentElement();
			}
			if(invokedElement == null) {
				invokedElement = context.page();
			}
		}
		return invokedElement;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		if(submitInContext(context)) {
			if(useIEConditionalsInContext(context)) {
				_appendIEOpenTagToResponse(response, context);
				response._appendContentAsciiString("<input type=\"submit\"");
				if(!isDisabledInContext(context)) {
					_appendNameAttributeToResponse(response, context);
					_appendValueAttributeToResponse(response, context);
				} else {
					_appendDisabledAttributeToResponse(response, context);
				}
				response._appendContentAsciiString("/>");
				_appendIECloseTagToResponse(response, context);
			}
			if((_directActionName != null || _actionClass != null) && !isDisabledInContext(context)) {
				response._appendContentAsciiString("<input type=\"hidden\" name=\"WOSubmitAction\"");
				response._appendTagAttributeAndValue("value", _actionClassAndName(context), false);
				response.appendContentString(" />");
			}
		}
	}

	@Override
	public void appendChildrenToResponse(WOResponse response, WOContext context) {
		_appendButtonOpenTagToResponse(response, context);
		super.appendChildrenToResponse(response, context);
		if(!hasChildrenElements()){
			_appendChildStringToResponse(response, context);
		}
		_appendButtonCloseTagToResponse(response, context);
	}

	@Override
	public void appendAttributesToResponse(WOResponse response, WOContext context) {
		super.appendAttributesToResponse(response, context);
		if(!submitInContext(context) && !isDisabledInContext(context)) {
			_appendOpeningHrefToResponse(response, context);
			if(_actionClass != null || _directActionName != null) {
				_appendCGIActionURLToResponse(response, context, true);
			} else if(_action != null || _pageName != null) {
				_appendComponentActionURLToResponse(response, context, true);
			} else if(_href != null) {
				_appendStaticURLToResponse(response, context, true);
			} else if(_fragmentIdentifier != null && fragmentIdentifierInContext(context).length() > 0) {
				_appendQueryStringToResponse(response, context, "", true, true);
				_appendFragmentToResponse(response, context);
			}
			_appendClosingHrefToResponse(response, context);
			_appendRelToResponse(response, context);
		}
	}

	@Override
	protected void _appendOpenTagToResponse(WOResponse response, WOContext context) {
		if(useIEConditionalsInContext(context) && submitInContext(context)) {
			_appendNotIEOpenTagToResponse(response, context);
		}
		super._appendOpenTagToResponse(response, context);
		if(useIEConditionalsInContext(context) && !submitInContext(context)) {
			_appendIEStringToResponse(response, context);
			_appendNotIEOpenTagToResponse(response, context);
		}
	}

	@Override
	protected void _appendCloseTagToResponse(WOResponse response, WOContext context) {
		if(useIEConditionalsInContext(context) && !submitInContext(context)) {
			_appendNotIECloseTagToResponse(response, context);
		}
		super._appendCloseTagToResponse(response, context);
		if(useIEConditionalsInContext(context) && submitInContext(context)) {
			_appendNotIECloseTagToResponse(response, context);
		}
	}
	
	protected void _appendQueryStringToResponse(WOResponse response, WOContext context, String aRequestHandlerPath, boolean htmlEscapeURL, boolean defaultIncludeSessionID) {
		NSDictionary<String, Object> aQueryDict = computeQueryDictionaryInContext(aRequestHandlerPath == null ? "" : aRequestHandlerPath, _queryDictionary, _otherQueryAssociations, defaultIncludeSessionID, context);
		if(aQueryDict.count() > 0) {
			String aQueryString = WOCGIFormValues.getInstance().encodeAsCGIFormValues(aQueryDict, htmlEscapeURL);
			if(aQueryString.length() > 0) {
				int questionMarkIndex = aRequestHandlerPath == null ? -1 : aRequestHandlerPath.indexOf("?");
				if(questionMarkIndex > 0) {
					response.appendContentString(htmlEscapeURL ? "&amp;" : "&");
				} else {
					response.appendContentCharacter('?');
				}
				response.appendContentString(aQueryString);
			}
		}
	}
	
	protected void _appendFragmentToResponse(WOResponse response, WOContext context) {
		String fragmentIdentifier = fragmentIdentifierInContext(context);
		if(fragmentIdentifier.length() > 0) {
			response.appendContentCharacter('#');
			response.appendContentString(fragmentIdentifier);
		}
	}
	
	protected void _appendCGIActionURLToResponse(WOResponse response, WOContext context, boolean htmlEscapeURL) {
		String actionPath = computeActionStringInContext(_actionClass, _directActionName, context);
		NSDictionary<String, Object> aQueryDict = computeQueryDictionaryInContext(actionPath, _queryDictionary, _otherQueryAssociations, true, context);
		response.appendContentString(context._directActionURL(actionPath, aQueryDict, secureInContext(context), 0, htmlEscapeURL));
		_appendFragmentToResponse(response, context);
	}
	
	protected void _appendComponentActionURLToResponse(WOResponse response, WOContext context, boolean escapeHTML) {
		String actionURL = context.componentActionURL(WOApplication.application().componentRequestHandlerKey(), secureInContext(context));
		response.appendContentString(actionURL);
		_appendQueryStringToResponse(response, context, actionURL, escapeHTML, true);
		_appendFragmentToResponse(response, context);
	}
	
	protected void _appendStaticURLToResponse(WOResponse response, WOContext context, boolean escapeHTML) {
		String staticURL = hrefInContext(context);
		if(WOStaticURLUtilities.isRelativeURL(staticURL) && !WOStaticURLUtilities.isFragmentURL(staticURL)) {
			String resourceURL = context._urlForResourceNamed(staticURL, null, false);
			if(resourceURL != null) {
				response.appendContentString(resourceURL);
				staticURL = resourceURL;
			} else {
				response.appendContentString(context.component().baseURL());
				response.appendContentCharacter('/');
				response.appendContentString(staticURL);
			}
		} else {
			response.appendContentString(staticURL);
		}
		_appendQueryStringToResponse(response, context, staticURL, escapeHTML, false);
		_appendFragmentToResponse(response, context);
	}
	
	protected boolean submitInContext(WOContext context) {
		boolean isInForm = context.isInForm();
		if(_pageName != null || _href != null || !isInForm) {
			return false;
		}
		return (_submit == null)?
				isInForm:
				ERXValueUtilities.booleanValueWithDefault(_submit.valueInComponent(context.component()), isInForm);
	}
	
	protected boolean useIEConditionalsInContext(WOContext context) {
		return (_useIEConditionals == null)?
				true:
				ERXValueUtilities.booleanValueWithDefault(_useIEConditionals.valueInComponent(context.component()), true);
	}
	
	private String _actionClassAndName(WOContext context) {
		String anActionString = computeActionStringInContext(_actionClass, _directActionName, context);
		return anActionString;
	}
	
	protected void _appendOpeningHrefToResponse(WOResponse response, WOContext context) {
		response.appendContentCharacter(' ');
		response.appendContentString("href");
		response.appendContentCharacter('=');
		response.appendContentCharacter('"');
		String prefix = prefixInContext(context);
		if(prefix.length() > 0) {
			response.appendContentString(prefix);
		}
	}
	
	protected void _appendClosingHrefToResponse(WOResponse response, WOContext context) {
		String suffix = suffixInContext(context);
		if(suffix.length() > 0) {
			response.appendContentString(suffix);
		}
		response.appendContentCharacter('"');
	}
	
	protected void _appendButtonOpenTagToResponse(WOResponse response, WOContext context) {
		response.appendContentString("<button");
		_appendTypeAttributeToResponse(response, context);
		if(submitInContext(context)) {
			_appendNameAttributeToResponse(response, context);
			_appendValueAttributeToResponse(response, context);
		}
		if(isDisabledInContext(context)) {
			_appendDisabledAttributeToResponse(response, context);
		}
		response.appendContentString(">");
	}
	
	protected void _appendButtonCloseTagToResponse(WOResponse response, WOContext context) {
		response.appendContentString("</button>");
	}
	
	protected void _appendIEOpenTagToResponse(WOResponse response, WOContext context) {
		response.appendContentString("<!--[if IE]>");
	}
	
	protected void _appendIECloseTagToResponse(WOResponse response, WOContext context) {
		response.appendContentString("<![endif]-->");
	}
	
	protected void _appendNotIEOpenTagToResponse(WOResponse response, WOContext context) {
		response.appendContentString("<!--[if !(IE)]> <-->");
	}
	
	protected void _appendNotIECloseTagToResponse(WOResponse response, WOContext context) {
		response.appendContentString("<!--> <![endif]-->");
	}
	
	protected void _appendTypeAttributeToResponse(WOResponse response, WOContext context) {
		String type = submitInContext(context) ? "submit":"button";
		_appendTagAttributeAndValueToResponse(response, "type", type, false);
	}
	
	protected void _appendNameAttributeToResponse(WOResponse response, WOContext context) {
		_appendTagAttributeAndValueToResponse(response, "name", nameInContext(context), escapeHTMLInContext(context));
	}
	
	protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
		_appendTagAttributeAndValueToResponse(response, "value", valueInContext(context), escapeHTMLInContext(context));
	}
	
	protected void _appendDisabledAttributeToResponse(WOResponse response, WOContext context) {
		_appendTagAttributeAndValueToResponse(response, "disabled", "disabled", false);
	}
	
	protected void _appendRelToResponse(WOResponse response, WOContext context) {
		if(_rel != null) {
			WOComponent component = context.component();
			Object val = _rel.valueInComponent(component);
			String value = (val == null)?null:val.toString();
			if(defaultNoFollow && _action != null) {
				if(value == null || value.length() == 0) {
					value = "nofollow";
				} else {
					value = "nofollow " + value;
				}
			}
			if(value != null) {
				_appendTagAttributeAndValueToResponse(response, "rel", value, false);
			}
		}
	}

	@Override
    public String classInContext(WOContext context) {
        String value = super.classInContext(context);
        if(defaultButtonClass) {
			if(value == null || value.length() == 0) {
				value = "button";
			} else {
				value = "button " + value;
			}
		}
        return value;
    }
	
	protected void _appendIEStringToResponse(WOResponse response, WOContext context) {
		Object value = childStringInContext(context);
		_appendIEOpenTagToResponse(response, context);
		if(escapeHTMLInContext(context)) {
			response.appendContentHTMLString(value.toString());
		} else {
			response.appendContentString(value.toString());
		}
		_appendIECloseTagToResponse(response, context);
	}
	
	protected void _appendChildStringToResponse(WOResponse response, WOContext context) {
		String childString = childStringInContext(context);
		if(escapeHTMLInContext(context)) {
			response.appendContentHTMLString(childString);
		} else {
			response.appendContentString(childString);
		}
	}
	
	public String nameInContext(WOContext context) {
		if(_actionClass != null || _directActionName != null) {
			return _actionClassAndName(context);
		}
		if(_name != null) {
			Object value = _name.valueInComponent(context.component());
			if(value != null) {
				return value.toString();
			}
		}
		Object elementID = context.elementID();
		if(elementID != null) {
			return elementID.toString();
		}
		throw new IllegalStateException(new StringBuilder().append('<').append(getClass().getName()).append("> Cannot evaluate 'name' attribute, and context element ID is null.").toString());
	}
	
	protected String hrefInContext(WOContext context) {
		Object value = _href == null ? null : _href.valueInComponent(context.component());
		return value == null ? "" : value.toString();
	}
	
	protected String fragmentIdentifierInContext(WOContext context) {
		Object value = _fragmentIdentifier == null ? null : _fragmentIdentifier.valueInComponent(context.component());
		return value == null ? "" : value.toString();
	}
	
	protected boolean isDisabledInContext(WOContext context) {
		boolean isDisabled = _disabled != null && _disabled.booleanValueInComponent(context.component()) || !isRenderedInContext(context);
		return isDisabled;
	}
	
	protected String valueInContext(WOContext context) {
		Object value = _value == null?null:_value.valueInComponent(context.component());
		return value == null?null:value.toString();
	}
	
	/**
	 * Returns the child string for the button supplied by the string or value attribute.
	 * If submit is true, then the value binding is preferred. Otherwise, the string 
	 * binding is preferred.
	 * @param context the current context
	 * @return the child string value
	 */
	protected String childStringInContext(WOContext context) {
		Object value = null;
		WOComponent component = context.component();
		Object stringVal = _string == null?null:_string.valueInComponent(component);
		Object valueVal = valueInContext(context);
		if(submitInContext(context)) {
			value = valueVal == null?stringVal == null?"Submit":stringVal:valueVal;
		} else {
			value = stringVal == null?valueVal == null?"":valueVal:stringVal;
		}
		return value.toString();
	}

	@Override
	public String toString() {
		return new StringBuilder().append('<').append(getClass().getName()).append(" action: ").append(_action).append(" actionClass: ").append(_actionClass).append(" href: ").append(_href).append(" value: ").append(_value).append(" queryDictionary: ").append(_queryDictionary).append(" otherQueryAssociations: ").append(_otherQueryAssociations).append(" pageName: ").append(_pageName).append(" fragmentIdentifier: ").append(_fragmentIdentifier).append(" disabled: ").append(_disabled).append(" secure: ").append(_secure).append('>').toString();
	}
	
	protected boolean defaultEscapeHTML() {
		return true;
	}
	
	protected boolean escapeHTMLInContext(WOContext context) {
		return _escapeHTML == null ?
				defaultEscapeHTML():
				ERXValueUtilities.booleanValueWithDefault(_escapeHTML.valueInComponent(context.component()),defaultEscapeHTML());
	}
	
	/**
	 * Overriding to prevent exceptions when actionClass or directActionName 
	 * are bound, but resolve to null.
	 */
	@Override
	protected String computeActionStringInContext(WOAssociation actionClass, WOAssociation directActionName, WOContext aContext) {

		WOComponent aComponent = aContext.component();
		Object anActionClassName = null;
		Object anActionName = null;
		Object anActionString = null;

		if (actionClass != null) {
			anActionClassName = actionClass.valueInComponent(aComponent);
			if (!(anActionClassName == null || anActionClassName instanceof String)) {
				throw new IllegalArgumentException(new StringBuilder().append('<').append(getClass().getName()).append("> Value for attribute named \"actionClass\" must be a string.  Received ").append(anActionClassName).toString());
			}
		}

		if (directActionName != null) {
			anActionName = directActionName.valueInComponent(aComponent);
			if (!(anActionName == null || anActionName instanceof String)) {
				throw new IllegalArgumentException(new StringBuilder().append('<').append(getClass().getName()).append("> Value for attribute named \"directActionName\" must be a string.  Received ").append(anActionName).toString());
			}
		}

		if (anActionClassName != null && anActionName != null) {
			if (anActionClassName.equals("DirectAction")) {
				anActionString = anActionName;
			} else {
				anActionString = new StringBuilder().append(anActionClassName).append('/').append(anActionName).toString();
			}
		} else if (anActionClassName != null) {
			anActionString = anActionClassName;
		} else if (anActionName != null) {
			anActionString = anActionName;
		} else {
			throw new IllegalStateException(new StringBuilder().append('<').append(getClass().getName()).append("> Both 'actionClass' and 'directActionName' are either absent or evaluated to null. Cannot generate dynamic url without an actionClass or directActionName.").toString());
		}

		return (String) anActionString;
	}

}