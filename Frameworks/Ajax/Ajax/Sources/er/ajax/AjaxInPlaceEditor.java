package er.ajax;

import java.text.ParseException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementBehavior;
import com.webobjects.appserver._private.WOFormatElementBehavior;
import com.webobjects.appserver._private.WOHTMLAttribute;
import com.webobjects.appserver._private.WOLocalizedFormatElementBehavior;
import com.webobjects.appserver.association.WOAssociation;
import com.webobjects.appserver.association.WOConstantValueAssociation;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSValidation;

import er.extensions.appserver.ERXWOContext;

// PROTOTYPE FUNCTIONS (WRAPPER)
public class AjaxInPlaceEditor extends AjaxDynamicElement implements WOFormatElementBehavior, WOLocalizedFormatElementBehavior {
	private WOAssociation _idAssociation;
	private WOAssociation _elementNameAssociation;
	private WOAssociation _classAssociation;
	private WOAssociation _valueAssociation;
	private WOFormatElementBehaviorImplementation<WOFormatElementBehavior> _formatElementBehavior;
	private WOLocalizedFormatElementBehaviorImplementation<WOLocalizedFormatElementBehavior> _localizedFormatElementBehavior;

	public AjaxInPlaceEditor(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(name, associations, template);
		_idAssociation = associations.objectForKey("id");
		_elementNameAssociation = associations.objectForKey("elementName");
		if (_elementNameAssociation == null) {
			_elementNameAssociation = new WOConstantValueAssociation("div");
		}
		_classAssociation = associations.objectForKey("class");
		_valueAssociation = associations.objectForKey("value");
		// WO 5.5 I have no idea if I'm calling this right ... does it actually need direct access to the read associations array, or just a mutable
		// dictionary?
		_formatElementBehavior = WODynamicElementBehavior.ImplementationFactory.getInstance().newFormatElementBehavior(name, associations(), template, this);
		_localizedFormatElementBehavior = WODynamicElementBehavior.ImplementationFactory.getInstance().newLocalizedElementBehavior(name, associations(), template, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.webobjects.appserver._private.WOFormatElement#formatElementBehavior()
	 */
	public WOFormatElementBehaviorImplementation<WOFormatElementBehavior> formatElementBehavior() {
		return _formatElementBehavior;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.webobjects.appserver._private.WOFormatElement#format(java.lang.Object
	 * )
	 */
	public String formatValueInContext(Object obj, WOContext context) {
		return this.formatElementBehavior().formatValueInContext(obj, context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.webobjects.appserver._private.WOFormatElement#parseObject(java.lang
	 * .String)
	 */
	public Object parseValueInContext(String source, WOContext context) throws ParseException {
		return this.formatElementBehavior().parseValueInContext(source, context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.webobjects.appserver._private.WOFormatElement#shouldFormat()
	 */
	public boolean shouldFormat() {
		return this.formatElementBehavior().shouldFormat();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.webobjects.appserver._private.WOFormatElement#shouldPushValue()
	 */
	public boolean shouldPushValue() {
		return this.formatElementBehavior().shouldPushValue();
	}

	/**
	 * @return WOLocalizedFormatElementBehaviorImplementation
	 */
	public WOLocalizedFormatElementBehaviorImplementation<WOLocalizedFormatElementBehavior> localizedFormatElementBehavior() {
		return _localizedFormatElementBehavior;
	}

	/**
	 * @return true if should localize
	 */
	public boolean shouldLocalizeFormat() {
		return this.localizedFormatElementBehavior().shouldFormat();
	}

	public NSDictionary createAjaxOptions(WOComponent component) {
		NSMutableArray ajaxOptionsArray = new NSMutableArray();
		ajaxOptionsArray.addObject(new AjaxOption("okButton", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("okText", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("cancelLink", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("cancelText", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("cancelControl", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("savingText", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("clickToEditText", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("formId", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("externalControl", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("rows", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("onComplete", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("onFailure", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("cols", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("size", AjaxOption.NUMBER));
		ajaxOptionsArray.addObject(new AjaxOption("highlightcolor", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("highlightendcolor", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("savingClassName", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("formClassName", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("loadTextURL", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("loadingText", AjaxOption.STRING));
		ajaxOptionsArray.addObject(new AjaxOption("callback", AjaxOption.SCRIPT));
		ajaxOptionsArray.addObject(new AjaxOption("submitOnBlur", AjaxOption.BOOLEAN));
		ajaxOptionsArray.addObject(new AjaxOption("valueWhenEmpty", AjaxOption.STRING));
		// ajaxOptionsArray.addObject(new AjaxOption("ajaxOptions",
		// AjaxOption.SCRIPT));
		NSMutableDictionary options = AjaxOption.createAjaxOptionsDictionary(ajaxOptionsArray, component, associations());
		return options;
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		String id;
		if (_idAssociation == null) {
			id = ERXWOContext.safeIdentifierName(context, false);
		}
		else {
			id = (String) _idAssociation.valueInComponent(component);
		}
		String elementName = (String) _elementNameAssociation.valueInComponent(component);
		String actionUrl = AjaxUtils.ajaxComponentActionUrl(context);
		super.appendToResponse(response, context);
		response.appendContentString("<");
		response.appendContentString(elementName);
		response.appendContentString(" id = \"");
		response.appendContentString(id);
		response.appendContentString("\"");
		if (_classAssociation != null) {
			String className = (String) _classAssociation.valueInComponent(component);
			response.appendContentString(" class = \"");
			response.appendContentString(className);
			response.appendContentString("\"");
		}
		response.appendContentString(">");

		_appendValueAttributeToResponse(response, context);

		response.appendContentString("</");
		response.appendContentString(elementName);
		response.appendContentString(">");
		AjaxUtils.appendScriptHeader(response);
		response.appendContentString("new Ajax.InPlaceEditor('");
		response.appendContentString(id);
		response.appendContentString("', '");
		response.appendContentString(actionUrl);
		response.appendContentString("',");
		NSDictionary options = createAjaxOptions(component);
		AjaxOptions.appendToResponse(options, response, context);
		response.appendContentString(");");
		AjaxUtils.appendScriptFooter(response);
	}

	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
		AjaxUtils.addScriptResourceInHead(context, response, "builder.js");
		AjaxUtils.addScriptResourceInHead(context, response, "effects.js");
		AjaxUtils.addScriptResourceInHead(context, response, "controls.js");
		AjaxUtils.addScriptResourceInHead(context, response, "wonder.js");
		AjaxUtils.addScriptResourceInHead(context, response, "wonder_inplace.js");
	}

	// Formatting/Parsing method "inspired by" WOTextField
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOComponent component = context.component();
		String strValue = request.stringFormValueForKey("value");
		Object objValue = strValue;
		if ((strValue != null) && this.shouldFormat()) {
			try {
				objValue = this.parseValueInContext(strValue, context);
			}
			catch (Exception exception) {
				if (log.isDebugEnabled()) {
					log.debug("Formatter Exception ", exception);
				}
				// Now allow the component to handle the validation failure if
				// desired
				String keyPath = _valueAssociation.keyPath();
				NSValidation.ValidationException validationException = new NSValidation.ValidationException("Formatter exception " + exception.getMessage(), strValue, keyPath, exception);
				component.validationFailedWithException(validationException, strValue, keyPath);
				return null; // don't touch the value if it failed validation
			}
		}
		else {
			objValue = strValue;
		}
		_valueAssociation.setValue(objValue, component);

		// just executing action, ignoring result
		valueForBinding("action", component);

		WOResponse response = AjaxUtils.createResponse(request, context);
		_appendValueAttributeToResponse(response, context);

		return response;
	}

	protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		String valueToAppend = null;
		Object valueValue = null;
		if (_valueAssociation != null) {
			valueValue = _valueAssociation.valueInComponent(component);
			if (this.shouldLocalizeFormat()) {
				try {
					valueValue = localizedFormatElementBehavior().formatValueInContext(valueValue, context);
				}
				catch (Exception exception) {
					log.error("Invalid localization format for value: " + valueValue, exception);
					valueValue = null;
				}

			}
			else if (valueValue == null && this.shouldFormat()) {
				try {
					valueValue = this.formatValueInContext(valueValue, context);
				}
				catch (Exception ex) {
					log.error("Invalid format for value: " + valueValue, ex);
					valueValue = null;
				}
			}
		}
		else {
			log.warn("WARNING value binding is null !");
		}

		if (valueValue != null) {
			valueToAppend = valueValue.toString();
			response._appendTagAttributeAndValue(WOHTMLAttribute.Value, valueToAppend, true);
		}

		// Workaround for inplace control staying in "Saving..." mode forever
		// when empty value was supplied
		String contentString = response.contentString();
		if (contentString == null || contentString.equals("")) {
			response.appendContentString(" ");
		}
	}
}
