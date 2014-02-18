package er.rest;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXResponse;
import er.extensions.eof.ERXKey;
import er.extensions.eof.ERXKeyFilter;
import er.extensions.foundation.ERXArrayUtilities;
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
public class ERXRestRequestNode implements NSKeyValueCoding, NSKeyValueCodingAdditions {
    private static final Logger log = Logger.getLogger(ERXRestRequestNode.class);
    
	private boolean _array;
	private String _name;
	private boolean _rootNode;
	private Object _value;
	private LinkedHashMap<String, Object> _attributes;
	private NSMutableArray<ERXRestRequestNode> _children;
	private Object _associatedObject;

	private Object _id;
	private String _type;
	private boolean _null;

	/**
	 * Constructs a new root node with no name.
	 */
	public ERXRestRequestNode() {
		this(null, true);
	}

	/**
	 * Construct a node with the given name
	 * 
	 * @param name
	 *            the name of this node
	 * @param rootNode
	 *            if true, the node is the root of a graph
	 */
	public ERXRestRequestNode(String name, boolean rootNode) {
		_name = name;
		_rootNode = rootNode;
		_attributes = new LinkedHashMap<String, Object>();
		_children = new NSMutableArray<ERXRestRequestNode>();
		guessNull();
	}

	/**
	 * Construct a node with the given name and value.
	 * 
	 * @param name
	 *            the name of this node
	 * @param rootNode
	 *            if true, the node is the root of a graph
	 * @param value
	 *            the value of this node
	 */
	public ERXRestRequestNode(String name, Object value, boolean rootNode) {
		this(name, rootNode);
		_value = value;
		guessNull();
	}

