package er.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXProperties;

public abstract class ERXStandardRestEntityDelegate extends ERXAbstractRestEntityDelegate {
	private NSMutableSet _viewPropertyNames;
	private NSMutableSet _updatePropertyNames;

	public ERXStandardRestEntityDelegate() {
		_viewPropertyNames = new NSMutableSet();
		_updatePropertyNames = new NSMutableSet();
	}

	protected void addDisplayPropertiesAsViewProperties(String entityName) {
		String key = ERXXmlRestResponseWriter.REST_PREFIX + entityName + ERXXmlRestResponseWriter.PROPERTIES_PREFIX;
		String propertyNamesStr = ERXProperties.stringForKey(key);
		if (propertyNamesStr != null) {
			String[] propertyNames = propertyNamesStr.split(",");
			for (int propertyNum = 0; propertyNum < propertyNames.length; propertyNum++) {
				String propertyName = propertyNames[propertyNum];
				addViewPropertyName(propertyName);
			}
		}
	}

	public void addViewPropertyName(String visiblePropertyName) {
		_viewPropertyNames.addObject(visiblePropertyName);
	}

	public void addUpdatePropertyName(String updatePropertyName) {
		addViewPropertyName(updatePropertyName);
		_updatePropertyNames.addObject(updatePropertyName);
	}

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return _updatePropertyNames.containsObject(propertyName);
	}

	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return _viewPropertyNames.containsObject(propertyName);
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
