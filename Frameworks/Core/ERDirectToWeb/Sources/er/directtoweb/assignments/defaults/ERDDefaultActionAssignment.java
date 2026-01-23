/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.defaults;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.assignments.ERDAssignment;

/**
 * This assignment calculates default actions for the current page
 */
public class ERDDefaultActionAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** holds the array of keys this assignment depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] {"isEntityPrintable", "readOnly", "isEntityInspectable", "isEntityEditable", "isEntityDeletable"});

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultActionAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDefaultActionAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDefaultActionAssignment (String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context keys: "entity.name" and 
     * "object.entityName". This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    /**
     * Calculates the actions names for a given context.
     * @param c a D2W context
     * @return the array of array of action names for that context.
     */
    public NSDictionary defaultActions(D2WContext c) {
        NSDictionary actions = new NSDictionary(new Object[] {defaultLeftActions(c), defaultRightActions(c)}, new Object [] {"left", "right"});
        log.debug(actions.toString());
        return actions;
    }

    /**
     * Calculates the default left actions names for a given context.
     * The array is set according to whether the entity is editable, inspectable and printable.
     * @param c a D2W context
     * @return array of action names for that context.
     */
    public NSArray defaultLeftActions(D2WContext c) {
        NSMutableArray actions = new NSMutableArray();
        if(booleanContextValueForKey(c, "isEntityEditable", false) || booleanContextValueForKey(c, "readOnly", true))
            actions.addObject("editAction");
        if(booleanContextValueForKey(c, "isEntityInspectable", false))
            actions.addObject("inspectAction");
        if(booleanContextValueForKey(c, "isEntityPrintable", false))
            actions.addObject("printAction");
        return actions;
    }

    /**
     * Calculates the default right actions names for a given context.
     * The array is set according to whether the entity is deletable.
     * @param c a D2W context
     * @return array of action names for that context.
     */
    public NSArray defaultRightActions(D2WContext c) {
        NSMutableArray actions = new NSMutableArray();
        if(booleanContextValueForKey(c, "isEntityDeletable", false))
            actions.addObject("deleteAction");
        return actions;
    }
}
