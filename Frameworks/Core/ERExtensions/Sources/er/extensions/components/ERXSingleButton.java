/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.extensions.appserver.ERXSession;
import er.extensions.components._private.ERXSubmitButton;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXValueUtilities;

/**
 * A stand alone submit button to be used as an action button.
 *<p>
 * This is useful for cancel buttons which should not submit the
 * page and create all the validation messages. It can also create
 * its own FORM, so you can drop this component anywhere.
 *
 * @binding action
 * @binding value
 * @binding doNotUseForm If <code>true</code>, do not output a form, ever.
 *          If <code>false</code> or not specified, do what is more efficient.
 * @binding actionClass
 * @binding directActionName
 * @binding target
 * @binding shouldSubmitForm If <code>false</code>, will let the submit button
 *          use javascript code to set "document.location", which does not submit
 *          the form the button is in. The default value is <code>false</code>.
 * @binding name If is null takes context.elementID
 * @binding class the CSS class for the button
 * @binding style the CSS style for the button
 * @binding id the id for the button
 */
public class ERXSingleButton extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXSingleButton(WOContext aContext) {
        super(aContext);
    }

    // determines whether this component will output its own form or not
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

    public boolean useButton() {
    	return ERXPatcher.classForName("WOSubmitButton").equals(ERXSubmitButton.class);
    }
    
    @Override
    public void appendToResponse(WOResponse aResponse, WOContext aContext) {
    	if(useButton()) {
    		ERXSubmitButton.appendIEButtonFixToResponse(aContext, aResponse);
    	}
    	super.appendToResponse(aResponse, aContext);
    }

    public boolean shouldSubmitForm() {
        return hasBinding("shouldSubmitForm") ? ERXValueUtilities.booleanValue(valueForBinding("shouldSubmitForm")) : false;
    }
    
    @Override
    public String componentName() {
        return hasBinding("name") ? String.valueOf(valueForBinding("name")) : context().elementID();
    }

    public boolean useSubmitButton() {
        return shouldSubmitForm() || !((ERXSession)session()).javaScriptEnabled();
    }

    /*
     * Use JavaScript to completely bypass form submission, when possible. This effectively
     * makes the button behave like a hyperlink. The processing of takeValuesFromRequest is
     * in this case more efficient.
     */
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

