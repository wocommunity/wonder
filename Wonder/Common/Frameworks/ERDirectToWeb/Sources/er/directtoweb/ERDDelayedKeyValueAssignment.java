/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;

///////////////////////////////////////////////////////////////////////////////
// KeyValueAssignment implemented as a delayed assignment.  In our current
// caching scheme KeyValueAssignments are cached the first time they fire.  This
// is usually not the behaviour you want.
///////////////////////////////////////////////////////////////////////////////
public class ERDDelayedKeyValueAssignment extends ERDDelayedAssignment implements ERDComputingAssignmentInterface  {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedKeyValueAssignment(eokeyvalueunarchiver);
    }
    
    public ERDDelayedKeyValueAssignment(EOKeyValueUnarchiver u) { super(u); }
    public ERDDelayedKeyValueAssignment(String key, Object value) { super(key,value); }

    public NSArray _dependentKeys;
    public NSArray dependentKeys(String keyPath) {
        if (_dependentKeys==null)
            _dependentKeys=new NSArray(value());
        return _dependentKeys;
    }

    public Object fireNow(D2WContext c) { return c.valueForKeyPath((String)value()); }
}
