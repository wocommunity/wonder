/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;

public class ERD2WEditYesNo extends D2WEditBoolean {

        public ERD2WEditYesNo(WOContext context) {super(context);}
    
    protected Integer yesNoBoolean;

    public Integer yesNoBoolean() {
        return (Integer)object().valueForKey(propertyKey());
    }
    
    public void setYesNoBoolean(Integer newYesNoBoolean) {
        object().validateTakeValueForKeyPath(newYesNoBoolean, propertyKey());
    }

    public String radioBoxGroupName(){
        return ("YesNoGroup_"+d2wContext().displayNameForProperty());
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
