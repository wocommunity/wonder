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
import er.extensions.*;
import java.util.Enumeration;

/**
 * A little smarter than the average assignment.  This entity assignment will take the current
 * pageConfiguration (or whatever the key in valu is) and try to find an entityName that matches.
 */
public class ERDEntityAssignment extends Assignment implements ERDComputingAssignmentInterface {

    /** holds the array of keys this assignment depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new Object[] {"pageConfiguration", "controllerName"});

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger("er.directtoweb.rules.ERDefaultEntityAssignment");

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
     * Implementation of the {@link ERDComputingAssignmentInterface}. This
     * assignment depends upon the context key: "pageConfiguration". This key 
     * is used when constructing the significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    // a fake entity that can be used for tasks such as error/confirm..
    private EOEntity _dummyEntity;
    public EOEntity dummyEntity() {
        if (_dummyEntity==null) {
            _dummyEntity=new EOEntity();
            _dummyEntity.setName("*all*");
        }
        return _dummyEntity;
    }

    protected boolean isTaskWithoutEntity(String task) {
        return ("queryAll".equals(task) || "confirm".equals(task) || "error".equals(task));
    }
    
    protected Object entityForKey(D2WContext c, String key) {
        Object result = null;
        if(key != null) {
            result = entityForName((String)c.valueForKey(key));
        }
        if(result == null && isTaskWithoutEntity(c.task())) {
            result = dummyEntity();
        }
        return result;
    }
    
    protected Object entityForName(String name) {
        Object result = null;
        if(name != null) {
            String lowerCaseName = name.toLowerCase();
            if (entityNames == null) {
                entityNames = (NSArray)((NSArray)ERXUtilities.entitiesForModelGroup(EOModelGroup.defaultGroup()).valueForKey("name")).valueForKey("toLowerCase");
            }
            NSMutableArray possibleEntities = new NSMutableArray();
            for (Enumeration e = entityNames.objectEnumerator(); e.hasMoreElements();) {
                String lowercaseEntityName = (String)e.nextElement();
                if (lowerCaseName.indexOf(lowercaseEntityName) != -1)
                    possibleEntities.addObject(lowercaseEntityName);
            }
            if (possibleEntities.count() == 1) {
                result = ERXUtilities.caseInsensitiveEntityNamed((String)possibleEntities.lastObject());
            } else if (possibleEntities.count() > 1) {
                ERXArrayUtilities.sortedArraySortedWithKey(possibleEntities, "length");
                if (((String)possibleEntities.objectAtIndex(0)).length() == ((String)possibleEntities.objectAtIndex(1)).length())
                    log.warn("Found multiple entities of the same length for configuration: " + name
                             + " possible entities: " + possibleEntities);
                result = ERXUtilities.caseInsensitiveEntityNamed((String)possibleEntities.lastObject());
            }
            if (log.isDebugEnabled())
                log.debug("Found possible entities: " + possibleEntities + " for configuration: " + name
                          + " result: " + result);
        }
        return result;
    }

    public Object entityForControllerName(D2WContext c) {
        return entityForKey(c, "controllerName");
    }

    public Object entityForPageConfiguration(D2WContext c) {
        return entityForKey(c, "pageConfiguration");
    }
    
    protected NSArray entityNames = null;
    public Object fire(D2WContext c) {
        Object result = null;
        // is it an entity name?
        if (value() != null && value() instanceof String && ((String)value()).length() > 0) {
            result = ERXUtilities.caseInsensitiveEntityNamed(((String)value()).toLowerCase());
        }
        // maybe it is a key? get the entity name from there.
        if(result == null && value() != null && value() instanceof String) {
            result = entityForName((String)value());
        }
        // try the pageConfiguration, if that does not match, give up
        if(result == null) {
            result = entityForKey(c, "pageConfiguration");
        }
        return result;
    }
}
