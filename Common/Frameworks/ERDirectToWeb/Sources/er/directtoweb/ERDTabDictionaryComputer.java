/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERTabDictionaryComputer.java created by patrice on Fri 01-Dec-2000 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;

public class ERDTabDictionaryComputer extends TabDictionaryComputer implements ERDComputingAssignmentInterface {

    public ERDTabDictionaryComputer (EOKeyValueUnarchiver u) { super(u); }
    public ERDTabDictionaryComputer (String key, Object value) { super(key,value); }

    // the class in D2W does not play nice with the new caching scheme
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "displayPropertyKeys" });
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }
}
