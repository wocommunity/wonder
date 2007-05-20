package er.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXProperties;

public abstract class ERXStandardRestEntityDelegate extends ERXAbstractRestEntityDelegate {
	private NSMutableSet _viewPropertyNames;
	private NSMutableSet _updatePropertyNames;
	private NSMutableSet _insertPropertyNames;
	private NSMutableDictionary _propertyAliasForPropertyName;
	private NSMutableDictionary _propertyNameForPropertyAlias;

	public ERXStandardRestEntityDelegate() {
		_viewPropertyNames = new NSMutableSet();
		_updatePropertyNames = new NSMutableSet();
		_insertPropertyNames = new NSMutableSet();
		_propertyAliasForPropertyName = new NSMutableDictionary();
		_propertyNameForPropertyAlias = new NSMutableDictionary();
	}

	public String entityAliasForEntityNamed(String entityName) {
		String entityAlias = ERXProperties.stringForKey(ERXXmlRestResponseWriter.REST_PREFIX + entityName + ".alias");
		if (entityAlias == null) {
			entityAlias = super.entityAliasForEntityNamed(entityName);
		}
		return entityAlias;
	}
	
	public String propertyAliasForPropertyNamed(EOEntity entity, String propertyName) {
		String propertyAlias = (String) _propertyAliasForPropertyName.objectForKey(entity.name() + "." + propertyName);
		if (propertyAlias == null) {
			propertyAlias = super.propertyAliasForPropertyNamed(entity, propertyName);
		}
		return propertyAlias;
	}
	
	public String propertyNameForPropertyAlias(EOEntity entity, String propertyAlias) {
		String propertyName = (String) _propertyNameForPropertyAlias.objectForKey(entity.name() + "." + propertyAlias);
		if (propertyName == null) {
			propertyName = super.propertyNameForPropertyAlias(entity, propertyAlias);
		}
		return propertyName;
	}

	protected void addDisplayPropertiesAsViewProperties(String entityName) {
		String propertiesKey = ERXXmlRestResponseWriter.REST_PREFIX + entityName + ERXXmlRestResponseWriter.PROPERTIES_PREFIX;
		String propertyNamesStr = ERXProperties.stringForKey(propertiesKey);
		if (propertyNamesStr != null) {
			String[] propertyNames = propertyNamesStr.split(",");
			for (int propertyNum = 0; propertyNum < propertyNames.length; propertyNum++) {
				String propertyName = propertyNames[propertyNum];
				addViewPropertyName(entityName, propertyName);
			}
		}
	}

	public void addViewPropertyName(String entityName, String visiblePropertyName) {
		_viewPropertyNames.addObject(entityName + "." + visiblePropertyName);
		
		String propertyAliasKey = ERXXmlRestResponseWriter.REST_PREFIX + entityName + "." + visiblePropertyName + ".alias";
		String propertyAlias = ERXProperties.stringForKey(propertyAliasKey);
		if (propertyAlias != null) {
			_propertyAliasForPropertyName.setObjectForKey(propertyAlias, entityName + "." + visiblePropertyName);
			_propertyNameForPropertyAlias.setObjectForKey(visiblePropertyName, entityName + "." + propertyAlias);
		}
	}

	public void addUpdatePropertyName(String entityName, String updatePropertyName) {
		addViewPropertyName(entityName, updatePropertyName);
		addInsertPropertyName(entityName, updatePropertyName);
		_updatePropertyNames.addObject(entityName + "." + updatePropertyName);
	}

	public void addInsertPropertyName(String entityName, String insertPropertyName) {
		addViewPropertyName(entityName, insertPropertyName);
		_insertPropertyNames.addObject(entityName + "." + insertPropertyName);
	}

	public boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return _insertPropertyNames.containsObject(entity.name() + "." + propertyName);
	}

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return _updatePropertyNames.containsObject(entity.name() + "." + propertyName);
	}

	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return _viewPropertyNames.containsObject(entity.name() + "." + propertyName);
	}

	public void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		// DO NOTHING
	}

	public void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		// DO NOTHING
	}

	public EOEntity nextEntity(EOEntity entity, String key) {
		return null;
	}
}
