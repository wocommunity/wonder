/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.delayed.ERDDelayedAssignment;
import er.directtoweb.assignments.delayed.ERDDelayedObjectCreationAssignment;

/**
 * Assignment used to create objects on the fly. You use this by
 * specifing the class name as a string, ie "foo.bar.MyClass". This
 * will create an instance of the MyClass object.
 * @deprecated use {@link er.directtoweb.assignments.delayed.ERDDelayedObjectCreationAssignment}
 */
@Deprecated
public class ERDInstanceCreationAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public final static Logger log = Logger.getLogger(ERDDelayedAssignment.class);
    
    /** holds the array of keys this assignment depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray();

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDInstanceCreationAssignment.class, ERDDelayedObjectCreationAssignment.class);
        return new ERDInstanceCreationAssignment(eokeyvalueunarchiver);
    }

    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */    
    public ERDInstanceCreationAssignment (EOKeyValueUnarchiver u) { 
        super(u); 
    }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDInstanceCreationAssignment (String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context key: "entity.name". This array 
     * of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    @Override
    public Object fireNow(D2WContext c) {
        Object o = null;
        Object value = value();
        String className;
        if(value instanceof String ) {
            className = (String)value();
            try {
                o = Class.forName(className).newInstance();
            } catch(Exception ex) {
                log.error("Can't create instance: "+  className, ex);
            }
        } else {
            log.error("Value not a class name: " + value);
        }
        return o;
    }
}
