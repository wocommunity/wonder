/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOKeyValueCodingAdditions;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSPathUtilities;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSSet;
import com.webobjects.foundation._NSArrayUtilities;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

/**
 * Enhanced model group that supports connection dict switching, definable and predictable model orderings and stackable prototypes.
 * It also fixes some errors when loading prototypes and EOModeler backup files (Foo.emodeld~). The class is the meant to be the default model
 * group abd works in conjunction with ERXExtensions to set itself up on load. <br>
 * You <b>must</b> use EOModelGroup.defaultGroup() and not EOModelGroup.globalModelGroup() because only the former will result in this class getting 
 * created.
 */
public class ERXModelGroup extends EOModelGroup {

	/** logging support */
	public static Logger log = Logger.getLogger(ERXModelGroup.class);
	
	private Hashtable cache;

	/**
	 * <code>er.extensions.ERXModelGroup.patchModelsOnLoad</code> is a boolean that defines is the created should be a {@link Model} not a EOModel. 
	 * Default is false.
	 */
	protected static boolean patchModelsOnLoad = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXModelGroup.patchModelsOnLoad", false);
	
	/**
	 * <code>er.extensions.ERXModelGroup.flattenPrototypes</code> defines if the prototypes should get flattened. Default is true.
	 */
	protected static boolean flattenPrototypes = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXModelGroup.flattenPrototypes", true);
	
	/**
	 * <code>er.extensions.ERXModelGroup.prototypeModelNames</code> defines the names of the models that are prototypes. They
	 * get put in front of the model load order. The default is <code>erprototypes</code>
	 */
	protected NSArray _prototypeModelNames = ERXProperties.componentsSeparatedByStringWithDefault("er.extensions.ERXModelGroup.prototypeModelNames", "," ,new NSArray(ERXProperties.stringForKeyWithDefault("er.extensions.ERXModelGroup.prototypeModelName", "erprototypes")));

	/**
	 * <code>er.extensions.ERXModelGroup.modelLoadOrder</code> defines the load order of the models. When you use this property
	 * the bundle loading will be disregarded. There is no default value.
	 */
	protected NSArray _modelLoadOrder = ERXProperties.componentsSeparatedByStringWithDefault("er.extensions.ERXModelGroup.modelLoadOrder", ",", NSArray.EmptyArray);
	
	/**
	 * Nofitication that is sent when the model group was created form the bundle loading.
	 */
	public static final String ModelGroupAddedNotification = "ERXModelGroupAddedNotification";

	/**
	 * Default public constructor
	 */
	public ERXModelGroup() {
		cache = new Hashtable();
	}

	/**
	 * The only reason this method is needed is so our model group subclass is used. Other than that it does the exact
	 * same thing as EOModelGroup's implementation.
	 * 
	 * @return ERXModelGroup for all of the loaded bundles
	 */
	public void loadModelsFromLoadedBundles() {
		EOModelGroup.setDefaultGroup(this);
		NSArray nsarray = NSBundle.frameworkBundles();

		if (log.isDebugEnabled()) {
			log.debug("Loading bundles" + nsarray.valueForKey("name"));
		}
		
		NSMutableDictionary modelNameURLDictionary = new NSMutableDictionary();
		NSMutableArray modelNames = new NSMutableArray();
		NSMutableArray bundles = new NSMutableArray();
		bundles.addObject(NSBundle.mainBundle());
		bundles.addObjectsFromArray(nsarray);

		for (Enumeration e = bundles.objectEnumerator(); e.hasMoreElements(); ) {
			NSBundle nsbundle = (NSBundle) e.nextElement();
			NSArray paths = nsbundle.resourcePathsForResources("eomodeld", null);
			int pathCount = paths.count();
			for (int currentPath = 0; currentPath < pathCount; currentPath++) {
				String indexPath = (String) paths.objectAtIndex(currentPath);
				if(indexPath.endsWith(".eomodeld~/index.eomodeld")) {
					// AK: we don't want to use temp files. This is actually an error in the 
					// builds or it happens when you open and change models from installed frameworks
					// but I'm getting so annoyed by this that we just skip the models here
					log.info("Not adding model, it's only a temp file: " + indexPath);
					continue;
				}
				String modelPath = NSPathUtilities.stringByDeletingLastPathComponent(indexPath);
				String modelName = (NSPathUtilities.stringByDeletingPathExtension(NSPathUtilities.lastPathComponent(modelPath)));
				EOModel eomodel = modelNamed(modelName);
				if (eomodel == null) {
					URL url = nsbundle.pathURLForResourcePath(modelPath);
					modelNameURLDictionary.setObjectForKey(url, modelName);
					modelNames.addObject(modelName);
				}
				else if (NSLog.debugLoggingAllowedForLevelAndGroups(1, 32768L)) {
					NSLog.debug.appendln("Ignoring model at path \"" + modelPath + "\" because the model group " + this + " already contains the model from the path \"" + eomodel.pathURL() + "\"");
				}
			}
		}

		NSMutableArray modelURLs = new NSMutableArray();
		// First, add prototyes if specified
		for(Enumeration prototypeModelNamesEnum = _prototypeModelNames.objectEnumerator(); prototypeModelNamesEnum.hasMoreElements(); ) {
			String prototypeModelName = (String) prototypeModelNamesEnum.nextElement();
			URL prototypeModelURL = (URL) modelNameURLDictionary.removeObjectForKey(prototypeModelName);
			modelNames.removeObject(prototypeModelName);
			if (prototypeModelURL == null) {
				// AK: we throw for everything except erprototypes, as it is set by default
				if(!"erprototypes".equals(prototypeModelName)) {
					throw new IllegalArgumentException("You specified the prototype model '" + prototypeModelName + "' in your prototypeModelNames array, but it can not be found.");
				}
			}
			else {
				modelURLs.addObject(prototypeModelURL);
			}
		}
		// Next, add all models that are stated explicitely
		for(Enumeration modelLoadOrderEnum = _modelLoadOrder.objectEnumerator(); modelLoadOrderEnum.hasMoreElements(); ) {
			String modelName = (String) modelLoadOrderEnum.nextElement();
			URL modelURL = (URL) modelNameURLDictionary.removeObjectForKey(modelName);
			modelNames.removeObject(modelName);
			if (modelURL == null) {
				throw new IllegalArgumentException("You specified the model '" + modelName + "' in your modelLoadOrder array, but it can not be found.");
			}
			modelURLs.addObject(modelURL);
		}
		// Finally add all the rest
		for (Enumeration e = modelNames.objectEnumerator(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			modelURLs.addObject(modelNameURLDictionary.objectForKey(name));
		}

		Enumeration modelURLEnum = modelURLs.objectEnumerator();
		while (modelURLEnum.hasMoreElements()) {
			URL url = (URL) modelURLEnum.nextElement();
			addModelWithPathURL(url);
		}
		
		// correcting an EOF Inheritance bug
		checkInheritanceRelationships();
		
		if (!patchModelsOnLoad) {
			flattenPrototypes();
			Enumeration modelsEnum = EOModelGroup.defaultGroup().models().objectEnumerator();
			while (modelsEnum.hasMoreElements()) {
				EOModel model = (EOModel)modelsEnum.nextElement();
				preloadERXConstantClassesForModel(model);
			}
		}

		adjustLocalizedAttributes();
		NSNotificationCenter.defaultCenter().postNotification(ModelGroupAddedNotification, this);
		if (!patchModelsOnLoad) {
			NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("modelAddedHandler", ERXConstant.NotificationClassArray), EOModelGroup.ModelAddedNotification, null);
		}
	}

