/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.qualifiers;

import com.webobjects.directtoweb.BooleanQualifier;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;

/* HACK Alert
   we need to sublass one of the qualifier class of D2W in order for the LA code to work properly
*/
/**
 * Tests if an object is a kind of an entity.<br />
 * 
 */

public class ERDIsKindOfEntityQualifier extends BooleanQualifier {

    private String _entityName;
    private String _keyPath;

    public static Object decodeWithKeyValueUnarchiver (EOKeyValueUnarchiver u) {
        return new ERDIsKindOfEntityQualifier(u);
    }
    
    public ERDIsKindOfEntityQualifier (EOKeyValueUnarchiver u) {
        super(null);
        _entityName=(String)u.decodeObjectForKey("entityName");
        _keyPath=(String)u.decodeObjectForKey("keyPath");
    }

    public boolean evaluateWithObject(Object o) {
        // FIXME here -- this could be sth else..
        D2WContext c=(D2WContext)o;
        EOEntity e=(EOEntity)c.valueForKeyPath(_keyPath);
        return isKindOfEntity(e);
    }

    public boolean isKindOfEntity(EOEntity e) {
        return e.name().equals(_entityName) ? true : (e.parentEntity()!= null ? isKindOfEntity(e.parentEntity()) : false);
    }

    public String toString() { return _keyPath+" isKindOfEntity "+_entityName; }    
}
