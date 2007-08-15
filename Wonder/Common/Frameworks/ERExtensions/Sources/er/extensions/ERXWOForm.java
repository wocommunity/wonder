//
// ERXWOForm.java
// Project armehaut
//
// Created by ak on Mon Apr 01 2002
//

package er.extensions;

import java.lang.reflect.Method;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODynamicElementCreationException;
import com.webobjects.appserver._private.WOHTMLDynamicElement;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation._NSDictionaryUtilities;

/** 
 * Transparent replacement for WOForm. You don't really need to do anything to use it, because it
 * will get used instead of WOForm elements automagically. In addition, it has a few new features:
 * <ul>
 * <li> it adds the FORM's name to the ERXWOContext's mutableUserInfo as as "formName" key,
 * which makes writing JavaScript elements a bit easier.
 * <li> it warns you when you have one FORM embedded inside another and ommits the tags for the nested FORM.
 * <li> it pushes the <code>enctype</code> into the userInfo, so that {@link ERXWOFileUpload}
 * can check if it is set correctly. ERXFileUpload will throw an exception if the enctype is not set.
 * <li> it has a "fragmentIdentifier" binding, which appends "#" + the value of the binding to the action. 
 * The obvious case comes when you have a form at the bottom of the page
 * and want to jump to the error messages if there are any.  
 * <li> it adds the <code>secure</code> boolean binding that rewrites the URL to use <code>https</code>.
 * <li> it adds the <code>disabled</code> boolean binding allows you to omit the form tag.
 * </ul>
 * This subclass is installed when the frameworks loads. <br />
 * If you actually want to see those new bindings in WOBuilder, edit the file 
 * <code>WebObjects Builder.app/Contents/Resources/WebObjectDefinitions.xml</code>, which 
 * contains the .api for the dynamic elements.
 * @author ak
 * @author Mike Schrag (idea to secure binding)
 */  
public class ERXWOForm extends com.webobjects.appserver._private.WOHTMLDynamicElement {
    // This constant is currently only used with Ajax.  If you change this value, you
    // must also change it in AjaxUtils.
    public static final String FORCE_FORM_SUBMITTED_KEY = "_forceFormSubmitted";
    static final Logger log = Logger.getLogger(ERXWOForm.class);
    
    WOAssociation _formName;
    WOAssociation _enctype;
    WOAssociation _fragmentIdentifier;
    WOAssociation _secure;
    WOAssociation _disabled;

    protected WOAssociation _action;
    protected WOAssociation _href;
    protected WOAssociation _multipleSubmit;
    protected WOAssociation _actionClass;
    protected WOAssociation _queryDictionary;
    protected NSDictionary _otherQueryAssociations;
    protected WOAssociation _directActionName;
    protected WOAssociation _addDefaultSubmitButton;

    public static boolean addDefaultSubmitButtonDefault = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXWOForm.addDefaultSubmitButtonDefault", false);
    
    public ERXWOForm(String s, NSDictionary nsdictionary, WOElement woelement) {
    	super("form", nsdictionary, woelement);
    	_otherQueryAssociations = _NSDictionaryUtilities.extractObjectsForKeysWithPrefix(_associations, "?", true);
    	if(_otherQueryAssociations.count() == 0) {
    		_otherQueryAssociations = null;
    	}
    	_action = (WOAssociation)_associations.removeObjectForKey("action");
    	_href = (WOAssociation)_associations.removeObjectForKey("href");
    	_multipleSubmit = (WOAssociation)_associations.removeObjectForKey("multipleSubmit");
    	_actionClass = (WOAssociation)_associations.removeObjectForKey("actionClass");
    	_queryDictionary = (WOAssociation)_associations.removeObjectForKey("queryDictionary");
    	_directActionName = (WOAssociation)_associations.removeObjectForKey("directActionName");
    	_formName = (WOAssociation) _associations.removeObjectForKey("name");
    	_enctype = (WOAssociation) _associations.removeObjectForKey("enctype");
    	_fragmentIdentifier = (WOAssociation) _associations.removeObjectForKey("fragmentIdentifier");
    	_secure = (WOAssociation) _associations.removeObjectForKey("secure");
    	_disabled = (WOAssociation) _associations.removeObjectForKey("disabled");
    	_addDefaultSubmitButton = (WOAssociation) _associations.removeObjectForKey("addDefaultSubmitButton");
    	if(_associations.objectForKey("method") == null && _associations.objectForKey("Method") == null && _associations.objectForKey("METHOD") == null) {
    		_associations.setObjectForKey(new WOConstantValueAssociation("post"), "method");
    	}
    	if(_action != null && _href != null || _action != null && _directActionName != null || _href != null && _directActionName != null || _action != null && _actionClass != null || _href != null && _actionClass != null) {
    		throw new WODynamicElementCreationException("<" + getClass().getName() + ">: At least two of these conflicting attributes are present: 'action', 'href', 'directActionName', 'actionClass'");
    	}
    	if(_action != null && _action.isValueConstant()) {
    		throw new WODynamicElementCreationException("<" + getClass().getName() + ">: 'action' is a constant.");
    	}
    }


