package er.rest;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.webobjects.appserver.WOResponse;
import com.webobjects.eocontrol.EOEditingContext;
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
import er.rest.routes.model.EOEntityProxy;
import er.rest.routes.model.IERXAttribute;
import er.rest.routes.model.IERXEntity;
import er.rest.routes.model.IERXRelationship;

/**
 * ERXRestRequestNode provides a model of a REST request. Because the incoming document format can vary (XML, JSON,
 * etc), we needed a document model that is more abstract than just an org.w3c.dom. Or, rather, one that isn't obnoxious
 * to use.
 * 
 * @author mschrag
 */
public class ERXRestRequestNode implements NSKeyValueCoding {
	public static final String ID_KEY = "id";
	public static final String NIL_KEY = "nil";
	public static final String TYPE_KEY = "type";

	private boolean _array;
	private String _name;
	private Object _value;
	private NSMutableDictionary<String, String> _attributes;
	private NSMutableArray<ERXRestRequestNode> _children;
	private Object _associatedObject;

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

	/**
	 * Returns the Java object that corresponds to this node hierarchy.
	 * 
	 * @return the Java object that corresponds to this node hierarchy
	 */
	public Object toJava() {
		return toJava(new HashMap<Object, Object>());
	}

	/**
	 * Returns the Java object that corresponds to this node hierarchy.
	 * 
	 * @param the
	 *            associatedObjects map (to prevent infinite loops)
	 * @return the Java object that corresponds to this node hierarchy
	 */
	protected Object toJava(Map<Object, Object> associatedObjects) {
		Object result = associatedObjects.get(_associatedObject);
		if (result == null) {
			if (isArray()) {
				List<Object> array = new LinkedList<Object>();
				for (ERXRestRequestNode child : _children) {
					array.add(child.toJava(associatedObjects));
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
					Object value = child.toJava(associatedObjects);
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

	/**
	 * Sets the type of this node (type as in the Class that it represents).
	 * 
	 * @param type
	 *            the type of this node
	 */
	public void setType(String type) {
		if (type == null) {
			_attributes.removeObjectForKey(ERXRestRequestNode.TYPE_KEY);
		}
		else {
			setAttributeForKey(type, ERXRestRequestNode.TYPE_KEY);
		}
	}

	/**
	 * Returns the type of this node.
	 * 
	 * @return the type of this node
	 */
	public String type() {
		String type = attributeForKey(ERXRestRequestNode.TYPE_KEY);
		if (type == null) {
			ERXRestRequestNode typeNode = childNamed(ERXRestRequestNode.TYPE_KEY);
			if (typeNode != null) {
				type = (String) typeNode.value();
			}
		}
		return type;
	}

	/**
	 * Returns the id attribute or id child node from this node.
	 * 
	 * @return the id attribute or id child node from this node
	 */
	public Object id() {
		Object id = attributeForKey(ERXRestRequestNode.ID_KEY);
		if (id == null) {
			ERXRestRequestNode idNode = childNamed(ERXRestRequestNode.ID_KEY);
			if (idNode != null) {
				id = idNode.value();
				// id = (idValue == null) ? null : String.valueOf(idValue);
				// MS: this ends up double converting non-String values
			}
		}
		return id;
	}

	protected void guessNull() {
		setNull(_value == null && _children.size() == 0 && id() == null && !isArray() && _associatedObject == null);
	}

	/**
	 * Sets whether or not this node represents a null value.
	 * 
	 * @param isNull
	 *            whether or not this node represents a null value
	 */
	public void setNull(boolean isNull) {
		if (isNull) {
			setAttributeForKey("true", "nil");
		}
		else {
			_attributes.removeObjectForKey("nil");
		}
	}

	/**
	 * Returns whether or not this node represents a null value.
	 * 
	 * @return true whether or not this node represents a null value
	 */
	public boolean isNull() {
		return "true".equals(attributeForKey("nil"));
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
		if (!"nil".equals(key)) {
			guessNull();
		}
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
	public Object objectWithFilter(String entityName, ERXKeyFilter keyFilter, ERXRestRequestNode.Delegate delegate) {
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
	public Object createObjectWithFilter(String entityName, ERXKeyFilter keyFilter, ERXRestRequestNode.Delegate delegate) {
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

	protected void _addToManyRelationshipNodeForKeyOfEntityInObject(ERXKey<?> key, IERXEntity destinationEntity, Object obj, ERXKeyFilter keyFilter, Set<Object> visitedObjects) {
		ERXRestRequestNode toManyRelationshipNode = new ERXRestRequestNode(keyFilter.keyMap(key).key());
		toManyRelationshipNode.setType(destinationEntity.name());
		toManyRelationshipNode.setArray(true);

		List childrenObjects = (List) key.valueInObject(obj);
		ERXKeyFilter childFilter = keyFilter._filterForKey(key);
		for (Object childObj : childrenObjects) {
			ERXRestRequestNode childNode = new ERXRestRequestNode(null);
			childNode._fillInWithObjectAndFilter(childObj, childFilter, visitedObjects);
			toManyRelationshipNode.addChild(childNode);
		}

		addChild(toManyRelationshipNode);

	}

	protected void _addToOneRelationshipNodeForKeyInObject(ERXKey<?> key, Object obj, ERXKeyFilter keyFilter, Set<Object> visitedObjects) {
		Object value = key.valueInObject(obj);
		if (value != null) {
			ERXRestRequestNode toOneRelationshipNode = new ERXRestRequestNode(keyFilter.keyMap(key).key());
			toOneRelationshipNode._fillInWithObjectAndFilter(value, keyFilter._filterForKey(key), visitedObjects);
			addChild(toOneRelationshipNode);
		}
	}

	protected void _addAttributesAndRelationshipsForObjectOfEntity(Object obj, IERXEntity entity, ERXKeyFilter keyFilter, Set<Object> visitedObjects) {
		Set<ERXKey> visitedKeys = new HashSet<ERXKey>();
		for (IERXAttribute attribute : entity.attributes()) {
			if (attribute.isClassProperty()) {
				ERXKey<Object> key = new ERXKey<Object>(attribute.name());
				if (keyFilter.matches(key, ERXKey.Type.Attribute)) {
					_addAttributeNodeForKeyInObject(key, obj, keyFilter);
					visitedKeys.add(key);
				}
			}
		}

		for (IERXRelationship relationship : entity.relationships()) {
			if (relationship.isClassProperty()) {
				ERXKey<Object> key = new ERXKey<Object>(relationship.name());
				if (relationship.isToMany() && keyFilter.matches(key, ERXKey.Type.ToManyRelationship)) {
					_addToManyRelationshipNodeForKeyOfEntityInObject(key, relationship.destinationEntity(), obj, keyFilter, visitedObjects);
					visitedKeys.add(key);
				}
				else if (!relationship.isToMany() && keyFilter.matches(key, ERXKey.Type.ToOneRelationship)) {
					_addToOneRelationshipNodeForKeyInObject(key, obj, keyFilter, visitedObjects);
					visitedKeys.add(key);
				}
			}
		}

		Set<ERXKey> includeKeys = keyFilter.includes().keySet();
		if (includeKeys != null && !includeKeys.isEmpty()) {
			Set<ERXKey> remainingKeys = new HashSet<ERXKey>(includeKeys);
			remainingKeys.removeAll(visitedKeys);
			if (!remainingKeys.isEmpty()) {
				// this is sort of expensive, but we want to support non-eomodel to-many relationships on EO's, so
				// we fallback and lookup the class entity ...
				if (entity instanceof EOEntityProxy) {
					IERXEntity classEntity = IERXEntity.Factory.entityForObject(obj.getClass());
					for (ERXKey<?> remainingKey : remainingKeys) {
						String keyName = remainingKey.key();
						IERXAttribute attribute = classEntity.attributeNamed(keyName);
						if (attribute != null) {
							_addAttributeNodeForKeyInObject(remainingKey, obj, keyFilter);
						}
						else {
							IERXRelationship relationship = classEntity.relationshipNamed(keyName);
							if (relationship != null && relationship.isToMany()) {
								_addToManyRelationshipNodeForKeyOfEntityInObject(remainingKey, relationship.destinationEntity(), obj, keyFilter, visitedObjects);
							}
							else if (relationship != null && !relationship.isToMany()) {
								_addToOneRelationshipNodeForKeyInObject(remainingKey, obj, keyFilter, visitedObjects);
							}
							else {
								throw new IllegalArgumentException("This key filter specified that the key '" + keyName + "' should be included on '" + entity.name() + "', but it does not exist.");
							}
						}
					}
				}
				else {
					throw new IllegalArgumentException("This key filter specified that the keys '" + remainingKeys + "' should be included on '" + entity.name() + "', but they do not exist.");
				}
			}
		}
	}

	protected void _fillInWithObjectAndFilter(Object obj, ERXKeyFilter keyFilter, Set<Object> visitedObjects) {
		if (obj instanceof List) {
			setAssociatedObject(obj);
			// setAttributeForKey(/* ??? */, ERXRestRequestNode.TYPE_KEY);
			setArray(true);

			for (Object childObj : (List) obj) {
				ERXRestRequestNode childNode = new ERXRestRequestNode(null);
				childNode._fillInWithObjectAndFilter(childObj, keyFilter, visitedObjects);
				addChild(childNode);
			}
		}
		else {
			IERXEntity entity = IERXEntity.Factory.entityForObject(obj);
			if (_name == null) {
				_name = entity.name();
			}
			setAssociatedObject(obj);
			setType(entity.name());
			Object pkValue = entity.primaryKeyValue(obj);
			if (pkValue != null) {
				setAttributeForKey(String.valueOf(pkValue), ERXRestRequestNode.ID_KEY);
			}
			if (!visitedObjects.contains(obj)) {
				visitedObjects.add(obj);
				_addAttributesAndRelationshipsForObjectOfEntity(obj, entity, keyFilter, visitedObjects);
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
		return toString(format.writer());
	}

	/**
	 * Returns a string representation of this request node using the given IERXRestWriter.
	 * 
	 * @param writer
	 *            the writer to use
	 * @return a string representation of this request node using the given IERXRestWriter
	 */
	public String toString(IERXRestWriter writer) {
		WOResponse response = new WOResponse();
		writer.appendToResponse(this, new ERXWORestResponse(response));
		return response.contentString();
	}

	protected boolean isClassProperty(IERXEntity entity, String key) {
		boolean isClassProperty = true;
		IERXAttribute attribute = entity.attributeNamed(key);
		if (attribute != null) {
			isClassProperty = attribute.isClassProperty();
		}
		else {
			IERXRelationship relationship = entity.relationshipNamed(key);
			if (relationship != null) {
				isClassProperty = relationship.isClassProperty();
			}
		}
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
	public void updateObjectWithFilter(Object obj, ERXKeyFilter keyFilter, ERXRestRequestNode.Delegate delegate) {
		if (obj == null) {
			return;
		}

		IERXEntity entity = IERXEntity.Factory.entityForObject(obj);
		for (Map.Entry<String, String> attribute : _attributes.entrySet()) {
			ERXKey<Object> key = keyFilter.keyMap(new ERXKey<Object>(attribute.getKey()));
			String keyName = key.key();
			if (keyFilter.matches(key, ERXKey.Type.Attribute) && isClassProperty(entity, keyName)) {
				Object value = ERXRestUtils.coerceValueToAttributeType(attribute.getValue(), null, obj, keyName);
				key.takeValueInObject(value, obj);
			}
		}

		for (ERXRestRequestNode childNode : _children) {
			ERXKey<Object> key = keyFilter.keyMap(new ERXKey<Object>(childNode.name()));
			String keyName = key.key();
			if (isClassProperty(entity, keyName)) {
				NSKeyValueCoding._KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(obj, keyName);
				Class valueType = binding.valueType();

				if (List.class.isAssignableFrom(valueType) && keyFilter.matches(key, ERXKey.Type.ToManyRelationship)) {
					// this is sort of expensive, but we want to support non-eomodel to-many relationships on EO's, so
					// we fallback and lookup the class entity ...
					IERXRelationship relationship = entity.relationshipNamed(keyName);
					if (relationship == null && entity instanceof EOEntityProxy) {
						IERXEntity classEntity = IERXEntity.Factory.entityForObject(obj.getClass());
						relationship = classEntity.relationshipNamed(keyName);
						if (relationship == null) {
							throw new IllegalArgumentException("There is no to-many relationship named '" + key.key() + "' on '" + entity.name() + "'.");
						}
					}
					IERXEntity destinationEntity = relationship.destinationEntity();

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
					// this is sort of expensive, but we want to support non-eomodel to-one relationships on EO's, so
					// we fallback and lookup the class entity ...
					IERXRelationship relationship = entity.relationshipNamed(keyName);
					if (relationship == null && entity instanceof EOEntityProxy) {
						IERXEntity classEntity = IERXEntity.Factory.entityForObject(obj.getClass());
						relationship = classEntity.relationshipNamed(keyName);
						if (relationship == null) {
							throw new IllegalArgumentException("There is no to-one relationship named '" + key.key() + "' on '" + entity.name() + "'.");
						}
					}
					IERXEntity destinationEntity = relationship.destinationEntity();
					Object childObj = delegate.objectOfEntityWithID(destinationEntity, childNode.id());
					childNode.updateObjectWithFilter(childObj, keyFilter._filterForKey(key), delegate);
					if (obj instanceof EOEnterpriseObject && childObj instanceof EOEnterpriseObject) {
						((EOEnterpriseObject) obj).addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject) childObj, keyName);
					}
					else {
						key.takeValueInObject(childObj, obj);
					}
				}
				else if (/* entity.attributeNamed(keyName) != null && */keyFilter.matches(key, ERXKey.Type.Attribute)) {
					Object value = childNode.value();
					if (value instanceof String) {
						value = ERXRestUtils.coerceValueToAttributeType(value, null, obj, keyName);
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
	public static ERXRestRequestNode requestNodeWithObjectAndFilter(IERXEntity entity, List<?> objects, ERXKeyFilter keyFilter) {
		ERXRestRequestNode requestNode = new ERXRestRequestNode(ERXLocalizer.defaultLocalizer().plurifiedString(entity.shortName(), 2));
		requestNode.setType(entity.shortName());
		requestNode._fillInWithObjectAndFilter(objects, keyFilter, new HashSet<Object>());
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
	public static ERXRestRequestNode requestNodeWithObjectAndFilter(Object obj, ERXKeyFilter keyFilter) {
		String shortName = (obj != null) ? IERXEntity.Factory.entityForObject(obj).shortName() : null;
		ERXRestRequestNode requestNode = new ERXRestRequestNode(shortName);
		if (ERXRestUtils.isPrimitive(obj)) {
			requestNode.setValue(obj);
		}
		else {
			if (!(obj instanceof List)) {
				requestNode.setType(shortName);
			}
			requestNode._fillInWithObjectAndFilter(obj, keyFilter, new HashSet<Object>());
		}
		return requestNode;
	}

	/**
	 * The delegate interface used to convert objects to and from request nodes.
	 * 
	 * @author mschrag
	 */
	public static interface Delegate {
		/**
		 * Creates a new instance of the entity with the given name.
		 * 
		 * @param name
		 *            the name
		 * @return a new instance of the entity with the given name
		 */
		public Object createObjectOfEntityNamed(String name);

		/**
		 * Creates a new instance of the entity.
		 * 
		 * @param entity
		 *            the entity
		 * @return a new instance of the entity
		 */
		public Object createObjectOfEntity(IERXEntity entity);

		/**
		 * Returns the object with the given entity name and ID.
		 * 
		 * @param name
		 *            the name of the entity
		 * @param id
		 *            the ID of the object
		 * @return the object with the given entity name and ID
		 */
		public Object objectOfEntityNamedWithID(String name, Object id);

		/**
		 * Returns the object with the given entity and ID.
		 * 
		 * @param entity
		 *            the entity
		 * @param id
		 *            the ID of the object
		 * @return the object with the given entity and ID
		 */
		public Object objectOfEntityWithID(IERXEntity entity, Object id);
	}

	/**
	 * EODelegate is an implementation of the ERXRestRequestNode.Delegate interface that understands EOF.
	 * 
	 * @author mschrag
	 */
	public static class EODelegate implements Delegate {
		private EOEditingContext _editingContext;

		public EODelegate(EOEditingContext editingContext) {
			_editingContext = editingContext;
		}

		public Object createObjectOfEntityNamed(String name) {
			IERXEntity entity = IERXEntity.Factory.entityNamed(_editingContext, name);
			return createObjectOfEntity(entity);
		}

		public Object createObjectOfEntity(IERXEntity entity) {
			Object obj = entity.createInstance(_editingContext);
			return obj;
		}

		public Object objectOfEntityNamedWithID(String entityName, Object id) {
			IERXEntity entity = IERXEntity.Factory.entityNamed(_editingContext, entityName);
			return objectOfEntityWithID(entity, id);
		}

		public Object objectOfEntityWithID(IERXEntity entity, Object id) {
			String strID = (id == null) ? null : String.valueOf(id);

			Object obj;
			if (id == null) {
				obj = createObjectOfEntity(entity);
			}
			else {
				obj = entity.objectWithPrimaryKeyValue(_editingContext, strID);
			}

			return obj;
		}
	}
}
