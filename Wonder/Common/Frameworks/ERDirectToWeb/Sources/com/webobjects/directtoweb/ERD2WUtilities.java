/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package com.webobjects.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import er.extensions.*;

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
    public static EOEnterpriseObject localInstanceFromObjectWithD2WContext(EOEnterpriseObject eo, D2WContext d2wContext) {
        if (eo.editingContext() == null) {
            throw new NullPointerException("localInstanceFromObjectWithD2WContext: the argument eo must have a valid EOEditingContext: eo.editingContext() != null");
        }
        EOEnterpriseObject localObject = eo;
        if (ERXExtensions.isNewObject(eo)) {
            //do nothing, a newObject cannot be faulted into another EC!
        } else if ("edit".equals(d2wContext.valueForKey("task"))) {
            //create a new nested EC and fault the object into it
            EOEditingContext ec = ERXEC.newEditingContext(eo.editingContext());
            //FIXME: we cannot use childEc's because if the EO is not saved to the DB
            //then the EO is not displayed as EO if one uses for example a ERD2WEditToOneRelationship
            //component: the new EO will not be displayed in the list
            //EOEditingContext ec = ERXEC.newEditingContext();
            ec.setSharedEditingContext(null);
            localObject = EOUtilities.localInstanceOfObject(ec, eo);
        } else {
            //create a new Peer EC and fault the object into it
            //this is done regardeless if the object's editingContext is
            //a sharedec or not.
            EOEditingContext ec = ERXEC.newEditingContext();
            ec.setSharedEditingContext(null);
            localObject = EOUtilities.localInstanceOfObject(ec, eo);
        }
        return localObject;
    }

    
}
