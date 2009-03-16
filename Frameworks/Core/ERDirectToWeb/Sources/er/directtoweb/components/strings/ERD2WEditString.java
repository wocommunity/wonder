/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEditString;

/**
 * Allows specifing the maxLength for a WOTextField.<br />
 * 
 */

public class ERD2WEditString extends D2WEditString {

        public ERD2WEditString(WOContext context) { super(context); }
    
    public void validationFailedWithException(Throwable theException,Object theValue, String theKeyPath) {
        // This is for number formatting exceptions
        String keyPath = theKeyPath.equals("value") ? propertyKey() : theKeyPath;
        parent().validationFailedWithException(theException, theValue, keyPath);
    }
}
