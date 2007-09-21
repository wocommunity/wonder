package er.extensions;

import java.math.BigDecimal;
import java.text.Format;
import java.text.ParseException;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOInput;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSValidation;

/**
 * Replacement for WOTextField. Provides for localized formatters. 
 * Never use this directly, rather use WOTextField and let the ERXPatcher handle the
 * replacement of WOTextField in all cases.
 * 
 * @binding blankIsNull if false, "" will not be converted to null; if true, "" will be converted to null. Default is true.
 * 
 * @author ak
 */
public class ERXWOTextField extends WOInput /*ERXPatcher.DynamicElementsPatches.TextField*/ {
	
    public static Logger log = Logger.getLogger(ERXWOTextField.class);
    
	protected WOAssociation _formatter;
	protected WOAssociation _dateFormat;
	protected WOAssociation _numberFormat;
	protected WOAssociation _useDecimalNumber;
	protected WOAssociation _blankIsNull;

	public ERXWOTextField(String tagname, NSDictionary nsdictionary, WOElement woelement) {
		super("input", nsdictionary, woelement);
		if(_value == null || !_value.isValueSettable())
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'value' attribute not present or is a constant");
		
		_formatter = (WOAssociation)_associations.removeObjectForKey("formatter");
		_dateFormat = (WOAssociation)_associations.removeObjectForKey("dateformat");
		_numberFormat = (WOAssociation)_associations.removeObjectForKey("numberformat");
		_useDecimalNumber = (WOAssociation)_associations.removeObjectForKey("useDecimalNumber");
		_blankIsNull = (WOAssociation)_associations.removeObjectForKey("blankIsNull");
		
		if(_dateFormat != null && _numberFormat != null) {
			throw new WODynamicElementCreationException("<" + getClass().getName() + "> Cannot have 'dateFormat' and 'numberFormat' attributes at the same time.");
		}
	}
	
	public String type() {
		return "text";
	}
	   
    protected boolean isDisabledInContext(WOContext context) {
    	WOAssociation disabled = (WOAssociation) ERXKeyValueCodingUtilities.privateValueForKey(this, "_disabled");
    	return disabled != null && disabled.booleanValueInComponent(context.component());
    }

	public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
		WOComponent component = wocontext.component();
		if(!isDisabledInContext(wocontext) && wocontext._wasFormSubmitted()) {
			String name = nameInContext(wocontext, component);
			if(name != null) {
				String stringValue;
				boolean blankIsNull = _blankIsNull == null || _blankIsNull.booleanValueInComponent(component);
				if (blankIsNull) {
					stringValue = worequest.stringFormValueForKey(name);
				}
				else {
					Object objValue = worequest.formValueForKey(name);
					stringValue = (objValue == null) ? null : objValue.toString();
				}
				Object result = stringValue;
				if(stringValue != null) {
					Format format = null;
					if(stringValue.length() != 0) {
						if(_formatter != null) {
							format = (Format)_formatter.valueInComponent(component);
						}
						if(format == null) {
							if(_dateFormat != null) {
								String formatString = (String)_dateFormat.valueInComponent(component);
								if(formatString != null) {
									format = ERXTimestampFormatter.dateFormatterForPattern(formatString);
								}
							} else if(_numberFormat != null) {
								String formatString = (String)_numberFormat.valueInComponent(component);
								if(formatString != null) {
									format = ERXNumberFormatter.numberFormatterForPattern(formatString);
								}
							}
						}
					}
					if(format != null) {
						try {
							Object parsedObject = format.parseObject(stringValue);
							String reformatedObject = format.format(parsedObject);
							result = format.parseObject(reformatedObject);
						} catch(ParseException parseexception) {
							String keyPath = _value.keyPath();
							NSValidation.ValidationException validationexception = new NSValidation.ValidationException(parseexception.getMessage(), stringValue, keyPath);
							component.validationFailedWithException(validationexception, stringValue, keyPath);
							return;
						}
						if(result != null && _useDecimalNumber != null && _useDecimalNumber.booleanValueInComponent(component)) {
							result = new BigDecimal(result.toString());
						}
					} else if(blankIsNull && result.toString().length() == 0) {
						result = null;
					}
				}
				_value.setValue(result, component);
			}
		}
	}

	protected void _appendValueAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
		WOComponent component = wocontext.component();
		Object valueInComponent = _value.valueInComponent(component);
		if(valueInComponent != null) {
			String stringValue = null;
			Format format = null;
			if(_formatter != null) {
				format = (Format)_formatter.valueInComponent(component);
			}
			if(format == null) {
				if(_dateFormat != null) {
					String formatString = (String)_dateFormat.valueInComponent(component);
					if(formatString != null) {
						format = ERXTimestampFormatter.dateFormatterForPattern(formatString);
					}
				} else if(_numberFormat != null) {
					String formatString = (String)_numberFormat.valueInComponent(component);
					if(formatString != null) {
						format = ERXNumberFormatter.numberFormatterForPattern(formatString);
					}
				}
			}
			if(format != null)
				try {
					String formatedValue = format.format(valueInComponent);
					Object reparsedObject = format.parseObject(formatedValue);
					stringValue = format.format(reparsedObject);
				} catch(IllegalArgumentException illegalargumentexception) {
					NSLog._conditionallyLogPrivateException(illegalargumentexception);
					stringValue = null;
				} catch(ParseException parseexception) {
					NSLog._conditionallyLogPrivateException(parseexception);
					stringValue = null;
				}
				if(stringValue == null) {
					stringValue = valueInComponent.toString();
				}
				woresponse._appendTagAttributeAndValue("value", stringValue, true);
		}
	}

	protected void _appendCloseTagToResponse(WOResponse woresponse, WOContext wocontext) {
	}

	public String toString() {
		StringBuffer stringbuffer = new StringBuffer();
		stringbuffer.append("<");
		stringbuffer.append(getClass().getName());
		stringbuffer.append(" formatter=" + _formatter);
		stringbuffer.append(" dateFormat=" + _dateFormat);
		stringbuffer.append(" numberFormat=" + _numberFormat);
		stringbuffer.append(" useDecimalNumber=" + _useDecimalNumber);
		stringbuffer.append(">");
		return stringbuffer.toString();
	}
	
	/**
	 * Overridden to make output XML compatible.
	 */
    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
        WOResponse newResponse = new WOResponse();
        super.appendToResponse(newResponse, wocontext);
        
        ERXPatcher.DynamicElementsPatches.processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
        woresponse.appendContentString(newResponse.contentString());
    }
}
