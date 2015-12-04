package er.extensions.components._private;

import org.apache.commons.lang3.CharEncoding;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOInput;
import com.webobjects.foundation.NSDictionary;

import er.extensions.foundation.ERXKeyValueCodingUtilities;
import er.extensions.foundation.ERXStringUtilities;

/**
 * ERXWOPasswordField is just like WOPasswordField except that it
 * doesn't show the actualy password as the value in the input.  Instead,
 * it replaces it with a "hidden value" (defaults to '_@secret@_').  When
 * the form posts back, the value binding is only set if the posted value
 * is different than the hidden value.  If you view the source of your
 * html page, you will only see the hidden value (which appears as *'s 
 * to the end-user anyway) rather than the actual value.  This is not a 
 * substitute by any means for SSL. It just adds one small additional 
 * amount of security to your HTML.
 * 
 * @binding value the actual password value (required)
 * @binding hiddenValue the string to display when hidden (optional)
 * @binding disabled whether or not the input field is disabled (optional)
 * @binding name the name of the input field (optional) 
 * @binding readonly whether or not the input field is readonly (optional)
 * 
 * @author mschrag
 */
public class ERXWOPasswordField extends WOInput {
  private static final String HIDDEN_STRING = "_@secret@_";

  private WOAssociation _hiddenValue;
  private WOAssociation _hashValue;
  private WOAssociation _readonly;

  public ERXWOPasswordField(String name, NSDictionary associations, WOElement template) {
    super("input", associations, null);
    if (_value == null || !_value.isValueSettable()) {
      throw new WODynamicElementCreationException("<ERXWOPasswordField> 'value' attribute not present or is a constant.");
    }
    _hiddenValue = _associations.removeObjectForKey("hiddenValue");
    _hashValue = _associations.removeObjectForKey("hashValue");
	_readonly = _associations.removeObjectForKey("readonly");
  }

  @Override
  protected String type() {
    return "password";
  }
  
  @Override
  protected boolean isDisabledInContext(WOContext context) {
  	WOAssociation disabled = (WOAssociation) ERXKeyValueCodingUtilities.privateValueForKey(this, "_disabled");
  	return disabled != null && disabled.booleanValueInComponent(context.component());
  }
  
  protected boolean isReadonlyInContext(WOContext context) {
  	return _readonly != null && _readonly.booleanValueInComponent(context.component());
  }

  @Override
  public void takeValuesFromRequest(WORequest request, WOContext context) {
    WOComponent component = context.component();
    if (!isDisabledInContext(context) && context.wasFormSubmitted() && !isReadonlyInContext(context)) {
      String name = nameInContext(context, component);
      if (name != null) {
        String value = request.stringFormValueForKey(name);
        if (value != null) {
          String hiddenValue = hiddenValueInContext(context, component);
          if (!value.equals(hiddenValue)) {
        	  boolean hashValue = (_hashValue != null && _hashValue.booleanValueInComponent(component));
        	  if (hashValue) {
    			  value = ERXStringUtilities.md5Hex(value, CharEncoding.UTF_8);
        	  }
        	  _value.setValue(value, component);
          }
        }
      }
    }
  }

  protected String hiddenValueInContext(WOContext context, WOComponent component) {
    String hiddenValue = null;
    Object actualValue = _value.valueInComponent(component);
    if (actualValue != null) {
      if (_hiddenValue != null) {
        hiddenValue = (String) _hiddenValue.valueInComponent(component);
      }
      if (hiddenValue == null) {
        hiddenValue = ERXWOPasswordField.HIDDEN_STRING;
      }
    }
    return hiddenValue;
  }

  @Override
  protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
    WOComponent component = context.component();
    String hiddenValue = hiddenValueInContext(context, component);
    if (hiddenValue != null) {
      response._appendTagAttributeAndValue("value", hiddenValue, true);
    }
    if (isReadonlyInContext(context)) {
      response._appendTagAttributeAndValue("readonly", "readonly", false);
    }
  }

  @Override
  protected void _appendCloseTagToResponse(WOResponse response, WOContext context) {
  }
}