	/**
	 * This implementation will load models that have entity name conflicts, removing the offending entity. The reason
	 * this is needed is because multiple models might have JDBC prototype entities which would cause problems for the
	 * model group.
	 * 
	 * @param eomodel
	 *            model to be added
	 */
	public void addModel(EOModel eomodel) {
		Enumeration enumeration = _modelsByName.objectEnumerator();
		String name = eomodel.name();
		if (_modelsByName.objectForKey(name) != null) {
			log.warn("The model '" + name + "' (path: " + eomodel.pathURL() + ") cannot be added to model group " + this + " because it already contains a model with that name.");
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
			log.warn("The model '" + name + "' (path: " + eomodel.pathURL() + ") has an entity name conflict with the entities " + intersection + " already in the model group " + this);
			Enumeration e = intersection.objectEnumerator();
			while (e.hasMoreElements()) {
				String entityName = (String) e.nextElement();
				log.debug("Removing entity " + entityName + " from model " + name);
				eomodel.removeEntity(eomodel.entityNamed(entityName));
			}
		}
		if (eomodel.modelGroup() != this) {
			eomodel.setModelGroup(this);
		}
		_modelsByName.setObjectForKey(eomodel, eomodel.name());
		resetConnectionDictionaryInModel(eomodel);

		NSNotificationCenter.defaultCenter().postNotification(EOModelGroup.ModelAddedNotification, eomodel);
	}

	public static String sqlDumpDirectory() {
		return ERXSystem.getProperty("er.extensions.ERXModelGroup.sqlDumpDirectory");
	}
	
	private void dumpSchemaSQL(EOModel eomodel) {
		String dumpDir = sqlDumpDirectory();
		if(dumpDir != null) {
			EOAdaptor adaptor = EOAdaptor.adaptorWithModel(eomodel);
			if (adaptor instanceof JDBCAdaptor) {
				JDBCAdaptor jdbc = (JDBCAdaptor) adaptor;
				try {
					ERXSQLHelper helper = ERXSQLHelper.newSQLHelper(jdbc);
					String sql = helper.createSchemaSQLForEntitiesInModelAndOptions(eomodel.entities(), eomodel, helper.defaultOptionDictionary(true, true));
					File file = new File(dumpDir + File.separator + eomodel.name() + ".sql");
					ERXFileUtilities.writeInputStreamToFile(new ByteArrayInputStream(sql.getBytes()), file);
					log.info("Wrote Schema SQL to " + file);
				}
				catch (IOException e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				}
			}
		}
	}

	/**
	 * Extends models by model-specific prototypes. You would use them by having an entity named
	 * <code>EOModelPrototypes</code>, <code>EOJDBCModelPrototypes</code> or
	 * <code>EOJDBC&lt;PluginName&gt;ModelPrototypes</code> in your model. These are loaded after the normal models,
	 * so you can override things here. Of course EOModeler knows nothing of them, so you may need to copy all
	 * attributes over to a <code>EOPrototypes</code> entity that is present only once in your model group. <br />
	 * This class is used by the runtime when the property
	 * <code>er.extensions.ERXModelGroup.useExtendedPrototypes=true</code>.
	 * 
	 * @author ak
	 */
	public static class Model extends EOModel {

		public Model(URL url) {
			super(url);
		}

		public void setModelGroup(EOModelGroup aGroup) {
			super.setModelGroup(aGroup);
			if (aGroup != null) {
				((ERXModelGroup) aGroup).resetConnectionDictionaryInModel(this);
			}
		}

		/**
		 * Utility for getting all names from an array of attributes.
		 * 
		 * @param attributes
		 * @return
		 */
		private NSArray namesForAttributes(NSArray attributes) {
			return (NSArray) attributes.valueForKey("name");
		}

		/**
		 * Utility for getting all the attributes off an entity. If the entity is null, an empty array is returned.
		 * 
		 * @param entity
		 * @return
		 */
		private NSArray attributesFromEntity(EOEntity entity) {
			NSArray result = NSArray.EmptyArray;
			if (entity != null) {
				result = entity.attributes();
				log.info("Attributes from " + entity.name() + ": " + result);
			}
			return result;
		}

