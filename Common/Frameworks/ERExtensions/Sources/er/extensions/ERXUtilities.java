/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

/**
 * Diverse collection of utility methods for handling everything from
 * EOF to foundation. In the future this class will most likely be
 * split into more meaning full groups of utility methods.
 */
public class ERXUtilities {

    /** logging support */
    public static Logger log = Logger.getLogger(ERXUtilities.class);

    /**
     * @deprecated use ERXEOControlUtilities.addObjectToObjectOnBothSidesOfRelationshipWithKey(EOEnterpriseObject,EOEnterpriseObject,String)
     */
    public static void addObjectToObjectOnBothSidesOfRelationshipWithKey(EOEnterpriseObject addedObject, EOEnterpriseObject referenceObject, String key) {
        ERXEOControlUtilities.addObjectToObjectOnBothSidesOfRelationshipWithKey(addedObject, referenceObject, key);
    }

    /**
     * @deprecated use ERXEOControlUtilities.createAndInsertObject(EOEditingContext,String)
     */
    public static EOEnterpriseObject createEO(String entityName, EOEditingContext editingContext) {
        return ERXUtilities.createEO(entityName, editingContext, null);
    }
    
    /**
     * @deprecated use  createAndInsertObject(EOEditingContext,String, NSDictionary)
     */    
    public static EOEnterpriseObject createEO(String entityName,
                                              EOEditingContext editingContext,
                                              NSDictionary objectInfo) {
        return ERXEOControlUtilities.createAndInsertObject(editingContext, entityName, objectInfo);
    }

    /**
     * @deprecated use ERXEOControlUtilities.createAndAddObjectToRelationship(EOEditingContext,EOEnterpriseObject,String,String,NSDictionary);
     */
    public static EOEnterpriseObject createEOLinkedToEO(String entityName,
                                                        EOEditingContext editingContext,
                                                        String relationshipName,
                                                        EOEnterpriseObject eo) {
        return ERXEOControlUtilities.createAndAddObjectToRelationship(editingContext,eo,relationshipName,entityName,null);
    }

    /**
     * @deprecated use ERXEOControlUtilities.createAndAddObjectToRelationship(EOEditingContext,EOEnterpriseObject,String,String,NSDictionary);
     */
    public static EOEnterpriseObject createEOLinkedToEO(String entityName,
                                                        EOEditingContext editingContext,
                                                        String relationshipName,
                                                        EOEnterpriseObject eo,
                                                        NSDictionary objectInfo) {
        return ERXEOControlUtilities.createAndAddObjectToRelationship(editingContext,eo,relationshipName,entityName,objectInfo);
    }

    /**
     * @deprecated use ERXEOControlUtilities.localInstanceOfObject(EOEditingContext,EOEnterpriseObject);
     */
    public static EOEnterpriseObject localInstanceOfObject(EOEditingContext ec, EOEnterpriseObject eo) {
        return ERXEOControlUtilities.localInstanceOfObject(ec, eo);
    }

    /**
     * @deprecated use ERXEOControlUtilities.localInstancesOfObjects(EOEditingContext,NSArray);
     */
    public static NSArray localInstancesOfObjects(EOEditingContext ec, NSArray eos) {
        return ERXEOControlUtilities.localInstancesOfObjects(ec, eos);
    }    

    /**
     * @deprecated use ERXEOControlUtilities.sharedObjectWithFetchSpec(String, String)
     */
    public static EOEnterpriseObject sharedObjectWithFetchSpec(String fetchSpec, String entityName) {
        return ERXEOControlUtilities.sharedObjectWithFetchSpec(entityName, fetchSpec);
    }

    /**
     * @deprecated use ERXEOControlUtilities.sharedObjectWithPrimaryKey(String, object)
     */
    public static EOEnterpriseObject sharedObjectWithPrimaryKey(Object pk, String entityName) {
        return ERXEOControlUtilities.sharedObjectWithPrimaryKey(entityName, pk);
    }
    
