/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* DelayedBooleanAssignment.java created by max on Fri 04-May-2001 */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import org.apache.log4j.*;
import er.extensions.*;

///////////////////////////////////////////////////////////////////////////////
// Stepchild of DelayedConditionalAssignment
//	Takes three entries in a dictionary format.
//	conditionKey - keyPath of the condition fired off of the d2wContext.
//	trueValue - the value used if the condition returns true
//	falseValue - the value used if the condition returns false
///////////////////////////////////////////////////////////////////////////////
public class ERDDelayedBooleanAssignment extends ERDDelayedAssignment implements ERDComputingAssignmentInterface {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedKeyValueAssignment(eokeyvalueunarchiver);
    }
    

    //////////////////////////////////////////////  log4j category  /////////////////////////////////////////////
    public static final Category cat = Category.getInstance("er.directtoweb.rules.DelayedBooleanAssignment");

    public ERDDelayedBooleanAssignment(EOKeyValueUnarchiver u) { super(u); }
    public ERDDelayedBooleanAssignment(String key, Object value) { super(key,value); }

    public NSArray _dependentKeys;
    public NSArray dependentKeys(String keyPath) {
        if (_dependentKeys==null) {
            NSDictionary booleanConditions = (NSDictionary)value(null);
            _dependentKeys=new NSArray(booleanConditions.objectForKey("conditionKey"));
        }
        return _dependentKeys;
    }

    public Object fireNow(D2WContext c) {
        NSDictionary booleanConditions = (NSDictionary)value(c);
        if (cat.isDebugEnabled())
            cat.debug("Resolving delayed fire for boolean conditions: " + booleanConditions);
        return ERXUtilities.booleanValue(c.valueForKeyPath((String)booleanConditions.objectForKey("conditionKey"))) ?
            booleanConditions.objectForKey("trueValue") : booleanConditions.objectForKey("falseValue");

    }
}
