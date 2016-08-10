/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.delayed;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.ERDComputingAssignmentInterface;

/**
 * This is an implementation of the KeyValueAssignment
 * implemented as a {@link ERDDelayedAssignment}. In the
 * usual cache scheme key-value assignments are cached
 * the first time that they are used. This may not be the 
 * intended use, i.e. you have a key value assignment with
 * the value "session.user.firstName", the result of this
 * computation would be cached the first time this rule is 
 * fired. With a delayed key value assignment everytime 
 * this assignment is the optimal choice from the cache
 * it will be fired and return that result.   
 */
public class ERDDelayedKeyValueAssignment extends ERDDelayedAssignment implements ERDComputingAssignmentInterface  {
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
        return new ERDDelayedKeyValueAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDelayedKeyValueAssignment(EOKeyValueUnarchiver u) { super(u); }

    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignmnet
     */
    public ERDDelayedKeyValueAssignment(String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon an array composed of the <code>value</code>
     * of this assignment. This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        return new NSArray(value());
    }

    /**
     * Implementation of the abstract method from
     * {@link ERDDelayedAssignment}. This method is
     * called each time this Assignment is resolved
     * from the rule cache. For the delayed key value
     * assignment this method simply calls
     * <code>valueForKeyPath</code> on the passed in 
     * context using the <code>value</code> of the
     * assignment as the key.
     * @param c current D2W context
     * @return result of <code>valueForKeyPath</code>
     *		called on the current context with the
     *		value of this assignment.
     */
    @Override
    public Object fireNow(D2WContext c) { 
        return c.valueForKeyPath((String)value()); 
    }
}
