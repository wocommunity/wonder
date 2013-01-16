/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb.assignments.defaults;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;

import er.directtoweb.assignments.ERDAssignment;

/**
 * This assignment calculates default page configuration
 * names for the current entity in the context.
 */
public class ERDDefaultConfigurationNameAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** holds the array of keys this assignment depends upon */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] {"propertyKey", "object.entityName", "entity.name"});

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultConfigurationNameAssignment(eokeyvalueunarchiver);
    }
    
    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDefaultConfigurationNameAssignment (EOKeyValueUnarchiver u) { super(u); }
    
    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDefaultConfigurationNameAssignment (String key, Object value) { super(key,value); }

    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context keys: "entity.name" and 
     * "object.entityName". This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) { return _DEPENDENT_KEYS; }

    /**
     * Calculates the entity name for a given context. If
     * the property is a relationship (meaning that destinationEntity is set)
     * then this entity is used
     * @param c a D2W context
     * @return the current entity name for that context.
     */
    // MOVEME: ERD2WContextUtilities?
    protected String entityNameForContext(D2WContext c) {
        EOEntity entity = (EOEntity)c.valueForKey("destinationEntity");
        String entityName;
        if(entity != null) {
            entityName = entity.name();
        } else if(c.valueForKey("object") != null && c.valueForKey("object") instanceof EOEnterpriseObject) {
            entityName = ((EOEnterpriseObject)c.valueForKey("object")).entityName();
        } else if (c.entity() != null) {
            entityName = c.entity().name();
        } else {
            entityName = "*all*";
        }
        return entityName;
    }

    /**
     * Generates a default confirm page configuration
     * based on the current entity name. Default format
     * is 'Confirm' + entity name.
     * @param c current D2W context
     * @return default confirm page configuration name
     */
    public Object confirmConfigurationName(D2WContext c) {
        return "Confirm" + entityNameForContext(c); 
    }

    /**
     * Generates a default confirm delete page configuration
     * based on the current entity name. Default format
     * is 'ConfirmDelete' + entity name.
     * @param c current D2W context
     * @return default confirm delete page configuration name
     */
    public Object confirmDeleteConfigurationName(D2WContext c) {
        return "ConfirmDelete" + entityNameForContext(c);
    }

    /**
     * Generates a default create page configuration
     * based on the current entity name. Default format
     * is 'Create' + entity name.
     * @param c current D2W context
     * @return default create page configuration name
     */
    public Object createConfigurationName(D2WContext c) {
        return "Create" +  entityNameForContext(c); 
    }

    /**
     * Generates a default embedded create page configuration
     * based on the current entity name. Default format
     * is 'CreateEmbedded' + entity name.
     * @param c current D2W context
     * @return default embedded edit page configuration name
     */
    public Object createEmbeddedConfigurationName(D2WContext c) {
        return "CreateEmbedded" + entityNameForContext(c);
    }
    
    /**
        * Generates a default edit page configuration
     * based on the current entity name. Default format
     * is 'Edit' + entity name.
     * @param c current D2W context
     * @return default edit page configuration name
     */
    public Object editConfigurationName(D2WContext c) {
        return "Edit" + entityNameForContext(c);
    }

    /**
     * Generates a default edit relationship page configuration
     * based on the current entity name. Default format
     * is 'EditRelationship' + entity name.
     * @param c current D2W context
     * @return default edit relationship page configuration name
     */
    public Object editRelationshipConfigurationName(D2WContext c) {
        return "EditRelationship" + entityNameForContext(c);
    }

    /**
     * Generates a default embedded edit page configuration
     * based on the current entity name. Default format
     * is 'EditEmbedded' + entity name.
     * @param c current D2W context
     * @return default embedded edit page configuration name
     */
    public Object editEmbeddedConfigurationName(D2WContext c) {
        return "EditEmbedded" + entityNameForContext(c);
    }
    
    /**
     * Generates a default embedded edit relationship page
     * configuration based on the current entity name. Default
     * format is 'EditRelationshipEmbedded' + entity.name.
     * @param c current D2W context
     * @return default embedded edit relationship page configuration name
     */
    public Object editRelationshipEmbeddedConfigurationName(D2WContext c) {
    	return "EditRelationshipEmbedded" + entityNameForContext(c);
    }

    /**
     * Generates a default inspect page configuration
     * based on the current entity name. Default format
     * is 'Inspect' + entity name.
     * @param c current D2W context
     * @return default inspect page configuration name
     */
    public Object inspectConfigurationName(D2WContext c) {
        return "Inspect" + entityNameForContext(c);
    }

    /**
     * Generates a default inspect page configuration
     * based on the current entity name. Default format
     * is 'InspectEmbedded' + entity name.
     * @param c current D2W context
     * @return default inspect page configuration name
     */
    public Object inspectEmbeddedConfigurationName(D2WContext c) {
        return "InspectEmbedded" + entityNameForContext(c);
    }

    /**
     * Generates a default list page configuration
     * based on the current entity name. Default format
     * is 'List' + entity name.
     * @param c current D2W context
     * @return default list page configuration name
     */
    public Object listConfigurationName(D2WContext c) {
        return "List" +  entityNameForContext(c); 
    }

    /**
     * Generates a default list page configuration
     * based on the current entity name. Default format
     * is 'ListEmbedded' + entity name.
     * @param c current D2W context
     * @return default list page configuration name
     */
    public Object listEmbeddedConfigurationName(D2WContext c) {
        return "ListEmbedded" +  entityNameForContext(c);
    }

    /**
     * Generates a default query page configuration
     * based on the current entity name. Default format
     * is 'Query' + entity name.
     * @param c current D2W context
     * @return default list page configuration name
     */
    public Object queryConfigurationName(D2WContext c) {
        return "Query" +  entityNameForContext(c);
    }

    /**
     * Generates a default inspect tab page configuration
     * based on the current entity name. Default format
     * is 'InspectTab' + entity name.
     * @param c current D2W context
     * @return default list page configuration name
     */
    public Object inspectTabConfigurationName(D2WContext c) {
        return "InspectTab" + entityNameForContext(c);
    }

    /**
     * Generates a default edit tab page configuration
     * based on the current entity name. Default format
     * is 'EditTab' + entity name.
     * @param c current D2W context
     * @return default list page configuration name
     */
    public Object editTabConfigurationName(D2WContext c) {
        return "EditTab" + entityNameForContext(c);
    }

    /**
     * Generates a default create tab page configuration
     * based on the current entity name. Default format
     * is 'CreateTab' + entity name.
     * @param c current D2W context
     * @return default list page configuration name
     */
    public Object createTabConfigurationName(D2WContext c) {
        return "CreateTab" + entityNameForContext(c);
    }

    /**
     * Generates a default create tab page configuration
     * based on the current entity name. Default format
     * is 'Create' + entity name + 'Wizard'.
     * @param c current D2W context
     * @return default list page configuration name
     */
    public Object createWizardConfigurationName(D2WContext c) {
        return "Create" + entityNameForContext(c) + "Wizard";
    }
}
