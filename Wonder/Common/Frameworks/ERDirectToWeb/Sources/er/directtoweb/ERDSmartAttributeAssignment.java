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
import com.webobjects.directtoweb.*;

//	Note that these assignments require that the object is pushed into the context.  Look
//	on some of the ERInspectPage setObject methods we push the object into the context.
public class ERDSmartAttributeAssignment extends ERDAssignment {

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDSmartAttributeAssignment(eokeyvalueunarchiver);
    }
    
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "object.entityName", "propertyKey"  });
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    /* This class works around the following problem:
An entity A has a relationship b to an entity B, which has a subentity B1. B1 has an attribute k, which B does not have. If in an inspect page for entity A, you use b.k as a display key, then the D2W rules which are based on d2wContext.attribute  will not fire properly. This is because attribute is null, instead of containing <EOAttribute entity=B1 name=k>. The reason D2W does not find it is that it uses the Model to find out the EOAttribute and starts from A. Following the relationship b, gives a B, and asking B for an attribute named k returns nil and you lose.
    */

    public ERDSmartAttributeAssignment(EOKeyValueUnarchiver u) { super(u); }
    public ERDSmartAttributeAssignment(String key, Object value) { super(key,value); }

    public Object smartAttribute(D2WContext c) {
        EOAttribute result=null;
        String propertyKey=c.propertyKey();
        Object rawObject=c.valueForKey("object");
        if (rawObject!=null && rawObject instanceof EOEnterpriseObject && propertyKey!=null) {
            EOEnterpriseObject object=(EOEnterpriseObject)rawObject;
            EOEnterpriseObject lastEO=object;
            if (lastEO!=null && propertyKey.indexOf(".")!=-1) {
                String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(propertyKey);
                Object rawLastEO=object.valueForKeyPath(partialKeyPath);
                lastEO=rawLastEO instanceof EOEnterpriseObject ? (EOEnterpriseObject)rawLastEO : null;
            }
            if (lastEO!=null) {
                EOEntity entity=EOModelGroup.defaultGroup().entityNamed(lastEO.entityName());
                String lastKey=KeyValuePath.lastPropertyKeyInKeyPath(propertyKey);
                result=entity.attributeNamed(lastKey);
            }
        }
        if (result==null) {
            // default to the basic attribute if the above didnt' work
            result=c.attribute(); 
        }
        return result;
    }    
}
