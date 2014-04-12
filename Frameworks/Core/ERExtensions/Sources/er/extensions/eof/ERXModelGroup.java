/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOJoin;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.ERXModel;
import com.webobjects.eocontrol.EOKeyValueCodingAdditions;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
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

import er.extensions.foundation.ERXConfigurationManager;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXPatcher;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXSystem;
import er.extensions.foundation.ERXValueUtilities;
import er.extensions.jdbc.ERXJDBCAdaptor;
import er.extensions.jdbc.ERXSQLHelper;

/**
 * Enhanced model group that supports connection dict switching, definable and predictable model orderings and stackable prototypes.
 * It also fixes some errors when loading prototypes and EOModeler backup files (Foo.emodeld~). The class is the meant to be the default model
 * group and works in conjunction with ERXExtensions to set itself up on load.
 *
 * <p>You <b>must</b> use EOModelGroup.defaultGroup() and not EOModelGroup.globalModelGroup() because only the former will result in this class getting 
 * created.</p>
 *
 * @property EOPrototypesFileGLOBAL
 * @property JNDI.global.authenticationMethod
 * @property JNDI.global.password
 * @property JNDI.global.plugin
 * @property JNDI.global.serverUrl
 * @property JNDI.global.username
 * @property dbConfigNameGLOBAL
 * @property [MODEL_NAME].DBConfigName
 * @property dbConnectAdaptorGLOBAL
 * @property [MODEL_NAME].adaptor
 * @property dbConnectDatabaseGLOBAL
 * @property [MODEL_NAME].DBDatabase
 * @property dbConnectDriverGLOBAL
 * @property [MODEL_NAME].DBDriver
 * @property dbConnectHostNameGLOBAL
 * @property [MODEL_NAME].DBHostName
 * @property dbConnectJDBCInfoGLOBAL
 * @property [MODEL_NAME].DBJDBCInfo
 * @property dbConnectPasswordGLOBAL
 * @property [MODEL_NAME].DBPassword
 * @property [MODEL_NAME].password
 * @property dbConnectPluginGLOBAL
 * @property [MODEL_NAME].DBPlugin
 * @property [MODEL_NAME].plugin
 * @property dbConnectServerGLOBAL
 * @property [MODEL_NAME].DBServer
 * @property dbConnectURLGLOBAL
 * @property [MODEL_NAME].URL
 * @property [MODEL_NAME].serverUrl
 * @property dbConnectUserGLOBAL
 * @property [MODEL_NAME].DBUser
 * @property [MODEL_NAME].username
 * @property dbConnectionRecycleGLOBAL
 * @property dbDebugLevelGLOBAL
 * @property dbEOPrototypesEntityGLOBAL
 * @property dbLogPathGLOBAL
 * @property dbMaxCheckoutGLOBAL
 * @property dbMaxConnectionsGLOBAL
 * @property dbMinConnectionsGLOBAL
 * @property dbRemoveJdbc2InfoGLOBAL
 * @property er.extensions.ERXModelGroup.[ENTITY_NAME].[ATTRIBUTE_NAME].columnName
 * @property er.extensions.ERXModelGroup.[ENTITY_NAME].[ATTRIBUTE_NAME].ignoreTypeMismatch
 * @property er.extensions.ERXModelGroup.[ENTITY_NAME].externalName
 * @property er.extensions.ERXModelGroup.flattenPrototypes defined if the prototypes should get flattened. Default is true. Note: this default value may be incompatible with {@link ERXModel#isUseExtendedPrototypesEnabled}.
 * @property er.extensions.ERXModelGroup.ignoreTypeMismatch
 * @property er.extensions.ERXModelGroup.modelClassName
 * @property er.extensions.ERXModelGroup.modelLoadOrder defines the load order of the models. When you use this property, the bundle loading will be disregarded. The default returns NSArray.EmptyArray.
 * @property er.extensions.ERXModelGroup.patchModelsOnLoad a boolean that defines whether the created should be a {@link Model}, not a EOModel. Default is false.
 * @property er.extensions.ERXModelGroup.patchedModelClassName
 * @property er.extensions.ERXModelGroup.prototypeModelName if defined, overrides the default name, erprototypes.eomodeld.
 * @property er.extensions.ERXModelGroup.prototypeModelNames defines the names of the models that are prototypes. They get put in front of the model load order. The default is <code>(erprototypes)</code>.
 * @property er.extensions.ERXModelGroup.raiseOnUnmatchingConnectionDictionaries defaut is true
 * @property er.extensions.ERXModelGroup.sqlDumpDirectory
 * @property [MODEL_NAME].DBConnectionRecycle
 * @property [MODEL_NAME].DBDebugLevel
 * @property [MODEL_NAME].DBLogPath
 * @property [MODEL_NAME].DBMaxCheckout
 * @property [MODEL_NAME].DBMaxConnections
 * @property [MODEL_NAME].DBMinConnections
 * @property [MODEL_NAME].EOPrototypesEntity
 * @property [MODEL_NAME].EOPrototypesFile
 * @property [MODEL_NAME].authenticationMethod
 * @property [MODEL_NAME].removeJdbc2Info
 */
