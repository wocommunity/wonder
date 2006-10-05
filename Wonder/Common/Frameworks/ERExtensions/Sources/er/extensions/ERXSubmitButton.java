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
    protected static boolean useButton;
    
    public ERXSubmitButton(String arg0, NSDictionary nsdictionary, WOElement arg2) {
        super("button", nsdictionary, arg2);
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
    	// System.out.println(worequest.formValues());
    	/*WOComponent wocomponent = wocontext.component();
    	if(!disabledInComponent(wocomponent) && wocontext._wasFormSubmitted()) {
    		String s1 = nameInContext(wocontext, wocomponent);
    		if(s1 != null) {
    			String s = worequest.stringFormValueForKey(s1);
    			_value.setValue(s, wocomponent);
    		}
    	}*/
    }

    protected String elementName(WOContext wocontext) {
    	return elementName();
    }
    
    protected void _appendOpenTagToResponse(WOResponse woresponse, WOContext wocontext) {
    	if(useButton) {
    		woresponse.appendContentCharacter('<');
    		woresponse._appendContentAsciiString(elementName(wocontext));   	
        	appendAttributesToResponse(woresponse, wocontext);
        	woresponse.appendContentCharacter('>');
    	} else {
           	woresponse._appendContentAsciiString("<span class=\"ERXSubmitButton\"><a");   	
        	appendAttributesToResponse(woresponse, wocontext);
        	woresponse.appendContentCharacter('>');
    	}
    }
    
    public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
    	appendConstantAttributesToResponse(woresponse, wocontext);
    	appendNonURLAttributesToResponse(woresponse, wocontext);
    	appendURLAttributesToResponse(woresponse, wocontext);
    	if(disabledInComponent(wocontext.component())) {
    		woresponse.appendContentCharacter(' ');
    		woresponse._appendContentAsciiString("disabled=\"disabled\"");
    	}
    	if(useButton) {
    		_appendValueAttributeToResponse(woresponse, wocontext);
    		_appendNameAttributeToResponse(woresponse, wocontext);
    	} else {
    		woresponse._appendContentAsciiString(" style=\"display: inline-block; text-decoration: none;\"; href=\"#\" onclick=\"this.firstChild.click(); void(0);\">");
    		woresponse._appendContentAsciiString("<input type=\"submit\" style=\"display:none;\"");

    		_appendValueAttributeToResponse(woresponse, wocontext);
    		_appendNameAttributeToResponse(woresponse, wocontext);
    		woresponse._appendContentAsciiString(" /");   	
    	}
    }

    protected void _appendCloseTagToResponse(WOResponse woresponse, WOContext wocontext) {
    	if(useButton) {
       		woresponse._appendContentAsciiString("</");
       		woresponse._appendContentAsciiString(elementName(wocontext));
       		woresponse.appendContentCharacter('>');
       		
     	} else {
       		woresponse._appendContentAsciiString("</a></span>");  
    	}
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
    
    public void appendChildrenToResponse(WOResponse woresponse, WOContext wocontext) {
        if(hasChildrenElements()) {
            super.appendChildrenToResponse(woresponse, wocontext);
        } else {
            String value = (String) _value.valueInComponent(wocontext.component());
            if(value == null) {
                value = "Submit";
            }
            woresponse._appendContentAsciiString(value);
        }
    }

    protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
        if(_directActionName != null || _actionClass != null)
            woresponse._appendTagAttributeAndValue("name", _actionClassAndName(wocontext), false);
        else
            super._appendNameAttributeToResponse(woresponse, wocontext);
    }

    public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
    	useButton = false;
    	if(wocontext == null || woresponse == null) {
    		return;
    	}
    	String s = elementName();
    	if(s != null) {
    		_appendOpenTagToResponse(woresponse, wocontext);
    	}
    	appendChildrenToResponse(woresponse, wocontext);
    	if(s != null) {
    		_appendCloseTagToResponse(woresponse, wocontext);
    	}
    	if(_directActionName != null || _actionClass != null) {
    		woresponse._appendContentAsciiString("<input type=\"hidden\" name=\"WOSubmitAction\"");
    		woresponse._appendTagAttributeAndValue("value", _actionClassAndName(wocontext), false);
    		woresponse.appendContentString(" />");
    	}
    }
}