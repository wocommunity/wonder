/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */

/* ERXUtilities.java created by max on Sat 24-Feb-2001 */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;
import java.util.Enumeration;
import java.util.TimeZone;
import java.io.*;

// Basic Utility Methods.
public class ERXUtilities {

    ////////////////////////////////////  log4j category  ///////////////////////////////////
    public static Category cat = Category.getInstance(ERXUtilities.class);

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

    public static EOEnterpriseObject createEO(String entityName,
                                              EOEditingContext editingContext) {
        return ERXUtilities.createEO(entityName,
                                    editingContext,
                                    null);
    }
    
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

    // This has the one advantage over the standard EOUtilites method of first checking the ecs.
    public static EOEnterpriseObject localInstanceOfObject(EOEditingContext ec, EOEnterpriseObject eo) {
        return eo != null && ec != null && eo.editingContext() != null && !ec.equals(eo.editingContext()) ?
        EOUtilities.localInstanceOfObject(ec, eo) : eo;
    }
    
    public static NSArray localInstancesOfObjects(EOEditingContext ec, NSArray eos) {
        if (eos == null)
            throw new RuntimeException("ERUtilites: localInstancesOfObjects: Array is null");
        if (ec == null)
            throw new RuntimeException("ERUtilites: localInstancesOfObjects: EditingContext is null");
        NSMutableArray localEos = new NSMutableArray();
        for (Enumeration e = eos.objectEnumerator(); e.hasMoreElements();) {
            localEos.addObject(localInstanceOfObject(ec, (EOEnterpriseObject)e.nextElement()));
        }
        return localEos;
    }    
    
    public static EOEnterpriseObject sharedObjectWithFetchSpec(String fetchSpec, String entityName) {
        return EOUtilities.objectWithFetchSpecificationAndBindings(EOSharedEditingContext.defaultSharedEditingContext(),
                                                                   entityName,
                                                                   fetchSpec,
                                                                   null);
    }

    public static EOEnterpriseObject sharedObjectWithPrimaryKey(Object pk, String entityName) {
        return EOUtilities.objectWithPrimaryKeyValue(EOSharedEditingContext.defaultSharedEditingContext(),
                                                     entityName,
                                                     pk);
    }
    

