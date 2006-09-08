/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.net.*;
import java.util.*;

import org.apache.log4j.*;

import com.webobjects.eoaccess.*;
import com.webobjects.foundation.*;
import com.webobjects.jdbcadaptor.*;

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
    public static Logger log = Logger.getLogger(ERXModelGroup.class);
    private Hashtable       cache;

    protected static boolean patchModelsOnLoad = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXModelGroup.patchModelsOnLoad", false);
     
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
        ERXModelGroup eomodelgroup = new ERXModelGroup();
        EOModelGroup.setDefaultGroup(eomodelgroup);
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
        eomodelgroup.checkInheritanceRelationships();
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
        if(eomodel.modelGroup() != this) {
        	eomodel.setModelGroup(this);
        }
        _modelsByName.setObjectForKey(eomodel, eomodel.name());
        NSNotificationCenter.defaultCenter().postNotification("EOModelAddedNotification", eomodel);
    }
    
    /**
     * Extends models by model-specific prototypes. You would use them by having an entity named
     * <code>EOModelPrototypes</code>, <code>EOJDBCModelPrototypes</code> or 
     * <code>EOJDBC&lt;PluginName&gt;ModelPrototypes</code> in your model. These are loaded after the
     * normal models, so you can override things here. Of course EOModeler knows nothing of them,
     * so you may need to copy all attributes over to a <code>EOPrototypes</code> entity that is
     * present only once in your model group. <br />
     * This class is used by the runtime when the property <code>er.extensions.ERXModelGroup.useExtendedPrototypes=true</code>.
     * @author ak
     */
    public static class Model extends EOModel {

        public Model(URL url) {
            super(url);
            log.info("init: " + name());
        }
        
        public void setModelGroup(EOModelGroup aGroup) {
            super.setModelGroup(aGroup);
            log.info("setModelGroup: " + name());
            if(aGroup != null) {
                ERXConfigurationManager.defaultManager().resetConnectionDictionaryInModel(this);
            }
        }

        /**
         * Utility for getting all names from an array of attributes.
         * @param attributes
         * @return
         */
        private NSArray namesForAttributes(NSArray attributes) {
            return (NSArray) attributes.valueForKey("name");
        }

        /**
         * Utility for getting all the attributes off an entity. If the entity
         * is null, an empty array is returned.
         * @param entity
         * @return
         */
        private NSArray attributesFromEntity(EOEntity entity) {
            NSArray result = NSArray.EmptyArray;
            if(entity != null) {
                result = entity.attributes();
                log.info("Attributes from " + entity.name() + ": " + result);
            }
            return result;
        }

        /**
         * Utility to add attributes to the prototype cache. As the attributes are
         * chosen by name, replace already existing ones.
         * @param attributes
         */
        private void addAttributesToPrototypesCache(NSArray attributes) {
            if(attributes.count() != 0) {
                NSArray keys = namesForAttributes(attributes);
                NSDictionary temp = new NSDictionary(attributes, keys);
                _prototypesByName.addEntriesFromDictionary(temp);
            }
        }
        
        /**
         * Create the prototype cache by walking a search order.
         *
         */
        private void createPrototypes() {
            log.info("Creating prototypes for model: " + name() + "->" + connectionDictionary());
            synchronized (_EOGlobalModelLock) {
                _prototypesByName = new NSMutableDictionary();
                NSArray adaptorPrototypes = NSArray.EmptyArray;
                EOAdaptor adaptor = EOAdaptor.adaptorWithModel(this);
                try {
                    adaptorPrototypes = adaptor.prototypeAttributes();
                } catch (Exception e) {
                    log.error(e, e);
                }
                NSArray globalAttributesFromModelGroup = attributesFromEntity(_group.entityNamed("EOPrototypes"));
                NSArray globalAttributesFromModel = attributesFromEntity(entityNamed("EOModelPrototypes"));
                
                NSArray adaptorAttributesFromModelGroup = attributesFromEntity(_group.entityNamed("EO" + adaptorName() + "Prototypes"));
                NSArray adaptorAttributesFromModel = attributesFromEntity(entityNamed("EO" + adaptorName() + "ModelPrototypes"));
                
                NSArray pluginAttributesFromModelGroup = NSArray.EmptyArray;
                NSArray pluginAttributesFromModel = NSArray.EmptyArray;
                
                if(adaptor instanceof JDBCAdaptor && !name().equals("erprototypes")) {
                    String plugin = (String) connectionDictionary().objectForKey("plugin");
                    if(plugin != null) {
                        pluginAttributesFromModelGroup = attributesFromEntity(_group.entityNamed("EOJDBC" + plugin + "Prototypes"));
                        pluginAttributesFromModel = attributesFromEntity(entityNamed("EOJDBC" + plugin + "ModelPrototypes"));
                    }
                }
                addAttributesToPrototypesCache(adaptorPrototypes);
                NSArray prototypesToHide = attributesFromEntity(_group.entityNamed("EOPrototypesToHide"));
                _prototypesByName.removeObjectsForKeys(namesForAttributes(prototypesToHide));
                
                addAttributesToPrototypesCache(globalAttributesFromModelGroup);
                addAttributesToPrototypesCache(adaptorAttributesFromModelGroup);
                addAttributesToPrototypesCache(pluginAttributesFromModelGroup);
                
                addAttributesToPrototypesCache(globalAttributesFromModel);
                addAttributesToPrototypesCache(adaptorAttributesFromModel);
                addAttributesToPrototypesCache(pluginAttributesFromModel);
            }
        }

        /**
         * Overridden to use our prototype creation method.
         */
        public EOAttribute prototypeAttributeNamed(String name) {
            synchronized (_EOGlobalModelLock) {
                if (_prototypesByName == null) {
                    createPrototypes();
                }
            }
            return (EOAttribute) super.prototypeAttributeNamed(name);
        }

        /**
         * Overridden to use our prototype creation method.
         */
        public NSArray availablePrototypeAttributeNames() {
            synchronized(_EOGlobalModelLock) {
                if(_prototypesByName == null) {
                    createPrototypes();
                }
            }
            return super.availablePrototypeAttributeNames();
        }

    }

    /**
     * Overridden to use our model class in the runtime.
     */
    public EOModel addModelWithPathURL(URL url) {
        EOModel model = null;
        if(patchModelsOnLoad) {
            model = new Model(url);
        } else {
            model = new EOModel(url);
        }
        addModel(model);
        return model;
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