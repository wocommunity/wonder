/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.delayed;

import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

//	This is essentially an assignment that will be fired everytime, note that the ERD2WModel will
//	cache the assignment instead of the value it returns.
//	Interesting subclasses are DelayedBooleanAssignment and DelayedConditionalAssignment
/**
 * Crazy assignment used when you actually don't want the computed value cached.
 */

public abstract class ERDDelayedAssignment extends Assignment  {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */    
    public ERDDelayedAssignment(EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDelayedAssignment(String key, Object value) { super(key,value); }

    // ENHANCEME: Might want to make this method final
    @Override
    public Object fire(D2WContext c) { return this; }
    
    /**
     * Implemented by subclasses.
     */
    public abstract Object fireNow(D2WContext c);
}