    public String toString() {
    	return "<" + getClass().getName() + " action: " + (_action == null ? "null" : _action.toString()) + " actionClass: " + (_actionClass == null ? "null" : _actionClass.toString()) + " directActionName: " + (_directActionName == null ? "null" : _directActionName.toString()) + " href: " + (_href == null ? "null" : _href.toString()) + " multipleSubmit: " + (_multipleSubmit == null ? "null" : _multipleSubmit.toString()) + " queryDictionary: " + (_queryDictionary == null ? "null" : _queryDictionary.toString()) + " otherQueryAssociations: " + (_otherQueryAssociations == null ? "null" : _otherQueryAssociations.toString()) + " >";
    } 

    protected boolean _enterFormInContext(WOContext context) {
    	boolean wasInForm = context.isInForm();
    	context.setInForm(true);
    	if(context.elementID().equals(context.senderID())) {
        	context._setFormSubmitted(true);
    	}
    	return wasInForm;
    }

    protected void _exitFormInContext(WOContext context, boolean wasInForm, boolean wasFormSubmitted) {
    	context.setInForm(wasInForm);
    	context._setFormSubmitted(wasFormSubmitted);
    }

    public WOActionResults invokeAction(WORequest worequest, WOContext context) {
    	boolean wasFormSubmitted = context._wasFormSubmitted();
		boolean wasInForm = _enterFormInContext(context);
		boolean wasMultipleSubmitForm = context._isMultipleSubmitForm();
    	context._setActionInvoked(false);
		context._setIsMultipleSubmitForm(_multipleSubmit == null ? false : _multipleSubmit.booleanValueInComponent(context.component()));
    	if (!wasInForm) {
    		_setFormName(context);
    	}
    	WOActionResults result = super.invokeAction(worequest, context);
    	if(!wasInForm && !context._wasActionInvoked() && context._wasFormSubmitted()) {
    		if(_action != null) {
    			result = (WOActionResults)_action.valueInComponent(context.component());
    		}
    		if (result == null && !ERXAjaxApplication.isAjaxSubmit(worequest)) {
    			result = context.page();
    		}
    	}
		context._setIsMultipleSubmitForm(wasMultipleSubmitForm);
    	_exitFormInContext(context, wasInForm, wasFormSubmitted);
    	if (!wasInForm) {
    		_clearFormName(context);
    	}
    	return result;
    }

    // WO 5.4
//    protected NSDictionary computeQueryDictionaryInContext(String aRequestHandlerPath, WOAssociation queryDictionary, NSDictionary otherQueryAssociations, WOContext aContext) {
//    	try {
//			Class woFormClass = getClass();
//			__queryDictionaryInContext(queryDictionary, aContext);
//			Method __queryDictionaryInContextMethod = woFormClass.getMethod("__queryDictionaryInContext", new Class[] { WOAssociation.class, WOContext.class });
//			NSDictionary aQueryDict = (NSDictionary) __queryDictionaryInContextMethod.invoke(this, new Object[] { queryDictionary, aContext });
//			
//			Method __otherQueryDictionaryInContextMethod = woFormClass.getMethod("__otherQueryDictionaryInContext", new Class[] { NSDictionary.class, WOContext.class });
//			NSDictionary anotherQueryDict = (NSDictionary) __otherQueryDictionaryInContextMethod.invoke(this, new Object[] { otherQueryAssociations, aContext });
//
//			Method computeQueryDictionaryMethod = woFormClass.getMethod("computeQueryDictionary", new Class[] { String.class, NSDictionary.class, NSDictionary.class });
//			NSDictionary queryDict = (NSDictionary) computeQueryDictionaryMethod.invoke(this, new Object[] { aRequestHandlerPath, aQueryDict, anotherQueryDict });
//			return queryDict;
//		}
//		catch (Exception e) {
//			throw new RuntimeException("computeQueryDictionaryInContext failed.", e);
//		}
//    }
    
