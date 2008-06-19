package er.sproutcore.views;

import java.util.Enumeration;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXThreadStorage;

public class SCView extends WODynamicGroup {
    protected Logger log = Logger.getLogger(getClass());

    private NSMutableDictionary<String, WOAssociation> _associations;

    private NSMutableDictionary<String, WOAssociation> _bindings;

    private static int idx = 0;

    public static class Item {

        private String _id;
        
        private String _className;

        private Item _parent;

        private NSMutableDictionary<String, Object> _outlets = new NSMutableDictionary<String, Object>();

        private NSMutableDictionary<String, Object> _properties = new NSMutableDictionary<String, Object>();

        private NSMutableDictionary<String, Object> _bindings = new NSMutableDictionary<String, Object>();

        private NSMutableArray<Item> _children = new NSMutableArray<Item>();

        public Item(Item parent, String className, String id) {
            _id = (id == null ? "id_" + (nextId()) + "" : id);
            _parent = parent;
            _className = className;
            if (_parent != null) {
                _parent.addChild(this);
            }
        }

        private synchronized int nextId() {
            return idx++;
        }
        
        public String id() {
            return _id;
        }
        
        public String outlet() {
            return "\"." + _id + "\"";
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

        public void addChild(Item context) {
            _children.addObject(context);
        }
        
        private String bindingsJavaScript() {
            String result = "";
            if(_bindings.count() > 0) {
                for (String key : _bindings.allKeys()) {
                    Object value = _bindings.objectForKey(key);
                    result += key + "Binding: " +  value + ",\n";
                }
            }
            return result;
        }
        
        private String outletJavaScript() {
            String result = "";
            if(_children.count() > 0) {
                result += "outlets: [";
                for (Item item : _children) {
                    if(item != _children.objectAtIndex(0)) {
                        result += ",";
                    }
                    result += "\"" + item.id() + "\"";
                }
                result += "],\n";
                for (Item item : _children) {
                    result += "\"" + item.id() + "\": " + item + ",\n";
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
                    result += key + ": " +  value + ",\n";
                }
            }
            return result;
        }

        public String toString() {
            boolean isPage = _className.equals("SC.Page");
            String core = "({\n" + outletJavaScript() + bindingsJavaScript() + propertyJavaScript() + "})";
            return  _className + "." + (isPage ? "create" : "extend") + core + (isPage ?"": ".outletFor("+ outlet() +")");
        }
    }

    public SCView(String name, NSDictionary associations, WOElement parent) {
        super(name, associations, parent);
        _associations = new NSMutableDictionary<String, WOAssociation>();
        _bindings = new NSMutableDictionary<String, WOAssociation>();
        for (Enumeration e = associations.keyEnumerator(); e.hasMoreElements();) {
            String element = (String) e.nextElement();
            if (element.charAt(0) == '?') {
                _bindings.setObjectForKey((WOAssociation) associations.objectForKey(element), element.substring(1));
            } else {
                _associations.setObjectForKey((WOAssociation) associations.objectForKey(element), element);
            }
        }
    }
    
    public NSDictionary<String, WOAssociation> associations() {
        return _associations;
    }

    public Object valueForBinding(String name, Object defaultValue, WOComponent component) {
        return AjaxUtils.valueForBinding(name, defaultValue, associations(), component);
    }

    public Object valueForBinding(String name, WOComponent component) {
        return AjaxUtils.valueForBinding(name, associations(), component);
    }

    public boolean booleanValueForBinding(String name, boolean defaultValue, WOComponent component) {
        return AjaxUtils.booleanValueForBinding(name, defaultValue, associations(), component);
    }

    protected void doAppendToResponse(WOResponse arg0, WOContext arg1) {
        super.appendToResponse(arg0, arg1);
    }

    public String className(WOContext context) {
        return "SC.View";
    }

    public String id(WOContext context) {
        return (String) valueForBinding("id", context.component());
    }

    @Override
    public final void appendToResponse(WOResponse response, WOContext context) {
        Item item = pushItem(id(context), className(context));
        for (String key : _bindings.allKeys()) {
            Object value = _bindings.objectForKey(key).valueInComponent(context.component());
            item.addBinding(key, value == null ? "null" : value);
        }
        for (String key : _associations.allKeys()) {
            Object value = _associations.objectForKey(key).valueInComponent(context.component());
            item.addProperty(key, value == null ? "null" : value);
        }
        doAppendToResponse(response, context);
        popItem();
    }

    public static Item pageItem() {
        Item context = (Item) ERXThreadStorage.valueForKey("SCView.PageItem");
        if (context == null) {
            context = new Item(null, "SC.Page", "SCPage");
            ERXThreadStorage.takeValueForKey(context, "SCView.PageItem");
        }
        return context;
    }

    protected static Stack<Item> currentItems() {
        Stack<Item> context = (Stack<Item>) ERXThreadStorage.valueForKey("SCView.CurrentItem");
        if (context == null) {
            context = new Stack<Item>();
            context.push(pageItem());
            ERXThreadStorage.takeValueForKey(context, "SCView.CurrentItem");
        }
        return context;
    }

    protected static Item currentItem() {
        return currentItems().peek();
    }

    protected static Item pushItem(String id, String className) {
        Stack<Item> stack = currentItems();
        Item parent = stack.peek();
        Item current = new Item(parent, className, id);
        stack.push(current);
        return current;
    }

    public static Item popItem() {
        Stack<Item> stack = currentItems();
        Item current = stack.pop();
        return stack.peek();
    }
}
