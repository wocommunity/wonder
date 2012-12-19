/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.defaults;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.ERDAssignment;
import er.directtoweb.assignments.ERDLocalizableAssignmentInterface;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Beautify the propertyKey name in a better way.<br />
 * @deprecated use {@link er.directtoweb.assignments.defaults.ERDDefaultDisplayNameAssignment}
 */
@Deprecated
public class ERDDefaultPropertyNameAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    static final Logger log = Logger.getLogger(ERDDefaultPropertyNameAssignment.class);

    /** holds the array of keys this assignment depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray("propertyKey");

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDDefaultPropertyNameAssignment.class, ERDDefaultDisplayNameAssignment.class);
        return new ERDDefaultPropertyNameAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */    
    public ERDDefaultPropertyNameAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDefaultPropertyNameAssignment (String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context key: "propertyKey". This array 
     * of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        return _DEPENDENT_KEYS;
    }

    // Default names
    public Object displayNameForProperty(D2WContext c) {
        String value = ERXStringUtilities.displayNameForKey((String)c.valueForKey("propertyKey"));
        return localizedValueForKeyWithDefaultInContext(value, c);
    }
}
