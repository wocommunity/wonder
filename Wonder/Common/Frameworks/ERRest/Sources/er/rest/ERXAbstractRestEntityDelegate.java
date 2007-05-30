package er.rest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.ERXEOGlobalIDUtilities;
import er.extensions.ERXGuardedObjectInterface;

/**
 * Provides default implementations of many of the common entity delegate behaviors.
 * 
 * @author mschrag
 */
public abstract class ERXAbstractRestEntityDelegate implements IERXRestEntityDelegate {
	/**
	 * Returns entityName;
	 * 
	 * @return entityName
	 */
	public String entityAliasForEntityNamed(String entityName) {
		return entityName;
	}
	
	/**
	 * Returns propertyAlias.
	 * 
	 * @return propertyAlias
	 */
	public String propertyNameForPropertyAlias(EOEntity entity, String propertyAlias) {
		return propertyAlias;
	}

	/**
	 * Returns propertyName.
	 * 
	 * @return propertyName
	 */
	public String propertyAliasForPropertyNamed(EOEntity entity, String propertyName) {
		return propertyName;
	}

	/**
	 * Returns the value for the given property name.
	 */
	public Object valueForKey(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return NSKeyValueCoding.Utility.valueForKey(obj, propertyName);
	}

	/**
	 * Parses the attribute with parseAttributeValue and sets it on the object.
	 */
	public void takeValueForKey(EOEntity entity, Object obj, String propertyName, String value, ERXRestContext context) throws ParseException, ERXRestException {
		Object parsedAttributeValue = parseAttributeValue(entity, obj, propertyName, value);
		EOKeyValueCoding.Utility.takeStoredValueForKey(obj, parsedAttributeValue, propertyName);
	}

