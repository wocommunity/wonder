/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import com.webobjects.directtoweb.ERD2WUtilities;
import er.extensions.*;

public class ERDDefaultEntityNameAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultEntityNameAssignment(eokeyvalueunarchiver);
    }
    
    public ERDDefaultEntityNameAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDDefaultEntityNameAssignment (String key, Object value) { super(key,value); }

    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "entity.name"});
    public NSArray dependentKeys(String keyPath) {
        return _DEPENDENT_KEYS;
    }

    // Default names
    public Object displayNameForEntity(D2WContext c) {
        String value = ERD2WUtilities.displayNameForKey((String)c.valueForKey("entity.name"));
        return localizedValueForKeyWithDefaultInContext(value, c);
    }

   // a fake entity that can be used for tasks such as error/confirm..
    private EOEntity _dummyEntity;
    public Object entity(D2WContext c) {
        if (_dummyEntity==null) {
            _dummyEntity=new EOEntity();
            _dummyEntity.setName("__Dummy__");
        }
        return _dummyEntity;
    }
}