package er.rest.entityDelegates;

import java.util.Enumeration;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.eof.ERXEC;
import er.rest.ERXRestException;
import er.rest.routes.model.IERXEntity;

/**
 * ERXUnsafeRestEntityDelegate should probably never be used in production. This is an entity delegate implementation
 * designed to allow you to explore the features of ERRest without having to actually write custom delegates. This
 * implementation allows full access to read any object in any model in your system that it is assigned to be a delegate
 * for.
 * 
 * @author mschrag
 */
public class ERXUnsafeReadOnlyRestEntityDelegate extends ERXStandardRestEntityDelegate {
	private NSMutableSet<String> _initializedEntityNames;

	public ERXUnsafeReadOnlyRestEntityDelegate() {
		_initializedEntityNames = new NSMutableSet<String>();
	}

	@Override
	public void initializeEntityNamed(String entityName) {
		if (!_initializedEntityNames.containsObject(entityName)) {
			super.initializeEntityNamed(entityName);
			NSArray allPropertyNames = ERXUnsafeRestEntityDelegate.allPropertyNames(ERXRestEntityDelegateUtils.getEntityNamed(new ERXRestContext(null, ERXEC.newEditingContext(), null), entityName), true);
			Enumeration allPropertyNamesEnum = allPropertyNames.objectEnumerator();
			while (allPropertyNamesEnum.hasMoreElements()) {
				String propertyName = (String) allPropertyNamesEnum.nextElement();
				updatePropertyAliasForPropertyNamed(entityName, propertyName);
			}
			_initializedEntityNames.addObject(entityName);
		}
	}

	@Override
	public void delete(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		throw new ERXRestSecurityException("You are not allowed to delete objects for the entity '" + entity.name() + ".");
	}

	@Override
	public void updated(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		throw new ERXRestSecurityException("You are not allowed to update objects for the entity '" + entity.name() + ".");
	}

	@Override
	public void inserted(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		throw new ERXRestSecurityException("You are not allowed to insert objects for the entity '" + entity.name() + ".");
	}

	@Override
	public boolean canInsertProperty(IERXEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return false;
	}

	@Override
	public boolean canUpdateProperty(IERXEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return false;
	}

	public NSArray objectsForEntity(IERXEntity entity, ERXRestContext context) {
		EOFetchSpecification entityFetchSpec = new EOFetchSpecification(entity.name(), null, null);
		NSArray objects = context.editingContext().objectsWithFetchSpecification(entityFetchSpec);
		return objects;
	}

	public boolean canInsertObject(IERXEntity entity, ERXRestContext context) {
		return false;
	}

	public boolean canInsertObject(IERXEntity parentEntity, Object parentObject, String parentKey, IERXEntity entity, ERXRestContext context) {
		return false;
	}

	public boolean canDeleteObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canUpdateObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return false;
	}

	public boolean canViewObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return true;
	}

	@Override
	public boolean canViewProperty(IERXEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return true;
	}

	public NSArray visibleObjects(IERXEntity parentEntity, Object parent, String key, IERXEntity entity, NSArray objects, ERXRestContext context) {
		return objects;
	}
}