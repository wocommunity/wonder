package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOInput;
import com.webobjects.foundation.NSDictionary;

/**
 * Clone of WOSubmitButton that emits &lt;button&gt; tags instead of 
 * &lt;input&gt; tags. This allows you to use HTML content and superior style
 * features. <br />
 * You can add this class via ERXPatcher.setClassForName(ERXSubmitButton.class, "WOSubmitButton");
 * and see how this works out or use it explicitely.
 * @author ak
 */
public class ERXSubmitButton extends WOInput {

    protected WOAssociation _action;
    protected WOAssociation _actionClass;
    protected WOAssociation _directActionName;

    public ERXSubmitButton(String arg0, NSDictionary nsdictionary, WOElement arg2) {
        super("button", nsdictionary, null);
        if(_value == null)
            _value = new WOConstantValueAssociation("Submit");
        _action = (WOAssociation)_associations.removeObjectForKey("action");
        _actionClass = (WOAssociation)_associations.removeObjectForKey("actionClass");
        _directActionName = (WOAssociation)_associations.removeObjectForKey("directActionName");
        if(_action != null && _action.isValueConstant())
            throw new WODynamicElementCreationException("<" + getClass().getName() + ">'action' is a constant.");
        if(_action != null && _directActionName != null || _action != null && _actionClass != null)
            throw new WODynamicElementCreationException("<" + getClass().getName() + "> Either 'action' and 'directActionName' both exist, or 'action' and 'actionClass' both exist ");
    }

    protected String type() {
        return "submit";
    }

    public String toString() {
        return "<ERXSubmitButton  action: " + (_action == null ? "null" : _action.toString()) + " actionClass: " + (_actionClass == null ? "null" : _actionClass.toString()) + ">";
    }

    public void takeValuesFromRequest(WORequest worequest, WOContext wocontext) {
    }

    public WOActionResults invokeAction(WORequest worequest, WOContext wocontext) {
        Object obj = null;
        WOComponent wocomponent = wocontext.component();
        if(!disabledInComponent(wocomponent) && wocontext._wasFormSubmitted()) {
            if(wocontext._isMultipleSubmitForm()) {
                if(worequest.formValueForKey(nameInContext(wocontext, wocomponent)) != null) {
                    wocontext._setActionInvoked(true);
                    if(_action != null)
                        obj = (WOActionResults)_action.valueInComponent(wocomponent);
                    if(obj == null)
                        obj = wocontext.page();
                }
            } else {
                wocontext._setActionInvoked(true);
                if(_action != null)
                    obj = (WOActionResults)_action.valueInComponent(wocomponent);
                if(obj == null)
                    obj = wocontext.page();
            }
        }
        return ((WOActionResults) (obj));
    }

    private String _actionClassAndName(WOContext wocontext) {
        String s = computeActionStringInContext(_actionClass, _directActionName, wocontext);
        return s;
    }
    
    public void appendChildrenToResponse(WOResponse arg0, WOContext arg1) {
        if(hasChildrenElements()) {
            super.appendChildrenToResponse(arg0, arg1);
        } else {
            String value = (String) _value.valueInComponent(arg1.component());
            if(value == null) {
                value = "Submit";
            }
            arg0._appendContentAsciiString(value);
        }
    }

    protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
        if(_directActionName != null || _actionClass != null)
            woresponse._appendTagAttributeAndValue("name", _actionClassAndName(wocontext), false);
        else
            super._appendNameAttributeToResponse(woresponse, wocontext);
    }

    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
        super.appendToResponse(woresponse, wocontext);
        if(_directActionName != null || _actionClass != null) {
            woresponse._appendContentAsciiString("<input type=\"hidden\" name=\"WOSubmitAction\"");
            woresponse._appendTagAttributeAndValue("value", _actionClassAndName(wocontext), false);
            woresponse.appendContentString(" />");
        }
    }
}