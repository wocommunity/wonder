/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.delayed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.assignments.ERDComputingAssignmentInterface;
import er.extensions.foundation.ERXValueUtilities;

///////////////////////////////////////////////////////////////////////////////
// Stepchild of DelayedConditionalAssignment
//	Takes three entries in a dictionary format.
//	conditionKey - keyPath of the condition fired off of the d2wContext.
//	trueValue - the value used if the condition returns true
//	falseValue - the value used if the condition returns false
///////////////////////////////////////////////////////////////////////////////
/**
 * Takes a condition and evalutaes this condition everytime the rule is asked for.
 */

public class ERDDelayedBooleanAssignment extends ERDDelayedAssignment implements ERDComputingAssignmentInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger("er.directtoweb.rules.DelayedBooleanAssignment");

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedBooleanAssignment(eokeyvalueunarchiver);
    }    

    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDelayedBooleanAssignment(EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDelayedBooleanAssignment(String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the value of the key "conditionKey" of the
     * value of this assignment. This key is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        NSDictionary booleanConditions = (NSDictionary)value();
        return new NSArray(booleanConditions.objectForKey("conditionKey"));
    }

    @Override
    public Object fireNow(D2WContext c) {
        NSDictionary booleanConditions = (NSDictionary)value();
        log.debug("Resolving delayed fire for boolean conditions: {}", booleanConditions);
        return ERXValueUtilities.booleanValue(c.valueForKeyPath((String)booleanConditions.objectForKey("conditionKey"))) ?
            booleanConditions.objectForKey("trueValue") : booleanConditions.objectForKey("falseValue");

    }
}
