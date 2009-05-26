package er.rest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.localization.ERXLocalizer;
import er.rest.format.ERXRestFormat;
import er.rest.format.ERXWORestResponse;
import er.rest.format.IERXRestWriter;

/**
 * ERXRestRequestNode provides a model of a REST request. Because the incoming document format can vary (XML, JSON,
 * etc), we needed a document model that is more abstract than just an org.w3c.dom. Or, rather, one that isn't obnoxious
 * to use.
 * 
 * @author mschrag
 */
public class ERXRestRequestNode implements NSKeyValueCoding {
	private boolean _array;
	private String _name;
	private Object _value;
	private NSMutableDictionary<String, String> _attributes;
	private NSMutableArray<ERXRestRequestNode> _children;
	private Object _associatedObject;

	private Object _id;
	private String _type;
	private boolean _null;

	/**
	 * Construct a node with the given name
	 * 
	 * @param name
	 *            the name of this node
	 */
	public ERXRestRequestNode(String name) {
		_name = name;
		_attributes = new NSMutableDictionary<String, String>();
		_children = new NSMutableArray<ERXRestRequestNode>();
		guessNull();
	}

	/**
	 * Construct a node with the given name and value.
	 * 
	 * @param name
	 *            the name of this node
	 * @param value
	 *            the value of this node
	 */
	public ERXRestRequestNode(String name, Object value) {
		this(name);
		_value = value;
		guessNull();
	}

	public ERXRestRequestNode cloneNode() {
		ERXRestRequestNode cloneNode = new ERXRestRequestNode(_name);
		cloneNode._attributes.addEntriesFromDictionary(_attributes);
		cloneNode._children.addObjectsFromArray(_children);
		cloneNode._value = _value;
		cloneNode._associatedObject = _associatedObject;
		cloneNode._array = _array;
		cloneNode._type = _type;
		cloneNode._id = _id;
		cloneNode._null = _null;
		return cloneNode;
	}

	/**
	 * Returns the Java object that corresponds to this node hierarchy.
	 * 
	 * @return the Java object that corresponds to this node hierarchy
	 */
	public Object toJavaCollection(ERXRestFormat.Delegate delegate) {
		return toJavaCollection(delegate, new HashMap<Object, Object>());
	}

	/**
	 * Returns the Java object that corresponds to this node hierarchy.
	 * 
	 * @param the
	 *            associatedObjects map (to prevent infinite loops)
	 * @return the Java object that corresponds to this node hierarchy
	 */
	protected Object toJavaCollection(ERXRestFormat.Delegate delegate, Map<Object, Object> associatedObjects) {
		Object result = associatedObjects.get(_associatedObject);
		if (result == null) {
			delegate.nodeWillWrite(this);

			if (isArray()) {
				List<Object> array = new LinkedList<Object>();
				for (ERXRestRequestNode child : _children) {
					array.add(child.toJavaCollection(delegate, associatedObjects));
				}
				result = array;
			}
			else if (isNull()) {
				result = null;
			}
			else if (_value != null) {
				result = _value;
			}
			else {
				Map<Object, Object> dict = new HashMap<Object, Object>();
				for (Map.Entry<String, String> attribute : _attributes.entrySet()) {
					String key = attribute.getKey();
					String value = attribute.getValue();
					// if (value != null) {
					dict.put(key, value);
					// }
				}
				for (ERXRestRequestNode child : _children) {
					String name = child.name();
					Object value = child.toJavaCollection(delegate, associatedObjects);
					// if (value != null) {
					dict.put(name, value);
					// }
				}
				if (dict.isEmpty()) {
					result = null;
				}
				else {
					result = dict;
				}
			}

			if (_associatedObject != null) {
				associatedObjects.put(_associatedObject, result);
			}
		}
		return result;
	}

	/**
	 * Returns the NSCollection/Java object that corresponds to this node hierarchy.
	 * 
	 * @return the NSCollection/Java object that corresponds to this node hierarchy
	 */
	public Object toNSCollection(ERXRestFormat.Delegate delegate) {
		return toNSCollection(delegate, new NSMutableDictionary<Object, Object>());
	}

