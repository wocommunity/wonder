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
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.ajax.AjaxOption;
import er.ajax.AjaxUtils;
import er.ajax.AjaxValue;
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
public class SCView extends WODynamicGroup implements ISCView {
    protected Logger log = Logger.getLogger(getClass());

    protected final String[] CSS_PROPERTIES = new String[]{"width", "height", "minHeight", "maxHeight", "minWidth", "maxWidth"};
    
    private NSMutableDictionary<String, WOAssociation> _associations;

    private NSMutableDictionary<String, SCProperty> _properties;

    private NSMutableDictionary<String, SCBinding> _bindings;

    @SuppressWarnings("unchecked")
    public SCView(String name, NSDictionary associations, WOElement parent) {
        super(name, associations, parent);
        _associations = associations.mutableClone();
        updateDefaultValues();
        _bindings = new NSMutableDictionary<String, SCBinding>();
        _properties = new NSMutableDictionary<String, SCProperty>();

        for (Enumeration e = associations.keyEnumerator(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            WOAssociation association = (WOAssociation) associations.objectForKey(key);
            if (key.startsWith("?")) {
            	addBinding(new SCBinding(key.substring(1), association));
            }
        }
        addProperties();
    }
    
    protected void addProperties() {
    	SCView.addDefaultProperties(this);
    }
    
    public NSDictionary<Object, Object> animate() {
    	return null;
    }
    
    public String paneDef() {
    	return null;
    }
    
    protected static void addDefaultProperties(ISCView view) {
		view.addProperty("enabled", "isEnabled");
		view.addProperty("modal", "isModal");
		view.addProperty("custom_panel", "hasCustomPanelWrapper");
		view.addProperty("localize");
		view.addProperty("validator");
		view.addProperty("field_label");
		view.addProperty("accepts_first_responder");
		view.addProperty("content");
		view.addProperty("value");
		view.addProperty("content_value_key");
		
		// For SC.SplitView support
		view.addProperty("max_thickness");
		view.addProperty("min_thickness");
		view.addProperty("can_collapse");
		view.addProperty("collapse", "isCollapsed");

		// General delegate support
		view.addProperty("delegate", AjaxOption.SCRIPT);
		view.addProperty("drop_target", "isDropTarget");
		
		String paneDef = view.paneDef();
		if (paneDef == null) {
			view.addProperty("pane", "paneType");
		}
		else {
			view.addPropertyWithDefault("pane", "paneType", paneDef);
		}
	
		final NSDictionary<Object, Object> animate = view.animate();
		if (animate != null) {
			view.addProperty(new SCProperty("visible_animation", null, "", AjaxOption.DEFAULT, true) {
				@Override
				public String javascriptValue(Object value) {
					NSMutableDictionary<String, String> keyMap = new NSMutableDictionary<String, String>();
					keyMap.setObjectForKey("onComplete", "complete");
					
					NSMutableDictionary<Object, Object> animationValues = new NSMutableDictionary<Object, Object>();
					for (Object key : animate.keySet()) {
						Object animateValue = animate.objectForKey(key);
						String normalizedKey = key.toString().toLowerCase();
						if (!"complete".equals(normalizedKey)) {
							animateValue = new AjaxValue(animateValue).javascriptValue();
						}
						if (keyMap.containsKey(normalizedKey)) {
							key = keyMap.objectForKey(normalizedKey);
						}
						animationValues.setObjectForKey(animateValue, key);
					}
					
					return super.javascriptValue(animationValues);
				}
			});
		}
    }

    protected void updateDefaultValues() {
        // TODO Auto-generated method stub
        
    }

    public boolean hasProperty(String string) {
        return properties().objectForKey(string).isBound();
    }
    
    protected void addBinding(String bindingName) {
    	addBinding(bindingName, bindingName);
    }
    
    protected void addBinding(String associationName, String bindingName) {
    	addBinding(new SCBinding(bindingName, _associations.objectForKey("?" + associationName)));
    }
    
    protected void addBinding(SCBinding binding) {
    	_bindings.setObjectForKey(binding, binding.name());
    }
    
    public void addProperty(String propertyName) {
    	addProperty(propertyName, propertyName, null, AjaxOption.DEFAULT, true);
    }
    
    public void addPropertyWithDefault(String propertyName, Object defaultValue) {
    	addProperty(propertyName, propertyName, defaultValue, AjaxOption.DEFAULT, true);
    }
    
    public void addPropertyWithDefault(String associationName, String propertyName, Object defaultValue) {
    	addProperty(associationName, propertyName, defaultValue, AjaxOption.DEFAULT, true);
    }
    
    public void addProperty(String associationName, String propertyName) {
    	addProperty(associationName, propertyName, null, AjaxOption.DEFAULT, true);
    }
    
    public void addProperty(String associationName, String propertyName, boolean skipIfNull) {
    	addProperty(associationName, propertyName, null, AjaxOption.DEFAULT, skipIfNull);
    }
    
    public void addProperty(String propertyName, AjaxOption.Type type) {
    	addProperty(propertyName, propertyName, null, type, true);
    }
    
    public void addProperty(String associationName, String propertyName, AjaxOption.Type type) {
    	addProperty(associationName, propertyName, null, type, true);
    }
    
    public void addProperty(String associationName, String propertyName, Object defaultValue, AjaxOption.Type type, boolean skipIfNull) {
    	addProperty(new SCProperty(propertyName, _associations.objectForKey(ERXStringUtilities.underscoreToCamelCase(associationName, false)), defaultValue, type, skipIfNull));
    }
    
    public void addProperty(SCProperty property) {
    	_properties.setObjectForKey(property, property.name());
    }
    
    public SCProperty propertyNamed(String propertyName) {
    	return _properties.objectForKey(propertyName);
    }
    
    protected NSDictionary<String, SCProperty> properties() {
        return _properties;
    }

    public WOAssociation associationNamed(String name) {
    	return _associations.objectForKey(name);
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
        String className = SCUtilities.defaultCssName(getClass());
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
        String style = (String) valueForBinding("style", context.component());
        /*boolean isVisible = booleanValueForBinding("visible", true, context.component());
        if(!isVisible) {
        	style = (style == null ? "" : style);
        	style += "display: none;";
        }
 */
        return style;
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
        boolean isVisible = booleanValueForBinding("visible", true, context.component());
        ERXResponse contentResponse = (ERXResponse) response;
        if(!isVisible) {
        	contentResponse =  ERXResponse.pushPartial(SCPageTemplate.RESOURCES);
            contentResponse.appendContentString("<div style=\"display: none\">");
        }
        String elementName = elementName(context);
        String css = css(context);
        String itemid = "";
        if (item.isRoot()) {
            itemid = " id=\"" + item.id() + "\"";
        }
        
        css += " " + item.id();
        prependToResponse(contentResponse, context);
        contentResponse.appendContentString("<" + elementName + itemid + " class=\"" + css + "\" ");
        appendAttributesToResponse(contentResponse, context);
        contentResponse.appendContentString(">");
        doAppendToResponse(contentResponse, context);
        contentResponse.appendContentString("</" + elementName + ">");
        if(!isVisible) {
            contentResponse.appendContentString("</div>");
        	ERXResponse.popPartial();
        }
        popItem();
    }

    protected void prependToResponse(WOResponse response, WOContext context) {
    	// DO NOTHING
    }

    public boolean isRoot(WOContext context) {
    	return booleanValueForBinding("root", false, context.component()) || valueForBinding("pane", null, context.component()) != null;
    }

    protected SCItem pushItem(WOContext context) {
        return SCItem.pushItem(id(context), className(context), outlet(context), isRoot(context));
    }

    protected SCItem popItem() {
        return SCItem.popItem();
    }

    protected void pullBindings(WOContext context, SCItem item) {
    	for (SCBinding binding : _bindings.allValues()) {
            Object value = binding.association().valueInComponent(context.component());
            log.debug("Binding: " + binding.name() + ":" + value);
    		item.addBinding(binding, value);
    	}
    	for (SCProperty property : _properties.allValues()) {
            Object value = property.association().valueInComponent(context.component());
            log.debug("Prop: " + property.name() + ":" + value);
    		item.addProperty(property, value);
    	}
    }

    protected String blankUrl() {
        return SCUtilities.staticUrl("blank.gif");
    }
    
    protected void appendStyleToResponse(WOResponse response, WOContext context) {
    	String style = "";
    	for (int i = 0; i < CSS_PROPERTIES.length; i++) {
    		String key = CSS_PROPERTIES[i];
    		Object value = valueForBinding(key, context.component());
    		if(value != null) {
    			String cssKey = ERXStringUtilities.camelCaseToUnderscore(key, true).replace('_', '-');
    			style += (value == null ? "" : "; " + cssKey + ": " + value +"px");
    		}
    	}
    	if(style.length() != 0) {
    		style = style.substring(2);
    	}
    	String bindingStyle = style(context);
    	if(bindingStyle != null) {
    		style += bindingStyle;
    	}
    	if(style.length() != 0) {
    		response.appendContentString(" style=\"" + style + "\"");
    	}
    }
    
    protected boolean appendStyleToContainer() {
    	return true;
    }
    
    public void appendAttributesToResponse(WOResponse response, WOContext context) {
    	if (appendStyleToContainer()) {
    		appendStyleToResponse(response, context);
    	}
    }
}