    protected void _appendHiddenFieldsToResponse(WOResponse response, WOContext context) {
    	boolean flag = _actionClass != null;
    	NSDictionary hiddenFields;
    	if (ERXApplication.isWO54()) {
			try {
				Method computeQueryDictionaryInContextMethod = WOHTMLDynamicElement.class.getDeclaredMethod("computeQueryDictionaryInContext", new Class[] { String.class, WOAssociation.class, NSDictionary.class, WOContext.class });
				hiddenFields = (NSDictionary) computeQueryDictionaryInContextMethod.invoke(this, new Object[] { "", _queryDictionary, _otherQueryAssociations, context });
			}
			catch (Exception e) {
				throw new RuntimeException("computeQueryDictionaryInContext failed.", e);
			}
    	}
    	else {
			try {
				Method computeQueryDictionaryInContextMethod = WOHTMLDynamicElement.class.getDeclaredMethod("computeQueryDictionaryInContext", new Class[] { WOAssociation.class, WOAssociation.class, WOAssociation.class, boolean.class, NSDictionary.class, WOContext.class });
				hiddenFields = (NSDictionary) computeQueryDictionaryInContextMethod.invoke(this, new Object[] { _actionClass, _directActionName, _queryDictionary, Boolean.valueOf(flag), _otherQueryAssociations, context });
			}
			catch (Exception e) {
				throw new RuntimeException("computeQueryDictionaryInContext failed.", e);
			}
    	}
    	if(hiddenFields.count() > 0) {
    		for(Enumeration enumeration = hiddenFields.keyEnumerator(); enumeration.hasMoreElements(); ) {
    			String s = (String)enumeration.nextElement();
    			Object obj = hiddenFields.objectForKey(s);
    			response._appendContentAsciiString("<input type=\"hidden\"");
    			response._appendTagAttributeAndValue("name", s, false);
    			response._appendTagAttributeAndValue("value", obj.toString(), false);
    			response._appendContentAsciiString(" />\n");
    		}

    	}
    }

    public void appendChildrenToResponse(WOResponse response, WOContext context) {
    	super.appendChildrenToResponse(response, context);
    	_appendHiddenFieldsToResponse(response, context);
    }

    protected String cgiAction(WOResponse response, WOContext context) {
    	String s = computeActionStringInContext(_actionClass, _directActionName, context);
    	return context.directActionURLForActionNamed(s, null);
    }

    public void takeValuesFromRequest(WORequest request, WOContext context) {
    	boolean wasFormSubmitted = context._wasFormSubmitted();
		boolean wasInForm = _enterFormInContext(context);
    	//log.info(this._formName + "->" + this.toString().replaceAll(".*(keyPath=\\w+).*", "$1"));
    	String forceFormSubmittedElementID = (String)request.formValueForKey(ERXWOForm.FORCE_FORM_SUBMITTED_KEY);
    	boolean forceFormSubmitted = (forceFormSubmittedElementID != null && forceFormSubmittedElementID.equals(context.elementID()));
    	if (forceFormSubmitted) {
    		context._setFormSubmitted(true);
    	}
    	if (!wasInForm) {
    		_setFormName(context);
    	}
    	super.takeValuesFromRequest(request, context);
    	if (forceFormSubmitted) {
    		context._setFormSubmitted(false);
    	}
    	//log.info(context.elementID() + "->" + context.senderID() + "->" + context._wasFormSubmitted());
    	_exitFormInContext(context, wasInForm, wasFormSubmitted);
    	if (!wasInForm) {
    		_clearFormName(context);
    	}
    }

