//
//  ERDSortedManyToManyAssignment.java
//  ERDirectToWeb
//
//  Created by Patrice Gautier on Fri Sep 13 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import java.util.*;
import er.extensions.*;

public class ERDSortedManyToManyAssignment extends ERDAssignment {

    /** holds the array of dependent keys */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] { "object.entityName" });


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
        * Implementation of the {@link ERDComputingAssignmentInterface}. This
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
        // for now we rely on the fact that join entity should only have 2 relationships
        // and one of them is going back to the original object
        NSArray relationships=joinEntity.relationships();
        if (relationships==null || relationships.count()!=2)
            throw new RuntimeException("Found unexpected relationship array on "+joinEntity.name()+": "+relationships);
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

}
    