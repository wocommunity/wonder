package er.extensions.components.javascript;

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

import er.extensions.foundation.ERXStringUtilities;

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

  public ERXJSSubmitFunction(String elementName, NSDictionary associations, WOElement woelement) {
    super(elementName, associations, null);
    _action = (WOAssociation) associations.objectForKey("action");
    _formName = (WOAssociation) associations.objectForKey("formName");
    if (_formName == null) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'formName' must be set.");
    }
    _functionName = (WOAssociation) associations.objectForKey("functionName");
    if (_functionName == null) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'functionName' must be set.");
    }
    if (_action != null && _action.isValueConstant()) {
      throw new WODynamicElementCreationException("<" + getClass().getName() + "> 'action' is a constant.");
    }
    _name = (WOAssociation) associations.objectForKey("name");
    _disabled = (WOAssociation) associations.objectForKey("disabled");
  }

  @Override
  public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
  }

  protected String nameInContext(WOContext context, WOComponent component) {
    String name;
    if (_name != null) {
      Object obj = _name.valueInComponent(component);
      if (obj != null) {
        name = obj.toString();
      }
      else {
        name = null;
      }
    }
    else {
      name = context.elementID();
      if (name == null) {
        throw new IllegalStateException("<" + getClass().getName() + "> Cannot evaluate 'name' attribute, and context element ID is null.");
      }
    }
    return name;
  }

  public boolean disabledInComponent(WOComponent component) {
    return _disabled != null && _disabled.booleanValueInComponent(component);
  }

  @Override
  public WOActionResults invokeAction(WORequest request, WOContext context) {
    Object obj = null;
    WOComponent component = context.component();
    if (!disabledInComponent(component) && context.wasFormSubmitted()) {
      if (context.isMultipleSubmitForm()) {
        if (ERXStringUtilities.nullForEmptyString((String) request.formValueForKey(nameInContext(context, component))) != null) {
          context.setActionInvoked(true);
          if (_action != null) {
            obj = _action.valueInComponent(component);
          }
          if (obj == null) {
            obj = context.page();
          }
        }
      }
      else {
        context.setActionInvoked(true);
        if (_action != null) {
          obj = _action.valueInComponent(component);
        }
        if (obj == null) {
          obj = context.page();
        }
      }
    }
    return (WOActionResults) obj;
  }

  @Override
  public void appendToResponse(WOResponse response, WOContext context) {
    super.appendToResponse(response, context);

    WOComponent component = context.component();
    String name = nameInContext(context, component);
    String functionName = (String) _functionName.valueInComponent(component);
    String formName = (String) _formName.valueInComponent(component);

    response.appendContentString("<input type = \"hidden\" value = \"\" name = \"" + name + "\">\n");
    response.appendContentString("<script language = \"JavaScript\">\n");
    response.appendContentString("function " + functionName + "() {\n");
    response.appendContentString("  document.forms['" + formName + "'].elements['" + name + "'].value = 'performAction';\n");
    response.appendContentString("  document.forms['" + formName + "'].submit();\n");
    response.appendContentString("}\n");
    response.appendContentString("</script>\n");
  }
}