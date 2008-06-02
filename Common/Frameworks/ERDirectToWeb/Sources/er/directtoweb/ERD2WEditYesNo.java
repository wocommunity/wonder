/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2WEditBoolean;
import com.webobjects.foundation.NSValidation;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Edits a boolean with radio buttons and Yes/No<br />
 * 
 */

public class ERD2WEditYesNo extends D2WEditBoolean {
    public static Logger log = Logger.getLogger(ERD2WEditYesNo.class);
	private String _groupName;

    public ERD2WEditYesNo(WOContext context) { super(context); }

    public Integer yesNoBoolean() {
        Object bool = object().valueForKeyPath(propertyKey());
        bool = new Integer(ERXValueUtilities.booleanValue(bool) ? 1 : 0);
        return (Integer)bool;
    }
    
    public void awake() {
    	_groupName = "YesNoGroup_"+context().elementID();
    }
    public void sleep() {
    	_groupName = null;
    }
    
    public void setYesNoBoolean(Integer newYesNoBoolean) {
        object().validateTakeValueForKeyPath(newYesNoBoolean, propertyKey());
    }

    public String radioBoxGroupName() {
        return _groupName;
    }

    public void validationFailedWithException(Throwable theException,Object theValue, String theKeyPath) {
        parent().validationFailedWithException(theException, theValue, theKeyPath);
    }

    public void takeValuesFromRequest(WORequest r, WOContext c) {
        super.takeValuesFromRequest(r,c);
        try {
            object().validateTakeValueForKeyPath(objectPropertyValue(), propertyKey());
        } catch (NSValidation.ValidationException e) {
            validationFailedWithException(e, objectPropertyValue(), propertyKey());
        }
    }
}
