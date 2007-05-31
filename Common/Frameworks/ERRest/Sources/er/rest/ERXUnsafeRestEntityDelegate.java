package er.rest;

import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.ERXApplication;

/**
 * ERXUnsafeRestEntityDelegate should NEVER be used in production. This is an entity delegate implementation designed to
 * allow you to explore the features of ERRest without having to actually write custom delegates. This implementation
 * allows full access to read, insert, update, and delegate any object in any model in your system that it is assigned
 * as a delegate for. It will throw an exception on creation if ERXApplication.erxApplication().isDevelopmentMode() is
 * false.
 * 
 * @author mschrag
 */
public class ERXUnsafeRestEntityDelegate extends ERXStandardRestEntityDelegate {
	private NSMutableSet _initializedEntityNames;

	public ERXUnsafeRestEntityDelegate() {
		_initializedEntityNames = new NSMutableSet();

		if (!ERXApplication.isDevelopmentModeSafe()) {
			throw new SecurityException("You are attempting to use ERXUnsafeRestEntityDelegate outside of development mode!.");
		}
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

	public boolean canInsertProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return canUpdateProperty(entity, eo, propertyName, context);
	}

	public boolean canUpdateProperty(EOEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		NSArray allPropertyNames = ERXUnsafeRestEntityDelegate.allPropertyNames(entity, true);
		return allPropertyNames.containsObject(propertyName);
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

	public static NSArray allPropertyNames(EOEntity entity, boolean includeToMany) {
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
			if (classProperties.containsObject(relationship) && (includeToMany || !relationship.isToMany())) {
				displayPropertyNames.addObject(relationship.name());
			}
		}

		return displayPropertyNames;
	}
}