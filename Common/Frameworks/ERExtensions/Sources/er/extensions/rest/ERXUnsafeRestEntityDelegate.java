package er.extensions.rest;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class ERXUnsafeRestEntityDelegate extends ERXAbstractRestEntityDelegate {
	public void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException {
		// DO NOTHING
	}

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return displayPropertyNames(entity, eo, context).containsObject(propertyName);
	}

	public NSArray displayPropertyNames(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		NSMutableArray displayPropertyNames = new NSMutableArray();
		NSArray classProperties = entity.classProperties();
		Enumeration attributesEnum = entity.attributes().objectEnumerator();
		while (attributesEnum.hasMoreElements()) {
			EOAttribute attribute = (EOAttribute) attributesEnum.nextElement();
			if (classProperties.containsObject(attribute)) {
				displayPropertyNames.addObject(attribute.name());
			}
		}

		Enumeration relationshipsEnum = entity.relationships().objectEnumerator();
		while (relationshipsEnum.hasMoreElements()) {
			EORelationship relationship = (EORelationship) relationshipsEnum.nextElement();
			if (classProperties.containsObject(relationship) && !relationship.isToMany()) {
				displayPropertyNames.addObject(relationship.name());
			}
		}

		return displayPropertyNames;
	}

	public NSArray objectsForEntity(EOEntity entity, ERXRestContext context) {
		EOFetchSpecification entityFetchSpec = new EOFetchSpecification(entity.name(), null, null);
		NSArray objects = context.editingContext().objectsWithFetchSpecification(entityFetchSpec);
		return objects;
	}

	public boolean canInsertObject(EOEntity entity, ERXRestContext context) {
		return true;
	}

	public boolean canInsertObject(EOEntity parentEntity, Object parentObject, String parentKey, EOEntity entity, ERXRestContext context) {
		return true;
	}

	public boolean canDeleteObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return true;
	}

	public boolean canUpdateObject(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return true;
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

	public boolean isNextKeyVisible(EOEntity entity, Object value, String nextKey, ERXRestContext context) {
		return true;
	}

	public ERXRestResult nextNonModelResult(EOEntity entity, Object value, String nextKey, String nextPath, boolean includeContent, ERXRestContext context) {
		return null;
	}
}