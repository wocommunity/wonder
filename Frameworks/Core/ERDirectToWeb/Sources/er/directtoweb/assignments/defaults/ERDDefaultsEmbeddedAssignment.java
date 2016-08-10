//
// ERDDefaultsEmbeddedAssignment.java
// Project ERDirectToWeb
//
// Created by ak on Tue Apr 23 2002
//
package er.directtoweb.assignments.defaults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.KeyValuePath;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.assignments.ERDAssignment;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * This assignment calculates default values for
 * embedded page configurations.
 */
// CHECKME ak I'm not sure that this is a value add anymore
// A better way to create embedded page configurations is to create a rule like:
//   propertyKey = "studios" => listPageConfiguration = "ListEmbeddedStudios"
//   propertyKey = "studios" => componentName = "ERDList"
//   pageConfiguration = "ListEmbeddedStudios" => displayPropertyKeys = (name, @sum.movies.revenue)

public class ERDDefaultsEmbeddedAssignment extends ERDAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ERDDefaultsEmbeddedAssignment.class);

    /** holds the array of dependent keys */
    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] {"embeddedEntityName", "object.entityName", "propertyKey", "pageConfiguration"});

    /**
     * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */
     // ENHANCEME: Only need one of these per value()
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        ERDAssignment.logDeprecatedMessage(ERDDefaultsEmbeddedAssignment.class, ERDDefaultConfigurationNameAssignment.class);
        return new ERDDefaultsEmbeddedAssignment (eokeyvalueunarchiver);
    }

    /** 
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files. 
     */
    public ERDDefaultsEmbeddedAssignment (EOKeyValueUnarchiver u) { super(u); }

    /** 
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */
    public ERDDefaultsEmbeddedAssignment (String key, Object value) { super(key,value); }
    
    /**
     * Implementation of the {@link er.directtoweb.assignments.ERDComputingAssignmentInterface}. This
     * assignment depends upon the context keys: "propertyKey", "object.entityName",
     * and "embeddedEntityName". This array of keys is used when constructing the 
     * significant keys for the passed in keyPath.
     * @param keyPath to compute significant keys for. 
     * @return array of context keys this assignment depends upon.
     */
    public NSArray dependentKeys(String keyPath) {
        return _DEPENDENT_KEYS; 
    }

    /**
     * Gets the localizer for the current context. Implementation
     * wise all this method does is get the current session from
     * the context and then calls <code>localizerForSession</code>
     * off of {@link ERXLocalizer}.
     * @param c a D2W context
     * @return localizer for the session stored in the context.
     */
    @Override
    public ERXLocalizer localizerForContext(D2WContext c) {
        return ERXLocalizer.currentLocalizer();
    }

    /**
     * Calculates and returns the default embedded entity
     * display name. The results are localized.
     * @param c current context
     * @return default localized embedded entity name.
     */
    public String defaultEmbeddedEntityDisplayName(D2WContext c) {
        String value = ERXStringUtilities.displayNameForKey((String)c.valueForKey("embeddedEntityName"));
        String result = (String)ERXLocalizer.currentLocalizer().valueForKey(value);
        if(result == null) {
            result = value;
        }
        return result;
    }

    /**
     * Calculates the default embedded display property keys for a 
     * given context. Implementation wise this method uses the 
     * embedded entity to determine what all of the class properties
     * are minus all of the relationship keys.
     * @param c current D2W context
     * @return array of display keys for the current embedded entity.
     */
    public NSArray defaultEmbeddedDisplayPropertyKeys(D2WContext c) {
        String entityName = (String)c.valueForKey("embeddedEntityName");
    
        if(entityName != null) {
            // FIXME: Should try for the 'object' in the context and use the
            //		model group from the object's ec.
            EOEntity e = EOModelGroup.defaultGroup().entityNamed(entityName);
            log.debug("embeddedEntityName = {}", entityName);
            NSMutableArray classProperties = e.classPropertyNames().mutableClone();
            NSArray relationships = (NSArray)e.relationships().valueForKey("name");
            classProperties.removeObjectsInArray(relationships);
            return classProperties.immutableClone();
        }
        
        return NSArray.EmptyArray;
    }
    
    /**
     * Calculates the default embedded entity name using the
     * current object and propertyKey. 
     * @param c current context
     * @return name of the entity pointed to by the propertyKey
     *		off of the object in the context.
     */
    // CHECKME: Should just be able to use 'smartRelationship.destinationEntity.name'
    public Object defaultEmbeddedEntityName(D2WContext c) {
        if(c.valueForKeyPath("propertyKey") == null && c.entity() != null) {
            return c.entity().name();
        }
        Object result = c.valueForKeyPath("smartRelationship.destinationEntity.name");
        if(result == null) {
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
                    // FIXME: Should be using the model group from the ec of the lastEO.
                    EOEntity entity=EOModelGroup.defaultGroup().entityNamed(lastEO.entityName());
                    String lastKey=KeyValuePath.lastPropertyKeyInKeyPath(propertyKey);
                    result=entity.relationshipNamed(lastKey);
                }
            }
            if (result==null) {
                result=c.relationship();
                log.warn("{}-{}", propertyKey, rawObject);
            }
            if (result != null)
                result = ((EORelationship)result).destinationEntity().name();
        }
        return result;
    }

    /**
     * Calculates and returns the default embedded list page 
     * configuration to be used for embedded list pages.
     * @param c current context
     * @return page configuration of the form: "ListEmbedded" + the 
     *		the value of the current embeddedEntityname.
     */
    public String defaultEmbeddedListPageConfiguration(D2WContext c) {
        String result = "ListEmbedded" + c.valueForKey("embeddedEntityName");
        return result;
    }

    /**
     * Calculates and returns the default embedded inspect page 
     * configuration to be used for embedded inspect pages.
     * @param c current context
     * @return page configuration of the form: "InspectEmbedded" + the 
     *		the value of the current embeddedEntityname.
     */
    public String defaultEmbeddedInspectPageConfiguration(D2WContext c) {
        return "InspectEmbedded" + c.valueForKey("embeddedEntityName");
    }

    /**
     * Calculates and returns the default embedded edit page 
     * configuration to be used for embedded edit pages.
     * @param c current context
     * @return page configuration of the form: "EditEmbedded" + the 
     *		the value of the current embeddedEntityname.
     */
    public String defaultEmbeddedEditPageConfiguration(D2WContext c) {
        return "EditEmbedded" + c.valueForKey("embeddedEntityName");
    }
    
    /**
     * By default the key path being requested is used
     * as the key to lookup the method to call on the 
     * ERDAssignment subclass. However in the case of this
     * assignment we want to use the string value of the
     * <code>value</code> of the assignment as the key to 
     * lookup the method. For example if value of this 
     * assignment is "foo", then when this assignment is
     * fired the method <code>foo(D2WContext)</code> will
     * be called.
     * @param c current context
     * @return key for method lookup, in this case the 
     *		<code>value</code> of the assignment is returned.
     */
    @Override
    public String keyForMethodLookup(D2WContext c) {
        return (String)value();
    }
}
