//
// EOGenericRecordClazz.java
// Project ERExtensions
//
// Created by ak on Fri Apr 12 2002
//
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/** WARNING: this is alpha and untested!
Use subclasses of EOGenericRecordClazz as inner classes in your EO subclasses to work around the missing class object inheritance of java. They <b>must</b> be named XXX.XXXClazz to work!
Every subclass of this class will get their own "ClazzObject" instance, so it's OK to store things which might be different in superclasses. That is, the "User"'s implementation can override the "Person"'s and because Person.clazz() will get it's own instance, it will do only "Person" things.
The methods from EOUtilities are mirrored here so you don't have to import EOAccess in your subclasses, which is not legal for client-side classes. The implementation for a client-side class could then be easily switched to use the server-side EOUtilites implementation.
*/

public class EOGenericRecordClazz extends Object {
    private String _entityName;

    private static NSMutableDictionary allClazzes = new NSMutableDictionary();
    
    public EOGenericRecordClazz() {
    }

    public static void resetClazzCache() { allClazzes.removeAllObjects(); }
    private static EOGenericRecordClazz classFromEntity(EOEntity entity) {
        EOGenericRecordClazz clazz = null;
        if(entity == null) {
            return new EOGenericRecordClazz();
        }
        try {
            String className = entity.className();
            if(className.equals("ERXGenericRecord"))
                clazz = new ERXGenericRecord.ERXGenericRecordClazz();
            else
                clazz = (EOGenericRecordClazz)Class.forName(className + "$" + entity.name() + "Clazz").newInstance();
        } catch (InstantiationException ex) {
        } catch (ClassNotFoundException ex) {
        } catch (IllegalAccessException ex) {
        }
        if(clazz == null) return classFromEntity(entity.parentEntity());
        return clazz;
    }
    
    public static EOGenericRecordClazz clazzForEntityNamed(String entityName) {
        EOGenericRecordClazz clazz = (EOGenericRecordClazz)allClazzes.objectForKey(entityName);
        if(clazz == null) {
            clazz = classFromEntity(EOModelGroup.defaultGroup().entityNamed(entityName));
            clazz.setEntityName(entityName);
            allClazzes.setObjectForKey(clazz,entityName);
        }
        return clazz;
    }
    
    public NSArray allObjects(EOEditingContext ec) {
        return EOUtilities.objectsForEntityNamed(ec, entityName());
    }

    public EOEnterpriseObject objectFromRawRow(EOEditingContext ec, NSDictionary dict) {
        return EOUtilities.objectFromRawRow(ec, entityName(), dict);
    }

    public NSArray objectsWithQualifierFormat(EOEditingContext ec, String qualifier, NSArray args) {
        return EOUtilities.objectsWithQualifierFormat(ec, entityName(), qualifier, args);
    }

    public NSArray objectsWithFetchSpecificationAndBindings(EOEditingContext ec, String name, NSDictionary bindings) {
        return EOUtilities.objectsWithFetchSpecificationAndBindings(ec, entityName(), name, bindings);
    }

    public void setEntityName(String name) { _entityName = name; }
    public String entityName() { return _entityName; }

    private EOEntity _entity;
    public EOEntity entity() {
        if(_entity == null) _entity = EOModelGroup.defaultGroup().entityNamed(entityName());
        return _entity;
    }

    public EOFetchSpecification fetchSpecificationNamed(String name) {
        return EOModelGroup.defaultGroup().fetchSpecificationNamed(name,entityName());
    }
    
    private static EOAttribute _objectCountAttribute = null;

    private static EOAttribute objectCountAttribute() {
        if ( _objectCountAttribute == null ) {
            _objectCountAttribute = new EOAttribute();

            _objectCountAttribute.setName("p_objectCountAttribute");
            _objectCountAttribute.setColumnName("p_objectCountAttribute");
            _objectCountAttribute.setClassName("java.lang.Number");
            _objectCountAttribute.setValueType("i");
            _objectCountAttribute.setReadFormat("count(*)");
        }
        return _objectCountAttribute;
    }

