package er.rest.entityDelegates;

import java.text.ParseException;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.eof.ERXEOGlobalIDUtilities;
import er.extensions.eof.ERXFetchSpecification;
import er.extensions.eof.ERXGuardedObjectInterface;
import er.extensions.eof.ERXQ;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;
import er.rest.ERXRestException;
import er.rest.ERXRestRequestNode;
import er.rest.ERXRestUtils;

/**
 * Provides default implementations of many of the common entity delegate behaviors.
 * 
 * @author mschrag
 * @deprecated  Will be deleted soon ["personally i'd mark them with a delete into the trashcan" - mschrag]
 */
@Deprecated
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
		return NSKeyValueCodingAdditions.Utility.valueForKeyPath(obj, propertyName);
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

	@SuppressWarnings("unchecked")
	protected NSArray<String> allPossiblePropertyNamesOnEarth(EOEntity entity) {
		NSArray<String> propertyNames = entity._propertyNames();
		NSArray<EOEntity> subEntities = entity.subEntities();
		if (subEntities.count() > 0) {
			NSMutableSet<String> mutablePropertyNames = new NSMutableSet<String>(propertyNames);
			for (EOEntity subEntity : subEntities) {
				mutablePropertyNames.addObjectsFromArray(subEntity._propertyNames());
			}
			propertyNames = mutablePropertyNames.allObjects();
		}
		return propertyNames;
	}

	/**
	 * Returns whether or not the given key value is the primary key of an EO. This is crazy -- It tries to guess if
	 * it's looking at a key or not.
	 * 
	 * @param restKey
	 *            the possible EO key
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
					if (allPossiblePropertyNamesOnEarth(entity).containsObject(key)) {
						isID = false;
					}
					else {
						isID = true;
					}
				}
			}
			else {
				if (restKey.previousKey().isKeyAll()) {
					isID = true;
				}
				else if (allPossiblePropertyNamesOnEarth(entity).containsObject(key)) {
					isID = false;
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
		boolean isID = !allPossiblePropertyNamesOnEarth(entity).containsObject(key);
		return isID;
	}

	/**
	 * Returns the string form of the primary key of the given EO.
	 * 
	 * @param eo
	 *            the EO to get a primary key for
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
	 * @param eo
	 *            the EO to get a primary key for
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
		Object idObj = attributeNode.id();
		String id = String.valueOf(idObj);
		if (idObj != null && id.length() == 0) {
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
		EOEnterpriseObject obj = _objectWithKey(entity, key, context);
		if (obj == null) {
			throw new ERXRestNotFoundException("There is no " + entityAliasForEntityNamed(entity.name()) + " with the id '" + key + "'.");
		}
		if (!canViewObject(entity, obj, context)) {
			throw new ERXRestSecurityException("You are not allowed to view the " + entityAliasForEntityNamed(entity.name()) + " with the id '" + key + "'.");
		}
		return obj;
	}

	protected EOEnterpriseObject _objectWithKey(EOEntity entity, String key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
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
				if(NSData.class.getName().equals(primaryKeyAttribute.className())) {
					if(!key.startsWith("<")) {
						key = "<" + key + ">";
					}
					gid = EOKeyGlobalID.globalIDWithEntityName(entity.name(), new Object[] { new NSData((NSData)NSPropertyListSerialization.propertyListFromString(key)) });
				} else {
					gid = EOKeyGlobalID.globalIDWithEntityName(entity.name(), new Object[] { key });
				}
			}

			obj = ERXEOGlobalIDUtilities.fetchObjectWithGlobalID(context.editingContext(), gid);
		}
		else {
			ERXFetchSpecification fetchSpec = new ERXFetchSpecification(entity.name(), ERXQ.equals(idAttributeName, key), null);
			fetchSpec.setIncludeEditingContextChanges(true);
			fetchSpec.setIsDeep(true);
			NSArray matchingObjects = context.editingContext().objectsWithFetchSpecification(fetchSpec);
			if (matchingObjects.count() == 0) {
				obj = null;
			}
			else if (matchingObjects.count() == 1) {
				obj = (EOEnterpriseObject) matchingObjects.objectAtIndex(0);
			}
			else {
				throw new ERXRestException("There was more than one " + entityAliasForEntityNamed(entity.name()) + " with the " + idAttributeName + " '" + key + "'.");
			}
		}
		return obj;
	}

	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, NSArray objs, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		NSMutableArray<EOEnterpriseObject> filteredObjs = new NSMutableArray<EOEnterpriseObject>();
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
		EOEnterpriseObject obj = filteredObjs.objectAtIndex(0);
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

	public Object parseAttributeValue(EOEntity entity, Object object, String attributeName, String attributeValue) throws ParseException, ERXRestException {
		return ERXRestUtils.coerceValueToAttributeType(attributeValue, entity.classDescriptionForInstances(), object, attributeName, new er.rest.ERXRestContext());
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
			if (eo == null) {
				eo = insertObjectFromDocument(entity, eoNode, null, null, null, context);
			}
			else {
				updateObjectFromDocument(entity, eo, eoNode, context);
			}
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
		String typeName = relationshipNode.type();
		if (typeName == null) {
			EOEntity destinationEntity = relationship.destinationEntity();
			typeName = destinationEntity.name();
		}
		EOEntity destinationEntity = ERXRestEntityDelegateUtils.requiredEntityNamed(context, typeName);
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
					// System.out.println("ERXAbstractRestEntityDelegate._updateRelationshipFromDocument: A " +
					// relationshipName);
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
		entity = ERXRestEntityDelegateUtils.requiredEntityNamed(context, eo.entityName());
		String entityAlias = entityAliasForEntityNamed(eo.entityName());
		String type = eoNode.type();
		if (!entityAlias.equals(type)) {
			if (!entityAlias.equals(eoNode.name())) {
				throw new ERXRestException("You attempted to put a " + eoNode.name() + " into a " + entityAlias + "." + eo + "  " + eoNode);
			}
		}

		IERXRestEntityDelegate entityDelegate = context.delegate().entityDelegate(entity);
		Enumeration attributeNodesEnum = eoNode.children().objectEnumerator();
		while (attributeNodesEnum.hasMoreElements()) {
			ERXRestRequestNode attributeNode = (ERXRestRequestNode) attributeNodesEnum.nextElement();

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
					String attributeValue = (String) attributeNode.value(); // MS: This cast is totally wrong in the general case, but I don't want to fix everything at the moment
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
		IERXRestSecurityDelegate parentEntityDelegate = context.delegate().entityDelegate(entity);
		if (parentObject != null && !parentEntityDelegate.canUpdateObject(parentEntity, parentObject, context)) {
			throw new ERXRestSecurityException("You are not allowed to update this " + entity.name() + " object.");
		}
		if (parentObject != null && attributeName != null && !parentEntityDelegate.canUpdateProperty(parentEntity, parentObject, attributeName, context)) {
			throw new ERXRestSecurityException("You are not allowed to update this " + entity.name() + " object.");
		}
		_updateArrayFromDocument(parentEntity, parentObject, attributeName, entity, currentObjects, toManyNodes, context);
	}

	protected void _updateArrayFromDocument(EOEntity parentEntity, EOEnterpriseObject parentObject, String attributeName, EOEntity entity, NSArray currentObjects, NSArray toManyNodes, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		NSMutableArray<EOEnterpriseObject> keepObjects = new NSMutableArray<EOEnterpriseObject>();
		NSMutableArray<EOEnterpriseObject> addObjects = new NSMutableArray<EOEnterpriseObject>();
		NSMutableArray<EOEnterpriseObject> removeObjects = new NSMutableArray<EOEnterpriseObject>();

		IERXRestEntityDelegate destinationEntityDelegate = context.delegate().entityDelegate(entity);
		Enumeration toManyNodesEnum = toManyNodes.objectEnumerator();
		while (toManyNodesEnum.hasMoreElements()) {
			ERXRestRequestNode toManyNode = (ERXRestRequestNode) toManyNodesEnum.nextElement();
			String toManyNodeName = toManyNode.name();
			entity = ERXRestEntityDelegateUtils.requiredEntityNamed(context, toManyNodeName);
			if (!entityAliasForEntityNamed(entity.name()).equals(toManyNodeName)) {
				throw new ERXRestException("You attempted to put a " + toManyNodeName + " into a " + entityAliasForEntityNamed(entity.name()) + ".");
			}

			try {
				EOEnterpriseObject relatedObject = destinationEntityDelegate.objectForNode(entity, toManyNode, context);
				if (relatedObject == null) {
					relatedObject = destinationEntityDelegate.insertObjectFromDocument(entity, toManyNode, parentEntity, parentObject, attributeName, context);
					addObjects.addObject(relatedObject);
				}
				else {
					destinationEntityDelegate.updateObjectFromDocument(entity, relatedObject, toManyNode, context);
					if (currentObjects.containsObject(relatedObject)) {
						keepObjects.addObject(relatedObject);
					}
					else {
						addObjects.addObject(relatedObject);
					}
				}
			}
			catch (ERXRestNotFoundException e) {
				// System.out.println("ERXAbstractRestEntityDelegate._updateArrayFromDocument: inserting " +
				// attributeName + " on " + parentObject);
				EOEnterpriseObject relatedObject = destinationEntityDelegate.insertObjectFromDocument(entity, toManyNode, parentEntity, parentObject, attributeName, context);
				addObjects.addObject(relatedObject);
			}
		}

		Enumeration currentObjectsEnum = currentObjects.immutableClone().objectEnumerator();
		while (currentObjectsEnum.hasMoreElements()) {
			EOEnterpriseObject currentObject = (EOEnterpriseObject) currentObjectsEnum.nextElement();
			if (!keepObjects.containsObject(currentObject)) {
				// System.out.println("AbstractERXRestDelegate.updateArray: removing " + currentObject + " from " +
				// parentObject + " (" + attributeName + ")");
				parentObject.removeObjectFromBothSidesOfRelationshipWithKey(currentObject, attributeName);
			}
		}

		Enumeration addObjectsEnum = addObjects.objectEnumerator();
		while (addObjectsEnum.hasMoreElements()) {
			EOEnterpriseObject addObject = (EOEnterpriseObject) addObjectsEnum.nextElement();
			// System.out.println("AbstractERXRestDelegate.updateArray: adding " + addObject + " to " + parentObject +
			// " (" + attributeName + ")");
			parentObject.addObjectToBothSidesOfRelationshipWithKey(addObject, attributeName);
		}

		updated(entity, parentObject, context);
	}


	public static String cascadingValue(ERXRestKey result, String propertyPrefix, String propertySuffix, String defaultValue) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		// System.out.println("ERXAbstractRestResponseWriter.cascadingValue: Checking " + result);
		ERXRestKey cascadingKey = result.firstKey();
		String propertyValue = _cascadingValue(cascadingKey, propertyPrefix, propertySuffix);
		if (propertyValue == null) {
			propertyValue = defaultValue;
		}
		// System.out.println("ERXAbstractRestResponseWriter.cascadingValue: == " + propertyValue);
		return propertyValue;
	}

	public static String _cascadingValue(ERXRestKey cascadingKey, String propertyPrefix, String propertySuffix) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		String propertyValue = null;
		EOEntity entity = cascadingKey.entity();
		while (entity != null && propertyValue == null) {
			ERXRestKey entityCascadingKey = cascadingKey.cloneKeyWithNewEntity(entity, true, true);
			// System.out.println("ERXAbstractRestResponseWriter._cascadingValue:   keys " + cascadingKey + "vs" +
			// entityCascadingKey);
			String keypathWithoutGIDs = entityCascadingKey.path(true);
			String propertyName = propertyPrefix + keypathWithoutGIDs.replace('/', '.') + propertySuffix;
			propertyValue = ERXProperties.stringForKey(propertyName);
			// System.out.println("ERXAbstractRestResponseWriter._cascadingValue:   checking " + entity + " + entity + "
			// + propertyName + "=>" + propertyValue);
			if (propertyValue == null) {
				entity = entity.parentEntity();
			}
		}
		if (propertyValue == null && cascadingKey.nextKey() != null) {
			propertyValue = _cascadingValue(cascadingKey.nextKey(), propertyPrefix, propertySuffix);
		}
		return propertyValue;
	}

	public static boolean _displayDetailsFromProperties(ERXRestKey result) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		boolean displayDetails;
		String displayDetailsStr = cascadingValue(result, IERXRestResponseWriter.REST_PREFIX, IERXRestResponseWriter.DETAILS_PREFIX, null);
		if (displayDetailsStr == null) {
			displayDetails = result.previousKey() == null && (result.key() == null || result.isKeyGID());
		}
		else {
			displayDetails = Boolean.valueOf(displayDetailsStr).booleanValue();
		}
		return displayDetails;
	}

	public static String[] _displayPropertiesFromProperties(ERXRestKey result, boolean displayAllProperties, boolean displayAllToMany) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		String[] displayPropertyNames;
		String displayPropertyNamesStr = cascadingValue(result, IERXRestResponseWriter.REST_PREFIX, IERXRestResponseWriter.DETAILS_PROPERTIES_PREFIX, null);
		if (displayPropertyNamesStr == null) {
			if (displayAllProperties) {
				NSArray allPropertyNames = ERXUnsafeRestEntityDelegate.allPropertyNames(result.nextEntity(), displayAllToMany);
				displayPropertyNames = new String[allPropertyNames.count()];
				for (int propertyNum = 0; propertyNum < displayPropertyNames.length; propertyNum++) {
					displayPropertyNames[propertyNum] = (String) allPropertyNames.objectAtIndex(propertyNum);
				}
			}
			else {
				displayPropertyNames = null;
			}
		}
		else {
			displayPropertyNames = displayPropertyNamesStr.split(",");
		}
		return displayPropertyNames;
	}
	
	public String[] displayProperties(ERXRestKey key, boolean allProperties, boolean allToMany, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		String[] displayProperties = _displayPropertiesFromProperties(key, allProperties, allToMany);
		return displayProperties;
	}


	public boolean displayDetails(ERXRestKey key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		return _displayDetailsFromProperties(key);
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
