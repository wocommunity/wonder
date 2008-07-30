/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.eocontrol.EOKeyValueUnarchiver;

/**
 * @deprecated use ERDDefaultConfigurationNameAssignment
 */
public class ERDConfigurationAssignment extends ERDDefaultConfigurationNameAssignment {
    public ERDConfigurationAssignment(EOKeyValueUnarchiver u) { super(u); }
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDConfigurationAssignment.class, ERDDefaultConfigurationNameAssignment.class);
        return new ERDDefaultConfigurationNameAssignment(eokeyvalueunarchiver);
    }
}
