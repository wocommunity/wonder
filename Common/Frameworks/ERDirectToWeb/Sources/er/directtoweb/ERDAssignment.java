/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERAssignment.java created by max on Tue 10-Oct-2000 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import java.lang.reflect.*;
import java.util.*;
import org.apache.log4j.*;
import er.extensions.*;

public abstract class ERDAssignment extends Assignment implements ERDComputingAssignmentInterface {

    
    //////////////////////////////////////////  log4j category  ////////////////////////////////////////
    public final static Category cat = Category.getInstance("er.directtoweb.rules.ERDAssignment");

    public final static Class[] D2WContextClassArray = new Class[] { D2WContext.class };
    
    public ERDAssignment(EOKeyValueUnarchiver u) { super(u); }
    public ERDAssignment(String key, Object value) { super(key,value); }

    // there are basically two choices to lookup the method
    // use they keypath, in which case you can use the value of the rule to provide a parameter to your assignment method
    // use the value, which gives you the flexibility to have several methods for the same key path..
    public String keyForMethodLookup(D2WContext c) {
        return keyPath();
    }
    
    public Object fire(D2WContext c) {
        Object result = null;
        try {
            Method m = getClass().getMethod(keyForMethodLookup(c), D2WContextClassArray);
            result = m.invoke(this, new Object[] { c });
        } catch (InvocationTargetException e) {
            cat.error("InvocationTargetException occurred in ERAssignment: " + e.toString() + " keyForMethodLookup(): " + keyForMethodLookup(c) + " target exception: " + e.getTargetException()+ " assignment was "+this+"\n\n"+"Target exception backtrace: "+ERXUtilities.stackTrace(e.getTargetException()));
        } catch (Exception e) {
            cat.error("Exception occurred in ERAssignment of class: "+this.getClass().getName()+": " + e.toString() + " keyForMethodLookup(): " + keyForMethodLookup(c) + " assignment was "+this);
        }
        return result;
    }



}