/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.NSArray;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.directtoweb.ERD2WUtilities;
import er.extensions.*;

public class ERDDefaultPropertyNameAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {
    static final ERXLogger log = ERXLogger.getLogger(ERDDefaultPropertyNameAssignment.class);

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultPropertyNameAssignment(eokeyvalueunarchiver);
    }
    
    public ERDDefaultPropertyNameAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDDefaultPropertyNameAssignment (String key, Object value) { super(key,value); }

    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "propertyKey"});
    public NSArray dependentKeys(String keyPath) {
        return _DEPENDENT_KEYS;
    }

    // Default names
    public Object displayNameForProperty(D2WContext c) {
        String value = ERD2WUtilities.displayNameForKey((String)c.valueForKey("propertyKey"));
        return localizedValueForKeyWithDefaultInContext(value, c);
    }
}