	/**
	 * Returns the NSCollection/Java object that corresponds to this node hierarchy.
	 * 
	 * @param the
	 *            associatedObjects map (to prevent infinite loops)
	 * @return the NSCollection/Java object that corresponds to this node hierarchy
	 */
	protected Object toNSCollection(ERXRestFormat.Delegate delegate, NSMutableDictionary<Object, Object> associatedObjects) {
		Object result = associatedObjects.get(_associatedObject);
		if (result == null) {
			delegate.nodeWillWrite(this);

			if (isArray()) {
				NSMutableArray<Object> array = new NSMutableArray<Object>();
				for (ERXRestRequestNode child : _children) {
					array.add(child.toNSCollection(delegate, associatedObjects));
				}
				result = array;
			}
			else if (isNull()) {
				result = NSKeyValueCoding.NullValue;
			}
			else if (_value != null) {
				result = _value;
			}
			else {
				NSMutableDictionary<Object, Object> dict = new NSMutableDictionary<Object, Object>();
				for (Map.Entry<String, String> attribute : _attributes.entrySet()) {
					String key = attribute.getKey();
					Object value = attribute.getValue();
					if (value == null) {
						value = NSKeyValueCoding.NullValue;
					}
					// if (value != null) {
					dict.put(key, value);
					// }
				}
				for (ERXRestRequestNode child : _children) {
					String name = child.name();
					Object value = child.toNSCollection(delegate, associatedObjects);
					// if (value != null) {
					dict.put(name, value);
					// }
				}
				if (dict.isEmpty()) {
					result = NSKeyValueCoding.NullValue;
				}
				else {
					result = dict;
				}
			}

			if (_associatedObject != null) {
				associatedObjects.put(_associatedObject, result);
			}
		}
		return result;
	}

	/**
	 * Sets whether or not this node represents an array or to-many relationship.
	 * 
	 * @param array
	 *            whether or not this node represents an array or to-many relationship
	 */
	public void setArray(boolean array) {
		_array = array;
		guessNull();
	}

	/**
	 * Return whether or not this node represents an array or to-many relationship.
	 * 
	 * @return whether or not this node represents an array or to-many relationship
	 */
	public boolean isArray() {
		return _array;
	}

	/**
	 * Sets the original object associated with this node.
	 * 
	 * @param associatedObject
	 *            the original object associated with this node
	 */
	public void setAssociatedObject(Object associatedObject) {
		_associatedObject = associatedObject;
	}

	/**
	 * Returns the original object associated with this node.
	 * 
	 * @return the original object associated with this node
	 */
	public Object associatedObject() {
		return _associatedObject;
	}

	public void takeValueForKey(Object value, String key) {
		if (_attributes.containsKey(key)) {
			_attributes.setObjectForKey((String) value, key);
		}
		else {
			ERXRestRequestNode child = childNamed(key);
			if (child == null) {
				throw new NSKeyValueCoding.UnknownKeyException("There is no key named '" + key + "' on this node.", this, key);
			}
			else if (child.children().size() == 0) {
				child.setValue(value);
			}
			else {
				throw new IllegalArgumentException("Unable to set the value of '" + key + "' to " + value + ".");
			}
		}
	}

	public Object valueForKey(String key) {
		Object value;
		if (_attributes.containsKey(key)) {
			value = _attributes.objectForKey(key);
		}
		else {
			ERXRestRequestNode child = childNamed(key);
			if (child == null) {
				throw new NSKeyValueCoding.UnknownKeyException("There is no key named '" + key + "' on this node.", this, key);
			}
			else if (child.children().size() == 0) {
				value = child.value();
			}
			else {
				value = child;
			}
		}
		return value;
	}

	/**
	 * Returns the first child named 'name'.
	 * 
	 * @param name
	 *            the name to look for
	 * @return the first child with this name (or null if not found)
	 */
	public ERXRestRequestNode childNamed(String name) {
		ERXRestRequestNode matchingChildNode = null;
		Enumeration childrenEnum = _children.objectEnumerator();
		while (matchingChildNode == null && childrenEnum.hasMoreElements()) {
			ERXRestRequestNode childNode = (ERXRestRequestNode) childrenEnum.nextElement();
			if (name.equals(childNode.name())) {
				matchingChildNode = childNode;
			}
		}
		return matchingChildNode;
	}

	public ERXRestRequestNode removeChildNamed(String name) {
		ERXRestRequestNode node = childNamed(name);
		if (node != null) {
			_children.remove(node);
		}
		return node;
	}

