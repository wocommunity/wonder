/**
 * 
 */
package com.webobjects.eoaccess;

import java.net.URL;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.jdbcadaptor.JDBCAdaptor;

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
 *  
 * @author ldeck
 */
public class ERXModel extends EOModel {
	
	private static final Logger log = Logger.getLogger(ERXModel.class);
	
	/**
	 * Utility to add attributes to the prototype cache. As the attributes are chosen by name, replace already
	 * existing ones.
	 * 
	 * @param entity
	 */
	private static void addAttributesToPrototypesCache(EOEntity entity) {
		addAttributesToPrototypesCache(entity.model(), attributesFromEntity(entity));
	}

	/**
	 * Utility to add attributes to the prototype cache for a given model. As the attributes are chosen by name, replace already
	 * existing ones.
	 * 
	 * @param model
	 * @param attributes
	 */
	private static void addAttributesToPrototypesCache(EOModel model, NSArray<? extends EOAttribute> attributes) {
		if (attributes.count() != 0) {
			NSArray keys = namesForAttributes(attributes);
			NSDictionary temp = new NSDictionary(attributes, keys);
			model._prototypesByName.addEntriesFromDictionary(temp);
		}
	}
	
	/**
	 * Utility for getting all the attributes off an entity. If the entity is null, an empty array is returned.
	 * 
	 * @param entity
	 * @return array of attributes from the given entity
	 */
	private static NSArray<EOAttribute> attributesFromEntity(EOEntity entity) {
		NSArray<EOAttribute> result = NSArray.emptyArray();
		if (entity != null) {
			result = entity.attributes();
			log.info("Attributes from " + entity.name() + ": " + result);
		}
		return result;
	}
	
	/**
	 * Create the prototype cache for the given model by walking a search order.
	 * @param model 
	 */
	public static void createPrototypes(EOModel model) {
		log.info("Creating prototypes for model: " + model.name() + "->" + model.connectionDictionary());
		synchronized (_EOGlobalModelLock) {
			model._prototypesByName = new NSMutableDictionary();
			String name = model.name();
			NSArray adaptorPrototypes = NSArray.EmptyArray;
			EOAdaptor adaptor = EOAdaptor.adaptorWithModel(model);
			try {
				adaptorPrototypes = adaptor.prototypeAttributes();
			}
			catch (Exception e) {
				log.error(e, e);
			}
			addAttributesToPrototypesCache(model, adaptorPrototypes);
			NSArray prototypesToHide = attributesFromEntity(model._group.entityNamed("EOPrototypesToHide"));
			model._prototypesByName.removeObjectsForKeys(namesForAttributes(prototypesToHide));

			String plugin = null;
			if (adaptor instanceof JDBCAdaptor && !model.name().equalsIgnoreCase("erprototypes")) {
				plugin = (String) model.connectionDictionary().objectForKey("plugin");
			}

			addAttributesToPrototypesCache(model._group.entityNamed("EOPrototypes"));
			addAttributesToPrototypesCache(model._group.entityNamed("EO" + model.adaptorName() + "Prototypes"));
			if (plugin != null) {
				addAttributesToPrototypesCache(model._group.entityNamed("EOJDBC" + plugin + "Prototypes"));
			}

			addAttributesToPrototypesCache(model._group.entityNamed("EOCustomPrototypes"));
			addAttributesToPrototypesCache(model._group.entityNamed("EO" + model.adaptorName() + "CustomPrototypes"));
			if (plugin != null) {
				addAttributesToPrototypesCache(model._group.entityNamed("EOJDBC" + plugin + "CustomPrototypes"));
			}

			addAttributesToPrototypesCache(model._group.entityNamed("EO" + name + "Prototypes"));
			addAttributesToPrototypesCache(model._group.entityNamed("EO" + model.adaptorName() + name + "Prototypes"));
			if (plugin != null) {
				addAttributesToPrototypesCache(model._group.entityNamed("EOJDBC" + plugin + name + "Prototypes"));
			}
		}
	}
	
	/**
	 * Utility for getting all names from an array of attributes.
	 * 
	 * @param attributes
	 * @return array of attribute names
	 */
	private static NSArray<String> namesForAttributes(NSArray<? extends EOAttribute> attributes) {
		return new ERXKey<String>("name").arrayValueInObject(attributes);
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
	public EOEntity _addEntityWithPropertyList(NSDictionary<String, Object> propertyList) throws java.lang.InstantiationException, java.lang.IllegalAccessException {
		NSMutableDictionary<String, Object> list = propertyList.mutableClone();
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
	 * <code>er.extensions.ERXModelGroup.useExtendedPrototypes=true</code>.
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
	 * Defaults to false.
	 * @return the boolean property value for <code>er.extensions.ERXModel.useExtendedPrototypes</code>.
	 */
	protected boolean useExtendedPrototypes() {
		return ERXProperties.booleanForKeyWithDefault("er.extensions.ERXModel.useExtendedPrototypes", false);
	}

}
