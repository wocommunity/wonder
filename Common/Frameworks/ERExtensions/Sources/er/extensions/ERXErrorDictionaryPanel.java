/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;

public class ERXErrorDictionaryPanel extends WOComponent {

    public ERXErrorDictionaryPanel(WOContext aContext) {
        super(aContext);
    }
    
    public NSMutableDictionary errorMessages=new NSMutableDictionary();
    public NSMutableArray errorKeyOrder;
    public String errorKey;
    //protected String errorMessageItem;
    public String extraErrorMessage;

    public boolean hasErrors() {
        boolean hasErrors = false;
        if (errorMessages!=null)
            hasErrors = errorMessages.count()>0 || extraErrorMessage!=null && extraErrorMessage.length()>0;
        return hasErrors;
    }

    public NSArray errorKeys() {
        return errorKeyOrder != null ? errorKeyOrder : (errorMessages != null ? errorMessages.allKeys() : ERXConstant.EmptyArray);
    }

    public String errorMessageItem() { return (String)errorMessages.objectForKey(errorKey); }
    
    public Boolean _shouldShowNewLineAbove;
    public boolean shouldShowNewLineAbove() {
        boolean result = false;
        if (_shouldShowNewLineAbove != null)
            result = ERXUtilities.booleanValue(_shouldShowNewLineAbove);
        return result;
    }

    public void takeValuesFromRequest(WORequest r, WOContext c) {
        if (errorMessages != null)
            errorMessages.removeAllObjects();
        if (errorKeyOrder != null)
            errorKeyOrder.removeAllObjects();
        extraErrorMessage=null;
        super.takeValuesFromRequest(r,c);
    }

    private final static String eliminable = "Could not save your changes: null";
    public static String massageErrorMessage(String initialMessage, String displayErrorKey) {
        String result=ERXExtensions.substituteStringByStringInString("EOValidationException:",
                                                                    "",
                                                                    initialMessage);
        if (displayErrorKey!=null) result=ERXExtensions.substituteStringByStringInString(ERXGenericRecord.KEY_MARKER,
                                                                                        displayErrorKey,
                                                                                        result);
        
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
            } else if (result.indexOf(eliminable)>0)
                result=result.substring(eliminable.length()+1, result.length());
        }
        return result;
    }
    
    public void appendToResponse(WOResponse r, WOContext c) {
        // this is a little silly but has the advantage of minimizing impact
        // on other pieces of code
        if(errorMessages!=null) {
            for (Enumeration e=errorMessages.keyEnumerator(); e.hasMoreElements();) {
                String key=(String)e.nextElement();
                String value=(String)errorMessages.objectForKey(key);
                errorMessages.setObjectForKey(massageErrorMessage(value, key), key);
            }
        }
        extraErrorMessage=massageErrorMessage(extraErrorMessage, null);
        super.appendToResponse(r,c);
    }
}
