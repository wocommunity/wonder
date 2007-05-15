package er.rest;

import java.text.ParseException;
import java.util.Enumeration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.ERXEOGlobalIDUtilities;
import er.extensions.ERXLocalizer;

public class ERXDefaultRestDelegate implements IERXRestDelegate {
	private NSMutableDictionary _entityDelegates;
	private IERXRestEntityDelegate _defaultDelegate;

	public ERXDefaultRestDelegate() {
		this(new ERXDenyRestEntityDelegate());
	}

	public ERXDefaultRestDelegate(IERXRestEntityDelegate defaultDelegate) {
		_entityDelegates = new NSMutableDictionary();
		_defaultDelegate = defaultDelegate;
	}

	public EOEnterpriseObject objectWithKey(EOEntity entity, String key, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		EOKeyGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(entity.name(), new Object[] { Integer.valueOf(key) });
		EOEnterpriseObject obj = ERXEOGlobalIDUtilities.fetchObjectWithGlobalID(context.editingContext(), gid);
		if (obj == null) {
			throw new ERXRestNotFoundException("There is no " + entity.name() + " with the id '" + key + "'.");
		}
		if (!entityDelegate(entity).canViewObject(entity, obj, context)) {
			throw new ERXRestSecurityException("You are not allowed to view the " + entity.name() + " with the id '" + key + "'.");
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
			throw new ERXRestNotFoundException("There is no " + entity.name() + " in this relationship with the id '" + key + "'.");
		}
		EOEnterpriseObject obj = (EOEnterpriseObject) objs.objectAtIndex(0);
		if (!entityDelegate(entity).canViewObject(entity, obj, context)) {
			throw new ERXRestSecurityException("You are not allowed to view the " + entity.name() + " with the id '" + key + "'.");
		}
		return obj;
	}

	public ERXRestKey insert(ERXRestKey lastKey, Document insertDocument, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		ERXRestKey insertResult;

		EOEntity lastEntity = lastKey.entity();
		if (lastKey.isKeyAll()) {
			insertResult = insertInto(lastEntity, insertDocument, null, null, null, context);
		}
		else if (lastKey.isKeyGID()) {
			throw new ERXRestException("You can't insert an object with a specific id.");
		}
		else {
			ERXRestKey previousKey = lastKey.previousKey();
			Object nextToLastValue = previousKey.value();
			if (nextToLastValue instanceof EOEnterpriseObject) {
				EOEnterpriseObject nextToLastEO = (EOEnterpriseObject) nextToLastValue;
				insertResult = insertInto(lastEntity, insertDocument, previousKey.entity(), nextToLastEO, lastKey.key(), context);
			}
			else {
				throw new ERXRestException("You attempted to insert something that could not be processed.");
			}
		}

		return insertResult;
	}

	protected ERXRestKey insertInto(EOEntity entity, Document insertDocument, EOEntity parentEntity, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		ERXRestKey insertResult;
		Element insertDocumentElement = insertDocument.getDocumentElement();
		String entityName = entity.name();
		String pluralEntityName = ERXLocalizer.currentLocalizer().plurifiedString(entityName, 2);

		String nodeName = insertDocumentElement.getNodeName();
		if (entityName.equals(nodeName)) {
			EOEnterpriseObject eo = insert(entity, insertDocumentElement, parentEntity, parentObject, parentKey, context);
			insertResult = new ERXRestKey(context, entity, "inserted", eo);
		}
		else if (pluralEntityName.equals(nodeName)) {
			NSMutableArray eos = new NSMutableArray();
			NodeList insertElements = insertDocumentElement.getChildNodes();
			for (int nodeNum = 0; nodeNum < insertElements.getLength(); nodeNum++) {
				Element insertElement = (Element) insertElements.item(nodeNum);
				EOEnterpriseObject eo = insert(entity, insertElement, parentEntity, parentObject, parentKey, context);
				eos.addObject(eo);
			}
			insertResult = new ERXRestKey(context, entity, "inserted", eos);
		}
		else {
			throw new ERXRestException("You attempted to put a " + nodeName + " into a " + entity.name() + ".");
		}
		return insertResult;
	}

	protected EOEnterpriseObject insert(EOEntity entity, Element insertElement, EOEntity parentEntity, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		boolean canInsert;
		if (parentObject == null) {
			canInsert = entityDelegate(entity).canInsertObject(entity, context);
		}
		else {
			canInsert = entityDelegate(entity).canInsertObject(parentEntity, parentObject, parentKey, entity, context);
		}
		if (!canInsert) {
			throw new ERXRestSecurityException("You are not allowed to insert this object.");
		}
		EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(context.editingContext(), entity.name());
		updateObjectFromElement(entity, eo, insertElement, context);
		if (parentObject != null) {
			parentObject.addObjectToBothSidesOfRelationshipWithKey(eo, parentKey);
		}
		entityDelegate(entity).updated(entity, eo, context);

		return eo;
	}

