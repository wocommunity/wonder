/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.KeyValuePath;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.defaults.ERDDefaultModelAssignment;


/**
 * This class works around the following problem:
 * <p>
 * An entity A has a relationship b to an entity B, which 
 * has a subentity B1. B1 has a relationship k, which B does 
 * not have. If in an inspect page for entity A, you use b.k 
 * as a display key, then the D2W rules which are based on 
 * d2wContext.relationship will not fire properly. This is 
 * because relationship is null, instead of containing 
 * &lt;EORelationship entity=B1 name=k&gt;. The reason D2W does not 
 * find it is that it uses the Model to find out the EORelationship
 * and starts from A. Following the relationship b, gives a B, and 
 * asking B for a relationship named k returns null and you lose.
 * @deprecated use {@link er.directtoweb.assignments.defaults.ERDDefaultModelAssignment}
 */
//	Note that these assignments require that the object is pushed into the context.  Look
//	on some of the ERInspectPage setObject methods we push the object into the context.
// MOVEME: Combind with SmartAttribute
@Deprecated
public class ERDSmartRelationshipAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** holds the array of dependent keys */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "object.entityName", "propertyKey"  });

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
     // ENHANCEME: Only need one of these ever created */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDSmartRelationshipAssignment.class, ERDDefaultModelAssignment.class);
        return new ERDSmartRelationshipAssignment(eokeyvalueunarchiver);
    }
    
    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context keys: "propertyKey" and 
     * "object.entityName". This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDSmartRelationshipAssignment(EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDSmartRelationshipAssignment(String key, Object value) { super(key,value); }

    /**
     * Resolves the {@link EORelationship} in a smarter manner using
     * the current object from the context as well as the propertyKey
     * to determine the current relationship. Works even with inheritance.
     * @param c current D2W context
     * @return relationship for the current propertyKey object combination.
     */
    public Object smartRelationship(D2WContext c) {
        Object result = null;
        Object rawObject=c.valueForKey("object");
        String propertyKey=c.propertyKey();
        if (rawObject!=null && rawObject instanceof EOEnterpriseObject && propertyKey != null) {
            EOEnterpriseObject object=(EOEnterpriseObject)rawObject;
            EOEnterpriseObject lastEO=object;
            if (propertyKey.indexOf(".")!=-1) {
                String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(propertyKey);
                Object rawLastEO=object.valueForKeyPath(partialKeyPath);
                lastEO=rawLastEO instanceof EOEnterpriseObject ? (EOEnterpriseObject)rawLastEO : null;
            }
            if (lastEO!=null) {
                EOEntity entity=EOModelGroup.defaultGroup().entityNamed(lastEO.entityName());
                String lastKey=KeyValuePath.lastPropertyKeyInKeyPath(propertyKey);
                result=entity.relationshipNamed(lastKey);
            }
        }
        if (result==null && propertyKey!=null) {
	    // working around D2W bug
            result=c.relationship();
        }
        return result;
    }
}
