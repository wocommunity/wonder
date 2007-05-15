package er.rest;

import java.text.ParseException;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

public class ERXDenyRestEntityDelegate implements IERXRestEntityDelegate {
	public String formatAttributeValue(EOEntity entity, Object object, String attributeName, Object attributeValue) throws ParseException, ERXRestException {
		return null;
	}

	public Object parseAttributeValue(EOEntity entity, Object object, String attributeName, String attributeValue) throws ParseException, ERXRestException {
		return null;
	}

	public void takeValueForKey(EOEntity entity, Object obj, String propertyName, String value, ERXRestContext context) throws ParseException, ERXRestException {
		// DO NOTHING
	}

	public Object valueForKey(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return null;
	}

	public void delete(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return false;
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

	public EOEntity nextEntity(EOEntity entity, String key) {
		return null;
	}
}