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

import er.directtoweb.ERDirectToWeb;
import er.directtoweb.assignments.delayed.ERDDelayedAssignment;

/**
 * Used to resolve units off of EOAttributes.  Will resolve paths with "@foo" off of the object itself.
 */

public class ERDUnitResolverAssignment extends ERDDelayedAssignment {
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
        return new ERDUnitResolverAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDUnitResolverAssignment (EOKeyValueUnarchiver u) { super(u); }

    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDUnitResolverAssignment (String key, Object value) { super(key,value); }

    @Override
    public Object fireNow(D2WContext c) {
        String userInfoUnitString = (String)c.valueForKey("unit");
        if (userInfoUnitString == null)
            userInfoUnitString = (String)c.valueForKeyPath("smartAttribute.userInfo.unit");
        return ERDirectToWeb.resolveUnit(userInfoUnitString,
                                         (EOEnterpriseObject)c.valueForKey("object"),
                                         c.propertyKey());
    }    
}
