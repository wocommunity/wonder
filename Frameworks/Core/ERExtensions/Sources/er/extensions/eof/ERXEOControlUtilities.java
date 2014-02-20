package er.extensions.eof;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eoaccess.EOUtilities.MoreThanOneException;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EODetailDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFaulting;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOObjectStore;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.eof.ERXEOAccessUtilities.DatabaseContextOperation;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXKeyValueCodingUtilities;
import er.extensions.jdbc.ERXSQLHelper;
import er.extensions.validation.ERXValidationException;
import er.extensions.validation.ERXValidationFactory;

/**
 * Collection of EOF utility method centered around
 * EOControl.
 * 
 * EOControl provides infrastructure for creating and managing enterprise objects.
 */
public class ERXEOControlUtilities {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERXEOControlUtilities.class);


    /**
     * Provides the same functionality as the equivalent method
     * in {@link com.webobjects.eoaccess.EOUtilities EOUtilities}
     * except it will use the localInstanceOfObject
     * method from this utilities class which has a few enhancements.
     * @param <T> data type of enterprise objects
     *
     * @param ec editing context to pull local object copies
     * @param eos array of enterprise objects
     * @return an array of copies of local objects
     */
    public static <T extends EOEnterpriseObject> NSArray<T> localInstancesOfObjects(EOEditingContext ec, NSArray<T> eos) {
        if (eos == null)
            throw new RuntimeException("ERXUtilites: localInstancesOfObjects: Array is null");
        if (ec == null)
            throw new RuntimeException("ERXUtilites: localInstancesOfObjects: EditingContext is null");
        if (eos.isEmpty()) {
            return NSArray.emptyArray();
        }
        NSMutableArray<T> localEos = new NSMutableArray<T>(eos.count());
        for (Enumeration<T> e = eos.objectEnumerator(); e.hasMoreElements();) {
            localEos.addObject(localInstanceOfObject(ec, e.nextElement()));
        }
        return localEos;
    }

    /**
     * Simple utility method that will convert an array
     * of enterprise objects into an EOArrayDataSource.<br/>
     * <br/>
     * Note that the data source that is constructed uses the
     * class description and editing context of the first object
     * of the array.
     * @param array collection of objects to be turned into a
     *		data source
     * @return an array data source corresponding to the array
     *		of objects passed in.
     */
    public static EOArrayDataSource dataSourceForArray(NSArray<? extends EOEnterpriseObject> array) {
        EOArrayDataSource dataSource = null;
        if (array != null && array.count() > 0) {
            EOEnterpriseObject eo = array.objectAtIndex(0);
            dataSource = new EOArrayDataSource(eo.classDescription(), eo.editingContext());
            dataSource.setArray(array);
        }
        return dataSource;
    }

    /**
     * Simple utility method that will convert an array
     * of enterprise objects into an EOArrayDataSource.
     * <p>
     * Note that the data source that is constructed uses the
     * class description and editing context of the first object
     * of the array.
     * @param ec editing context for the data source
     * @param entityName entity name
     * @param array collection of objects to be turned into a
     *		data source
     * @return an array data source corresponding to the array
     *		of objects passed in.
     */
    public static EOArrayDataSource dataSourceForArray(EOEditingContext ec, String entityName, NSArray array) {
    	EOArrayDataSource dataSource = new EOArrayDataSource(EOClassDescription.classDescriptionForEntityName(entityName), ec);
    	if (array != null && array.count() > 0) {
    		dataSource.setArray(array);
    	}
    	return dataSource;
    }

    /**
     * Converts a data source into an array.
     * @param dataSource data source to be converted
     * @return array of objects that the data source represents
     */
    public static NSArray arrayFromDataSource(EODataSource dataSource) {
        return dataSource.fetchObjects();
    }

    /**
     * Creates a detail data source for a given enterprise
     * object and a relationship key. These types of data sources
     * can be very handy when you are displaying a list of objects
     * a la D2W style and then some objects are added or removed
     * from the relationship. If an array data source were used
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
     * @param <T> data type of the enterprise object
     *
     * @param eo object for the new instance
     * @param createNestedContext true, if we should create a nested context (otherwise we create a peer context)
     *
     * @return new EO in new editing context
     */
     public static <T extends EOEnterpriseObject> T editableInstanceOfObject(T eo, 
     		boolean createNestedContext) {
     	
     	if(eo == null) throw new IllegalArgumentException("EO can't be null");
     	EOEditingContext ec = eo.editingContext();
     	
     	if(ec == null) throw new IllegalArgumentException("EO must live in an EC");
     	
        boolean isNewObject = ERXEOControlUtilities.isNewObject(eo);

        T localObject = eo;
        
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
     	     		localObject = (T) EOUtilities.localInstanceOfObject(newEc, eo);
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
     * @param <T> data type of the enterprise object
     * @param ec editing context to get a local instance of the object in
     * @param eo object to get a local copy of
     * @return enterprise object local to the passed in editing context
     */
	public static <T extends EOEnterpriseObject> T localInstanceOfObject(EOEditingContext ec, T eo) {
        return eo != null && ec != null && eo.editingContext() != null && !ec.equals(eo.editingContext()) ?
        (T)EOUtilities.localInstanceOfObject(ec, eo) : eo;
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
     * @param <T> data type of the enterprise object
     * @param ec editingContext to insert the created object into
     * @param eoClass class of the enterprise object to be created
     * @return created and inserted enterprise object of type T
     */
    public static <T extends EOEnterpriseObject> T createAndInsertObject(EOEditingContext ec, Class<T> eoClass) {
    	return (T)createAndInsertObject(ec, eoClass.getSimpleName());
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
     * of the entity to create the enterprise object and
     * then adding values from the dictionary. The object is
     * then inserted into the editing context and returned.
     *
     * @param editingContext editingContext to insert the created object into
     * @param entityName name of the entity to be created.
     * @param objectInfo dictionary of values pushed onto the object
     *		before being inserted into the editing context.
     * @return created and inserted enterprise object
     */
    public static EOEnterpriseObject createAndInsertObject(EOEditingContext editingContext,
                                                           String entityName,
                                                           NSDictionary objectInfo) {
        if (log.isDebugEnabled())
            log.debug("Creating object of type: " + entityName);
        EOClassDescription cd=EOClassDescription.classDescriptionForEntityName(entityName);
        if (cd==null)
            throw new RuntimeException("Could not find class description for entity named "+entityName);
        EOEnterpriseObject newEO=cd.createInstanceWithEditingContext(editingContext,null);
        editingContext.insertObject(newEO);
        if (objectInfo != null)
            newEO.takeValuesFromDictionary(objectInfo);
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
     * and then add it to both sides of the relationship "toBars" off of
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
    public static EOEnterpriseObject createAndAddObjectToRelationship(EOEditingContext editingContext,
                                                                      EOEnterpriseObject source,
                                                                      String relationshipName,
                                                                      String destinationEntityName,
                                                                      NSDictionary objectInfo) {
        EOEnterpriseObject newEO = createAndInsertObject(editingContext, destinationEntityName, objectInfo);
        EOEnterpriseObject eoBis = editingContext != source.editingContext() ?
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
    public static void addObjectToObjectOnBothSidesOfRelationshipWithKey(EOEnterpriseObject addedObject,
                                                                         EOEnterpriseObject referenceObject,
                                                                         String key) {
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
            NSArray<EOGlobalID> gids = new NSArray<EOGlobalID>(ec.globalIDForObject(eo));
            ec.invalidateObjectsWithGlobalIDs(gids);
        }
    }

	/**
	 * Sets the fetch time stamp of the eo's ec to now to ensure fresh data. and
	 * refreshes the EO (which merges latest database snapshots with current
	 * unsaved changes if we have unsaved changes)
	 *
	 * @param eo
	 *            EO to be refreshed
	 */
	public static void refreshObject(EOEnterpriseObject eo) {
		eo.editingContext().setFetchTimestamp(System.currentTimeMillis());
		eo.editingContext().refreshObject(eo);
	}

    /**
     * Clears snapshot the relationship of a given enterprise so it will be read again when next accessed.
     * @param eo enterprise object
     * @param relationshipName relationship name
     */
    public static void clearSnapshotForRelationshipNamed(EOEnterpriseObject eo, String relationshipName) {
        EOEditingContext ec = eo.editingContext();
        EOModel model = EOUtilities.entityForObject(ec, eo).model();
        EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(model, ec);
        EODatabase database = dbc.database();
        ERXEOControlUtilities.clearSnapshotForRelationshipNamedInDatabase(eo, relationshipName, database);
    }

    /**
     * Clears snapshot the relationship of a given enterprise so it will be read again when next accessed.
     * @param eo enterprise object
     * @param relationshipName relationship name
     * @param database database object
     */
    public static void clearSnapshotForRelationshipNamedInDatabase(EOEnterpriseObject eo, String relationshipName, EODatabase database) {
        EOEditingContext ec = eo.editingContext();
        EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) ec.rootObjectStore();
        osc.lock();
        try {
	        EOGlobalID gid = ec.globalIDForObject(eo);
	        database.recordSnapshotForSourceGlobalID(null, gid, relationshipName);
	        Object o = eo.storedValueForKey(relationshipName);
	        boolean needRefresh = false;
	        if(o instanceof EOFaulting) {
	        	EOFaulting toManyArray = (EOFaulting)o;
	            if (!toManyArray.isFault()) {
	            	EOFaulting tmpToManyArray = (EOFaulting)((EOObjectStoreCoordinator)ec.rootObjectStore()).arrayFaultWithSourceGlobalID(gid, relationshipName, ec);
	            	toManyArray.turnIntoFault(tmpToManyArray.faultHandler());
	            	needRefresh = true;
	            }
	        } else {
	        	EOFaulting tmpToManyArray = (EOFaulting)((EOObjectStoreCoordinator)ec.rootObjectStore()).arrayFaultWithSourceGlobalID(gid, relationshipName, ec);
	        	eo.takeStoredValueForKey(tmpToManyArray, relationshipName);
	        	needRefresh = true;
	        }
	        if(needRefresh && (eo instanceof ERXEnterpriseObject)) {
	        	((ERXEnterpriseObject)eo).flushCaches();
	        }
        }
        finally {
        	osc.unlock();
        }
    }

    /**
     * Constructs a fetch specification that will only fetch the primary
     * keys for a given qualifier.
     * @param ec editing context, only used to determine the entity
     * @param entityName name of the entity, only used to determine the entity
     * @param eoqualifier to construct the fetch specification with
     * @param sortOrderings array of sort orderings to sort the result set with.
     * @param additionalKeys array of additional key paths to construct the
     *		raw rows key paths to fetch.
     * @return fetch specification that can be used to fetch primary keys for
     * 		a given qualifier and sort orderings.
     */
    @SuppressWarnings("unchecked")
    public static EOFetchSpecification primaryKeyFetchSpecificationForEntity(EOEditingContext ec,
                                                                      String entityName,
                                                                      EOQualifier eoqualifier,
                                                                      NSArray<EOSortOrdering> sortOrderings,
                                                                      NSArray<String> additionalKeys) {
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
        EOFetchSpecification fs = new EOFetchSpecification(entityName, eoqualifier, sortOrderings);
        fs.setFetchesRawRows(true);
        NSMutableArray<String> keys = new NSMutableArray<String>(entity.primaryKeyAttributeNames());
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
                                                       NSArray<EOSortOrdering> sortOrderings) {
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
     * @param primaryKeyValue primary key value. Compound primary keys are given as NSDictionaries.
     * @param prefetchingKeyPaths key paths to fetch off of the eo
     * @return enterprise object matching the given value
     */
    public static EOEnterpriseObject objectWithPrimaryKeyValue(EOEditingContext ec,
                                                               String entityName,
                                                               Object primaryKeyValue,
                                                               NSArray prefetchingKeyPaths) {
    	return ERXEOControlUtilities.objectWithPrimaryKeyValue(ec, entityName, primaryKeyValue, prefetchingKeyPaths, true);
    }
    
    /**
     * Fetches an enterprise object based on a given primary key value.
     * This method has an advantage over the standard EOUtilities method
     * in that you can specify prefetching key paths as well as refreshing
     * the snapshot of the given object
     * @param ec editing context to fetch into
     * @param entityName name of the entity
     * @param primaryKeyValue primary key value. Compound primary keys are given as NSDictionaries.
     * @param prefetchingKeyPaths key paths to fetch off of the eo
     * @param refreshRefetchedObjects if true, the object will be refetched and refreshed
     * @return enterprise object matching the given value or null if none is found
     * @throws IllegalStateException if the entity has a compound key and only one key is provided or 
     * if more than one object is found matching the value.
     */
    @SuppressWarnings("unchecked")
	public static EOEnterpriseObject objectWithPrimaryKeyValue(EOEditingContext ec,
                                                               String entityName,
                                                               Object primaryKeyValue,
                                                               NSArray prefetchingKeyPaths, boolean refreshRefetchedObjects) {
    	return objectWithPrimaryKeyValue(ec, entityName, primaryKeyValue, prefetchingKeyPaths, refreshRefetchedObjects, false);
    }

    /**
     * Fetches an enterprise object based on a given primary key value.
     * This method has an advantage over the standard EOUtilities method
     * in that you can specify prefetching key paths as well as refreshing
     * the snapshot of the given object.
     * @param ec editing context to fetch into
     * @param entityName name of the entity
     * @param primaryKeyValue primary key value. Compound primary keys are given as NSDictionaries.
     * @param prefetchingKeyPaths key paths to prefetch for the eo
     * @param refreshRefetchedObjects if true, the object will be refetched and refreshed
     * @param throwIfMissing if true, an exception is thrown for a missing object
     * @return enterprise object matching the given value or null if none is found
     * @throws IllegalStateException if the entity has a compound key and only one key is provided or 
     * if more than one object is found matching the value.
     * @throws EOObjectNotAvailableException if throwIfMissing is true and the object is missing
     */
    @SuppressWarnings("unchecked")
    public static EOEnterpriseObject objectWithPrimaryKeyValue(EOEditingContext ec,
                                                               String entityName,
                                                               Object primaryKeyValue,
                                                               NSArray prefetchingKeyPaths, boolean refreshRefetchedObjects, boolean throwIfMissing) {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        NSDictionary<String, Object> values;
        if(primaryKeyValue instanceof NSDictionary) {
            values = (NSDictionary<String, Object>)primaryKeyValue;
        }  else {
            if (entity.primaryKeyAttributes().count() != 1) {
                throw new IllegalStateException("The entity '" + entity.name() + "' has a compound primary key and cannot be used with a single primary key value.");
            }
            values = new NSDictionary<String, Object>(primaryKeyValue, entity.primaryKeyAttributeNames().lastObject());
        }
        NSArray eos;
        if (prefetchingKeyPaths == null && !refreshRefetchedObjects) {
        	EOGlobalID gid = entity.globalIDForRow(values);
        	EOEnterpriseObject eo = ec.faultForGlobalID(gid, ec);
        	if (throwIfMissing) {
        		eo.willRead();
        	}
        	eos = new NSArray<EOEnterpriseObject>(eo);
        }
        else {
	        EOQualifier qualfier = EOQualifier.qualifierToMatchAllValues(values);
	        EOFetchSpecification fs = new EOFetchSpecification(entityName, qualfier, null);
	        // Might as well get fresh stuff
	        fs.setRefreshesRefetchedObjects(refreshRefetchedObjects);
	        if (prefetchingKeyPaths != null) {
	        	fs.setPrefetchingRelationshipKeyPaths(prefetchingKeyPaths);
	        }
	        eos = ec.objectsWithFetchSpecification(fs);
        }
        if (eos.count() > 1) {
        	throw new MoreThanOneException("Found multiple objects for the entity '" + entity.name() + "' with primary key value: " + primaryKeyValue);
        }
        if (eos.count() == 0) {
        	if (throwIfMissing) {
        		throw new EOObjectNotAvailableException("There was no '" + entity.name() + "' found with the id '" + primaryKeyValue + "'.");
        	}
        	return null;
        }
        return (EOEnterpriseObject)eos.lastObject();
    }
    
    /**
     * Returns an {@link com.webobjects.foundation.NSArray NSArray} containing the objects from the resulting rows starting
     * at start and stopping at end using a custom SQL, derived from the SQL
     * which the {@link com.webobjects.eocontrol.EOFetchSpecification EOFetchSpecification} would use normally {@link com.webobjects.eocontrol.EOFetchSpecification#setHints(NSDictionary) setHints()}
     * @param <T> data type of the enterprise objects
     *
     * @param ec editing context to fetch objects into
     * @param spec fetch specification for the fetch
     * @param start the starting row number
     * @param end the last row number
     *
     * @return objects in the given range
     */
    public static <T extends EOEnterpriseObject> NSArray<T> objectsInRange(EOEditingContext ec, EOFetchSpecification spec, int start, int end) {
    	return objectsInRange(ec, spec, start, end, true);
    }
    
    /**
     * Returns an {@link com.webobjects.foundation.NSArray NSArray} containing the objects from the resulting rows starting
     * at start and stopping at end using a custom SQL, derived from the SQL
     * which the {@link com.webobjects.eocontrol.EOFetchSpecification EOFetchSpecification} would use normally {@link com.webobjects.eocontrol.EOFetchSpecification#setHints(NSDictionary) setHints()}
     * @param <T> data type of the enterprise objects
     *
     * @param ec editing context to fetch objects into
     * @param spec fetch specification for the fetch
     * @param start the starting row number
     * @param end the last row number
     * @param rawRowsForCustomQueries if true, raw rows will be returned from the fetch when there is a custom query
     *
     * @return objects in the given range
     */
    public static <T extends EOEnterpriseObject> NSArray<T> objectsInRange(EOEditingContext ec, EOFetchSpecification spec, int start, int end, boolean rawRowsForCustomQueries) {
		NSArray result;
		if (spec.hints() == null || spec.hints().isEmpty() || spec.hints().valueForKey(EODatabaseContext.CustomQueryExpressionHintKey) == null) {
			// no hints on the fs
			
			// This used to turn the EO's into faults and then fetch the faults. The problem
			// with that is that for abstract entities, this would fetch the objects one-by-one
			// to determine what the correct EOGlobalID is for each subentity. Instead, I've
			// changed this to created a global ID for each row (which supports being a guess)
			// and then fetch the objects with the global IDs.
			NSArray primKeys = ERXEOControlUtilities.primaryKeyValuesInRange(ec, spec, start, end);
			EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
			NSMutableArray<EOGlobalID> gids = new NSMutableArray<EOGlobalID>();
			for (Object obj : primKeys) {
				NSDictionary pkDict = (NSDictionary) obj;
				EOGlobalID gid = entity.globalIDForRow(pkDict);
				gids.addObject(gid);
			}
			NSMutableArray objects = ERXEOGlobalIDUtilities.fetchObjectsWithGlobalIDs(ec, gids, spec.refreshesRefetchedObjects());

			if (spec.prefetchingRelationshipKeyPaths() != null && spec.prefetchingRelationshipKeyPaths().count() > 0) {
				ERXBatchFetchUtilities.batchFetch(objects, spec.prefetchingRelationshipKeyPaths(), ! spec.refreshesRefetchedObjects());
			}

			// HP: There is no guarantee that the array of objects has the same order of the array of
			// globalIDs. We must ensure the array of objects is ordered as expected.
			ensureSortOrdering(ec, gids, objects);
			
			result = objects.immutableClone();
		}
		else {
			// we have hints, use them
			
			EOModel model = EOModelGroup.defaultGroup().entityNamed(spec.entityName()).model();
			ERXSQLHelper sqlHelper = ERXSQLHelper.newSQLHelper(ec, model.name());
			Object hint = spec.hints().valueForKey(EODatabaseContext.CustomQueryExpressionHintKey);
			String sql = sqlHelper.customQueryExpressionHintAsString(hint);
			sql = sqlHelper.limitExpressionForSQL(null, spec, sql, start, end);
			
			if (rawRowsForCustomQueries) {
				result = EOUtilities.rawRowsForSQL(ec, model.name(), sql, null);
			}
			else {
				EOFetchSpecification fs = new EOFetchSpecification(spec.entityName(), null, null);
				fs.setHints(new NSDictionary(sql, EODatabaseContext.CustomQueryExpressionHintKey));
				result = ec.objectsWithFetchSpecification(fs);
			}
		}
		return result;
	}
    
    /**
     * Returns an {@link com.webobjects.foundation.NSArray NSArray} containing the primary keys from the resulting rows starting
     * at <i>start</i> and stopping at <i>end</i> using a custom SQL, derived from the SQL
     * which the {@link com.webobjects.eocontrol.EOFetchSpecification EOFetchSpecification} would use normally {@link com.webobjects.eocontrol.EOFetchSpecification#setHints(NSDictionary) setHints()}
     *
     * @param ec editing context to fetch objects into
     * @param spec fetch specification for the fetch
     * @param start the starting row number
     * @param end the last row number
     *
     * @return primary keys in the given range
     */
    public static NSArray<NSDictionary<String, Object>> primaryKeyValuesInRange(EOEditingContext ec, EOFetchSpecification spec, int start, int end) {
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, spec.entityName());
        NSArray<String> pkNames = entity.primaryKeyAttributeNames();
        EOFetchSpecification clonedFetchSpec = (EOFetchSpecification)spec.clone();
        clonedFetchSpec.setFetchesRawRows(true);
        clonedFetchSpec.setRawRowKeyPaths(pkNames);
        if (clonedFetchSpec instanceof ERXFetchSpecification) {
            // remove any range setting as we will explicitly set start and end limit
            ((ERXFetchSpecification)clonedFetchSpec).setFetchRange(null);
        }
        EOSQLExpression sql = ERXEOAccessUtilities.sqlExpressionForFetchSpecification(ec, clonedFetchSpec, start, end);
        NSDictionary<String, EOSQLExpression> hints = new NSDictionary<String, EOSQLExpression>(sql, EODatabaseContext.CustomQueryExpressionHintKey);
        clonedFetchSpec.setHints(hints);
        return ec.objectsWithFetchSpecification(clonedFetchSpec);
    }
    
    /**
     * Returns the number of objects matching the given
     * qualifier for a given entity name. Implementation
     * wise this method will generate the correct SQL to only
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
     * qualifier for a given entity name. This method will generate
     * the correct SQL to perform a count and not to fetch the objects.
     *
     * @param ec editing context to use for the count qualification
     * @param entityName name of the entity to fetch
     * @param qualifier to find the matching objects
     * @param attributeName name of attribute in same entity to consider in order to determine uniqueness
     * @return number of matching objects
     */
    public static Integer objectCountUniqueWithQualifierAndAttribute(EOEditingContext ec, String entityName, EOQualifier qualifier, String attributeName) {
        EOEntity entity = EOUtilities.entityNamed(ec, entityName);
        EOAttribute attribute = entity.attributeNamed(attributeName);
        EOAttribute aggregateAttribute = EOEnterpriseObjectClazz.objectCountUniqueAttribute(attribute);
        return (Integer)_aggregateFunctionWithQualifierAndAggregateAttribute(ec, entityName, qualifier, aggregateAttribute);
    }

	/**
	 * Counts the objects in a toMany relationship in an effective way.
	 *
	 * If the relationship fault has already been fired,
	 * it just returns the array count. If relationship is a fault, it looks for a relationship snapshot array, and if that
	 * is present, returns the count of the snapshot array. Finally if it is a fault and no snapshot array
	 * exists, performs a count in the database.
	 *
	 * @param object
	 *            the EOEnterpriseObject whose relationship is being counted.
	 * @param relationshipName
	 *            the name of the relationship being counted.
	 * @return count of related objects in the relationship named {@code relationshipName}
	 * @throws NullPointerException if either {@code object} or {@code relationshipName} are null
	 * @throws IllegalArgumentException if {@code relationshipName} is not a toMany relationship key 
	 */
	public static Integer objectCountForToManyRelationship(EOEnterpriseObject object, final String relationshipName) {

		try {
			// --- (1) Simple case of a new unsaved object ---
			// Does not exist in the db, does not have a snapshot in EOdb.
			if (isNewObject(object)) {
				NSArray<EOEnterpriseObject> relatedObjects = (NSArray<EOEnterpriseObject>) object.storedValueForKey(relationshipName);
				// --- (1) return result
				return Integer.valueOf(relatedObjects.count());
			}

			// Get relationship object which may, or may not, be a fault
			Object relationshipValue = object.storedValueForKey(relationshipName);

			// --- (2) Case where the relationship fault has already been fired
			if (!EOFaultHandler.isFault(relationshipValue)) {
				NSArray relatedItems = (NSArray) relationshipValue;
				// --- (2) return result
				return Integer.valueOf(relatedItems.count());
			}
		}
		catch (NullPointerException e) {
			if (object == null) {
				NullPointerException e1 = new NullPointerException("object argument cannot be null");
				e1.initCause(e);
				throw e1;
			} else if (relationshipName == null) {
				NullPointerException e1 = new NullPointerException("relationshipName argument cannot be null");
				e1.initCause(e);
				throw e1;
			}
			// Otherwise NPE here means the attribute returned null value, indicating it
			// is not a toMany since a toMany always returns an NSArray object
			throw new IllegalArgumentException("The attribute named '" + relationshipName + "' in the entity named '" + object.entityName() + "' is not a toMany relationship! Expected an NSArray, but got null.");
		}
		catch (ClassCastException e) {
			// CCE here means the attribute returned some non-null value other than an
			// NSArray instance value, indicating it is not a toMany
			throw new IllegalArgumentException("The attribute named '" + relationshipName + "' in the entity named '" + object.entityName() 
					+ "' is not a toMany relationship! Expected an NSArray, but got a " 
					+ object.storedValueForKey(relationshipName).getClass().getName(), e);
		}
		
		final EOEditingContext ec = object.editingContext();
		EOEntity entity = ERXEOAccessUtilities.entityForEo(object);
		EORelationship relationship = entity.relationshipNamed(relationshipName);

		// Fail in the event that the relationship fault is not a toMany
		if (!relationship.isToMany()) {
			// Happens if toOne key used and the to-one is a fault
			throw new IllegalArgumentException("The attribute named '" + relationshipName
					+ "' in the entity named '" + object.entityName() +"' is not a toMany relationship!");
		}

		// --- (3) Case of a fault and a snapshot exists to provide a count
		final EOGlobalID gid = ec.globalIDForObject(object);
		String modelName = entity.model().name();
		final EODatabaseContext dbc = EOUtilities.databaseContextForModelNamed(ec, modelName);

		NSArray toManySnapshot = ERXEOAccessUtilities.executeDatabaseContextOperation(dbc, 2,
				new DatabaseContextOperation<NSArray>() {
					public NSArray execute(EODatabaseContext databaseContext) throws Exception {
						// Search for and return the snapshot
						return dbc.snapshotForSourceGlobalID(gid, relationshipName, ec.fetchTimestamp());
					}
				});

		// Null means that a relationship snapshot array was not found in EODBCtx or EODB.
		if (toManySnapshot != null) {
			// --- (3) return result
			return Integer.valueOf(toManySnapshot.count());
		}

		// Default case
		// --- (4) Case where relationship has not been faulted, and no snapshot array exists
		EOQualifier q = EODatabaseDataSource._qualifierForRelationshipKey(relationshipName, object);
		// --- (4) return result
		return objectCountWithQualifier(ec, relationship.destinationEntity().name(), q);
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
        EODatabaseContext dbc = EODatabaseContext.registeredDatabaseContextForModel(model, ec);
        Object aggregateValue = null;
        
        dbc.lock();
        try {
            aggregateValue = __aggregateFunctionWithQualifierAndAggregateAttribute(dbc, ec, entityName, qualifier, aggregateAttribute);
        }
        catch (Exception localException) {
            if (dbc._isDroppedConnectionException(localException)) {
                try {
                    dbc.database().handleDroppedConnection();
                    aggregateValue = __aggregateFunctionWithQualifierAndAggregateAttribute(dbc, ec, entityName, qualifier, aggregateAttribute);
                }
                catch(Exception ex) {
                    throw NSForwardException._runtimeExceptionForThrowable(ex);
                }
            }
            else {
                throw NSForwardException._runtimeExceptionForThrowable(localException);
            }
        }
        finally {
            dbc.unlock();
        }
        return aggregateValue;
    }
    
    private static Object __aggregateFunctionWithQualifierAndAggregateAttribute(EODatabaseContext databaseContext, EOEditingContext ec, String entityName, EOQualifier qualifier, EOAttribute aggregateAttribute) {
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);

        EOSQLExpressionFactory sqlFactory = databaseContext.adaptorContext().adaptor().expressionFactory();
        EOQualifier schemaBasedQualifier = entity.schemaBasedQualifier(qualifier);
        EOFetchSpecification fetchSpec = new EOFetchSpecification(entity.name(), schemaBasedQualifier, null);
        fetchSpec.setFetchesRawRows(true);

        if (sqlFactory == null) {
        	/* if there is no expression factory we have no choice but to fetch */
        	NSArray<?> array = ec.objectsWithFetchSpecification(fetchSpec);
        	if (aggregateAttribute == EOEnterpriseObjectClazz.objectCountAttribute()) {
        		return array.count();
        	}
        	if (aggregateAttribute.name().startsWith("p_objectCountUnique")) {
        		String attributeName = aggregateAttribute.name().substring("p_objectCountUnique".length());
        		return ERXArrayUtilities.arrayWithoutDuplicateKeyValue(array, attributeName).count();
        	}
        	throw new UnsupportedOperationException("Unable to perform aggregate function for attribute " + aggregateAttribute.name());
        }
        
        EOSQLExpression sqlExpr = sqlFactory.expressionForEntity(entity);
        sqlExpr.prepareSelectExpressionWithAttributes(new NSArray<EOAttribute>(aggregateAttribute), false, fetchSpec);

        EOAdaptorChannel adaptorChannel = databaseContext.availableChannel().adaptorChannel();
        if (!adaptorChannel.isOpen()) {
            adaptorChannel.openChannel();
        }
        Object aggregateValue = null; 
        NSArray<EOAttribute> attributes = new NSArray<EOAttribute>(aggregateAttribute);
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

    /**
     * Computes an aggregate function for a given attribute
     * restricted by a given qualifier. For instance
     * select MAX(AGE) from User where name like 'M*'
     *
     * @param ec editing context used for the fetch
     * @param entityName name of the entity
     * @param attributeName attribute for the function to be performed on
     * @param function name, ie MAX, MIN, AVG, etc.
     * @param fetchSpecificationName name of a fetch specification
     * @param bindings bindings for the fetch specification
     *
     * @return aggregate result of the function call
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
     * @return aggregate result of the function call
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
     * and value. This has the benefit of not requiring a database
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
     * and value. This has the benefit of not requiring a database
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
     *
     * @param fetchSpec specification on the shared object
     * @param entityName name of the shared entity
     * @return the shared enterprise object fetch by the fetch spec named.
     */
    public static EOEnterpriseObject sharedObjectWithFetchSpec(String entityName, String fetchSpec) {
        return EOUtilities.objectWithFetchSpecificationAndBindings(EOSharedEditingContext.defaultSharedEditingContext(), entityName, fetchSpec, null);
    }

    /**
     * Fetches a shared enterprise object from the default shared editing context
     * given the name of a fetch specification.
     *
     * @param fetchSpecName name of the fetch specification on the shared object.
     * @param entityName name of the shared entity
     * @return the shared enterprise object fetch by the fetch spec named.
     */
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
     * lookup the object. But it will fetch if the object is not
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
     * are passed in then the fetch specification is cloned and the binding variables
     * are substituted, returning a fetch specification that can be used.
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
    @SuppressWarnings("unchecked")
    public static NSDictionary<String, Object> newPrimaryKeyDictionaryForObjectFromClassProperties(EOEnterpriseObject eo) {
        EOEditingContext ec = eo.editingContext();
        EOEntity entity = EOUtilities.entityNamed(ec, eo.entityName());
        NSArray<String> pkAttributes = entity.primaryKeyAttributeNames();
        int count = pkAttributes.count();
        NSMutableDictionary<String, Object> nsmutabledictionary = new NSMutableDictionary<String, Object>(count);
        NSArray classPropertyNames = entity.classPropertyNames();
        while (count-- != 0) {
            String key = pkAttributes.objectAtIndex(count);
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
     * {@link #newPrimaryKeyDictionaryForObjectFromClassProperties(EOEnterpriseObject)} and if that returns null,
     * {@link #newPrimaryKeyDictionaryForEntityNamed(EOEditingContext, String)}
     * @param eo enterprise object to create keys for
     * @return new primary key dictionary or null if a failure occurred.
     */
    public static NSDictionary<String, Object> newPrimaryKeyDictionaryForObject(EOEnterpriseObject eo) {
        NSDictionary<String, Object> dict = newPrimaryKeyDictionaryForObjectFromClassProperties(eo);
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
    @SuppressWarnings("unchecked")
    public static NSDictionary<String, Object> newPrimaryKeyDictionaryForEntityNamed(EOEditingContext ec, String entityName) {
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
        EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        NSDictionary<String, Object> primaryKey = null;
        try {
            dbContext.lock();
            
            boolean willRetryAfterHandlingDroppedConnection = true;
            while (willRetryAfterHandlingDroppedConnection) {
            	try {
		            EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
		            if (!adaptorChannel.isOpen()) {
		                adaptorChannel.openChannel();
		            }
		            NSArray<NSDictionary<String, Object>> arr = adaptorChannel.primaryKeysForNewRowsWithEntity(1, entity);
		            if (arr != null) {
		                primaryKey = arr.lastObject();
		            } else {
		                log.warn("Could not get primary key array for entity: " + entityName);
		            }
		            willRetryAfterHandlingDroppedConnection = false;
	            }
	            catch (Exception localException) {
	            	if (willRetryAfterHandlingDroppedConnection && 
	            			dbContext._isDroppedConnectionException(localException)) {
	                    try {
	                    	dbContext.database().handleDroppedConnection();
	                        
	                    }
	                    catch(Exception ex) {
	                        throw NSForwardException._runtimeExceptionForThrowable(ex);
	                    }
	                }
	                else {
	                	throw NSForwardException._runtimeExceptionForThrowable(localException);
	                }
	            }
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
     * Returns the propertylist-encoded string representation of the global ID.
     * @param gid the global id of the object to get the primary key for.
     * @return string representation of the primary key of the object.
     */
    public static String primaryKeyStringForGlobalID(EOKeyGlobalID gid) {
    	if(gid.keyValuesArray().count() > 1) {
    		return _stringForPrimaryKey(gid.keyValuesArray());
    	}
        return _stringForPrimaryKey(gid.keyValuesArray().lastObject());
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
     *
     * @return string representation of the primary key of the
     *		object.
     */
    @SuppressWarnings("unchecked")
    public static NSDictionary<String, Object> primaryKeyDictionaryForString(EOEditingContext ec, String entityName, String string) {
        if(string == null) {
            return null;
        }
        
        if(string.trim().length()==0) {
            return NSDictionary.EmptyDictionary;
        }
        
        EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
        NSArray<EOAttribute> pks = entity.primaryKeyAttributes();
        NSMutableDictionary<String, Object> pk = new NSMutableDictionary<String, Object>();
        try {
            Object rawValue = NSPropertyListSerialization.propertyListFromString(string);
            if(rawValue instanceof NSArray) {
                int index = 0;
                for(Enumeration e = ((NSArray)rawValue).objectEnumerator(); e.hasMoreElements();) {
                    EOAttribute attribute = pks.objectAtIndex(index++);
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
            	if(rawValue instanceof NSMutableData) {
                	// AK: wtf!! I got an exception 
                	// java.lang.IllegalArgumentException: Attempt to create an EOGlobalID for the entity "Asset" with a primary key component of type com.webobjects.foundation.NSMutableData instead of type com.webobjects.foundation.NSData!
                	// so this is a lame attempt to fix it.
                	
            		rawValue = new NSData((NSMutableData)rawValue);
            	}
                EOAttribute attribute = pks.objectAtIndex(0);
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
     * Returns the decoded global id for an propertylist encoded string representation
     * of the primary key for a given object.
     */
    @SuppressWarnings("unchecked")
    public static EOGlobalID globalIDForString(EOEditingContext ec, String entityName, String string) {
    	NSDictionary<String, Object> values = primaryKeyDictionaryForString(ec, entityName, string);
    	EOEntity entity = ERXEOAccessUtilities.entityNamed(ec, entityName);
        NSArray<String> pks = entity.primaryKeyAttributeNames();
        EOGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(entityName, values.objectsForKeys(pks, null).objects());
    	return gid;
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
     * Calls <code>objectsWithQualifierFormat(ec, entityName, qualifierFormat, args, prefetchKeyPaths, includeNewObjects, false)</code>.
     * 
     * That is, passes false for <code>includeNewObjectsInParentEditingContexts</code>.  This method exists
     * to maintain API compatibility.
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
     *
     * That is, passes false for <code>includeNewObjectsInParentEditingContexts</code>.  This method
     * exists to maintain API compatibility.
     */
    public static NSArray objectsWithQualifier(EOEditingContext ec,
                                               String entityName,
                                               EOQualifier qualifier,
                                               NSArray prefetchKeyPaths,
                                               boolean includeNewObjects) {
        return objectsWithQualifier(ec, entityName, qualifier, prefetchKeyPaths, includeNewObjects, false);
    }

    public static NSArray objectsWithQualifier(EOEditingContext _editingContext, String _entityName, EOQualifier _qualifier, NSArray _prefetchKeyPaths, boolean _includeNewObjects, boolean _includeNewObjectsInParentEditingContext) {
      return ERXEOControlUtilities.objectsWithQualifier(_editingContext, _entityName, _qualifier, _prefetchKeyPaths, _includeNewObjects, _includeNewObjectsInParentEditingContext, false, false);
    }
    
    /**
     * Utility method used to fetch an array of objects given a qualifier. Also
     * has support for filtering the newly inserted, updated, and deleted objects in the 
     * passed editing context or any parent editing contexts as well as specifying prefetching 
     * key paths.  Note that only NEW objects are supported in parent editing contexts.
     * 
     * @param editingContext editing context to fetch it into
     * @param entityName name of the entity
     * @param qualifier qualifier
     * @param prefetchKeyPaths prefetching key paths
     * @param includeNewObjects option to include newly inserted objects in the result set
     * @param includeNewObjectsInParentEditingContext option to include newly inserted objects in parent editing
     *        contexts.  if true, the editing context lineage is explored, any newly-inserted objects matching the
     *        qualifier are collected and faulted down through all parent editing contexts of ec.
     * @param filterUpdatedObjects option to include updated objects that now match the qualifier or remove updated
     *         objects thats no longer match the qualifier
     * @param removeDeletedObjects option to remove objects that have been deleted
     *
     * @return array of objects matching the constructed qualifier
     */
    public static NSArray objectsWithQualifier(EOEditingContext editingContext, String entityName, EOQualifier qualifier, NSArray prefetchKeyPaths, boolean includeNewObjects, boolean includeNewObjectsInParentEditingContext, boolean filterUpdatedObjects, boolean removeDeletedObjects) {
    	return ERXEOControlUtilities.objectsWithQualifier(editingContext, entityName, qualifier, prefetchKeyPaths, null, 0, false, true, null, includeNewObjects, includeNewObjectsInParentEditingContext, filterUpdatedObjects, removeDeletedObjects);
    }
    
    /**
     * Utility method used to fetch an array of objects given a qualifier. Also
     * has support for filtering the newly inserted, updated, and deleted objects in the 
     * passed editing context or any parent editing contexts as well as specifying prefetching 
     * key paths.  Note that only NEW objects are supported in parent editing contexts.
     * 
     * @param editingContext editing context to fetch it into
     * @param entityName name of the entity
     * @param qualifier qualifier
     * @param prefetchKeyPaths prefetching key paths
     * @param sortOrderings the sort orderings to use on the results
     * @param usesDistinct whether or not to distinct the results
     * @param isDeep whether or not to fetch deeply
     * @param hints fetch hints to apply
     * @param includeNewObjects option to include newly inserted objects in the result set
     * @param includeNewObjectsInParentEditingContext option to include newly inserted objects in parent editing
     *        contexts.  if true, the editing context lineage is explored, any newly-inserted objects matching the
     *        qualifier are collected and faulted down through all parent editing contexts of ec.
     * @param filterUpdatedObjects option to include updated objects that now match the qualifier or remove updated
     *         objects thats no longer match the qualifier
     * @param removeDeletedObjects option to remove objects that have been deleted
     *
     * @return array of objects matching the constructed qualifier
     */
    // ENHANCEME: This should handle entity inheritance for in memory filtering
    public static NSArray objectsWithQualifier(EOEditingContext editingContext, String entityName, EOQualifier qualifier, NSArray prefetchKeyPaths, NSArray sortOrderings, boolean usesDistinct, boolean isDeep, NSDictionary hints, boolean includeNewObjects, boolean includeNewObjectsInParentEditingContext, boolean filterUpdatedObjects, boolean removeDeletedObjects) {
    	return objectsWithQualifier(editingContext, entityName, qualifier, prefetchKeyPaths, sortOrderings, 0, usesDistinct, isDeep, hints, includeNewObjects, includeNewObjectsInParentEditingContext, filterUpdatedObjects, removeDeletedObjects);
    }
    
    /**
     * Utility method used to fetch an array of objects given a qualifier. Also
     * has support for filtering the newly inserted, updated, and deleted objects in the 
     * passed editing context or any parent editing contexts as well as specifying prefetching 
     * key paths.  Note that only NEW objects are supported in parent editing contexts.
     * 
     * @param editingContext editing context to fetch it into
     * @param entityName name of the entity
     * @param qualifier qualifier
     * @param prefetchKeyPaths prefetching key paths
     * @param sortOrderings the sort orderings to use on the results
     * @param fetchLimit the fetch limit to use
     * @param usesDistinct whether or not to distinct the results
     * @param isDeep whether or not to fetch deeply
     * @param hints fetch hints to apply
     * @param includeNewObjects option to include newly inserted objects in the result set
     * @param includeNewObjectsInParentEditingContext option to include newly inserted objects in parent editing
     *        contexts.  if true, the editing context lineage is explored, any newly-inserted objects matching the
     *        qualifier are collected and faulted down through all parent editing contexts of ec.
     * @param filterUpdatedObjects option to include updated objects that now match the qualifier or remove updated
     *         objects thats no longer match the qualifier
     * @param removeDeletedObjects option to remove objects that have been deleted
     *
     * @return array of objects matching the constructed qualifier
     */
    // ENHANCEME: This should handle entity inheritance for in memory filtering
    public static NSArray objectsWithQualifier(EOEditingContext editingContext, String entityName, EOQualifier qualifier, NSArray prefetchKeyPaths, NSArray sortOrderings, int fetchLimit, boolean usesDistinct, boolean isDeep, NSDictionary hints, boolean includeNewObjects, boolean includeNewObjectsInParentEditingContext, boolean filterUpdatedObjects, boolean removeDeletedObjects) {
    	boolean objectsMayGetAdded = includeNewObjects || includeNewObjectsInParentEditingContext || filterUpdatedObjects;
    	NSArray<EOSortOrdering> fetchSortOrderings = sortOrderings;
    	if (objectsMayGetAdded) {
    		fetchSortOrderings = null;
    	}
        EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, fetchSortOrderings);
        fs.setFetchLimit(fetchLimit);
        fs.setPrefetchingRelationshipKeyPaths(prefetchKeyPaths);
        fs.setIsDeep(isDeep);
        fs.setUsesDistinct(usesDistinct);
        fs.setHints(hints);
        NSArray matchingObjects = editingContext.objectsWithFetchSpecification(fs);
        
        matchingObjects = filteredObjectsWithQualifier(editingContext, matchingObjects, entityName, qualifier, sortOrderings, usesDistinct, isDeep, includeNewObjects, includeNewObjectsInParentEditingContext, filterUpdatedObjects, removeDeletedObjects);
      
      return matchingObjects;
    }

    /**
     * Utility method used to filter an array of objects given a qualifier. Also
     * has support for filtering the newly inserted, updated, and deleted objects in the 
     * passed editing context or any parent editing contexts as well as specifying prefetching 
     * key paths.  Note that only NEW objects are supported in parent editing contexts.
     * 
     * @param editingContext editing context to fetch it into
     * @param objectsToFilter objects to filter
     * @param entityName name of the entity
     * @param qualifier qualifier
     * @param sortOrderings the sort orderings to use on the results
     * @param usesDistinct whether or not to distinct the results
     * @param isDeep whether or not to fetch deeply
     * @param includeNewObjects option to include newly inserted objects in the result set
     * @param includeNewObjectsInParentEditingContext option to include newly inserted objects in parent editing
     *        contexts.  if true, the editing context lineage is explored, any newly-inserted objects matching the
     *        qualifier are collected and faulted down through all parent editing contexts of ec.
     * @param filterUpdatedObjects option to include updated objects that now match the qualifier or remove updated
     *         objects thats no longer match the qualifier
     * @param removeDeletedObjects option to remove objects that have been deleted
     *
     * @return array of objects matching the constructed qualifier
     */
	public static NSArray filteredObjectsWithQualifier(EOEditingContext editingContext, NSArray objectsToFilter, String entityName, EOQualifier qualifier, NSArray sortOrderings, boolean usesDistinct, boolean isDeep, boolean includeNewObjects, boolean includeNewObjectsInParentEditingContext, boolean filterUpdatedObjects, boolean removeDeletedObjects) {
    	boolean objectsMayGetAdded = includeNewObjects || includeNewObjectsInParentEditingContext || filterUpdatedObjects;
		NSMutableArray cloneMatchingObjects = null;
    	NSMutableArray<String> entityNames = new NSMutableArray<String>();
		entityNames.addObject(entityName);
    	if (isDeep) {
    		EOModelGroup modelGroup = ERXEOAccessUtilities.modelGroup(editingContext);
    		EOEntity rootEntity = modelGroup.entityNamed(entityName);
    		for (EOEntity subEntity : rootEntity.subEntities()) {
    			entityNames.addObject(subEntity.name());
    		}
    	}
      // Filter again, because the in-memory versions may have been modified and no longer may match the qualifier
      if (filterUpdatedObjects) {
        // remove any old objects that now no longer match the qualifier (the version we get THIS time is the in-memory one, because
        // it's already been faulted in if it's updated)
        objectsToFilter = EOQualifier.filteredArrayWithQualifier(objectsToFilter, qualifier);

        // and then we need to add back in any updated objects that now DO match the qualifier that didn't originally match the qualifier
        NSArray updatedObjects = ERXEOControlUtilities.updatedObjects(editingContext, entityNames, qualifier);
        if (updatedObjects.count() > 0) {
          Enumeration updatedObjectsEnum = updatedObjects.objectEnumerator();
          while (updatedObjectsEnum.hasMoreElements()) {
            Object obj = updatedObjectsEnum.nextElement();
            if (!objectsToFilter.containsObject(obj)) {
              if (cloneMatchingObjects == null) {
                cloneMatchingObjects = objectsToFilter.mutableClone();
              }
              cloneMatchingObjects.addObject(obj);
            }
          }
          if (cloneMatchingObjects != null) {
            objectsToFilter = cloneMatchingObjects;
          }
        }
      }

      if (includeNewObjects) {
        NSMutableArray insertedObjects = ERXEOControlUtilities.insertedObjects(editingContext, entityNames, qualifier);
        if (insertedObjects.count() > 0) {
          if (cloneMatchingObjects != null) {
            cloneMatchingObjects.addObjectsFromArray(insertedObjects);
          }
          else {
            insertedObjects.addObjectsFromArray(objectsToFilter);
            objectsToFilter = insertedObjects;
          }
        }
      }
      
      if (includeNewObjectsInParentEditingContext && ! (editingContext.parentObjectStore() instanceof EOObjectStoreCoordinator) ) {
        final NSMutableArray parentEditingContexts = new NSMutableArray();
        EOObjectStore objectStore = editingContext.parentObjectStore();
        NSArray objects = NSArray.EmptyArray;
        int i;
        while (!(objectStore instanceof EOObjectStoreCoordinator)) {
          final EOEditingContext theEC = (EOEditingContext)objectStore;
          parentEditingContexts.addObject(theEC);
          objectStore = theEC.parentObjectStore();
        }

        i = parentEditingContexts.count();
        while (i-- > 0) {
          EOEditingContext theEC = (EOEditingContext)parentEditingContexts.objectAtIndex(i);
          NSArray objectsMatchingQualifier = ERXEOControlUtilities.insertedObjects(theEC, entityNames, qualifier);
          if (objectsMatchingQualifier.count() > 0) {
	          // fault the previous batch down
	          objects = EOUtilities.localInstancesOfObjects(theEC, objects);
	
	          if (objectsMatchingQualifier.count() > 0) {
	            objects = objects.arrayByAddingObjectsFromArray(objectsMatchingQualifier);
	          }
          }
        }

        if (objects.count() > 0) {
          objects = EOUtilities.localInstancesOfObjects(editingContext, objects);
          if (cloneMatchingObjects != null) {
            cloneMatchingObjects.addObjectsFromArray(objects);
          }
          else {
            NSMutableArray newMatchingObjects = objectsToFilter.mutableClone();
            newMatchingObjects.addObjectsFromArray(objects);
            objectsToFilter = newMatchingObjects;
          }
        }
      }

      if (removeDeletedObjects) {
        NSArray deletedObjects = ERXEOControlUtilities.deletedObjects(editingContext, entityNames, qualifier);
        if (deletedObjects.count() > 0) {
          if (cloneMatchingObjects == null) {
            cloneMatchingObjects = objectsToFilter.mutableClone();
            objectsToFilter = cloneMatchingObjects;
          }
          cloneMatchingObjects.removeObjectsInArray(deletedObjects);
        }
      }
      
      // MS: We need an arrayWithoutDuplicates that can work in-place for NSMutable ...
      if (objectsMayGetAdded && usesDistinct) {
		  objectsToFilter = ERXArrayUtilities.arrayWithoutDuplicates(objectsToFilter);
      }

      if (objectsMayGetAdded && sortOrderings != null && sortOrderings.count() > 0) {
    	  if (cloneMatchingObjects != null) {
    		  ERXS.sort(cloneMatchingObjects, sortOrderings);
    	  }
    	  else {
    		  objectsToFilter = ERXS.sorted(objectsToFilter, sortOrderings);
    	  }
      }
		return objectsToFilter;
	}
    
    /**
     * Returns the single object of the given type matching the qualifier.
     *  
     * @param editingContext the editing context to look in
     * @param entityName the name of the entity to look for
     * @param qualifier the qualifier to restrict by
     * @return the single object of the given type matching the qualifier or null if no matching object found
     * @throws MoreThanOneException if more than one object matches the qualifier
     */
    public static EOEnterpriseObject objectWithQualifier(EOEditingContext editingContext, String entityName, EOQualifier qualifier) {
        EOFetchSpecification fetchSpec = new EOFetchSpecification(entityName, qualifier, null);
        NSArray results = editingContext.objectsWithFetchSpecification(fetchSpec);
        if( results.count() > 1)
        {
        	throw new MoreThanOneException("objectMatchingValueForKeyEntityNamed: Matched more than one object with " +qualifier);
        }
        return (results.count() == 0) ? null : (EOEnterpriseObject)results.objectAtIndex(0);
    }

    /**
     * Returns the single object of the given type matching the qualifier.
     *  
     * @param editingContext the editing context to look in
     * @param entityName the name of the entity to look for
     * @param qualifier the qualifier to restrict by
     * @return he single object of the given type matching the qualifier
     * @throws EOObjectNotAvailableException if no objects match the qualifier
     * @throws MoreThanOneException if more than one object matches the qualifier
     */
    public static EOEnterpriseObject requiredObjectWithQualifier(EOEditingContext editingContext, String entityName, EOQualifier qualifier) {
        EOEnterpriseObject result = objectWithQualifier(editingContext, entityName, qualifier);
        if(result == null)
        {
        	throw new EOObjectNotAvailableException("objectWithQualifier: No objects match qualifier " + qualifier);
        }
        return result;
    }
    
    /**
     * Returns the array of objects of the given type that have been inserted into 
     * the editing context and match the given qualifier.  Yes, it's odd that it
     * returns NSMutableArray -- it's an optimization specifically for objectsWithQualifier.
     * Returns an empty array if no objects match.
     * 
     * @param editingContext the editing context to look in
     * @param entityNames the names of the entity to look for
     * @param qualifier the qualifier to restrict by
     * @return array of filtered inserted objects
     */
    public static NSMutableArray insertedObjects(EOEditingContext editingContext, NSArray<String> entityNames, EOQualifier qualifier) {
      NSMutableArray result = new NSMutableArray();
      NSDictionary insertedObjects = ERXArrayUtilities.arrayGroupedByKeyPath(editingContext.insertedObjects(), "entityName");
      for (String entityName : entityNames) {
	      NSArray insertedObjectsForEntity = (NSArray) insertedObjects.objectForKey(entityName);
	      if (insertedObjectsForEntity != null && insertedObjectsForEntity.count() > 0) {
	        NSArray inMemory = EOQualifier.filteredArrayWithQualifier(insertedObjectsForEntity, qualifier);
	        if (inMemory.count() > 0) {
	        	result.addObjectsFromArray(inMemory);
	        }
	      }
      }
      return result;
    }

    /**
     * Returns the array of objects of the given type that have been updated in
     * the editing context and match the given qualifier. Returns an empty array if no objects match.
     * 
     * @param editingContext the editing context to look in
     * @param entityNames the names of the entity to look for
     * @param qualifier the qualifier to restrict by
     * @return array of filtered updated objects
     */
    public static NSMutableArray updatedObjects(EOEditingContext editingContext, NSArray<String> entityNames, EOQualifier qualifier) {
      NSMutableArray result = new NSMutableArray();
      NSDictionary updatedObjects = ERXArrayUtilities.arrayGroupedByKeyPath(editingContext.updatedObjects(), "entityName");
      for (String entityName : entityNames) {
	      NSArray updatedObjectsForEntity = (NSArray) updatedObjects.objectForKey(entityName);
	      if (updatedObjectsForEntity != null && updatedObjectsForEntity.count() > 0) {
	        NSArray inMemory = EOQualifier.filteredArrayWithQualifier(updatedObjectsForEntity, qualifier);
	        if (inMemory.count() > 0) {
	        	result.addObjectsFromArray(inMemory);
	        }
	      }
      }
      return result;
    }

    /**
     * Return the array of objects of the given type that have been deleted from
     * the editing context and match the given qualifier.
     * 
     * @param editingContext the editing context to look in
     * @param entityNames the names of the entity to look for
     * @param qualifier the qualifier to restrict by
     * @return array of objects or an empty array
     */
    public static NSMutableArray deletedObjects(EOEditingContext editingContext, NSArray<String> entityNames, EOQualifier qualifier) {
      NSMutableArray result = new NSMutableArray();
      NSDictionary deletedObjects = ERXArrayUtilities.arrayGroupedByKeyPath(editingContext.deletedObjects(), "entityName");
      for (String entityName : entityNames) {
        NSArray deletedObjectsForEntity = (NSArray) deletedObjects.objectForKey(entityName);
        if (deletedObjectsForEntity != null && deletedObjectsForEntity.count() > 0) {
          NSArray inMemory = EOQualifier.filteredArrayWithQualifier(deletedObjectsForEntity, qualifier);
          if (inMemory.count() > 0) {
            result.addObjectsFromArray(inMemory);
          }
        }
      }
      return result;
    }

    /** Faults every EO in the qualifiers into the specified editingContext. This is important for 
     * in memory filtering and eo comparison.
     * @param ec
     * @param q
     */
    public static EOQualifier localInstancesInQualifier(EOEditingContext ec, EOQualifier q) {
        if (q instanceof EOKeyValueQualifier) {
            EOKeyValueQualifier q1 = (EOKeyValueQualifier)q;
            if (q1.value() instanceof EOEnterpriseObject) {
                EOEnterpriseObject eo = (EOEnterpriseObject)q1.value();
                if (eo.editingContext() != ec && !ERXEOControlUtilities.isNewObject(eo)) {
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
            return q instanceof EOAndQualifier ? new EOAndQualifier(qualifiers) : new EOOrQualifier(qualifiers);
        } else if (q instanceof EONotQualifier) {
            EONotQualifier qNot = (EONotQualifier)q;
            EOQualifier qual = localInstancesInQualifier(ec, qNot.qualifier());
            return new EONotQualifier(qual);
        } 
        return q;
        
    }

    /** Returns a NSArray containing EOGlobalIDs from the provided eos.
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
     * <b>NOTE:</b> this only returns objects that are already registered, if you
     * need all objects from the GIDs, use {@link #faultsForGlobalIDs(EOEditingContext, NSArray)}.
     * @see com.webobjects.eocontrol.EOEditingContext#objectForGlobalID(EOGlobalID)
     */
    public static NSArray objectsForGlobalIDs(final EOEditingContext ec, final NSArray globalIDs) {
        NSArray result = null;
        
        if ( globalIDs != null && globalIDs.count() > 0 ) {
            final NSMutableArray a = new NSMutableArray();
            final Enumeration e = globalIDs.objectEnumerator();
            
            while ( e.hasMoreElements() ) {
                final EOGlobalID theGID = (EOGlobalID)e.nextElement();
                final EOEnterpriseObject theObject = ec.faultForGlobalID(theGID, ec);
                
                if ( theObject != null )
                    a.addObject(theObject);
            }
            
            result = a.immutableClone();
        }
        
        return result != null ? result : NSArray.EmptyArray;
    }

    /** Returns a NSArray containing EOEnterpriseObjects (actually faults...) for the provided EOGlobalIDs.
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
     * Determines if an enterprise object is a new object and
     * hasn't been saved to the database yet. 
     * 
     * <br/>
     * Note: An object that has been deleted will have it's
     * editing context set to null which means this method
     * would report true for an object that has been deleted
     * from the database.
     * 
     * @param eo enterprise object to check
     * @return true or false depending on if the object is a
     *		new object.
     */
    public static boolean isNewObject(EOEnterpriseObject eo) {
        if (eo.editingContext() == null) return true;
        
        EOGlobalID gid = eo.editingContext().globalIDForObject(eo);
        return gid.isTemporary();
    }

    
    /**
     * Creates an OR qualifier of EOKeyValueQualifiers for every keypath in the given array of keys.
     * This is useful when trying to find a string in a set of attributes.
     * @param keys
     * @param selector
     * @param value
     * @author ak
     */
    public static EOQualifier qualifierMatchingAnyKey(NSArray keys, NSSelector selector, Object value) {
        NSMutableArray qualifiers = new NSMutableArray();
        EOQualifier result = null;
        if(keys.count() > 0) {
            for (Enumeration i = keys.objectEnumerator(); i.hasMoreElements();) {
                String key = (String) i.nextElement();
                qualifiers.addObject(new EOKeyValueQualifier(key, selector, value));
            }
            result = new EOOrQualifier(qualifiers);
        }
        return result;
    }
    
    /**
     * Uses <code>ERXEOControlUtilities.objectForFaults</code> to turn the faults into objects, then does in memory
     * ordering with <code>EOSortOrdering.EOSortOrdering.sortedArrayUsingKeyOrderArray()</code>
     *
     * @param ec
     * @param possibleFaults
     * @param sortOrderings
     * @return sorted array of EOs (no faults)
     */
    public static NSArray objectsForFaultWithSortOrderings (EOEditingContext ec, NSArray possibleFaults, NSArray sortOrderings) {
            if (sortOrderings != null) {
                return EOSortOrdering.sortedArrayUsingKeyOrderArray(ERXEOControlUtilities.objectsForFaults(ec, possibleFaults), sortOrderings);
            }
            return ERXEOControlUtilities.objectsForFaults(ec, possibleFaults);
    }

    /**
     * Triggers all faults in an efficient manner.
     * @param ec
     * @param possibleFaults globalIDs
     */
    public static NSArray objectsForFaults(EOEditingContext ec, NSArray possibleFaults) {
        NSMutableArray result = new NSMutableArray();
        NSMutableArray faultGIDs = new NSMutableArray();
        for (Enumeration objects = possibleFaults.objectEnumerator(); objects.hasMoreElements();) {
            EOEnterpriseObject eo = (EOEnterpriseObject)objects.nextElement();
            if(EOFaultHandler.isFault(eo)) {
                EOGlobalID gid = ec.globalIDForObject(eo);
                faultGIDs.addObject(gid);
            } else {
                result.addObject(eo);
            }
        }
        NSArray loadedObjects = ERXEOGlobalIDUtilities.fetchObjectsWithGlobalIDs(ec, faultGIDs);
        result.addObjectsFromArray(loadedObjects);
        return result;
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

    /** Caches the string attribute keys on a per entity name basis */
    private static NSMutableDictionary _attributeKeysPerEntityName=new NSMutableDictionary();
    
    /**
     * Calculates all of the EOAttributes of a given entity that
     * are mapped to String objects.
     * @return array of all attribute names that are mapped to
     *      String objects.
     */
    public static synchronized NSArray stringAttributeListForEntityNamed(EOEditingContext ec, String entityName) {
        NSArray result=(NSArray)_attributeKeysPerEntityName.objectForKey(entityName);
        if (result==null) {
            EOEntity entity=ERXEOAccessUtilities.entityNamed(ec, entityName);
            NSMutableArray attList=new NSMutableArray();
            _attributeKeysPerEntityName.setObjectForKey(attList,entityName);
            result=attList;
            for (Enumeration e=entity.classProperties().objectEnumerator();e.hasMoreElements();) {
                Object property=e.nextElement();
                if (property instanceof EOAttribute) {
                    EOAttribute a=(EOAttribute)property;
                    if (a.className().equals("java.lang.String"))
                        attList.addObject(a.name());
                }
            }
        }
        return result;
    }

    /**
     * Trims all values from string attributes from the given EO unless the EO itself
     * or the string attribute is flagged as read-only.
     * 
     * @param object the EO whose string attributes should be trimmed
     */
    public static void trimSpaces(EOEnterpriseObject object) {
        EOEntity entity = EOUtilities.entityForObject(object.editingContext(), object);
        if (entity.isReadOnly()) {
            return;
        }
        for (Enumeration e=ERXEOControlUtilities.stringAttributeListForEntityNamed(object.editingContext(), object.entityName()).objectEnumerator(); e.hasMoreElements();) {
            String key=(String)e.nextElement();
            String value=(String)object.storedValueForKey(key);
            if (value != null && !entity.attributeNamed(key).isReadOnly()) {
                String trimmedValue=value.trim();
                if (trimmedValue.length()!=value.length())
                    object.takeStoredValueForKey(trimmedValue,key);
            }
        }
    }

    /**
     * Convenience to get the destination entity name from a key path of an object.
     * Returns null if no destination found.
     * @param eo an enterprise object
     * @param keyPath key path
     * @return entity name or null
     */
   public static String destinationEntityNameForKeyPath(EOEnterpriseObject eo, String keyPath) {
	   EOEntity entity = ERXEOAccessUtilities.entityForEo(eo);
	   EOEntity destination = ERXEOAccessUtilities.destinationEntityForKeyPath(entity, keyPath);
	   if(destination != null) {
		   return destination.name();
	   }
	   return null;
   }
   
   /**
    * Creates an OR qualifier with the given selector for all the given key paths. If you want
    * LIKE matches, you need to the add "*" yourself.
    * @param keyPaths
    * @param selector
    * @param value
    */
   public static EOQualifier orQualifierForKeyPaths(NSArray keyPaths, NSSelector selector, Object value) {
	   NSMutableArray qualifiers = new NSMutableArray(keyPaths.count());
	   for (Enumeration e=keyPaths.objectEnumerator(); e.hasMoreElements();) {
		  String key = (String)e.nextElement();
		  EOQualifier qualifier = new EOKeyValueQualifier(key, selector, value);
		  qualifiers.addObject(qualifier);
	   }
	   return new EOOrQualifier(qualifiers);
   }
   
   /**
	 * Creates an OR qualifier with the given selector for all the given key
	 * paths and all the given search terms. If you want LIKE matches, you need
	 * to the add "*" yourself.
	 * 
	 * @param keyPaths
	 * @param selector
	 * @param values
	 */
   public static EOQualifier orQualifierForKeyPaths(NSArray keyPaths, NSSelector selector, NSArray values) {
	   NSMutableArray qualifiers = new NSMutableArray(values.count());
	   for (Enumeration e=values.objectEnumerator(); e.hasMoreElements();) {
		  Object value = e.nextElement();
		  EOQualifier qualifier = orQualifierForKeyPaths(keyPaths, selector, value);
		  qualifiers.addObject(qualifier);
	   }
	   return new EOOrQualifier(qualifiers);
   }
   
   /**
    * Joins the given qualifiers with an AND. One or both arguments may be null,
    * if both are null, null is returned.
    * @param q1 first qualifier
    * @param q2 second qualifier
    * @return combined qualifier
    */
   public static EOQualifier andQualifier(EOQualifier q1, EOQualifier q2) {
	   if(q1 == null) {
		   return q2;
	   }
	   if(q2 == null) {
		   return q1;
	   }
	   return new EOAndQualifier(new NSArray(new Object[]{q1, q2}));
    }

   
   /**
    * Joins the given qualifiers with an OR. One or both arguments may be null,
    * if both are null, null is returned.
    * @param q1 first qualifier
    * @param q2 second qualifier
    * @return combined qualifier
    */
   public static EOQualifier orQualifier(EOQualifier q1, EOQualifier q2) {
	   if(q1 == null) {
		   return q2;
	   }
	   if(q2 == null) {
		   return q1;
	   }
	   return new EOOrQualifier(new NSArray(new Object[]{q1, q2}));
    }

   /**
    * Given a qualifier of EOAndQualifiers and EOKVQualifiers, make then evaluate to
    * true on the given object. 
    * 
    * @param qualifier the qualifier to apply to the object
    * @param obj the object to make qualifier evaluate to true for
    */
   public static void makeQualifierTrue(EOQualifier qualifier, Object obj) {
	   if (qualifier instanceof EOAndQualifier) {
		   Enumeration qualifiersEnum = ((EOAndQualifier)qualifier).qualifiers().objectEnumerator();
		   while (qualifiersEnum.hasMoreElements()) {
			   EOQualifier nestedQualifier = (EOQualifier)qualifiersEnum.nextElement();
			   makeQualifierTrue(nestedQualifier, obj);
		   }
	   }
	   else if (qualifier instanceof EOKeyValueQualifier) {
		   EOKeyValueQualifier kvQualifier = (EOKeyValueQualifier)qualifier;
		   String keypath = kvQualifier.key();
		   Object value = kvQualifier.value();
		   NSKeyValueCoding.Utility.takeValueForKey(obj, value, keypath);
	   }
	   else if (qualifier instanceof EOKeyComparisonQualifier) {
		   EOKeyComparisonQualifier comparisonQualifier = (EOKeyComparisonQualifier)qualifier;
		   String leftKey = comparisonQualifier.leftKey();
		   String rightKey = comparisonQualifier.rightKey();
		   if ("true".equalsIgnoreCase(rightKey) || "yes".equalsIgnoreCase(rightKey)) {
			   NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(obj, Boolean.TRUE, leftKey);
		   }
		   else if ("false".equalsIgnoreCase(rightKey) || "no".equalsIgnoreCase(rightKey)) {
			   NSKeyValueCodingAdditions.Utility.takeValueForKeyPath(obj, Boolean.FALSE, leftKey);
		   }
		   else {
			   throw new IllegalArgumentException("Unable to make " + qualifier + " true for " + obj + " in a consistent way.");
		   }
	   }
	   else {
		   throw new IllegalArgumentException("Unable to make " + qualifier + " true for " + obj + " in a consistent way.");
	   }
   }
   
   /**
    * Given a dictionary, array, set, EO, etc, this will recursively turn
    * EO's into GID's.  You should lock the editingContext before calling
    * this.
    * 
    * @param obj the object to recursively turn EO's into GID's for
    * @return the GIDful object
    */
   public static Object convertEOtoGID(Object obj) {
	   Object result;
	   if (obj instanceof EOEnterpriseObject) {
		   EOEnterpriseObject eoful = (EOEnterpriseObject)obj;
		   EOGlobalID gidful = eoful.editingContext().globalIDForObject(eoful);
		   result = gidful;
	   }
	   else if (obj instanceof IERXEOContainer) {
		   result = ((IERXEOContainer)obj).toGIDContainer();
	   }
	   else if (obj instanceof NSArray) {
		   NSArray eoful = (NSArray)obj;
		   NSMutableArray gidful = new NSMutableArray();
		   Enumeration objEnum = eoful.objectEnumerator();
		   while (objEnum.hasMoreElements()) {
			   gidful.addObject(ERXEOControlUtilities.convertEOtoGID(objEnum.nextElement()));
		   }
		   result = gidful;
	   }
	   else if (obj instanceof NSSet) {
		   NSSet eoful = (NSSet)obj;
		   NSMutableSet gidful = new NSMutableSet();
		   Enumeration objEnum = eoful.objectEnumerator();
		   while (objEnum.hasMoreElements()) {
			   gidful.addObject(ERXEOControlUtilities.convertEOtoGID(objEnum.nextElement()));
		   }
		   result = gidful;
	   }
	   else if (obj instanceof NSDictionary) {
		   NSDictionary eoful = (NSDictionary)obj;
		   NSMutableDictionary gidful = new NSMutableDictionary();
		   Enumeration keyEnum = eoful.keyEnumerator();
		   while (keyEnum.hasMoreElements()) {
			   Object eofulKey = keyEnum.nextElement();
			   Object gidfulKey = ERXEOControlUtilities.convertEOtoGID(eofulKey);
			   Object gidfulValue = ERXEOControlUtilities.convertEOtoGID(eoful.objectForKey(eofulKey));
			   gidful.setObjectForKey(gidfulValue, gidfulKey);
		   }
		   result = gidful;
	   }
	   else {
		   result = obj;
	   }
	   return result;
   }
   
   /**
    * Given a dictionary, array, set, EO, etc, this will recursively turn
    * GID's into EO's.  You should lock the editingContext before calling
    * this.
    *  
    * @param obj the object to recursively turn GID's into EO's for
    * @return the EOful object
    */
   public static Object convertGIDtoEO(EOEditingContext editingContext, Object obj) {
	   Object result;
	   if (obj instanceof EOGlobalID) {
		   EOGlobalID gidful = (EOGlobalID)obj;
		   EOEnterpriseObject eoful = ERXEOGlobalIDUtilities.fetchObjectWithGlobalID(editingContext, gidful);
		   result = eoful;
	   }
	   else if (obj instanceof IERXGIDContainer) {
		   result = ((IERXGIDContainer)obj).toEOContainer(editingContext);
	   }
	   else if (obj instanceof NSArray) {
		   NSArray gidful = (NSArray)obj;
		   boolean allGIDs = true;
		   Enumeration objEnum = gidful.objectEnumerator();
		   while (allGIDs && objEnum.hasMoreElements()) {
			   allGIDs = (objEnum.nextElement() instanceof EOGlobalID);
		   }
		   if (allGIDs) {
			   result = ERXEOGlobalIDUtilities.fetchObjectsWithGlobalIDs(editingContext, gidful);
		   }
		   else {
			   NSMutableArray eoful = new NSMutableArray();
			   objEnum = gidful.objectEnumerator();
			   while (objEnum.hasMoreElements()) {
				   eoful.addObject(ERXEOControlUtilities.convertGIDtoEO(editingContext, objEnum.nextElement()));
			   }
			   result = eoful;
		   }
	   }
	   else if (obj instanceof NSSet) {
		   NSSet gidful = (NSSet)obj;
		   boolean allGIDs = true;
		   Enumeration objEnum = gidful.objectEnumerator();
		   while (allGIDs && objEnum.hasMoreElements()) {
			   allGIDs = (objEnum.nextElement() instanceof EOGlobalID);
		   }
		   if (allGIDs) {
			   result = new NSSet(ERXEOGlobalIDUtilities.fetchObjectsWithGlobalIDs(editingContext, gidful.allObjects()));
		   }
		   else {
			   NSMutableSet eoful = new NSMutableSet();
			   objEnum = gidful.objectEnumerator();
			   while (objEnum.hasMoreElements()) {
				   eoful.addObject(ERXEOControlUtilities.convertGIDtoEO(editingContext, objEnum.nextElement()));
			   }
			   result = eoful;
		   }
	   }
	   else if (obj instanceof NSDictionary) {
		   NSDictionary gidful = (NSDictionary)obj;
		   NSMutableDictionary eoful = new NSMutableDictionary();
		   Enumeration keyEnum = gidful.keyEnumerator();
		   while (keyEnum.hasMoreElements()) {
			   Object gidfulKey = keyEnum.nextElement();
			   Object eofulKey = ERXEOControlUtilities.convertGIDtoEO(editingContext, gidfulKey);
			   Object eofulValue = ERXEOControlUtilities.convertGIDtoEO(editingContext, gidful.objectForKey(gidfulKey));
			   eoful.setObjectForKey(eofulValue, eofulKey);
		   }
		   result = eoful;
	   }
	   else {
		   result = obj;
	   }
	   return result;
   }

	/**
	 * Validates whether the values of the specified keyPaths are unique for an
	 * Entity. Throws a {@link ERXValidationException} if there is already an EO
	 * with the same values on the given key paths.
	 * 
	 * Should be combined with a constraint on the corresponding database
	 * columns. <br />
	 * <br />
	 * Based on Zak Burke's idea he posted to WOCode a while ago. <br />
	 * <br />
	 * Use in <code>validateForSave</code> like this:
	 * 
	 * <pre>
	 *       ...
	 *       public class WikiPage extends _WikiPage {
	 *       ...
	 *       	public void validateForSave() throws ValidationException {
	 *       		super.validateForSave();
	 *       		ERXEOControlUtilities.validateUniquenessOf(null, this, ERXQ.equals(&quot;active&quot;, true), &quot;title&quot;, &quot;wiki&quot;);
	 *       	}
	 *       ...
	 *       }
	 * </pre>
	 * 
	 * Combine with entries in <code>ValidationTemplate.strings</code> like:
	 * 
	 * <pre>
	 * &quot;WikiPage.title,space.UniquenessViolationNewObject&quot; = &quot;The page cannot be created. There is already a page named &lt;b&gt;@@value.title@@&lt;/b&gt; in Wiki &lt;b&gt;@@value.wiki.name@@&lt;/b&gt;.&quot;;
	 * &quot;WikiPage.title,space.UniquenessViolationExistingObject&quot; = &quot;The page cannot be changed this way. There is already a page named &lt;b&gt;@@value.title@@&lt;/b&gt; in Wiki &lt;b&gt;@@value.wiki.name@@&lt;/b&gt;.&quot;;
	 * </pre>
	 * 
	 * @param eo
	 *            the {@link EOEnterpriseObject} to validate
	 * 
	 * @param keys
	 *            an arbitrary number of keyPaths to validate.
	 * 
	 * @param restrictingQualifier
	 *            an optional restricting qualifier to exclude certain objects
	 *            from the check
	 * 
	 * @param entityName
	 *            the name of the entity to check. Defaults to eo.entityName().
	 *            It can be necessary to set this to the name of the parent
	 *            entity when using single table inheritance.
	 * 
	 * @throws ERXValidationException
	 *             if an EO with the same property values already exists. If you
	 *             specify more than one keyPath to validate, the 'key' property
	 *             will be a comma separated string of the provided keyPaths.
	 *             'value' will be a dictionary with the supplied keyPaths as
	 *             keys and the values corresponding to these keys in the
	 *             supplied eo as values.
	 * 
	 * @author th
	 */
	public static void validateUniquenessOf(String entityName, EOEnterpriseObject eo, EOQualifier restrictingQualifier, String... keys) {
		if (restrictingQualifier != null && !restrictingQualifier.evaluateWithObject(eo)) {
			return;
		}
		if(entityName==null) {
			entityName=eo.entityName();
		}
		NSArray<String> keyPaths = new NSArray(keys);
		NSDictionary<String, Object> dict = ERXDictionaryUtilities.dictionaryFromObjectWithKeys(eo, keyPaths);
		EOQualifier qualifier = EOKeyValueQualifier.qualifierToMatchAllValues(dict);
		if (restrictingQualifier != null) {
			qualifier = ERXEOControlUtilities.andQualifier(qualifier, restrictingQualifier);
		}
		// take into account unsaved objects and skip deleted objects. The
		NSArray<EOEnterpriseObject> objects = ERXEOControlUtilities.objectsWithQualifier(eo.editingContext(), entityName, qualifier, null, true, false, true, true);
		// should we throw if the supplied eo is not included in the results?
		objects = ERXArrayUtilities.arrayMinusObject(objects, eo);
		int count = objects.count();
		if (count == 0) {
			// everything OK
		}
		else {
			String keyPathsString = keyPaths.componentsJoinedByString(",");
			if (count == 1) {
				// if we get here, we found an object matching the values
				if (ERXEOControlUtilities.isNewObject(eo)) {
					throw ERXValidationFactory.defaultFactory().createException(eo, keyPathsString, dict, "UniquenessViolationNewObject");
				}
				throw ERXValidationFactory.defaultFactory().createException(eo, keyPathsString, dict, "UniquenessViolationExistingObject");
			}
			// DB is already inconsitent!
			throw ERXValidationFactory.defaultFactory().createException(eo, keyPathsString, dict, "UniquenessViolationDatabaseInconsistent");
		}
	}

	/**
	 * Convenience method which passes <code>null</code> for
	 * <code>entityName</code>.
	 * 
	 * @param eo
	 *            the {@link EOEnterpriseObject} to validate
	 * @param restrictingQualifier
	 *            an optional restricting qualifier to exclude certain objects
	 *            from the check
	 * @param keys
	 *            an arbitrary number of keyPaths to validate.
	 * @author th
	 */
	public static void validateUniquenessOf(EOEnterpriseObject eo, EOQualifier restrictingQualifier, String... keys) {
		validateUniquenessOf(null, eo, restrictingQualifier, keys);
	}

	/**
	 * Convenience method which passes <code>null</code> for
	 * <code>restrictingQualifier</code> and <code>entityName</code>.
	 * 
	 * @param eo
	 *            the {@link EOEnterpriseObject} to validate
	 * @param keys
	 *            an arbitrary number of keyPaths to validate.
	 * @author th
	 */
	public static void validateUniquenessOf(EOEnterpriseObject eo, String... keys) {
		validateUniquenessOf(null, eo, null, keys);
	}
	
	/**
	 * Convenience method which passes <code>null</code> for
	 * <code>restrictingQualifier</code>.
	 * 
	 * @param eo
	 *            the {@link EOEnterpriseObject} to validate
	 * @param keys
	 *            an arbitrary number of keyPaths to validate.
	 * @param entityName
	 *            the name of the entity to check. Defaults to eo.entityName().
	 *            It can be necessary to set this to the name of the parent
	 *            entity when using single table inheritance.
	 * @author th
	 */
	public static void validateUniquenessOf(String entityName, EOEnterpriseObject eo, String... keys) {
		validateUniquenessOf(entityName, eo, null, keys);
	}
	
	/**
	 * Returns an NSArray of distinct values available for the given entity and key path. 
	 * The result can be narrowed by an optional qualifier and optionally sorted with sort orderings
	 * 
	 * @param <T> type of values for the key path
	 * @param editingContext editingContext
	 * @param entityName entityName
	 * @param keyPath keyPath
	 * @param qualifier restricting qualifier (optional)
	 * @param sortOrderings sortOrderings to be applied (optional)
	 * @return array of values
	 * 
	 * @author th
	 */
	public static <T> NSArray<T> distinctValuesForKeyPath(EOEditingContext editingContext, String entityName, String keyPath, EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings) {
		if (editingContext == null || entityName == null || keyPath == null)
			throw new IllegalArgumentException("The editingContext, entityName and keyPath parameters must not be null");
		EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, sortOrderings);
		fs.setUsesDistinct(true);
		fs.setFetchesRawRows(true);
		fs.setRawRowKeyPaths(new NSArray<String>(keyPath));
		NSArray<NSDictionary<String, T>> rawRows = editingContext.objectsWithFetchSpecification(fs);
		// Note that the raw row keyPath becomes a key in the raw row dictionary having the value derived from the schema keyPath
		NSArray<T> values = (NSArray<T>) rawRows.valueForKey(keyPath);
		values = ERXArrayUtilities.arrayWithoutDuplicates(values);
		return values;
	}

	/**
	 * Returns the count of registered objects in the EC grouped by entity name, which is useful for memory debugging. 
	 * Put this in a log on session.sleep() for example.
	 * @param ec editing context to get count of
	 * @return dictionary of counts
	 */
	public static NSDictionary<String, Integer> registeredObjectCount(EOEditingContext ec) {
		NSMutableDictionary<String, Integer> counts = new NSMutableDictionary<String, Integer>();
		ERXEC erxec = (ERXEC) ec;
		NSArray<EOGlobalID> gids = (NSArray<EOGlobalID>)ERXKeyValueCodingUtilities.privateValueForKey(erxec, "_globalIDsForRegisteredObjects");
		for(EOGlobalID gid : gids) {
			if (gid instanceof EOKeyGlobalID) {
				EOKeyGlobalID kgid = (EOKeyGlobalID) gid;
				String entityName = kgid.entityName();
				Integer count = counts.objectForKey(entityName);
				if(count == null) {
					count = Integer.valueOf(0);
					counts.setObjectForKey(count, entityName);
				}
				counts.setObjectForKey(count+1, entityName);
			}
		}
		return counts;
	}

	/**
	 * Returns the changes in count registered objects in the EC grouped by entity name, which is useful for memory debugging.
	 * @param currentCounts current count of objects
	 * @param oldCounts previous count of objects
	 * @return dictionary of counts
	 */
	public static NSDictionary<String, Integer> changedRegisteredObjectCount(NSDictionary<String, Integer> currentCounts, NSDictionary<String, Integer> oldCounts) {
		NSMutableDictionary<String, Integer> counts = currentCounts.mutableClone();
		if(oldCounts != null) {
			for(String entityName : counts.allKeys()) {
				Integer count = oldCounts.objectForKey(entityName);
				if(count != null) {
					int changes = counts.objectForKey(entityName) - count;
					if(changes == 0) {
						counts.removeObjectForKey(entityName);
					} else {
						counts.setObjectForKey(changes, entityName);
					}
				}
			}
		}
		return counts;
	}
	
	/**
	 * Ensures the array of objects follow the same order than the array of globalIDs
	 * returned by the first fetch.
	 *
	 * @param ec
	 *            an editingContext
	 * @param gids
	 *            the array of globalIDs ordered as expected
	 * @param objects
	 *            the array of objects to be ordered based on the array of gids
	 */
	private static void ensureSortOrdering(EOEditingContext ec, NSArray<? extends EOGlobalID> gids, NSMutableArray<? extends EOEnterpriseObject> objects) {
		for (int i = 0; i < objects.size(); i++) {
			EOEnterpriseObject object = objects.objectAtIndex(i);

			EOGlobalID gid = gids.objectAtIndex(i);

			if (gid.equals(ec.globalIDForObject(object))) {
				continue;
			}

			for (int j = i + 1; j < objects.size(); j++) {
				if (gid.equals(ec.globalIDForObject(objects.objectAtIndex(j)))) {
					ERXArrayUtilities.swapObjectsAtIndexesInArray(objects, i, j);

					break;
				}
			}
		}
	}

	/**
	 * Useful for ensuring a fetch specification is safe to pass around between
	 * threads and not having to be concerned about references to EOs in the
	 * qualifier.
	 * 
	 * @param ec
	 *            a locked EOEditingContext that can be used for getting the
	 *            entity
	 * @param fetchSpecification
	 * 
	 * @return a clone of the fetchSpecification with the EOQualifier converted
	 *         to a schema-based qualifier, or the original fetchSpec if the
	 *         fetchSpec has no qualifier
	 */
	public static EOFetchSpecification schemaBasedFetchSpecification(EOEditingContext ec, EOFetchSpecification fetchSpecification) {
	
		EOQualifier q = fetchSpecification.qualifier();
		if (q != null) {
	
			// Clone the fetchSpec
			fetchSpecification = (EOFetchSpecification) fetchSpecification.clone();
			q = schemaBasedQualifier(ec, fetchSpecification.entityName(), q);
			fetchSpecification.setQualifier(q);
	
		} // ~ if (q != null)
		return fetchSpecification;
	}

	/**
	 * Useful for ensuring a qualifier is safe to pass around between threads and not having to be
	 * concerned about references to EOs in it. Also handy to verify that an EOQualifier can be used in
	 * a database fetch.
	 * 
	 * @param ec
	 *            a locked EOEditingContext that can be used for getting the entity
	 * @param entityName
	 *            the entity being qualified
	 * @param qualifier
	 *            original qualifier
	 * @return a schema based qualifier that contains no references to
	 *         EOEnterpriseObjects
	 *         
	 */
	public static EOQualifier schemaBasedQualifier(EOEditingContext ec, String entityName, EOQualifier qualifier) {

		EOEntity entity = ERXEOAccessUtilities.entityMatchingString(ec, entityName);
		// Convert the qualifier to a schema-based qualifier
		ec.rootObjectStore().lock();
		try {
			return entity.schemaBasedQualifier(qualifier);
		} finally {
			ec.rootObjectStore().unlock();
		}

	}
}
