/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import com.webobjects.directtoweb.*;

public class ERDRelationshipSortAssignment extends ERDAssignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDRelationshipSortAssignment(eokeyvalueunarchiver);
    }
    
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "keyWhenRelationship", "propertyKey"  });
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    public ERDRelationshipSortAssignment (String key, Object value) { super(key,value); }
    public ERDRelationshipSortAssignment(EOKeyValueUnarchiver unarchiver) { super(unarchiver); }

    public boolean canBeTakenIntoAccountForCache() { return true; }

    public Object sortKeyForList(D2WContext context) {
        return (String)context.valueForKey("propertyKey")+"."+(String)context.valueForKey("keyWhenRelationship");
    }
}
