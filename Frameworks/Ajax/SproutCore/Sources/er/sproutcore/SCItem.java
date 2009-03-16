/**
 * 
 */
package er.sproutcore;

import java.util.Stack;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXStringUtilities;
import er.extensions.foundation.ERXThreadStorage;
import er.sproutcore.views.SCBinding;
import er.sproutcore.views.SCProperty;

/**
 * Contains the actual binding data for one view.
 * 
 * @author ak
 * 
 */
public class SCItem {

	private static long idx = (System.currentTimeMillis() / 1000);

	private String _id;

	private String _outletName;

	private String _indent = "    ";

	private String _className;

	private SCItem _parent;

	private NSMutableDictionary<String, Object> _outlets = new NSMutableDictionary<String, Object>();

	private NSMutableDictionary<SCProperty, Object> _properties = new NSMutableDictionary<SCProperty, Object>();

	private NSMutableDictionary<SCBinding, Object> _bindings = new NSMutableDictionary<SCBinding, Object>();

	private NSMutableArray<SCItem> _children = new NSMutableArray<SCItem>();

	public SCItem(SCItem parent, String className, String id, String outletName) {
		_id = (id == null ? "id_" + (nextId()) + "" : id);
		_outletName = (outletName == null) ? _id : outletName;
		_parent = parent;
		_className = className;
		if (_parent != null) {
			_parent.addChild(this);
		}
		while (parent != null) {
			_indent += "    ";
			parent = parent._parent;
		}
	}

	public synchronized static long nextId() {
		return idx++;
	}

	public String id() {
		return _id;
	}

	public boolean isRoot() {
		return _parent != null && _parent._parent == null;
	}

	public String itemOutletName() {
		return ERXStringUtilities.underscoreToCamelCase(outletName(), false);
	}

	public String outletName() {
		return _outletName;
	}

	public String outlet() {
		if (isRoot()) {
			return "\"#" + _id + "\"";
		}
		return "\"." + _id + "?\"";
	}

	public String itemId() {
		return ERXStringUtilities.underscoreToCamelCase(id(), false);
	}

	public void addProperty(SCProperty property, Object value) {
		if (value == null) {
			value = NSKeyValueCoding.NullValue;
		}
		_properties.setObjectForKey(value, property);
	}

	public void addOutlet(String key, Object value) {
		_outlets.setObjectForKey(value, key);
	}

	public void addBinding(SCBinding binding, Object value) {
		if (value == null) {
			value = NSKeyValueCoding.NullValue;
		}
		_bindings.setObjectForKey(value, binding);
	}

	public void addChild(SCItem context) {
		_children.addObject(context);
	}

	private String bindingsJavaScript() {
		String result = "";
		if (_bindings.count() > 0) {
			// result += _indent + "// bindings" + "\n";
			result += "\n";
			for (SCBinding binding : _bindings.allKeys()) {
				Object value = _bindings.objectForKey(binding);
				if ((value != null && value != NSKeyValueCoding.NullValue) || !binding.skipIfNull()) {
					value = binding.javascriptValue(value);
					result += _indent + ERXStringUtilities.underscoreToCamelCase(binding.name(), false) + "Binding: " + value + ",\n";
				}
			}
		}
		return result;
	}

	private String outletJavaScript() {
		String result = "";
		if (_children.count() > 0) {
			if (_parent != null) {
				result += _indent + "outlets: [";
				for (SCItem item : _children) {
					if (item != _children.objectAtIndex(0)) {
						result += ",";
					}
					result += "\"" + item.itemOutletName() + "\"";
				}
				result += "],\n";
			}
			for (SCItem item : _children) {
				result += "\n" + _indent + item.itemOutletName() + ": " + item + ",\n";
			}
			// result = result.substring(0, result.length() - 2);
		}
		return result;
	}

	private String propertyJavaScript() {
		String result = "";
		if (_properties.count() > 0) {
			// result += _indent + "// properties" + "\n";
			result += "\n";
			for (SCProperty property : _properties.allKeys()) {
				Object value = _properties.objectForKey(property);
				if ((value != null && value != NSKeyValueCoding.NullValue) || !property.skipIfNull()) {
					Object jsValue = property.javascriptValue(value);
					result += _indent + ERXStringUtilities.underscoreToCamelCase(property.name(), false) + ": " + jsValue + ",\n";
				}
			}
			// result = result.substring(0, result.length() - 2) + "\n";
		}
		return result;
	}

	@Override
	public String toString() {
		boolean isPage = (_className != null && _className.equals("SC.Page"));
		String script = outletJavaScript() + bindingsJavaScript() + propertyJavaScript();
		script = script.replaceAll(",\n$", "\n");
		String core = "({\n" + script + _indent.substring(4) + "})";
		if (isPage) {
			return _className + ".create" + core;
		}
		return _className + ".extend" + core + ".outletFor(" + outlet() + ")";
	}

	public static SCItem pageItem() {
		SCItem context = (SCItem) ERXThreadStorage.valueForKey("SCView.PageItem");
		if (context == null) {
			context = new SCItem(null, "SC.Page", "SCPage", null);
			ERXThreadStorage.takeValueForKey(context, "SCView.PageItem");
		}
		return context;
	}

	@SuppressWarnings("unchecked")
	protected static Stack<SCItem> currentItems() {
		Stack<SCItem> context = (Stack<SCItem>) ERXThreadStorage.valueForKey("SCView.CurrentItem");
		if (context == null) {
			context = new Stack<SCItem>();
			context.push(pageItem());
			ERXThreadStorage.takeValueForKey(context, "SCView.CurrentItem");
		}
		return context;
	}

	public static SCItem currentItem() {
		return currentItems().peek();
	}

	public static SCItem pushItem(String id, String className, String outletName, boolean toRoot) {
		Stack<SCItem> stack = currentItems();
		SCItem parent = stack.peek();
		if (toRoot) {
			parent = pageItem();
		}
		SCItem current = new SCItem(parent, className, id, outletName);
		stack.push(current);
		return current;
	}

	public static SCItem popItem() {
		Stack<SCItem> stack = currentItems();
		SCItem current = stack.pop();
		return stack.peek();
	}
}