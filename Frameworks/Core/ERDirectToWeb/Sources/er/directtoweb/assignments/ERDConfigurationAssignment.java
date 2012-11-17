/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments;

import com.webobjects.eocontrol.EOKeyValueUnarchiver;

import er.directtoweb.assignments.defaults.ERDDefaultConfigurationNameAssignment;

/**
 * @deprecated use {@link er.directtoweb.assignments.defaults.ERDDefaultConfigurationNameAssignment}
 */
@Deprecated
public class ERDConfigurationAssignment extends ERDDefaultConfigurationNameAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERDConfigurationAssignment(EOKeyValueUnarchiver u) { super(u); }
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDConfigurationAssignment.class, ERDDefaultConfigurationNameAssignment.class);
        return new ERDDefaultConfigurationNameAssignment(eokeyvalueunarchiver);
    }
}
