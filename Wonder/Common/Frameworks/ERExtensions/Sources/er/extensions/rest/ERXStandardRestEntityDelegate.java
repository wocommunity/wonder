package er.extensions.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;

public abstract class ERXStandardRestEntityDelegate extends ERXAbstractRestEntityDelegate {
	private NSMutableSet _displayPropertyNames;
	private NSMutableSet _viewPropertyNames;
	private NSMutableSet _updatePropertyNames;

	public ERXStandardRestEntityDelegate() {
		_displayPropertyNames = new NSMutableSet();
		_viewPropertyNames = new NSMutableSet();
		_updatePropertyNames = new NSMutableSet();
	}

	public void addViewPropertyName(String visiblePropertyName) {
		_viewPropertyNames.addObject(visiblePropertyName);
	}

	public void addUpdatePropertyName(String visiblePropertyName) {
		_updatePropertyNames.addObject(visiblePropertyName);
	}

	public void addDisplayPropertyName(String visiblePropertyName) {
		_displayPropertyNames.addObject(visiblePropertyName);
		addViewPropertyName(visiblePropertyName);
	}

	public NSArray displayPropertyNames(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		return _displayPropertyNames.allObjects();
	}

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return _updatePropertyNames.containsObject(propertyName);
	}

	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return _viewPropertyNames.containsObject(propertyName);
	}
}