	/**
	 * Does nothing.
	 */
	public void preprocess(EOEntity entity, NSArray objects, ERXRestContext context) throws ERXRestException {
		// Enumeration displayPropertiesEnum = displayProperties(entity).objectEnumerator();
		// while (displayPropertiesEnum.hasMoreElements()) {
		// EOProperty displayProperty = (EOProperty) displayPropertiesEnum.nextElement();
		// if (displayProperty instanceof EORelationship) {
		// EORelationship displayRelationship = (EORelationship) displayProperty;
		// ERXRecursiveBatchFetching.batchFetch(objects, displayRelationship.name());
		// }
		// }
	}

	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		EOGlobalID gid = ERXRestUtils.gidForID(entity, key);
		EOEnterpriseObject obj = ERXEOGlobalIDUtilities.fetchObjectWithGlobalID(context.editingContext(), gid);
		if (obj == null) {
			throw new ERXRestNotFoundException("There is no " + entityAliasForEntityNamed(entity.name()) + " with the id '" + key + "'.");
		}
		if (!canViewObject(entity, obj, context)) {
			throw new ERXRestSecurityException("You are not allowed to view the " + entityAliasForEntityNamed(entity.name()) + " with the id '" + key + "'.");
		}
		return obj;
	}

	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, NSArray objs, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		NSMutableArray filteredObjs = new NSMutableArray();
		Enumeration objsEnum = objs.objectEnumerator();
		while (objsEnum.hasMoreElements()) {
			EOEnterpriseObject eo = (EOEnterpriseObject) objsEnum.nextElement();
			if (ERXRestUtils.idForEO(eo).equals(key)) {
				filteredObjs.addObject(eo);
			}
		}
		if (filteredObjs.count() == 0) {
			throw new ERXRestNotFoundException("There is no " + entityAliasForEntityNamed(entity.name()) + " in this relationship with the id '" + key + "'.");
		}
		EOEnterpriseObject obj = (EOEnterpriseObject) objs.objectAtIndex(0);
		if (!canViewObject(entity, obj, context)) {
			throw new ERXRestSecurityException("You are not allowed to view the " + entityAliasForEntityNamed(entity.name()) + " with the id '" + key + "'.");
		}
		return obj;
	}

	public void delete(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		if (eo instanceof ERXGuardedObjectInterface) {
			((ERXGuardedObjectInterface) eo).delete();
		}
		else {
			eo.editingContext().deleteObject(eo);
		}
	}

	public String formatAttributeValue(EOEntity entity, Object object, String attributeName, Object attributeValue) throws ParseException, ERXRestException {
		String formattedValue;
		if (attributeValue == null) {
			formattedValue = null;
		}
		else {
			formattedValue = attributeValue.toString();
		}
		return formattedValue;
	}

	/**
	 * Parses the given String and returns an object.
	 * 
	 * @param entity the entity
	 * @param object the object
	 * @param attributeName the name of the property
	 * @param attributeValue the value of the property
	 * @return a parsed version of the String
	 * @throws ParseException if a parse failure occurs
	 * @throws ERXRestException if a general failure occurs
	 */
	public Object parseAttributeValue(EOEntity entity, Object object, String attributeName, String attributeValue) throws ParseException, ERXRestException {
		NSKeyValueCoding._KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(object, attributeName);
		Class valueType = binding.valueType();

		Object parsedValue;
		if (attributeValue == null || attributeValue.length() == 0) {
			EOAttribute attribute = entity.attributeNamed(attributeName);
			if (attribute != null && !attribute.allowsNull() && String.class.isAssignableFrom(valueType)) {
				parsedValue = "";
			}
			else {
				parsedValue = EOKeyValueCoding.NullValue;
			}
		}
		else {
			if (String.class.isAssignableFrom(valueType)) {
				parsedValue = attributeValue;
			}
			else if (Boolean.class.isAssignableFrom(valueType)) {
				parsedValue = Boolean.valueOf(attributeValue);
			}
			else if (Character.class.isAssignableFrom(valueType)) {
				parsedValue = new Character(attributeValue.charAt(0));
			}
			else if (Byte.class.isAssignableFrom(valueType)) {
				parsedValue = Byte.valueOf(attributeValue);
			}
			else if (BigDecimal.class.isAssignableFrom(valueType)) {
				parsedValue = new BigDecimal(attributeValue);
			}
			else if (Integer.class.isAssignableFrom(valueType)) {
				parsedValue = Integer.valueOf(attributeValue);
			}
			else if (Short.class.isAssignableFrom(valueType)) {
				parsedValue = Short.valueOf(attributeValue);
			}
			else if (Long.class.isAssignableFrom(valueType)) {
				parsedValue = Long.valueOf(attributeValue);
			}
			else if (Float.class.isAssignableFrom(valueType)) {
				parsedValue = Float.valueOf(attributeValue);
			}
			else if (Double.class.isAssignableFrom(valueType)) {
				parsedValue = Double.valueOf(attributeValue);
			}
			else if (NSTimestamp.class.isAssignableFrom(valueType)) {
				parsedValue = new NSTimestampFormatter().parseObject(attributeValue);
			}
			else {
				throw new ERXRestException("Unable to parse the value '" + attributeValue + "' into a " + valueType.getName() + ".");
			}
		}
		return parsedValue;
	}

	public EOEnterpriseObject insertObjectFromDocument(EOEntity entity, ERXRestRequestNode insertNode, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(context.editingContext(), entity.name());
		_updateObjectFromDocument(true, entity, eo, insertNode, context);
		if (parentObject != null) {
			parentObject.addObjectToBothSidesOfRelationshipWithKey(eo, parentKey);
		}
		inserted(entity, eo, context);
		return eo;
	}

	public void updateObjectFromDocument(EOEntity entity, EOEnterpriseObject eo, ERXRestRequestNode eoNode, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		_updateObjectFromDocument(false, entity, eo, eoNode, context);
	}

	public void _updateObjectFromDocument(boolean inserting, EOEntity entity, EOEnterpriseObject eo, ERXRestRequestNode eoNode, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		if (!entityAliasForEntityNamed(entity.name()).equals(eoNode.name())) {
			throw new ERXRestException("You attempted to put a " + eoNode.name() + " into a " + entityAliasForEntityNamed(entity.name()) + ".");
		}

		Enumeration attributeNodesEnum = eoNode.children().objectEnumerator();
		while (attributeNodesEnum.hasMoreElements()) {
			ERXRestRequestNode attributeNode = (ERXRestRequestNode)attributeNodesEnum.nextElement();

			String attributeName = attributeNode.name();
			if (inserting && !canInsertProperty(entity, eo, attributeName, context)) {
				throw new ERXRestSecurityException("You are not allowed to insert the property '" + attributeName + "' on " + entityAliasForEntityNamed(entity.name()) + ".");
			}
			else if (!inserting && !canUpdateProperty(entity, eo, attributeName, context)) {
				throw new ERXRestSecurityException("You are not allowed to update the property '" + attributeName + "' on " + entityAliasForEntityNamed(entity.name()) + ".");
			}

			EORelationship relationship = entity.relationshipNamed(attributeName);
			if (relationship != null) {
				EOEntity destinationEntity = relationship.destinationEntity();
				if (!relationship.isToMany()) {
					EOEnterpriseObject originalObject = (EOEnterpriseObject) valueForKey(entity, eo, attributeName, context);
					String id = attributeNode.attributeForKey("id");
					if (id == null) {
						eo.removeObjectFromBothSidesOfRelationshipWithKey(originalObject, attributeName);
					}
					else {
						EOEnterpriseObject newObject = context.delegate().entityDelegate(destinationEntity).objectWithKey(destinationEntity, id, context);
						if (originalObject == null && newObject != null) {
							eo.addObjectToBothSidesOfRelationshipWithKey(newObject, attributeName);
						}
						else if (originalObject != null && !originalObject.equals(newObject)) {
							eo.removeObjectFromBothSidesOfRelationshipWithKey(originalObject, attributeName);
							if (newObject != null) {
								eo.addObjectToBothSidesOfRelationshipWithKey(newObject, attributeName);
							}
						}
					}
				}
				else {
					NSArray currentObjects = (NSArray) valueForKey(entity, eo, attributeName, context);
					NSArray toManyNodes = attributeNode.children();
					updateArrayFromDocument(entity, eo, attributeName, destinationEntity, currentObjects, toManyNodes, context);
				}
			}
			else {
				String attributeValue = attributeNode.value();
				try {
					takeValueForKey(entity, eo, attributeName, attributeValue, context);
				}
				catch (ParseException e) {
					throw new ERXRestException("Failed to parse attribute value '" + attributeValue + "'.", e);
				}
			}
		}

		updated(entity, eo, context);
	}

	public void updateArrayFromDocument(EOEntity parentEntity, EOEnterpriseObject parentObject, String attributeName, EOEntity entity, NSArray currentObjects, NSArray toManyNodes, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		NSMutableArray keepObjects = new NSMutableArray();
		NSMutableArray addObjects = new NSMutableArray();
		NSMutableArray removeObjects = new NSMutableArray();

		Enumeration toManyNodesEnum = toManyNodes.objectEnumerator();
		while (toManyNodesEnum.hasMoreElements()) {
			ERXRestRequestNode toManyNode = (ERXRestRequestNode) toManyNodesEnum.nextElement();
			String toManyNodeName = toManyNode.name();
			if (!entityAliasForEntityNamed(entity.name()).equals(toManyNodeName)) {
				throw new ERXRestException("You attempted to put a " + toManyNodeName + " into a " + entityAliasForEntityNamed(entity.name()) + ".");
			}

			String id = toManyNode.attributeForKey("id");
			Object relatedObject = objectWithKey(entity, id, context);
			if (currentObjects.containsObject(relatedObject)) {
				System.out.println("AbstractERXRestDelegate.updateArray: keeping " + relatedObject + " in " + parentObject + " (" + attributeName + ")");
				keepObjects.addObject(relatedObject);
			}
			else {
				addObjects.addObject(relatedObject);
			}
		}

		Enumeration currentObjectsEnum = currentObjects.immutableClone().objectEnumerator();
		while (currentObjectsEnum.hasMoreElements()) {
			EOEnterpriseObject currentObject = (EOEnterpriseObject) currentObjectsEnum.nextElement();
			if (!keepObjects.containsObject(currentObject)) {
				System.out.println("AbstractERXRestDelegate.updateArray: removing " + currentObject + " from " + parentObject + " (" + attributeName + ")");
				parentObject.removeObjectFromBothSidesOfRelationshipWithKey(currentObject, attributeName);
			}
		}

		Enumeration addObjectsEnum = addObjects.objectEnumerator();
		while (addObjectsEnum.hasMoreElements()) {
			EOEnterpriseObject addObject = (EOEnterpriseObject) addObjectsEnum.nextElement();
			System.out.println("AbstractERXRestDelegate.updateArray: adding " + addObject + " to " + parentObject + " (" + attributeName + ")");
			parentObject.addObjectToBothSidesOfRelationshipWithKey(addObject, attributeName);
		}

		updated(entity, parentObject, context);
	}

	/**
	 * Called after performing the user's requested updates.  This provides support for subclasses to extend
	 * and perform "automatic" updates.  For instance, if you wanted to set a last modified date, or a
	 * modified-by-user field, you could do that here.
	 *  
	 * @param entity the entity of the object being updated
	 * @param eo the updated object
	 * @param context the rest context
	 * @throws ERXRestException if a general error occurs
	 * @throws ERXRestSecurityException if a security error occurs
	 */
	public abstract void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

	/**
	 * Called after performing the user's requested insert.  This provides support for subclasses to extend
	 * and set "automatic" attributes.  For instance, if you wanted to set a creation date, or a
	 * created-by-user field, you could do that here.
	 *  
	 * @param entity the entity of the object being inserted
	 * @param eo the inserted object
	 * @param context the rest context
	 * @throws ERXRestException if a general error occurs
	 * @throws ERXRestSecurityException if a security error occurs
	 */
	public abstract void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

}
