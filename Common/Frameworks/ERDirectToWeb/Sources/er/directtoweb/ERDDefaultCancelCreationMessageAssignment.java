/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.directtoweb.*;
import com.webobjects.eocontrol.*;

public class ERDDefaultCancelCreationMessageAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultCancelCreationMessageAssignment(eokeyvalueunarchiver);
    }
    
    public ERDDefaultCancelCreationMessageAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDDefaultCancelCreationMessageAssignment (String key, Object value) { super(key,value); }

    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] {"entity.name"});
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    public Object cancelMessage(D2WContext c) {
        Object value = localizedTemplateStringForKeyInContext("Are you sure you want to stop creating this Object", c);
        return value;
    }
}