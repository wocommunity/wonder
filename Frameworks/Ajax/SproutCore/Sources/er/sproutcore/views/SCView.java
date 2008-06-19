package er.sproutcore.views;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxUtils;
import er.extensions.foundation.ERXStringUtilities;
import er.sproutcore.SCItem;

/**
 * Superclass for a SC view.
 * @author ak
 *
 */
public class SCView extends WODynamicGroup {
    protected Logger log = Logger.getLogger(getClass());

    private NSMutableDictionary<String, WOAssociation> _associations;

    private NSMutableDictionary<String, WOAssociation> _bindings;

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
        return "SC." +  ERXStringUtilities.lastPropertyKeyInKeyPath(getClass().getName()).replaceAll("^SC", "");
    }

    public String id(WOContext context) {
        return (String) valueForBinding("id", context.component());
    }

    @Override
    public final void appendToResponse(WOResponse response, WOContext context) {
        SCItem item = SCItem.pushItem(id(context), className(context));
        for (String key : _bindings.allKeys()) {
            Object value = _bindings.objectForKey(key).valueInComponent(context.component());
            item.addBinding(key, value == null ? NSKeyValueCoding.NullValue : value);
        }
        for (String key : _associations.allKeys()) {
            Object value = _associations.objectForKey(key).valueInComponent(context.component());
            item.addProperty(key, value == null ? NSKeyValueCoding.NullValue : value);
        }
        doAppendToResponse(response, context);
        SCItem.popItem();
    }
}
