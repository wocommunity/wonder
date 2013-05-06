//
//  ERDSortedManyToManyAssignment.java
//  ERDirectToWeb
//
//  Created by Patrice Gautier on Fri Sep 13 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

package er.directtoweb.assignments;

import java.util.Enumeration;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.foundation.ERXValueUtilities;

public class ERDSortedManyToManyAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** holds the array of dependent keys */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "object.entityName", "propertyKey" });

    /** User info key that specifies if a given relationship is a sorted join */
    public static final String SortedJoinRelationshipUserInfoKey = "SortedJoinRelationship";
    
    /**
    * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    // CHECKME: Pretty sure we only need one of these ever created.
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDSortedManyToManyAssignment(eokeyvalueunarchiver);
    }

    /**
        * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDSortedManyToManyAssignment (String key, Object value) { super(key,value); }

    /**
        * Public constructor
     * @param unarchiver key-value unarchiver used when unarchiving
     *		from rule files.
     */
    public ERDSortedManyToManyAssignment(EOKeyValueUnarchiver unarchiver) { super(unarchiver); }

    /**
        * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context keys: "propertyKey" and
     * "keyWhenRelationship". This array of keys is used when constructing the
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for.
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return null; }

    /**
        * Called when firing this assignment with the key-path:
     * <b>keyWhenRelationship</b>.
     * @return the current propertyKey + "." + the current value for
     *		keyWhenRelationship.
     */
    public Object keyWhenRelationship(D2WContext context) {
        EOEntity joinEntity=context.entity();
        NSArray relationships=joinRelationshipsForJoinEntity(joinEntity);
        
        EORelationship destinationRelationship=null;
        String originEntityName=(String)context.valueForKeyPath("object.entityName");
        for (Enumeration e=relationships.objectEnumerator(); e.hasMoreElements();) {
            EORelationship r=(EORelationship)e.nextElement();
            if (!originEntityName.equals(r.destinationEntity().name())) {
                destinationRelationship=r;
                break;
            }
        }
        return (destinationRelationship!=null ? (destinationRelationship.name()+".") : "") + "userPresentableDescription";
    }

    /**
     * Calculates the join relationships for a given join entity. 
     * @param entity to find join relationships
     * @return array containing two join relationships
     */
    public static NSArray joinRelationshipsForJoinEntity(EOEntity entity) {
        NSArray joinRelationships = null;
        if (entity.relationships() == null || entity.relationships().count() < 2) {
            throw new RuntimeException("Join entity: " + entity + " does not have any relationships!");
        } else if (entity.relationships().count() == 2) {
            joinRelationships = entity.relationships();
        } else {
            NSMutableArray relationshipCache = new NSMutableArray();
            for (Enumeration e = entity.relationships().objectEnumerator(); e.hasMoreElements();) {
                EORelationship relationship = (EORelationship)e.nextElement();
                if (relationship.userInfo() != null
                    && ERXValueUtilities.booleanValue(relationship.userInfo().objectForKey(SortedJoinRelationshipUserInfoKey))) {
                    relationshipCache.addObject(relationship);
                }
            }
            if (relationshipCache.count() != 2)
                throw new RuntimeException("Did not find two relationships with user info entries: " +
                                           SortedJoinRelationshipUserInfoKey + " found: " + relationshipCache.valueForKey("name")
                                           + " for entity: " + entity.name());
            joinRelationships = relationshipCache;
        }
        return joinRelationships;
    }
}
    