	/**
	 * Sets the type of this node (type as in the Class that it represents).
	 * 
	 * @param type
	 *            the type of this node
	 */
	public void setType(String type) {
		_type = type;
	}

	public String type() {
		return _type;
	}

	public void setID(Object id) {
		_id = id;
		guessNull();
	}

	public Object id() {
		return _id;
	}

	public Object removeAttributeOrChildNodeNamed(String name) {
		Object value = removeAttributeForKey(name);
		if (value == null) {
			ERXRestRequestNode childNode = removeChildNamed(name);
			if (childNode != null) {
				value = childNode.value();
			}
		}
		return value;
	}

	/**
	 * Returns the type of this node.
	 * 
	 * @return the type of this node
	 */
	public Object attributeOrChildNodeValue(String name) {
		Object value = attributeForKey(name);
		if (value == null) {
			ERXRestRequestNode typeNode = childNamed(name);
			if (typeNode != null) {
				value = typeNode.value();
			}
		}
		return value;
	}

	protected void guessNull() {
		setNull(_value == null && _children.size() == 0 && _id == null && !isArray() && _associatedObject == null);
	}

	/**
	 * Sets whether or not this node represents a null value.
	 * 
	 * @param isNull
	 *            whether or not this node represents a null value
	 */
	public void setNull(boolean isNull) {
		_null = isNull;
	}

	/**
	 * Returns whether or not this node represents a null value.
	 * 
	 * @return true whether or not this node represents a null value
	 */
	public boolean isNull() {
		return _null;
	}

	/**
	 * Returns the name of this node.
	 * 
	 * @return the name of this node
	 */
	public String name() {
		return _name;
	}

	/**
	 * Returns the value for this node (or null if it doesn't exist).
	 * 
	 * @return the name of this node
	 */
	public Object value() {
		return _value;
	}

	/**
	 * Sets the value for this node.
	 * 
	 * @param value
	 *            the value for this node
	 */
	public void setValue(Object value) {
		_value = value;
		guessNull();
	}

	/**
	 * Sets the attribute value for the given key.
	 * 
	 * @param attribute
	 *            the attribute value
	 * @param key
	 *            the key
	 */
	public void setAttributeForKey(String attribute, String key) {
		_attributes.setObjectForKey(attribute, key);
		// if (!"nil".equals(key)) {
		guessNull();
		// }
	}

	public String removeAttributeForKey(String key) {
		String attribute = _attributes.removeObjectForKey(key);
		return attribute;
	}

	/**
	 * Returns the attribute value for the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the attribute value
	 */
	public String attributeForKey(String key) {
		return _attributes.objectForKey(key);
	}

	/**
	 * Returns the attributes dictionary for this node.
	 * 
	 * @return the attributes dictionary
	 */
	public NSDictionary<String, String> attributes() {
		return _attributes;
	}

	/**
	 * Adds a child to this node.
	 * 
	 * @param child
	 *            the child to add
	 */
	public void addChild(ERXRestRequestNode child) {
		_children.addObject(child);
		guessNull();
	}

