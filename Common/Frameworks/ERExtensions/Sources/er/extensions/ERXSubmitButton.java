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
 * Clone of WOSubmitButton that should emit <code>&lt;button&gt;</code> tags instead of 
 * <code>&lt;input&gt;</code> tags. This allows you to use HTML content and superior style
 * features. <br />
 * Unfortunately, IE is totally broken and always submits all buttons on a page,
 * which makes it unusable for actions. So, for the time being this component emits
 * a <code>&lt;span class="ERXSubmitButton"&gt;</code> with a hyperlink and the real, but invisible submit button
 * inside. The actual click is triggered by javascript, so your IE users must have it turned on.<br />
 * You need some stylesheet similar to this to make it look like a normal <code>button</code>:<pre><code>
 * .ERXSubmitButton a {
 * 	color: buttontext;
 * 	cursor: default;
 * 	background-color: buttonface;
 * 	border-style: outset;
 * 	border-width: 2px;
 * 	border-top-color: buttonface;
 * 	border-left-color: buttonface;
 * 	border-right-color: buttonshadow;
 * 	border-bottom-color: buttonshadow;
 * 	padding: 0px 6px;
 * 	font-size: small;
 * 	font-family: sans-serif;
 * 	line-height: normal !important;
 * }
 * 
 * .ERXSubmitButton a:active {
 * 	border-top-color: buttonshadow;
 * 	border-left-color: buttonshadow;
 * 	border-right-color: buttonface;
 * 	border-bottom-color: buttonface;
 * }
 * </code></pre>
 * but of course, the whole idea is that you can style it any way you want.<br />
 * You can add this class via ERXPatcher.setClassForName(ERXSubmitButton.class, "WOSubmitButton");
 * and see how this works out or use it explicitely.
 * @author ak
 */
public class ERXSubmitButton extends WOInput {

    protected WOAssociation _action;
    protected WOAssociation _actionClass;
    protected WOAssociation _directActionName;
    protected WOAssociation _shouldSubmitForm;
    
    private static String ieFix = "<script>window.btnunload = window.onload;\n" +
    "window.onload = function() {\n" +
    "    var btns = document.getElementsByTagName('button');\n" +
    "    for(var i=0;i<btns.length;i++) {\n" +
    "        btns[i].btnonclick = btns[i].onclick;\n" +
    "        btns[i].onclick = function() {\n" +
    "            var bs = document.getElementsByTagName('button');\n" +
    "            for (var i=0;i<btns.length;i++) {\n" +
    "                if (btns[i] != this) \n" +
    "                    btns[i].disabled = true;\n" +
    "            }" +
    "            this.innerHTML = this.value;\n" +
    "            if(this.btnonclick) return this.btnonclick();\n" +
    "            return true;\n" +
    "        }\n" +
    "    }\n" +
    "    if(window.btnunload) return window.btnunload();\n" +
    "};</script>";
    
    public static void appendIEButtonFixToResponse(WOResponse woresponse) {
		if(!ERXWOContext.contextDictionary().containsKey("ERXWOSubmit.ieFixed")) {
			woresponse._appendContentAsciiString(ieFix);
			ERXWOContext.contextDictionary().setObjectForKey(Boolean.TRUE, "ERXWOSubmit.ieFixed");
		}
    }
    
    public ERXSubmitButton(String arg0, NSDictionary nsdictionary, WOElement arg2) {
        super("button", nsdictionary, arg2);
        if(_value == null)
            _value = new WOConstantValueAssociation("Submit");
        _shouldSubmitForm = (WOAssociation)_associations.removeObjectForKey("shouldSubmitForm");
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
    	//System.out.println(worequest.formValues());
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
    	appendIEButtonFixToResponse(woresponse);
    	woresponse.appendContentCharacter('<');
    	woresponse._appendContentAsciiString(elementName(wocontext));   	
    	appendAttributesToResponse(woresponse, wocontext);
    	woresponse.appendContentCharacter('>');
     }
    
    public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
    	appendConstantAttributesToResponse(woresponse, wocontext);
    	appendNonURLAttributesToResponse(woresponse, wocontext);
    	appendURLAttributesToResponse(woresponse, wocontext);
    	boolean shouldSubmitForm = (_shouldSubmitForm != null ? _shouldSubmitForm.booleanValueInComponent(wocontext.component()) : true);

    	if(disabledInComponent(wocontext.component())) {
    		woresponse.appendContentCharacter(' ');
    		woresponse._appendContentAsciiString("disabled=\"disabled\"");
    	}
    	_appendValueAttributeToResponse(woresponse, wocontext);
    	_appendNameAttributeToResponse(woresponse, wocontext);
    	if(!shouldSubmitForm) {
    		String action = (String) wocontext.componentActionURL();
    		woresponse._appendTagAttributeAndValue("onclick", "document.location.href='" + action + "'; return false;", false);
    	}
    }

    protected void _appendCloseTagToResponse(WOResponse woresponse, WOContext wocontext) {
    	woresponse._appendContentAsciiString("</");
    	woresponse._appendContentAsciiString(elementName(wocontext));
    	woresponse.appendContentCharacter('>');
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
    	if(wocontext == null || woresponse == null) {
    		return;
    	}
    	//System.out.println(useButton + ": " + userAgent);
    	// useButton = !useButton;
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