package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXJSSubmitFunction generates a javascript method that can submit a particular form and trigger
 * the call of a component action.  This allows more flexibility for submitting forms.  For instance,
 * if you set the functionName to "submitForm" and action set to processAction, you could have a 
 * WOPopupButton with the following binding:
 * 
 * onChange = "submitForm();";
 * 
 * Which will cause the form to submit when the popup button changes and call the action specified
 * in the ERXJSSubmitFunction (in this example, processAction).
 * 
 * @binding action the action to call when the javascript function is called and the form is submitted
 * @binding formName the name of the form to submit (right now you must set the name properly on the desired form)
 * @binding functionName the name of the javascript function that executes the submit
 * @binding name the name of the hidden field that identifies which action is to be executed
 * 
 * @author mschrag
 */
public class ERXJSSubmitFunction extends WODynamicElement {
  protected WOAssociation _action;
  protected WOAssociation _name;
  protected WOAssociation _functionName;
  protected WOAssociation _disabled;
  protected WOAssociation _formName;

  public ERXJSSubmitFunction(String _elementName, NSDictionary _associations, WOElement woelement) {
    super(_elementName, _associations, null);
    _action = (WOAssociation) _associations.objectForKey("action");
    _formName = (WOAssociation) _associations.objectForKey("formName");
    if (_formName == null) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'formName' must be set.");
    }
    _functionName = (WOAssociation) _associations.objectForKey("functionName");
    if (_functionName == null) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'functionName' must be set.");
    }
    if (_action != null && _action.isValueConstant()) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'action' is a constant.");
    }
    _name = (WOAssociation) _associations.objectForKey("name");
    _disabled = (WOAssociation) _associations.objectForKey("disabled");
  }

  public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
  }

  protected String nameInContext(WOContext _context, WOComponent _component) {
    String name;
    if (_name != null) {
      Object obj = _name.valueInComponent(_component);
      if (obj != null) {
        name = obj.toString();
      }
      else {
        name = null;
      }
    }
    else {
      name = _context.elementID();
      if (name == null) {
        throw new IllegalStateException("<" + getClass().getName() + "> Cannot evaluate 'name' attribute, and context element ID is null.");
      }
    }
    return name;
  }

  public boolean disabledInComponent(WOComponent wocomponent) {
    return _disabled != null && _disabled.booleanValueInComponent(wocomponent);
  }

  public WOActionResults invokeAction(WORequest _request, WOContext _context) {
    Object obj = null;
    com.webobjects.appserver.WOComponent wocomponent = _context.component();
    if (!disabledInComponent(wocomponent) && _context._wasFormSubmitted()) {
      if (_context._isMultipleSubmitForm()) {
        if (ERXStringUtilities.nullForEmptyString((String)_request.formValueForKey(nameInContext(_context, wocomponent))) != null) {
          _context._setActionInvoked(true);
          if (_action != null) {
            obj = (WOActionResults) _action.valueInComponent(wocomponent);
          }
          if (obj == null) {
            obj = _context.page();
          }
        }
      }
      else {
        _context._setActionInvoked(true);
        if (_action != null) {
          obj = (WOActionResults) _action.valueInComponent(wocomponent);
        }
        if (obj == null) {
          obj = _context.page();
        }
      }
    }
    return ((WOActionResults) (obj));
  }

  public void appendToResponse(WOResponse _response, WOContext _context) {
    super.appendToResponse(_response, _context);
    
    WOComponent component = _context.component();
    _response._appendContentAsciiString("<input");
    _response._appendTagAttributeAndValue("type", "hidden", false);
    _response._appendTagAttributeAndValue("value", "", false);
    String name = nameInContext(_context, component);
    _response._appendTagAttributeAndValue("name", name, false);
    _response.appendContentCharacter('>');
    _response.appendContentCharacter('\n');

    String formName = (String) _formName.valueInComponent(component);
    _response._appendContentAsciiString("<script language = \"JavaScript\">\n");
    _response._appendContentAsciiString("function ");
    _response._appendContentAsciiString((String) _functionName.valueInComponent(component));
    _response._appendContentAsciiString("() {\n");

    _response._appendContentAsciiString("  document.forms['");
    _response._appendContentAsciiString(formName);
    _response._appendContentAsciiString("'].elements['");
    _response._appendContentAsciiString(name);
    _response._appendContentAsciiString("'].value = 'performAction';\n");
    
    _response._appendContentAsciiString("  document.forms['");
    _response._appendContentAsciiString(formName);
    _response._appendContentAsciiString("'].submit();\n");

    _response._appendContentAsciiString("}\n");
    _response._appendContentAsciiString("</script>\n");
  }
}
