package er.rest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.ERXEOGlobalIDUtilities;
import er.extensions.ERXGuardedObjectInterface;
import er.extensions.ERXQ;
import er.extensions.ERXStringUtilities;

/**
 * Provides default implementations of many of the common entity delegate behaviors.
 * 
 * @author mschrag
 */
public abstract class ERXAbstractRestEntityDelegate implements IERXRestEntityDelegate {
	/**
	 * Do nothing by default
	 */
	public void initializeEntityNamed(String entityName) {
		// DO NOTHING
	}

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
	
	/**
	 * Returns whether or not the given key value is the primary key of
	 * an EO.  This is crazy -- It tries to guess if it's looking at
	 * a key or not.
	 * 
	 * @param restKey the possible EO key
         *
	 * @return true if key is a primary key
	 */
	public boolean isEOID(ERXRestKey restKey) {
		boolean isID = false;
		String key = restKey.key();
		if (key != null) {
			EOEntity entity = restKey.entity();
			if (_isEOID(entity, key)) {
				isID = true;
			}
			else if (restKey.previousKey() == null) {
				if (!restKey.isKeyAll()) {
					isID = true;
				}
			}
			else {
				if (restKey.previousKey().isKeyAll()) {
					isID = true;
				}
				else {
					try {
						Object previousValue = restKey.previousKey()._value(false);
						if (previousValue instanceof NSArray) {
							isID = true;
						}
					}
					catch (ERXRestException e) {
						throw new RuntimeException("Failed to check key '" + key + "'.", e);
					}
					catch (ERXRestSecurityException e) {
						throw new RuntimeException("Failed to check key '" + key + "'.", e);
					}
					catch (ERXRestNotFoundException e) {
						throw new RuntimeException("Failed to check key '" + key + "'.", e);
					}
				}
			}
		}
		return isID;
	}
	
	protected String idAttributeName(EOEntity entity) {
		return null;
	}
	
	protected boolean _isEOID(EOEntity entity, String key) {
		boolean isID = !entity._propertyNames().containsObject(key);
		return isID;
	}

	/**
	 * Returns the string form of the primary key of the given EO.
	 * 
	 * @param eo the EO to get a primary key for
	 * @return the primary key
	 */
	public String stringIDForEO(EOEntity entity, EOEnterpriseObject eo) {
		Object id = idForEO(entity, eo);
		String idStr;
		if (id instanceof Object[]) {
			throw new IllegalArgumentException(eo.entityName() + " has a compound primary key, which is currently not supported.");
		}
		else {
			idStr = String.valueOf(id);
		}
		return idStr;
	}
	
	/**
	 * Returns the primary key of the given EO.
	 * 
	 * @param eo the EO to get a primary key for
	 * @return the primary key
	 */
	public Object idForEO(EOEntity entity, EOEnterpriseObject eo) {
		String idAttributeName = idAttributeName(entity);
		Object id;
		if (idAttributeName != null) {
			id = eo.valueForKey(idAttributeName);
		}
		else {
			EOGlobalID gid = eo.editingContext().globalIDForObject(eo);
			if (!(gid instanceof EOKeyGlobalID)) {
				throw new IllegalArgumentException("Unsupported primary key type '" + gid + "'.");
			}
			EOKeyGlobalID keyGID = (EOKeyGlobalID) gid;
			Object[] keyValues = keyGID.keyValues();
			if (keyValues.length > 1) {
				throw new IllegalArgumentException("Compound primary keys (" + eo.entityName() + ") are not currently supported.");
			}
			if (keyValues.length == 1) {
				id = keyValues[0];
			}
			else {
				id = keyValues;
			}
		}
		return id;
	}

	protected String idForNode(ERXRestRequestNode attributeNode) {
		String id = attributeNode.attributeForKey("id");
		if (id == null) {
			ERXRestRequestNode idNode = attributeNode.childNamed("id");
			if (idNode != null) {
				id = idNode.value();
			}
			else {
				id = attributeNode.value();
			}
		}
		if (id != null && id.length() == 0) {
			id = null;
		}
		return id;
	}

