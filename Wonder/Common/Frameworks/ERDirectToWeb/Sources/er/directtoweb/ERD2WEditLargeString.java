/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

/**
 * Works around an issue in WOText where a null value is transformed into "". This is not what WOTextField does.<br />
 * 
 */

public class ERD2WEditLargeString extends D2WEditLargeString {

    public ERD2WEditLargeString(WOContext context) { super(context); }
    // Quick around for the following problem:
    // address2 is not mandatory, which means it often ends up as null in the DB
    // WOText however transforms this into "". WOTextField does not do this.

    public void validationFailedWithException(Throwable theException,Object theValue, String theKeyPath) {
        // This is for number formatting exceptions
        String keyPath = theKeyPath.equals("value") ? propertyKey() : theKeyPath;
        parent().validationFailedWithException(theException, theValue, keyPath);
    }

    public Object validateTakeValueForKeyPath(Object newValue, String keyPath) {
        return super.validateTakeValueForKeyPath((newValue!=null && ((String)newValue).length()==0) ? null : newValue, keyPath);
    }
}
