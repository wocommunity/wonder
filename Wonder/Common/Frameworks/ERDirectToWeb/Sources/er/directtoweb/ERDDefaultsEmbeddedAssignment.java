//
// ERDDefaultsEmbeddedAssignment.java
// Project ERDirectToWeb
//
// Created by ak on Tue Apr 23 2002
//
package er.directtoweb;
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.directtoweb.*;
import er.extensions.*;

public class ERDDefaultsEmbeddedAssignment extends ERDAssignment {
    static final ERXLogger log = ERXLogger.getLogger(ERDDefaultsEmbeddedAssignment.class);

    public ERDDefaultsEmbeddedAssignment (EOKeyValueUnarchiver u) { super(u); }
    public ERDDefaultsEmbeddedAssignment (String key, Object value) { super(key,value); }

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDefaultsEmbeddedAssignment (eokeyvalueunarchiver);
    }

    public static final NSArray _DEPENDENT_KEYS=new NSArray(new String[] {"embeddedEntityName", "object.entityName", "propertyKey"});
    public NSArray dependentKeys(String keyPath) {
        return _DEPENDENT_KEYS;
    }


    public NSArray defaultEmbeddedDisplayPropertyKeys(D2WContext c) {
        String entityName = (String)c.valueForKey("embeddedEntityName");
        EOEntity e = EOModelGroup.defaultGroup().entityNamed(entityName);
        NSMutableArray classProperties = e.classPropertyNames().mutableClone();
        NSArray relationships = (NSArray)e.relationships().valueForKey("name");
        classProperties.removeObjectsInArray(relationships);
        log.info(classProperties);
        return classProperties.immutableClone();
    }

    public Object embeddedEntityName(D2WContext c) {
        return defaultEmbeddedEntityName(c);
    }
    
    public Object defaultEmbeddedEntityName(D2WContext c) {
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
        } else {
        }
        if (result==null) {
            result=c.relationship();
            log.warn(propertyKey + "-" + rawObject);
        }
        if(result != null)
            result = ((EORelationship)result).destinationEntity().name();
        return result;
    }

    public String defaultEmbeddedListPageConfiguration(D2WContext c) {
        return "ListEmbedded" + c.valueForKey("embeddedEntityName");
    }

    public String defaultEmbeddedInspectPageConfiguration(D2WContext c) {
        return "InspectEmbedded" + c.valueForKey("embeddedEntityName");
    }

    public String defaultEmbeddedEditPageConfiguration(D2WContext c) {
        return "EditEmbedded" + c.valueForKey("embeddedEntityName");
    }
    
    public String keyForMethodLookup(D2WContext c) {
        return (String)value();
    }
}
