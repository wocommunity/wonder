package er.sproutcore.views;

import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import er.extensions.appserver.ERXResponse;
import er.extensions.foundation.ERXStringUtilities;
import er.sproutcore.SCItem;
import er.sproutcore.SCJavaScript;
import er.sproutcore.SCPageTemplate;
import er.sproutcore.SCUtilities;

/**
 * Superclass for a SC view.
 * 
 * @author ak
 * 
 */
public class SCView extends WODynamicGroup {
    protected Logger log = Logger.getLogger(getClass());

    private NSMutableDictionary<String, WOAssociation> _associations;

    private NSMutableDictionary<String, WOAssociation> _properties;

    private NSMutableDictionary<String, WOAssociation> _bindings;

    private static NSArray BINDABLE_KEYS = new NSArray(new String[] { "isEnabled", "isVisible", });

    private static NSArray PROPERTY_KEYS = new NSArray(new String[] { "isEnabled", "isVisible", "hasCustomPanelWrapper", "localize", "validator", "fieldLabel",
            "acceptsFirstResponder", "content", "value", "maxThickness", "minThickness", "canCollapse", "isCollapsed", "delegate", "isDropTarget", "paneType", "view", "animate",
            "visibleAnimation", "style", "class", });

    public SCView(String name, NSDictionary associations, WOElement parent) {
        super(name, associations, parent);
        _associations = new NSMutableDictionary<String, WOAssociation>();
        _bindings = new NSMutableDictionary<String, WOAssociation>();
        _properties = new NSMutableDictionary<String, WOAssociation>();

        for (Enumeration e = associations.keyEnumerator(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            WOAssociation association = (WOAssociation) associations.objectForKey(key);
            if (key.charAt(0) == '?') {
                _bindings.setObjectForKey(association, key.substring(1));
            } else {
                _properties.setObjectForKey(association, key);
            }
            log.debug(key + ": " + association);
            _associations.setObjectForKey(association, key);
        }
        _properties.removeObjectForKey("id");
        _properties.removeObjectForKey("default");
        _properties.removeObjectForKey("className");
        _properties.removeObjectForKey("class");
        _properties.removeObjectForKey("elementName");
        _properties.removeObjectForKey("style");
        _properties.removeObjectForKey("outlet");
        _properties.removeObjectForKey("view");
    }

    public NSArray propertyKeys() {
        return PROPERTY_KEYS;
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
       String defaultName = "SC." + ERXStringUtilities.lastPropertyKeyInKeyPath(getClass().getName()).replaceAll("^SC", "");
       return (String) valueForBinding("view", defaultName, context.component());
    }

    public String cssName(WOContext context) {
        if (getClass() == SCView.class)
            return "";
        // this is just the default... it morphs SCFooBar -> sc-foo-bar
        String className = ERXStringUtilities.lastPropertyKeyInKeyPath(getClass().getName());
        Pattern p = Pattern.compile("^([A-Z]+?)([A-Z])");
        Matcher m = p.matcher(className);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, String.valueOf(m.group(0)).toLowerCase() + "-");
        }
        m.appendTail(sb);
        className = sb.toString();

        p = Pattern.compile("([A-Z][a-z0-9]+)");
        m = p.matcher(className);
        sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, String.valueOf(m.group()).toLowerCase() + "-");
        }
        m.appendTail(sb);
        className = sb.toString();
        className = className.substring(0, className.length() - 1);
        return className;
    }

    public String id(WOContext context) {
        return (String) valueForBinding("id", context.component());
    }

    public String elementName(WOContext context) {
        String tag = (String) valueForBinding("elementName", defaultElementName(), context.component());
        tag = (String) valueForBinding("tag",tag, context.component());
        return tag;
    }

    protected Object defaultElementName() {
        return "div";
    }

    public String css(WOContext context) {
        String css = (String) valueForBinding("class", context.component());
        if (css == null) {
            css = "";
        }
        return css + " " + cssName(context);
    }

    public String style(WOContext context) {
        return (String) valueForBinding("style", context.component());
    }

    public SCItem currentItem() {
        return SCItem.currentItem();
    }

    
    protected String scriptName() {
    	if(getClass() == SCView.class) {
    		return "views/view.js";
    	}
        String name = getClass().getName().replaceAll("er.sproutcore", "");
        name = name.replaceAll("\\.SC", "\\.");
        name = name.replaceAll("\\.", "/");
        name = name.replaceAll("View$", "");
        name = name + ".js";
        name = ERXStringUtilities.camelCaseToUnderscore(name, true);
        name = name.replaceAll("/_", "/");
        name = name.replaceAll("^/+", "");
        return name;
    }
    
    @Override
    public final void appendToResponse(WOResponse response, WOContext context) {
        SCItem item = pushItem(context);
        pullBindings(context, item);
        
        ERXResponse scriptResponse = ERXResponse.pushPartial(SCPageTemplate.CLIENT_JS);
        NSArray<String> scripts = SCUtilities.require("SproutCore", scriptName());
        log.info("adding: " +scripts);
        for (String script : scripts) {
            SCJavaScript.appendScript(scriptResponse, context, script);
        }
        ERXResponse.popPartial();

        String elementName = elementName(context);
        String css = css(context);
        String itemid = "";
        if (item.isRoot()) {
            itemid = " id=\"" + item.id() + "\"";
        }
        css += " " + item.id();
        response.appendContentString("<" + elementName + itemid + " class=\"" + css + "\" ");
        appendAttributesToResponse(response, context);
        response.appendContentString(">");
        doAppendToResponse(response, context);
        response.appendContentString("</" + elementName + ">");
        popItem();
    }

    protected SCItem pushItem(WOContext context) {
        return SCItem.pushItem(id(context), className(context));
    }

    protected SCItem popItem() {
        return SCItem.popItem();
    }

    protected void pullBindings(WOContext context, SCItem item) {
        for (String key : _bindings.allKeys()) {
            Object value = _bindings.objectForKey(key).valueInComponent(context.component());
            log.debug("Binding: " + key + ":" + value);
            item.addBinding(key, value == null ? NSKeyValueCoding.NullValue : value);
        }
        for (String key : _properties.allKeys()) {
            Object value = _properties.objectForKey(key).valueInComponent(context.component());
            log.debug("Prop: " + key + ":" + value);
            item.addProperty(key, value == null ? NSKeyValueCoding.NullValue : value);
        }
    }

    public void appendAttributesToResponse(WOResponse arg0, WOContext arg1) {
        return;
    }
}
