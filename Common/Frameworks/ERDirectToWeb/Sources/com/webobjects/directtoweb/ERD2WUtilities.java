/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package com.webobjects.directtoweb;

import com.webobjects.foundation.*;

// This is needed because pageFinalized is a protected method.
public class ERD2WUtilities {

    public static void finalizeContext(D2WContext context) {
        if (context != null)
            context.pageFinalized();
    }

    /*
    public static void resetContextCache(D2WContext context) {
        if (context != null)
            context._localValues.clear();
    }
     */
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
        int i = keyPath.indexOf(".");
        if (i == -1) {
            result = c.valueForKeyNoInference(keyPath);
        } else {
            String first = keyPath.substring(0, i);
            String second = keyPath.substring(i + 1);
            result = c.valueForKeyNoInference(first);
            if (result != null) {
                // Optimized for two paths deep
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
            }
        }
        return result;
    }
}
