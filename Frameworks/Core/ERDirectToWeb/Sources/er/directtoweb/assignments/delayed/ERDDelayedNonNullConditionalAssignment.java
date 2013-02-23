/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.delayed;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * The delayed non-null conditional is a way to provide a 
 * branching result from a rule resolution. The value of this 
 * assignment must be a dictionary that has the following keys: 
 *
 * dictionary key "nonNullKeyPath": key path to be tested for nullality
 *		off of the current D2W context.
 * dictionary key "trueValue": value to be returned if the key path is
 *		not null.
 * dictionary key "falseValue": value to be returned if the key path is
 *		null.
 *
 * Because this assignment is a delayed assignment the above condition 
 * will be evaluated everytime that the D2W fired rule cache resolves to
 * a rule of this class.</br>
 * <br/>
 * Example usage. Let's imagine that a User has a relationship called
 * <code>toOwnedHouse</code> this relationship is only set if the User is
 * a home owner. Now let's imagine that we have a page configuration 
 * for displaying information about a User. One of the propertyKeys
 * for this page configuration is 'residence' which is not an attribute 
 * or relationship off of the User object. Imagine that we have already built
 * a custom component for displaying the address of either an owned house or
 * a rented house for a given User. However we want the displayed name to be
 * either "Rented House" or "Owned House" depending on if the User is a home
 * owner. The usual approach would be to create two page configurations and
 * set the displayNameForProperty to be different for each of these page
 * configurations. However by using a DelayedNonNullConditionalAssignment
 * we will only have to use a single page configuration. Using this rule:
 *
 * pageConfguration = 'InspectUser' AND propertyKey = 'residence' =>
 *	displayNameForProperty =</br><code> 
 * {</br>
 *	nonNullKeyPath = "object.toOwnedHouse";</br>
 *	trueValue = "Owned House";</br>
 *	falseValue = "Rented House";</br>
 * }<br/></code>
 */
 // ENHANCEME: Should add a dictionarykey to support both localization and negating.
public class ERDDelayedNonNullConditionalAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public final static Logger log = Logger.getLogger("er.directtoweb.rules.DelayedNonNullConditionalAssigment");

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedNonNullConditionalAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDelayedNonNullConditionalAssignment(EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    // FIXME: Should put a check in that the value dictionary has all of the correct keys.
    public ERDDelayedNonNullConditionalAssignment(String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon an array composed of the single value 
     * "nonNullKeyPath" from the dictionary of the value of this assignment. 
     * This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        NSDictionary conditionAssignment = (NSDictionary)value();
        return new NSArray(conditionAssignment.valueForKey("nonNullKeyPath"));
    }

    /**
     * Implementation of the abstract method from
     * {@link er.directtoweb.assignments.delayed.ERDDelayedAssignment}. This method is
     * called each time this Assignment is resolved
     * from the rule firing cache. For the non-null
     * conditional the dictionary key 'nonNullKeyPath'
     * is checked against the current context. If the 
     * key path is indeed non-null then the object 
     * returned by the dictionarykey 'trueValue' will
     * be returned otherwise the object returned by the
     * dictionarykey 'falseValue' will be returned.
     * @param c current D2W context
     * @return Either the 'trueValeue' or 'falseValue'
     *		depending on if the key path is non-null or
     *		null.
     */
    // FIXME: Should check for NSKeyValueCoding.NullValue
    @Override
    public Object fireNow(D2WContext c) {
        Object result = null;
        String keyPath;
        String resultKey;
        NSDictionary conditionAssignment = (NSDictionary)value();
        keyPath = (String)conditionAssignment.valueForKey("nonNullKeyPath");
        resultKey = c.valueForKeyPath(keyPath) == null ? "falseValue" : "trueValue";
        result = conditionAssignment.objectForKey(resultKey);
        if (log.isDebugEnabled()) log.debug("ResultKey:  " + resultKey + " = " + result);
        return result;
    }
}
