/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* DelayedAssignment.java created by patrice on Tue 17-Apr-2001 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import org.apache.log4j.*;

//	This is essentially an assignment that will be fired everytime, note that the ERD2WModel will
//	cache the assignment instead of the value it returns.
//	Interesting subclasses are DelayedBooleanAssignment and DelayedConditionalAssignment

public abstract class ERDDelayedAssignment extends Assignment  {
    
    public ERDDelayedAssignment(EOKeyValueUnarchiver u) { super(u); }
    public ERDDelayedAssignment(String key, Object value) { super(key,value); }

    public Object fire(D2WContext c) { return this; }
    public abstract Object fireNow(D2WContext c);
    
}
