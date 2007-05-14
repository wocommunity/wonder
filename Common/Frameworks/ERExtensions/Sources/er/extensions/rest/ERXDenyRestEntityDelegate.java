package er.extensions.rest;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

public class ERXDenyRestEntityDelegate extends ERXAbstractRestEntityDelegate {
	public void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return false;
	}

	public NSArray displayPropertyNames(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return NSArray.EmptyArray;
	}

	public NSArray objectsForEntity(EOEntity entity, ERXRestContext context) {
		return NSArray.EmptyArray;
	}

	public boolean canInsertObject(EOEntity entity, ERXRestContext context) {
		return false;
	}

	public boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context) {
		return false;
	}

	public boolean canDeleteObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canUpdateObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canViewObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return false;
	}

	public NSArray visibleObjects(EOEntity parentEntity, Object parent, String key, EOEntity entity, NSArray objects, ERXRestContext context) {
		return NSArray.EmptyArray;
	}

	public boolean isNextKeyVisible(EOEntity entity, Object value, String nextKey, ERXRestContext context) {
		return false;
	}

	public ERXRestResult nextNonModelResult(EOEntity entity, Object value, String nextKey, String nextPath, boolean includeContent, ERXRestContext context) {
		return null;
	}
}