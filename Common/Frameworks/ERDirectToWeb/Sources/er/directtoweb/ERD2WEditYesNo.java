/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.directtoweb.D2WEditBoolean;
import er.extensions.*;

public class ERD2WEditYesNo extends D2WEditBoolean {
    public static ERXLogger log = ERXLogger.getERXLogger(ERD2WEditYesNo.class);

    public ERD2WEditYesNo(WOContext context) { super(context); }

    public Integer yesNoBoolean() {
        Object bool = object().valueForKeyPath(propertyKey());
        bool = new Integer(ERXUtilities.booleanValue(bool) ? 1 : 0);
        return (Integer)bool;
    }
    
    public void setYesNoBoolean(Integer newYesNoBoolean) {
        object().validateTakeValueForKeyPath(newYesNoBoolean, propertyKey());
    }

    public String radioBoxGroupName(){
        return ("YesNoGroup_"+d2wContext().propertyKey());
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
