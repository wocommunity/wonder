/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.directtoweb.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;

public class ERDUnitResolverAssignment extends ERDDelayedAssignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDUnitResolverAssignment(eokeyvalueunarchiver);
    }
    
    public ERDUnitResolverAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDUnitResolverAssignment (String key, Object value) { super(key,value); }

    public Object fireNow(D2WContext c) {
        String userInfoUnitString = (String)c.valueForKey("unit");
        if (userInfoUnitString == null)
            userInfoUnitString = (String)c.valueForKeyPath("smartAttribute.userInfo.unit");
        return ERDirectToWeb.resolveUnit(userInfoUnitString,
                                         (EOEnterpriseObject)c.valueForKey("object"),
                                         c.propertyKey());
    }    
}