public class ERXModelGroup extends EOModelGroup {

	/** logging support */
	public static final Logger log = Logger.getLogger(ERXModelGroup.class);
	
	private Hashtable cache;

	/**
	 * Key for languages, can be either in properties or in the model object's user info.
	 */
	public static final String LANGUAGES_KEY = "ERXLanguages";

	protected static final boolean patchModelsOnLoad = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXModelGroup.patchModelsOnLoad", false);
	
	protected static final boolean flattenPrototypes = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXModelGroup.flattenPrototypes", true);
	
	protected NSArray<String> _prototypeModelNames = ERXProperties.componentsSeparatedByStringWithDefault("er.extensions.ERXModelGroup.prototypeModelNames", "," ,new NSArray<String>(ERXProperties.stringForKeyWithDefault("er.extensions.ERXModelGroup.prototypeModelName", "erprototypes")));

	protected NSArray<String> _modelLoadOrder = ERXProperties.componentsSeparatedByStringWithDefault("er.extensions.ERXModelGroup.modelLoadOrder", ",", NSArray.EmptyArray);
	
	private boolean raiseOnUnmatchingConnectionDictionaries = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXModelGroup.raiseOnUnmatchingConnectionDictionaries", true);
	
	/**
	 * Notification that is sent when the model group was created form the bundle loading.
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
	 */
	@SuppressWarnings("cast")
	public void loadModelsFromLoadedBundles() {
		EOModelGroup.setDefaultGroup(this);
		NSArray<NSBundle> frameworkBundles = NSBundle.frameworkBundles();
		
		if (log.isDebugEnabled()) {
			log.debug("Loading bundles" + frameworkBundles.valueForKey("name"));
		}
		// clear the cached class descriptions - if descriptions are there, they
		// are from a previous load of the models, and may be out of date
		if (ERXEntityClassDescription.factory() != null) {
			log.warn("Clearing previous class descriptions");
			ERXEntityClassDescription.factory().reset();
		}

		NSMutableDictionary<String, URL> modelNameURLDictionary = new NSMutableDictionary<String, URL>();
		NSMutableArray<String> modelNames = new NSMutableArray<String>();
		NSMutableSet<NSBundle> bundles = new NSMutableSet<NSBundle>();
		bundles.addObject(NSBundle.mainBundle());
		bundles.addObjectsFromArray(frameworkBundles);

		for (Enumeration<NSBundle> e = bundles.objectEnumerator(); e.hasMoreElements(); ) {
			NSBundle nsbundle = e.nextElement();
			NSArray<String> paths = nsbundle.resourcePathsForResources("eomodeld", null);
			int pathCount = paths.count();
			for (int currentPath = 0; currentPath < pathCount; currentPath++) {
				String indexPath = paths.objectAtIndex(currentPath);
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

		NSMutableArray<URL> modelURLs = new NSMutableArray<URL>();
		// First, add prototyes if specified
		for(Enumeration prototypeModelNamesEnum = _prototypeModelNames.objectEnumerator(); prototypeModelNamesEnum.hasMoreElements(); ) {
			String prototypeModelName = (String) prototypeModelNamesEnum.nextElement();
			URL prototypeModelURL = (URL) modelNameURLDictionary.removeObjectForKey(prototypeModelName); // WO53
			modelNames.removeObject(prototypeModelName);
			if (prototypeModelURL != null) {
				modelURLs.addObject(prototypeModelURL);
			} else {
				// AK: we throw for everything except erprototypes, as it is set by default
				if(!"erprototypes".equals(prototypeModelName)) {
					throw new IllegalArgumentException("You specified the prototype model '" + prototypeModelName + "' in your prototypeModelNames array, but it can not be found.");
				}
			}
		}
		// Next, add all models that are stated explicitely
		for(Enumeration<String> modelLoadOrderEnum = _modelLoadOrder.objectEnumerator(); modelLoadOrderEnum.hasMoreElements(); ) {
			String modelName = modelLoadOrderEnum.nextElement();
			URL modelURL = modelNameURLDictionary.removeObjectForKey(modelName);
			modelNames.removeObject(modelName);
			if (modelURL == null) {
				throw new IllegalArgumentException("You specified the model '" + modelName + "' in your modelLoadOrder array, but it can not be found.");
			}
			modelURLs.addObject(modelURL);
		}
		// Finally add all the rest
		for (Enumeration<String> e = modelNames.objectEnumerator(); e.hasMoreElements();) {
			String name = e.nextElement();
			modelURLs.addObject(modelNameURLDictionary.objectForKey(name));
		}

		Enumeration<URL> modelURLEnum = modelURLs.objectEnumerator();
		while (modelURLEnum.hasMoreElements()) {
			URL url = modelURLEnum.nextElement();
			addModelWithPathURL(url);
		}
		
		// correcting an EOF Inheritance bug
		checkInheritanceRelationships();
		
		if (!patchModelsOnLoad) {
			modifyModelsFromProperties();
			flattenPrototypes();
			Enumeration<EOModel> modelsEnum = models().objectEnumerator();
			while (modelsEnum.hasMoreElements()) {
				EOModel model = modelsEnum.nextElement();
				preloadERXConstantClassesForModel(model);
			}
		}
		
		NSNotificationCenter.defaultCenter().postNotification(ModelGroupAddedNotification, this);
		if (!patchModelsOnLoad) {
			NSNotificationCenter.defaultCenter().addObserver(this, new NSSelector("modelAddedHandler", ERXConstant.NotificationClassArray), EOModelGroup.ModelAddedNotification, null);
		}

		checkForMismatchedJoinTypes();
	}
	
	static {
		NSNotificationCenter.defaultCenter().addObserver(LocalizedAttributeProcessor.class, new NSSelector("modelGroupAdded", ERXConstant.NotificationClassArray), ModelGroupAddedNotification, null);
	}
	
	/**
	 * Processes ERXLanguages attributes.
	 * @author ak
	 *
	 */
	public static class LocalizedAttributeProcessor {

		/**
		 * Copies an attribute to a new name.
		 * @param entity
		 * @param attribute
		 * @param newName
		 * @return cloned attribute
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

		protected void adjustLocalizedAttributes(EOModelGroup group) {
			for (Enumeration enumerator = group.models().objectEnumerator(); enumerator.hasMoreElements();) {
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
					Object languagesObject = attribute.userInfo() != null ? attribute.userInfo().objectForKey(LANGUAGES_KEY) : null;
					if (languagesObject != null && !(languagesObject instanceof NSArray)) {
						languagesObject = entity.model().userInfo() != null ? entity.model().userInfo().objectForKey(LANGUAGES_KEY) : null;
						if(languagesObject == null) {
							languagesObject = ERXProperties.arrayForKey(LANGUAGES_KEY);
						}
					}
					NSArray languages = (languagesObject != null ? (NSArray) languagesObject : NSArray.EmptyArray);
					if (languages.count() > 0) {
						String name = attribute.name();
						String columnName = attribute.columnName();
						NSMutableDictionary attributeUserInfo = new NSMutableDictionary();
						if(attribute.userInfo() != null) {
							attributeUserInfo.addEntriesFromDictionary(attribute.userInfo());
						}
						attributeUserInfo.setObjectForKey(languages, LANGUAGES_KEY);
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
							copy.setUserInfo(attributeUserInfo.mutableClone());
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

		public static void modelGroupAdded(NSNotification n) {
			EOModelGroup group = (EOModelGroup) n.object();
			new LocalizedAttributeProcessor().adjustLocalizedAttributes(group);
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
	@Override
	public void addModel(EOModel eomodel) {
		Enumeration enumeration = _modelsByName.objectEnumerator();
		String name = eomodel.name();
		if (_modelsByName.objectForKey(name) != null) {
			log.warn("The model '" + name + "' (path: " + eomodel.pathURL() + ") cannot be added to model group " + this + " because it already contains a model with that name.");
			return;
		}
		NSMutableSet nsmutableset = new NSMutableSet(128);
		NSSet<String> nsset = new NSSet<String>(eomodel.entityNames());
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

	private final static String SQLDUMP_DIR_NOT_WRITEABLE_DIR = "The er.extensions.ERXModelGroup.sqlDumpDirectory property is set and is not a valid, writeable directory.";
	private final static String SQLDUMP_FILE_NOT_WRITEABLE = "The er.extensions.ERXModelGroup.sqlDumpDirectory property is set and the dump file for this model exists and is not writeable.";

	private void dumpSchemaSQL(EOModel eomodel) {
		String dumpDir = sqlDumpDirectory();
		if(dumpDir != null) {
			try {
				File dumpDirectory = new File(dumpDir);
				if (! dumpDirectory.isDirectory() || ! dumpDirectory.canWrite()) {
					throw NSForwardException._runtimeExceptionForThrowable(new IllegalArgumentException(SQLDUMP_DIR_NOT_WRITEABLE_DIR));
				}
				File dumpFile = new File(dumpDir + File.separator + eomodel.name() + ".sql");
				if (dumpFile.exists() && ! dumpFile.canWrite()) {
					throw NSForwardException._runtimeExceptionForThrowable(new IllegalArgumentException(SQLDUMP_FILE_NOT_WRITEABLE));
				}
				EOAdaptor adaptor = EOAdaptor.adaptorWithModel(eomodel);
				if (adaptor instanceof JDBCAdaptor) {
					JDBCAdaptor jdbc = (JDBCAdaptor) adaptor;
					ERXSQLHelper helper = ERXSQLHelper.newSQLHelper(jdbc);
					String sql = helper.createSchemaSQLForEntitiesInModelAndOptions(eomodel.entities(), eomodel, helper.defaultOptionDictionary(true, true));
					ERXFileUtilities.writeInputStreamToFile(new ByteArrayInputStream(sql.getBytes()), dumpFile);
					log.info("Wrote Schema SQL to " + dumpFile);
				}
			} catch (java.io.IOException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
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
	 * <code>er.extensions.ERXModelGroup.patchModelsOnLoad=true</code>.
	 * 
	 * <p>Note: <code>er.extensions.ERXModelGroup.patchModelsOnLoad=true</code> sets the <code>er.extensions.ERXModel.useExtendedPrototypes</code>
         * property to <code>true</code>.
	 * 
	 * @author ak
	 */
	public static class Model extends ERXModel {

		public Model(URL url) {
			super(url);
		}

		/**
		 * @return <code>true</code>
		 * @see com.webobjects.eoaccess.ERXModel#useExtendedPrototypes()
		 */
		@Override
		protected boolean useExtendedPrototypes() {
			return true;
		}
	}

	/**
	 * Overridden to use our model class in the runtime.
	 * @param url URL to model
	 * @return model object
	 */
	@Override
	public EOModel addModelWithPathURL(URL url) {
		EOModel model = null;
		String customModelClass = null;
		if (patchModelsOnLoad) {
			if ((customModelClass = ERXProperties.stringForKey("er.extensions.ERXModelGroup.patchedModelClassName")) != null) {
				try {
					Class<? extends EOModel> modelClass = Class.forName(customModelClass).asSubclass(Model.class);
					Constructor<? extends EOModel> modelConstructor = modelClass.getConstructor(URL.class);
					model = modelConstructor.newInstance(url);
				}
				catch (Exception e) {
					throw new RuntimeException("Failed to create custom patched Model subclass '" + customModelClass + "'.", e);
				}
			}
			else {
				model = new Model(url);
			}
		}
		else if ((customModelClass = ERXProperties.stringForKey("er.extensions.ERXModelGroup.modelClassName")) != null) {
			try {
				Class<? extends EOModel> modelClass = Class.forName(customModelClass).asSubclass(EOModel.class);
				Constructor<? extends EOModel> modelConstructor = modelClass.getConstructor(URL.class);
				model = modelConstructor.newInstance(url);
			}
			catch (Exception e) {
				throw new RuntimeException("Failed to create custom EOModel subclass '" + customModelClass + "'.", e);
			}
		}
		else {
			model = new ERXModel(url);
		}
		addModel(model);
		return model;
	}

	/**
	 * Looks for foreign key attributes that have a different type from the destination attribute.  The classic example of this is a
	 * long foreign key pointing to an integer primary key, which has a terrible consequence that is nearly impossible to track down.
	 */
	@SuppressWarnings("cast")
	public void checkForMismatchedJoinTypes() {
		if (ERXProperties.booleanForKey("er.extensions.ERXModelGroup.ignoreTypeMismatch")) {
			return;
		}
		for (EOModel model : (NSArray<EOModel>)models()) {
			for (EOEntity entity : (NSArray<EOEntity>)model.entities()) {
				for (EORelationship relationship : (NSArray<EORelationship>)entity.relationships()) {
					for (EOJoin join : (NSArray<EOJoin>)relationship.joins()) {
						EOAttribute sourceAttribute = join.sourceAttribute();
						EOAttribute destinationAttribute = join.destinationAttribute();
						if (sourceAttribute != null && destinationAttribute != null) {
							if (ObjectUtils.notEqual(sourceAttribute.className(), destinationAttribute.className()) || ObjectUtils.notEqual(sourceAttribute.valueType(), destinationAttribute.valueType())) {
								if (!ERXProperties.booleanForKey("er.extensions.ERXModelGroup." + sourceAttribute.entity().name() + "." + sourceAttribute.name() + ".ignoreTypeMismatch")) {
									throw new RuntimeException("The attribute " + sourceAttribute.name() + " in " + sourceAttribute.entity().name() + " (" + sourceAttribute.className() + ", " + sourceAttribute.valueType() + ") is a foreign key to " + destinationAttribute.name() + " in " + destinationAttribute.entity().name() + " (" + destinationAttribute.className() + ", " + destinationAttribute.valueType() + ") but their class names or value types do not match.  If this is actually OK, you can set er.extensions.ERXModelGroup." + sourceAttribute.entity().name() + "." + sourceAttribute.name() + ".ignoreTypeMismatch=true in your Properties file.");
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Corrects a strange EOF inheritance issue where if a model gets loaded and an entity that has children located in
	 * a different model that hasn't been loaded yet will not be setup correctly. Specifically when those child entities
	 * are loaded they will not have their parentEntity relationship set correctly.
	 */
	@SuppressWarnings("cast")
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
			cachedValue = o == null ? null : Integer.valueOf(o.toString());
			if (cachedValue == null) {
				cachedValue = Integer.valueOf(0);
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
	 * @param entity the entity to check
	 * @return whether or not the entity is a prototype
	 */
	public static boolean isPrototypeEntity(EOEntity entity) {
		return ERXModelGroup.isPrototypeEntityName(entity.name());
	}
	
	/**
	 * Returns whether or not the given entity name is a prototype entity
	 * @param entityName entity name
	 * @return <code>true</code> if entity if a prototype
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

	private static String getProperty(String key, String alternateKey, String defaultValue) {
		String value = ERXProperties.stringForKey(key);
		if (value == null) {
			value = ERXProperties.stringForKey(alternateKey);
		}
		if (value == null) {
			value = defaultValue;
		}
		return value;
	}

	private static String getProperty(String key, String alternateKey) {
		return getProperty(key, alternateKey, null);
	}

	private static String decryptProperty(String key, String alternateKey) {
		String value = ERXProperties.decryptedStringForKey(key);
		if (value == null) {
			value = ERXProperties.decryptedStringForKey(alternateKey);
		}
		return value;
	}

	protected void fixOracleDictionary(EOModel model) {
		String modelName = model.name();
		String serverName = getProperty(modelName + ".DBServer", "dbConnectServerGLOBAL");
		String userName = getProperty(modelName + ".DBUser", "dbConnectUserGLOBAL");
		String passwd = decryptProperty(modelName + ".DBPassword", "dbConnectPasswordGLOBAL");

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

	/**
	 * Similar to fixJDBCDictionary, but for JNDI EOModels.
	 * 
	 * @param model the JNDI EOModel to fix
	 * @property [modelName].serverUrl the per-model server URL to set
	 * @property [modelName].user the per-model username to set
	 * @property [modelName].password the per-model password to set
	 * @property [modelName].authenticationModel the per-model authenticationMethod to set
	 * @property JNDI.global.serverUrl the global JNDI serverUrl to use by default
	 * @property JNDI.global.username the global JNDI username to use by default
	 * @property JNDI.global.password the global JNDI password to use by default
	 * @property JNDI.global.authenticationMethod the global JNDI authenticationMethod to use by default
	 */
	protected void fixJNDIDictionary(EOModel model) {
		String modelName = model.name();
		String serverUrl = getProperty(modelName + ".serverUrl", "JNDI.global.serverUrl");
		String userName = getProperty(modelName + ".username", "JNDI.global.username");
		String password = decryptProperty(modelName + ".password", "JNDI.global.password");
		String authenticationMethod = getProperty(modelName + ".authenticationMethod", "JNDI.global.authenticationMethod");
		String plugin = getProperty(modelName + ".plugin", "JNDI.global.plugin");
		
		NSDictionary<String, Object> connectionDictionary = model.connectionDictionary();
		if (connectionDictionary == null) {
			connectionDictionary = new NSMutableDictionary<String, Object>();
			ERXModelGroup.log.warn("The EOModel '" + model.name() + "' does not have a connection dictionary, providing an empty one");
			model.setConnectionDictionary(connectionDictionary);
		}

		NSMutableDictionary<String, Object> newConnectionDictionary = new NSMutableDictionary<String, Object>(connectionDictionary);
		if (serverUrl != null) {
			newConnectionDictionary.setObjectForKey(serverUrl, "serverUrl");
		}
		if (userName != null) {
			newConnectionDictionary.setObjectForKey(userName, "username");
		}
		if (password != null) {
			newConnectionDictionary.setObjectForKey(password, "password");
		}
		if (authenticationMethod != null) {
			newConnectionDictionary.setObjectForKey(authenticationMethod, "authenticationMethod");
		}
		if (plugin != null) {
			newConnectionDictionary.setObjectForKey(plugin, "plugInClassName");
		}

		model.setConnectionDictionary(newConnectionDictionary);
	}
	
	protected void fixJDBCDictionary(EOModel model) {
		String aModelName = model.name();

		boolean poolConnections = ERXJDBCAdaptor.useConnectionBroker();

		String adaptor = getProperty(aModelName + ".adaptor", "dbConnectAdaptorGLOBAL");
		String url = getProperty(aModelName + ".URL", "dbConnectURLGLOBAL");
		String userName = getProperty(aModelName + ".DBUser", "dbConnectUserGLOBAL");
		String passwd = decryptProperty(aModelName + ".DBPassword", "dbConnectPasswordGLOBAL");
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
			model.setConnectionDictionary(connectionDictionary);
		}

		NSMutableDictionary newConnectionDictionary = new NSMutableDictionary(connectionDictionary);
		if (adaptor != null) {
			model.setAdaptorName(adaptor);
		}
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

		if (newConnectionDictionary.count() == 0) {
			ERXModelGroup.log.warn("The EOModel '" + model.name() + "' has an empty connection dictionary.");
		}
		
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

		// we want to be a bit more aggressive here
		String[] keysThatMatter = { "URL", "username" };
		Enumeration modelsEnum = model.modelGroup().models().objectEnumerator();
		while (modelsEnum.hasMoreElements()) {
			EOModel otherModel = (EOModel)modelsEnum.nextElement();
			if (otherModel != model) {
				NSDictionary otherConnectionDictionary = otherModel.connectionDictionary();
				if (otherConnectionDictionary != null && ObjectUtils.equals(newConnectionDictionary.objectForKey("adaptorName"), otherConnectionDictionary.objectForKey("adaptorName"))) {
					boolean valuesThatMatterMatch = true;
					for (int keyNum = 0; valuesThatMatterMatch && keyNum < keysThatMatter.length; keyNum ++) {
						String thisValue = (String)newConnectionDictionary.objectForKey(keysThatMatter[keyNum]);
						String otherValue = (String)otherConnectionDictionary.objectForKey(keysThatMatter[keyNum]);
						valuesThatMatterMatch = ERXStringUtilities.stringEqualsString(thisValue, otherValue);
					}
					if (valuesThatMatterMatch && !newConnectionDictionary.equals(otherConnectionDictionary)) {
						if (!isPrototypeModel(model) && !isPrototypeModel(otherModel)) {
							String message = "The connection dictionaries for " + model.name() + " and " + otherModel.name() + " have the same URL and username, but the connection dictionaries are not equal. Check your connection dictionaries carefully! This problem is often caused by jdbc2Info not matching between the two.  One fix for this is to set " + model.name() + ".removeJdbc2Info=true and " + otherModel.name() + ".removeJdbc2Info=true in your Properties file. (" + model.name() + "=" + newConnectionDictionary + "; and " + otherModel.name() + "=" + otherConnectionDictionary + ")."; 
							if (!raiseOnUnmatchingConnectionDictionaries) {
								// was intentionally switched off, so log only
								log.warn(message);
							}
							else {
								throw new IllegalArgumentException(message);
							}
						}
						log.info("The connection dictionaries for " + model.name() + " and " + otherModel.name() + " have the same URL and username, but at least one of them is a prototype model, so it shouldn't be a problem.");
					}
				}
			}
		}
	}
	
	/**
	 * Returns whether the given model is listed as a prototype model in the properties.
	 * @param model model object
	 * @return <code>true</code> if model is used for prototypes
	 */
	public boolean isPrototypeModel(EOModel model) {
		if (_prototypeModelNames != null && model != null && _prototypeModelNames.containsObject(model.name())) {
			return true;
		}
		return false;
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
		else if (model.adaptorName().indexOf("JNDI") != -1) {
			fixJNDIDictionary(model);
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

	public static String prototypeEntityNameForModel(EOModel model) {
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
				// we are guessing here, I don't want them to
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

	protected static NSDictionary databaseConfigForModel(EOModel model) {
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
			modifyModelsFromProperties();
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
	 * Modifies various settings of the entities and attributes in this model group based on System properties.
	 * 
	 * @property er.extensions.ERXModelGroup.[entityName].externalName change the table name for the given entityName
	 * @property er.extensions.ERXModelGroup.[entityName].[attributeName].columnName change the column name for the given attribute
	 */
	protected void modifyModelsFromProperties() {
		for (Enumeration modelsEnum = models().objectEnumerator(); modelsEnum.hasMoreElements();) {
			EOModel model = (EOModel) modelsEnum.nextElement();
			for (Enumeration entitiesEnum = model.entities().objectEnumerator(); entitiesEnum.hasMoreElements();) {
				EOEntity entity = (EOEntity) entitiesEnum.nextElement();
				
				String externalName = ERXProperties.stringForKey("er.extensions.ERXModelGroup." + entity.name() + ".externalName");
				if (externalName != null) {
					entity.setExternalName(externalName);
				}
				
				for (Enumeration attributesEnum = entity.attributes().objectEnumerator(); attributesEnum.hasMoreElements();) {
					EOAttribute attribute = (EOAttribute) attributesEnum.nextElement();
					
					String attributeColumnName = ERXProperties.stringForKey("er.extensions.ERXModelGroup." + entity.name() + "." + attribute.name() + ".columnName");
					if (attributeColumnName != null) {
						attribute.setColumnName(attributeColumnName);
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
		else if (ERXModel.isUseExtendedPrototypesEnabled()) {
			log.warn("Using er.extensions.ERXModel.useExtendedPrototypes=true may be incompatible with er.extensions.ERXModelGroup.flattenPrototypes=true (its default value).");
		}
		String prototypesFixedKey = "_EOPrototypesFixed";
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
								if (!attribute.isFlattened()) {
									String prototypeAttributeName = attribute.prototypeName();
									if (prototypeAttributeName == null) {
										if (attribute.externalType() == null) {
											log.warn(model.name() + "/" + entity.name() + "/" + attribute.name() + " does not have a prototype attribute name.  This can occur if the model cannot resolve ANY prototypes when loaded.  There must be a stub prototype for the model to load with that can then be replaced with the appropriate database-specific model.");
										}
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

	private static final NSArray _prototypeKeys = new NSArray(new Object[] { "externalType", "columnName", "readOnly", "className", "valueType", "width", "precision", "scale", "writeFormat", "readFormat", "userInfo", "serverTimeZone", "valueFactoryMethodName", "adaptorValueConversionMethodName", "factoryMethodArgumentType", "allowsNull", "parameterDirection", "_internalInfo" });

	public static NSArray _prototypeKeys() {
		return _prototypeKeys;
	}

	public static Object _keyForEnum(int key) {
		return _prototypeKeys().objectAtIndex(key);
	}

	public static int _enumForKey(String key) {
		return _prototypeKeys().indexOfObject(key);
	}

	public static boolean _isKeyEnumOverriden(EOAttribute att, int key) {
		boolean result = false;
		if(att.prototype() != null) {
			Map characteristics = (Map) NSKeyValueCoding.Utility.valueForKey(att, "overwrittenCharacteristics");
			for (Iterator iter = characteristics.entrySet().iterator(); iter.hasNext();) {
				Map.Entry element = (Map.Entry) iter.next();
				String charateristic = element.getKey().toString();
				Boolean value =  ((Boolean)element.getValue());
				if(charateristic.equalsIgnoreCase(_keyForEnum(key).toString())) {
					return value.booleanValue();
				}
			}
		}
		return result;
	}

	/**
	 * Flattens a single attribute with the respective prototype.
	 * 
	 * @param prototypeAttribute
	 * @param attribute
	 */
	private void flattenPrototypeAttribute(EOAttribute prototypeAttribute, EOAttribute attribute) {
		NSArray prototypeKeys = _prototypeKeys();
		NSMutableArray overriddenKeys = new NSMutableArray();
		Enumeration prototypeKeysEnum = prototypeKeys.objectEnumerator();
		while (prototypeKeysEnum.hasMoreElements()) {
			String prototypeKey = (String) prototypeKeysEnum.nextElement();
			if (_isKeyEnumOverriden(attribute, _enumForKey(prototypeKey))) {
				overriddenKeys.addObject(prototypeKey);
			}
		}
		String className = attribute.className();
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
		if(attribute.isDerived()) {
			overriddenKeys.addObject("columnName");
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
			Class clazz = ERXPatcher.classForName(className);
			if(ERXConstant.StringConstant.class.isAssignableFrom(clazz)) {
				attribute.setFactoryMethodArgumentType(EOAttribute.FactoryMethodArgumentIsString);
				
				// AK: the following two calls are needed to clear the cached values from the attribute
				attribute.setClassName(className);
				attribute.setValueFactoryMethodName(attribute.valueFactoryMethodName());
				log.info("Attribute : " + attribute + " changed " + attribute.adaptorValueType() + " " + attribute.factoryMethodArgumentType());
			}
		}
	}
}
