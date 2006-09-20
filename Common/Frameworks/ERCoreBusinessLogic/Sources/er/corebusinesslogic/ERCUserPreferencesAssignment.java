/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.corebusinesslogic;

import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

public class ERCUserPreferencesAssignment extends Assignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERCUserPreferencesAssignment(eokeyvalueunarchiver);
    }    
    
    public ERCUserPreferencesAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERCUserPreferencesAssignment (String key, Object value) { super(key,value); }

    public Object fire(D2WContext c) { return ERCoreUserPreferences.userPreferences(); }
}
