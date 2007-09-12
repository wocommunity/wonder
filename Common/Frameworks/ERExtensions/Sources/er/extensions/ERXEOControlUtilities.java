//
// ERXEOControlUtilities.java
// Project ERExtensions
//
// Created by max on Wed Oct 09 2002
//
package er.extensions;

import com.webobjects.foundation.NSArray;
import com.webobjects.appserver.*;
import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import java.util.*;

/**
 * Collection of EOF utility method centered around
 * EOControl.
 */
public class ERXEOControlUtilities {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXEOControlUtilities.class);


    /**
     * Provides the same functionality as the equivalent method
     * in {@link com.webobjects.eoaccess.EOUtilities EOUtilities}
     * except it will use the localInstanceOfObject
     * method from this utilities class which has a few enhancements.
     *
     * @param ec editing context to pull local object copies
     * @param eos array of enterprise objects
     * @return an array of copies of local objects
     */
    public static NSArray localInstancesOfObjects(EOEditingContext ec, NSArray eos) {
        if (eos == null)
            throw new RuntimeException("ERXUtilites: localInstancesOfObjects: Array is null");
        if (ec == null)
            throw new RuntimeException("ERXUtilites: localInstancesOfObjects: EditingContext is null");
        NSMutableArray localEos = new NSMutableArray();
        for (Enumeration e = eos.objectEnumerator(); e.hasMoreElements();) {
            localEos.addObject(localInstanceOfObject(ec, (EOEnterpriseObject)e.nextElement()));
        }
        return localEos;
    }

    /**
     * Simple utility method that will convert an array
     * of enterprise objects into an EOArrayDataSource.<br/>
     * <br/>
     * Note that the datasource that is constructed uses the
     * class description and editing context of the first object
     * of the array.
     * @param array collection of objects to be turned into a
     *		datasource
     * @return an array datasource corresponding to the array
     *		of objects passed in.
     */
    public static EOArrayDataSource dataSourceForArray(NSArray array) {
        EOArrayDataSource dataSource = null;
        if (array != null && array.count() > 0) {
            EOEnterpriseObject eo = (EOEnterpriseObject)array.objectAtIndex(0);
            dataSource = new EOArrayDataSource(eo.classDescription(), eo.editingContext());
            dataSource.setArray(array);
        }
        return dataSource;
    }

    /**
     * Converts a datasource into an array.
     * @param dataSource data source to be converted
     * @return array of objects that the data source represents
     */
    public static NSArray arrayFromDataSource(EODataSource dataSource) {
        // FIXME: Now in WO 5 we can use fetchObjects() off of the dataSource and it should work (unlike 4.5).
        WODisplayGroup dg = new WODisplayGroup();
        dg.setDataSource(dataSource);
        dg.fetch(); // Have to fetch in the array, go figure.
        return dg.allObjects();
    }

    /**
     * Creates a detail data source for a given enterprise
     * object and a relationship key. These types of datasources
     * can be very handy when you are displaying a list of objects
     * a la D2W style and then some objects are added or removed
     * from the relationship. If an array datasource were used
     * then the list would not reflect the changes made, however
     * the detail data source will reflect changes made to the
     * relationship.<br/>
     * Note: the relationship key does not have to be an eo
     * relationship, instead it just has to return an array of
     * enterprise objects.
     * @param object that has the relationship
     * @param key relationship key
     * @return detail data source for the given object-key pair.
     */
    public static EODetailDataSource dataSourceForObjectAndKey(EOEnterpriseObject object, String key) {
        EODetailDataSource eodetaildatasource = new EODetailDataSource(EOClassDescription.classDescriptionForEntityName(object.entityName()), key);
        eodetaildatasource.qualifyWithRelationshipKey(key, object);
        return eodetaildatasource;
    }
    
    /**
     * Creates a new, editable instance of the supplied object. Takes into account if the object is
     * newly inserted, lives in a shared context and can either create a peer or nested context.
     *
     * @param eo object for the new instance
     * @param createNestedContext true, if we should create a nested context (otherwise we create a peer context)
     *
     * @return new EO in new editing context
     */
     public static EOEnterpriseObject editableInstanceOfObject(EOEnterpriseObject eo,
     		boolean createNestedContext) {

     	if(eo == null) throw new IllegalArgumentException("EO can't be null");
     	EOEditingContext ec = eo.editingContext();

     	if(ec == null) throw new IllegalArgumentException("EO must live in an EC");

        boolean isNewObject = ERXEOControlUtilities.isNewObject(eo);

        // Check for old EOF bug and do nothing as we can't localInstance
        // anything here
        if (ERXProperties.webObjectsVersionAsDouble() < 5.21d && isNewObject) {
            return eo;
        }

        EOEnterpriseObject localObject = eo;

        // Either we have an already saved object or a new one and create a nested context.
        // Otherwise (new object and a peer) we should probably raise, but simple return the EO
        if((isNewObject && createNestedContext) || !isNewObject) {
    		// create either peer or nested context
     		EOEditingContext newEc = ERXEC.newEditingContext(createNestedContext
     				? ec : ec.parentObjectStore());
     		ec.lock();
     		try {
     			newEc.lock();
     			try {
     				if(ec instanceof EOSharedEditingContext
     	     				|| ec.sharedEditingContext() == null) {
     	     			newEc.setSharedEditingContext(null);
     	     		}
     	     		localObject = EOUtilities.localInstanceOfObject(newEc, eo);
     				localObject.willRead();
     			} finally {
     				newEc.unlock();
     			}
     		} finally {
     			ec.unlock();
     		}
     	}
      	return localObject;
     }

    /**
     * This has one advantage over the standard EOUtilites
     * method of first checking if the editingcontexts are
     * equal before creating a fault for the object in the
     * editing context.
     * @param ec editing context to get a local instance of the object in
     * @param eo object to get a local copy of
     * @return enterprise object local to the passed in editing contex
     */
    public static EOEnterpriseObject localInstanceOfObject(EOEditingContext ec, EOEnterpriseObject eo) {
        return eo != null && ec != null && eo.editingContext() != null && !ec.equals(eo.editingContext()) ?
        EOUtilities.localInstanceOfObject(ec, eo) : eo;
    }
    
    /**
     * Determines if two EOs are equal by comparing their EOGlobalIDs.  This does not require
     * the two EOs to be in the same EOEditingContext and will be safe when either is or both are
     * null.  This does not test the two EOs for content equality.
     * @param firstEO first EO to compare
     * @param secondEO second EO to compare
     * @return true if firstEO and secondEO correspond to the same object or if both are null.  false otherwise.
     */
    public static boolean eoEquals(EOEnterpriseObject firstEO, EOEnterpriseObject secondEO) {
        boolean result = false;
        
        if ( firstEO == secondEO ) {
            result = true;
        }
        else if (firstEO != null && secondEO != null) {
            final EOEditingContext firstContext = firstEO.editingContext();
            final EOEditingContext secondContext = secondEO.editingContext();
            
            if (firstContext != null && secondContext != null) {
                final EOGlobalID firstGID = firstContext.globalIDForObject(firstEO);
                final EOGlobalID secondGID = secondContext.globalIDForObject(secondEO);
                
                result = firstGID.equals(secondGID);
            }
        }
        
        return result;        
    }

    /**
     * Creates an enterprise object for the given entity
     * name by first looking up the class description
     * of the entity to create the enterprise object.
     * The object is then inserted into the editing context
     * and returned.
     * @param editingContext editingContext to insert the created object into
     * @param entityName name of the entity to be created.
     * @return created and inserted enterprise object
     */
    public static EOEnterpriseObject createAndInsertObject(EOEditingContext editingContext, String entityName) {
        return createAndInsertObject(editingContext,entityName,null);
    }

    /**
     * Creates an enterprise object for the given entity
     * name by first looking up the class description
     * of the entity to create the enterprise object.
     * The object is then inserted into the editing context
     * and returned.
     * @param editingContext editingContext to insert the created object into
     * @param entityName name of the entity to be created.
     * @param objectInfo dictionary of values pushed onto the object
     *		before being inserted into the editing context.
     * @return created and inserted enterprise object
     */
    public static EOEnterpriseObject createAndInsertObject(EOEditingContext editingContext, String entityName, NSDictionary objectInfo) {
        if (log.isDebugEnabled())
            log.debug("Creating object of type: " + entityName);
        EOClassDescription cd=EOClassDescription.classDescriptionForEntityName(entityName);
        if (cd==null)
            throw new RuntimeException("Could not find class description for entity named "+entityName);
        EOEnterpriseObject newEO=cd.createInstanceWithEditingContext(editingContext,null);
        if (objectInfo != null)
            newEO.takeValuesFromDictionary(objectInfo);
        editingContext.insertObject(newEO);
        return newEO;
    }

    /**
     * Creates an object using the utility method <code>createEO</code>
     * from this utility class. After creating the enterprise object it
     * is added to the relationship of the enterprise object passed in.
     * For instance:<br/>
     * <code>createAndAddObjectToRelationship(ec, foo, "toBars", "Bar", dictValues);</code><br/>
     * <br/>
     * will create an instance of Bar, set all of the key-value pairs
     * from the dictValues dictionary, insert it into an editing context
     * and then add it to both sides of the realtionship "toBars" off of
     * the enterprise object foo.
     *
     * @param editingContext editing context to create the object in
     * @param source enterprise object to whose relationship the newly created
     *		object will be added.
     * @param relationshipName relationship name of the enterprise object
     *		that is passed in to which the newly created eo should be
     *		added.
     * @param destinationEntityName name of the entity of the object to be created.
     * @param objectInfo dictionary of values to be set on the newly created
     *		object before it is inserted into the editing context.
     * @return the newly created enterprise object
     */
    public static EOEnterpriseObject createAndAddObjectToRelationship(EOEditingContext editingContext,  EOEnterpriseObject source, String relationshipName, String destinationEntityName, NSDictionary objectInfo) {
        
        EOEnterpriseObject newEO=createAndInsertObject(editingContext, destinationEntityName, objectInfo);
        EOEnterpriseObject eoBis = editingContext!=source.editingContext() ?
            EOUtilities.localInstanceOfObject(editingContext,source) : source;
        eoBis.addObjectToBothSidesOfRelationshipWithKey(newEO, relationshipName);
        return newEO;
    }

    /**
     * Adds an object to another objects relationship. Has
     * the advantage of ensuring that the added object is
     * in the same editing context as the reference object.
     * @param addedObject object to be added to the relationship
     * @param referenceObject object that has the relationship
     * @param key relationship key
     */
    public static void addObjectToObjectOnBothSidesOfRelationshipWithKey(EOEnterpriseObject addedObject, EOEnterpriseObject referenceObject, String key) {
        EOEditingContext referenceEc = referenceObject.editingContext();
        EOEditingContext ec = addedObject.editingContext();
        EOEnterpriseObject copy = addedObject;
        if (referenceEc != ec) {
            copy = EOUtilities.localInstanceOfObject(referenceEc, addedObject);
        }
        referenceObject.addObjectToBothSidesOfRelationshipWithKey(copy, key);
    }

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
     * Clears snapshot the relaationship of a given enterprise so it will be read again when next accessed.
     * @param eo enterprise object
     * @param relationshipName relationship name
     */
    public static void clearSnapshotForRelationshipNamed(EOEnterpriseObject eo, String relationshipName) {
        EOEditingContext ec = eo.editingContext();
        EOModel model = EOUtilities.entityForObject(ec, eo).model();
        EOGlobalID gid = ec.globalIDForObject(eo);
        EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(model, ec);
        EODatabase database = dbc.database();
        database.recordSnapshotForSourceGlobalID(null, gid, relationshipName);
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
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
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
    public static NSArray primaryKeysMatchingQualifier(EOEditingContext ec,
                                                       String entityName,
                                                       EOQualifier eoqualifier,
                                                       NSArray sortOrderings) {
        EOFetchSpecification fs = ERXEOControlUtilities.primaryKeyFetchSpecificationForEntity(ec,
                                                                                              entityName,
                                                                                              eoqualifier,
                                                                                              sortOrderings,
                                                                                              null);
        return ec.objectsWithFetchSpecification(fs);
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
            throw new IllegalStateException("Found multiple objects for entity \"" + entity.name() + " with primary key value: " + primaryKeyValue);
        return eos.count() == 1 ? (EOEnterpriseObject)eos.lastObject() : null;
    }
    
    /**
     * Returns an {@link com.webobjects.foundation.NSArray NSArray} containing the objects from the resulting rows starting
     * at start and stopping at end using a custom SQL, derived from the SQL
     * which the {@link com.webobjects.eocontrol.EOFetchSpecification EOFetchSpecification} would use normally {@link com.webobjects.eocontrol.EOFetchSpecification#setHints(NSDictionary) setHints()}
     *
     * @param ec editingcontext to fetch objects into
     * @param spec fetch specification for the fetch
     * @param start
     * @param end
     *
     * @return objects in the given range
     */
    public static NSArray objectsInRange(EOEditingContext ec, EOFetchSpecification spec, int start, int end) {
        EOSQLExpression sql = ERXEOAccessUtilities.sqlExpressionForFetchSpecification(ec, spec, start, end);
        NSDictionary hints = new NSDictionary(sql, "EOCustomQueryExpressionHintKey");
        spec.setHints(hints);

        return ec.objectsWithFetchSpecification(spec);
    }

    /**
     * Returns an {@link com.webobjects.foundation.NSArray NSArray} containing the primary keys from the resulting rows starting
     * at start and stopping at end using a custom SQL, derived from the SQL
     * which the {@link com.webobjects.eocontrol.EOFetchSpecification EOFetchSpecification} would use normally {@link com.webobjects.eocontrol.EOFetchSpecification#setHints(NSDictionary) setHints()}
     *
     * @param ec editingcontext to fetch objects into
     * @param spec fetch specification for the fetch
     * @param start
     * @param end
     *
     * @return primary keys in the given range
     */
    public static NSArray primaryKeyValuesInRange(EOEditingContext ec, EOFetchSpecification spec, int start, int end) {
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
        NSArray pkNames = (NSArray) entity.primaryKeyAttributes().valueForKey("name");
        spec.setFetchesRawRows(true);
        spec.setRawRowKeyPaths(pkNames);
        EOSQLExpression sql = ERXEOAccessUtilities.sqlExpressionForFetchSpecification(ec, spec, start, end);
        NSDictionary hints = new NSDictionary(sql, "EOCustomQueryExpressionHintKey");
        spec.setHints(hints);
        return ec.objectsWithFetchSpecification(spec);
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
    public static Integer objectCountWithQualifier(EOEditingContext ec, String entityName, EOQualifier qualifier) {
        EOAttribute attribute = EOEnterpriseObjectClazz.objectCountAttribute();
        return (Integer)_aggregateFunctionWithQualifierAndAggregateAttribute(ec, entityName, qualifier, attribute);
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
    public static Integer objectCountUniqueWithQualifierAndAttribute(EOEditingContext ec, String entityName, EOQualifier qualifier, String attributeName) {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        EOAttribute attribute = entity.attributeNamed(attributeName);
        EOAttribute aggregateAttribute = EOEnterpriseObjectClazz.objectCountUniqueAttribute(attribute);
        return (Integer)_aggregateFunctionWithQualifierAndAggregateAttribute(ec, entityName, qualifier, aggregateAttribute);
    }

    /**
     * Returns the number of objects in the database with the qualifier and counting attribute.  This implementation
     * queries the database directly without loading the objects into memory.
     *
     * @param ec the editing context
     * @param entityName the name of the entity
     * @param qualifier the qualifier to filter with
     * @param aggregateAttribute the attribute that contains the "count(*)" definition
     * @return the number of objects
     */
    public static Object _aggregateFunctionWithQualifierAndAggregateAttribute(EOEditingContext ec, String entityName, EOQualifier qualifier, EOAttribute aggregateAttribute) {
		EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
		EOModel model = entity.model();
		EODatabaseContext databaseContext = EODatabaseContext.registeredDatabaseContextForModel(model, ec);
		databaseContext.lock();
		try {
			EOSQLExpressionFactory sqlFactory = databaseContext.adaptorContext().adaptor().expressionFactory();
			EOQualifier schemaBasedQualifier = entity.schemaBasedQualifier(qualifier);
			EOFetchSpecification fetchSpec = new EOFetchSpecification(entity.name(), schemaBasedQualifier, null);
			fetchSpec.setFetchesRawRows(true);

			EOSQLExpression sqlExpr = sqlFactory.expressionForEntity(entity);
			sqlExpr.prepareSelectExpressionWithAttributes(new NSArray(aggregateAttribute), false, fetchSpec);

			EOAdaptorChannel adaptorChannel = databaseContext.availableChannel().adaptorChannel();
			if (!adaptorChannel.isOpen()) {
				adaptorChannel.openChannel();
			}
			Object aggregateValue = null;
			NSArray attributes = new NSArray(aggregateAttribute);
			adaptorChannel.evaluateExpression(sqlExpr);
			try {
				adaptorChannel.setAttributesToFetch(attributes);
				NSDictionary row = adaptorChannel.fetchRow();
				if (row != null) {
					aggregateValue = row.objectForKey(aggregateAttribute.name());
				}
			}
			finally {
				adaptorChannel.cancelFetch();
			}
		    return aggregateValue;
		}
		finally {
			databaseContext.unlock();
		}
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
     * @param fetchSpecificationName of fetch specification from which the qualifier will be derived
     * @return aggregate result of the fuction call
     */
    public static Number aggregateFunctionWithQualifier(EOEditingContext ec,
                                                        String entityName,
                                                        String attributeName,
                                                        String function,
                                                        String fetchSpecificationName,
                                                        NSDictionary bindings) {
       EOFetchSpecification fs = fetchSpecificationNamedWithBindings(entityName,
                                                                     fetchSpecificationName,
                                                                     bindings);
        return aggregateFunctionWithQualifier(ec, entityName, attributeName, function, fs.qualifier());
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
    	Number number = null;
    	Object obj = _aggregateFunctionWithQualifier(ec, entityName, attributeName, function, Number.class, "i", qualifier);
    	if (obj instanceof Number) {
    		number = (Number)obj;
    	}
    	return number;
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
    public static NSTimestamp aggregateTimestampWithQualifier(EOEditingContext ec,
            String entityName,
            String attributeName,
            String function,
            EOQualifier qualifier) {
    	NSTimestamp timestamp = null;
    	Object obj = _aggregateFunctionWithQualifier(ec, entityName, attributeName, function, NSTimestamp.class, null, qualifier);
    	if (obj instanceof NSTimestamp) {
    		timestamp = (NSTimestamp)obj;
    	}
    	return timestamp;
    }

    public static Object _aggregateFunctionWithQualifier(EOEditingContext ec,
                                                        String entityName,
                                                        String attributeName,
                                                        String function,
                                                        Class valueClass,
                                                        String valueType,
                                                        EOQualifier qualifier) {
        EOAttribute attribute = ERXEOAccessUtilities.createAggregateAttribute(ec,
                                                                              function,
                                                                              attributeName,
                                                                              entityName, valueClass, valueType);

        return ERXEOControlUtilities._aggregateFunctionWithQualifierAndAggregateAttribute(ec, entityName, qualifier, attribute);
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
        NSArray sharedEos = sharedObjectsForEntityNamed(entityName);
        if (sharedEos != null) {
            filtered = EOQualifier.filteredArrayWithQualifier(sharedEos, qualifier);
        } else {
            log.warn("Unable to find any shared objects for entity name: " + entityName);
        }
        return filtered != null ? filtered : NSArray.EmptyArray;
    }

    /**
     * Gets all of the shared objects for a given entity name. Note that
     * if the shared objects for the corresponding model have not been loaded yet,
     * this method will trigger their loading (unless automatic loading has been disabled via a call to
     * {@link com.webobjects.eoaccess.EODatabaseContext#setSharedObjectLoadingEnabled(boolean)}).
     * Returns an empty array if no shared objects were found.
     * @param entityName name of the shared entity
     * @return array of bound shared objects for the given entity name, or an empty array
     */
    public static NSArray sharedObjectsForEntityNamed(String entityName) {
        EOSharedEditingContext sharedEC = EOSharedEditingContext.defaultSharedEditingContext();
        NSArray sharedEos = (NSArray)sharedEC.objectsByEntityName().objectForKey(entityName);
        if (sharedEos == null) { //call registeredDatabaseContextForModel() to trigger loading the model's shared EOs (if set to happen automatically), then try again
            EOEntity entity = ERXEOAccessUtilities.entityNamed(sharedEC, entityName);
            EODatabaseContext.registeredDatabaseContextForModel(entity.model(), sharedEC);
            sharedEos = (NSArray)sharedEC.objectsByEntityName().objectForKey(entityName);
        }

        if (sharedEos == null) {
            log.warn("Unable to find any shared objects for the entity named: " + entityName);
        }
        return sharedEos != null ? sharedEos : NSArray.EmptyArray;
    }

    /**
     * Fetches a shared enterprise object for a given fetch
     * specification from the default shared editing context.
     * @param fetchSpec name of the fetch specification on the
     *		shared object.
     * @param entityName name of the shared entity
     * @return the shared enterprise object fetch by the fetch spec named.
     */
    public static EOEnterpriseObject sharedObjectWithFetchSpec(String entityName, String fetchSpec) {
        return EOUtilities.objectWithFetchSpecificationAndBindings(EOSharedEditingContext.defaultSharedEditingContext(), entityName, fetchSpec, null);
    }

    public static NSArray sharedObjectsWithFetchSpecificationNamed(String entityName, String fetchSpecName) {
        NSArray result = null;

        EOSharedEditingContext sharedEditingContext = EOSharedEditingContext.defaultSharedEditingContext();
        NSDictionary objectsByFetchSpecName = (NSDictionary)sharedEditingContext.objectsByEntityNameAndFetchSpecificationName().objectForKey(entityName);
        if( objectsByFetchSpecName != null ) {
            result = (NSArray)objectsByFetchSpecName.objectForKey(fetchSpecName);
        }

        if( result == null ) {
            EOEntity entity = EOUtilities.entityNamed(sharedEditingContext, entityName);
            EOFetchSpecification fetchSpecification = entity.fetchSpecificationNamed(fetchSpecName);

            sharedEditingContext.bindObjectsWithFetchSpecification(fetchSpecification, fetchSpecName);

            objectsByFetchSpecName = (NSDictionary)sharedEditingContext.objectsByEntityNameAndFetchSpecificationName().objectForKey(entityName);
            if( objectsByFetchSpecName != null ) { //shouldn't be
                result = (NSArray)objectsByFetchSpecName.objectForKey(fetchSpecName);
            }
        }

        if( result == null ) {
            result = NSArray.EmptyArray;
        }

        return result;
    }
    /**
     * Gets the shared enterprise object with the given primary
     * from the default shared editing context. This has the
     * advantage of not requiring a roundtrip to the database to
     * lookup the object. But, it will fetch if the object is not
     * found in the default shared editing context.
     * @param entityName name of the entity
     * @param primaryKey primary key of object to be found
     * @return the shared object registered in the default shared editing context
     */
    public static EOEnterpriseObject sharedObjectWithPrimaryKey(String entityName, Object primaryKey) {
        EOKeyGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(entityName, new Object[] {primaryKey});
        EOSharedEditingContext sharedEditingContext = EOSharedEditingContext.defaultSharedEditingContext();
        EOEnterpriseObject eo = sharedEditingContext.objectForGlobalID(gid);
        return (eo != null) ? eo : EOUtilities.objectWithPrimaryKeyValue(sharedEditingContext,
                                                     entityName,
                                                     primaryKey);
    }
    
    /**
     * Gets a fetch specification from a given entity. If qualifier binding variables
     * are passed in then the fetchspecification is cloned and the binding variables
     * are substituted returning a fetch specification that can be used.
     * @param entityName name of the entity that the fetch specification is bound to
     * @param fetchSpecificationName name of the fetch specification
     * @param bindings dictionary of qualifier bindings
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
     * @return new primary key dictionary or null if one of the properties is
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
     * {@link #newPrimaryKeyForObjectFromClassProperties(EOEnterpriseObject)} and if that returns null,
     * {@link #newPrimaryKeyDictionaryForEntityNamed(EOEditingContext, String)}
     * @return new primary key dictionary or null if a failure occured.
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
     * @return a dictionary containing a new primary key for the given
     *		entity.
     */
    public static NSDictionary newPrimaryKeyDictionaryForEntityNamed(EOEditingContext ec, String entityName) {
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
        EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        NSDictionary primaryKey = null;
        try {
            dbContext.lock();
            EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
            if (!adaptorChannel.isOpen())
                adaptorChannel.openChannel();
            NSArray arr = adaptorChannel.primaryKeysForNewRowsWithEntity(1, entity);
            if (arr != null) {
                primaryKey = (NSDictionary)arr.lastObject();
            } else {
                log.warn("Could not get primary key array for entity: " + entityName);
            }
        } catch (Exception e) {
            log.error("Caught exception when generating primary key for entity: " + entityName, e);
            throw new NSForwardException(e);
        } finally {
            dbContext.unlock();
        }
        return primaryKey;
    }

    /**
     * Returns the propertylist-encoded string representation of the primary key for
     * a given object.
     * @param eo object to get the primary key for.
     * @return string representation of the primary key of the
     *		object.
     */
    public static String primaryKeyStringForObject(EOEnterpriseObject eo) {
        return _stringForPrimaryKey(primaryKeyObjectForObject(eo));
    }

    /**
     * Returns the propertylist-encoded string representation of the primary key for
     * a given object. Made public only for ERXGenericRecord.
     * @param pk the primary key
     * @return string representation of the primary key.
     */
    // FIXME ak this PK creation is too byzantine
    public static String _stringForPrimaryKey(Object pk) {
        if(pk == null)
            return null;
        if(pk instanceof String || pk instanceof Number) {
            return pk.toString();
        }
        return NSPropertyListSerialization.stringFromPropertyList(pk);
    }

    /**
     * Returns the decoded dictionary for an propertylist encoded string representation
     * of the primary key for a given object.
     * @param eo object to get the primary key for.
     * @return string representation of the primary key of the
     *		object.
     */
    public static NSDictionary primaryKeyDictionaryForString(EOEditingContext ec, String entityName, String string) {
        if(string == null)
            return null;
        if(string.trim().length()==0) {
            return NSDictionary.EmptyDictionary;
        }
        
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
        NSArray pks = entity.primaryKeyAttributes();
        NSMutableDictionary pk = new NSMutableDictionary();
        try {
            Object rawValue = NSPropertyListSerialization.propertyListFromString(string);
            if(rawValue instanceof NSArray) {
                int index = 0;
                for(Enumeration e = ((NSArray)rawValue).objectEnumerator(); e.hasMoreElements();) {
                    EOAttribute attribute = (EOAttribute)pks.objectAtIndex(index++);
                    Object value = e.nextElement();
                    if(attribute.adaptorValueType() == EOAttribute.AdaptorDateType && !(value instanceof NSTimestamp)) {
                        value = new NSTimestampFormatter("%Y-%m-%d %H:%M:%S %Z").parseObject((String)value);
                    }
                    value = attribute.validateValue(value);
                    pk.setObjectForKey(value, attribute.name());
                    if(pks.count() == 1) {
                        break;
                    }
                }
            } else {
                EOAttribute attribute = (EOAttribute)pks.objectAtIndex(0);
                Object value = rawValue;
                value = attribute.validateValue(value);
                pk.setObjectForKey(value, attribute.name());
            }
            return pk;
        } catch (Exception ex) {
            throw new NSForwardException(ex, "Error while parsing primary key: " + string);
        }
    }
    
    /**
     * Returns either the single object the PK consist of or the NSArray of its values if the key is compound.
     * @param eo object to get the primary key for.
     * @return single object or NSArray
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
     * Calls <code>objectsWithQualifierFormat(ec, entityName, qualifierFormat, args, prefetchKeyPaths, includeNewObjects, false)</code>
     * <p>
     * That is, passes false for <code>includeNewObjectsInParentEditingContexts</code>.  This method exists
     * to maintain API compatability.
     */
    public static NSArray objectsWithQualifierFormat(EOEditingContext ec,
                                                     String entityName,
                                                     String qualifierFormat,
                                                     NSArray args,
                                                     NSArray prefetchKeyPaths,
                                                     boolean includeNewObjects) {
        return objectsWithQualifierFormat(ec, entityName, qualifierFormat, args, prefetchKeyPaths, includeNewObjects, false);
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
     * @param includeNewObjectsInParentEditingContexts option to include newly inserted objects in parent editing contexts
     * @return array of objects matching the constructed qualifier
     */
    public static NSArray objectsWithQualifierFormat(EOEditingContext ec,
                                                     String entityName,
                                                     String qualifierFormat,
                                                     NSArray args,
                                                     NSArray prefetchKeyPaths,
                                                     boolean includeNewObjects,
                                                     boolean includeNewObjectsInParentEditingContexts) {
        EOQualifier qual = EOQualifier.qualifierWithQualifierFormat(qualifierFormat, args);
        return objectsWithQualifier(ec, entityName, qual, prefetchKeyPaths, includeNewObjects, includeNewObjectsInParentEditingContexts);
    }

    /**
     * Calls objectsWithQualifier(ec, entityName, qualifier, prefetchKeyPaths, includeNewObjects, false).
     * <p>
     * That is, passes false for <code>includeNewObjectsInParentEditingContexts</code>.  This method
     * exists to maintain API compatability.
     */
    public static NSArray objectsWithQualifier(EOEditingContext ec,
                                               String entityName,
                                               EOQualifier qualifier,
                                               NSArray prefetchKeyPaths,
                                               boolean includeNewObjects) {
        return objectsWithQualifier(ec, entityName, qualifier, prefetchKeyPaths, includeNewObjects, false);
    }
    
    /**
     * Utility method used to fetch an array of objects given a qualifier. Also
     * has support for filtering the newly inserted objects in the passed editing context or
     * any parent editing contexts as well as specifying prefetching key paths.
     * @param ec editing context to fetch it into
     * @param entityName name of the entity
     * @param qualifier qualifier
     * @param prefetchKeyPaths prefetching key paths
     * @param includeNewObjects option to include newly inserted objects in the result set
     * @param includeNewObjectsInParentEditingContexts option to include newly inserted objects in parent editing
     *        contexts.  if true, the editing context lineage is explored, any newly-inserted objects matching the
     *        qualifier are collected and faulted down through all parent editing contexts of ec.
     * @return array of objects matching the constructed qualifier
     */
    // ENHANCEME: This should handle entity inheritance for in memory filtering
    public static NSArray objectsWithQualifier(EOEditingContext ec,
                                               String entityName,
                                               EOQualifier qualifier,
                                               NSArray prefetchKeyPaths,
                                               boolean includeNewObjects,
                                               boolean includeNewObjectsInParentEditingContexts) {
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

        if ( includeNewObjectsInParentEditingContexts && ! (ec.parentObjectStore() instanceof EOObjectStoreCoordinator) ) {
            final NSMutableArray parentEditingContexts = new NSMutableArray();
            EOObjectStore objectStore = ec.parentObjectStore();
            NSArray objects = NSArray.EmptyArray;
            int i;

            while ( ! (objectStore instanceof EOObjectStoreCoordinator) ) {
                final EOEditingContext theEC = (EOEditingContext)objectStore;

                parentEditingContexts.addObject(theEC);
                objectStore = theEC.parentObjectStore();
            }

            i = parentEditingContexts.count();

            while ( i-- > 0 ) {
                final EOEditingContext theEC = (EOEditingContext)parentEditingContexts.objectAtIndex(i);
                final NSArray insertedObjects = ERXArrayUtilities.objectsWithValueForKeyPath(theEC.insertedObjects(), entityName, "entityName");
                final NSArray objectsMatchingQualifier = EOQualifier.filteredArrayWithQualifier(insertedObjects, qualifier);

                // fault the previous batch down
                objects = EOUtilities.localInstancesOfObjects(theEC, objects);

                if ( objectsMatchingQualifier.count() > 0 )
                    objects = objects.arrayByAddingObjectsFromArray(objectsMatchingQualifier);
            }

            if ( objects.count() > 0 ) {
                objects = EOUtilities.localInstancesOfObjects(ec, objects);
                if ( result == null )
                    result = new NSMutableArray();
                result.addObjectsFromArray(objects);
            }
        }

        EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, null);
        fs.setPrefetchingRelationshipKeyPaths(prefetchKeyPaths);
        NSArray fromDb = ec.objectsWithFetchSpecification(fs);
        if (result != null)
            result.addObjectsFromArray(fromDb);
        return result != null ? result : fromDb;
    }

    /** faults every EO in the qualifiers into the specified editingContext. This is important for 
     * in memory filtering and eo comparision
     * @param ec
     * @param q
     */
    public static EOQualifier localInstancesInQualifier(EOEditingContext ec, EOQualifier q) {
        if (q instanceof EOKeyValueQualifier) {
            EOKeyValueQualifier q1 = (EOKeyValueQualifier)q;
            if (q1.value() instanceof EOEnterpriseObject) {
                EOEnterpriseObject eo = (EOEnterpriseObject)q1.value();
                if (eo.editingContext() != ec && !ERXExtensions.isNewObject(eo)) {
                    eo = EOUtilities.localInstanceOfObject(ec, eo);
                    EOKeyValueQualifier qual = new EOKeyValueQualifier(q1.key(), q1.selector(), eo);
                    return qual;
                }
            }
        } else if (q instanceof EOAndQualifier || q instanceof EOOrQualifier) {
            NSArray oriQualifiers = (NSArray)NSKeyValueCoding.Utility.valueForKey(q, "qualifiers");
            NSMutableArray qualifiers = new NSMutableArray();
            for (int i = oriQualifiers.count(); i-- > 0;) {
                EOQualifier qual = (EOQualifier)oriQualifiers.objectAtIndex(i);
                qualifiers.addObject(localInstancesInQualifier(ec, qual));
            }
            return new EOAndQualifier(qualifiers);
        } else if (q instanceof EONotQualifier) {
            EONotQualifier qNot = (EONotQualifier)q;
            EOQualifier qual = localInstancesInQualifier(ec, qNot.qualifier());
            return new EONotQualifier(qual);
        } 
        return q;
        
    }

    /** returns a NSArray containing EOGlobalIDs from the provided eos.
     * @param eos the NSArray of EOEnterpriseObjects
     * @return a NSArray of EOGlobalIDs
     */
    public static NSArray globalIDsForObjects(NSArray eos) {
        int c = eos != null ? eos.count() : 0;
        NSMutableArray ids = new NSMutableArray(c);
        for (int i = 0; i < c; i++) {
            EOEnterpriseObject eo = (EOEnterpriseObject)eos.objectAtIndex(i);
            EOEditingContext ec = eo.editingContext();
            EOGlobalID gid = ec.globalIDForObject(eo);
            ids.addObject(gid);
        }
        return ids;
    }

    /**
     * Aggregate method for <code>EOEditingContext.objectForGlobalID()</code>.
     *
     * @see com.webobjects.eocontrol.EOEditingContext#objectForGlobalID(EOGlobalID)
     */
    public static NSArray objectsForGlobalIDs(final EOEditingContext ec, final NSArray globalIDs) {
        NSArray result = null;

        if ( globalIDs != null && globalIDs.count() > 0 ) {
            final NSMutableArray a = new NSMutableArray();
            final Enumeration e = globalIDs.objectEnumerator();

            while ( e.hasMoreElements() ) {
                final EOGlobalID theGID = (EOGlobalID)e.nextElement();
                final EOEnterpriseObject theObject = ec.objectForGlobalID(theGID);

                if ( theObject != null )
                    a.addObject(theObject);
            }

            result = a.immutableClone();
        }

        return result != null ? result : NSArray.EmptyArray;
    }
    
    /** returns a NSArray containing EOEnterpriseObjects (actually faults...) for the provided EOGlobalIDs.
     * @param ec the EOEditingContext in which the EOEnterpriseObjects should be faulted
     * @param gids the EOGlobalIDs
     * @return a NSArray of EOEnterpriseObjects
     */
    public static NSArray faultsForGlobalIDs(EOEditingContext ec, NSArray gids) {
        int c = gids.count();
        NSMutableArray a = new NSMutableArray(c);
        for (int i = 0; i < c; i++) {
            EOGlobalID gid = (EOGlobalID)gids.objectAtIndex(i);
            EOEnterpriseObject eo = ec.faultForGlobalID(gid, ec);
            a.addObject(eo);
        }
        return a;
    }
    
    public static NSArray faultsForRawRowsFromEntity(EOEditingContext ec, NSArray primKeys, String entityName) {
        int c = primKeys.count();
        NSMutableArray a = new NSMutableArray(c);
        for (int i = 0; i < c; i++) {
            NSDictionary pkDict = (NSDictionary)primKeys.objectAtIndex(i);
            EOEnterpriseObject eo = ec.faultForRawRow(pkDict, entityName);
            a.addObject(eo);
        }
        return a;
    }

    /**
     * Tests if an enterprise object is a new object by
     * looking to see if it is in the list of inserted
     * objects for the editing context or if the editing
     * context is null.<br/>
     * <br/>
     * Note: An object that has been deleted will have it's
     * editing context set to null which means this method
     * would report true for an object that has been deleted
     * from the database.
     * @param eo enterprise object to check
     * @return true or false depending on if the object is a
     *		new object.
     */
    public static boolean isNewObject(EOEnterpriseObject eo) {
        if (eo.editingContext() == null) return true;

        EOGlobalID gid = eo.editingContext().globalIDForObject(eo);
        return gid.isTemporary();
    }

    /** Returns the name from the root entity from the EOEnterpriseObject
     *
     * @param eo the EOEnterpriseObject from which to the the root entity
     *
     * @return the name from the root entity from the EOEnterpriseObject
     */
    public static String rootEntityName(EOEnterpriseObject eo) {
        EOEntity entity = rootEntity(eo);
        return entity.name();
    }

    /** Returns the root entity from the EOEnterpriseObject
     *
     * @param eo the EOEnterpriseObject from which to the the root entity
     *
     * @return the root entity from the EOEnterpriseObject
     */
    public static EOEntity rootEntity(EOEnterpriseObject eo) {
        EOEntity entity = ERXEOAccessUtilities.entityForEo(eo);
        while (entity.parentEntity() != null) {
            entity = entity.parentEntity();
        }
        return entity;
    }
    
    /**
     * Smarter version of normal <code>saveChanges()</code> method that corrects issues with
     * <code>flushCaches()</code> needing to be called on objects in the parent context when
     * committing the child context to the parent. If the editing context is a child of the object-store
     * coordinator---that is, it's not a nested context---this method
     * behaves exactly the same as <code>EOEditingContext.saveChanges()</code>. Otherwise,
     * this method looks over the changed objects in <code>ec</code> (<code>updatedObjects()</code>,
     * <code>insertedObjects()</code> and <code>deletedObjects()</code>).  The changed objects lists
     * are filtered for instances of <code>ERXGenericRecord</code>.  The order of operations then becomes:
     *
     * <ol>
     * <li> Call <code>processRecentChanges()</code> on the child context to propogate changes.
     * <li> Lock the parent editing context.
     * <li> On the deleted objects list in the child editing context, call <code>flushCaches()</code> on
     *      each corresponding EO in the parent context.
     * <li> Unlock the parent editing context.
     * <li> Call <code>saveChanges()</code> on the child, commiting the child changes to the
     *      parent editing context.
     * <li> Lock the parent editing context.
     * <li> On the objects that were updated or inserted in the child, call <code>flushCaches()</code>
     *      on each corresponding EO in the parent context.
     * <li> Unlock the parent editing context.
     * </ol>
     *
     * <p>
     *
     * The order of operations is a bit peculiar: flush deletes, save, flush inserts and updates.  This
     * is done because deletes must be flushed because there may be dependant computed state that needs to
     * be reset.  But following the delete being committed, the relationships to other objects cannot be
     * relied upon so it isn't reliable to call flushCaches after the commit.  It's not entirely correct to
     * flush the deletes like this, but it's the best we can do.
     *
     * <p>
     *
     * This works around an issue in EOF that we don't get a merge notification when a child
     * EC saves to its parent.  Because there's no merge notification, <code>flushCaches()</code>
     * isn't called by the EC delegate and we're essentially screwed vis-a-vis resetting computed
     * state.
     *
     * <p>
     *
     * This method assumes that the <code>ec</code> is locked before this method is invoked, but this
     * method will take the lock on the parent editing context if the <code>ec</code> is a
     * nested context before and after the save in order to get the objects and to flush caches on
     * them.
     *
     * @param ec editing context to save
     */
    public static void saveChanges(final EOEditingContext ec) {
        final EOObjectStore parentObjectStore = ec.parentObjectStore();
        final boolean isNestedEditingContext = ! (parentObjectStore instanceof EOObjectStoreCoordinator);
        final EOEditingContext parentEC = isNestedEditingContext ? (EOEditingContext)parentObjectStore : null;
        NSArray insertedObjectGIDs = null;
        NSArray updatedObjectGIDs = null;

        // we don't need to lock ec because we can assume that we're locked before this method
        // is called, but we do need to lock our parent

        if ( isNestedEditingContext ) {
            ec.processRecentChanges();  // need to do this to make sure the updated objects list is current

            try {
                parentEC.lock();

                final NSArray insertedFlushableObjects = ERXArrayUtilities.arrayBySelectingInstancesOfClass(ec.insertedObjects(), ERXGenericRecord.class);
                final NSArray updatedFlushableObjects = ERXArrayUtilities.arrayBySelectingInstancesOfClass(ec.updatedObjects(), ERXGenericRecord.class);
                final NSArray deletedFlushableObjects = ERXArrayUtilities.arrayBySelectingInstancesOfClass(ec.deletedObjects(), ERXGenericRecord.class);

                insertedObjectGIDs = globalIDsForObjects(insertedFlushableObjects);
                updatedObjectGIDs = globalIDsForObjects(updatedFlushableObjects);

                if ( deletedFlushableObjects.count() > 0 ) {
                    final NSArray deletedFlushableGIDs = globalIDsForObjects(deletedFlushableObjects);
                    final NSArray deletedObjectsToFlushInParent = objectsForGlobalIDs(parentEC, deletedFlushableGIDs);

                    if ( log.isDebugEnabled() ) {
                        log.debug("saveChanges: before save to child context " + ec +
                                  ", need to flush caches on deleted objects in parent context " + parentEC + ": " + deletedObjectsToFlushInParent);
                    }
                    deletedObjectsToFlushInParent.makeObjectsPerformSelector(ERXGenericRecord.FlushCachesSelector, null);
                }
            }
            finally {
                parentEC.unlock();
            }
        }

        ec.saveChanges();

        if ( isNestedEditingContext ) {
            // we can assume insertedObjectGIDs and updatedObjectGIDs are non null.  if we execute this branch, they're at
            // least empty arrays.

            if ( insertedObjectGIDs.count() > 0 || updatedObjectGIDs.count() > 0 ) {
                try {
                    parentEC.lock();

                    final NSArray insertedObjectsInParent = objectsForGlobalIDs(parentEC, insertedObjectGIDs);
                    final NSArray updatedObjectsInParent = objectsForGlobalIDs(parentEC, updatedObjectGIDs);

                    if ( log.isDebugEnabled() ) {
                        if ( insertedObjectsInParent.count() > 0 ) {
                            log.debug("saveChanges: before save to child context " + ec +
                                      ", need to flush caches on inserted objects in parent context " + parentEC + ": " + insertedObjectsInParent);
                        }
                        if ( updatedObjectsInParent.count() > 0 ) {
                            log.debug("saveChanges: before save to child context " + ec +
                                      ", need to flush caches on updated objects in parent context " + parentEC + ": " + updatedObjectsInParent);
                        }
                    }

                    insertedObjectsInParent.makeObjectsPerformSelector(ERXGenericRecord.FlushCachesSelector, null);
                    updatedObjectsInParent.makeObjectsPerformSelector(ERXGenericRecord.FlushCachesSelector, null);
                }
                finally {
                    parentEC.unlock();
                }
            }
        }
    }


}