    public Number objectCountWithQualifier(EOEditingContext ec, EOQualifier qualifier) {
        String entityName = entityName();
        
        NSArray results = null;

        EOAttribute attribute = EOGenericRecordClazz.objectCountAttribute();
        EOEntity entity = entity();
        EOQualifier schemaBasedQualifier = entity.schemaBasedQualifier(qualifier);
        EOFetchSpecification fs = new EOFetchSpecification(entityName, schemaBasedQualifier, null);
        synchronized (entity) {
            entity.addAttribute(attribute);

            fs.setFetchesRawRows(true);
            fs.setRawRowKeyPaths(new NSArray(attribute.name()));

            results = ec.objectsWithFetchSpecification(fs);

            entity.removeAttribute(attribute);
        }
        if ((results != null) && (results.count() == 1)) {
            NSDictionary row = (NSDictionary) results.lastObject();

            return (Number)row.objectForKey(attribute.name());
        }

        return null;
    }

    public Number objectCountWithFetchSpecificationAndBindings(EOEditingContext editingContext, String fetchSpecName,  NSDictionary bindings) {
        String entityName = entityName();
        EOFetchSpecification unboundFetchSpec;
        EOFetchSpecification boundFetchSpec;

        unboundFetchSpec = fetchSpecificationNamed(fetchSpecName);
        if (unboundFetchSpec == null) {
            return null;
        }
        boundFetchSpec = unboundFetchSpec.fetchSpecificationWithQualifierBindings(bindings);
        return objectCountWithQualifier(editingContext, boundFetchSpec.qualifier());
    }

    public EOFetchSpecification primaryKeyFetchSpecificationForEntity(EOEditingContext ec, EOQualifier eoqualifier, NSArray sortOrderings, NSArray additionalKeys) {
        String entityName = entityName();
        EOFetchSpecification fs = new EOFetchSpecification(entityName, eoqualifier, sortOrderings);
        fs.setFetchesRawRows(true);
        NSMutableArray keys = new NSMutableArray(entity().primaryKeyAttributeNames());
        if(additionalKeys != null) {
            keys.addObjectsFromArray(additionalKeys);
        }
        fs.setRawRowKeyPaths(keys);
        return fs;
    }

    public NSArray primaryKeysMatchingQualifier(EOEditingContext ec, EOQualifier eoqualifier, NSArray sortOrderings) {
        String entityName = entityName();
        EOFetchSpecification fs = primaryKeyFetchSpecificationForEntity(ec, eoqualifier, sortOrderings, null);
        NSArray nsarray = ec.objectsWithFetchSpecification(fs);
        return nsarray;
    }

    public NSArray primaryKeysMatchingValues(EOEditingContext ec, NSDictionary nsdictionary, NSArray sortOrderings) {
        String entityName = entityName();
        return primaryKeysMatchingQualifier(ec, EOQualifier.qualifierToMatchAllValues(nsdictionary), sortOrderings);
    }

    public NSArray faultsFromRawRows(EOEditingContext ec, NSArray nsarray) {
        String entityName = entityName();
        int count = nsarray.count();
        NSMutableArray faults = new NSMutableArray(count);
        for( int i = 0; i < count; i ++ ) {
            faults.addObject(objectFromRawRow(ec, (NSDictionary)nsarray.objectAtIndex(i)));
        }
        return faults;
    }

    public NSArray faultsMatchingQualifier(EOEditingContext ec, EOQualifier eoqualifier) {
        String entityName = entityName();
        NSArray nsarray = primaryKeysMatchingQualifier(ec, eoqualifier, null);
        return faultsFromRawRows(ec, nsarray);
    }

    public NSArray faultsMatchingQualifier(EOEditingContext ec, EOQualifier eoqualifier, NSArray sortOrderings) {
        String entityName = entityName();
        NSArray nsarray = primaryKeysMatchingQualifier(ec, eoqualifier, sortOrderings);
        return faultsFromRawRows(ec, nsarray);
    }

    public NSArray faultsMatchingValues(EOEditingContext ec, NSDictionary nsdictionary, NSArray sortOrderings) {
        String entityName = entityName();
        NSArray nsarray = primaryKeysMatchingValues(ec, nsdictionary, sortOrderings);
        return faultsFromRawRows(ec, nsarray);
    }
}
