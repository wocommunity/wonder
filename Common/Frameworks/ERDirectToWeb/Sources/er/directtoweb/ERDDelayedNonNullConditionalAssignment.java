/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.directtoweb.*;
import org.apache.log4j.Category;

public class ERDDelayedNonNullConditionalAssignment extends ERDDelayedAssignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedNonNullConditionalAssignment(eokeyvalueunarchiver);
    }
    
    //////////////////////////////////////////////  log4j category  //////////////////////////////////////////
    public final static Category cat = Category.getInstance("er.directtoweb.rules.DelayedNonNullConditionalAssigment");

    public ERDDelayedNonNullConditionalAssignment(EOKeyValueUnarchiver u) { super(u); }
    public ERDDelayedNonNullConditionalAssignment(String key, Object value) { super(key,value); }

    public NSArray _dependentKeys;
    public NSArray dependentKeys(String keyPath) {
        if (_dependentKeys==null) {
            NSDictionary conditionAssignment = (NSDictionary)this.value(null);
            _dependentKeys=new NSArray(conditionAssignment.valueForKey("nonNullKeyPath"));
        }
        return _dependentKeys;
    }

    /**
     * This method is called whenever the propertyKey is requested,
     * but the value in the cache is actually a rule.
     */
    public Object fireNow(D2WContext c) {
        Object result = null;
        String keyPath;
        String resultKey;
        NSDictionary conditionAssignment = (NSDictionary)this.value(c);
        keyPath = (String)conditionAssignment.valueForKey("nonNullKeyPath");
        resultKey = c.valueForKeyPath(keyPath) == null ? "falseValue" : "trueValue";
        if (keyPath == null) {
            keyPath = (String)conditionAssignment.valueForKey("nullKeyPath");
            resultKey = c.valueForKeyPath(keyPath) == null ? "trueValue" : "falseValue";
        }
        result = conditionAssignment.objectForKey(resultKey);
        if (cat.isDebugEnabled()) cat.debug("   " + resultKey + " = " + result);
        return result;
    }
}