	/**
	 * Returns the children of this node.
	 * 
	 * @return the children of this node
	 */
	public NSArray<ERXRestRequestNode> children() {
		return _children;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb, 0);
		return sb.toString();
	}

	protected void toString(StringBuffer sb, int depth) {
		for (int i = 0; i < depth; i++) {
			sb.append("  ");
		}
		sb.append("[");
		sb.append(_name);
		if (_id != null || _type != null) {
			if (_id != null) {
				sb.append(" id=" + _id);
			}
			if (_type != null) {
				sb.append(" type=" + _type);
			}
		}
		if (!_attributes.isEmpty()) {
			sb.append(" ");
			sb.append(_attributes);
		}
		if (_value != null) {
			sb.append("=");
			sb.append(_value);
		}
		if (!_children.isEmpty()) {
			sb.append("\n");
			for (ERXRestRequestNode child : _children) {
				child.toString(sb, depth + 1);
			}
			for (int i = 0; i < depth; i++) {
				sb.append("  ");
			}
		}
		sb.append("]");
		sb.append("\n");
	}

	/**
	 * Returns the object that this request node represents.
	 * 
	 * @param entityName
	 *            the entity name of the object to use
	 * @param keyFilter
	 *            the filter to use for determining which keys can be updated (or null for no update)
	 * @param delegate
	 *            the delegate to use
	 * @return the object that this request node represents
	 */
	public Object objectWithFilter(String entityName, ERXKeyFilter keyFilter, IERXRestDelegate delegate) {
		Object obj = delegate.objectOfEntityNamedWithID(entityName, id());
		if (keyFilter != null) {
			updateObjectWithFilter(obj, keyFilter, delegate);
		}
		return obj;
	}

	/**
	 * Creates a new instance of an object represented by this request node.
	 * 
	 * @param entityName
	 *            the entity name of the object to use
	 * @param keyFilter
	 *            the filter to use for determining which keys can be updated (or null for no update)
	 * @param delegate
	 *            the delegate to use
	 * @return a new instance of an object represented by this request node
	 */
	public Object createObjectWithFilter(String entityName, ERXKeyFilter keyFilter, IERXRestDelegate delegate) {
		Object obj = delegate.createObjectOfEntityNamed(entityName);
		if (keyFilter != null) {
			updateObjectWithFilter(obj, keyFilter, delegate);
		}
		return obj;
	}

	protected void _addAttributeNodeForKeyInObject(ERXKey<?> key, Object obj, ERXKeyFilter keyFilter) {
		ERXRestRequestNode attributeNode = new ERXRestRequestNode(keyFilter.keyMap(key).key());
		attributeNode.setValue(key.valueInObject(obj));
		addChild(attributeNode);
	}

	protected void _addToManyRelationshipNodeForKeyOfEntityInObject(ERXKey<?> key, EOClassDescription destinationEntity, Object obj, ERXKeyFilter keyFilter, IERXRestDelegate delegate, Set<Object> visitedObjects) {
		ERXRestRequestNode toManyRelationshipNode = new ERXRestRequestNode(keyFilter.keyMap(key).key());
		toManyRelationshipNode.setArray(true);
		toManyRelationshipNode.setType(destinationEntity.entityName());

		List childrenObjects = (List) key.valueInObject(obj);
		ERXKeyFilter childFilter = keyFilter._filterForKey(key);
		for (Object childObj : childrenObjects) {
			ERXRestRequestNode childNode = new ERXRestRequestNode(null);
			childNode._fillInWithObjectAndFilter(childObj, destinationEntity, childFilter, delegate, visitedObjects);
			toManyRelationshipNode.addChild(childNode);
		}

		addChild(toManyRelationshipNode);

	}

	protected void _addToOneRelationshipNodeForKeyInObject(ERXKey<?> key, Object obj, EOClassDescription destinationEntity, ERXKeyFilter keyFilter, IERXRestDelegate delegate, Set<Object> visitedObjects) {
		Object value = key.valueInObject(obj);
		//if (value != null) {
			ERXRestRequestNode toOneRelationshipNode = new ERXRestRequestNode(keyFilter.keyMap(key).key());
			toOneRelationshipNode._fillInWithObjectAndFilter(value, destinationEntity, keyFilter._filterForKey(key), delegate, visitedObjects);
			addChild(toOneRelationshipNode);
		//}
	}

	@SuppressWarnings("unchecked")
	protected void _addAttributesAndRelationshipsForObjectOfEntity(Object obj, EOClassDescription classDescription, ERXKeyFilter keyFilter, IERXRestDelegate delegate, Set<Object> visitedObjects) {
		Set<ERXKey> visitedKeys = new HashSet<ERXKey>();
		for (String attributeName : (NSArray<String>) classDescription.attributeKeys()) {
			// if (attribute.isClassProperty()) {
			ERXKey<Object> key = new ERXKey<Object>(attributeName);
			if (keyFilter.matches(key, ERXKey.Type.Attribute)) {
				_addAttributeNodeForKeyInObject(key, obj, keyFilter);
				visitedKeys.add(key);
			}
			// }
		}

		for (String relationshipName : (NSArray<String>) classDescription.toOneRelationshipKeys()) {
			// if (relationship.isClassProperty()) {
			ERXKey<Object> key = new ERXKey<Object>(relationshipName);
			if (keyFilter.matches(key, ERXKey.Type.ToOneRelationship)) {
				_addToOneRelationshipNodeForKeyInObject(key, obj, classDescription.classDescriptionForDestinationKey(relationshipName), keyFilter, delegate, visitedObjects);
				visitedKeys.add(key);
			}
			// }
		}

		for (String relationshipName : (NSArray<String>) classDescription.toManyRelationshipKeys()) {
			// if (relationship.isClassProperty()) {
			ERXKey<Object> key = new ERXKey<Object>(relationshipName);
			if (keyFilter.matches(key, ERXKey.Type.ToManyRelationship)) {
				_addToManyRelationshipNodeForKeyOfEntityInObject(key, classDescription.classDescriptionForDestinationKey(relationshipName), obj, keyFilter, delegate, visitedObjects);
				visitedKeys.add(key);
			}
			// }
		}

		Set<ERXKey> includeKeys = keyFilter.includes().keySet();
		if (includeKeys != null && !includeKeys.isEmpty()) {
			Set<ERXKey> remainingKeys = new HashSet<ERXKey>(includeKeys);
			remainingKeys.removeAll(visitedKeys);
			if (!remainingKeys.isEmpty()) {
				// this is sort of expensive, but we want to support non-eomodel to-many relationships on EO's, so
				// we fallback and lookup the class entity ...
				if (classDescription instanceof EOEntityClassDescription) {
					// EOEntityClassDescription.classDescriptionForEntityName(obj.getClass().getName());
					EOClassDescription nonModelClassDescription = ERXRestClassDescriptionFactory.classDescriptionForClass(obj.getClass(), true);
					for (ERXKey<?> remainingKey : remainingKeys) {
						String keyName = remainingKey.key();
						if (nonModelClassDescription.attributeKeys().containsObject(keyName)) {
							_addAttributeNodeForKeyInObject(remainingKey, obj, keyFilter);
						}
						else {
							if (nonModelClassDescription.toManyRelationshipKeys().containsObject(keyName)) {
								_addToManyRelationshipNodeForKeyOfEntityInObject(remainingKey, nonModelClassDescription.classDescriptionForDestinationKey(keyName), obj, keyFilter, delegate, visitedObjects);
							}
							else if (nonModelClassDescription.toOneRelationshipKeys().containsObject(keyName)) {
								_addToOneRelationshipNodeForKeyInObject(remainingKey, obj, nonModelClassDescription.classDescriptionForDestinationKey(keyName), keyFilter, delegate, visitedObjects);
							}
							else {
								throw new IllegalArgumentException("This key filter specified that the key '" + keyName + "' should be included on '" + nonModelClassDescription.entityName() + "', but it does not exist.");
							}
						}
					}
				}
				else {
					throw new IllegalArgumentException("This key filter specified that the keys '" + remainingKeys + "' should be included on '" + classDescription.entityName() + "', but they do not exist.");
				}
			}
		}
	}

	protected void _fillInWithObjectAndFilter(Object obj, EOClassDescription classDescription, ERXKeyFilter keyFilter, IERXRestDelegate delegate, Set<Object> visitedObjects) {
		if (obj instanceof List) {
			setAssociatedObject(obj);
			// setAttributeForKey(/* ??? */, ERXRestRequestNode.TYPE_KEY);
			setArray(true);

			for (Object childObj : (List) obj) {
				ERXRestRequestNode childNode = new ERXRestRequestNode(null);
				childNode._fillInWithObjectAndFilter(childObj, classDescription, keyFilter, delegate, visitedObjects);
				addChild(childNode);
			}
		}
		else {
			// in case we have a superclass class description passed in
			if (obj != null) {
				classDescription = ERXRestClassDescriptionFactory.classDescriptionForObject(obj);
			}
			if (_name == null) {
				_name = classDescription.entityName();
			}
			setAssociatedObject(obj);
			setType(classDescription.entityName());
			if (obj != null) {
				Object id = delegate.primaryKeyForObject(obj);
				if (id != null) {
					setID(id);
				}
				if (!visitedObjects.contains(obj)) {
					visitedObjects.add(obj);
					_addAttributesAndRelationshipsForObjectOfEntity(obj, classDescription, keyFilter, delegate, visitedObjects);
				}
			}
		}
	}

	/**
	 * Returns a string representation of this request node using the given format.
	 * 
	 * @param format
	 *            the format to use
	 * @return a string representation of this request node using the given format
	 */
	public String toString(ERXRestFormat format) {
		return toString(format.writer(), format.delegate());
	}

	/**
	 * Returns a string representation of this request node using the given IERXRestWriter.
	 * 
	 * @param writer
	 *            the writer to use
	 * @return a string representation of this request node using the given IERXRestWriter
	 */
	public String toString(IERXRestWriter writer, ERXRestFormat.Delegate delegate) {
		WOResponse octopusHair = new WOResponse();
		writer.appendToResponse(this, new ERXWORestResponse(octopusHair), delegate);
		return octopusHair.contentString();
	}

	protected boolean isClassProperty(EOClassDescription classDescription, String key) {
		boolean isClassProperty = true;
		// IERXAttribute attribute = entity.attributeNamed(key);
		// if (attribute != null) {
		// isClassProperty = attribute.isClassProperty();
		// }
		// else {
		// IERXRelationship relationship = entity.relationshipNamed(key);
		// if (relationship != null) {
		// isClassProperty = relationship.isClassProperty();
		// }
		// }
		return isClassProperty;
	}

	/**
	 * Updates the given object based on this request node.
	 * 
	 * @param obj
	 *            the object to update
	 * @param keyFilter
	 *            the filter to use to determine how to update
	 * @param delegate
	 *            the delegate
	 */
	public void updateObjectWithFilter(Object obj, ERXKeyFilter keyFilter, IERXRestDelegate delegate) {
		if (obj == null) {
			return;
		}

		EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForObject(obj);
		for (Map.Entry<String, String> attribute : _attributes.entrySet()) {
			ERXKey<Object> key = keyFilter.keyMap(new ERXKey<Object>(attribute.getKey()));
			String keyName = key.key();
			if (keyFilter.matches(key, ERXKey.Type.Attribute) && isClassProperty(classDescription, keyName)) {
				Object value = ERXRestUtils.coerceValueToAttributeType(attribute.getValue(), null, obj, keyName);
				if (value instanceof NSKeyValueCoding.Null) {
					value = null;
				}
				key.takeValueInObject(value, obj);
			}
		}

		for (ERXRestRequestNode childNode : _children) {
			ERXKey<Object> key = keyFilter.keyMap(new ERXKey<Object>(childNode.name()));
			String keyName = key.key();
			if (isClassProperty(classDescription, keyName)) {
				NSKeyValueCoding._KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(obj, keyName);
				Class valueType = binding.valueType();

				if (List.class.isAssignableFrom(valueType) && keyFilter.matches(key, ERXKey.Type.ToManyRelationship)) {
					EOClassDescription destinationEntity;
					// this is sort of expensive, but we want to support non-eomodel to-many relationships on EO's, so
					// we fallback and lookup the class entity ...
					if (!classDescription.toManyRelationshipKeys().containsObject(keyName) && classDescription instanceof EOEntityClassDescription) {
						EOClassDescription nonModelClassDescription = ERXRestClassDescriptionFactory.classDescriptionForClass(obj.getClass(), true);
						if (!nonModelClassDescription.toManyRelationshipKeys().containsObject(keyName)) {
							throw new IllegalArgumentException("There is no to-many relationship named '" + key.key() + "' on '" + classDescription.entityName() + "'.");
						}
						destinationEntity = classDescription.classDescriptionForDestinationKey(keyName);
					}
					else {
						destinationEntity = classDescription.classDescriptionForDestinationKey(keyName);
					}

					@SuppressWarnings("unchecked")
					List<Object> existingValues = (List<Object>) NSKeyValueCoding.DefaultImplementation.valueForKey(obj, keyName);

					Set<Object> removedValues = new HashSet<Object>(existingValues);
					List<Object> newValues = new LinkedList<Object>();
					List<Object> allValues = new LinkedList<Object>();
					for (ERXRestRequestNode toManyNode : childNode.children()) {
						Object childObj = delegate.objectOfEntityWithID(destinationEntity, toManyNode.id());
						toManyNode.updateObjectWithFilter(childObj, keyFilter._filterForKey(key), delegate);
						if (!existingValues.contains(childObj)) {
							newValues.add(childObj);
						}
						allValues.add(childObj);
						removedValues.remove(childObj);
					}

					if (obj instanceof EOEnterpriseObject) {
						for (Object removedValue : removedValues) {
							((EOEnterpriseObject) obj).removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject) removedValue, keyName);
						}
						for (Object newValue : newValues) {
							((EOEnterpriseObject) obj).addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject) newValue, keyName);
						}
					}
					else {
						key.takeValueInObject(allValues, obj);
					}
				}
				else if (!ERXRestUtils.isPrimitive(valueType) && keyFilter.matches(key, ERXKey.Type.ToOneRelationship)) {
					EOClassDescription destinationClassDescription;
					// this is sort of expensive, but we want to support non-eomodel to-one relationships on EO's, so
					// we fallback and lookup the class entity ...
					if (!classDescription.toOneRelationshipKeys().containsObject(keyName) && classDescription instanceof EOEntityClassDescription) {
						EOClassDescription nonModelClassDescription = ERXRestClassDescriptionFactory.classDescriptionForClass(obj.getClass(), true);
						if (!nonModelClassDescription.toManyRelationshipKeys().containsObject(keyName)) {
							throw new IllegalArgumentException("There is no to-one relationship named '" + key.key() + "' on '" + classDescription.entityName() + "'.");
						}
						destinationClassDescription = nonModelClassDescription.classDescriptionForDestinationKey(keyName);
					}
					else {
						destinationClassDescription = classDescription.classDescriptionForDestinationKey(keyName);
					}
					Object childObj = delegate.objectOfEntityWithID(destinationClassDescription, childNode.id());
					childNode.updateObjectWithFilter(childObj, keyFilter._filterForKey(key), delegate);
					if (obj instanceof EOEnterpriseObject && childObj instanceof EOEnterpriseObject) {
						((EOEnterpriseObject) obj).addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject) childObj, keyName);
					}
					else {
						key.takeValueInObject(childObj, obj);
					}
				}
				else if (/* entity.attributeNamed(keyName) != null && */ERXRestUtils.isPrimitive(valueType) && keyFilter.matches(key, ERXKey.Type.Attribute)) {
					Object value = childNode.value();
					if (value instanceof String) {
						value = ERXRestUtils.coerceValueToAttributeType(value, null, obj, keyName);
					}
					if (value instanceof NSKeyValueCoding.Null) {
						value = null;
					}
					key.takeValueInObject(value, obj);
				}
				else {
					// ignore key
				}
			}
		}
	}

	/**
	 * Creates a hierarchy of ERXRestRequestNodes based off of the given array of objects.
	 * 
	 * @param entity
	 *            the entity type of the objects in the array
	 * @param objects
	 *            the array to turn into request nodes
	 * @param keyFilter
	 *            the filter to use
	 * @return the root ERXRestRequestNode
	 */
	public static ERXRestRequestNode requestNodeWithObjectAndFilter(EOClassDescription classDescription, List<?> objects, ERXKeyFilter keyFilter, IERXRestDelegate delegate) {
		String entityName = classDescription.entityName();
		ERXRestRequestNode requestNode = new ERXRestRequestNode(ERXLocalizer.defaultLocalizer().plurifiedString(entityName, 2));
		requestNode.setType(entityName);
		requestNode._fillInWithObjectAndFilter(objects, classDescription, keyFilter, delegate, new HashSet<Object>());
		return requestNode;
	}

	/**
	 * Creates a hierarchy of ERXRestRequestNodes based off of the given object.
	 * 
	 * @param object
	 *            the object to turn into request nodes
	 * @param keyFilter
	 *            the filter to use
	 * @return the root ERXRestRequestNode
	 */
	public static ERXRestRequestNode requestNodeWithObjectAndFilter(Object obj, ERXKeyFilter keyFilter, IERXRestDelegate delegate) {
		String shortName = null;
		EOClassDescription classDescription = null;
		if (obj != null) {
			classDescription = ERXRestClassDescriptionFactory.classDescriptionForObject(obj);
			shortName = classDescription.entityName();
		}
		ERXRestRequestNode requestNode = new ERXRestRequestNode(shortName);
		if (ERXRestUtils.isPrimitive(obj)) {
			requestNode.setValue(obj);
		}
		else {
			if (!(obj instanceof List)) {
				requestNode.setType(shortName);
			}
			requestNode._fillInWithObjectAndFilter(obj, classDescription, keyFilter, delegate, new HashSet<Object>());
		}
		return requestNode;
	}
}