    /**
     * @deprecated use ERXEOAccessUtilities.primaryKeyDictionaryForEntity(EOEditingContext, String)
     */
    public static NSDictionary primaryKeyDictionaryForEntity(EOEditingContext ec, String entityName) {
        return ERXEOAccessUtilities.primaryKeyDictionaryForEntity(ec,entityName);
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
            if (eo instanceof ERXEnterpriseObject)
                result.addObject(((ERXEnterpriseObject)eo).primaryKeyInTransaction());
            else
                result.addObject(EOUtilities.primaryKeyForObject(ec, eo));
        }
        return result;
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
        EOEntity e = ERXEOAccessUtilities.entityNamed(null, entityName);
        if (e.isReadOnly()) {
            e.setReadOnly(false);
            e.setCachesObjects(false);
            e.removeSharedObjectFetchSpecificationByName("FetchAll");
        } else {
            log.warn("MakeSharedEntityEditable: entity already editable: " + entityName);
        }
    }

    /**
     * deprecated see {@link ERXEOControlUtilities.dataSourceForArray(NSArray)}
     */
    public static EOArrayDataSource dataSourceForArray(NSArray array) {
        return ERXEOControlUtilities.dataSourceForArray(array);
    }

    /**
     * @deprecated see {@link ERXEOControlUtilities.arrayFromDataSource(EODataSource)}
     */
    public static NSArray arrayFromDataSource(EODataSource dataSource) {
        return ERXEOControlUtilities.arrayFromDataSource(dataSource);
    }
    
    /**
     * @deprecated use ERXArrayUtilities.sortArrayWithKey(NSMutableArray,String) instead
     */
    public static void sortEOsUsingSingleKey(NSMutableArray eos, String key) {
        ERXArrayUtilities.sortArrayWithKey(eos, key, EOSortOrdering.CompareCaseInsensitiveAscending);
    }

    /**
     * @deprecated use ERXArrayUtilities.sortArrayWithKey(NSMutableArray,String,NSSelector) instead
     */
    public static void sortEOsUsingSingleKey(NSMutableArray eos, String key, NSSelector selector) {
        ERXArrayUtilities.sortArrayWithKey(eos, key, selector);
    }

    /**
     * @deprecated use ERXValueUtilities.booleanValueForBindingOnComponentWithDefault(String,WOComponent,boolean) instead
     */
    public static boolean booleanValueForBindingOnComponentWithDefault(String binding, WOComponent component, boolean def) {
        return ERXValueUtilities.booleanValueForBindingOnComponentWithDefault(binding,component,def);
    }
    
    /**
     * @deprecated use ERXValueUtilities.booleanValue(Object)
     */
    public static boolean booleanValue(Object obj) {
        return ERXValueUtilities.booleanValue(obj);
    }

    /**
     * @deprecated use ERXValueUtilities.booleanValueWithDefault(Object,boolean)
     */
    public static boolean booleanValueWithDefault(Object obj, boolean def) {
        return ERXValueUtilities.booleanValueWithDefault(obj,def);
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
        EOEnterpriseObject lastEO = relationshipObjectWithObjectAndKeyPath(object, keyPath);
        EORelationship relationship = null;
        
        if (lastEO!=null) {
            EOEntity entity=ERXEOAccessUtilities.entityNamed(object.editingContext(), lastEO.entityName());
            String lastKey=ERXStringUtilities.lastPropertyKeyInKeyPath(keyPath);
            relationship=entity.relationshipNamed(lastKey);
        }
        return relationship;
    }

    
    public static NSDictionary relationshipEntityWithEntityAndKeyPath(EOEntity srcentity, String keyPath) {
        //keyPath is something like 'project.user.person.firstname'
        //we will get the Person entity
        if (keyPath.indexOf(".") == -1) {
            NSDictionary d = new NSDictionary(new Object[]{srcentity, keyPath}, new Object[]{"entity", "keyPath"});
            return d;
        }

        while (keyPath.indexOf(".") != -1) {
            String key = ERXStringUtilities.firstPropertyKeyInKeyPath(keyPath);
            EORelationship rel = srcentity.relationshipNamed(key);
            if (rel == null) {
                break;

            } else {
                srcentity = rel.destinationEntity();
                keyPath = ERXStringUtilities.keyPathWithoutFirstProperty(keyPath);
                
            }
        }
        NSDictionary d = new NSDictionary(new Object[]{srcentity, keyPath}, new Object[]{"entity", "keyPath"});
        return d;
    }
    
    public static EOEnterpriseObject relationshipObjectWithObjectAndKeyPath(EOEnterpriseObject object, String keyPath) {
        if(object == null) {
        	return null;
        }
    	EOEnterpriseObject lastEO=object;
        if (keyPath.indexOf(".")!=-1) {
            String partialKeyPath=ERXStringUtilities.keyPathWithoutLastProperty(keyPath);
            Object rawLastEO=object.valueForKeyPath(partialKeyPath);
            lastEO=rawLastEO instanceof EOEnterpriseObject ? (EOEnterpriseObject)rawLastEO : null;
        }
        return lastEO;
    }

    /**
     * Simple utility method for deleting an array
     * of objects from an editing context.
     * @param ec editing context to have objects deleted from
     * @param objects objects to be deleted.
     */
    public static void deleteObjects(EOEditingContext ec, NSArray objects) {
        if (ec == null)
            throw new RuntimeException("Attempting to delete objects with a null editing context!");
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
            if (bundle.name() != null)
                frameworkNames.addObject(bundle.name());
            else
                log.warn("Null framework name for bundle: " + bundle);
        }
        return frameworkNames;
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
        return intersection != null ? intersection : NSArray.EmptyArray;
    }

    /**
     * Simple utility method for getting all of the
     * entities for all of the models of a given
     * model group.
     * @param group eo model group
     * @return array of all entities for a given model group.
     */
    public static NSArray entitiesForModelGroup(EOModelGroup group) {
        return ERXArrayUtilities.flatten((NSArray)group.models().valueForKey("entities"));
    }

    /** entity name cache */
    private static NSMutableDictionary _entityNameEntityCache;
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
            if (_entityNameEntityCache == null) {
                _entityNameEntityCache = new NSMutableDictionary();
                for (Enumeration e = entitiesForModelGroup(ERXEOAccessUtilities.modelGroup(null)).objectEnumerator(); e.hasMoreElements();) {
                    EOEntity anEntity = (EOEntity)e.nextElement();
                    _entityNameEntityCache.setObjectForKey(anEntity, anEntity.name().toLowerCase());    
                }
            }
            entity = (EOEntity)_entityNameEntityCache.objectForKey(entityName.toLowerCase());
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
        //"java.lang.Throwable 	at er.extensions.ERXUtilities.stackTrace(ERXUtilities.java:429)".length()
        return result.substring(84);
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
     * @deprecated use ERXArrayUtilities.setFromArray(NSArray)
     */
    public static NSSet setFromArray(NSArray array) {
        return ERXArrayUtilities.setFromArray(array);
    }

    /**
     * @deprecated use ERXArrayUtilities.sortSelectorWithKey(String)
     */
    public static NSSelector sortSelectorWithKey(String key) {
        return ERXArrayUtilities.sortSelectorWithKey(key);
    }

    /** Copies values from one EO to another using an array of Attributes */
    public static void replicateDataFromEOToEO(EOEnterpriseObject r1, EOEnterpriseObject r2, NSArray attributeNames){
        for(Enumeration e = attributeNames.objectEnumerator(); e.hasMoreElements();){
            String attributeName = (String)e.nextElement();
            r2.takeValueForKey(r1.valueForKey(attributeName), attributeName);
        }
    }

    /** Copies a relationship from one EO to another using the name of the relationship */
    public static void replicateRelationshipFromEOToEO(EOEnterpriseObject r1, EOEnterpriseObject r2, String relationshipName){
        for(Enumeration e = ((NSArray)r1.valueForKey(relationshipName)).objectEnumerator(); e.hasMoreElements();){
            ERXReplicableInterface replicableTarget = (ERXReplicableInterface)e.nextElement();
            r2.addObjectToBothSidesOfRelationshipWithKey(replicableTarget.replicate(r2.editingContext()), relationshipName);
        }
    }

    /** Copies a relationship from one EO to another using the name of the relationship */
    public static void deplicateRelationshipFromEO(EOEnterpriseObject r1, String relationshipName){
        //System.out.println("r1 "+r1);
        //System.out.println("relationshipName "+relationshipName);
        //System.out.println("array "+r1.valueForKey(relationshipName));
        for(Enumeration e = ((NSArray)r1.valueForKey(relationshipName)).objectEnumerator(); e.hasMoreElements();){
            ERXReplicableInterface replicableTarget = (ERXReplicableInterface)e.nextElement();
            //System.out.println("replicableTarget "+replicableTarget);
            r1.removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject)replicableTarget,
                                                              relationshipName);
            replicableTarget.deplicate();
        }
    }

}
