//
// ERXWOForm.java
// Project armehaut
//
// Created by ak on Mon Apr 01 2002
//

package er.extensions;

import org.apache.log4j.Logger;

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
public class ERXWOForm extends com.webobjects.appserver._private.WOForm {
    // This constant is currently only used with Ajax.  If you change this value, you
    // must also change it in AjaxUtils.
    public static final String FORCE_FORM_SUBMITTED_KEY = "_forceFormSubmitted";
    static final Logger log = Logger.getLogger(ERXWOForm.class);
    WOAssociation _formName;
    WOAssociation _enctype;
    WOAssociation _fragmentIdentifier;
    WOAssociation _secure;
    WOAssociation _disabled;
    
    public ERXWOForm(String name, NSDictionary associations,
                     WOElement template) {
        super(name, associations, template);
        _formName = (WOAssociation) _associations.removeObjectForKey("name");
        _enctype = (WOAssociation) _associations.removeObjectForKey("enctype");
        _fragmentIdentifier = (WOAssociation) _associations.removeObjectForKey("fragmentIdentifier");
        _secure = (WOAssociation) _associations.removeObjectForKey("secure");
        _disabled = (WOAssociation) _associations.removeObjectForKey("disabled");
    }
    
    public void takeValuesFromRequest(WORequest request, WOContext context) {
      String forceFormSubmittedElementID = (String)request.formValueForKey(ERXWOForm.FORCE_FORM_SUBMITTED_KEY);
      boolean forceFormSubmitted = (forceFormSubmittedElementID != null && forceFormSubmittedElementID.equals(context.elementID()));
      if (forceFormSubmitted) {
        context._setFormSubmitted(true);
      }
      super.takeValuesFromRequest(request, context);
      if (forceFormSubmitted) {
        context._setFormSubmitted(false);
      }
    }

    public void appendAttributesToResponse(WOResponse response, WOContext context) {
    	if(_formName != null) {
    		String formName = (String)_formName.valueInComponent(context.component());
    		if(formName != null) {
    			ERXWOContext.contextDictionary().setObjectForKey(formName, "formName");
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
        Object fragmentIdentifier = (_fragmentIdentifier != null ? _fragmentIdentifier.valueInComponent(context.component()) : null);
        if (secure || fragmentIdentifier != null) {
            //FIXME: (ak) we assume that relative URL creation is on by default, so we may restore the wrong type 
            WOResponse newResponse = new WOResponse();
            if(secure) {
                context._generateCompleteURLs();
                super.appendAttributesToResponse(newResponse, context);
                context._generateRelativeURLs();
            } else {
                super.appendAttributesToResponse(newResponse, context);
            }
            String attributes = newResponse.contentString();
            if(secure && attributes.indexOf("action=\"http://") >= 0) {
                attributes = attributes.replaceFirst("action=\"http://", "action=\"https://");
            }
            if(fragmentIdentifier != null) {
                attributes = attributes.replaceFirst("action=\"([^\"]*)\"", "action=\"$1#" + fragmentIdentifier + "\"");
            }
            
            response.appendContentString(attributes);
        } else {
            super.appendAttributesToResponse(response, context);
        }
    }
     
    public void appendToResponse(WOResponse response, WOContext context) {
        boolean wasInForm = context.isInForm();
        
        context.setInForm(true);
        
        boolean disable = _disabled != null && _disabled.booleanValueInComponent(context.component());
        boolean shouldAppendFormTags = !disable && !wasInForm;
        
        if (shouldAppendFormTags) {
            _appendOpenTagToResponse(response, context);
            appendChildrenToResponse(response, context);
            _appendCloseTagToResponse(response, context);
            ERXWOContext.contextDictionary().removeObjectForKey("formName");
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
