/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* DelayedConditionalAssignment.java created by jd on Wed 18-Apr-2001 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import java.util.*;
import org.apache.log4j.*;
import er.extensions.*;

/**
 * DelayedConditionalAssignment expects a value dictionary that contains the
 * following keys:
 *    - qualifierFormat (see EOQualifier for more info)
 *    - args: the arguments used by the qualifier format
 *    - trueValue: the value used if the condition returns true
 *    - falseValue: the value used if the condition returns false
 * To specify a null value for true and false values simply ommit the
 * corresponding key.
 * The condition is evaluated 
 * every time that the propertyKey is requested thus making the rule system
 * a lot more dynamic.
 */

public class ERDDelayedConditionalAssignment extends ERDDelayedAssignment implements ERDComputingAssignmentInterface  {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedConditionalAssignment(eokeyvalueunarchiver);
    }
    
    ///////////////////////////  log4j category  ///////////////////////////
    public final static Category cat = Category.getInstance("er.directtoweb.rules.DelayedConditionalAssignment");
    ////////////////////////////////////////////////////////////////////////

    public ERDDelayedConditionalAssignment(EOKeyValueUnarchiver u) { super(u); }
    public ERDDelayedConditionalAssignment(String key, Object value) { super(key,value); }

    public NSArray _dependentKeys;
    public NSArray dependentKeys(String keyPath) {
        if (_dependentKeys==null) {
            NSDictionary conditionAssignment = (NSDictionary)this.value(null);
            String qualFormat =
                (String)conditionAssignment.objectForKey("qualifierFormat");
            NSArray args = (NSArray)conditionAssignment.objectForKey("args");
            if (cat.isDebugEnabled()) cat.debug("parsing "+qualFormat);
            EOQualifier qualifier =
                EOQualifier.qualifierWithQualifierFormat(qualFormat, args);
            if (cat.isDebugEnabled())
                System.err.println("Qualifier keys: " + qualifier.allQualifierKeys());
            _dependentKeys=qualifier.allQualifierKeys().allObjects();
        }
        return _dependentKeys;
    }

    public Object fire(D2WContext c) {
        return super.fire(c);
    }


    

    /**
     * This method is called whenever the propertyKey is requested,
     * but the value in the cache is actually a rule.
     */
    public Object fireNow(D2WContext c) {
        Object result = null;
        NSDictionary conditionAssignment = (NSDictionary)this.value(c);
        String qualFormat =
            (String)conditionAssignment.objectForKey("qualifierFormat");
        NSArray args = (NSArray)conditionAssignment.objectForKey("args");
        if (cat.isDebugEnabled()) {
            cat.debug("Entity: " + c.entity().name());
            cat.debug("Object " + c.valueForKey("object"));
        }
        EOQualifier qualifier = 
           EOQualifier.qualifierWithQualifierFormat(qualFormat, args);
        if (cat.isDebugEnabled()) {
            System.err.println("Qualifier keys: " + qualifier.allQualifierKeys());
        }
        if (cat.isDebugEnabled())
            cat.debug("DelayedConditonalQualifier: " + qualifier);
        if (qualifier.evaluateWithObject(c)) {
            result = conditionAssignment.objectForKey("trueValue");
            cat.debug("   trueValue = " + result);
        } else {
            result = conditionAssignment.objectForKey("falseValue");
            cat.debug("   falseValue = " + result);
        }
        return result;
    }
}
