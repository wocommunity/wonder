/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* UnitResolverAssignment.java created by patrice on Sun 03-Dec-2000 */
package er.directtoweb;

import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

public class ERDUnitResolverAssignment extends ERDDelayedAssignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDUnitResolverAssignment(eokeyvalueunarchiver);
    }
    
    public ERDUnitResolverAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDUnitResolverAssignment (String key, Object value) { super(key,value); }

    // FIXME warning: note the 'object' in the list of dependent keys..
    //public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "smartAttribute", "propertyKey", "unit", "object" });
    //public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    public Object fireNow(D2WContext c) {
        String userInfoUnitString = (String)c.valueForKey("unit");
        if (userInfoUnitString == null)
            userInfoUnitString = (String)c.valueForKeyPath("smartAttribute.userInfo.unit");
        return ERDirectToWeb.resolveUnit(userInfoUnitString,
                                         (EOEnterpriseObject)c.valueForKey("object"),
                                         c.propertyKey());
    }
    
}
