package er.rest.entityDelegates;

import java.util.Enumeration;

import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;

import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXProperties;
import er.rest.routes.model.IERXAttribute;
import er.rest.routes.model.IERXEntity;
import er.rest.routes.model.IERXRelationship;

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
	private NSMutableSet<String> _initializedEntityNames;

	/**
	 * Constructs an ERXUnsafeRestEntityDelegate.
	 */
	public ERXUnsafeRestEntityDelegate() {
		this(ERXProperties.booleanForKeyWithDefault("ERXRest.allowUnsafeDelegates", false));
	}

	/**
	 * Constructs an ERXUnsafeRestEntityDelegate.
	 * 
	 * @param allowProductionUse
	 *            if true, this can be used in production without throwing an exception. BE VERY CAREFUL.
	 */
	public ERXUnsafeRestEntityDelegate(boolean allowProductionUse) {
		_initializedEntityNames = new NSMutableSet<String>();

		if (!allowProductionUse && !ERXApplication.isDevelopmentModeSafe()) {
			throw new SecurityException("You are attempting to use ERXUnsafeRestEntityDelegate outside of development mode!.");
		}
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
	public boolean canInsertProperty(IERXEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		return canUpdateProperty(entity, eo, propertyName, context);
	}

	@Override
	public boolean canUpdateProperty(IERXEntity entity, EOEnterpriseObject eo, String propertyName, ERXRestContext context) {
		NSArray allPropertyNames = ERXUnsafeRestEntityDelegate.allPropertyNames(entity, true);
		return allPropertyNames.containsObject(propertyName);
	}

	public NSArray objectsForEntity(IERXEntity entity, ERXRestContext context) {
		EOQualifier qualifier = qualifierFromContext(context);
		NSArray<EOSortOrdering> sortOrderings = sortOrderingsFromContext(context);
		EOFetchSpecification entityFetchSpec = new EOFetchSpecification(entity.name(), qualifier, sortOrderings);
		NSArray objects = context.editingContext().objectsWithFetchSpecification(entityFetchSpec);
		return objects;
	}

	public boolean canInsertObject(IERXEntity entity, ERXRestContext context) {
		return true;
	}

	public boolean canInsertObject(IERXEntity parentEntity, Object parentObject, String parentKey, IERXEntity entity, ERXRestContext context) {
		return true;
	}

	public boolean canDeleteObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return true;
	}

	public boolean canUpdateObject(IERXEntity entity, EOEnterpriseObject eo, ERXRestContext context) {
		return true;
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

	public static NSArray<String> allPropertyNames(IERXEntity entity, boolean includeToMany) {
		NSMutableArray<String> displayPropertyNames = new NSMutableArray<String>();
		Enumeration attributesEnum = entity.attributes().objectEnumerator();
		while (attributesEnum.hasMoreElements()) {
			IERXAttribute attribute = (IERXAttribute) attributesEnum.nextElement();
			if (attribute.isClassProperty()) {
				displayPropertyNames.addObject(attribute.name());
			}
		}

		Enumeration relationshipsEnum = entity.relationships().objectEnumerator();
		while (relationshipsEnum.hasMoreElements()) {
			IERXRelationship relationship = (IERXRelationship) relationshipsEnum.nextElement();
			if (relationship.isClassProperty() && (includeToMany || !relationship.isToMany())) {
				displayPropertyNames.addObject(relationship.name());
			}
		}

		return displayPropertyNames;
	}
}