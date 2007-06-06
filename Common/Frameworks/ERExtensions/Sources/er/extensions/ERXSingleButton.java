/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

// A Submit button that can be used stand alone.
/**
 * A stand alone submit button to be used as an action button.<br />
 * Usefull for cancel buttons which should not submit the page and create all the validation messages.
 * Can also create its own FORM, so you can drop it anywhere.
 * @binding action
 * @binding value
 * @binding doNotUseForm" defaults="Boolean
 * @binding actionClass
 * @binding directActionName
 * @binding target
 * @binding shouldSubmitForm" defaults="Boolean
 */

public class ERXSingleButton extends WOComponent {

    public ERXSingleButton(WOContext aContext) {
        super(aContext);
    }

    /* Bindings:

doNotUseForm: if true, do not output a form ever. If false or not specified, do what is more efficient.
shouldSubmitForm: if false, will let the submit button use javascript code to set document.location, which does not submit the form the button is in. the default is false
    
    */

    public boolean isStateless() { return true; }


    // determines wether this component will output its own form or not
    public boolean useForm() {
        boolean result=hasBinding("doNotUseForm") ? !ERXValueUtilities.booleanValue(valueForBinding("doNotUseForm")) : true;
        // however, if the form does not have to be submitted AND javascript is enabled, no need for a form
        if (result && !shouldSubmitForm() &&
            ((ERXSession)session()).javaScriptEnabled() &&
            !((ERXSession)session()).browser().isNetscape() &&
            !((ERXSession)session()).browser().isOmniWeb())
            result=false;
        return result;
    }

    public String buttonCssClass() {
    	String css = (String) valueForBinding("css");
    	if(css == null) {
    		css = "";
    	}
    	WOAssociation assoc = _associationWithName("action");
    	if(assoc != null) {
    		css += " " + ERXSubmitButton.STYLE_PREFIX + assoc.keyPath().replaceAll("\\W+", "");
    	} else {
    		css += " " + ERXSubmitButton.STYLE_PREFIX + valueForBinding("directActionName");
    	}
    	if(css.length() == 0) {
    		css = null;
    	}
    	return css;
    }

    public boolean useButton() {
    	return ERXPatcher.classForName("WOSubmitButton").equals(ERXSubmitButton.class);
    }
    
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	if(useButton()) {
    		ERXSubmitButton.appendIEButtonFixToResponse(aResponse);
    	}
    	super.appendToResponse(aResponse, aContext);
    }

    // determines wether the form this component is in (wether it was output by this component or not
    // has to be submitted or can be bypassed
    public boolean shouldSubmitForm() {
        return hasBinding("shouldSubmitForm") ? ERXValueUtilities.booleanValue(valueForBinding("shouldSubmitForm")) : false;
    }

    public boolean useSubmitButton() {
        return shouldSubmitForm() || !((ERXSession)session()).javaScriptEnabled();
    }

    // When possible we use JavaScript to completely bypass form submission (effectively ending up with a button behaving like a
    // hyperlink). The processing of takeValuesFromRequest is in this case more efficient
    public String jsString() {
        String directActionName=(String)valueForBinding("directActionName");
        String url=null;
        if (directActionName!=null) {
            String actionClass=(String)valueForBinding("actionClass");
            String directActionURL=actionClass!=null ? directActionName : actionClass + "/" + directActionName;
            url=context().directActionURLForActionNamed(directActionURL, null);
        } else {
            url=context().componentActionURL();
        }
        return !shouldSubmitForm() ? "javascript:document.location='"+ url +"'; return false;" : "";
    }

}
