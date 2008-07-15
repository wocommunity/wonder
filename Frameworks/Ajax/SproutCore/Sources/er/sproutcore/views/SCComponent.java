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
	
    public SCComponent(WOContext context) {
        super(context);
    	_movedProperties = new NSMutableDictionary<String, String>();
    	_removedProperties = new NSMutableArray<String>();
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

    protected Object evaluateValueForBinding(String name, Object value) {
    	return value;
    }

    @Override
	@SuppressWarnings("unchecked")
	public NSArray<String> bindingKeys() {
    	return super.bindingKeys();
    }

	@Override
    public final void appendToResponse(WOResponse response, WOContext context) {
        SCItem item = SCItem.pushItem(id(), className(), outlet());
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
        doAppendToResponse(response, context);
        SCItem.popItem();
    }

    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
    }

    /**
     * Returns a nice class name like MyApp.MyView.
     * @return
     */
    protected String className() {
        return SCView.defaultClassName(getClass());
    }
}
