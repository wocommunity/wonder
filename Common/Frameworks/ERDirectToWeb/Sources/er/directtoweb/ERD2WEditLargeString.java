/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WEditLargeString;

public class ERD2WEditLargeString extends D2WEditLargeString {

    public ERD2WEditLargeString(WOContext context) { super(context); }
    // Quick around for the following problem:
    // address2 is not mandatory, which means it often ends up as null in the DB
    // WOText however transforms this into "". WOTextField does not do this.

    public Object validateTakeValueForKeyPath(Object newValue, String keyPath) {
        Object result=null;
        if (newValue!=null && ((String)newValue).length()==0) {
            if (value()!=null)
                result=super.validateTakeValueForKeyPath(newValue,keyPath);
            else
                result=super.validateTakeValueForKeyPath(null,keyPath);
        } else
            result=super.validateTakeValueForKeyPath(newValue,keyPath);
        return result;
    }
}
