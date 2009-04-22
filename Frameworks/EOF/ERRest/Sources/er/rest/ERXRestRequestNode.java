package er.rest;

import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import er.rest.format.ERXStringBufferRestResponse;
import er.rest.format.IERXRestWriter;
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
	}

	public ERXRestRequestNode(String name, Object value) {
		this(name);
		_value = value;
	}

	public Object toCollection() {
		return toCollection(new HashMap<Object, Object>());
	}

	public Object toCollection(Map<Object, Object> associatedObjects) {
		Object result = associatedObjects.get(_associatedObject);
		if (result == null) {
			if (_array) {
				List<Object> array = new LinkedList<Object>();
				for (ERXRestRequestNode child : _children) {
					array.add(child.toCollection());
				}
				result = array;
			}
			else if (_value != null) {
				result = _value;
			}
			else {
				Map<Object, Object> dict = new HashMap<Object, Object>();
				for (Map.Entry<String, String> attribute : _attributes.entrySet()) {
					String key = attribute.getKey();
					String value = attribute.getValue();
					if (value != null && !ERXRestRequestNode.NIL_KEY.equals(key) && !ERXRestRequestNode.TYPE_KEY.equals(key)) {
						dict.put(key, value);
					}
				}
				for (ERXRestRequestNode child : _children) {
					String name = child.name();
					Object value = child.toCollection();
					if (value != null) {
						dict.put(name, value);
					}
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

	public void setArray(boolean array) {
		_array = array;
	}

	public boolean isArray() {
		return _array;
	}

	public void setAssociatedObject(Object associatedObject) {
		_associatedObject = associatedObject;
	}

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
		return attributeForKey(ERXRestRequestNode.TYPE_KEY);
	}

	public void setNull(boolean isNull) {
		if (isNull) {
			setAttributeForKey("true", "nil");
		}
		else {
			_attributes.removeObjectForKey("nil");
		}
	}

	/**
	 * Returns true if the attribute "nil" is "true".
	 * 
	 * @return true if this is a nil attribute
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
		setNull(_value == null);
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

	public void toString(StringBuffer sb, int depth) {
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

	public Object objectWithFilter(String entityName, ERXKeyFilter keyFilter, ERXRestRequestNode.Delegate delegate) throws ParseException, ERXRestException {
		Object obj = delegate.objectOfEntityNamedWithRequestNode(entityName, this);
		// updateObjectWithFilter(obj, keyFilter, delegate);
		return obj;
	}

	public Object createObjectWithFilter(String entityName, ERXKeyFilter keyFilter, ERXRestRequestNode.Delegate delegate) throws ParseException, ERXRestException {
		Object obj = delegate.createObjectOfEntityNamed(entityName);
		updateObjectWithFilter(obj, keyFilter, delegate);
		return obj;
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
			setAttributeForKey(entity.name(), ERXRestRequestNode.TYPE_KEY);
			setAttributeForKey(String.valueOf(entity.primaryKeyValue(obj)), ERXRestRequestNode.ID_KEY);
			if (!visitedObjects.contains(obj)) {
				visitedObjects.add(obj);

				for (IERXAttribute attribute : entity.attributes()) {
					if (attribute.isClassProperty()) {
						ERXKey<Object> key = new ERXKey<Object>(attribute.name());
						if (keyFilter.matches(key, ERXKey.Type.Attribute)) {
							ERXRestRequestNode attributeNode = new ERXRestRequestNode(keyFilter.keyMap(key).key());
							attributeNode.setValue(key.valueInObject(obj));
							addChild(attributeNode);
						}
					}
				}

				for (IERXRelationship relationship : entity.relationships()) {
					if (relationship.isClassProperty()) {
						ERXKey<Object> key = new ERXKey<Object>(relationship.name());
						if (relationship.isToMany() && keyFilter.matches(key, ERXKey.Type.ToManyRelationship)) {
							ERXRestRequestNode toManyRelationshipNode = new ERXRestRequestNode(keyFilter.keyMap(key).key());
							toManyRelationshipNode.setType(relationship.destinationEntity().name());
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
						else if (!relationship.isToMany() && keyFilter.matches(key, ERXKey.Type.ToOneRelationship)) {
							ERXRestRequestNode toOneRelationshipNode = new ERXRestRequestNode(keyFilter.keyMap(key).key());
							Object value = key.valueInObject(obj);
							toOneRelationshipNode._fillInWithObjectAndFilter(value, keyFilter._filterForKey(key), visitedObjects);
							addChild(toOneRelationshipNode);
						}
					}
				}
			}
		}
	}

	public String toString(IERXRestWriter writer) {
		ERXStringBufferRestResponse response = new ERXStringBufferRestResponse();
		writer.appendToResponse(this, response);
		return response.toString();
	}

	public static ERXRestRequestNode requestNodeWithObjectAndFilter(IERXEntity entity, List<?> objects, ERXKeyFilter keyFilter) {
		ERXRestRequestNode requestNode = new ERXRestRequestNode(ERXLocalizer.defaultLocalizer().plurifiedString(entity.shortName(), 2));
		requestNode.setType(entity.shortName());
		requestNode._fillInWithObjectAndFilter(objects, keyFilter, new HashSet<Object>());
		return requestNode;
	}

	public static ERXRestRequestNode requestNodeWithObjectAndFilter(Object obj, ERXKeyFilter keyFilter) {
		String shortName = IERXEntity.Factory.entityForObject(obj).shortName();
		ERXRestRequestNode requestNode = new ERXRestRequestNode(shortName);
		if (!(obj instanceof List)) {
			requestNode.setType(shortName);
		}
		requestNode._fillInWithObjectAndFilter(obj, keyFilter, new HashSet<Object>());
		return requestNode;
	}

	public void updateObjectWithFilter(Object obj, ERXKeyFilter keyFilter, ERXRestRequestNode.Delegate delegate) throws ParseException, ERXRestException {
		if (obj == null) {
			return;
		}

		for (Map.Entry<String, String> attribute : _attributes.entrySet()) {
			ERXKey<Object> key = keyFilter.keyMap(new ERXKey<Object>(attribute.getKey()));
			if (keyFilter.matches(key, ERXKey.Type.Attribute) && delegate.isClassProperty(obj, key.key())) {
				Object value = ERXRestUtils.coerceValueToAttributeType(attribute.getValue(), null, obj, key.key());
				key.takeValueInObject(value, obj);
			}
		}

		for (ERXRestRequestNode childNode : _children) {
			ERXKey<Object> key = keyFilter.keyMap(new ERXKey<Object>(childNode.name()));
			if (delegate.isClassProperty(obj, key.key())) {
				NSKeyValueCoding._KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(obj, key.key());
				Class valueType = binding.valueType();
				if (List.class.isAssignableFrom(valueType) && keyFilter.matches(key, ERXKey.Type.ToManyRelationship)) {
					IERXEntity destinationEntity = IERXEntity.Factory.entityForObject(obj).relationshipNamed(key.key()).destinationEntity();

					@SuppressWarnings("unchecked")
					List<Object> existingValues = (List<Object>) NSKeyValueCoding.DefaultImplementation.valueForKey(obj, key.key());

					Set<Object> removedValues = new HashSet<Object>(existingValues);
					List<Object> newValues = new LinkedList<Object>();
					List<Object> allValues = new LinkedList<Object>();
					for (ERXRestRequestNode toManyNode : childNode.children()) {
						Object childObj = delegate.objectOfEntityWithRequestNode(destinationEntity, toManyNode);
						toManyNode.updateObjectWithFilter(childObj, keyFilter._filterForKey(key), delegate);
						if (!existingValues.contains(childObj)) {
							newValues.add(childObj);
						}
						allValues.add(childObj);
						removedValues.remove(childObj);
					}

					if (obj instanceof EOEnterpriseObject) {
						for (Object removedValue : removedValues) {
							((EOEnterpriseObject) obj).removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject) removedValue, key.key());
						}
						for (Object newValue : newValues) {
							((EOEnterpriseObject) obj).addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject) newValue, key.key());
						}
					}
					else {
						key.takeValueInObject(allValues, obj);
					}
				}
				else if (ERXRestUtils.isPrimitive(valueType) && keyFilter.matches(key, ERXKey.Type.Attribute)) {
					Object value = childNode.value();
					if (value instanceof String) {
						value = ERXRestUtils.coerceValueToAttributeType(value, null, obj, key.key());
					}
					key.takeValueInObject(value, obj);
				}
				else if (keyFilter.matches(key, ERXKey.Type.ToOneRelationship)) {
					IERXEntity destinationEntity = IERXEntity.Factory.entityForObject(obj).relationshipNamed(key.key()).destinationEntity();
					Object childObj = delegate.objectOfEntityWithRequestNode(destinationEntity, childNode);
					childNode.updateObjectWithFilter(childObj, keyFilter._filterForKey(key), delegate);
					if (obj instanceof EOEnterpriseObject && childObj instanceof EOEnterpriseObject) {
						((EOEnterpriseObject) obj).addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject) childObj, key.key());
					}
					else {
						key.takeValueInObject(childObj, obj);
					}
				}
			}
		}
	}

	public static interface Delegate {
		public Object createObjectOfEntityNamed(String name);

		public Object createObjectOfEntity(IERXEntity entity);

		public Object objectOfEntityNamedWithRequestNode(String name, ERXRestRequestNode node);

		public Object objectOfEntityWithRequestNode(IERXEntity entity, ERXRestRequestNode node);

		public boolean isClassProperty(Object object, String key);
	}

	public static class EODelegate implements Delegate {
		private EOEditingContext _editingContext;

		public EODelegate(EOEditingContext editingContext) {
			_editingContext = editingContext;
		}

		public boolean isClassProperty(Object object, String key) {
			boolean isClassProperty = true;
			IERXEntity entity = IERXEntity.Factory.entityForObject(object);
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

		public Object createObjectOfEntityNamed(String name) {
			IERXEntity entity = IERXEntity.Factory.entityNamed(_editingContext, name);
			return createObjectOfEntity(entity);
		}

		public Object createObjectOfEntity(IERXEntity entity) {
			Object obj = entity.createInstance(_editingContext);
			return obj;
		}

		public Object objectOfEntityNamedWithRequestNode(String entityName, ERXRestRequestNode node) {
			IERXEntity entity = IERXEntity.Factory.entityNamed(_editingContext, entityName);
			return objectOfEntityWithRequestNode(entity, node);
		}

		public Object objectOfEntityWithRequestNode(IERXEntity entity, ERXRestRequestNode node) {
			String id = node.attributeForKey(ERXRestRequestNode.ID_KEY);
			if (id == null) {
				ERXRestRequestNode idNode = node.childNamed(ERXRestRequestNode.ID_KEY);
				if (idNode != null) {
					Object idValue = idNode.value();
					id = (idValue == null) ? null : String.valueOf(idValue); // MS: this ends up double converting
					// non-String values
				}
			}

			Object obj;
			if (id == null) {
				obj = createObjectOfEntity(entity);
			}
			else {
				obj = entity.objectWithPrimaryKeyValue(_editingContext, id);
			}

			return obj;
		}
	}
}
