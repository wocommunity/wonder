/**
 * 
 */
package com.webobjects.eoaccess;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXModelGroup;
import er.extensions.foundation.ERXProperties;

/**
 * This EOModel subclass primarily provides the opportunity to subclass EOEntity.
 * 
 * <p><b>Note</b> the package <code>com.webobjects.eoaccess</code> is used to
 * allow any protected or default access superclass instance methods to resolve
 * at runtime.
 * 
 * <p>To allow for extended prototypes set
 * <code>er.extensions.ERXModel.useExtendedPrototypes=true</code>.
 * Note: this may be incompatible with {@link er.extensions.eof.ERXModelGroup#flattenPrototypes}.</p>
 * 
 * <p>The existence of prototype entities based on specific conventions
 * is checked and the attributes of those prototype entities are added to the model's
 * prototype attributes cache in a specific order. The search order ensures that
 * the same prototype attribute names in different prototype entities get chosen
 * in a predictable way.</p>
 * 
 * <p>Consequently, you can use this search order knowledge to over-ride Wonder's
 * ERPrototypes for your entire set of application eomodels or just for specific
 * named eomodels.</p>
 * 
 * To understand the variables used in deriving the prototype entity names that are searched
 * a few definitions are appropriate
 * <dl>
 * <dt>&lt;pluginName&gt;</dt>
 * 		<dd>Relates to the database type. Examples of pluginName are MySQL, Derby, FrontBase, OpenBase, Oracle, Postgresql</dd>
 * <dt>&lt;adaptorName&gt;</dt>
 * 		<dd>Relates to the general persistence mechanism. Examples of adaptorName are JDBC, Memory, REST</dd>
 * <dt>&lt;modelName&gt;</dt>
 * 		<dd>The name of an eomodel in your app or frameworks</dd>
 *  
 * </dl>
 * 
 * The priority order (which is basically the reverse of the search order) 
 * for prototype entities is as follows:
 * <ul>
 * <li>EOJDBC&lt;pluginName&gt;&lt;modelname&gt;Prototypes</li>
 * <li>EO&lt;adaptorName&gt;&lt;modelname&gt;Prototypes</li>
 * <li>EO&lt;modelname&gt;Prototypes</li>
 * <li>EOJDBC&lt;pluginName&gt;CustomPrototypes</li>
 * <li>EO&lt;adaptorName&gt;CustomPrototypes</li>
 * <li>EOCustomPrototypes</li>
 * <li>EOJDBC&lt;pluginName&gt;Prototypes <em>(Available for popular databases in ERPrototypes framework)</em></li>
 * <li>EO&lt;adaptorName&gt;Prototypes <em>(ERPrototypes has some of these too for generic-JDBC, Memory, etc.)</em></li>
 * <li>EOPrototypes <em>(Without ERXModel and the extendedPrototypes, this was pretty much your only way to add your own prototypes alongside ERPrototypes)</em></li>
 * </ul>
 *  
 * @author ldeck
 */
public class ERXModel extends EOModel {
	// Expose EOModel._EOGlobalModelLock so that ERXModelGroup can lock on it
	public static Object _ERXGlobalModelLock = EOModel._EOGlobalModelLock;
	
	private static final Logger log = LoggerFactory.getLogger(ERXModel.class);
	
	/**
	 * Utility to add attributes to the prototype cache. As the attributes are chosen by name, replace already
	 * existing ones.
	 * 
	 * @param model - the model to which the prototype attributes will be cached
	 * @param prototypesEntity - the entity from which to copy the prototype attributes
	 */
	private static void addAttributesToPrototypesCache(EOModel model, EOEntity prototypesEntity) {
		if (model != null && prototypesEntity != null) {
			addAttributesToPrototypesCache(model, attributesFromEntity(prototypesEntity));
		}
	}

	/**
	 * Utility to add attributes to the prototype cache for a given model. As the attributes are chosen by name, replace already
	 * existing ones.
	 * 
	 * @param model - the model to which the prototype attributes will be cached
	 * @param prototypeAttributes - the prototype attributes to add to the model
	 */
	private static void addAttributesToPrototypesCache(EOModel model, NSArray<? extends EOAttribute> prototypeAttributes) {
		if (model != null && prototypeAttributes.count() != 0) {
			NSArray keys = namesForAttributes(prototypeAttributes);
			NSDictionary temp = new NSDictionary(prototypeAttributes, keys);
			model._prototypesByName.addEntriesFromDictionary(temp);
		}
	}
	
