/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEditLargeString;

import er.extensions.foundation.ERXValueUtilities;

/**
 * Same as D2WEditLargeString except that it allows you to
 * have empty strings in fields that don't allow null.
 * You need to set <code>isMandatory</code> to false and the null
 * value is morphed to the empty string. It also pulls
 * the <code>disabled</code> binding from the WOContext, allowing
 * you to have a readonly field.
 */

public class ERD2WEditLargeString extends D2WEditLargeString {

    public ERD2WEditLargeString(WOContext context) {
        super(context);
    }

    private Object fixValue(Object value) {
        if("".equals(value)) {
            // AK: this is probably obsolete. It fixes that WOText would give you
            // an empty string instead of null, which was was WOTextField is doing
            // This seems to be fixed in >=5.3.1 where context.stringFormValueForKey returns 
            // null on empty strings.
            value = null;
        }
        if (value == null) {
            boolean fixNullValue = d2wContext().attribute() != null && !d2wContext().attribute().allowsNull();
            if(fixNullValue) {
                fixNullValue = !ERXValueUtilities.booleanValue(d2wContext().valueForKey("isMandatory"));
            }
            if(fixNullValue) {
                value = "";
            }
        }
        return value;
    }

    public void validationFailedWithException(Throwable theException, Object theValue, String theKeyPath) {
        // This is for number formatting exceptions
        String keyPath = theKeyPath.equals("value") ? propertyKey() : theKeyPath;
        parent().validationFailedWithException(theException, theValue, keyPath);
    }

    public Object validateTakeValueForKeyPath(Object value, String keyPath) throws ValidationException {
        value = fixValue(value);
        return super.validateTakeValueForKeyPath(value, keyPath);
    }

    public void setValue(Object value) {
        value = fixValue(value);
        super.setValue(value);
    }
}
