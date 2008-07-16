package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.components.ERXNonSynchronizingComponent;
import er.sproutcore.SCItem;

/**
 * Superclass for your own components that are actual views, so you don't have
 * to write dynamic elements.
 * 
 * @author ak
 * 
 */
public class SCComponent extends ERXNonSynchronizingComponent {
	private NSMutableDictionary<String, String> _movedProperties;
	private NSMutableArray<String> _removedProperties;
	private String _className;
	
    public SCComponent(WOContext context) {
        super(context);
    	_movedProperties = new NSMutableDictionary<String, String>();
    	_removedProperties = new NSMutableArray<String>();
		removeProperty("id");
		removeProperty("outlet");
    	setClassName(SCView.defaultClassName(getClass()));
    }

    public String containerID() {
      SCItem item = SCItem.currentItem();
      return (item.isRoot()) ? item.id() : null;
    }

    public String nextID() {
      return "id_" +SCItem.nextId();
    }

    protected void moveProperty(String bindingName, String propertyName) {
    	_movedProperties.setObjectForKey(propertyName, bindingName);
    }
    
    protected void removeProperty(String propertyName) {
    	_removedProperties.addObject(propertyName);
    }
    
    protected boolean skipPropertyIfNull(String propertyName) {
      return false;
    }

    public String id() {
        return stringValueForBinding("id");
    }

    public String outlet() {
    	return stringValueForBinding("outlet", id());
    }

    public boolean root() {
    	return booleanValueForBinding("root", false);
    }

    protected Object evaluateValueForBinding(String name, Object value) {
    	return value;
    }

    @Override
	@SuppressWarnings("unchecked")
	public NSArray<String> bindingKeys() {
    	return super.bindingKeys();
    }

    public String containerClass() {
      StringBuffer css = new StringBuffer();
      if (!booleanValueForBinding("enabled", true)) {
        css.append("disabled");
      }
      //css.append(" ");
      //css.append(SCUtilities.defaultCssName(getClass()));
      css.append(" ");
      css.append(containerID());
      return css.toString();
    }
 
	@Override
    public final void appendToResponse(WOResponse response, WOContext context) {
        SCItem item = SCItem.pushItem(id(), className(), outlet(), root());
        for (String key : bindingKeys()) {
            Object value = valueForBinding(key);
            value = evaluateValueForBinding(key, value);
            
            boolean binding = key.startsWith("?");
            String itemName;
            if (binding) {
            	itemName = key.substring(1);
            }
            else {
            	itemName = key;
            }
            
            String movedPropertyName = _movedProperties.objectForKey(itemName);
            if (movedPropertyName != null) {
            	itemName = movedPropertyName;
            }
            
            if (!_removedProperties.containsObject(itemName)) {
	            if (binding) {
	                item.addBinding(itemName, value == null ? NSKeyValueCoding.NullValue : value);
	            } else if (value == null && !skipPropertyIfNull(itemName)) {
	            	item.addProperty(itemName, NSKeyValueCoding.NullValue);
	            } else if (value != null) {
	            	item.addProperty(itemName, value);
	            }
            }
        }
        String id =  containerID();
        String elementName = elementName(); 
        response._appendContentAsciiString("<" + elementName);
        
        if(id != null) {
            response._appendContentAsciiString(" id=\"" + containerID() + "\"");
        }
        String containerClass = containerClass();
        if (containerClass != null) {
        	response._appendContentAsciiString(" class=\"" + containerClass + "\"");
        }
        
        String style = style();
        if (style != null) {
        	response._appendContentAsciiString(" style=\"" + style + "\"");
        }
        
        response._appendContentAsciiString(">");

        doAppendToResponse(response, context);
        SCItem.popItem();
        response._appendContentAsciiString("</" + elementName + ">");
    }

    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
    }
    
    protected String elementName() {
    	return "div";
    }
    
    protected String style() {
    	return stringValueForBinding("style");
    }

    /**
     * Returns a nice class name like MyApp.MyView.
     * @return
     */
    protected String className() {
        return _className;
    }
    
    protected void setClassName(String name) {
    	_className = name;
    }
}
