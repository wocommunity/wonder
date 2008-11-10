/**
 * 
 */
package com.webobjects.eoaccess;

import java.net.URL;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXProperties;

/**
 * This EOModel subclass simply provides the opportunity to subclass EOEntity.
 * 
 * <p><b>Note</b> the package <code>com.webobjects.eoaccess</code> is used to
 * allow any protected or default access superclass instance methods to resolve
 * at runtime.
 *  
 * @author ldeck
 */
public class ERXModel extends EOModel {

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

}
