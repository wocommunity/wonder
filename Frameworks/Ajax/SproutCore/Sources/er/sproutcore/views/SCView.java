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

    private WOElement _children;

    private NSMutableDictionary<String, WOAssociation> _associations;

    private NSMutableDictionary<String, WOAssociation> _bindings;

    private static int idx = 0;

    public static class Item {

        private String _id;

        private Item _parent;

        private NSMutableDictionary<String, Object> _outlets = new NSMutableDictionary<String, Object>();

        private NSMutableDictionary<String, Object> _properties = new NSMutableDictionary<String, Object>();

        private NSMutableDictionary<String, Object> _bindings = new NSMutableDictionary<String, Object>();

        private NSMutableArray<Item> _children = new NSMutableArray<Item>();

        public Item(Item parent, String id) {
            _id = (id == null ? "id_" + (idx) + "" : id);
            _parent = parent;
            if (_parent != null) {
                _parent.addChild(this);
            }
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

        public String toString() {
            return _id + ": (" + _children.componentsJoinedByString(", ") + ")";
        }
    }

    public SCView(String arg0, NSDictionary arg1, WOElement arg2) {
        super(arg0, arg1, arg2);
        _associations = new NSMutableDictionary<String, WOAssociation>();
        _bindings = new NSMutableDictionary<String, WOAssociation>();
        for (Enumeration e = arg1.keyEnumerator(); e.hasMoreElements();) {
            String element = (String) e.nextElement();
            if (element.charAt(0) == '?') {
                _bindings.setObjectForKey((WOAssociation) arg1.objectForKey(element), element.substring(1));
            } else {
                _associations.setObjectForKey((WOAssociation) arg1.objectForKey(element), element);
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

    public String id(WOContext context) {
        synchronized (SCView.class) {
            // AK: DO NOT CALL TWICE for the same component
            return (String) valueForBinding("id", "id_" + (idx++) + "", context.component());
        }
    }

    @Override
    public final void appendToResponse(WOResponse response, WOContext context) {
        Item item = pushItem(id(context));
        doAppendToResponse(response, context);
        popItem();
    }

    public static Item pageItem() {
        Item context = (Item) ERXThreadStorage.valueForKey("SCView.PageItem");
        if (context == null) {
            context = new Item(null, "SCPage");
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

    protected static Item pushItem(String id) {
        Stack<Item> stack = currentItems();
        Item parent = stack.peek();
        Item current = new Item(parent, id);
        stack.push(current);
        return current;
    }

    protected static Item popItem() {
        Stack<Item> stack = currentItems();
        Item current = stack.pop();
        return stack.peek();
    }
}
