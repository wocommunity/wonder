/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSSet;

/**
 * The reason that this model group is needed is because the regular
 * EOModelGroup will fail to load a model if it has an entity name conflict.
 * While normally this could be considered a 'good thing' in the case of
 * EOPrototypes multiple EOModels might all need there own prototype entities
 * (in fact EOM requires it). This model group subclass will only print warning
 * messages when duplicate entity names are found.
 */
public class ERXModelGroup extends
        EOModelGroup {

    /** logging support */
    public static ERXLogger log = ERXLogger.getERXLogger(ERXModelGroup.class);
    private Hashtable       cache;

    /**
     * Default public constructor
     */
    public ERXModelGroup() {
        cache = new Hashtable();
    }

    /**
     * The only reason this method is needed is so our model group subclass is
     * used. Other than that it does the exact same thing as EOModelGroup's
     * implementation.
     * 
     * @return ERXModelGroup for all of the loaded bundles
     */
    public static EOModelGroup modelGroupForLoadedBundles() {
        EOModelGroup eomodelgroup = new ERXModelGroup();
        NSArray nsarray = NSBundle.frameworkBundles();
        int bundleCount = nsarray.count() + 1;

        if (log.isDebugEnabled()) log.debug("Loading bundles" + nsarray.valueForKey("name"));

        NSMutableArray bundles = new NSMutableArray(bundleCount);
        bundles.addObject(NSBundle.mainBundle());
        bundles.addObjectsFromArray(nsarray);
        for (int currentBundle = 0; currentBundle < bundleCount; currentBundle++) {
            NSBundle nsbundle = (NSBundle) bundles.objectAtIndex(currentBundle);
            NSArray paths = nsbundle.resourcePathsForResources("eomodeld", null);
            int pathCount = paths.count();
            for (int currentPath = 0; currentPath < pathCount; currentPath++) {
                String path = (String) paths.objectAtIndex(currentPath);
                String modelName = (NSPathUtilities.stringByDeletingPathExtension(NSPathUtilities
                        .lastPathComponent(path)));
                EOModel eomodel = eomodelgroup.modelNamed(modelName);
                if (eomodel == null) {
                    URL url = nsbundle.pathURLForResourcePath(path);
                    eomodelgroup.addModelWithPathURL(url);
                } else if (NSLog.debugLoggingAllowedForLevelAndGroups(1, 32768L)) {
                        NSLog.debug
                                .appendln("Ignoring model at path \"" + path + "\" because the model group "
                                        + eomodelgroup + " already contains the model from the path \""
                                        + eomodel.pathURL() + "\"");
                }
            }
        }
        // correcting an EOF Inheritance bug
        ((ERXModelGroup) eomodelgroup).checkInheritanceRelationships();
        return eomodelgroup;
    }

    /**
     * This implementation will load models that have entity name conflicts,
     * removing the offending entity. The reason this is needed is because
     * multiple models might have JDBC prototype entities which would cause
     * problems for the model group.
     * 
     * @param eomodel
     *            model to be added
     */
    public void addModel(EOModel eomodel) {
        Enumeration enumeration = _modelsByName.objectEnumerator();
        String name = eomodel.name();
        if (_modelsByName.objectForKey(name) != null) {
            log.warn("The model '" + name + "' (path: " + eomodel.pathURL() + ") cannot be added to model group " + this
                    + " because it already contains a model with that name.");
            return;
        }
        NSMutableSet nsmutableset = new NSMutableSet(128);
        NSSet nsset = new NSSet(eomodel.entityNames());
        while (enumeration.hasMoreElements()) {
            EOModel eomodel1 = (EOModel) enumeration.nextElement();
            nsmutableset.addObjectsFromArray(eomodel1.entityNames());
        }
        NSSet intersection = nsmutableset.setByIntersectingSet(nsset);
        if (intersection.count() != 0) {
            log.warn("The model '" + name + "' (path: " + eomodel.pathURL()
                    + ") has an entity name conflict with the entities " + intersection
                    + " already in the model group " + this);
            Enumeration e = intersection.objectEnumerator();
            while (e.hasMoreElements()) {
                String entityName = (String) e.nextElement();
                log.debug("Removing entity " + entityName + " from model " + name);
                eomodel.removeEntity(eomodel.entityNamed(entityName));
            }
        }
        eomodel.setModelGroup(this);
        _modelsByName.setObjectForKey(eomodel, eomodel.name());
        NSNotificationCenter.defaultCenter().postNotification("EOModelAddedNotification", eomodel);
    }

    /**
     * Corrects a strange EOF inheritance issue where if a model gets loaded and
     * an entity that has children located in a different model that hasn't been
     * loaded yet will not be setup correctly. Specifically when those child
     * entities are loaded they will not have their parentEntity relationship
     * set correctly.
     */
    public void checkInheritanceRelationships() {
        if (_subEntitiesCache != null && _subEntitiesCache.count() > 0) {
            for (Enumeration parentNameEnumerator = _subEntitiesCache.keyEnumerator(); parentNameEnumerator
                    .hasMoreElements();) {
                String parentName = (String) parentNameEnumerator.nextElement();
                NSArray children = (NSArray) _subEntitiesCache.objectForKey(parentName);
                EOEntity parent = entityNamed(parentName);
                for (Enumeration childrenEnumerator = children.objectEnumerator(); childrenEnumerator.hasMoreElements();) {
                    String childName = (String) childrenEnumerator.nextElement();
                    EOEntity child = entityNamed(childName);

                    if (child.parentEntity() != parent && !parent.subEntities().containsObject(child)) {
                        log.debug("Found entity: " + child.name() + " which should have: " + parent.name()
                                + " as it's parent.");
                        parent.addSubEntity(child);
                    }
                }
            }
        }
    }

    /**
     * Looks up the userInfo for the Entity with the specified entityName and
     * returns it if the code could be found.
     * 
     * @param ename
     *            the name from the Entity for which we want to the get
     *            entityCode
     * @return either the userInfo.entityCode or 0 if no entry could be found
     */
    public int entityCode(String ename) {
        return entityCode(entityNamed(ename));
    }

    /**
     * Looks up the userInfo for the Entity with the specified entityName and
     * returns it if the code could be found.
     * 
     * @param entity
     *            the Entity for which we want to the get entityCode
     *            
     * @return either the userInfo.entityCode or 0 if no entry could be found
     */
    public int entityCode(EOEntity entity) {
        Integer cachedValue = (Integer) cache.get(entity);
        if (cachedValue == null) {
            NSDictionary d = entity.userInfo();
            if (d == null) d = NSDictionary.EmptyDictionary;
            Object o = d.objectForKey("entityCode");
            cachedValue = o == null ? null : new Integer(o.toString()); 
            if (cachedValue == null) {
                cachedValue = new Integer(0);
            }
            cache.put(entity, cachedValue);
        }
        return cachedValue.intValue();
    }
}