    public static NSDictionary primaryKeyDictionaryForEntity(EOEditingContext ec, String entityName) {
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

    static public NSArray deletedObjectsPKeys(EOEditingContext ec) {
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


    // Convenience method to get a unique ID from a sequence
    public static int getNextValFromSequenceNamed(String sequenceName, EOEditingContext ec) throws RuntimeException{
        String sqlString = "select "+sequenceName+".nextVal from dual";
        NSArray array = EOUtilities.rawRowsForSQL( ec, "ER", sqlString);
        if(array.count()==0)
            throw new RuntimeException("Unable to generate value from sequence");
        NSDictionary dictionary = (NSDictionary)array.objectAtIndex(0);
        NSArray valuesArray = dictionary.allValues();
        return ((Number)valuesArray.objectAtIndex(0)).intValue();
    }

    
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

    public static EOArrayDataSource dataSourceForArray(NSArray array) {
        EOArrayDataSource dataSource = null;
        if (array != null && array.count() > 0) {
            EOEnterpriseObject eo = (EOEnterpriseObject)array.objectAtIndex(0);
            dataSource = new EOArrayDataSource(eo.classDescription(), eo.editingContext());
            dataSource.setArray(array);
        }
        return dataSource;
    }

    public static NSArray arrayFromDataSource(EODataSource dataSource) {
        WODisplayGroup dg = new WODisplayGroup();
        dg.setDataSource(dataSource);
        dg.fetch(); // Have to fetch in the array, go figure.
        return dg.allObjects();
    }
    
    // WO5 This is from WOExtensions _RelationshipSupport covered under Apple Open Source License 1.2.
    public static void sortEOsUsingSingleKey(NSMutableArray eos, String key) {
        sortEOsUsingSingleKey(eos, key, EOSortOrdering.CompareCaseInsensitiveAscending);
    }

    public static void sortEOsUsingSingleKey(NSMutableArray eos, String key, NSSelector selector) {
        if (eos == null)
            throw new RuntimeException("Attempting to sort null array of eos.");
        if (key == null)
            throw new RuntimeException("Attepting to sort array of eos with null key.");
        EOSortOrdering.sortArrayUsingKeyOrderArray(eos,
                                                   new NSArray(new EOSortOrdering(key, selector)));
    }


    public static boolean booleanValueForBindingOnComponentWithDefault(String binding, WOComponent component, boolean def) {
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
    
    // WO5 This is from WOExtensions _RelationshipSupport covered under Apple Open Source License 1.2.
    public static boolean booleanValue(Object obj) {
        return booleanValueWithDefault(obj,false);
    }

    public static boolean booleanValueWithDefault(Object obj, boolean def) {
        boolean flag = true;
        if(obj != null) {
            if(obj instanceof Number) {
                if(((Number)obj).intValue() == 0)
                    flag = false;
            } else if(obj instanceof String) {
                String s = (String)obj;
                if(s.equalsIgnoreCase("no") || s.equalsIgnoreCase("false"))
                    flag = false;
		else if(s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true"))
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
    

    // This is nice because it does not depend on every path being modeled, ie this works with EOF inheritance.
    public static EORelationship relationshipWithObjectAndKeyPath(EOEnterpriseObject object, String keyPath) {
        EOEnterpriseObject lastEO=object;
        EORelationship relationship = null;
        if (keyPath.indexOf(".")!=-1) {
            String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(keyPath);
            Object rawLastEO=object.valueForKeyPath(partialKeyPath);
            lastEO=rawLastEO instanceof EOEnterpriseObject ? (EOEnterpriseObject)rawLastEO : null;
        }
        if (lastEO!=null) {
            EOEntity entity=EOModelGroup.defaultGroup().entityNamed(lastEO.entityName());
            String lastKey=KeyValuePath.lastPropertyKeyInKeyPath(keyPath);
            relationship=entity.relationshipNamed(lastKey);
        }
        return relationship;
    }

    public static void deleteObjects(EOEditingContext ec, NSArray objects) {
        if (objects != null && objects.count() > 0) {
            for (Enumeration e = objects.objectEnumerator(); e.hasMoreElements();)
                ec.deleteObject((EOEnterpriseObject)e.nextElement());            
        }
    }
    
    // This will return a list of all the framework names loaded into the application.
    public static NSArray allFrameworkNames() {
        NSMutableArray frameworkNames = new NSMutableArray();
        for (Enumeration e = NSBundle.frameworkBundles().objectEnumerator(); e.hasMoreElements();) {
            NSBundle bundle = (NSBundle)e.nextElement();
            String frameworkName = D2WModel.nameFromFrameworkBundle(bundle);
            if (frameworkName != null)
                frameworkNames.addObject(frameworkName);
            else
                cat.warn("Null framework name for bundle: " + bundle);
        }
        cat.info(frameworkNames);
        return frameworkNames;
    }

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

    public static NSArray entitiesForModelGroup(EOModelGroup group) {
        return ERXExtensions.flatten((NSArray)group.models().valueForKey("entities"));
    }

    // Returns the EOEntity for a case insensitive compare.
    private static NSMutableDictionary _enityNameEntityCache;
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

    public static EOEntity rootParentEntityForEntity(EOEntity entity) {
        EOEntity root = entity;
        while (root!=null && root.parentEntity() != null)
            root = root.parentEntity();
        return root;
    }
    
    private static NSTimestampFormatter _gregorianDateFormatterForJavaDate;
    public static NSTimestampFormatter gregorianDateFormatterForJavaDate() {
        if (_gregorianDateFormatterForJavaDate == null)
            _gregorianDateFormatterForJavaDate = new NSTimestampFormatter("%a %b %d %H:%M:%S %Z %Y") /* JC_WARNING - Removed the ommit natural language flag "<e2>".*/;
        return _gregorianDateFormatterForJavaDate;
    }
    
    // FIXME: Probablly should have an observer listening for ModelWasAdded notifications and
    //		clear this cache when it is called.  For now this method shouldn't be called
    //		until after all of the models have been loaded.
    /*
    private static NSMutableDictionary _entitiesByClass;
    public static NSDictionary entitiesByClass() {
        _entitiesByClass = new NSMutableDictionary();
        if (_entitiesByClass == null) {
            for (Enumeration e = EOModelGroup.defaultGroup().models().objectEnumerator(); e.hasMoreElements();) {
                for (Enumeration ee = ((EOModel)(e.nextElement())).entities().objectEnumerator(); ee.hasMoreElements();) {
                    
                }
            }
        }
        return _entitiesByClass;
    }
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

    public static String stackTrace(Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        PrintStream printStream = new PrintStream(baos);
        t.printStackTrace(printStream);
        return baos.toString();
    }

    public static interface BooleanOperation {
        public boolean value();
    }
    
    public static interface Operation {
        public Object value();
    }

    public static interface Callback {
        public Object invoke(Object ctx);
    }

    public static interface BooleanCallback {
        public boolean invoke(Object ctx);
    }
    
    public static final NSTimestamp DISTANT_FUTURE = new NSTimestamp(2999,1,1,1,1,1,TimeZone.getDefault());
    public static NSTimestamp distantFuture() { return DISTANT_FUTURE; }

    public static final NSTimestamp DISTANT_PAST = new NSTimestamp(0001,1,1,1,1,1,TimeZone.getDefault());
    public static NSTimestamp distantPast() { return DISTANT_PAST; }

    /**
     * This method has to be used with great care.
     * Any ec that has faults for object will continue to see object as an instance of
     * it original class. Beware of methods executed with existing contexts that may have fetched
     * objects with relationship to object.
     * The method saves the object to the database. So do not change the object before changing its
     * class, unless you really know what you are doing. This makes it nearly impossible to make
     * atomic changes.
     * objEC should not be used after calling this method.
     */
    public static EOEnterpriseObject changeClassOfObject(String classFieldName, Object classDescriptor, ERXGenericRecord object, EOEditingContext destEC) {
        EOEditingContext objEC = object.editingContext();
        EOGlobalID gID = objEC.globalIDForObject(object);
        String pKey = object.primaryKey();
        object = (ERXGenericRecord)ERXUtilities.localInstanceOfObject(objEC, object);
        object.takeStoredValueForKey(classDescriptor, classFieldName);
        try {
            objEC.saveChanges();
        } catch (Exception e) {
            cat.warn("changeClassOfObject caused the following exception: " + e.toString());
        }
        objEC.invalidateObjectsWithGlobalIDs(new NSArray(gID));
        /* We have to fetch from a new ec because the older one still knows about this object, eventhough we invalidated the object
        */
        EOEditingContext refetchEC = destEC == null ? ERXExtensions.newEditingContext() : destEC;
        EOEnterpriseObject newObject = EOUtilities.objectMatchingKeyAndValue(refetchEC, "User", "id", pKey);
        //System.err.println("New Object : " + newObject + "\n object class: " + newObject.getClass());
        return newObject;
    }

    public static String escapeApostrophe(String aString) {
        NSArray parts = NSArray.componentsSeparatedByString(aString,"'");
        return parts.componentsJoinedByString("");
    }

    public static NSSet setFromArray(NSArray array) {
        if (array == null || array.count() == 0)
            return new NSSet();
        else {
            Object [] objs = new Object[array.count()];
            objs = array.objects();
            return new NSSet(objs);
        }
    }


   /* The qualifiers EOSortOrdering.CompareAscending.. and friends are
    actually 'special' and processed in a different way when sorting than a selector that would be created
    by new NSSelector("compareAscending", ObjectClassArray). This method eases the pain on creating
    those selectors from a string */

    private final static NSDictionary _selectorsByKey=new NSDictionary(new NSSelector [] {
        EOSortOrdering.CompareAscending,
        EOSortOrdering.CompareCaseInsensitiveAscending,
        EOSortOrdering.CompareCaseInsensitiveDescending,
        EOSortOrdering.CompareDescending,        
    },
    new String [] {
        "compareAscending",
        "compareCaseInsensitiveAscending",
        "compareCaseInsensitiveDescending",
        "compareDescending",
    });
                                                                       
    public static NSSelector sortSelectorWithKey(String key) {
        NSSelector result=null;
        if (key!=null) {
            result=(NSSelector)_selectorsByKey.objectForKey(key);
            if (result==null) result=new NSSelector(key, ERXConstant.ObjectClassArray);
        }
        return result;
    }



}