	public EOEnterpriseObject objectForNode(EOEntity entity, ERXRestRequestNode node, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		String idForNode = idForNode(node);
		EOEnterpriseObject eo;
		if (node.isNull()) {
			eo = null;
		}
		else if (idForNode == null) {
			if (node.children().count() == 0) {
				eo = null;
			}
			else {
				eo = null;
			}
		}
		else {
			eo = objectWithKey(entity, idForNode, context);
		}
		return eo;
	}

	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		EOEnterpriseObject obj;
		String idAttributeName = idAttributeName(entity);
		if (idAttributeName == null) {
			EOGlobalID gid;
			if (ERXStringUtilities.isDigitsOnly(key)) {
				gid = EOKeyGlobalID.globalIDWithEntityName(entity.name(), new Object[] { Integer.valueOf(key) });
			}
			else {
				NSArray primaryKeyAttributes = entity.primaryKeyAttributes();
				if (primaryKeyAttributes.count() > 1) {
					throw new IllegalArgumentException("Compound primary keys (" + entity + ") are not currently supported.");
				}
				EOAttribute primaryKeyAttribute = (EOAttribute) primaryKeyAttributes.objectAtIndex(0);
				String valueType = primaryKeyAttribute.valueType();
				gid = EOKeyGlobalID.globalIDWithEntityName(entity.name(), new Object[] { key });
			}
		
			obj = ERXEOGlobalIDUtilities.fetchObjectWithGlobalID(context.editingContext(), gid);
		}
		else {
			EOFetchSpecification fetchSpec = new EOFetchSpecification(entity.name(), ERXQ.equals(idAttributeName, key), null);
			fetchSpec.setIsDeep(true);
			NSArray matchingObjects = context.editingContext().objectsWithFetchSpecification(fetchSpec);
			if (matchingObjects.count() == 0) {
				obj = null;
			}
			else if (matchingObjects.count() == 1) {
				obj = (EOEnterpriseObject) matchingObjects.objectAtIndex(0);
			}
			else {
				throw new ERXRestException("There was more than one " + entityAliasForEntityNamed(entity.name()) + " with the id '" + key + "'.");
			}
		}
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
			if (stringIDForEO(entity, eo).equals(key)) {
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
		if (!canDeleteObject(entity, eo, context)) {
			throw new ERXRestSecurityException("You are not allowed to delete the given " + entityAliasForEntityNamed(entity.name()) + " object.");
		}

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
		else if (attributeValue instanceof NSTimestamp) {
			NSTimestamp timestamp = (NSTimestamp) attributeValue;
			formattedValue = new NSTimestampFormatter("%Y-%m-%dT%H:%M:%SZ").format(timestamp);
		}
		else {
			formattedValue = attributeValue.toString();
		}
		return formattedValue;
	}