    protected void _setFormName(WOContext context) {
    	if(_formName != null) {
    		String formName = (String)_formName.valueInComponent(context.component());
    		if(formName != null) {
    			ERXWOContext.contextDictionary().setObjectForKey(formName, "formName");
    		}
    	}
    }

    protected void _clearFormName(WOContext context) {
    	if(_formName != null) {
    		String formName = (String)_formName.valueInComponent(context.component());
    		if(formName != null) {
    			ERXWOContext.contextDictionary().removeObjectForKey("formName");
    		}
    	}
    }
    
    public void appendAttributesToResponse(WOResponse response, WOContext context) {
    	_setFormName(context);
    	if(_formName != null) {
    		String formName = (String)_formName.valueInComponent(context.component());
    		if(formName != null) {
    			response._appendTagAttributeAndValue("name", formName, false);
    		}
    	}
    	if(_enctype != null) {
    		String enctype = (String)_enctype.valueInComponent(context.component());
    		if(enctype != null) {
    			ERXWOContext.contextDictionary().setObjectForKey(enctype.toLowerCase(), "enctype");
    			response._appendTagAttributeAndValue("enctype", enctype, false);
    		}
    	}
        boolean secure = _secure != null && _secure.booleanValueInComponent(context.component());
        Object hrefObject = null;
    	WOComponent wocomponent = context.component();
    	super.appendAttributesToResponse(response, context);
    	if(secure) {
            context._generateCompleteURLs();
    	}
    	if(_href != null) {
    		hrefObject = _href.valueInComponent(wocomponent);
    	} else if(_directActionName != null || _actionClass != null) {
    		hrefObject = cgiAction(response, context);
    	} else {
    		hrefObject = context.componentActionURL();
    	}
    	if(hrefObject != null) {
    		String href = hrefObject.toString();
    		Object fragmentIdentifier = (_fragmentIdentifier != null ? _fragmentIdentifier.valueInComponent(context.component()) : null);
    		if(secure) {
    			href = href.replaceFirst("http://", "https://");
            }
            if(fragmentIdentifier != null) {
            	href = href + "#" + fragmentIdentifier;
            }
            response._appendTagAttributeAndValue("action", href, false);
    	} else {
			NSLog.err.appendln("<WOForm> : action attribute evaluates to null");
		}
    	if(secure) {
//    		FIXME: (ak) we assume that relative URL creation is on by default, so we may restore the wrong type 
            context._generateRelativeURLs();
    	}
    }

    public void appendToResponse(WOResponse response, WOContext context) {
        boolean wasInForm = context.isInForm();
        
        context.setInForm(true);
        
        boolean disable = _disabled != null && _disabled.booleanValueInComponent(context.component());
        boolean shouldAppendFormTags = !disable && !wasInForm;

        if (shouldAppendFormTags) {
        	_appendOpenTagToResponse(response, context);
        	if(_multipleSubmit != null && _multipleSubmit.booleanValueInComponent(context.component())) {
        		if(_addDefaultSubmitButton != null && _addDefaultSubmitButton.booleanValueInComponent(context.component())
        				|| (_addDefaultSubmitButton == null  && addDefaultSubmitButtonDefault)) {
        			response._appendContentAsciiString("<input type=\"submit\" style=\"position: absolute; left: -100px; top: -1000px; \" name=\"WOFormDummySubmit\" value=\"WOFormDummySubmit\" />");
        		}
        	}
        	appendChildrenToResponse(response, context);
            _appendCloseTagToResponse(response, context);
            _clearFormName(context);
            ERXWOContext.contextDictionary().removeObjectForKey("enctype");
        } else {
        	if(!disable) {
        		log.warn("This FORM is embedded inside another FORM. Omitting Tags: " + this.toString());
            }
            appendChildrenToResponse(response, context);
        }

        context.setInForm(wasInForm);
    }
    
    /**
     * Retrieves the current FORM's name in the supplied context. If none is set 
     * (either the FORM is not a ERXWOForm or the context is not ERXMutableUserInfo) the supplied default
     * value is used. 
     * @param context current context
     * @param defaultName default name to use
     * @return form name in context or default value
     */
    public static String formName(WOContext context, String defaultName) {
        String formName = (String) ERXWOContext.contextDictionary().objectForKey("formName");
        if(formName == null) {
            formName = defaultName;
        }
        return formName;
    }
}
