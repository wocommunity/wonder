/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.components;

import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXEnterpriseObject;
import er.extensions.localization.ERXLocalizer;

/**
 * Useful for displaying a dictionary of caught exception messages.<br />
 * 
 * @binding errorMessages An NSMutableDictionary that contains the list of errors to display
 * @binding extraErrorMessage An String containing an error. If errorMessages is also set, 
 * 		extraErrorMessage will be added below errorMessages
 * @binding shouldShowNewLineAbove An Boolean, if true, will add a new empty line above 
 * 		the list of errors
 * @binding shouldShowNewLineBelow An Boolean, if true, will add a new empty line below 
 * 		the list of errors
 * @binding errorKeyOrder An NSMutableArray containing a list of string keys to sort
 * 		the errorMessages list
 */

public class ERXErrorDictionaryPanel extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    protected NSMutableDictionary errorMessages;
    protected NSMutableArray errorKeyOrder;
    protected String extraErrorMessage;

    public String errorKey;

    public ERXErrorDictionaryPanel(WOContext aContext) {
        super(aContext);
    }

    public NSMutableDictionary errorMessages() {
        if(errorMessages == null) {
            errorMessages = (NSMutableDictionary)valueForBinding("errorMessages");
            if(errorMessages == null) {
                errorMessages = new NSMutableDictionary(); 
            }
        }
        return errorMessages;
    }

    public NSMutableArray errorKeyOrder() {
        if(errorKeyOrder == null)
            errorKeyOrder = (NSMutableArray)valueForBinding("errorKeyOrder");
        return errorKeyOrder;
    }
    //protected String errorMessageItem;
    public String extraErrorMessage() {
        if(extraErrorMessage == null)
            extraErrorMessage = (String)valueForBinding("extraErrorMessage");
        return extraErrorMessage;
    }
    //public boolean shouldShowNewLineAbove;
    //public boolean shouldShowNewLineBelow;
    
    public boolean hasErrors() {
        boolean hasErrors = false;
        hasErrors = errorMessages().count()>0 || (extraErrorMessage() != null && extraErrorMessage().length() > 0);
        return hasErrors;
    }

    public NSArray errorKeys() {
        return errorKeyOrder() != null ? errorKeyOrder() : errorMessages().allKeys();
    }

    public String errorMessageItem() { return (String)errorMessages().objectForKey(errorKey); }
    
    private final static String eliminable = "Could not save your changes: null";
    private final static String couldNotSave = "Could not save your changes: ";
    public static String massageErrorMessage(String initialMessage, String displayErrorKey) {
        String result = StringUtils.replace(initialMessage, "EOValidationException:", "");
        if (displayErrorKey!=null) {
        	result = StringUtils.replace(result, ERXEnterpriseObject.KEY_MARKER, displayErrorKey);
        }
        
        if (result!=null) {
            if (result.endsWith("is not allowed to be null.") ||
                (result.startsWith(" The ") &&
                 result.indexOf(" property ")!=-1 &&
                 result.indexOf(" must have a ")!=-1 &&
                 result.endsWith(" assigned"))
                ) {
                char c;
                if (displayErrorKey == null) {
                    result=result.substring(result.indexOf("'")+1,
                                            result.indexOf("is not allowed to be null.")-2);
                    c=result.charAt(0);
                } else {
                    result = displayErrorKey;
                    c=result.toLowerCase().charAt(0);
                }
                String article=(c=='a' || c=='e' || c=='i' || c=='o' || c=='u') ? "an" : "a";
                result="Please provide "+article+" <b>"+result+"</b>.";
            } else if (result.indexOf(": Invalid number")!=-1) {
                int colon=result.indexOf(':');
                result= "<b>"+(displayErrorKey==null ?  result.substring(0,colon-1) : displayErrorKey);
                result+="</b>: I could not understand the number you typed.";
            } else if (result.indexOf(eliminable)>0) {
                result=result.substring(eliminable.length()+1, result.length());
            }
            if(result.indexOf(couldNotSave)>0) {
                String replace = (String)ERXLocalizer.currentLocalizer().valueForKey(couldNotSave);
                if(replace != null)
                    result=replace + result.substring(couldNotSave.length()+1, result.length());
            }
        }
        return result;
    }

    public Object value;

    @Override
    public void reset() {
        super.reset();
        errorMessages = null;
        errorKeyOrder = null;
        extraErrorMessage = null;
    }

    @Override
    public void appendToResponse(WOResponse r, WOContext c) {
        // this is a little silly but has the advantage of minimizing impact
        // on other pieces of code
        for (Enumeration e=errorMessages().keyEnumerator(); e.hasMoreElements();) {
            String key=(String)e.nextElement();
            String errorMessageValue=(String)errorMessages().objectForKey(key);
            errorMessages().setObjectForKey(massageErrorMessage(errorMessageValue, key), key);
        }
        extraErrorMessage=massageErrorMessage(extraErrorMessage(), null);
        super.appendToResponse(r,c);
    }
}
