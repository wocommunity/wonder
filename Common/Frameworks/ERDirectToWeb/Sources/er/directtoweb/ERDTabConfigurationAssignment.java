/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERAssignment.java created by max on Tue 10-Oct-2000 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import java.lang.reflect.*;
import java.util.*;

public class ERDTabConfigurationAssignment extends ERDConfigurationAssignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDTabConfigurationAssignment(eokeyvalueunarchiver);
    }
    
    public ERDTabConfigurationAssignment(EOKeyValueUnarchiver u) { super(u); }
    public ERDTabConfigurationAssignment(String key, Object value) { super(key,value); }

    public Object inspectConfigurationNameForEntity(D2WContext c) { return "InspectTab" + (c.valueForKey("object") != null ?
                                                                                        ((EOEnterpriseObject)c.valueForKey("object")).entityName() :
                                                                                        c.entity().name()); }
}