	/**
	 * Clones this node.
	 * 
	 * @return a clone of this node
	 */
	public ERXRestRequestNode cloneNode() {
		ERXRestRequestNode cloneNode = new ERXRestRequestNode(_name, _rootNode);
		cloneNode._attributes.putAll(_attributes);
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
	 * Sets whether or not this is a root node (a root node is one that would typically have a node name that is an
	 * entity name -- the actual root, or elements in an array, for instance).
	 * 
	 * @param rootNode
	 *            whether or not this is a root node
	 */
	public void setRootNode(boolean rootNode) {
		_rootNode = rootNode;
	}

	/**
	 * Returns whether or not this is a root node (a root node is one that would typically have a node name that is an
	 * entity name -- the actual root, or elements in an array, for instance).
	 * 
	 * @return whether or not this is a root node
	 */
	public boolean isRootNode() {
		return _rootNode;
	}

	/**
	 * Returns the Java object that corresponds to this node hierarchy.
	 * 
	 * @param delegate
	 *            the format delegate to notify during rendering
	 * 
	 * @return the Java object that corresponds to this node hierarchy
	 */
	public Object toJavaCollection(ERXRestFormat.Delegate delegate) {
		return toJavaCollection(delegate, null, new HashMap<Object, Object>());
	}

	/**
	 * Returns the Java object that corresponds to this node hierarchy.
	 * 
	 * @param delegate
	 *            the format delegate to notify during rendering
	 * @param conversionMap
	 *            the conversion map to use to record object => request node mappings
	 * 
	 * @return the Java object that corresponds to this node hierarchy
	 */
	public Object toJavaCollection(ERXRestFormat.Delegate delegate, Map<Object, ERXRestRequestNode> conversionMap) {
		return toJavaCollection(delegate, conversionMap, new HashMap<Object, Object>());
	}

	/**
	 * Returns the Java object that corresponds to this node hierarchy.
	 * 
	 * @param delegate
	 *            the format delegate to notify during rendering
	 * @param conversionMap
	 *            the conversion map to use to record object => request node mappings
	 * @param associatedObjects
	 *            the associatedObjects map (to prevent infinite loops)
	 * @return the Java object that corresponds to this node hierarchy
	 */
	protected Object toJavaCollection(ERXRestFormat.Delegate delegate, Map<Object, ERXRestRequestNode> conversionMap, Map<Object, Object> associatedObjects) {
		Object result = associatedObjects.get(_associatedObject);
		if (result == null) {
			if (delegate != null) {
				delegate.nodeWillWrite(this);
			}

			if (isArray()) {
				List<Object> array = new LinkedList<Object>();
				for (ERXRestRequestNode child : _children) {
					array.add(child.toJavaCollection(delegate, conversionMap, associatedObjects));
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
				Map<Object, Object> dict = new LinkedHashMap<Object, Object>();
				for (Map.Entry<String, Object> attribute : _attributes.entrySet()) {
					String key = attribute.getKey();
					Object value = attribute.getValue();
					// if (value != null) {
					dict.put(key, value);
					// }
				}
				for (ERXRestRequestNode child : _children) {
					Object value = child.toJavaCollection(delegate, conversionMap, associatedObjects);
					// MS: name has to be after toJavaCollection, because the naming delegate could rename it ... little
					// sketchy, i know
					String name = child.name();
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
			if (conversionMap != null && result != null) {
				conversionMap.put(result, this);
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
	 * @param associatedObjects
	 *            the associatedObjects map (to prevent infinite loops)
	 * @return NSCollection/Java object that corresponds to this node hierarchy
	 */
	protected Object toNSCollection(ERXRestFormat.Delegate delegate, NSMutableDictionary<Object, Object> associatedObjects) {
		Object result = associatedObjects.get(_associatedObject);
		if (result == null) {
			if (delegate != null) {
				delegate.nodeWillWrite(this);
			}

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
				for (Map.Entry<String, Object> attribute : _attributes.entrySet()) {
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
 	
 	/**
 	 * A parsed keypath segment that can contain either a name or a name and an index 
 	 */
 	private static class Key {
 		public final String _name;
 		public final int _index;
 		
 		public Key(String name, int index) {
 			_name = name;
 			_index = index;
 		}
 		
 		/**
 		 * Parses "keyName" or "keyName[x]" format keys.
 		 * 
 		 * @param keySegment the segment of a keypath to parse
 		 * @return a Key object
 		 */
 		public static Key parse(String keySegment) {
 			String key = keySegment;
			int keyIndex = -1;
			int closeBracketIndex = key.lastIndexOf(']');
			if (closeBracketIndex != -1) {
				int openBracketIndex = key.lastIndexOf('[');
				if (openBracketIndex != -1) {
					keyIndex = Integer.valueOf(key.substring(openBracketIndex + 1, closeBracketIndex));
					key = key.substring(0, openBracketIndex);
				}
			}
			return new Key(key, keyIndex);
 		}
 	}

	public void takeValueForKey(Object value, String keyName) {
		if (value instanceof ERXRestRequestNode) {
			removeAttributeForKey(keyName);
			removeChildNamed(keyName);
			((ERXRestRequestNode)value).setName(keyName);
			addChild((ERXRestRequestNode)value);
		}
		else if (_attributes.containsKey(keyName)) {
			_attributes.put(keyName, value);
		}
		else {
			Key key = Key.parse(keyName);
			ERXRestRequestNode child = childNamed(key._name);
			if (child == null) {
 				if (key._index != -1) {
 					child = new ERXRestRequestNode(key._name, null, false);
 					addChild(child);
 					child.childAtIndex(key._index).setValue(value);
 				}
 				else {
 					addChild(new ERXRestRequestNode(key._name, value, false));
 				}
				//throw new NSKeyValueCoding.UnknownKeyException("There is no key named '" + key + "' on this node.", this, key);
			}
 			else if (key._index != -1) {
	 	    		child.childAtIndex(key._index).setValue(value);
 			}
			else {
				throw new IllegalArgumentException("Unable to set the value of '" + key._name + "' to " + value + ".");
			}
		}
	}

	public Object valueForKey(String keyName) {
		Object value;
		if (_attributes.containsKey(keyName)) {
			value = _attributes.get(keyName);
		}
		else {
 			Key key = Key.parse(keyName);
 			
 			ERXRestRequestNode child = childNamed(key._name);
			if (child == null) {
				throw new NSKeyValueCoding.UnknownKeyException("There is no key named '" + key._name + "' on this node.", this, key._name);
			}
 			else if (key._index != -1) {
 				if (child.children().count() <= key._index) {
 					throw new NSKeyValueCoding.UnknownKeyException("There is no key named '" + key._name + "' with a child index " + key._index + " on this node.", this, key._name);
 				}
 				else {
 					ERXRestRequestNode indexChild = child.children().objectAtIndex(key._index);
 					if (indexChild.children().count() == 0) {
 						value = indexChild.value();
 					}
 					else {
 						value = indexChild;
 					}
 				}
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

	public Object valueForKeyPath(String keyPath) {
		return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
	}
	
	public void takeValueForKeyPath(Object value, String keyPath) {
        if (keyPath == null) {
            throw new IllegalArgumentException("Key path cannot be null");
        }

        int separatorIndex = keyPath.indexOf(NSKeyValueCodingAdditions._KeyPathSeparatorChar);
        if (separatorIndex != -1) {
	    	Key key = Key.parse(keyPath.substring(0, separatorIndex));
			ERXRestRequestNode child = childNamed(key._name);
			if (child == null) {
				child = new ERXRestRequestNode(key._name, false); 
				addChild(child);
			}
			String nextKeyPath = keyPath.substring(separatorIndex + 1);
	    	if (key._index == -1) {
				child.takeValueForKeyPath(value, nextKeyPath);
	    	}
	    	else {
	    		child.childAtIndex(key._index).takeValueForKeyPath(value, nextKeyPath);
	    	}
        }
        else {
    		takeValueForKey(value, keyPath);
        }
	}
	
	public ERXRestRequestNode childAtIndex(int index) {
		int childCount = _children.count();
		if (childCount <= index) {
			setArray(true);
			for (int i = childCount; i <= index; i ++) {
				addChild(new ERXRestRequestNode(null, false));
			}
		}
		return _children.objectAtIndex(index);
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
	 * Removes the child name that has the given name.
	 * 
	 * @param name
	 *            the name of the node to remove
	 * @return the node that was removed
	 */
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

	/**
	 * Returns the type of this node (type as in the Class that it represents).
	 * 
	 * @return the type of this node
	 */
	public String type() {
		return _type;
	}

	/**
	 * Sets the ID associated with this node.
	 * 
	 * @param id
	 *            the ID associated with this node
	 */
	public void setID(Object id) {
		_id = id;
		guessNull();
	}

	/**
	 * Returns the ID associated with this node.
	 * 
	 * @return the ID associated with this node
	 */
	public Object id() {
		return _id;
	}

	/**
	 * Removes the attribute or child node that has the given name (and returns it).
	 * 
	 * @param name
	 *            the name of the attribute or node to remove
	 * @return the removed attribute value
	 */
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
	 * Sets the name of this node.
	 * 
	 * @param name
	 *            the name of this node
	 */
	public void setName(String name) {
		_name = name;
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
		if (value instanceof NSKeyValueCoding.Null) {
			_value = null;
		}
		else {
			_value = value;
		}
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
	public void setAttributeForKey(Object attribute, String key) {
		_attributes.put(key, attribute);
		// if (!"nil".equals(key)) {
		guessNull();
		// }
	}

	/**
	 * Removes the attribute that has the given name.
	 * 
	 * @param key
	 *            the name of the attribute to remove
	 * @return the attribute value
	 */
	public Object removeAttributeForKey(String key) {
		Object attribute = _attributes.remove(key);
		return attribute;
	}

	/**
	 * Returns the attribute value for the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the attribute value
	 */
	public Object attributeForKey(String key) {
		return _attributes.get(key);
	}

	/**
	 * Returns the attributes dictionary for this node.
	 * 
	 * @return the attributes dictionary
	 */
	public Map<String, Object> attributes() {
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
		sb.append('[');
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
			sb.append(' ');
			sb.append(_attributes);
		}
		if (_value != null) {
			sb.append('=');
			sb.append(_value);
		}
		if (!_children.isEmpty()) {
			sb.append('\n');
			for (ERXRestRequestNode child : _children) {
				child.toString(sb, depth + 1);
			}
			for (int i = 0; i < depth; i++) {
				sb.append("  ");
			}
		}
		sb.append(']');
		if (depth > 0) {
			sb.append('\n');
		}
	}
	
	protected String entityName(String suggestedEntityName) {
		String entityName = suggestedEntityName;
		if (entityName == null) {
			entityName = type();
			if (entityName == null && value() == null) {
				entityName = "NSDictionary";
			}
		}
		return entityName;
	}

	/**
	 * Equivalent to objectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
	 * 
	 * @return the object that this request node represents
	 */
	public Object object() {
		return objectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
	}

	/**
	 * Returns the object that this request node represents.
	 * 
	 * @param entityName
	 *            the entity name of the object to use
	 * @param keyFilter
	 *            the filter to use for determining which keys can be updated (or null for no update)
	 * @param context
	 *            the delegate to use
	 * @return the object that this request node represents
	 */
	public Object objectWithFilter(String entityName, ERXKeyFilter keyFilter, ERXRestContext context) {
		Object obj;
		if (isArray()) {
			NSMutableArray<Object> objs = new NSMutableArray<Object>();
			for (ERXRestRequestNode childNode : children()) {
				Object child = childNode.objectWithFilter(entityName, ERXKeyFilter.filterWithAllRecursive(), context);
				if (child != null) {
					objs.addObject(child);
				}
			}
			obj = objs;
		}
		else {
			String finalEntityName = entityName(entityName);
			EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(entityName);
			if (classDescription == null) {
				throw new IllegalArgumentException("There is no registered entity with the name '" + finalEntityName + "'.");
			}
			obj = IERXRestDelegate.Factory.delegateForClassDescription(classDescription).objectOfEntityWithID(classDescription, id(), context);
			if (keyFilter != null) {
				updateObjectWithFilter(obj, keyFilter, context);
			}
		}
		return obj;
	}

	/**
	 * Equivalent to createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
	 *  
	 * @return a new instance of an object represented by this request node
	 */
	public Object createObject() {
		return createObjectWithFilter(null, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
	}
	
	/**
	 * Creates a new instance of an object represented by this request node.
	 * 
	 * @param entityName
	 *            the entity name of the object to use
	 * @param keyFilter
	 *            the filter to use for determining which keys can be updated (or null for no update)
	 * @param context
	 *            the delegate to use
	 * @return a new instance of an object represented by this request node
	 */
	public Object createObjectWithFilter(String entityName, ERXKeyFilter keyFilter, ERXRestContext context) {
		// MS: if it's a null node, just hand back a null
		if (isNull()) {
			return null;
		}
		
		String finalEntityName = entityName(entityName);
		// MS: if there is no type, just return the value of this object
		if (finalEntityName == null) {
			return value();
		}
		
		EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(finalEntityName);
		if (classDescription == null) {
			throw new IllegalArgumentException("There is no registered entity with the name '" + finalEntityName + "'.");
		}
		Object obj = IERXRestDelegate.Factory.delegateForClassDescription(classDescription).createObjectOfEntityWithID(classDescription, id(), context);
		if (keyFilter != null) {
			updateObjectWithFilter(obj, keyFilter, context);
		}
		return obj;
	}

	protected void _addAttributeNodeForKeyInObject(ERXKey<?> key, Object obj, ERXKeyFilter keyFilter) {
		ERXRestRequestNode attributeNode = new ERXRestRequestNode(keyFilter.keyMap(key).key(), false);
		attributeNode.setValue(key.valueInObject(obj));
		addChild(attributeNode);
	}

	protected void _addToManyRelationshipNodeForKeyOfEntityInObject(ERXKey<?> key, EOClassDescription destinationEntity, Object obj, ERXKeyFilter keyFilter, ERXRestContext context, Set<Object> visitedObjects) {
		ERXRestRequestNode toManyRelationshipNode = new ERXRestRequestNode(keyFilter.keyMap(key).key(), false);
		toManyRelationshipNode.setArray(true);
		toManyRelationshipNode.setType(destinationEntity.entityName());

		List childrenObjects = (List) key.valueInObject(obj);
		ERXKeyFilter childFilter = keyFilter._filterForKey(key);
		if (childFilter.isDistinct()) {
			if (childrenObjects instanceof NSArray) {
				childrenObjects = ERXArrayUtilities.distinct((NSArray<?>) childrenObjects);
			} else {
				childrenObjects = new ArrayList(new HashSet(childrenObjects));
			}
		}
        NSArray<EOSortOrdering> sortOrderings = childFilter.sortOrderings();
        if (sortOrderings != null && sortOrderings.count() > 0) {
                if (childrenObjects instanceof NSArray) {
                        childrenObjects = EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray<?>)childrenObjects, sortOrderings);
                }
                else {
                        log.warn("Skipping sort orderings for '" + key + "' on " + obj + " because sort orderings are only supported for NSArrays.");
                }
        }
        for (Object childObj : childrenObjects) {
			ERXRestRequestNode childNode = new ERXRestRequestNode(null, false);
			childNode._fillInWithObjectAndFilter(childObj, destinationEntity, childFilter, context, visitedObjects);
			toManyRelationshipNode.addChild(childNode);
		}

		addChild(toManyRelationshipNode);

	}

	protected void _addToOneRelationshipNodeForKeyInObject(ERXKey<?> key, Object obj, EOClassDescription destinationEntity, ERXKeyFilter keyFilter, ERXRestContext context, Set<Object> visitedObjects) {
		Object value = key.valueInObject(obj);
		// if (value != null) {
		ERXRestRequestNode toOneRelationshipNode = new ERXRestRequestNode(keyFilter.keyMap(key).key(), false);
		toOneRelationshipNode._fillInWithObjectAndFilter(value, destinationEntity, keyFilter._filterForKey(key), context, visitedObjects);
		addChild(toOneRelationshipNode);
		// }
	}

	@SuppressWarnings("unchecked")
	protected void _addAttributesAndRelationshipsForObjectOfEntity(Object obj, EOClassDescription classDescription, ERXKeyFilter keyFilter, ERXRestContext context, Set<Object> visitedObjects) {
		// just break out ... no key filter = nothing to do
		if (keyFilter == null) {
			return;
		}
		
		Set<ERXKey> visitedKeys = new HashSet<ERXKey>();
		for (String attributeName : classDescription.attributeKeys()) {
			// if (attribute.isClassProperty()) {
			ERXKey<Object> key = new ERXKey<Object>(attributeName);
			if (keyFilter.matches(key, ERXKey.Type.Attribute)) {
				_addAttributeNodeForKeyInObject(key, obj, keyFilter);
				visitedKeys.add(key);
			}
			// }
		}

		for (String relationshipName : classDescription.toOneRelationshipKeys()) {
			// if (relationship.isClassProperty()) {
			ERXKey<Object> key = new ERXKey<Object>(relationshipName);
			if (keyFilter.matches(key, ERXKey.Type.ToOneRelationship)) {
				_addToOneRelationshipNodeForKeyInObject(key, obj, classDescription.classDescriptionForDestinationKey(relationshipName), keyFilter, context, visitedObjects);
				visitedKeys.add(key);
			}
			// }
		}

		for (String relationshipName : classDescription.toManyRelationshipKeys()) {
			// if (relationship.isClassProperty()) {
			ERXKey<Object> key = new ERXKey<Object>(relationshipName);
			if (keyFilter.matches(key, ERXKey.Type.ToManyRelationship)) {
				_addToManyRelationshipNodeForKeyOfEntityInObject(key, classDescription.classDescriptionForDestinationKey(relationshipName), obj, keyFilter, context, visitedObjects);
				visitedKeys.add(key);
			}
			// }
		}

		Set<ERXKey> includeKeys = keyFilter.includes().keySet();
		if (includeKeys != null && !includeKeys.isEmpty()) {
			Set<ERXKey> remainingKeys = new LinkedHashSet<ERXKey>(includeKeys);
			remainingKeys.removeAll(visitedKeys);
			if (!remainingKeys.isEmpty()) {
				// this is sort of expensive, but we want to support non-eomodel to-many relationships on EO's, so
				// we fallback and lookup the class entity ...
				if (classDescription instanceof EOEntityClassDescription) {
					// EOEntityClassDescription.classDescriptionForEntityName(obj.getClass().getName());
					EOClassDescription nonModelClassDescription = ERXRestClassDescriptionFactory.classDescriptionForObject(obj, true);
					for (ERXKey<?> remainingKey : remainingKeys) {
						String keyName = remainingKey.key();
						if (nonModelClassDescription.attributeKeys().containsObject(keyName)) {
							_addAttributeNodeForKeyInObject(remainingKey, obj, keyFilter);
						}
						else if (nonModelClassDescription.toManyRelationshipKeys().containsObject(keyName)) {
							_addToManyRelationshipNodeForKeyOfEntityInObject(remainingKey, nonModelClassDescription.classDescriptionForDestinationKey(keyName), obj, keyFilter, context, visitedObjects);
						}
						else if (nonModelClassDescription.toOneRelationshipKeys().containsObject(keyName)) {
							_addToOneRelationshipNodeForKeyInObject(remainingKey, obj, nonModelClassDescription.classDescriptionForDestinationKey(keyName), keyFilter, context, visitedObjects);
						}
						else if (nonModelClassDescription instanceof BeanInfoClassDescription && ((BeanInfoClassDescription) nonModelClassDescription).isAttributeMethod(keyName)) {
							_addAttributeNodeForKeyInObject(remainingKey, obj, keyFilter);
						}
						else if (nonModelClassDescription instanceof BeanInfoClassDescription && ((BeanInfoClassDescription) nonModelClassDescription).isToManyMethod(keyName)) {
							_addToManyRelationshipNodeForKeyOfEntityInObject(remainingKey, nonModelClassDescription.classDescriptionForDestinationKey(keyName), obj, keyFilter, context, visitedObjects);
						}
						else if (nonModelClassDescription instanceof BeanInfoClassDescription && ((BeanInfoClassDescription) nonModelClassDescription).isToOneMethod(keyName)) {
							_addToOneRelationshipNodeForKeyInObject(remainingKey, obj, nonModelClassDescription.classDescriptionForDestinationKey(keyName), keyFilter, context, visitedObjects);
						}
						else if (!keyFilter.isUnknownKeyIgnored()) {
							throw new IllegalArgumentException("This key filter specified that the key '" + keyName + "' should be included on '" + nonModelClassDescription.entityName() + "', but it does not exist.");
						}
					}
				}
				else if (classDescription instanceof BeanInfoClassDescription) {
					BeanInfoClassDescription beanInfoClassDescription = (BeanInfoClassDescription) classDescription;
					for (ERXKey<?> remainingKey : remainingKeys) {
						String keyName = remainingKey.key();
						if (beanInfoClassDescription.isAttributeMethod(keyName)) {
							_addAttributeNodeForKeyInObject(remainingKey, obj, keyFilter);
						}
						else if (beanInfoClassDescription.isToManyMethod(keyName)) {
							_addToManyRelationshipNodeForKeyOfEntityInObject(remainingKey, beanInfoClassDescription.classDescriptionForDestinationKey(keyName), obj, keyFilter, context, visitedObjects);
						}
						else if (beanInfoClassDescription.isToOneMethod(keyName)) {
							_addToOneRelationshipNodeForKeyInObject(remainingKey, obj, beanInfoClassDescription.classDescriptionForDestinationKey(keyName), keyFilter, context, visitedObjects);
						}
						else if (!keyFilter.isUnknownKeyIgnored()) {
							throw new IllegalArgumentException("This key filter specified that the key '" + keyName + "' should be included on '" + beanInfoClassDescription.entityName() + "', but it does not exist.");
						}
					}
				}
				else if (!keyFilter.isUnknownKeyIgnored()) {
					throw new IllegalArgumentException("This key filter specified that the keys '" + remainingKeys + "' should be included on '" + classDescription.entityName() + "', but they do not exist.");
				}
			}
		}
	}

	protected void _fillInWithObjectAndFilter(Object obj, EOClassDescription classDescription, ERXKeyFilter keyFilter, ERXRestContext context, Set<Object> visitedObjects) {
		if (obj instanceof List) {
			setAssociatedObject(obj);
			// setAttributeForKey(/* ??? */, ERXRestRequestNode.TYPE_KEY);
			setArray(true);

			for (Object childObj : (List) obj) {
				ERXRestRequestNode childNode = new ERXRestRequestNode(null, false);
				childNode._fillInWithObjectAndFilter(childObj, classDescription, keyFilter, context, visitedObjects);
				addChild(childNode);
			}
		}
		else if (ERXRestUtils.isPrimitive(obj)) {
			if (obj instanceof NSKeyValueCoding.Null) {
				setValue(null);
				setAssociatedObject(null);
			}
			else {
				if (_name == null) {
					_name = classDescription.entityName();
				}
				setValue(obj);
				setAssociatedObject(obj);
			}
		}
		else {
			// in case we have a superclass class description passed in
			if (obj != null) {
				classDescription = ERXRestClassDescriptionFactory.classDescriptionForObject(obj, false);
			}
			if (_name == null) {
				_name = classDescription.entityName();
				_rootNode = true;
			}
			setAssociatedObject(obj);
			setType(classDescription.entityName());
			if (obj != null) {
				Object id = IERXRestDelegate.Factory.delegateForClassDescription(classDescription).primaryKeyForObject(obj, context);
				if (id != null) {
					setID(id);
				}
				if (!visitedObjects.contains(obj) || !keyFilter.isDeduplicationEnabled()) {
					visitedObjects.add(obj);
					_addAttributesAndRelationshipsForObjectOfEntity(obj, classDescription, keyFilter, context, visitedObjects);
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
	public String toString(ERXRestFormat format, ERXRestContext context) {
		return toString(format.writer(), format.delegate(), context);
	}

	/**
	 * Returns a string representation of this request node using the given IERXRestWriter.
	 * 
	 * @param writer
	 *            the writer to use
	 * @return a string representation of this request node using the given IERXRestWriter
	 */
	public String toString(IERXRestWriter writer, ERXRestFormat.Delegate delegate, ERXRestContext context) {
		ERXResponse octopusHair = new ERXResponse();
		writer.appendToResponse(this, new ERXWORestResponse(octopusHair), delegate, context);
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

	protected void _safeWillTakeValueForKey(ERXKeyFilter keyFilter, Object target, Object value, String key) {
		ERXKeyFilter.Delegate delegate = keyFilter.delegate();
		if (delegate != null) {
			delegate.willTakeValueForKey(target, value, key);
		}
	}

	protected void _safeDidTakeValueForKey(ERXKeyFilter keyFilter, Object target, Object value, String key) {
		ERXKeyFilter.Delegate delegate = keyFilter.delegate();
		if (delegate != null) {
			delegate.didTakeValueForKey(target, value, key);
		}
	}

	protected void _safeDidSkipValueForKey(ERXKeyFilter keyFilter, Object target, Object value, String key) {
		ERXKeyFilter.Delegate delegate = keyFilter.delegate();
		if (delegate != null) {
			delegate.didSkipValueForKey(target, value, key);
		}
	}

	/**
	 * Equivalent to updateObjectWithFilter(obj, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
	 * 
	 * @param obj
	 *            the object to update
	 */
	public void updateObject(Object obj) {
		updateObjectWithFilter(obj, ERXKeyFilter.filterWithAllRecursive(), new ERXRestContext());
	}

	/**
	 * Updates the given object based on this request node.
	 * 
	 * @param obj
	 *            the object to update
	 * @param keyFilter
	 *            the filter to use to determine how to update
	 * @param context
	 *            the delegate
	 */
	public void updateObjectWithFilter(Object obj, ERXKeyFilter keyFilter, ERXRestContext context) {
		if (obj == null) {
			return;
		}

		EOClassDescription classDescription = ERXRestClassDescriptionFactory.classDescriptionForObject(obj, false);
		for (Map.Entry<String, Object> attribute : _attributes.entrySet()) {
			ERXKey<Object> key = keyFilter.keyMap(new ERXKey<Object>(attribute.getKey()));
			String keyName = key.key();
			if (keyFilter.matches(key, ERXKey.Type.Attribute) && isClassProperty(classDescription, keyName)) {
				Object value = ERXRestUtils.coerceValueToAttributeType(attribute.getValue(), null, obj, keyName, context);
				if (value instanceof NSKeyValueCoding.Null) {
					value = null;
				}
				_safeWillTakeValueForKey(keyFilter, obj, value, keyName);
				key.takeValueInObject(value, obj);
				_safeDidTakeValueForKey(keyFilter, obj, value, keyName);
			}
			else {
				_safeDidSkipValueForKey(keyFilter, obj, attribute.getValue(), keyName); // MS: we didn't coerce the
				// value .. i think that's ok
			}
		}

		for (ERXRestRequestNode childNode : _children) {
			ERXKey<Object> key = keyFilter.keyMap(new ERXKey<Object>(childNode.name()));
			String keyName = key.key();
			if (isClassProperty(classDescription, keyName)) {
				NSKeyValueCoding._KeyBinding binding = NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(obj, keyName);
				Class<?> valueType = binding.valueType();
				if (valueType == Object.class) {
					if (childNode.isArray()) {
						valueType = NSArray.class;
					}
					else {
						Object childValue = childNode.value();
						if (childValue != null) {
							valueType = childValue.getClass();
						}
					}
				}

				if (List.class.isAssignableFrom(valueType) && keyFilter.matches(key, ERXKey.Type.ToManyRelationship)) {
					EOClassDescription destinationClassDescription;
					// this is sort of expensive, but we want to support non-eomodel to-many relationships on EO's, so
					// we fallback and lookup the class entity ...
					if (!classDescription.toManyRelationshipKeys().containsObject(keyName) && classDescription instanceof EOEntityClassDescription) {
						EOClassDescription nonModelClassDescription = ERXRestClassDescriptionFactory.classDescriptionForObject(obj, true);
						if (!nonModelClassDescription.toManyRelationshipKeys().containsObject(keyName)) {
							throw new IllegalArgumentException("There is no to-many relationship named '" + key.key() + "' on '" + classDescription.entityName() + "'.");
						}
						destinationClassDescription = classDescription.classDescriptionForDestinationKey(keyName);
					}
					else {
						destinationClassDescription = classDescription.classDescriptionForDestinationKey(keyName);
					}
					if (destinationClassDescription == null) {
						if (keyFilter.isUnknownKeyIgnored()) {
							continue;
						}
						else {
							throw new NSKeyValueCoding.UnknownKeyException("There is no key '" + keyName + "' on this object.", obj, keyName);
						}
					}
					boolean lockedRelationship = keyFilter.lockedRelationship(key);

					@SuppressWarnings("unchecked")
					List<Object> existingValues = (List<Object>) NSKeyValueCoding.DefaultImplementation.valueForKey(obj, keyName);

					Set<Object> removedValues;
					if (existingValues == null) {
						removedValues = new HashSet<Object>();
					}
					else {
						removedValues = new HashSet<Object>(existingValues);
					}
					List<Object> newValues = new LinkedList<Object>();
					List<Object> allValues = new LinkedList<Object>();
					for (ERXRestRequestNode toManyNode : childNode.children()) {
						Object id = toManyNode.id();

						if (toManyNode.type() != null) {
							destinationClassDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(toManyNode.type());
						}

						Object childObj;
						if (toManyNode.children().count() == 0 && ERXRestUtils.isPrimitive(toManyNode.value())) {
							if (lockedRelationship) {
								childObj = null;
							}
							else {
								if (toManyNode.value() != null) {
									childObj = toManyNode.value();
								} else {
									childObj = IERXRestDelegate.Factory.delegateForClassDescription(destinationClassDescription).objectOfEntityWithID(destinationClassDescription, id, context);
								}
							}
						}
						else if (id == null) {
							if (lockedRelationship) {
								childObj = null;
							}
							else {
								childObj = IERXRestDelegate.Factory.delegateForClassDescription(destinationClassDescription).createObjectOfEntityWithID(destinationClassDescription, id, context);
							}
						}
						else {
							childObj = IERXRestDelegate.Factory.delegateForClassDescription(destinationClassDescription).objectOfEntityWithID(destinationClassDescription, id, context);
						}

						if (childObj != null) {
							boolean newMemberOfRelationship = existingValues == null || !existingValues.contains(childObj);
							if (newMemberOfRelationship) {
								if (!lockedRelationship) {
									toManyNode.updateObjectWithFilter(childObj, keyFilter._filterForKey(key), context);
									newValues.add(childObj);
									allValues.add(childObj);
								}
							}
							else {
								toManyNode.updateObjectWithFilter(childObj, keyFilter._filterForKey(key), context);
								allValues.add(childObj);
							}
							removedValues.remove(childObj);
						}
					}

					if (!lockedRelationship) {
						_safeWillTakeValueForKey(keyFilter, obj, allValues, keyName);
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
						_safeDidTakeValueForKey(keyFilter, obj, allValues, keyName);
					}
					else {
						_safeDidSkipValueForKey(keyFilter, obj, allValues, keyName);
					}
				}
				else if (!ERXRestUtils.isPrimitive(valueType) && keyFilter.matches(key, ERXKey.Type.ToOneRelationship)) {
					EOClassDescription destinationClassDescription;
					// this is sort of expensive, but we want to support non-eomodel to-one relationships on EO's, so
					// we fallback and lookup the class entity ...
					if (!classDescription.toOneRelationshipKeys().containsObject(keyName) && classDescription instanceof EOEntityClassDescription) {
						EOClassDescription nonModelClassDescription = ERXRestClassDescriptionFactory.classDescriptionForObject(obj, true);
						if (!nonModelClassDescription.toOneRelationshipKeys().containsObject(keyName)) {
							throw new IllegalArgumentException("There is no to-one relationship named '" + key.key() + "' on '" + classDescription.entityName() + "'.");
						}
						destinationClassDescription = nonModelClassDescription.classDescriptionForDestinationKey(keyName);
					}
					else {
						destinationClassDescription = classDescription.classDescriptionForDestinationKey(keyName);
					}
					if (destinationClassDescription == null) {
						if (keyFilter.isUnknownKeyIgnored()) {
							continue;
						}
						else {
							throw new NSKeyValueCoding.UnknownKeyException("There is no key '" + keyName + "' on this object.", obj, keyName);
						}
					}
					boolean lockedRelationship = keyFilter.lockedRelationship(key);

					if (childNode.isArray()) {
						throw new IllegalArgumentException("You attempted to pass an array of values for the key '" + key + "'.");
					}

					if (childNode.isNull()) {
						Object previousChildObj = NSKeyValueCoding.DefaultImplementation.valueForKey(obj, keyName);
						if (previousChildObj != null && !lockedRelationship) {
							_safeWillTakeValueForKey(keyFilter, obj, null, keyName);
							if (obj instanceof EOEnterpriseObject && previousChildObj instanceof EOEnterpriseObject) {
								((EOEnterpriseObject) obj).removeObjectFromBothSidesOfRelationshipWithKey((EOEnterpriseObject) previousChildObj, keyName);
							}
							else {
								key.takeValueInObject(null, obj);
							}
							_safeDidTakeValueForKey(keyFilter, obj, null, keyName);
						}
						else if (lockedRelationship) {
							_safeDidSkipValueForKey(keyFilter, obj, null, keyName);
						}
					}
					else {
						Object id = childNode.id();

						ERXKeyFilter childKeyFilter = keyFilter._filterForKey(key);
						Object childObj;

						if (childNode.type() != null) {
							destinationClassDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(childNode.type());
						}

						if (id == null) {
							if (lockedRelationship) {
								childObj = null;
							}
							else if (childKeyFilter.isAnonymousUpdateEnabled()) {
								childObj = NSKeyValueCoding.DefaultImplementation.valueForKey(obj, keyName);
								if (childObj == null) {
									childObj = IERXRestDelegate.Factory.delegateForClassDescription(destinationClassDescription).createObjectOfEntityWithID(destinationClassDescription, null, context);
								}
							}
							else {
								childObj = IERXRestDelegate.Factory.delegateForClassDescription(destinationClassDescription).createObjectOfEntityWithID(destinationClassDescription, null, context);
							}
						}
						else if ("_".equals(id)) {
							childObj = NSKeyValueCoding.DefaultImplementation.valueForKey(obj, keyName);
							if (!lockedRelationship && childObj == null) {
								childObj = IERXRestDelegate.Factory.delegateForClassDescription(destinationClassDescription).createObjectOfEntityWithID(destinationClassDescription, null, context);
							}
						}
						else {
							childObj = IERXRestDelegate.Factory.delegateForClassDescription(destinationClassDescription).objectOfEntityWithID(destinationClassDescription, id, context);
						}

						boolean updateChildObj;
						if (childObj == null) {
							updateChildObj = false;
						}
						else if (lockedRelationship) {
							Object previousChildObj = NSKeyValueCoding.DefaultImplementation.valueForKey(obj, keyName);
							updateChildObj = previousChildObj != null && previousChildObj.equals(childObj);
						}
						else {
							updateChildObj = true;
						}

						if (updateChildObj) {
							childNode.updateObjectWithFilter(childObj, childKeyFilter, context);
							if (!lockedRelationship) {
								_safeWillTakeValueForKey(keyFilter, obj, childObj, keyName);
								if (obj instanceof EOEnterpriseObject && childObj instanceof EOEnterpriseObject) {
									((EOEnterpriseObject) obj).addObjectToBothSidesOfRelationshipWithKey((EOEnterpriseObject) childObj, keyName);
								}
								else {
									key.takeValueInObject(childObj, obj);
								}
								_safeDidTakeValueForKey(keyFilter, obj, childObj, keyName);
							}
							else {
								_safeDidSkipValueForKey(keyFilter, obj, childObj, keyName);
							}
						}
					}
				}
				else if (/* entity.attributeNamed(keyName) != null && */ERXRestUtils.isPrimitive(valueType) && keyFilter.matches(key, ERXKey.Type.Attribute)) {
					Object value = childNode.value();
					if (value instanceof String) {
						value = ERXRestUtils.coerceValueToAttributeType(value, null, obj, keyName, context);
					}
					if (value instanceof NSKeyValueCoding.Null) {
						value = null;
					}
					_safeWillTakeValueForKey(keyFilter, obj, value, keyName);
					try {
						key.takeValueInObject(value, obj);
					}
					catch (NSKeyValueCoding.UnknownKeyException e) {
						if (!keyFilter.isUnknownKeyIgnored()) {
							throw e;
						}
					}
					_safeDidTakeValueForKey(keyFilter, obj, value, keyName);
				}
				else {
					// ignore key
					_safeDidSkipValueForKey(keyFilter, obj, childNode, keyName); // MS: what is the value here? i'm just
					// hanging in the node ...
				}
			}
		}
	}

	// MS: Totally debatable .... I may take this back out, but it makes things look prettier.
	public void _removeRedundantTypes() {
		String type = type();
		if ("NSDictionary".equals(type) || "NSMutableDictionary".equals(type) || "HashMap".equals(type)) {
			setType(null);
		}
		NSArray<ERXRestRequestNode> children = children();
		if (children != null) {
			for (ERXRestRequestNode child : children) {
				child._removeRedundantTypes();
			}
		}
	}
    
	/**
	 * Creates a hierarchy of ERXRestRequestNodes based off of the given array of objects.
	 * 
	 * @param classDescription
	 *            the entity type of the objects in the array
	 * @param objects
	 *            the array to turn into request nodes
	 * @param keyFilter
	 *            the filter to use
	 * @return the root ERXRestRequestNode
	 */
	public static ERXRestRequestNode requestNodeWithObjectAndFilter(EOClassDescription classDescription, List<?> objects, ERXKeyFilter keyFilter, ERXRestContext context) {
		ERXRestRequestNode requestNode = new ERXRestRequestNode(null, true);
		if (classDescription != null) {
			String entityName = classDescription.entityName();
			requestNode = new ERXRestRequestNode(entityName, true);
			requestNode.setType(entityName);
		}
		requestNode._fillInWithObjectAndFilter(objects, classDescription, keyFilter, context, new HashSet<Object>());
		return requestNode;
	}

	/**
	 * Creates a hierarchy of ERXRestRequestNodes based off of the given object.
	 * 
	 * @param obj
	 *            the object to turn into request nodes
	 * @param keyFilter
	 *            the filter to use
	 * @return the root ERXRestRequestNode
	 */
	public static ERXRestRequestNode requestNodeWithObjectAndFilter(Object obj, ERXKeyFilter keyFilter, ERXRestContext context) {
		String shortName = null;
		EOClassDescription classDescription = null;
		if (obj != null) {
			classDescription = ERXRestClassDescriptionFactory.classDescriptionForObject(obj, false);
			shortName = classDescription.entityName();
		}
		ERXRestRequestNode requestNode = new ERXRestRequestNode(shortName, true);
		if (ERXRestUtils.isPrimitive(obj)) {
			requestNode.setValue(obj);
		}
		else {
			if (!(obj instanceof List)) {
				requestNode.setType(shortName);
			}
			requestNode._fillInWithObjectAndFilter(obj, classDescription, keyFilter, context, new HashSet<Object>());
		}
		return requestNode;
	}
}