	/**
	 * Utility for getting all the attributes off an entity. If the entity is null, an empty array is returned.
	 * 
	 * @param entity an entity
	 * @return array of attributes from the given entity
	 */
	private static NSArray<EOAttribute> attributesFromEntity(EOEntity entity) {
		NSArray<EOAttribute> result = NSArray.emptyArray();
		if (entity != null) {
			result = entity.attributes();
			log.debug("Attributes from {}: {}", entity.name(), result);
		}
		return result;
	}
	
	/**
	 * Create the prototype cache for the given model by walking a search order.
	 * @param model 
	 */
	public static void createPrototypes(EOModel model) {
		// Remove password for logging
		NSMutableDictionary dict = model.connectionDictionary().mutableClone();
		if (dict.objectForKey("password") != null) {
			dict.setObjectForKey("<deleted for log>", "password");
		}
		log.info("Creating prototypes for model: {}->{}", model.name(), dict);
		synchronized (_EOGlobalModelLock) {
			StringBuilder debugInfo = null;
			if (log.isDebugEnabled()) {
				debugInfo = new StringBuilder();
				debugInfo.append("Model = " + model.name());
			}
			model._prototypesByName = new NSMutableDictionary();
			String name = model.name();
			NSArray adaptorPrototypes = NSArray.EmptyArray;
			EOAdaptor adaptor = EOAdaptor.adaptorWithModel(model);
			try {
				adaptorPrototypes = adaptor.prototypeAttributes();
			}
			catch (Exception e) {
				log.error("Could not get prototype attributes from adaptor.", e);
			}
			addAttributesToPrototypesCache(model, adaptorPrototypes);
			NSArray prototypesToHide = attributesFromEntity(model._group.entityNamed("EOPrototypesToHide"));
			model._prototypesByName.removeObjectsForKeys(namesForAttributes(prototypesToHide));

			String plugin = null;
			if (adaptor instanceof JDBCAdaptor && !model.name().equalsIgnoreCase("erprototypes")) {
				plugin = (String) model.connectionDictionary().objectForKey("plugin");
				if (plugin == null) {
					plugin = ERXEOAccessUtilities.guessPluginName(model);
				} //~ if (plugin == null)
				if (plugin != null && plugin.toLowerCase().endsWith("plugin")) {
					plugin = plugin.substring(0, plugin.length() - "plugin".length());
				}
				if (log.isDebugEnabled()) debugInfo.append("; plugin = " + plugin);
			}

			addAttributesToPrototypesCache(model, model._group.entityNamed("EOPrototypes"));
			addAttributesToPrototypesCache(model, model._group.entityNamed("EO" + model.adaptorName() + "Prototypes"));
			if (log.isDebugEnabled()) debugInfo.append("; Prototype Entities Searched = EOPrototypes, " + "EO" + model.adaptorName() + "Prototypes");
			if (plugin != null) {
				addAttributesToPrototypesCache(model, model._group.entityNamed("EOJDBC" + plugin + "Prototypes"));
				if (log.isDebugEnabled()) debugInfo.append(", " + "EOJDBC" + plugin + "Prototypes");
			}

			addAttributesToPrototypesCache(model, model._group.entityNamed("EOCustomPrototypes"));
			addAttributesToPrototypesCache(model, model._group.entityNamed("EO" + model.adaptorName() + "CustomPrototypes"));
			if (log.isDebugEnabled()) debugInfo.append(", EOCustomPrototypes, " + "EO" + model.adaptorName() + "CustomPrototypes");
			if (plugin != null) {
				addAttributesToPrototypesCache(model, model._group.entityNamed("EOJDBC" + plugin + "CustomPrototypes"));
				if (log.isDebugEnabled()) debugInfo.append(", " + "EOJDBC" + plugin + "CustomPrototypes");
			}

			addAttributesToPrototypesCache(model, model._group.entityNamed("EO" + name + "Prototypes"));
			addAttributesToPrototypesCache(model, model._group.entityNamed("EO" + model.adaptorName() + name + "Prototypes"));
			if (log.isDebugEnabled()) debugInfo.append(", " + "EO" + name + "Prototypes" + ", " + "EO" + model.adaptorName() + name + "Prototypes");
			if (plugin != null) {
				addAttributesToPrototypesCache(model, model._group.entityNamed("EOJDBC" + plugin + name + "Prototypes"));
				if (log.isDebugEnabled()) debugInfo.append(", " + "EOJDBC" + plugin + name + "Prototypes");
			}
			
			if (log.isDebugEnabled()) log.debug(debugInfo.toString());
		}
	}
	