	public void update(ERXRestKey lastKey, Document updateDocument, ERXRestContext context) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException {
		if (lastKey.isKeyAll()) {
			throw new ERXRestSecurityException("You are not allowed to update all " + lastKey.entity().name() + " objects.");
		}

		EOEntity lastEntity = lastKey.entity();
		Object lastValue = lastKey.value();
		if (lastValue instanceof EOEnterpriseObject) {
			EOEnterpriseObject eo = (EOEnterpriseObject) lastValue;

			if (!entityDelegate(lastEntity).canUpdateObject(lastEntity, eo, context)) {
				throw new ERXRestSecurityException("You are not allowed to update this object.");
			}

			Element updateElement = updateDocument.getDocumentElement();
			updateObjectFromElement(lastEntity, eo, updateElement, context);
			entityDelegate(lastEntity).updated(lastEntity, eo, context);
		}
		else if (lastValue instanceof NSArray) {
			ERXRestKey nextToLastKey = lastKey.previousKey();
			Object nextToLastValue = nextToLastKey.value();
			if (nextToLastValue instanceof EOEnterpriseObject) {
				EOEntity previousEntity = nextToLastKey.entity();
				EOEnterpriseObject eo = (EOEnterpriseObject) nextToLastValue;
				if (!entityDelegate(previousEntity).canUpdateObject(previousEntity, eo, context)) {
					throw new ERXRestSecurityException("You are not allowed to update this object.");
				}

				NSArray currentObjects = (NSArray) lastValue;
				Element arrayElement = updateDocument.getDocumentElement();
				String arrayNodeName = arrayElement.getNodeName();
				String pluralToManyEntityName = ERXLocalizer.currentLocalizer().plurifiedString(lastEntity.name(), 2);
				if (!pluralToManyEntityName.equals(arrayNodeName)) {
					throw new ERXRestException("You attempted to put " + arrayNodeName + " into " + pluralToManyEntityName + ".");
				}

				NodeList toManyNodes = arrayElement.getChildNodes();
				updateArray(eo, nextToLastKey.key(), lastEntity, currentObjects, toManyNodes, context);

				entityDelegate(lastEntity).updated(lastEntity, eo, context);
			}
			else {
				throw new ERXRestException("You attempted to put an array into something other than a relationship of a single object.");
			}
		}
		else {
			throw new ERXRestException("You attempted to update something that could not be processed.");
		}
	}

