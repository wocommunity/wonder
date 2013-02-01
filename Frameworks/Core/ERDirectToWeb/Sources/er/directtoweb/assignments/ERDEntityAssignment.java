/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.defaults.ERDDefaultModelAssignment;
import er.extensions.eof.ERXEOAccessUtilities;

/**
 * @deprecated use a {@link er.directtoweb.assignments.ERDKeyValueAssignment} or a {@link er.directtoweb.assignments.defaults.ERDDefaultModelAssignment} with key entityForPageConfiguration instead
 */
@Deprecated
public class ERDEntityAssignment extends Assignment implements ERDComputingAssignmentInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** holds the array of keys this assignment depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new Object[] {"pageConfiguration", "controllerName"});

    /** logging support */
    public final static Logger log = Logger.getLogger(ERDEntityAssignment.class);

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    // ENHANCEME: Could maintain a weak reference of all the values().
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDEntityAssignment.class, ERDDefaultModelAssignment.class);
        return new ERDEntityAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDEntityAssignment(EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDEntityAssignment(String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context key: "pageConfiguration". This key 
     * is used when constructing the significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    @Override
    public Object fire(D2WContext c) {
        Object result = null;
        Object value = value();
        EOEditingContext ec = (EOEditingContext)c.valueForKey("session.defaultEditingContext");

        log.info("fire with value: " + value);
        if(log.isDebugEnabled()) {
            log.debug("fire with value: " + value);
        }
        // is it an entity name?
        if(value instanceof String) {
            result = ERXEOAccessUtilities.entityMatchingString(ec, (String)value);
        }

        // probably a keypath?
        if((value instanceof String) && ((String)value).length() > 0) {
            result = ERXEOAccessUtilities.entityMatchingString(ec, (String)c.valueForKey((String)value));
        }

        // try the controllerName, then the pageConfiguration, if that does not match, give up
        if(result == null) {
            result = c.valueForKey("entityForControllerName");
        }
        
        if(result == null) {
            result = c.valueForKey("entityForPageConfiguration");
        }
        return result;
    }
}
