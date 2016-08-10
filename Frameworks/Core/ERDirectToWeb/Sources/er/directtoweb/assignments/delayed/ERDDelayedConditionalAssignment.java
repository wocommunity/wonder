/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.delayed;

import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.assignments.ERDComputingAssignmentInterface;

/**
 * DelayedConditionalAssignment expects a value dictionary that contains the
 * following keys:
 *    - qualifierFormat (see EOQualifier for more info)
 *    - args: the arguments used by the qualifier format
 *    - trueValue: the value used if the condition returns true
 *    - falseValue: the value used if the condition returns false
 * To specify a null value for true and false values simply ommit the
 * corresponding key.
 * The condition is evaluated 
 * every time that the propertyKey is requested thus making the rule system
 * a lot more dynamic.
 */

public class ERDDelayedConditionalAssignment extends ERDDelayedAssignment implements ERDComputingAssignmentInterface  {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private final static Logger log = LoggerFactory.getLogger("er.directtoweb.rules.DelayedConditionalAssignment");

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedConditionalAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDelayedConditionalAssignment(EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDelayedConditionalAssignment(String key, Object value) { super(key,value); }

    public NSArray _dependentKeys;
    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon all of the qualifier keys from the formed
     * qualifier of the value of this assignment. This array of keys is 
     * used when constructing the significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        if (_dependentKeys==null) {
            NSDictionary conditionAssignment;
            try {
               conditionAssignment = (NSDictionary)value();
            } catch (ClassCastException e) {
                log.error("expected a NSDictionary object but received {}", value(), e);
                throw e;
            }
            String qualFormat =
                (String)conditionAssignment.objectForKey("qualifierFormat");
            NSArray args = (NSArray)conditionAssignment.objectForKey("args");
            log.debug("parsing {}", qualFormat);
            EOQualifier qualifier =
                EOQualifier.qualifierWithQualifierFormat(qualFormat, args);
            if (log.isDebugEnabled())
                log.debug("Qualifier keys: {}", qualifier.allQualifierKeys());
            _dependentKeys=qualifier.allQualifierKeys().allObjects();
        }
        return _dependentKeys;
    }

    /**
     * This method is called whenever the propertyKey is requested,
     * but the value in the cache is actually a rule.
     */
    @Override
    public Object fireNow(D2WContext c) {
        Object result = null;
        NSDictionary conditionAssignment = (NSDictionary)value();
        String qualFormat =
            (String)conditionAssignment.objectForKey("qualifierFormat");
        NSArray args = (NSArray)conditionAssignment.objectForKey("args");
        if (args != null && args.count() > 0) {
            // Need to resolve the args from the context.
            NSMutableArray argHolder = new NSMutableArray(args.count());
            for (Enumeration argEnumerator = args.objectEnumerator(); argEnumerator.hasMoreElements();) {
                Object arg = argEnumerator.nextElement();
                if (arg instanceof String && ((String)arg).length() > 1 && ((String)arg).charAt(0) == '^') {
                    Object value = c.valueForKeyPath(((String)arg).substring(1, ((String)arg).length()));
                    if (value == null)
                        value = NSKeyValueCoding.NullValue;
                    argHolder.addObject(value);
                } else {
                    argHolder.addObject(arg);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Entity '{}'", c.entity().name());
            log.debug("Object {}", c.valueForKey("object"));
            log.debug("qualifierFormat {}", qualFormat);
            log.debug("args {}", args);
        }
        EOQualifier qualifier = EOQualifier.qualifierWithQualifierFormat(qualFormat, args);
        if (log.isDebugEnabled()) {
            log.debug("Qualifier keys: {}", qualifier.allQualifierKeys());
            log.debug("Qualifier: {}", qualifier);
            log.debug("DelayedConditonalQualifier: {}", qualifier);
        }
        if (qualifier.evaluateWithObject(c)) {
            result = conditionAssignment.objectForKey("trueValue");
            log.debug("trueValue = {}", result);
        } else {
            result = conditionAssignment.objectForKey("falseValue");
            log.debug("falseValue = {}", result);
        }
        return result;
    }
}
