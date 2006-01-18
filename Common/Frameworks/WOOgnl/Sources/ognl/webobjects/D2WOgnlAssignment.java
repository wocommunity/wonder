/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* D2WOgnlAssignment.java created by max on Tue 16-Oct-2001 */
package ognl.webobjects;

import com.webobjects.directtoweb.*;
import com.webobjects.eocontrol.*;

public class D2WOgnlAssignment extends Assignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new D2WOgnlAssignment(eokeyvalueunarchiver);
    }

    public D2WOgnlAssignment(EOKeyValueUnarchiver u) { super(u); }
    public D2WOgnlAssignment(String key, Object value) { super(key,value); }

    public Object fire(D2WContext c) { return WOOgnl.factory().getValue((String)value(), c); }
}
