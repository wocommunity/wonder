//
// ERXWOForm.java
// Project armehaut
//
// Created by ak on Mon Apr 01 2002
//

package er.extensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;

/** 
 * Transparent replacement for WOForm. You don't really need to do anything to use it, because it
 * will get used instead of WOForm elements automagically. In addition, it has a few new features:
 * <ul>
 * <li> it adds the FORM's name to the ERXWOContext's mutableUserInfo as as "formName" key,
 * which makes writing JavaScript elements a bit easier.
 * <li> it warns you when you have one FORM embedded inside another and ommits the tags for the nested FORM.
 * <li> it pushes the <code>enctype</code> into the userInfo, so that {@link ERXWOFileUpload}
 * can check if it is set correctly. ERXFileUpload will throw an exception if the enctype is not set.
 * <li> it has a "fragmentIdentifier" binding, which, when set spews out some javascript that appends 
 * "#" + the value of the binding to the action. The obvious case comes when you have a form at the bottom of the page
 * and want to jump to the error messages if there are any.  That javascript is used is an implementation 
 * detail, though and shouldn't be relied on.
 * <li> it adds the <code>secure</code> boolean binding that rewrites the URL to use <code>https</code>.
 * </ul>
 * This subclass is installed when the frameworks loads. 
 * @author ak
 * @author Mike Schrag (idea to secure binding)
 */  
public class ERXWOForm extends com.webobjects.appserver._private.WOForm {
    static final ERXLogger log = ERXLogger.getERXLogger(ERXWOForm.class);
    WOAssociation _formName;
    WOAssociation _enctype;
    WOAssociation _fragmentIdentifier;
    WOAssociation _secure;
    
    public ERXWOForm(String name, NSDictionary associations,
                     WOElement template) {
        super(name, associations, template);
        _formName = (WOAssociation) _associations.removeObjectForKey("name");
        _enctype = (WOAssociation) _associations.removeObjectForKey("enctype");
        _fragmentIdentifier = (WOAssociation) _associations.removeObjectForKey("fragmentIdentifier");
        _secure = (WOAssociation) _associations.removeObjectForKey("secure");
    }

    public void appendAttributesToResponse(WOResponse response, WOContext context) {
        if(context != null && context instanceof ERXMutableUserInfoHolderInterface) {
            NSMutableDictionary ui = ((ERXMutableUserInfoHolderInterface)context).mutableUserInfo();
            if(_formName != null) {
                String formName = (String)_formName.valueInComponent(context.component());
                if(formName != null) {
                    ui.setObjectForKey(formName, "formName");
                    response._appendTagAttributeAndValue("name", formName, false);
                }
            }
            if(_enctype != null) {
                String enctype = (String)_enctype.valueInComponent(context.component());
                if(enctype != null) {
                    ui.setObjectForKey(enctype.toLowerCase(), "enctype");
                    response._appendTagAttributeAndValue("enctype", enctype, false);
                }
            }
        }
        boolean secure = _secure != null && _secure.booleanValueInComponent(context.component());
        if (secure) {
            //FIXME: (ak) we assume that relative URL creation is on by default, so we may restore the wrong type 
            WOResponse newResponse = new WOResponse();
            context._generateCompleteURLs();
            super.appendAttributesToResponse(newResponse, context);
            context._generateRelativeURLs();
            
            String action = newResponse.contentString();
            if(action.indexOf("action=\"http://") >= 0) {
                action = action.replaceFirst("action=\"http://", "action=\"https://");
            }
            response.appendContentString(action);
        } else {
            super.appendAttributesToResponse(response, context);
        }
    }
     
    public void appendToResponse(WOResponse response, WOContext context) {
        boolean inForm = context.isInForm();
        
        context.setInForm(true);
        if (context != null && response != null) {

            String elementName = elementName();
            boolean shouldAppendFormTags = !inForm  && (elementName != null);

            if (shouldAppendFormTags)
                _appendOpenTagToResponse(response, context);
            else
                log.warn("This FORM is embedded inside another FORM. Omitting Tags.");

            this.appendChildrenToResponse(response, context);

            if (shouldAppendFormTags)
                _appendCloseTagToResponse(response, context);
        }
        context.setInForm(false);
        if(_fragmentIdentifier != null) {
            Object value = _fragmentIdentifier.valueInComponent(context.component());
            if(value != null) {
                response.appendContentString("<script language=\"javascript\">document.forms[document.forms.length-1].action+=\"#"+value.toString()+"\";</script>");
            }
        }
        if(context instanceof ERXMutableUserInfoHolderInterface) {
            NSMutableDictionary ui = ((ERXMutableUserInfoHolderInterface)context).mutableUserInfo();
            
            ui.removeObjectForKey("formName");
            ui.removeObjectForKey("enctype");
        }
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
    	String formName = defaultName;
    	if(context instanceof ERXMutableUserInfoHolderInterface) {
        	formName = (String) ((ERXMutableUserInfoHolderInterface)context).mutableUserInfo().objectForKey("formName");
        }
    	return formName;
    }
}