	protected void updateObjectFromElement(EOEntity entity, EOEnterpriseObject eo, Element eoElement, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		if (!entity.name().equals(eoElement.getNodeName())) {
			throw new ERXRestException("You attempted to put a " + eoElement.getNodeName() + " into a " + entity.name() + ".");
		}

		NodeList attributeNodes = eoElement.getChildNodes();
		for (int attributeNum = 0; attributeNum < attributeNodes.getLength(); attributeNum++) {
			Node attributeNode = attributeNodes.item(attributeNum);
			if (attributeNode instanceof Text) {
				continue;
			}

			String attributeName = attributeNode.getNodeName();
			if (!entityDelegate(entity).canUpdateProperty(entity, eo, attributeName, context)) {
				throw new ERXRestSecurityException("You are not allowed to update '" + attributeName + "' on " + entity.name() + ".");
			}

			EORelationship relationship = entity.relationshipNamed(attributeName);
			if (relationship != null) {
				EOEntity destinationEntity = relationship.destinationEntity();
				if (!relationship.isToMany()) {
					EOEnterpriseObject originalObject = (EOEnterpriseObject) entityDelegate(entity).valueForKey(entity, eo, attributeName, context);
					Node idNode = attributeNode.getAttributes().getNamedItem("id");
					if (idNode == null) {
						eo.removeObjectFromBothSidesOfRelationshipWithKey(originalObject, attributeName);
					}
					else {
						String id = idNode.getNodeValue();
						EOEnterpriseObject newObject = objectWithKey(destinationEntity, id, context);
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
					NSArray currentObjects = (NSArray) entityDelegate(entity).valueForKey(entity, eo, attributeName, context);
					NodeList toManyNodes = attributeNode.getChildNodes();
					updateArray(eo, attributeName, destinationEntity, currentObjects, toManyNodes, context);
				}
			}
			else {
				NodeList attributeChildNodes = attributeNode.getChildNodes();
				String attributeValue;
				if (attributeChildNodes.getLength() == 0) {
					attributeValue = null;
				}
				else {
					attributeValue = attributeChildNodes.item(0).getNodeValue();
				}
				try {
					entityDelegate(entity).takeValueForKey(entity, eo, attributeName, attributeValue, context);
				}
				catch (ParseException e) {
					throw new ERXRestException("Failed to parse attribute value '" + attributeValue + "'.", e);
				}
			}
		}
	}

	protected void updateArray(EOEnterpriseObject eo, String attributeName, EOEntity entity, NSArray currentObjects, NodeList toManyNodes, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
		NSMutableArray keepObjects = new NSMutableArray();
		NSMutableArray addObjects = new NSMutableArray();
		NSMutableArray removeObjects = new NSMutableArray();

		for (int toManyNum = 0; toManyNum < toManyNodes.getLength(); toManyNum++) {
			Node toManyNode = toManyNodes.item(toManyNum);
			String toManyNodeName = toManyNode.getNodeName();
			if (!entity.name().equals(toManyNodeName)) {
				throw new ERXRestException("You attempted to put a " + toManyNodeName + " into a " + entity.name() + ".");
			}

			String id = toManyNode.getAttributes().getNamedItem("id").getNodeValue();
			Object relatedObject = objectWithKey(entity, id, context);
			if (currentObjects.containsObject(relatedObject)) {
				System.out.println("AbstractERXRestDelegate.updateArray: keeping " + relatedObject + " in " + eo + " (" + attributeName + ")");
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
				System.out.println("AbstractERXRestDelegate.updateArray: removing " + currentObject + " from " + eo + " (" + attributeName + ")");
				eo.removeObjectFromBothSidesOfRelationshipWithKey(currentObject, attributeName);
			}
		}

		Enumeration addObjectsEnum = addObjects.objectEnumerator();
		while (addObjectsEnum.hasMoreElements()) {
			EOEnterpriseObject addObject = (EOEnterpriseObject) addObjectsEnum.nextElement();
			System.out.println("AbstractERXRestDelegate.updateArray: adding " + addObject + " to " + eo + " (" + attributeName + ")");
			eo.addObjectToBothSidesOfRelationshipWithKey(addObject, attributeName);
		}
	}

	public void preprocess(EOEntity entity, NSArray objects, ERXRestContext context) {
		// Enumeration displayPropertiesEnum = displayProperties(entity).objectEnumerator();
		// while (displayPropertiesEnum.hasMoreElements()) {
		// EOProperty displayProperty = (EOProperty) displayPropertiesEnum.nextElement();
		// if (displayProperty instanceof EORelationship) {
		// EORelationship displayRelationship = (EORelationship) displayProperty;
		// ERXRecursiveBatchFetching.batchFetch(objects, displayRelationship.name());
		// }
		// }
	}

	public void delete(EOEntity entity, Object obj, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		if (obj instanceof NSArray) {
			NSArray values = (NSArray) obj;
			Enumeration valuesEnum = values.objectEnumerator();
			while (valuesEnum.hasMoreElements()) {
				EOEnterpriseObject eo = (EOEnterpriseObject) valuesEnum.nextElement();
				delete(entity, eo, context);
			}
		}
		else {
			delete(entity, obj, context);
		}
	}

	protected void _delete(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		if (!entityDelegate(entity).canDeleteObject(entity, eo, context)) {
			throw new ERXRestSecurityException("You are not allowed to delete the given obj.");
		}
		else {
			entityDelegate(entity).delete(entity, eo, context);
		}
	}

	public void addDelegateForEntityNamed(IERXRestEntityDelegate entityDelegate, String entityName) {
		_entityDelegates.setObjectForKey(entityDelegate, entityName);
	}

	public void removeDelegateForEntityNamed(String entityName) {
		_entityDelegates.removeObjectForKey(entityName);
	}

	public IERXRestEntityDelegate entityDelegate(EOEntity entity) {
		IERXRestEntityDelegate entityDelegate = (IERXRestEntityDelegate) _entityDelegates.objectForKey(entity.name());
		if (entityDelegate == null) {
			entityDelegate = _defaultDelegate;
		}
		return entityDelegate;
	}

	public NSArray objectsForEntity(EOEntity entity, ERXRestContext context) throws ERXRestException, ERXRestSecurityException {
		return entityDelegate(entity).objectsForEntity(entity, context);
	}
}