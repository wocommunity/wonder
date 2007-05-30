package er.rest;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class ERXRestRequestNode {
	private String _name;
	private String _value;
	private NSMutableDictionary _attributes;
	private NSMutableArray _children;

	public ERXRestRequestNode(String name) {
		_name = name;
		_attributes = new NSMutableDictionary();
		_children = new NSMutableArray();
	}

	public String name() {
		return _name;
	}

	public String value() {
		return _value;
	}

	public void setValue(String value) {
		_value = value;
	}

	public void setAttributeForKey(String attribute, String key) {
		_attributes.setObjectForKey(attribute, key);
	}

	public String attributeForKey(String key) {
		return (String) _attributes.objectForKey(key);
	}

	public NSDictionary attributes() {
		return _attributes;
	}

	public void addChild(ERXRestRequestNode child) {
		_children.addObject(child);
	}

	public NSArray children() {
		return _children;
	}
}
