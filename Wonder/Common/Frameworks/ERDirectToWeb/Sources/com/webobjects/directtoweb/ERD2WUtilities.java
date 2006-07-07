/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package com.webobjects.directtoweb;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.*;

// This is needed because pageFinalized is a protected method.
public class ERD2WUtilities {

    private static Logger log = Logger.getLogger(ERD2WUtilities.class);

    public static void finalizeContext(D2WContext context) {
        if (context != null)
            context.pageFinalized();
    }

    public static void resetContextCache(D2WContext context) {
        if (context != null)
            context._localValues.clear();
    }
    
    public static boolean assignmentsAreEqual(Assignment a1, Assignment a2) {
        boolean areEqual = false;
        if (a1.getClass().equals(a2.getClass()) && a1.keyPath() != null && a2.keyPath() != null && a1.value() != null && a2.value() != null) {
            areEqual = a1.keyPath().equals(a2.keyPath()) && a1.value().equals(a2.value());
        }
        return areEqual;
    }

    // This prevents the dreaded KeyValueCoding null object exception, for say key paths: object.entityName
    // Should just return null instead of throwing.
    public static Object contextValueForKeyNoInferenceNoException(D2WContext c, String keyPath) {
        Object result = null;
        String oriKeyPath = keyPath;
        int i = keyPath.indexOf(".");
        if (i == -1) {
            result = c.valueForKeyNoInference(keyPath);
        } else {
            String first = keyPath.substring(0, i);
            String second = keyPath.substring(i + 1);
            result = c.valueForKeyNoInference(first);
            if (result != null) {
                // Optimized for two paths deep
                
                try {
                    if (second.indexOf(".") == -1) {
                        result = NSKeyValueCoding.Utility.valueForKey(result, second);
                    } else {
                        NSArray parts = NSArray.componentsSeparatedByString(second, ".");
                        for (int j = 0; j < parts.count(); j++) {
                            String part = (String)parts.objectAtIndex(j);
                            result = NSKeyValueCoding.Utility.valueForKey(result, part);
                            if (result == null)
                                break;
                        }
                    }
                } catch (NSKeyValueCoding.UnknownKeyException e) {
                    log.warn("keyPath "+keyPath+" is not available for context "+c);
                    return null;
                }
            }
        }
        return result;
    }

    /** 
     * @deprecated use ERXEOControlUtilities.editableInstanceOfObject(EOEnterpriseObject,boolean)
     */
    public static EOEnterpriseObject localInstanceFromObjectWithD2WContext(EOEnterpriseObject eo,
                                                                           D2WContext d2wContext) {
    	boolean createNestedContext = ERXValueUtilities.booleanValue(d2wContext.valueForKey("useNestedEditingContext"));
    	return ERXEOControlUtilities.editableInstanceOfObject(eo,createNestedContext);
    }
}
