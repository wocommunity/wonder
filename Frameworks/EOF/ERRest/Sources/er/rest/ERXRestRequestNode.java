package er.rest;

import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;

import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;

/**
 * ERXRestRequestNode provides a model of a REST request. Because the incoming document format can vary (XML, JSON,
 * etc), we needed a document model that is more abstract than just an org.w3c.dom. Or, rather, one that isn't obnoxious
 * to use.
 * 
 * @author mschrag
 */
public class ERXRestRequestNode {
	private String _name;
	private Object _value;
	private NSMutableDictionary<String, String> _attributes;
	private NSMutableArray<ERXRestRequestNode> _children;

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
	 * Returns the type of this node.
	 * 
	 * @return the type of this node
	 */
	public String type() {
		return attributeForKey("type");
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

	public Object createObjectWithFilter(String name, ERXKeyFilter keyFilter, ERXRestRequestNode.Delegate delegate) throws ParseException, ERXRestException {
		Object obj = delegate.createObjectNamed(name);
		applyToObjectWithFilter(obj, keyFilter, delegate);
		return obj;
	}

	public void applyToObjectWithFilter(Object obj, ERXKeyFilter keyFilter, ERXRestRequestNode.Delegate delegate) throws ParseException, ERXRestException {
		if (obj == null) {
			return;
		}

		for (Map.Entry<String, String> attribute : _attributes.entrySet()) {
			ERXKey<Object> key = keyFilter.keyMap(new ERXKey<Object>(attribute.getKey()));
			if (keyFilter.matches(key, ERXKey.Type.Attribute) && delegate.isClassProperty(obj, key.key())) {
				Object value = ERXRestUtils.coerceValueType(null, obj, key.key(), attribute.getValue());
				key.takeValueInObject(value, obj);
			}
		}

		for (ERXRestRequestNode childNode : _children) {
			ERXKey<Object> key = keyFilter.keyMap(new ERXKey<Object>(childNode.name()));
			if (delegate.isClassProperty(obj, key.key())) {
				NSKeyValueCoding._KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(obj, key.key());
				Class valueType = binding.valueType();
				if (List.class.isAssignableFrom(valueType) && keyFilter.matches(key, ERXKey.Type.ToManyRelationship)) {
					@SuppressWarnings("unchecked")
					List<Object> existingValues = (List<Object>) NSKeyValueCoding.DefaultImplementation.valueForKey(obj, key.key());

					Set<Object> removedValues = new HashSet<Object>(existingValues);
					List<Object> newValues = new LinkedList<Object>();
					List<Object> allValues = new LinkedList<Object>();
					for (ERXRestRequestNode toManyNode : childNode.children()) {
						Object childObj = delegate.objectForRequestNode(toManyNode, obj, key.key());
						toManyNode.applyToObjectWithFilter(childObj, keyFilter._filterForKey(key), delegate);
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
						value = ERXRestUtils.coerceValueType(null, obj, key.key(), (String) value);
					}
					key.takeValueInObject(value, obj);
				}
				else if (keyFilter.matches(key, ERXKey.Type.ToOneRelationship)) {
					Object childObj = delegate.objectForRequestNode(childNode, obj, key.key());
					childNode.applyToObjectWithFilter(childObj, keyFilter._filterForKey(key), delegate);
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
		public Object createObjectNamed(String name);

		public Object objectForRequestNode(ERXRestRequestNode node, Object parent, String key);

		public boolean isClassProperty(Object object, String key);
	}

	public static class EODelegate implements Delegate {
		private EOEditingContext _editingContext;

		public EODelegate(EOEditingContext editingContext) {
			_editingContext = editingContext;
		}

		public String idAttributeName(EOEntity entity) {
			return "id";
		}

		public boolean isClassProperty(Object object, String key) {
			boolean isClassProperty = true;
			if (object instanceof EOEnterpriseObject) {
				EOEnterpriseObject eoParent = (EOEnterpriseObject) object;
				EOEntity entity = ERXEOAccessUtilities.entityForEo(eoParent);
				EOAttribute attribute = entity.attributeNamed(key);
				if (attribute != null) {
					isClassProperty = entity.classProperties().containsObject(attribute);
				}
				else {
					EORelationship relationship = entity.relationshipNamed(key);
					if (relationship != null) {
						isClassProperty = entity.classProperties().containsObject(relationship);
					}
				}
			}
			return isClassProperty;
		}

		public Object createObjectNamed(String name) {
			Object obj;
			EOEntity entity = ERXEOAccessUtilities.entityNamed(_editingContext, name);
			if (entity == null) {
				try {
					obj = _NSUtilities.classWithName(name).newInstance();
				}
				catch (Exception e) {
					throw NSForwardException._runtimeExceptionForThrowable(e);
				}
			}
			else {
				obj = EOUtilities.createAndInsertInstance(_editingContext, name);
			}
			return obj;
		}

		public Object objectForRequestNode(ERXRestRequestNode node, Object parent, String key) {
			EOEnterpriseObject eoParent = (EOEnterpriseObject) parent;
			EOEntity parentEntity = ERXEOAccessUtilities.entityForEo(eoParent);
			EORelationship relationship = parentEntity.relationshipNamed(key);
			EOEntity destinationEntity = relationship.destinationEntity();
			String idKey = idAttributeName(destinationEntity);
			String id = node.attributeForKey(idKey);
			if (id == null) {
				ERXRestRequestNode idNode = node.childNamed(idKey);
				if (idNode != null) {
					Object idValue = idNode.value();
					id = (idValue == null) ? null : String.valueOf(idValue); // MS: this ends up double converting non-String values  
				}
			}

			Object obj;
			if (id == null) {
				obj = createObjectNamed(destinationEntity.name());
			}
			else {
				Object pkValue = ((EOAttribute) destinationEntity.primaryKeyAttributes().objectAtIndex(0)).validateValue(id);
				obj = ERXEOControlUtilities.objectWithPrimaryKeyValue(_editingContext, destinationEntity.name(), pkValue, null, false);
			}

			return obj;
		}
	}
}
