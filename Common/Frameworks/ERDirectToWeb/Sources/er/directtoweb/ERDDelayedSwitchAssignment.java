/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

public class ERDDelayedSwitchAssignment extends ERDDelayedAssignment implements ERDComputingAssignmentInterface  {

    /** logging support */
    public final static Logger log = Logger.getLogger("er.directtoweb.rules.ERDDelayedSwitchAssignment");


    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedSwitchAssignment(eokeyvalueunarchiver);
    }


    public ERDDelayedSwitchAssignment(EOKeyValueUnarchiver u) { super(u); }


    public ERDDelayedSwitchAssignment(String key, Object value) { super(key,value); }

    public NSArray _dependentKeys;

    public NSArray dependentKeys(String keyPath) {
        if (_dependentKeys==null) {
            NSDictionary conditionAssignment = (NSDictionary)this.value();
            String qualFormat =
                (String)conditionAssignment.objectForKey("qualifierFormat");
            NSArray args = (NSArray)conditionAssignment.objectForKey("args");
            if (log.isDebugEnabled()) log.debug("parsing "+qualFormat);
            EOQualifier qualifier =
                EOQualifier.qualifierWithQualifierFormat(qualFormat, args);
            if (log.isDebugEnabled())
                log.debug("Qualifier keys: " + qualifier.allQualifierKeys());
            _dependentKeys=qualifier.allQualifierKeys().allObjects();
        }
        return _dependentKeys;
    }


    public Object fireNow(D2WContext c) {
        Object result = null;
        NSDictionary conditionAssignment = (NSDictionary)this.value();
        String qualFormat =
            (String)conditionAssignment.objectForKey("qualifierFormat");
        NSDictionary switchDictionary = (NSDictionary)conditionAssignment.objectForKey("switch");
        NSArray args = (NSArray)conditionAssignment.objectForKey("args");
        if (log.isDebugEnabled()) {
            log.debug("Entity: " + c.entity().name());
            log.debug("Object " + c.valueForKey("object"));
            log.debug("qualifierFormat "+qualFormat);
            log.debug("args "+args);
            log.debug("switchDictionary " + switchDictionary);
        }
        for(Enumeration e = switchDictionary.allKeys().objectEnumerator();
            e.hasMoreElements();){
            String switchKey = (String)e.nextElement();
            String completedQualFormat =
                NSArray.componentsSeparatedByString( qualFormat, "@@").componentsJoinedByString(switchKey);
            EOQualifier qualifier =
                EOQualifier.qualifierWithQualifierFormat(completedQualFormat, args);
            if (log.isDebugEnabled()) {
                System.err.println("Qualifier keys: " + qualifier.allQualifierKeys());
                System.err.println("Qualifier : " + qualifier);
            }
            if (log.isDebugEnabled())
                log.debug("DelayedConditonalQualifier: " + qualifier);
            if (qualifier.evaluateWithObject(c)) {
                result = switchDictionary.valueForKey(switchKey);
                log.debug("result = " + result);
                break;
            }            
        }
        if(result == null){
            result = switchDictionary.valueForKey("defaultValueForAssignement");
        }
        return result;
    }
}
