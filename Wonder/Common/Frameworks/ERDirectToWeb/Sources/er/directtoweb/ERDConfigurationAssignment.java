/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * This assignment calculates default page configuration
 * names for the current entity in the context. This can
 * be used 
 */
// RENAMEME or MOVEME, either ERDDefaultConfigurationAssignment or ERDDefaultsAssignment
public class ERDConfigurationAssignment extends ERDAssignment {

    /** holds the array of keys this assignment depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] {"object.entityName", "entity.name"});

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDConfigurationAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDConfigurationAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDConfigurationAssignment (String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link ERDComputingAssignmentInterface}. This
     * assignment depends upon the context keys: "entity.name" and 
     * "object.entityName". This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    /**
     * Calculates the entity name for a given context.
     * @param c a D2W context
     * @return the current entity name for that context.
     */
    // MOVEME: ERD2WContextUtilities?
    public String entityNameForContext(D2WContext c) {
        return c.valueForKey("object") != null ?
                             ((EOEnterpriseObject)c.valueForKey("object")).entityName() :
                             c.entity().name();
    }

    /**
     * Generates a default confirm page configuration
     * based on the current entity name. Default format
     * is 'Confirm' + entity name.
     * @param c current D2W context
     * @return default confirm page configuration name
     */
    public Object confirmConfigurationNameForEntity(D2WContext c) {
        return "Confirm" + entityNameForContext(c); 
    }

    /**
     * Generates a default create page configuration
     * based on the current entity name. Default format
     * is 'Create' + entity name.
     * @param c current D2W context
     * @return default create page configuration name
     */
    public Object createConfigurationNameForEntity(D2WContext c) {
        return "Create" +  entityNameForContext(c); 
    }
 
    /**
     * Generates a default edit page configuration
     * based on the current entity name. Default format
     * is 'Edit' + entity name.
     * @param c current D2W context
     * @return default edit page configuration name
     */       
    public Object editConfigurationNameForEntity(D2WContext c) {
        return "Edit" + entityNameForContext(c); 
    }

    /**
     * Generates a default inspect page configuration
     * based on the current entity name. Default format
     * is 'Inspect' + entity name.
     * @param c current D2W context
     * @return default inspect page configuration name
     */        
    public Object inspectConfigurationNameForEntity(D2WContext c) {
        return "Inspect" + entityNameForContext(c); 
    }
    
    /**
     * Generates a default list page configuration
     * based on the current entity name. Default format
     * is 'List' + entity name.
     * @param c current D2W context
     * @return default list page configuration name
     */
    public Object listConfigurationNameForEntity(D2WContext c) {
        return "List" +  entityNameForContext(c); 
    }
}