	/**
	 * Parses the given String and returns an object.
	 * 
	 * @param entity
	 *            the entity
	 * @param object
	 *            the object
	 * @param attributeName
	 *            the name of the property
	 * @param attributeValue
	 *            the value of the property
	 * @return a parsed version of the String
	 * @throws ParseException
	 *             if a parse failure occurs
	 * @throws ERXRestException
	 *             if a general failure occurs
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
				if (attributeValue.indexOf(' ') == -1) {
					parsedValue = new NSTimestampFormatter("%Y-%m-%dT%H:%M:%SZ").parseObject(attributeValue);
				}
				else {
					parsedValue = new NSTimestampFormatter().parseObject(attributeValue);
				}
			}
			else {
				throw new ERXRestException("Unable to parse the value '" + attributeValue + "' into a " + valueType.getName() + ".");
			}
		}
		return parsedValue;
	}

	public EOEnterpriseObject insertObjectFromDocument(EOEntity entity, ERXRestRequestNode insertNode, EOEntity parentEntity, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		boolean canInsert;
		if (parentObject == null) {
			canInsert = canInsertObject(entity, context);
		}
		else {
			canInsert = canInsertObject(parentEntity, parentObject, parentKey, entity, context);
		}
		if (!canInsert) {
			throw new ERXRestSecurityException("You are not allowed to insert this " + entityAliasForEntityNamed(entity.name()) + " object.");
		}

		EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(context.editingContext(), entity.name());
		_updatePropertiesFromDocument(true, entity, eo, insertNode, context);
		if (parentObject != null) {
			parentObject.addObjectToBothSidesOfRelationshipWithKey(eo, parentKey);
		}
		inserted(entity, eo, context);
		return eo;
	}

	public EOEnterpriseObject processObjectFromDocument(EOEntity entity, ERXRestRequestNode eoNode, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		IERXRestEntityDelegate delegate = context.delegate().entityDelegate(entity);
		EOEnterpriseObject eo;
		try {
			eo = delegate.objectForNode(entity, eoNode, context);
			updateObjectFromDocument(entity, eo, eoNode, context);
		}
		catch (ERXRestNotFoundException e) {
			eo = insertObjectFromDocument(entity, eoNode, null, null, null, context);
		}
		return eo;
	}
	
	public void updateObjectFromDocument(EOEntity entity, EOEnterpriseObject eo, ERXRestRequestNode eoNode, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		if (!canUpdateObject(entity, eo, context)) {
			throw new ERXRestSecurityException("You are not allowed to update this " + entityAliasForEntityNamed(entity.name()) + " object.");
		}

		_updatePropertiesFromDocument(false, entity, eo, eoNode, context);
	}

	public void _updateRelationshipFromDocument(EOEntity entity, EOEnterpriseObject eo, EORelationship relationship, ERXRestRequestNode relationshipNode, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		String relationshipName = relationship.name();
		EOEntity destinationEntity = relationship.destinationEntity();
		IERXRestEntityDelegate destinationEntityDelegate = context.delegate().entityDelegate(destinationEntity);
		if (!relationship.isToMany()) {
			EOEnterpriseObject originalObject = (EOEnterpriseObject) valueForKey(entity, eo, relationship.name(), context);
			EOEnterpriseObject newObject = destinationEntityDelegate.objectForNode(destinationEntity, relationshipNode, context);
			
			if (newObject == null && !relationshipNode.isNull() && relationshipNode.children().count() > 0) {
				newObject = destinationEntityDelegate.insertObjectFromDocument(destinationEntity, relationshipNode, entity, eo, relationshipName, context);
			}
			
			// MS: ignore nil="true" to-one?
			if (relationshipNode.isNull() || newObject == null) {
				if (!relationship.isMandatory()) {
					eo.removeObjectFromBothSidesOfRelationshipWithKey(originalObject, relationshipName);
				}
				else {
					//System.out.println("ERXAbstractRestEntityDelegate._updateRelationshipFromDocument: A " + relationshipName);
					// MS: Throw?
				}
			}
			else {
				if (originalObject == null && newObject != null) {
					eo.addObjectToBothSidesOfRelationshipWithKey(newObject, relationshipName);
				}
				else if (originalObject != null && !originalObject.equals(newObject)) {
					eo.removeObjectFromBothSidesOfRelationshipWithKey(originalObject, relationshipName);
					if (newObject != null) {
						eo.addObjectToBothSidesOfRelationshipWithKey(newObject, relationshipName);
					}
				}
			}
		}
		else {
			NSArray originalObjects = (NSArray) valueForKey(entity, eo, relationshipName, context);
			NSArray newNodes = relationshipNode.children();
			// MS: ignore nil="true" to-many?
			if (!relationshipNode.isNull()) {
				_updateArrayFromDocument(entity, eo, relationshipName, destinationEntity, originalObjects, newNodes, context);
			}
			else {
				// MS: ???
			}
		}
	}
	
	public void _updatePropertiesFromDocument(boolean inserting, EOEntity entity, EOEnterpriseObject eo, ERXRestRequestNode eoNode, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		String entityAlias = entityAliasForEntityNamed(entity.name());
		String type = eoNode.type();
		if (!entityAlias.equals(type)) {
			if (!entityAlias.equals(eoNode.name())) {
				throw new ERXRestException("You attempted to put a " + eoNode.name() + " into a " + entityAlias + ".");
			}
		}

		IERXRestEntityDelegate entityDelegate = context.delegate().entityDelegate(entity);
		Enumeration attributeNodesEnum = eoNode.children().objectEnumerator();
		while (attributeNodesEnum.hasMoreElements()) {
			ERXRestRequestNode attributeNode = (ERXRestRequestNode)attributeNodesEnum.nextElement();

			boolean updateAttribute = true;
			String attributeName = entityDelegate.propertyNameForPropertyAlias(entity, attributeNode.name());
			if ("id".equals(attributeName)) {
				updateAttribute = false;
			}
			
			if (updateAttribute) {
				if (inserting && !canInsertProperty(entity, eo, attributeName, context)) {
					throw new ERXRestSecurityException("You are not allowed to insert the property '" + attributeName + "' on " + entityAlias + ".");
				}
				else if (!inserting && !canUpdateProperty(entity, eo, attributeName, context)) {
					throw new ERXRestSecurityException("You are not allowed to update the property '" + attributeName + "' on " + entityAlias + ".");
				}
				
				EORelationship relationship = entity.relationshipNamed(attributeName);
				if (relationship != null) {
					_updateRelationshipFromDocument(entity, eo, relationship, attributeNode, context);
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
		}

		updated(entity, eo, context);
	}

	public void updateArrayFromDocument(EOEntity parentEntity, EOEnterpriseObject parentObject, String attributeName, EOEntity entity, NSArray currentObjects, NSArray toManyNodes, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		IERXRestEntityDelegate parentEntityDelegate = context.delegate().entityDelegate(entity);
		if (parentObject != null && !parentEntityDelegate.canUpdateObject(parentEntity, parentObject, context)) {
			throw new ERXRestSecurityException("You are not allowed to update this " + entity.name() + " object.");
		}
		if (parentObject != null && attributeName != null && !parentEntityDelegate.canUpdateProperty(parentEntity, parentObject, attributeName, context)) {
			throw new ERXRestSecurityException("You are not allowed to update this " + entity.name() + " object.");
		}
		_updateArrayFromDocument(parentEntity, parentObject, attributeName, entity, currentObjects, toManyNodes, context);
	}

	protected void _updateArrayFromDocument(EOEntity parentEntity, EOEnterpriseObject parentObject, String attributeName, EOEntity entity, NSArray currentObjects, NSArray toManyNodes, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		NSMutableArray keepObjects = new NSMutableArray();
		NSMutableArray addObjects = new NSMutableArray();
		NSMutableArray removeObjects = new NSMutableArray();

		IERXRestEntityDelegate destinationEntityDelegate = context.delegate().entityDelegate(entity);
		Enumeration toManyNodesEnum = toManyNodes.objectEnumerator();
		while (toManyNodesEnum.hasMoreElements()) {
			ERXRestRequestNode toManyNode = (ERXRestRequestNode) toManyNodesEnum.nextElement();
			String toManyNodeName = toManyNode.name();
			if (!entityAliasForEntityNamed(entity.name()).equals(toManyNodeName)) {
				throw new ERXRestException("You attempted to put a " + toManyNodeName + " into a " + entityAliasForEntityNamed(entity.name()) + ".");
			}

			try {
				EOEnterpriseObject relatedObject = objectForNode(entity, toManyNode, context);
				destinationEntityDelegate.updateObjectFromDocument(entity, relatedObject, toManyNode, context);
				if (currentObjects.containsObject(relatedObject)) {
					keepObjects.addObject(relatedObject);
				}
				else {
					addObjects.addObject(relatedObject);
				}
			}
			catch (ERXRestNotFoundException e) {
				//System.out.println("ERXAbstractRestEntityDelegate._updateArrayFromDocument: inserting " + attributeName + " on " + parentObject);
				EOEnterpriseObject relatedObject = destinationEntityDelegate.insertObjectFromDocument(entity, toManyNode, parentEntity, parentObject, attributeName, context);
				addObjects.addObject(relatedObject);
			}
		}

		Enumeration currentObjectsEnum = currentObjects.immutableClone().objectEnumerator();
		while (currentObjectsEnum.hasMoreElements()) {
			EOEnterpriseObject currentObject = (EOEnterpriseObject) currentObjectsEnum.nextElement();
			if (!keepObjects.containsObject(currentObject)) {
				//System.out.println("AbstractERXRestDelegate.updateArray: removing " + currentObject + " from " + parentObject + " (" + attributeName + ")");
				parentObject.removeObjectFromBothSidesOfRelationshipWithKey(currentObject, attributeName);
			}
		}

		Enumeration addObjectsEnum = addObjects.objectEnumerator();
		while (addObjectsEnum.hasMoreElements()) {
			EOEnterpriseObject addObject = (EOEnterpriseObject) addObjectsEnum.nextElement();
			//System.out.println("AbstractERXRestDelegate.updateArray: adding " + addObject + " to " + parentObject + " (" + attributeName + ")");
			parentObject.addObjectToBothSidesOfRelationshipWithKey(addObject, attributeName);
		}

		updated(entity, parentObject, context);
	}

	/**
	 * Called after performing the user's requested updates. This provides support for subclasses to extend and perform
	 * "automatic" updates. For instance, if you wanted to set a last modified date, or a modified-by-user field, you
	 * could do that here.
	 * 
	 * @param entity
	 *            the entity of the object being updated
	 * @param eo
	 *            the updated object
	 * @param context
	 *            the rest context
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 */
	public abstract void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

	/**
	 * Called after performing the user's requested insert. This provides support for subclasses to extend and set
	 * "automatic" attributes. For instance, if you wanted to set a creation date, or a created-by-user field, you could
	 * do that here.
	 * 
	 * @param entity
	 *            the entity of the object being inserted
	 * @param eo
	 *            the inserted object
	 * @param context
	 *            the rest context
	 * @throws ERXRestException
	 *             if a general error occurs
	 * @throws ERXRestSecurityException
	 *             if a security error occurs
	 */
	public abstract void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

}
