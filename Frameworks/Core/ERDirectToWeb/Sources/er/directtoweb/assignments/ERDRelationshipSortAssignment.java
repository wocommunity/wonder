/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.defaults.ERDDefaultModelAssignment;


/**
 * Relationship sort assignment that works with the new caching 
 * scheme. Should only ever need one of these assignments.
 * @deprecated use {@link er.directtoweb.assignments.defaults.ERDDefaultModelAssignment}
 */
@Deprecated
public class ERDRelationshipSortAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;


    /** holds the array of dependent keys */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "keyWhenRelationship", "propertyKey"  });

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    // CHECKME: Pretty sure we only need one of these ever created.
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDRelationshipSortAssignment.class, ERDDefaultModelAssignment.class);
        return new ERDRelationshipSortAssignment(eokeyvalueunarchiver);
    }

    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDRelationshipSortAssignment (String key, Object value) { super(key,value); }

    /** 
     * Public constructor
     * @param unarchiver key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDRelationshipSortAssignment(EOKeyValueUnarchiver unarchiver) { super(unarchiver); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context keys: "propertyKey" and 
     * "keyWhenRelationship". This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    /**
     * Called when firing this assignment with the key-path:
     * <b>sortKeyForList</b>.
     * @return the current propertyKey + "." + the current value for
     *		keyWhenRelationship. 
     */
    public Object sortKeyForList(D2WContext context) {
        return (String)context.valueForKey("propertyKey")+"."+(String)context.valueForKey("keyWhenRelationship");
    }
}
