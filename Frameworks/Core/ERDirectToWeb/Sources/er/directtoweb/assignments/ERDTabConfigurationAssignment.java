/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

import er.directtoweb.assignments.defaults.ERDDefaultConfigurationNameAssignment;

// MOVEME: ERDConfigurationAssignment
/**
 * Generated pageConfigurations that will use the tab inspect templates.
 * @deprecated use {@link er.directtoweb.assignments.defaults.ERDDefaultConfigurationNameAssignment} with key inspectTabConfigurationName
 */
@Deprecated
public class ERDTabConfigurationAssignment extends ERDDefaultConfigurationNameAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDTabConfigurationAssignment.class, ERDDefaultConfigurationNameAssignment.class);
        return new ERDTabConfigurationAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
     public ERDTabConfigurationAssignment(EOKeyValueUnarchiver u) { super(u); }
     
     /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDTabConfigurationAssignment(String key, Object value) { super(key,value); }

    @Override
    public Object inspectConfigurationName(D2WContext c) { return "InspectTab" + (c.valueForKey("object") != null ?((EOEnterpriseObject)c.valueForKey("object")).entityName() :                                                                                         c.entity().name()); }
}
