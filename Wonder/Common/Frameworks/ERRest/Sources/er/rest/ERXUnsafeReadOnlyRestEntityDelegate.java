package er.rest;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;

/**
 * ERXUnsafeRestEntityDelegate should probably never be used in production.  This is an entity delegate
 * implementation designed to allow you to explore the features of ERRest without having to 
 * actually write custom delegates.  This implementation allows full access to read any object in 
 * any model in your system that it is assigned to be a delegate for.
 * 
 * @author mschrag
 */
public class ERXUnsafeReadOnlyRestEntityDelegate extends ERXStandardRestEntityDelegate {
	private NSMutableSet _initializedEntityNames;

	public ERXUnsafeReadOnlyRestEntityDelegate() {
		_initializedEntityNames = new NSMutableSet();
	}
	
	public void initializeEntityNamed(String entityName) {
		if (!_initializedEntityNames.containsObject(entityName)) {
			super.initializeEntityNamed(entityName);
			NSArray allPropertyNames = ERXUnsafeRestEntityDelegate.allPropertyNames(EOModelGroup.defaultGroup().entityNamed(entityName), true);
			Enumeration allPropertyNamesEnum = allPropertyNames.objectEnumerator();
			while (allPropertyNamesEnum.hasMoreElements()) {
				String propertyName = (String) allPropertyNamesEnum.nextElement();
				updatePropertyAliasForPropertyNamed(entityName, propertyName);
			}
			_initializedEntityNames.addObject(entityName);
		}
	}

	public void delete(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		throw new ERXRestSecurityException("You are not allowed to delete objects for the entity '" + entity.name() + ".");
	}

	public void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		throw new ERXRestSecurityException("You are not allowed to update objects for the entity '" + entity.name() + ".");
	}

	public void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		throw new ERXRestSecurityException("You are not allowed to insert objects for the entity '" + entity.name() + ".");
	}

	public boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return false;
	}

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return false;
	}

	public NSArray objectsForEntity(EOEntity entity, ERXRestContext context) {
		EOFetchSpecification entityFetchSpec = new EOFetchSpecification(entity.name(), null, null);
		NSArray objects = context.editingContext().objectsWithFetchSpecification(entityFetchSpec);
		return objects;
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
		return true;
	}

	public boolean canViewProperty(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return true;
	}

	public NSArray visibleObjects(EOEntity parentEntity, Object parent, String key, EOEntity entity, NSArray objects, ERXRestContext context) {
		return objects;
	}
}