	/**
	 * Utility for getting all names from an array of attributes.
	 * 
	 * @param attributes array of attributes
	 * @return array of attribute names
	 */
	private static NSArray<String> namesForAttributes(NSArray<? extends EOAttribute> attributes) {
		return new ERXKey<String>("name").arrayValueInObject(attributes);
	}
	
	/**
	 * Defaults to false.
	 * Note: when enabled, this may be incompatible with {@link er.extensions.eof.ERXModelGroup#flattenPrototypes}.
	 * @return the boolean property value for <code>er.extensions.ERXModel.useExtendedPrototypes</code>.
	 */
	public static boolean isUseExtendedPrototypesEnabled() {
		return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXModel.useExtendedPrototypes", false);
	}

	/**
	 * Creates and returns a new ERXModel.
	 */
	public ERXModel() {
		super();
	}

	/**
	 * Creates a new EOModel object by reading the contents of the model archive
	 * at url. Sets the EOModel's name and path from the context of the model
	 * archive. Throws an IllegalArgumentException if url is null or if unable
	 * to read content from url. Throws a runtime exception if unable for any
	 * other reason to initialize the model from the specified java.net.URL;
	 * the error text indicates the nature of the exception.
	 * 
	 * @param url - The java.net.URL to a model archive.
	 */
	public ERXModel(URL url) {
		super(url);
	}

	/**
	 * @param propertyList
	 * @param path
	 */
	public ERXModel(NSDictionary propertyList, String path) {
		super(propertyList, path);
	}

	/**
	 * @param propertyList
	 * @param url
	 */
	public ERXModel(NSDictionary propertyList, URL url) {
		super(propertyList, url);
	}

	/**
	 * Sets the default EOEntity class to com.webobjects.eoaccess.ERXEntity. You can provide your
	 * own via the property <code>er.extensions.ERXModel.defaultEOEntityClassName</code> however your class
	 * must be in the same package unless you plan on re-implementing eof itself.
	 * 
	 * @see com.webobjects.eoaccess.EOModel#_addEntityWithPropertyList(java.lang.Object)
	 */
	@Override
	public Object _addEntityWithPropertyList(Object propertyList) throws InstantiationException, IllegalAccessException {
		NSMutableDictionary<String, Object> list = ((NSDictionary<String, Object> )propertyList).mutableClone();
		if (list.objectForKey("entityClass") == null) {
			String eoEntityClassName = ERXProperties.stringForKey("er.extensions.ERXModel.defaultEOEntityClassName");
			if (eoEntityClassName == null) {
				eoEntityClassName = ERXEntity.class.getName();
			}
			list.setObjectForKey(eoEntityClassName, "entityClass" );
		}
		return super._addEntityWithPropertyList(list);
	}
	
	/**
	 * Overridden to use our prototype creation method if
	 * <code>er.extensions.ERXModel.useExtendedPrototypes=true</code>.
	 */
	@Override
	public NSArray availablePrototypeAttributeNames() {
		synchronized (_EOGlobalModelLock) {
			if (_prototypesByName == null && useExtendedPrototypes()) {
				createPrototypes(this);
			}
		}
		return super.availablePrototypeAttributeNames();
	}
	
	/**
	 * Overridden to use our prototype creation method if
	 * <code>er.extensions.ERXModel.useExtendedPrototypes=true</code>.
	 */
	@Override
	public EOAttribute prototypeAttributeNamed(String name) {
		synchronized (_EOGlobalModelLock) {
			if (_prototypesByName == null && useExtendedPrototypes()) {
				createPrototypes(this);
			}
		}
		return super.prototypeAttributeNamed(name);
	}
	
	@Override
	public void setModelGroup(EOModelGroup modelGroup) {
		super.setModelGroup(modelGroup);
		if (modelGroup instanceof ERXModelGroup) {
			((ERXModelGroup) modelGroup).resetConnectionDictionaryInModel(this);
		}
	}
	
	/**
	 * Defaults to false as returned by {@link #isUseExtendedPrototypesEnabled()}.
	 * @return <code>true</code> if extended prototypes are used
	 * @see #isUseExtendedPrototypesEnabled()
	 */
	protected boolean useExtendedPrototypes() {
		return isUseExtendedPrototypesEnabled();
	}

}
