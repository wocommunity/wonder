/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.wrox.wo;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
//import er.wrox.User;

public class UserPreferencesAssignment extends Assignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new UserPreferencesAssignment(eokeyvalueunarchiver);
    }
    
    public UserPreferencesAssignment (EOKeyValueUnarchiver u) { super(u); }
    public UserPreferencesAssignment (String key, Object value) { super(key,value); }

    public Object fire(D2WContext c) { return er.wrox.User.userPreferences(); }
}
