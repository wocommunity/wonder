package er.rest;

import java.util.Enumeration;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * ERXRestRequestNode provides a model of a REST request.  Because the incoming document
 * format can vary (XML, JSON, etc), we needed a document model that is more abstract
 * than just an org.w3c.dom.  Or, rather, one that isn't obnoxious to use.
 * 
 * @author mschrag
 */
public class ERXRestRequestNode {
	private String _name;
	private String _value;
	private NSMutableDictionary _attributes;
	private NSMutableArray _children;

	/**
	 * Construct a node with the given name
	 * 
	 * @param name the name of this node
	 */
	public ERXRestRequestNode(String name) {
		_name = name;
		_attributes = new NSMutableDictionary();
		_children = new NSMutableArray();
	}
	
	/**
	 * Returns the first child named 'name'.
	 * 
	 * @param name the name to look for
	 * @return the first child with this name (or null if not found)
	 */
	public ERXRestRequestNode childNamed(String name) {
		ERXRestRequestNode matchingChildNode = null;
		Enumeration childrenEnum = _children.objectEnumerator();
		while (matchingChildNode == null && childrenEnum.hasMoreElements()) {
			ERXRestRequestNode childNode = (ERXRestRequestNode)childrenEnum.nextElement();
			if (name.equals(childNode.name())) {
				matchingChildNode = childNode;
			}
		}
		return matchingChildNode;
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
	public String value() {
		return _value;
	}

	/**
	 * Sets the value for this node.
	 * 
	 * @param value the value for this node
	 */
	public void setValue(String value) {
		_value = value;
	}

	/**
	 * Sets the attribute value for the given key.
	 * 
	 * @param attribute the attribute value
	 * @param key the key
	 */
	public void setAttributeForKey(String attribute, String key) {
		_attributes.setObjectForKey(attribute, key);
	}

	/**
	 * Returns the attribute value for the given key.
	 * 
	 * @param key the key
	 * @return the attribute value
	 */
	public String attributeForKey(String key) {
		return (String) _attributes.objectForKey(key);
	}

	/**
	 * Returns the attributes dictionary for this node.
	 * 
	 * @return the attributes dictionary
	 */
	public NSDictionary attributes() {
		return _attributes;
	}

	/**
	 * Adds a child to this node.
	 * 
	 * @param child the child to add
	 */
	public void addChild(ERXRestRequestNode child) {
		_children.addObject(child);
	}

	/**
	 * Returns the children of this node.
	 * 
	 * @return the children of this node
	 */
	public NSArray children() {
		return _children;
	}
}
