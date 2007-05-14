package er.extensions.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableSet;

public abstract class ERXStandardRestEntityDelegate extends ERXAbstractRestEntityDelegate {
	private NSMutableSet _viewPropertyNames;
	private NSMutableSet _updatePropertyNames;

	public ERXStandardRestEntityDelegate() {
		_viewPropertyNames = new NSMutableSet();
		_updatePropertyNames = new NSMutableSet();
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
}
