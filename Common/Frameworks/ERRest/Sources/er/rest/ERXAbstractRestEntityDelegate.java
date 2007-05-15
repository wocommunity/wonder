package er.rest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Enumeration;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOKeyValueCoding;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.extensions.ERXEOGlobalIDUtilities;
import er.extensions.ERXGuardedObjectInterface;

public abstract class ERXAbstractRestEntityDelegate implements IERXRestEntityDelegate {
	public Object valueForKey(EOEntity entity, Object obj, String propertyName, ERXRestContext context) {
		return NSKeyValueCoding.Utility.valueForKey(obj, propertyName);
	}

	public void takeValueForKey(EOEntity entity, Object obj, String propertyName, String value, ERXRestContext context) throws ParseException, ERXRestException {
		Object parsedAttributeValue = parseAttributeValue(entity, obj, propertyName, value);
		EOKeyValueCoding.Utility.takeStoredValueForKey(obj, parsedAttributeValue, propertyName);
	}

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
		EOKeyGlobalID gid = EOKeyGlobalID.globalIDWithEntityName(entity.name(), new Object[] { Integer.valueOf(key) });
		EOEnterpriseObject obj = ERXEOGlobalIDUtilities.fetchObjectWithGlobalID(context.editingContext(), gid);
		if (obj == null) {
			throw new ERXRestNotFoundException("There is no " + entity.name() + " with the id '" + key + "'.");
		}
		if (!canViewObject(entity, obj, context)) {
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
		if (!canViewObject(entity, obj, context)) {
			throw new ERXRestSecurityException("You are not allowed to view the " + entity.name() + " with the id '" + key + "'.");
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
	
	public EOEnterpriseObject insertObjectFromDocument(EOEntity entity, Element insertElement, EOEnterpriseObject parentObject, String parentKey, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
		EOEnterpriseObject eo = EOUtilities.createAndInsertInstance(context.editingContext(), entity.name());
		updateObjectFromDocument(entity, eo, insertElement, context);
		if (parentObject != null) {
			parentObject.addObjectToBothSidesOfRelationshipWithKey(eo, parentKey);
		}
		inserted(entity, eo, context);
		return eo;
	}

	public void updateObjectFromDocument(EOEntity entity, EOEnterpriseObject eo, Element eoElement, ERXRestContext context) throws ERXRestSecurityException, ERXRestException, ERXRestNotFoundException {
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
			if (!canUpdateProperty(entity, eo, attributeName, context)) {
				throw new ERXRestSecurityException("You are not allowed to update '" + attributeName + "' on " + entity.name() + ".");
			}

			EORelationship relationship = entity.relationshipNamed(attributeName);
			if (relationship != null) {
				EOEntity destinationEntity = relationship.destinationEntity();
				if (!relationship.isToMany()) {
					EOEnterpriseObject originalObject = (EOEnterpriseObject) valueForKey(entity, eo, attributeName, context);
					Node idNode = attributeNode.getAttributes().getNamedItem("id");
					if (idNode == null) {
						eo.removeObjectFromBothSidesOfRelationshipWithKey(originalObject, attributeName);
					}
					else {
						String id = idNode.getNodeValue();
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
					NodeList toManyNodes = attributeNode.getChildNodes();
					updateArrayFromDocument(entity, eo, attributeName, destinationEntity, currentObjects, toManyNodes, context);
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
					takeValueForKey(entity, eo, attributeName, attributeValue, context);
				}
				catch (ParseException e) {
					throw new ERXRestException("Failed to parse attribute value '" + attributeValue + "'.", e);
				}
			}
		}
		
		updated(entity, eo, context);
	}

	public void updateArrayFromDocument(EOEntity parentEntity, EOEnterpriseObject parentObject, String attributeName, EOEntity entity, NSArray currentObjects, NodeList toManyNodes, ERXRestContext context) throws ERXRestException, ERXRestNotFoundException, ERXRestSecurityException {
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

	public abstract void updated(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

	public abstract void inserted(EOEntity entity, EOEnterpriseObject eo, ERXRestContext context) throws ERXRestException, ERXRestSecurityException;

}
