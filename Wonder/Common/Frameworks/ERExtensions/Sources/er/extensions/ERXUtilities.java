/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;
import java.util.Enumeration;
import java.util.TimeZone;
import java.io.*;

/**
 * Diverse collection of utility methods for handling everything from
 * EOF to foundation. In the future this class will most likely be
 * split into more meaning full groups of utility methods.
 */
public class ERXUtilities {

    /** logging support */
    public static Category cat = Category.getInstance(ERXUtilities.class);

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
     * Creates an enterprise object for the given entity
     * name by first looking up the class description
     * of the entity to create the enterprise object.
     * The object is then inserted into the editing context
     * and returned.
     * @param entityName name of the entity to be
     *		created.
     * @param editingContext editingContext to insert
     *		the created object into
     * @return created and inserted enterprise object
     */
    public static EOEnterpriseObject createEO(String entityName,
                                              EOEditingContext editingContext) {
        return ERXUtilities.createEO(entityName,
                                    editingContext,
                                    null);
    }
    
    /**
     * Creates an enterprise object for the given entity
     * name by first looking up the class description
     * of the entity to create the enterprise object.
     * The object next has the values pushed onto it
     * from the objectInfo dictionary before being
     * inserted into the editing context. The advantage
     * of this is that you can have values already set
     * on the object when awakeFromInsertion is called
     * on the object.
     * @param entityName name of the entity to be
     *		created.
     * @param editingContext editingContext to insert
     *		the created object into
     * @param objectInfo dictionary of values pushed onto
     *		the object before being inserted into the
     *		editing context.
     * @return created and inserted enterprise object
     */    
    public static EOEnterpriseObject createEO(String entityName,
                                              EOEditingContext editingContext,
                                              NSDictionary objectInfo) {
        if (cat.isDebugEnabled())
            cat.debug("Creating object of type: " + entityName);
        EOClassDescription cd=EOClassDescription.classDescriptionForEntityName(entityName);
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
     * <code>createEOLinkedToEO("Bar", ec, "toBars", foo);</code><br/>
     * <br/>
     * will create an instance of Bar, insert it into an editing context
     * and then add it to both sides of the realtionship "toBars" off of
     * the enterprise object foo.
     *
     * @param entityName name of the entity of the object to be created.
     * @param editingContext editing context to create the object in
     * @param relationshipName relationship name of the enterprise object
     *		that is passed in to which the newly created eo should be
     *		added.
     * @param eo enterprise object to whose relationship the newly created
     *		object will be added.
     * @return the newly created enterprise object
     */
    public static EOEnterpriseObject createEOLinkedToEO(String entityName,
                                                        EOEditingContext editingContext,
                                                        String relationshipName,
                                                        EOEnterpriseObject eo) {
        return ERXUtilities.createEOLinkedToEO(entityName,
                                              editingContext,
                                              relationshipName,
                                              eo,
                                              null);
    }

    /**
     * Creates an object using the utility method <code>createEO</code>
     * from this utility class. After creating the enterprise object it
     * is added to the relationship of the enterprise object passed in.
     * For instance:<br/>
     * <code>createEOLinkedToEO("Bar", ec, "toBars", foo, dictValues);</code><br/>
     * <br/>
     * will create an instance of Bar, set all of the key-value pairs
     * from the dictValues dictionary, insert it into an editing context
     * and then add it to both sides of the realtionship "toBars" off of
     * the enterprise object foo.
     *
     * @param entityName name of the entity of the object to be created.
     * @param editingContext editing context to create the object in
     * @param relationshipName relationship name of the enterprise object
     *		that is passed in to which the newly created eo should be
     *		added.
     * @param eo enterprise object to whose relationship the newly created
     *		object will be added.
     * @param objectInfo dictionary of values to be set on the newly created
     *		object before it is inserted into the editing context.
     * @return the newly created enterprise object
     */
    public static EOEnterpriseObject createEOLinkedToEO(String entityName,
                                                        EOEditingContext editingContext,
                                                        String relationshipName,
                                                        EOEnterpriseObject eo,
                                                        NSDictionary objectInfo) {
        EOEnterpriseObject newEO=createEO(entityName, editingContext, objectInfo);
        EOEnterpriseObject eoBis = editingContext!=eo.editingContext() ?
            EOUtilities.localInstanceOfObject(editingContext,eo) : eo;
        eoBis.addObjectToBothSidesOfRelationshipWithKey(newEO, relationshipName);
        return newEO;
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
     * Provides the same functionality as the equivalent method
     * in {@link EOUtilities} except it will use the localInstanceOfObject
     * method from this utilities class which has a few enhancements.
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
     * Fetches a shared enterprise object for a given fetch
     * specification from the default shared editing context.
     * @param fetchSpec name of the fetch specification on the
     *		shared object.
     * @param entityName name of the shared entity
     * @return the shared enterprise object fetch by the fetch spec named.
     */
    public static EOEnterpriseObject sharedObjectWithFetchSpec(String fetchSpec, String entityName) {
        return EOUtilities.objectWithFetchSpecificationAndBindings(EOSharedEditingContext.defaultSharedEditingContext(),
                                                                   entityName,
                                                                   fetchSpec,
                                                                   null);
    }

    /**
     * Gets the shared enterprise object with the given primary
     * from the default shared editing context. This has the
     * advantage of not requiring a roundtrip to the database to
     * lookup the object.
     * @param pk primary key of object to be found
     * @param entityName name of the entity
     * @return the shared object registered in the default shared editing context
     */
    public static EOEnterpriseObject sharedObjectWithPrimaryKey(Object pk, String entityName) {
        return EOUtilities.objectWithPrimaryKeyValue(EOSharedEditingContext.defaultSharedEditingContext(),
                                                     entityName,
                                                     pk);
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
    public static NSDictionary primaryKeyDictionaryForEntity(EOEditingContext ec, String entityName) {
        // FIXME: Should use the modelgroup for the root object store of the
        //	  editing context.
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        NSDictionary primaryKey = null;
        try {
            dbContext.lock();
            EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
            if (!adaptorChannel.isOpen())
                adaptorChannel.openChannel();
            primaryKey = (NSDictionary)adaptorChannel.primaryKeysForNewRowsWithEntity(1, entity).lastObject();
            dbContext.unlock();
        } catch (Exception e) {
            dbContext.unlock();
            cat.error("Caught exception when generating primary key for entity: " + entityName + " exception: " + e);
        }
        return primaryKey;
    }

    /**
     * Utility method for returning all of the primary keys for
     * all of the objects that are marked for deletion in
     * the editing context.
     * @param ec editing context
     * @return an array containing all of the dictionaries of
     *		primary keys for all of the objects marked for
     *		deletion
     */
    // CHECKME: I don't think this is a value add
    public static NSArray deletedObjectsPKeys(EOEditingContext ec) {
        NSMutableArray result = new NSMutableArray();
        for (Enumeration e = ec.deletedObjects().objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo=(EOEnterpriseObject)e.nextElement();
            if (eo instanceof ERXGenericRecord)
                result.addObject(((ERXGenericRecord)eo).primaryKeyInTransaction());
            else
                result.addObject(EOUtilities.primaryKeyForObject(ec, eo));
        }
        return result;
    }

    /**
     * Convenience method to get the next unique ID from a sequence.
     * @param ec editing context
     * @param sequenceName name of the sequence
     * @return next value in the sequence
     */
    // FIXME: In following with standard EOUtilities naming ec should be the first parameter.
    //	      also shouldn't have throws RuntimeException
    // FIXME: Also might want to return a long
    public static int getNextValFromSequenceNamed(String sequenceName, EOEditingContext ec) throws RuntimeException{
        // CHECKME: Need a non-oracle specific way of doing this. Should poke around at
        //		the adaptor level and see if we can't find something better.
        String sqlString = "select "+sequenceName+".nextVal from dual";
        // FIXME: Bad name reference here, should be a parameter.
        NSArray array = EOUtilities.rawRowsForSQL( ec, "ER", sqlString);
        if(array.count()==0)
            throw new RuntimeException("Unable to generate value from sequence");
        NSDictionary dictionary = (NSDictionary)array.objectAtIndex(0);
        NSArray valuesArray = dictionary.allValues();
        return ((Number)valuesArray.objectAtIndex(0)).intValue();
    }

    /**
     * Utility method to make a shared entity editable. This
     * can be useful if you want to have an adminstration
     * application that can edit shared enterprise objects
     * and need a way at start up to disable the sharing
     * constraints.
     * @param entityName name of the shared entity to make
     *		shareable.
     */
    // FIXME: Should have to pass in an editing context so that the
    //		correct model group and shared ec will be used.
    // FIXME: Should also dump all of the currently shared eos from
    //		the shared context.
    public static void makeEditableSharedEntityNamed(String entityName) {
        EOEntity e = EOModelGroup.defaultGroup().entityNamed(entityName);
        if (e.isReadOnly()) {
            e.setReadOnly(false);
            e.setCachesObjects(false);
            e.removeSharedObjectFetchSpecificationByName("FetchAll");
        } else {
            cat.warn("MakeSharedEntityEditable: enity already editable: " + entityName);
        }
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
     * This is protected utility method from JavaWOExtensions.
     * All it does is sort a mutable array using a single key.
     * The sort is performed using the selector:
     * {@link EOSortOrdering.CompareCaseInsensitiveAscending}
     * Note: if you want to return a new array of sorted eos
     * you can use 'sort' {@link NSArray.Operator} found in
     * {@link ERXArrayUtilities}. 
     * @param eos mutable array to be sorted
     * @param key key to be sorted on.
     */
    // CHECKME: Should these methods move to ERXArrayUtilities or the
    //		yet to be created ERXEOFUtilities?
    public static void sortEOsUsingSingleKey(NSMutableArray eos, String key) {
        sortEOsUsingSingleKey(eos, key, EOSortOrdering.CompareCaseInsensitiveAscending);
    }

    /**
     * This is protected utility method from JavaWOExtensions.
     * All it does is sort a mutable array using a single key
     * and a selector.
     * @param eos mutable array to be sorted
     * @param key key to be sorted on.
     * @param selector sort selector.
     */
    // CHECKME: Should these methods move to ERXArrayUtilities or the
    //		yet to be created ERXEOFUtilities?
    public static void sortEOsUsingSingleKey(NSMutableArray eos, String key, NSSelector selector) {
        if (eos == null)
            throw new RuntimeException("Attempting to sort null array of eos.");
        if (key == null)
            throw new RuntimeException("Attepting to sort array of eos with null key.");
        EOSortOrdering.sortArrayUsingKeyOrderArray(eos,
                                                   new NSArray(new EOSortOrdering(key, selector)));
    }

    /**
     * This method resolves bindings from WOComponents to
     * boolean values. The added benifit (and this might not
     * still be the case) is that when <code>false</code> is
     * bound to a binding will pass through null. This makes
     * it difficult to handle the case where a binding should
     * default to true but false was actually bound to the
     * binding.<br/>
     * Note: This is only needed for non-syncronizing components
     * @param binding name of the binding
     * @param component to resolve binding request
     * @param def default value if binding is not set
     * @return boolean resolution of the object returned from the
     *		valueForBinding request.
     */
    public static boolean booleanValueForBindingOnComponentWithDefault(String binding, WOComponent component, boolean def) {
        // CHECKME: I don't believe the statement below is true with WO 5
        // this method is useful because binding=NO in fact sends null, which in turns
        // leads booleanValueWithDefault(valueForBinding("binding", true) to return true when binding=NO was specified
        boolean result=def;
        if (component!=null) {
            if (component.canGetValueForBinding(binding)) {
                Object value=component.valueForBinding(binding);
                result=value==null ? false : booleanValueWithDefault(value, def);
            }
        }
        return result;
    }
    
    /**
     * Basic utility method for determining if an object
     * represents either a true or false value. The current
     * implementation tests if the object is an instance of
     * a String or a Number. Numbers are false if they equal
     * <code>0</code>, Strings are false if they equal (case insensitive)
     * 'no', 'false' or parse to 0.
     * @param obj object to be evaluated
     * @return boolean evaluation of the given object
     */
    public static boolean booleanValue(Object obj) {
        return booleanValueWithDefault(obj,false);
    }

    /**
     * Basic utility method for determining if an object
     * represents either a true or false value. The current
     * implementation tests if the object is an instance of
     * a String or a Number. Numbers are false if they equal
     * <code>0</code>, Strings are false if they equal (case insensitive)
     * 'no', 'false' or parse to 0. The default value is used if
     * the object is null.
     * @param obj object to be evaluated
     * @param def default value if object is null
     * @return boolean evaluation of the given object
     */
    public static boolean booleanValueWithDefault(Object obj, boolean def) {
        boolean flag = true;
        if (obj != null) {
            // FIXME: Should add support for the BooleanOperation interface
            if (obj instanceof Number) {
                if (((Number)obj).intValue() == 0)
                    flag = false;
            } else if(obj instanceof String) {
                String s = (String)obj;
                if (s.equalsIgnoreCase("no") || s.equalsIgnoreCase("false") || s.equalsIgnoreCase("n"))
                    flag = false;
		else if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("y"))
                    flag = true;
		else
                    try {
                        if (Integer.parseInt(s) == 0)
                            flag = false;
                    } catch(NumberFormatException numberformatexception) {
                        throw new RuntimeException("error parsing boolean from value " + s);
                    }
            } else if (obj instanceof Boolean)
                flag = ((Boolean)obj).booleanValue();
        } else {
            flag = def;
        }
        return flag;
    }
    
    /**
     * Traverses a key path to return the last {@link EORelationship}
     * object.<br/>
     * Note: that this method uses the object and not the model to traverse
     * the key path, this has the added benefit of handling EOF inheritance
     * @param object enterprise object to find the relationship off of
     * @param keyPath key path used to find the relationship
     * @return relationship object corresponding to the last property key of
     * 		the key path.
     */
    public static EORelationship relationshipWithObjectAndKeyPath(EOEnterpriseObject object, String keyPath) {
        EOEnterpriseObject lastEO=object;
        EORelationship relationship = null;
        if (keyPath.indexOf(".")!=-1) {
            String partialKeyPath=ERXStringUtilities.keyPathWithoutLastProperty(keyPath);
            Object rawLastEO=object.valueForKeyPath(partialKeyPath);
            lastEO=rawLastEO instanceof EOEnterpriseObject ? (EOEnterpriseObject)rawLastEO : null;
        }
        if (lastEO!=null) {
            // FIXME: Should use the model group of the object's editing context's
            //		root object store coordinator
            EOEntity entity=EOModelGroup.defaultGroup().entityNamed(lastEO.entityName());
            String lastKey=ERXStringUtilities.lastPropertyKeyInKeyPath(keyPath);
            relationship=entity.relationshipNamed(lastKey);
        }
        return relationship;
    }

    /**
     * Simple utility method for deleting an array
     * of objects from an editing context.
     * @param ec editing context to have objects deleted from
     * @param objects objects to be deleted.
     */
    public static void deleteObjects(EOEditingContext ec, NSArray objects) {
        if (objects != null && objects.count() > 0) {
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();)
                ec.deleteObject((EOEnterpriseObject)e.nextElement());            
        }
    }
    
    /**
     * Utility method to get all of the framework names that
     * have been loaded into the application.
     * @return array containing all of the framework names
     */
    public static NSArray allFrameworkNames() {
        NSMutableArray frameworkNames = new NSMutableArray();
        for (Enumeration e = NSBundle.frameworkBundles().objectEnumerator(); e.hasMoreElements();) {
            NSBundle bundle = (NSBundle)e.nextElement();
            String frameworkName = nameFromFrameworkBundle(bundle);
            if (frameworkName != null)
                frameworkNames.addObject(frameworkName);
            else
                cat.warn("Null framework name for bundle: " + bundle);
        }
        return frameworkNames;
    }


    public static String nameFromFrameworkBundle(NSBundle nsbundle) {
        String s = nsbundle.bundlePath();
        return !s.endsWith(".framework") ? null :
            s.substring(s.lastIndexOf(File.separator) + 1, s.lastIndexOf("."));
    }
    

    
    /**
     * Performs a basic intersection between two arrays.
     * @param array1 first array
     * @param array2 second array
     * @return array containing the intersecting elements of
     *		the two arrays.
     */
    // MOVEME: Should move to ERXArrayUtilities
    public static NSArray intersectingElements(NSArray array1, NSArray array2) {
        NSArray intersection = null;
        if (array1 != null && array2 != null && array1.count() > 0 && array2.count() > 0) {
            NSMutableSet set1 = new NSMutableSet();
            NSMutableSet set2 = new NSMutableSet();
            set1.addObjectsFromArray(array1);
            set2.addObjectsFromArray(array2);
            intersection = (set1.setByIntersectingSet(set2)).allObjects();
        }
        return intersection != null ? intersection : ERXConstant.EmptyArray;
    }

    /**
     * Simple utility method for getting all of the
     * entities for all of the models of a given
     * model group.
     * @param group eo model group
     * @return array of all entities for a given model group.
     */
    public static NSArray entitiesForModelGroup(EOModelGroup group) {
        return ERXExtensions.flatten((NSArray)group.models().valueForKey("entities"));
    }

    /** entity name cache */
    private static NSMutableDictionary _enityNameEntityCache;
    /**
     * Finds an entity given a case insensitive search
     * of all the entity names.<br/>
     * Note: The current implementation caches the entity-entity name
     * pair in an insensitive manner. This means that all of the
     * models should be loaded before this method is called.
     */
    // FIXME: Should add an EOEditingContext parameter to get the right
    //	      EOModelGroup. Should also have a way to clear the cache.
    // CHECKME: Should this even be cached? Not thread safe now.
    public static EOEntity caseInsensitiveEntityNamed(String entityName) {
        EOEntity entity = null;
        if (entityName != null) {
            if (_enityNameEntityCache == null) {
                _enityNameEntityCache = new NSMutableDictionary();
                for (Enumeration e = entitiesForModelGroup(EOModelGroup.defaultGroup()).objectEnumerator(); e.hasMoreElements();) {
                    EOEntity anEntity = (EOEntity)e.nextElement();
                    _enityNameEntityCache.setObjectForKey(anEntity, anEntity.name().toLowerCase());    
                }
            }
            entity = (EOEntity)_enityNameEntityCache.objectForKey(entityName.toLowerCase());
        }
        return entity;
    }

    /**
     * Utility method used to find all of the sub entities
     * for a given entity.
     * @param entity to walk all of the <code>subEntities</code>
     *		relationships
     * @param includeAbstracts determines if abstract entities should
     *		be included in the returned array
     * @return all of the sub-entities for a given entity.
     */
    public static NSArray allSubEntitiesForEntity(EOEntity entity, boolean includeAbstracts) {
        NSMutableArray entities = new NSMutableArray();
        if (entity != null) {
            for (Enumeration e = entity.subEntities().objectEnumerator(); e.hasMoreElements();) {
                EOEntity anEntity = (EOEntity)e.nextElement();
                if ((includeAbstracts && anEntity.isAbstractEntity()) || !anEntity.isAbstractEntity())
                    entities.addObject(anEntity);
                if (anEntity.subEntities() != null && anEntity.subEntities().count() > 0)
                    entities.addObjectsFromArray(allSubEntitiesForEntity(anEntity, includeAbstracts));
            }
        }
        return entities;
    }

    /**
     * Walks all of the parentEntity relationships to
     * find the root entity.
     * @param entity to find the root parent
     * @return root parent entity
     */
    public static EOEntity rootParentEntityForEntity(EOEntity entity) {
        EOEntity root = entity;
        while (root!=null && root.parentEntity() != null)
            root = root.parentEntity();
        return root;
    }
    /** caches date formatter the first time it is used */
    private static NSTimestampFormatter _gregorianDateFormatterForJavaDate;
    /**
     * Utility method to return a standard timestamp
     * formatter for the default string representation
     * of java dates.
     * @return timestamp formatter for java dates.
     */
    // MOVEME: Should move to ERXTimestampUtilities
    public static NSTimestampFormatter gregorianDateFormatterForJavaDate() {
        if (_gregorianDateFormatterForJavaDate == null)
            _gregorianDateFormatterForJavaDate = new NSTimestampFormatter("%a %b %d %H:%M:%S %Z %Y");
        return _gregorianDateFormatterForJavaDate;
    }    

    /**
     * Generates a string representation of
     * the current stacktrace.
     * @return current stacktrace.
     */
    public static String stackTrace() {
        String result;
        try {
            throw new Throwable();
        } catch (Throwable t) {
            result = ERXUtilities.stackTrace(t);
        }
        // clipping the early parts of the stack trace which include
        // ERXUtilities.stackTrace()
        return result.substring(122);
    }

    /**
     * Converts a throwable's stacktrace into a
     * string representation.
     * @param t throwable to print to a string
     * @return string representation of stacktrace
     */
    public static String stackTrace(Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        PrintStream printStream = new PrintStream(baos);
        t.printStackTrace(printStream);
        return baos.toString();
    }

    /**
     * Useful interface for binding objects to
     * WOComponent bindings where you want to
     * delay the evaluation of the boolean operation
     * until <code>valueForBinding</code> is
     * actually called. See {@link ERXStatelessComponent}
     * for examples.
     */
    public static interface BooleanOperation {
        public boolean value();
    }

    /**
     * Useful interface for binding objects to
     * WOComponent bindings where you want to
     * delay the evaluation of the operation
     * until <code>valueForBinding</code> is
     * actually called. See {@link ERXStatelessComponent}
     * for examples.
     */
    public static interface Operation {
        public Object value();
    }

    /**
     * Generic callback interface with a context
     * object.
     */
    public static interface Callback {
        public Object invoke(Object ctx);
    }

    /**
     * Generic boolean callback interface with a
     * context object.
     */
    public static interface BooleanCallback {
        public boolean invoke(Object ctx);
    }

    // DELETEME: These are not needed now that all of the distant stuff works again.
    public static final NSTimestamp DISTANT_FUTURE = new NSTimestamp(2999,1,1,1,1,1,TimeZone.getDefault());
    public static NSTimestamp distantFuture() { return DISTANT_FUTURE; }
    public static final NSTimestamp DISTANT_PAST = new NSTimestamp(1000,1,1,1,1,1,TimeZone.getDefault());
    public static NSTimestamp distantPast() { return DISTANT_PAST; }

    /**
     * Gets rid of all ' from a String.
     * @param aString string to check
     * @return string without '
     */
    // CHECKME: Is this a value add? I don't think so.
    public static String escapeApostrophe(String aString) {
        NSArray parts = NSArray.componentsSeparatedByString(aString,"'");
        return parts.componentsJoinedByString("");
    }

    /**
     * Simply utility method to create a concreate
     * set object from an array
     * @param array of elements
     * @return concreate set.
     */
    // MOVEME: Should move to ERXArrayUtilities
    // CHECKME: Is this a value add?
    public static NSSet setFromArray(NSArray array) {
        if (array == null || array.count() == 0)
            return NSSet.EmptySet;
        else {
            Object [] objs = new Object[array.count()];
            objs = array.objects();
            return new NSSet(objs);
        }
    }

    /** Caches sort orderings for given keys */
    private final static NSDictionary _selectorsByKey=new NSDictionary(new NSSelector [] {
        EOSortOrdering.CompareAscending,
        EOSortOrdering.CompareCaseInsensitiveAscending,
        EOSortOrdering.CompareCaseInsensitiveDescending,
        EOSortOrdering.CompareDescending,        
    }, new String [] {
        "compareAscending",
        "compareCaseInsensitiveAscending",
        "compareCaseInsensitiveDescending",
        "compareDescending",
    });

    /**
     * The qualifiers EOSortOrdering.CompareAscending.. and friends are
     * actually 'special' and processed in a different/faster way when
     * sorting than a selector that would be created by
     * new NSSelector("compareAscending", ObjectClassArray). This method
     * eases the pain on creating those selectors from a string.
     * @param key sort key
     */
    // MOVEME: Should move to ERXArrayUtilities
    public static NSSelector sortSelectorWithKey(String key) {
        NSSelector result=null;
        if (key!=null) {
            result=(NSSelector)_selectorsByKey.objectForKey(key);
            if (result==null) result=new NSSelector(key, ERXConstant.ObjectClassArray);
        }
        return result;
    }

    /** Copies values from one EO to another using an array of Attributes */
    public static void replicateDataFromEOToEO(ERXGenericRecord r1, ERXGenericRecord r2, NSArray attributeNames){
        for(Enumeration e = attributeNames.objectEnumerator(); e.hasMoreElements();){
            String attributeName = (String)e.nextElement();
            r2.takeValueForKey(r1.valueForKey(attributeName), attributeName);
        }
    }

    /** Copies a relationship from one EO to another using the name of the relationship */
    public static void replicateRelationshipFromEOToEO(ERXGenericRecord r1, ERXGenericRecord r2, String relationshipName){
        for(Enumeration e = ((NSArray)r1.valueForKey(relationshipName)).objectEnumerator(); e.hasMoreElements();){
            ERXReplicableInterface replicableTarget = (ERXReplicableInterface)e.nextElement();
            r2.addObjectToBothSidesOfRelationshipWithKey(replicableTarget.replicate(r2.editingContext()), relationshipName);
        }
    }
}
