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
import com.webobjects.foundation.NSBundle;
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

    @SuppressWarnings("unchecked")
    public SCView(String name, NSDictionary associations, WOElement parent) {
        super(name, associations, parent);
        _associations = associations.mutableClone();
        updateDefaultValues();
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
        }
        adjustProperties();
    }

    protected void adjustProperties() {
        removeProperty("id");
        removeProperty("className");
        removeProperty("class");
        removeProperty("elementName");
        removeProperty("style");
        removeProperty("outlet");
        removeProperty("view");
    }

    protected void updateDefaultValues() {
        // TODO Auto-generated method stub
        
    }

    public boolean hasProperty(String string) {
        return properties().containsKey(string);
    }

    public WOAssociation removeProperty(String name) {
        return _properties.removeObjectForKey(name);
    }
    
    public void setProperty(String name, WOAssociation association) {
        _properties.setObjectForKey(association, name);
    }

    public void moveProperty(String from, String to) {
        WOAssociation association = _properties.removeObjectForKey(from);
        if(association != null) {
            _properties.setObjectForKey(association, to);
        }
        association = _bindings.removeObjectForKey(from);
        if(association != null) {
            _bindings.setObjectForKey(association, to);
        }
    }
    
    protected NSDictionary properties() {
        return _properties;
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
       return (String) valueForBinding("view", SCView.defaultClassName(getClass()), context.component());
    }
    
    protected static String defaultClassName(Class clazz) {
      String bundleName = NSBundle.bundleForClass(clazz).name();
      if ("SproutCore".equals(bundleName)) {
        bundleName = "SC";
      }
      String defaultName = bundleName + "." + ERXStringUtilities.lastPropertyKeyInKeyPath(clazz.getName()).replaceAll("^SC", "");
      return defaultName;
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

    public String outlet(WOContext context) {
    	Object outlet = valueForBinding("outlet", context.component());
    	if(outlet instanceof Boolean || outlet == null) {
    		return (String) valueForBinding("id", context.component());
    	}
    	return (String)outlet;
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
        String cssName = cssName(context);
        if (cssName != null) {
        	css = css + " " + cssName;
        }
        return css;
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
        NSArray<String> scripts = SCUtilities.require("SproutCore", "sproutcore", scriptName());
        log.debug("adding: " +scripts);
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
        prependToResponse(response, context);
        response.appendContentString("<" + elementName + itemid + " class=\"" + css + "\" ");
        appendAttributesToResponse(response, context);
        response.appendContentString(">");
        doAppendToResponse(response, context);
        response.appendContentString("</" + elementName + ">");
        popItem();
    }
    
    protected void prependToResponse(WOResponse response, WOContext context) {
    	// DO NOTHING
    }

    protected SCItem pushItem(WOContext context) {
        return SCItem.pushItem(id(context), className(context), outlet(context));
    }

    protected SCItem popItem() {
        return SCItem.popItem();
    }
    
    protected boolean skipPropertyIfNull(String propertyName) {
      return false;
    }
    
    protected Object evaluateValueForBinding(WOContext context, String name, Object value) {
    	return value;
    }

    protected void pullBindings(WOContext context, SCItem item) {
        for (String key : _bindings.allKeys()) {
            Object value = _bindings.objectForKey(key).valueInComponent(context.component());
            value = evaluateValueForBinding(context, key, value);
            log.debug("Binding: " + key + ":" + value);
            item.addBinding(key, value == null ? NSKeyValueCoding.NullValue : value);
        }
        for (String key : _properties.allKeys()) {
            Object value = _properties.objectForKey(key).valueInComponent(context.component());
            value = evaluateValueForBinding(context, key, value);
            log.debug("Prop: " + key + ":" + value);
            if (value == null && !skipPropertyIfNull(key)) {
              item.addProperty(key, NSKeyValueCoding.NullValue);
            }
            else if (value != null) {
              item.addProperty(key, value);
            }
        }
    }

    protected String blankUrl() {
        return SCUtilities.staticUrl("blank.gif");
    }
    
    public void appendAttributesToResponse(WOResponse arg0, WOContext arg1) {
        return;
    }
}
