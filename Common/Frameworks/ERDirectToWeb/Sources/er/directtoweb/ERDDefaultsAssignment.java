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
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import er.extensions.ERXLogger;

// now that we use reflection to fire methods, there's no reason to have a lot a assignment classes
public class ERDDefaultsAssignment extends ERDAssignment {

    /** logging support */
    public final static ERXLogger log = ERXLogger.getERXLogger("er.directtoweb.ERDDefaultsAssignment");

    /** holds the array of keys this assignmnet depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray("smartAttribute");

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
     // ENHANCEME: Only ever need one of these assignments.
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultsAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDefaultsAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDefaultsAssignment (String key, Object value) { super(key,value); }
    
    /**
     * Implementation of the {@link ERDComputingAssignmentInterface}. This
     * assignment depends upon the context key: "smartAttribute". This array 
     * of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }
    
    public String keyForMethodLookup(D2WContext c) { return (String)value(); }

    public int attributeWidthAsInt(D2WContext c) {
        EOAttribute a = (EOAttribute)c.valueForKey("smartAttribute");
        return a!=null ? a.width() : 0;
    }

    public int smartDefaultAttributeWidthAsInt(D2WContext c) {
        int i=attributeWidthAsInt(c);
        return i<50 ? ( i==0 ? 20 : i ) : 50;        
    }

    public Object smartDefaultAttributeWidth(D2WContext c) {
        return String.valueOf(smartDefaultAttributeWidthAsInt(c));
    }

    public Object smartDefaultRows(D2WContext c) {
        int i = attributeWidthAsInt(c);
        int j = smartDefaultAttributeWidthAsInt(c);
        int k = j == 0 ? i : (int)((double)(i / j) + 0.5D);
        if(k > 8) k = 8;
        return String.valueOf(k);
    }
}
