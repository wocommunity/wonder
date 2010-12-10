/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.defaults;

import com.webobjects.eocontrol.EOKeyValueUnarchiver;

import er.directtoweb.assignments.ERDAssignment;


/**
 * @deprecated use ERDDefaultModelAssignment
 */

public class ERDDefaultsAssignment extends ERDDefaultModelAssignment {
    public ERDDefaultsAssignment(EOKeyValueUnarchiver u) { super(u); }
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDDefaultsAssignment.class, ERDDefaultModelAssignment.class);
        return new ERDDefaultModelAssignment(eokeyvalueunarchiver);
    }
}
