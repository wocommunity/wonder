/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;

/**
 * Allows editing boolean values based on radio buttons and strings.<br />
 * 
 */

public class ERD2WCustomEditBoolean extends D2WEditBoolean {

    public ERD2WCustomEditBoolean(WOContext context) {
        super(context);
    }

    protected NSArray _choicesNames;

    public void reset(){
        super.reset();
        _choicesNames = null;
    }

    public Object yesNoBoolean() {
        Object value = object().valueForKeyPath(propertyKey());
        if(null == value)
            value = nullValue();
        return value;
    }
    public void setYesNoBoolean(Object newYesNoBoolean) {
        if(nullValue().equals(newYesNoBoolean))
            newYesNoBoolean = null;
        object().validateTakeValueForKeyPath(newYesNoBoolean, propertyKey());
    }
    public String radioBoxGroupName(){
        return ("YesNoGroup_"+d2wContext().propertyKey());
    }

    public NSArray choicesNames(){
        if(_choicesNames == null)
            _choicesNames = (NSArray)d2wContext().valueForKey("choicesNames");
        return _choicesNames;
    }

    public String yesName(){
        return (String)choicesNames().objectAtIndex(0);
    }

    public String noName(){
        return (String)choicesNames().objectAtIndex(1);
    }

    public String unsetName(){
        return (String)choicesNames().objectAtIndex(2);
    }

    public Object nullValue(){
        return "ERXUnsetBooleanValue";
    }

    public void validationFailedWithException(Throwable theException,Object theValue, String theKeyPath) {
        parent().validationFailedWithException(theException, theValue, theKeyPath);
    }

    public Object validateTakeValueForKeyPath(Object object, String string) {
        if(nullValue().equals(object)) {
            object().validateTakeValueForKeyPath(null, propertyKey());
            return null;
        }
        return super.validateTakeValueForKeyPath(object, string);
    }

    public void takeValuesFromRequest(WORequest r, WOContext c) {
        super.takeValuesFromRequest(r,c);
        try {
            object().validateTakeValueForKeyPath(objectPropertyValue(), propertyKey());
        } catch (NSValidation.ValidationException e) {
            validationFailedWithException(e, objectPropertyValue(), propertyKey());
        }
    }

    public boolean allowsNull() {
        return choicesNames().count() > 2;
    }
}
