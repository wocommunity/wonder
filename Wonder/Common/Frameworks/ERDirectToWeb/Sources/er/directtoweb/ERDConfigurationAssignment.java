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

public class ERDConfigurationAssignment extends ERDAssignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDConfigurationAssignment(eokeyvalueunarchiver);
    }
    
    public ERDConfigurationAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDConfigurationAssignment (String key, Object value) { super(key,value); }

    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "object.entityName", "entity.name"});
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    // Default configuration names
    public Object confirmConfigurationNameForEntity(D2WContext c) {
        return "Confirm" +  (c.valueForKey("object") != null ?
                             ((EOEnterpriseObject)c.valueForKey("object")).entityName() :
                             c.entity().name()); }
    public Object createConfigurationNameForEntity(D2WContext c) {
        return "Create" +  (c.valueForKey("object") != null ?
                            ((EOEnterpriseObject)c.valueForKey("object")).entityName() :
                            c.entity().name()); }
    public Object editConfigurationNameForEntity(D2WContext c) {
        return "Edit" + (c.valueForKey("object") != null ?
                         ((EOEnterpriseObject)c.valueForKey("object")).entityName() :
                         c.entity().name()); }
    public Object inspectConfigurationNameForEntity(D2WContext c) {
        return "Inspect" + (c.valueForKey("object") != null ?
                            ((EOEnterpriseObject)c.valueForKey("object")).entityName() :
                            c.entity().name()); }
    public Object listConfigurationNameForEntity(D2WContext c) {
        return "List" +  (c.valueForKey("object") != null ?
                          ((EOEnterpriseObject)c.valueForKey("object")).entityName() :
                          c.entity().name()); }
}
