//
// ERXEOControlUtilities.java
// Project ERExtensions
//
// Created by max on Wed Oct 09 2002
//
package er.extensions;

import com.webobjects.foundation.NSArray;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOUtilities;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaulting;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGenericRecord;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSharedEditingContext;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSPropertyListSerialization;

/**
 * Collection of EOF utility method centered around
 * EOControl.
 */
public class ERXEOControlUtilities {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXEOControlUtilities.class);
    
    /**
     * Turns a given enterprise object back into a fault.
     * @param eo enterprise object to refault
     */
    public static void refaultObject(EOEnterpriseObject eo) {
        if (eo != null && !eo.isFault()) {
            EOEditingContext ec = eo.editingContext();
            NSArray gids = new NSArray(ec.globalIDForObject(eo));
            ec.invalidateObjectsWithGlobalIDs(gids);
        }
    }

    /**
     * Constructs a fetch specification that will only fetch the primary
     * keys for a given qualifier.
     * @param ec editing context, only used to determine the entity
     * @param entityName name of the entity, only used to determine the entity
     * @param eoqualifier to construct the fetch spec with
     * @param sortOrderings array of sort orderings to sort the result set
     *		with.
     * @param additionalKeys array of additional key paths to construct the
     *		raw rows key paths to fetch.
     * @return fetch specification that can be used to fetch primary keys for
     * 		a given qualifier and sort orderings.
     */
    public static EOFetchSpecification primaryKeyFetchSpecificationForEntity(EOEditingContext ec,
                                                                      String entityName,
                                                                      EOQualifier eoqualifier,
                                                                      NSArray sortOrderings,
                                                                      NSArray additionalKeys) {
        EOModelGroup group = EOModelGroup.modelGroupForObjectStoreCoordinator((EOObjectStoreCoordinator)ec.rootObjectStore());
        EOEntity entity = group.entityNamed(entityName);
        EOFetchSpecification fs = new EOFetchSpecification(entityName, eoqualifier, sortOrderings);
        fs.setFetchesRawRows(true);
        NSMutableArray keys = new NSMutableArray(entity.primaryKeyAttributeNames());
        if (additionalKeys != null) {
            keys.addObjectsFromArray(additionalKeys);
        }
        fs.setRawRowKeyPaths(keys);
        return fs;
    }

    /**
    * Fetches an array of primary keys matching a given qualifier
     * and sorted with a given array of sort orderings.
     * @param ec editing context to fetch into
     * @param entityName name of the entity
     * @param eoqualifier to restrict matching primary keys
     * @param sortOrderings array of sort orders to sort result set
     * @return array of primary keys matching a given qualifier
     */
    public static NSArray primaryKeysMatchingQualifier(EOEditingContext ec, String entityName, EOQualifier eoqualifier, NSArray sortOrderings) {
        EOFetchSpecification fs = ERXEOControlUtilities.primaryKeyFetchSpecificationForEntity(ec, entityName, eoqualifier, sortOrderings, null);
        return ec.objectsWithFetchSpecification(fs);
    }

    /**
     * Returns the number of objects matching the given
     * qualifier for a given entity name. Implementation
     * wise this method will generate the correct sql to only
     * perform a count, i.e. all of the objects wouldn't be
     * pulled into memory.
     * @param ec editing context to use for the count qualification
     * @param entityName name of the entity to fetch
     * @param qualifier to find the matching objects
     * @return number of matching objects
     */
    public static Number objectCountWithQualifier(EOEditingContext ec, String entityName, EOQualifier qualifier) {
        EOAttribute attribute = EOGenericRecordClazz.objectCountAttribute();
        return _objectCountWithQualifierAndAttribute(ec,entityName,qualifier,attribute);
    }

    /**
     * Returns the number of unique objects matching the given
     * qualifier for a given entity name. Implementation
     * wise this method will generate the correct sql to only
     * perform a count, i.e. all of the objects wouldn't be
     * pulled into memory.
     * @param ec editing context to use for the count qualification
     * @param entityName name of the entity to fetch
     * @param qualifier to find the matching objects
     * @return number of matching objects
     * @attributeName name of attribute in same entity to consider in order to determine uniqueness
     */
    public static Number objectCountUniqueWithQualifierAndAttribute(EOEditingContext ec, String entityName, EOQualifier qualifier, String attributeName) {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        EOAttribute attribute = entity.attributeNamed(attributeName);
        EOAttribute att2 = EOGenericRecordClazz.objectCountUniqueAttribute(attribute);
        return _objectCountWithQualifierAndAttribute(ec,entityName,qualifier,att2);
    }

    private static Number _objectCountWithQualifierAndAttribute(EOEditingContext ec, String entityName, EOQualifier qualifier, EOAttribute attribute) {
        NSArray results = null;
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        EOQualifier schemaBasedQualifier = entity.schemaBasedQualifier(qualifier);
        EOFetchSpecification fs = new EOFetchSpecification(entity.name(), schemaBasedQualifier, null);
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

    /**
     * Computes an aggregate function for a given attribute
     * restricted by a given qualifier. For instance
     * select MAX(AGE) from User where name like 'M*'
     * 
     * @param ec editing context used for the fetch
     * @param entityName name of the entity
     * @param attributeName attribute for the function to be performed on
     * @param function name, ie MAX, MIN, AVG, etc.
     * @param qualifier to restrict data set
     * @return aggregate result of the fuction call
     */
    public static Number aggregateFunctionWithQualifier(EOEditingContext ec,
                                                        String entityName,
                                                        String attributeName,
                                                        String function,
                                                        EOQualifier qualifier) {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);

        NSArray results = null;

        EOAttribute attribute = ERXEOAccessUtilities.createAggregateAttribute(ec,
                                                                              function,
                                                                              attributeName,
                                                                              entityName);

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

    /**
     * Finds an object  in the shared editing context matching a key
     * and value. This has the benifit of not requiring a database
     * round trip if the entity is shared.
     * @param entityName name of the shared entity
     * @param key to match against
     * @param value value to match
     * @return matching shared object
     */
    public static EOEnterpriseObject sharedObjectMatchingKeyAndValue(String entityName, String key, Object value) {
        NSArray filtered = sharedObjectsMatchingKeyAndValue(entityName, key, value);
        if (filtered.count() > 1)
            log.warn("Found multiple shared objects for entityName: " + entityName + " matching key: "
                     + key + " value: " + value + " matched against: " + filtered);
        return filtered.count() > 0 ? (EOEnterpriseObject)filtered.lastObject() : null;
    }

    /**
     * Finds objects in the shared editing context matching a key
     * and value. This has the benifit of not requiring a database
     * round trip if the entity is shared.
     * @param entityName name of the shared entity
     * @param key to match against
     * @param value value to match
     * @return array of shared objects matching key and value
     */
    public static NSArray sharedObjectsMatchingKeyAndValue(String entityName, String key, Object value) {
        NSArray filtered = null;
        EOKeyValueQualifier qualifier = new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorEqual, value);
        NSArray sharedEos = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(entityName);
        if (sharedEos != null) {
            filtered = EOQualifier.filteredArrayWithQualifier(sharedEos, qualifier);
        } else {
            log.warn("Unable to find any shared objects for entity name: " + entityName);
        }
        return filtered != null ? filtered : NSArray.EmptyArray;
    }

    /**
     * Gets all of the shared objects for a given entity name. Note that
     * if the entity has not been loaded yet, ie it's model has not been
     * accessed then this method will return an empty array.
     * @param entityName name of the shared entity
     * @return array of bound shared objects for the given entity name
     */
    public static NSArray sharedObjectsForEntityNamed(String entityName) {
        NSArray sharedEos = (NSArray)EOSharedEditingContext.defaultSharedEditingContext().objectsByEntityName().objectForKey(entityName);
        if (sharedEos == null) {
            log.warn("Unable to find any shared objects for the entity named: " + entityName);
        }
        return sharedEos != null ? sharedEos : NSArray.EmptyArray;
    }

    /**
     * Gets the shared enterprise object with the given primary
     * from the default shared editing context. This has the
     * advantage of not requiring a roundtrip to the database to
     * lookup the object.
     * @param entityName name of the entity
     * @param pk primary key of object to be found
     * @return the shared object registered in the default shared editing context
     */
    public static EOEnterpriseObject sharedObjectWithPrimaryKey(String entityName, Object primaryKey) {
        EOKeyGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(entityName, new Object[] {primaryKey});
        EOEnterpriseObject eo = EOSharedEditingContext.defaultSharedEditingContext().objectForGlobalID(gid);
        return (eo != null) ? eo : EOUtilities.objectWithPrimaryKeyValue(EOSharedEditingContext.defaultSharedEditingContext(),
                                                     entityName,
                                                     primaryKey);
    }
    
    /**
     * Gets a fetch specification from a given entity. If qualifier binding variables
     * are passed in then the fetchspecification is cloned and the binding variables
     * are substituted returning a fetch specification that can be used.
     * @param entityName name of the entity that the fetch specification is bound to
     * @param fetchSpecificationName name of the fetch specification
     * @param dictionary of qualifier bindings
     * @return fetch specification identified by name and potentially with the qualifier
     * 		bindings replaced.
     */
    public static EOFetchSpecification fetchSpecificationNamedWithBindings(String entityName,
                                                                    String fetchSpecificationName,
                                                                    NSDictionary bindings) {
        EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed(fetchSpecificationName,
                                                                                      entityName);
        return fetchSpec != null && bindings != null ?
            fetchSpec.fetchSpecificationWithQualifierBindings(bindings) : fetchSpec;
    }

    /**
     * Utility method to generate a new primary key dictionary using
     * the objects class properties. Use it when your PKs are class properties.
     * @param eo object in question
     * @returns new primary key dictionary or null if one of the properties is
     * null or one of the primary key attributes is not a class property.
     */

    public static NSDictionary newPrimaryKeyDictionaryForObjectFromClassProperties(EOEnterpriseObject eo) {
        EOEditingContext ec = eo.editingContext();
        EOEntity entity = EOUtilities.entityNamed(ec, eo.entityName());
        NSArray pkAttributes = entity.primaryKeyAttributeNames();
        int count = pkAttributes.count();
        NSMutableDictionary nsmutabledictionary = new NSMutableDictionary(count);
        NSArray classPropertyNames = entity.classPropertyNames();
        while (count-- != 0) {
            String key = (String)pkAttributes.objectAtIndex(count);
            if(!classPropertyNames.containsObject(key))
                return null;
            Object value = eo.valueForKey(key);
            if(value == null)
                return null;
            nsmutabledictionary.setObjectForKey(value, key);
        }
        return nsmutabledictionary;
    }


    /**
     * Utility method to generate a new primary key for an object. Calls
     * {@link newPrimaryKeyForObjectFromClassProperties(EOEnterpriseObject)} and if that returns null,
     * {@link newPrimaryKeyDictionaryForEntityNamed(EOEditingContext, String)}
     * @returns new primary key dictionary or null if a failure occured.
     */

    public static NSDictionary newPrimaryKeyDictionaryForObject(EOEnterpriseObject eo) {
        NSDictionary dict = newPrimaryKeyDictionaryForObjectFromClassProperties(eo);
        if(dict == null) {
            dict = newPrimaryKeyDictionaryForEntityNamed(eo.editingContext(), eo.entityName());
        }
        return dict;
    }


    /**
     * Utility method to generate a new primary key dictionary using
     * the adaptor for a given entity. This is can be handy if you
     * need to have a primary key for an object before it is saved to
     * the database. This method uses the same method that EOF uses
     * by default for generating primary keys. See
     * {@link ERXGeneratesPrimaryKeyInterface} for more information
     * about using a newly created dictionary as the primary key for
     * an enterprise object.
     * @param ec editing context
     * @param entityName name of the entity to generate the primary
     *		key dictionary for.
     * @returns a dictionary containing a new primary key for the given
     *		entity.
     */
    public static NSDictionary newPrimaryKeyDictionaryForEntityNamed(EOEditingContext ec, String entityName) {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        NSDictionary primaryKey = null;
        try {
            dbContext.lock();
            EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
            if (!adaptorChannel.isOpen())
                adaptorChannel.openChannel();
            NSArray arr = adaptorChannel.primaryKeysForNewRowsWithEntity(1, entity);
            if(arr != null)
                primaryKey = (NSDictionary)arr.lastObject();
            else
                log.warn("Could not get primary key for entity: " + entityName + " exception");
            dbContext.unlock();
        } catch (Exception e) {
            dbContext.unlock();
            log.error("Caught exception when generating primary key for entity: " + entityName + " exception: " + e, e);
        }
        return primaryKey;
    }

    /**
     * Returns the propertylist-encoded string representation of the primary key for
     * a given object.
     * @param eo object to get the primary key for.
     * @returns string representation of the primary key of the
     *		object.
     */
    public static String primaryKeyStringForObject(EOEnterpriseObject eo) {
        Object pk=primaryKeyObjectForObject(eo);
        if(pk == null)
            return null;
        if(pk instanceof String || pk instanceof Number) {
            return pk.toString();
        }
        return NSPropertyListSerialization.stringFromPropertyList(pk);
    }

    /**
     * Returns either the single object the PK consist of or the NSArray of its values if the key is compound.
     * @param eo object to get the primary key for.
     * @returns single object or NSArray
     */
    public static Object primaryKeyObjectForObject(EOEnterpriseObject eo) {
        NSArray arr=primaryKeyArrayForObject(eo);
        if(arr != null && arr.count() == 1) return arr.lastObject();
        return arr;
    }

    /**
     * Gives the primary key array for a given enterprise
     * object. This has the advantage of not firing the
     * fault of the object, unlike the method in
     * {@link com.webobjects.eoaccess.EOUtilities EOUtilities}.
     * @param obj enterprise object to get the primary key array from.
     * @return array of all the primary key values for the object.
     */
    public static NSArray primaryKeyArrayForObject(EOEnterpriseObject obj) {
        EOEditingContext ec = obj.editingContext();
        if (ec == null) {
            //you don't have an EC! Bad EO. We can do nothing.
            return null;
        }
        EOGlobalID gid = ec.globalIDForObject(obj);
        if (gid.isTemporary()) {
            //no pk yet assigned
            return null;
        }
        EOKeyGlobalID kGid = (EOKeyGlobalID) gid;
        return kGid.keyValuesArray();
    }


    /**
        * Fetches an enterprise object based on a given primary key value.
     * This method has an advantage over the standard EOUtilities method
     * in that you can specify prefetching key paths as well as refreshing
     * the snapshot of the given object
     * @param ec editing context to fetch into
     * @param entityName name of the entity
     * @param primaryKeyValue primary key value
     * @param prefetchingKeyPaths key paths to fetch off of the eo
     * @return enterprise object matching the given value
     */
    public static EOEnterpriseObject objectWithPrimaryKeyValue(EOEditingContext ec,
                                                               String entityName,
                                                               Object primaryKeyValue,
                                                               NSArray prefetchingKeyPaths) {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        if (entity.primaryKeyAttributes().count() != 1) {
            throw new IllegalStateException("Entity \"" + entity.name() + "\" has a compound primary key. Can't be used with the method: objectWithPrimaryKeyValue");
        }
        NSDictionary values = new NSDictionary(primaryKeyValue,
                                               ((EOAttribute)entity.primaryKeyAttributes().lastObject()).name());
        EOQualifier qualfier = EOQualifier.qualifierToMatchAllValues(values);
        EOFetchSpecification fs = new EOFetchSpecification(entityName, qualfier, null);
        // Might as well get fresh stuff
        fs.setRefreshesRefetchedObjects(true);
        if (prefetchingKeyPaths != null)
            fs.setPrefetchingRelationshipKeyPaths(prefetchingKeyPaths);
        NSArray eos = ec.objectsWithFetchSpecification(fs);
        if (eos.count() > 1)
            throw new IllegalStateException("Found multiple objects for entity \"" + entity.name()
                                            + " with primary key value: " + primaryKeyValue);
        return eos.count() == 1 ? (EOEnterpriseObject)eos.lastObject() : null;
    }

    /**
     * Enhanced version of the utility method found in EOUtilities. Adds
     * support for including newly created objects in the fetch as well
     * as prefetching key paths.
     * @param ec editing context to fetch it into
     * @param entityName name of the entity
     * @param qualifierFormat format of the qualifier string
     * @param args qualifier arguments
     * @param prefetchKeyPaths prefetching key paths
     * @param includeNewObjects option to include newly inserted objects in the result set
     * @return array of objects matching the constructed qualifier
     */
    public static NSArray objectsWithQualifierFormat(EOEditingContext ec,
                                                         String entityName,
                                                         String qualifierFormat,
                                                         NSArray args,
                                                         NSArray prefetchKeyPaths,
                                                         boolean includeNewObjects) {
        EOQualifier qual = EOQualifier.qualifierWithQualifierFormat(qualifierFormat, args);
        return objectsWithQualifier(ec, entityName, qual, prefetchKeyPaths, includeNewObjects);
    }

    /**
     * Utility method used to fetch an array of objects given a qualifier. Also
     * has support for filtering the newly inserted objects as well as specifying
     * prefetching key paths.
     * @param ec editing context to fetch it into
     * @param entityName name of the entity
     * @param qualifierFormat format of the qualifier string
     * @param args qualifier arguments
     * @param prefetchKeyPaths prefetching key paths
     * @param includeNewObjects option to include newly inserted objects in the result set
     * @return array of objects matching the constructed qualifier
     */    
    // ENHANCEME: This should handle entity inheritance for in memory filtering
    public static NSArray objectsWithQualifier(EOEditingContext ec,
                                                   String entityName,
                                                   EOQualifier qualifier,
                                                   NSArray prefetchKeyPaths,
                                                   boolean includeNewObjects) {
        NSMutableArray result = null;
        if (includeNewObjects) {
            NSDictionary insertedObjects = ERXArrayUtilities.arrayGroupedByKeyPath(ec.insertedObjects(), "entityName");
            NSArray insertedObjectsForEntity = (NSArray)insertedObjects.objectForKey(entityName);

            if (insertedObjectsForEntity != null && insertedObjectsForEntity.count() > 0) {
                NSArray inMemory = EOQualifier.filteredArrayWithQualifier(insertedObjectsForEntity, qualifier);
                if (inMemory.count() > 0)
                    result = inMemory.mutableClone();
            }            
        }
        EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, null);
        fs.setPrefetchingRelationshipKeyPaths(prefetchKeyPaths);
        NSArray fromDb = ec.objectsWithFetchSpecification(fs);
        if (result != null)
            result.addObjectsFromArray(fromDb);
        return result != null ? result : fromDb;
    }

    /**
     * Refaults a relationship off of an enterprise object.
     * @param sourceObject eo to refault the relationship off of
     * @param relationshipName relationship to refault
     */
    public static void refaultRelationshipForObject(EOGenericRecord sourceObject, String relationshipName) {
        
        if (!sourceObject.isFault()) {
            EOEditingContext ec = sourceObject.editingContext();
            EOGlobalID aGlobalID = ec.globalIDForObject(sourceObject);

            // Don't want to refault the source object because I will lose any
            // any changes to it.
            //ec.refaultObject(sourceObject);

            EOModel model = EOUtilities.entityForObject(ec, sourceObject).model();
            
            EODatabaseContext dc = EOUtilities.databaseContextForModelNamed (ec,
                                                                             model.name());
            
            // Clear out the existing snapshot
            dc.database().recordSnapshotForSourceGlobalID(null, aGlobalID, relationshipName);

            EOFaulting toManyArray = (EOFaulting)sourceObject.storedValueForKey(relationshipName);
            if (!toManyArray.isFault()) {
                EOFaulting tmpToManyArray = (EOFaulting)((EOObjectStoreCoordinator)ec.rootObjectStore()).arrayFaultWithSourceGlobalID(aGlobalID, relationshipName, ec);
                toManyArray.turnIntoFault(tmpToManyArray.faultHandler());
            }
        }
    }
}
