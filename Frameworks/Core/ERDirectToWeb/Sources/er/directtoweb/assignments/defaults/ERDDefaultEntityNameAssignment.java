/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.defaults;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.ERDAssignment;
import er.directtoweb.assignments.ERDLocalizableAssignmentInterface;
import er.extensions.foundation.ERXStringUtilities;

// MOVEME: This should move into the defaults assignment, only reason
//		it is here is because we used to not have the dependent
//		keys passing in the current keyPath.
/**
 * Beautify the entity name.<br />
 * @deprecated for entityName, use {@link er.directtoweb.assignments.defaults.ERDDefaultModelAssignment}, for displayNameForEntity and displayNameForDestinationEntity use {@link er.directtoweb.assignments.defaults.ERDDefaultDisplayNameAssignment}
 */
@Deprecated
public class ERDDefaultEntityNameAssignment extends ERDAssignment implements ERDLocalizableAssignmentInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    public static final Logger log = Logger.getLogger(ERDDefaultEntityNameAssignment.class);
    
    /** holds the array of keys this assignment depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray("entity.name");

    private static final NSArray DependentKeysDestinationEntityDisplayName = new NSArray(new Object[]{ "object.entity", "propertyKey"});
    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDDefaultEntityNameAssignment.class, ERDDefaultModelAssignment.class);
        return new ERDDefaultEntityNameAssignment(eokeyvalueunarchiver);
    }

    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */    
    public ERDDefaultEntityNameAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDefaultEntityNameAssignment (String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context key: "entity.name". This array 
     * of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        return keyPath.equals("displayNameForDestinationEntity") ? DependentKeysDestinationEntityDisplayName : _DEPENDENT_KEYS;
    }

    // Default names
    public Object displayNameForEntity(D2WContext c) {
        String value = ERXStringUtilities.displayNameForKey((String)c.valueForKeyPath("entity.name"));
        return localizedValueForKeyWithDefaultInContext(value, c);
    }

   // a fake entity that can be used for tasks such as error/confirm..
    private transient EOEntity _dummyEntity;
    public EOEntity dummyEntity() {
        if (_dummyEntity==null) {
            _dummyEntity=new EOEntity();
            _dummyEntity.setName("__Dummy__");
        }
        return _dummyEntity;
    }

    public Object entity(D2WContext c) {
        return dummyEntity();
    }

    /**
     * Calculates the display name for a destination entity.
     * @param context current context
     * @return display name for the destination entity
     */
    public String displayNameForDestinationEntity(D2WContext context) {
        String displayName = null;
        EORelationship relationship = (EORelationship)context.valueForKey("smartRelationship");
        if (relationship != null) {
            EOEntity entity = (EOEntity)context.valueForKey("entity");
            if (entity != null) {
                context.takeValueForKey(relationship.destinationEntity(), "entity");
                displayName = (String)context.valueForKey("displayNameForEntity");
            } else {
                log.warn("Current context: " + context + " doesn't have an entity, very strange, defaulting to destination entity name.");
                displayName = relationship.destinationEntity().name();
            }
        }
        return displayName;
    }    
}