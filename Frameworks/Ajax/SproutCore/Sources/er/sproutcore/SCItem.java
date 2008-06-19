/**
 * 
 */
package er.sproutcore;

import java.util.Stack;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXThreadStorage;

/**
 * Contains the actual binding data for one view.
 * @author ak
 *
 */
public class SCItem {

    private static int idx = 0;

    private String _id;

    private String _indent = "    ";

    private String _className;

    private SCItem _parent;

    private NSMutableDictionary<String, Object> _outlets = new NSMutableDictionary<String, Object>();

    private NSMutableDictionary<String, Object> _properties = new NSMutableDictionary<String, Object>();

    private NSMutableDictionary<String, Object> _bindings = new NSMutableDictionary<String, Object>();

    private NSMutableArray<SCItem> _children = new NSMutableArray<SCItem>();

    public SCItem(SCItem parent, String className, String id) {
        _id = (id == null ? "id_" + (nextId()) + "" : id);
        _parent = parent;
        _className = className;
        if (_parent != null) {
            _parent.addChild(this);
        }
        while(parent != null) {
            _indent +="    ";
            parent = parent._parent;
        }
    }

    private synchronized int nextId() {
        return idx++;
    }
    
    public String id() {
        return _id;
    }
    
    public String outlet() {
        return "\"." + _id + "?\"";
    }

    public void addProperty(String key, Object value) {
        _properties.setObjectForKey(value, key);
    }

    public void addOutlet(String key, Object value) {
        _outlets.setObjectForKey(value, key);
    }

    public void addBinding(String key, Object value) {
        _bindings.setObjectForKey(value, key);
    }

    public void addChild(SCItem context) {
        _children.addObject(context);
    }
    
    private String bindingsJavaScript() {
        String result = "";
        if(_bindings.count() > 0) {
            for (String key : _bindings.allKeys()) {
                Object value = _bindings.objectForKey(key);
                value = quotedValue(key, value);
                result += _indent + key + "Binding: " +  value + ",\n";
            }
        }
        return result;
    }
    
    private String outletJavaScript() {
        String result = "";
        if(_children.count() > 0) {
            result += _indent +  "outlets: [";
            for (SCItem item : _children) {
                if(item != _children.objectAtIndex(0)) {
                    result += ",";
                }
                result += "\"" + item.id() + "\"";
            }
            result += "],\n";
            for (SCItem item : _children) {
                result += _indent + "\"" + item.id() + "\": " + item + ",\n";
            }
            //result = result.substring(0, result.length() - 2);
        }
        return result;
    }
    
    private String propertyJavaScript() {
        String result = "";
        if(_properties.count() > 0) {
            for (String key : _properties.allKeys()) {
                Object value = _properties.objectForKey(key);
                value = quotedValue(key, value);
                result += _indent + key + ": " +  value + ",\n";
            }
        }
        return result;
    }

    protected Object quotedValue(String key, Object value) {
        if(value != null && !value.toString().startsWith("SC.Binding")) {
            if(value == null || value == NSKeyValueCoding.NullValue) {
                return null;
            }
            if(value instanceof Boolean || value instanceof Number) {
                return value;
            }
            
            value = "\"" +  value + "\"";
        }
        return value;
    }

    public String toString() {
        boolean isPage = _className.equals("SC.Page");
        String core = "({\n" + outletJavaScript() + bindingsJavaScript() + propertyJavaScript() + _indent.substring(4) +  "})";
        if(isPage) {
            return _className + ".create" + core;
        }
        return  _className + ".extend" + core + ".outletFor("+ outlet() +")";
    }

    public static SCItem pageItem() {
        SCItem context = (SCItem) ERXThreadStorage.valueForKey("SCView.PageItem");
        if (context == null) {
            context = new SCItem(null, "SC.Page", "SCPage");
            ERXThreadStorage.takeValueForKey(context, "SCView.PageItem");
        }
        return context;
    }

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

    public static SCItem pushItem(String id, String className) {
        Stack<SCItem> stack = currentItems();
        SCItem parent = stack.peek();
        SCItem current = new SCItem(parent, className, id);
        stack.push(current);
        return current;
    }

    public static SCItem popItem() {
        Stack<SCItem> stack = currentItems();
        SCItem current = stack.pop();
        return stack.peek();
    }
}