		/**
		 * Utility to add attributes to the prototype cache. As the attributes are chosen by name, replace already
		 * existing ones.
		 * 
		 * @param attributes
		 */
		private void addAttributesToPrototypesCache(NSArray attributes) {
			if (attributes.count() != 0) {
				NSArray keys = namesForAttributes(attributes);
				NSDictionary temp = new NSDictionary(attributes, keys);
				_prototypesByName.addEntriesFromDictionary(temp);
			}
		}

		/**
		 * Utility to add attributes to the prototype cache. As the attributes are chosen by name, replace already
		 * existing ones.
		 * 
		 * @param attributes
		 */
		private void addAttributesToPrototypesCache(EOEntity entity) {
			addAttributesToPrototypesCache(attributesFromEntity(entity));
		}

		/**
		 * Create the prototype cache by walking a search order.
		 * 
		 */
		private void createPrototypes() {
			log.info("Creating prototypes for model: " + name() + "->" + connectionDictionary());
			synchronized (_EOGlobalModelLock) {
				_prototypesByName = new NSMutableDictionary();
				String name = name();
				NSArray adaptorPrototypes = NSArray.EmptyArray;
				EOAdaptor adaptor = EOAdaptor.adaptorWithModel(this);
				try {
					adaptorPrototypes = adaptor.prototypeAttributes();
				}
				catch (Exception e) {
					log.error(e, e);
				}
				addAttributesToPrototypesCache(adaptorPrototypes);
				NSArray prototypesToHide = attributesFromEntity(_group.entityNamed("EOPrototypesToHide"));
				_prototypesByName.removeObjectsForKeys(namesForAttributes(prototypesToHide));

				String plugin = null;
				if (adaptor instanceof JDBCAdaptor && !name().equalsIgnoreCase("erprototypes")) {
					plugin = (String) connectionDictionary().objectForKey("plugin");
				}

				addAttributesToPrototypesCache(_group.entityNamed("EOPrototypes"));
				addAttributesToPrototypesCache(_group.entityNamed("EO" + adaptorName() + "Prototypes"));
				if (plugin != null) {
					addAttributesToPrototypesCache(_group.entityNamed("EOJDBC" + plugin + "Prototypes"));
				}

				addAttributesToPrototypesCache(entityNamed("EO" + name + "Prototypes"));
				addAttributesToPrototypesCache(entityNamed("EO" + adaptorName() + name + "Prototypes"));
				if (plugin != null) {
					addAttributesToPrototypesCache(entityNamed("EOJDBC" + plugin + name + "Prototypes"));
				}
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
			return super.prototypeAttributeNamed(name);
		}

		/**
		 * Overridden to use our prototype creation method.
		 */
		public NSArray availablePrototypeAttributeNames() {
			synchronized (_EOGlobalModelLock) {
				if (_prototypesByName == null) {
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
		if (patchModelsOnLoad) {
			model = new Model(url);
		}
		else {
			model = new EOModel(url);
		}
		addModel(model);
		return model;
	}

	/**
	 * Corrects a strange EOF inheritance issue where if a model gets loaded and an entity that has children located in
	 * a different model that hasn't been loaded yet will not be setup correctly. Specifically when those child entities
	 * are loaded they will not have their parentEntity relationship set correctly.
	 */
	public void checkInheritanceRelationships() {
		if (_subEntitiesCache != null && _subEntitiesCache.count() > 0) {
			for (Enumeration parentNameEnumerator = _subEntitiesCache.keyEnumerator(); parentNameEnumerator.hasMoreElements();) {
				String parentName = (String) parentNameEnumerator.nextElement();
				NSArray children = (NSArray) _subEntitiesCache.objectForKey(parentName);
				EOEntity parent = entityNamed(parentName);
				for (Enumeration childrenEnumerator = children.objectEnumerator(); childrenEnumerator.hasMoreElements();) {
					String childName = (String) childrenEnumerator.nextElement();
					EOEntity child = entityNamed(childName);

					if (child.parentEntity() != parent && !parent.subEntities().containsObject(child)) {
						log.debug("Found entity: " + child.name() + " which should have: " + parent.name() + " as it's parent.");
						parent.addSubEntity(child);
					}
				}
			}
		}
	}

	/**
	 * Looks up the userInfo for the Entity with the specified entityName and returns it if the code could be found.
	 * 
	 * @param ename
	 *            the name from the Entity for which we want to the get entityCode
	 * @return either the userInfo.entityCode or 0 if no entry could be found
	 */
	public int entityCode(String ename) {
		return entityCode(entityNamed(ename));
	}

	/**
	 * Looks up the userInfo for the Entity with the specified entityName and returns it if the code could be found.
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
			if (d == null)
				d = NSDictionary.EmptyDictionary;
			Object o = d.objectForKey("entityCode");
			cachedValue = o == null ? null : new Integer(o.toString());
			if (cachedValue == null) {
				cachedValue = new Integer(0);
			}
			cache.put(entity, cachedValue);
		}
		return cachedValue.intValue();
	}

	public static boolean patchModelsOnLoad() {
		return patchModelsOnLoad;
	}
	
	/**
	 * Returns whether or not the given entity is a prototype.
	 */
	public static boolean isPrototypeEntity(EOEntity entity) {
		return ERXModelGroup.isPrototypeEntityName(entity.name());
	}
	
	/**
	 * Returns whether or not the given entity name is a prototype entity
	 */
	public static boolean isPrototypeEntityName(String entityName) {
		return (entityName.startsWith("EO") && entityName.endsWith("Prototypes"));
	}

	/**
	 * Called when a model is loaded. This will reset the connection dictionary and insert the correct EOPrototypes if
	 * those are used
	 * 
	 * @param n
	 *            notification posted when a model is loaded. The object is the model.
	 */
	public void modelAddedHandler(NSNotification n) {
		EOModel model = (EOModel) n.object();
		resetConnectionDictionaryInModel(model);
	}

	/**
	 * Copies an attribute to a new name.
	 * @param entity
	 * @param attribute
	 * @param newName
	 * @return
	 */
	protected EOAttribute cloneAttribute(EOEntity entity, EOAttribute attribute, String newName) {
		// NOTE: order is important here. To add the prototype,
		// we need it in the entity and we need a name to add it there
		EOAttribute copy = new EOAttribute();
		copy.setName(newName);
		entity.addAttribute(copy);
		copy.setPrototype(attribute.prototype());
		copy.setColumnName(attribute.columnName());
		copy.setExternalType(attribute.externalType());
		copy.setValueType(attribute.valueType());
		copy.setPrecision(attribute.precision());
		copy.setAllowsNull(attribute.allowsNull());
		copy.setClassName(attribute.className());
		copy.setWidth(attribute.width());
		copy.setScale(attribute.scale());
		copy.setExternalType(attribute.externalType());
		return copy;
	}

	protected void adjustLocalizedAttributes() {
		for (Enumeration enumerator = models().objectEnumerator(); enumerator.hasMoreElements();) {
			EOModel model = (EOModel) enumerator.nextElement();
			for (Enumeration e1 = model.entities().objectEnumerator(); e1.hasMoreElements();) {
				EOEntity entity = (EOEntity) e1.nextElement();
				adjustLocalizedAttributes(entity);
			}
		}
	}

	protected void adjustLocalizedAttributes(EOEntity entity) {
		NSArray attributes = entity.attributes().immutableClone();
		NSArray classProperties = entity.classProperties().immutableClone();
		NSArray attributesUsedForLocking = entity.attributesUsedForLocking().immutableClone();
		if (attributes == null)
			attributes = NSArray.EmptyArray;
		if (classProperties == null)
			classProperties = NSArray.EmptyArray;
		if (attributesUsedForLocking == null)
			attributesUsedForLocking = NSArray.EmptyArray;
		NSMutableArray mutableClassProperties = classProperties.mutableClone();
		NSMutableArray mutableAttributesUsedForLocking = attributesUsedForLocking.mutableClone();
		if (attributes != null) {
			for (Enumeration e = attributes.objectEnumerator(); e.hasMoreElements();) {
				EOAttribute attribute = (EOAttribute) e.nextElement();
				boolean isClassProperty = classProperties.containsObject(attribute);
				boolean isUsedForLocking = attributesUsedForLocking.containsObject(attribute);
				Object languagesObject = attribute.userInfo() != null ? attribute.userInfo().objectForKey("ERXLanguages") : null;
				if (languagesObject != null && !(languagesObject instanceof NSArray)) {
					languagesObject = entity.model().userInfo() != null ? entity.model().userInfo().objectForKey("ERXLanguages") : null;
				}
				NSArray languages = (languagesObject != null ? (NSArray) languagesObject : NSArray.EmptyArray);
				if (languages.count() > 0) {
					String name = attribute.name();
					String columnName = attribute.columnName();
					for (int i = 0; i < languages.count(); i++) {
						String language = (String) languages.objectAtIndex(i);
						String newName = name + "_" + language;
						// columnName = columnName.replaceAll("_(\\w)$", "_" + language);
						EOAttribute copy = cloneAttribute(entity, attribute, newName);

						String newColumnName = columnName + "_" + language;
						copy.setColumnName(newColumnName);

						if (isClassProperty) {
							mutableClassProperties.addObject(copy);
						}
						if (isUsedForLocking) {
							mutableAttributesUsedForLocking.addObject(copy);
						}
					}
					entity.removeAttribute(attribute);
					mutableClassProperties.removeObject(attribute);
					mutableAttributesUsedForLocking.removeObject(attribute);
				}
			}
			entity.setAttributesUsedForLocking(mutableAttributesUsedForLocking);
			entity.setClassProperties(mutableClassProperties);
		}
	}

	private String getProperty(String key, String alternateKey, String defaultValue) {
		String value = ERXSystem.getProperty(key);
		if (value == null) {
			value = ERXSystem.getProperty(alternateKey);
		}
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	private String getProperty(String key, String alternateKey) {
		return getProperty(key, alternateKey, null);
	}

	protected void fixOracleDictionary(EOModel model) {
		String modelName = model.name();
		String serverName = getProperty(modelName + ".DBServer", "dbConnectServerGLOBAL");
		String userName = getProperty(modelName + ".DBUser", "dbConnectUserGLOBAL");
		String passwd = getProperty(modelName + ".DBPassword", "dbConnectPasswordGLOBAL");

		NSMutableDictionary newConnectionDictionary = new NSMutableDictionary(model.connectionDictionary());
		if (serverName != null)
			newConnectionDictionary.setObjectForKey(serverName, "serverId");
		if (userName != null)
			newConnectionDictionary.setObjectForKey(userName, "userName");
		if (passwd != null)
			newConnectionDictionary.setObjectForKey(passwd, "password");
		model.setConnectionDictionary(newConnectionDictionary);
	}

	protected void fixFlatDictionary(EOModel model) {
		String aModelName = model.name();
		String path = getProperty(aModelName + ".DBPath", "dbConnectPathGLOBAL");
		if (path != null) {
			if (path.indexOf(" ") != -1) {
				NSArray a = NSArray.componentsSeparatedByString(path, " ");
				if (a.count() == 2) {
					path = ERXFileUtilities.pathForResourceNamed((String) a.objectAtIndex(0), (String) a.objectAtIndex(1), null);
				}
			}
		}
		else {
			// by default we take <modelName>.db in the directory we
			// found the model
			path = model.pathURL().getFile();
			path = NSPathUtilities.stringByDeletingLastPathComponent(path);
			path = NSPathUtilities.stringByAppendingPathComponent(path, model.name() + ".db");
		}
		NSMutableDictionary newConnectionDictionary = new NSMutableDictionary(model.connectionDictionary());
		if (path != null)
			newConnectionDictionary.setObjectForKey(path, "path");
		if (ERXConfigurationManager.defaultManager().operatingSystem() == ERXConfigurationManager.WindowsOperatingSystem)
			newConnectionDictionary.setObjectForKey("\r\n", "rowSeparator");
		model.setConnectionDictionary(newConnectionDictionary);
	}

	protected void fixOpenBaseDictionary(EOModel model) {
		String aModelName = model.name();
		String db = getProperty(aModelName + ".DBDatabase", "dbConnectDatabaseGLOBAL");
		String h = getProperty(aModelName + ".DBHostName", "dbConnectHostNameGLOBAL");
		NSMutableDictionary newConnectionDictionary = new NSMutableDictionary(model.connectionDictionary());
		if (db != null)
			newConnectionDictionary.setObjectForKey(db, "databaseName");
		if (h != null)
			newConnectionDictionary.setObjectForKey(h, "hostName");
		model.setConnectionDictionary(newConnectionDictionary);
	}

	protected void fixJDBCDictionary(EOModel model) {
		String aModelName = model.name();

		boolean poolConnections = ERXJDBCAdaptor.useConnectionBroker();

		String url = getProperty(aModelName + ".URL", "dbConnectURLGLOBAL");
		String userName = getProperty(aModelName + ".DBUser", "dbConnectUserGLOBAL");
		String passwd = getProperty(aModelName + ".DBPassword", "dbConnectPasswordGLOBAL");
		String driver = getProperty(aModelName + ".DBDriver", "dbConnectDriverGLOBAL");
		String serverName = getProperty(aModelName + ".DBServer", "dbConnectServerGLOBAL");
		String h = getProperty(aModelName + ".DBHostName", "dbConnectHostNameGLOBAL");
		String jdbcInfo = getProperty(aModelName + ".DBJDBCInfo", "dbConnectJDBCInfoGLOBAL");

		// additional information used for ERXJDBCConnectionBroker
		NSMutableDictionary poolingDictionary = new NSMutableDictionary();
		if (poolConnections) {
			poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBMinConnections", "dbMinConnectionsGLOBAL", "1"), "minConnections");
			poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBMaxConnections", "dbMaxConnectionsGLOBAL", "20"), "maxConnections");
			poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBLogPath", "dbLogPathGLOBAL", "/tmp/ERXJDBCConnectionBroker_@@name@@_@@WOPort@@.log"), "logPath");
			poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBConnectionRecycle", "dbConnectionRecycleGLOBAL", "1.0"), "connectionRecycle");
			poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBMaxCheckout", "dbMaxCheckoutGLOBAL", "86400"), "maxCheckout");
			poolingDictionary.setObjectForKey(getProperty(aModelName + ".DBDebugLevel", "dbDebugLevelGLOBAL", "1"), "debugLevel");
		}

		NSDictionary jdbcInfoDictionary = null;
		if (jdbcInfo != null && jdbcInfo.length() > 0 && jdbcInfo.charAt(0) == '^') {
			String modelName = jdbcInfo.substring(1, jdbcInfo.length());
			EOModel modelForCopy = model.modelGroup().modelNamed(modelName);
			if (modelForCopy != null && modelForCopy != model) {
				jdbcInfoDictionary = (NSDictionary) modelForCopy.connectionDictionary().objectForKey("jdbc2Info");
			}
			else {
				log.warn("Unable to find model named \"" + modelName + "\"");
				jdbcInfo = null;
			}
		}

		String plugin = getProperty(aModelName + ".DBPlugin", "dbConnectPluginGLOBAL");

		// build the URL if we have a Postgresql plugin
		if ("Postgresql".equals(plugin) && ERXStringUtilities.stringIsNullOrEmpty(url) && !ERXStringUtilities.stringIsNullOrEmpty(serverName) && !ERXStringUtilities.stringIsNullOrEmpty(h)) {
			url = "jdbc:postgresql://" + h + "/" + serverName;
		}

		NSDictionary connectionDictionary = model.connectionDictionary();
		if (connectionDictionary == null) {
			connectionDictionary = new NSMutableDictionary();
			ERXModelGroup.log.warn("The EOModel '" + model.name() + "' does not have a connection dictionary, providing an empty one");
			model.setConnectionDictionary(connectionDictionary);
		}

		NSMutableDictionary newConnectionDictionary = new NSMutableDictionary(connectionDictionary);
		if (url != null)
			newConnectionDictionary.setObjectForKey(url, "URL");
		if (userName != null)
			newConnectionDictionary.setObjectForKey(userName, "username");
		if (passwd != null)
			newConnectionDictionary.setObjectForKey(passwd, "password");
		if (driver != null)
			newConnectionDictionary.setObjectForKey(driver, "driver");
		if (jdbcInfoDictionary != null) {
			newConnectionDictionary.setObjectForKey(jdbcInfoDictionary, "jdbc2Info");
		}
		else if (jdbcInfo != null) {
			NSDictionary d = (NSDictionary) NSPropertyListSerialization.propertyListFromString(jdbcInfo);
			if (d != null)
				newConnectionDictionary.setObjectForKey(d, "jdbc2Info");
			else
				newConnectionDictionary.removeObjectForKey("jdbc2Info");
		}
		if (plugin != null) {
			newConnectionDictionary.setObjectForKey(plugin, "plugin");
		}

		// set the information for ERXJDBCConnectionBroker
		newConnectionDictionary.addEntriesFromDictionary(poolingDictionary);

		String removeJdbc2Info = getProperty(aModelName + ".removeJdbc2Info", "dbRemoveJdbc2InfoGLOBAL", "true");
		if (ERXValueUtilities.booleanValue(removeJdbc2Info)) {
			newConnectionDictionary.removeObjectForKey("jdbc2Info");
		}

		// We want to clean up our connection dictionaries so all our models match.  When EOF
		// compares connection dictionaries, undefined plugin is not the same as plugin = ""
		// even though it semantically is the same.  So we are normalizing our connection
		// dictionaries here by removing blank keys that we know about.
		String pluginCheck = (String)newConnectionDictionary.objectForKey("plugin");
		if (pluginCheck != null && pluginCheck.length() == 0){
			newConnectionDictionary.removeObjectForKey("plugin");
		}
		String driverCheck = (String)newConnectionDictionary.objectForKey("driver");
		if (driverCheck != null && driverCheck.length() == 0){
			newConnectionDictionary.removeObjectForKey("driver");
		}

		model.setConnectionDictionary(newConnectionDictionary);

		String[] keysThatMatter = { "URL", "username", "password", "driver", "plugin" };
		Enumeration modelsEnum = model.modelGroup().models().objectEnumerator();
		while (modelsEnum.hasMoreElements()) {
			EOModel otherModel = (EOModel)modelsEnum.nextElement();
			if (otherModel != model) {
				NSDictionary otherConnectionDictionary = otherModel.connectionDictionary();
				if (otherConnectionDictionary != null) {
					boolean valuesThatMatterMatch = true;
					for (int keyNum = 0; valuesThatMatterMatch && keyNum < keysThatMatter.length; keyNum ++) {
						String thisValue = (String)newConnectionDictionary.objectForKey(keysThatMatter[keyNum]);
						String otherValue = (String)otherConnectionDictionary.objectForKey(keysThatMatter[keyNum]);
						valuesThatMatterMatch = ERXStringUtilities.stringEqualsString(thisValue, otherValue);
					}
					if (valuesThatMatterMatch && !newConnectionDictionary.equals(otherConnectionDictionary)) {
						throw new IllegalArgumentException("The connection dictionaries for " + model.name() + " and " + otherModel.name() + " have the same URL, username, password, driver, and plugin, but the connection dictionaries are not equal.  This is often caused by jdbc2Info not matching between the two.  One fix for this is to set " + model.name() + ".removeJdbc2Info=true and " + otherModel.name() + ".removeJdbc2Info=true in your Properties file. (" + model.name() + "=" + newConnectionDictionary + "; and " + otherModel.name() + "=" + otherConnectionDictionary + ").");
					}
				}
			}
		}
	}

	/**
	 * Resets the connection dictionary to the specified values that are in the defaults. This method will look for
	 * defaults in the form:
	 * 
	 * <pre><code>
	 *   		&lt;MODELNAME&gt;.DBServer
	 *   		&lt;MODELNAME&gt;.DBUser
	 *   		&lt;MODELNAME&gt;.DBPassword
	 *   		&lt;MODELNAME&gt;.URL (for JDBC)        
	 * </code></pre>
	 * 
	 * if the serverName and username both exists, we overwrite the connection dict (password is optional). Otherwise we
	 * fall back to what's in the model.
	 * 
	 * Likewise default values can be specified of the form:
	 * 
	 * <pre><code>
	 *   dbConnectUserGLOBAL
	 *   dbConnectPasswordGLOBAL
	 *   dbConnectURLGLOBAL
	 * </code></pre>
	 * 
	 * @param model
	 *            to be reset
	 */
	public void resetConnectionDictionaryInModel(EOModel model) {
		if (model == null) {
			throw new IllegalArgumentException("Model can't be null");
		}
		String modelName = model.name();
		log.debug("Adjusting " + modelName);
		NSDictionary old = model.connectionDictionary();

		if (model.adaptorName() == null) {
			log.info("Skipping model '" + modelName + "', it has no adaptor name set");
			return;
		}

		NSDictionary databaseConfig = databaseConfigForModel(model);
		if (databaseConfig != null) {
			NSDictionary connectionDictionary = (NSDictionary) databaseConfig.objectForKey("connectionDictionary");
			model.setConnectionDictionary(connectionDictionary);
		}

		if (model.adaptorName().indexOf("Oracle") != -1) {
			fixOracleDictionary(model);
		}
		else if (model.adaptorName().indexOf("Flat") != -1) {
			fixFlatDictionary(model);
		}
		else if (model.adaptorName().indexOf("OpenBase") != -1) {
			fixOpenBaseDictionary(model);
		}
		else if (model.adaptorName().indexOf("JDBC") != -1) {
			fixJDBCDictionary(model);
		}

		if (log.isDebugEnabled() && old != null && !old.equals(model.connectionDictionary()) && model.connectionDictionary() != null) {
			NSMutableDictionary dict = model.connectionDictionary().mutableClone();
			if (dict.objectForKey("password") != null) {
				dict.setObjectForKey("<deleted for log>", "password");
				log.debug("New Connection Dictionary for " + modelName + ": " + dict);
			}
		}

		fixPrototypesForModel(model);
	}

	protected String prototypeEntityNameForModel(EOModel model) {
		String modelName = model.name();
		String prototypeEntityName = getProperty(modelName + ".EOPrototypesEntity", "dbEOPrototypesEntityGLOBAL");
		NSDictionary databaseConfig = databaseConfigForModel(model);
		if (prototypeEntityName == null && databaseConfig != null) {
			prototypeEntityName = (String) databaseConfig.objectForKey("prototypeEntityName");
		}

		if (prototypeEntityName == null && !(ERXModelGroup.patchModelsOnLoad())) {
			String pluginName = ERXEOAccessUtilities.guessPluginName(model);
			if (pluginName != null) {
				String pluginPrototypeEntityName = "EOJDBC" + pluginName + "Prototypes";
				// This check isn't technically necessary since
				// it doesn't down below, but since
				// we are guessing here, I don't want themt o
				// get a warning about the prototype not
				// being found if they aren't even using Wonder
				// prototypes.
				if (model.modelGroup().entityNamed(pluginPrototypeEntityName) != null) {
					prototypeEntityName = pluginPrototypeEntityName;
				}
			}
		}
		return prototypeEntityName;
	}

	protected NSDictionary databaseConfigForModel(EOModel model) {
		// Support for EODatabaseConfig from EntityModeler. The value of YourEOModelName.DBConfigName is
		// used to lookup the corresponding EODatabaseConfig name from user info. The connection dictionary
		// defined in the databaseConfig section completely replaces the connection dictionary in the
		// EOModel. After the initial replacement, all the additional PW model configurations are then
		// applied to the new dictionary.
		String modelName = model.name();
		String databaseConfigName = getProperty(modelName + ".DBConfigName", "dbConfigNameGLOBAL");
		NSDictionary databaseConfig = null;
		NSDictionary userInfo = model.userInfo();
		if (userInfo != null) {
			NSDictionary entityModelerDictionary = (NSDictionary) userInfo.objectForKey("_EntityModeler");
			if (entityModelerDictionary != null) {
				if (databaseConfigName == null) {
					databaseConfigName = (String) entityModelerDictionary.objectForKey("activeDatabaseConfigName");
				}
				if (databaseConfigName != null) {
					NSDictionary databaseConfigsDictionary = (NSDictionary) entityModelerDictionary.objectForKey("databaseConfigs");
					if (databaseConfigsDictionary != null) {
						databaseConfig = (NSDictionary) databaseConfigsDictionary.objectForKey(databaseConfigName);
					}
				}
			}
		}
		return databaseConfig;
	}

	private void fixPrototypesForModel(EOModel model) {
		String modelName = model.name();
		// based on an idea from Stefan Apelt <stefan@tetlabors.de>
		String f = getProperty(modelName + ".EOPrototypesFile", "EOPrototypesFileGLOBAL");
		if (f != null) {
			NSDictionary dict = (NSDictionary) NSPropertyListSerialization.propertyListFromString(ERXStringUtilities.stringFromResource(f, "", null));
			if (dict != null) {
				if (log.isDebugEnabled()) {
					log.debug("Adjusting prototypes from " + f);
				}
				EOEntity proto = model.entityNamed("EOPrototypes");
				if (proto == null) {
					log.warn("No prototypes found in model named \"" + modelName + "\", although the EOPrototypesFile default was set!");
				}
				else {
					model.removeEntity(proto);
					proto = new EOEntity(dict, model);
					proto.awakeWithPropertyList(dict);
					model.addEntity(proto);
				}
			}
		}

		if (patchModelsOnLoad) {
			flattenPrototypes();
			preloadERXConstantClassesForModel(model);
		}
	}

	/**
	 * The classes referenced in the ERXConstantClassName field of an attribute's userInfo needs to be
	 * class-loaded before the attribute is used.  This method enumerates all the attributes of all
	 * the entities in a model looking for those class names, and class loads them.  Because the constant
	 * class name could be an inner class, it tries the raw value first, then replaces the last dot of 
	 * the class name with a dollar sign and tries again.
	 *   
	 * @param model the model to load constants for
	 * @throws IllegalArgumentException if the ERXConstantClassName cannot be resolved. 
	 */
	protected void preloadERXConstantClassesForModel(EOModel model) {
		for (Enumeration entitiesEnum = model.entities().objectEnumerator(); entitiesEnum.hasMoreElements();) {
			EOEntity entity = (EOEntity) entitiesEnum.nextElement();
			for (Enumeration attributesEnum = entity.attributes().objectEnumerator(); attributesEnum.hasMoreElements();) {
				EOAttribute attribute = (EOAttribute) attributesEnum.nextElement();
				NSDictionary attributeUserInfo = attribute.userInfo();
				if (attributeUserInfo != null) {
					String constantClassName = (String)attributeUserInfo.objectForKey("ERXConstantClassName");
					if (constantClassName != null) {
						boolean constantClassFound = true;
						try {
							Class.forName(constantClassName);
						}
						catch (ClassNotFoundException e) {
							int lastDotIndex = constantClassName.lastIndexOf('.');
							if (lastDotIndex != -1) {
								String innerClassName = constantClassName.substring(0, lastDotIndex) + "$" + constantClassName.substring(lastDotIndex + 1);
								try {
									Class.forName(innerClassName);
								}
								catch (ClassNotFoundException e2) {
									constantClassFound = false;
								}
							}
							else {
								constantClassFound = false;
							}
						}
						if (!constantClassFound) {
							throw new IllegalArgumentException(attribute.name() + " specified an ERXConstantClass of '" + constantClassName + "', which could not be found.");
						}
					}
				}
			}
		}
	}
	
	/**
	 * If a prototypeEntityName is specified for a given model, go through and flatten the specified prototype down into
	 * all of the attributes of the model. This allows support for multiple databases in a single EOModelGroup, each
	 * using the correct database-specified variant of the prototype. Without flattening, you could not use ERPrototypes
	 * with a PostgreSQL and FrontBase database loaded at the same time, because the attribute names overlap. To get the
	 * most out of this behavior, you should use the Wonder-style prototype entity naming (EOJDBCFrontBasePrototypes)
	 * rather than the traditional EOPrototypes or EOJDBCPrototypes to provide unique namespaces for your prototype
	 * attributes.
	 */
	private void flattenPrototypes() {
		if (!ERXModelGroup.flattenPrototypes) {
			return;
		}
		String prototypesFixedKey = "_EOPrototypesFixed";
		NSMutableDictionary prototypeReplacement = new NSMutableDictionary();
		for (Enumeration modelsEnum = models().objectEnumerator(); modelsEnum.hasMoreElements();) {
			EOModel model = (EOModel) modelsEnum.nextElement();
			if(_prototypeModelNames.containsObject(model.name())) {
				log.debug("Skipping prototype model " + model.name());
				continue;
			}
			NSDictionary userInfo = model.userInfo();
			Boolean prototypesFixedBoolean = (Boolean) userInfo.objectForKey(prototypesFixedKey);
			if (prototypesFixedBoolean == null || !prototypesFixedBoolean.booleanValue()) {
				boolean prototypesFixed = false;
				String prototypeEntityName = prototypeEntityNameForModel(model);
				if (prototypeEntityName == null) {
					prototypesFixed = true;
				}
				else {
					EOEntity prototypeEntity = entityNamed(prototypeEntityName);
					if (prototypeEntity == null) {
						log.info(model.name() + " references a prototype entity named " + prototypeEntityName + " which is not yet loaded.");
					}
					else {
						if (log.isDebugEnabled()) {
							log.debug("Flattening " + model.name() + " using the prototype " + prototypeEntity.name());
						}
						for (Enumeration entitiesEnum = model.entities().objectEnumerator(); entitiesEnum.hasMoreElements();) {
							EOEntity entity = (EOEntity) entitiesEnum.nextElement();
							for (Enumeration attributesEnum = entity.attributes().objectEnumerator(); attributesEnum.hasMoreElements();) {
								EOAttribute attribute = (EOAttribute) attributesEnum.nextElement();
								if (!attribute.isDerived() && !attribute.isFlattened()) {
									String prototypeAttributeName = attribute.prototypeName();
									if (prototypeAttributeName == null) {
										log.warn(model.name() + "/" + entity.name() + "/" + attribute.name() + " does not have a prototype attribute name.  This can occur if the model cannot resolve ANY prototypes when loaded.  There must be a stub prototype for the model to load with that can then be replaced with the appropriate database-specific model.");
									}
									else {
										EOAttribute prototypeAttribute = prototypeEntity.attributeNamed(prototypeAttributeName);
										if (prototypeAttribute == null) {
											log.warn(model.name() + "/" + entity.name() + "/" + attribute.name() + " references a prototype attribute named " + prototypeAttributeName + " that does not exist in " + prototypeEntity.name() + ".");
										}
										else if (attribute.prototype().entity() == prototypeEntity) {
											if (log.isDebugEnabled()) {
												log.debug("Skipping " + model.name() + "/" + entity.name() + "/" + attribute.name() + " because it is already prototyped by the correct entity.");
											}
										}
										else {
											flattenPrototypeAttribute(prototypeAttribute, attribute);
											if (log.isDebugEnabled()) {
												log.debug("Flattening " + model.name() + "/" + entity.name() + "/" + attribute.name() + " with the prototype attribute " + prototypeAttribute.entity().model().name() + "/" + prototypeAttribute.entity().name() + "/" + prototypeAttribute.name());
											}
										}
									}
								}
								else {
									log.debug("Skipping " + model.name() + "/" + entity.name() + "/" + attribute.name() + " because it's derived or flattened.");
								}
							}
						}
						prototypesFixed = true;
					}
				}
				
				NSMutableDictionary mutableUserInfo = userInfo.mutableClone();
				mutableUserInfo.setObjectForKey(Boolean.valueOf(prototypesFixed), prototypesFixedKey);
				model.setUserInfo(mutableUserInfo);
				dumpSchemaSQL(model);

			}
		}
	}

	/**
	 * Flattens a single attribute with the respective prototype.
	 * @param prototypeAttribute
	 * @param attribute
	 */
	private void flattenPrototypeAttribute(EOAttribute prototypeAttribute, EOAttribute attribute) {
		NSArray prototypeKeys = EOAttribute._prototypeKeys();
		NSMutableArray overriddenKeys = new NSMutableArray();
		Enumeration prototypeKeysEnum = prototypeKeys.objectEnumerator();
		while (prototypeKeysEnum.hasMoreElements()) {
			String prototypeKey = (String) prototypeKeysEnum.nextElement();
			if (attribute._isKeyEnumOverriden(EOAttribute._enumForKey(prototypeKey))) {
				overriddenKeys.addObject(prototypeKey);
			}
		}
		// AK: for whatever reason, when we have a custom value type of type string, it gets reset to NSData.
		// Presumably, this is because EOM outputs other keys than WO and the logic to get at the actual
		// value type is pretty broken.
		// Adding factoryMethodArgumentType to the overridden key solves this. Of course,  this breaks
		// when you *do* have a different factoryMethodArgumentType in the attribute but this shouldn't ever be the case.
		boolean hasCustomClass = false;
		if(overriddenKeys.containsObject("valueFactoryMethodName")) {
			overriddenKeys.addObject("factoryMethodArgumentType");
			hasCustomClass = true;
		}

		NSArray keysToReplace = _NSArrayUtilities.arrayExcludingObjectsFromArray(prototypeKeys, overriddenKeys);
		NSDictionary valuesToReplace = EOKeyValueCodingAdditions.Utility.valuesForKeys(prototypeAttribute, keysToReplace);
		attribute.setPrototype(null);
		NSMutableDictionary userInfo = new NSMutableDictionary(prototypeAttribute.name(), "prototypeName");
		if(attribute.userInfo() != null) {
			userInfo.addEntriesFromDictionary(attribute.userInfo());
		}
		attribute.setUserInfo(userInfo);
		EOKeyValueCodingAdditions.Utility.takeValuesFromDictionary(attribute, valuesToReplace);
		if(hasCustomClass) {
			Class clazz = ERXPatcher.classForName(attribute.className());
			if(ERXConstant.StringConstant.class.isAssignableFrom(clazz)) {
				attribute.setFactoryMethodArgumentType(EOAttribute.FactoryMethodArgumentIsString);
				// AK: the following two calls are needed to clear the cached values from the attribute
				attribute.setClassName(attribute.className());
				attribute.setValueFactoryMethodName(attribute.valueFactoryMethodName());
				log.info("Attribute : " + attribute + " changed " + attribute.adaptorValueType() + " " + attribute.factoryMethodArgumentType());
			}
		}
	}
}