/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package com.webobjects.directtoweb;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

import er.extensions.*;

// This is needed because pageFinalized is a protected method.
public class ERD2WUtilities {

    private static ERXLogger log = ERXLogger.getERXLogger(ERD2WUtilities.class);

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

    /** This method faults the provied <code>EOEnterpriseObject</code> into a new <code>EOEditingContext</code>
    * based on the current state:<br>
    * 1. the <code>EOEnterpriseObject</code> is new: simply returns the <code>EOEnterpriseObject</code>,
    * a new <code>EOEnterpriseObject</code> cannot be faulted into another <code>EOEditingContext</code><br>
    *
    * 2. if the d2wContext's task is 'edit' it creates a new -nested- <code>EOEditingContext</code> and
    * faults the <code>EOEnterpriseObject</code> into it. Calls <code>setSharedEditingContext(false)</code> on
    * the ec to ensure that this also works if one uses EOSharedEditingContexts.
    * 
    * 3. otherwise it creates a new PeerEditingContext and faults the <code>EOEnterpriseObject</code> into it.
    * Calls <code>setSharedEditingContext(false)</code> on the ec to ensure that this also works if
    * one uses EOSharedEditingContexts.
    *
    * This method should be called whenever on returns a new Page for a D2W action. It should simply behave like
    * one assumes and not like the WO implementation: no nested EC's, no SharedEditingContext support.
    *
    * @param eo The <code>EOEnterpriseObject</code> that should be faulted. If the current page is
    * a List page then it would mostly be current list object. If the current page is a Edit page then
    * it would be object() and NOT the relationship object or whatever. object() because its the main object
    * that needs to be faulted, then one must not care about relationships and so on.
    * 
    * @param d2wContext the current d2wContext
    *
    * @return
    */
    public static EOEnterpriseObject localInstanceFromObjectWithD2WContext(EOEnterpriseObject eo,
                                                                           D2WContext d2wContext) {
        EOEditingContext ec = eo.editingContext();
        if(ec == null) throw new IllegalStateException("EC can't be null in localinstance");
        EOEnterpriseObject localObject = eo;
        if(ERXProperties.webObjectsVersionAsDouble() < 5.21d && ERXExtensions.isNewObject(localObject)) {
            // do nothing as we can't localInstance anything here
        } else {
            boolean nest = d2wContext != null && ERXValueUtilities.booleanValue(d2wContext.valueForKey("useNestedEditingContext"));
            EOEditingContext newEc = ERXEC.newEditingContext(nest ? ec : ec.parentObjectStore());
            if(ec instanceof EOSharedEditingContext || ec.sharedEditingContext() == null) {
                newEc.setSharedEditingContext(null);
            }
            ec.lock();
            newEc.lock();
            try {
                localObject = EOUtilities.localInstanceOfObject(newEc, eo);
                localObject.willRead();
            } finally {
                ec.unlock();
                newEc.unlock();
            }
        }

        return localObject;
    }

    
}
