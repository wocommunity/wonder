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
import org.apache.log4j.Category;
import er.extensions.ERXUtilities;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////////////////////////
// A little smarter than the average assignment.  This entity assignment will take the current
// pageConfiguration and try to find an entityName that matches.
//////////////////////////////////////////////////////////////////////////////////////////////
public class ERDEntityAssignment extends Assignment implements ERDComputingAssignmentInterface {

    /** holds the array of keys this assignment depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray("pageConfiguration");

    /** logging support */
    public final static Category cat = Category.getInstance("er.directtoweb.rules.ERDefaultEntityAssignment");

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

    protected NSArray entityNames = null;
    public Object fire(D2WContext c) {
        Object result = null;
        if (value() != null && value() instanceof String && ((String)value()).length() > 0) {
            result = ERXUtilities.caseInsensitiveEntityNamed(((String)value()).toLowerCase());
        }
        if (result == null && c.valueForKey("pageConfiguration") != null) {
            String lowerCasePageConfiguration = ((String)c.valueForKey("pageConfiguration")).toLowerCase();
            if (entityNames == null) {
                entityNames = (NSArray)((NSArray)ERXUtilities.entitiesForModelGroup(EOModelGroup.defaultGroup()).valueForKey("name")).valueForKey("toLowerCase");
            }
            NSMutableArray possibleEntities = new NSMutableArray();
            for (Enumeration e = entityNames.objectEnumerator(); e.hasMoreElements();) {
                String lowercaseEntityName = (String)e.nextElement();
                if (lowerCasePageConfiguration.indexOf(lowercaseEntityName) != -1)
                    possibleEntities.addObject(lowercaseEntityName);
            }
            if (possibleEntities.count() == 1) {
                result = ERXUtilities.caseInsensitiveEntityNamed((String)possibleEntities.lastObject());
            } else if (possibleEntities.count() > 1) {
                ERXUtilities.sortEOsUsingSingleKey(possibleEntities, "length");
                if (((String)possibleEntities.objectAtIndex(0)).length() == ((String)possibleEntities.objectAtIndex(1)).length())
                    cat.warn("Found multiple entities of the same length for pageConfiguration: " + c.valueForKey("pageConfiguration")
                             + " possible entities: " + possibleEntities);
                result = ERXUtilities.caseInsensitiveEntityNamed((String)possibleEntities.lastObject());
            }
            if (cat.isDebugEnabled())
                cat.debug("Found possible entities: " + possibleEntities + " for pageConfiguration: " + c.valueForKey("pageConfiguration")
                          + " result: " + result);
            return result;
        }
        return result;
    }
}
