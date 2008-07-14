package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSKeyValueCoding;

import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.foundation.ERXStringUtilities;
import er.sproutcore.SCItem;

/**
 * Superclass for your own components that are actual views, so you don't have
 * to write dynamic elements.
 * 
 * @author ak
 * 
 */
public class SCComponent extends ERXNonSynchronizingComponent {

    public SCComponent(WOContext arg0) {
        super(arg0);
    }

    @SuppressWarnings("cast")
    @Override
    public final void appendToResponse(WOResponse response, WOContext context) {
        SCItem item = SCItem.pushItem(stringValueForBinding("id"), className());
        for (String key : ((NSArray<String>) bindingKeys())) {
            Object value = valueForBinding(key);
            value = value == null ? NSKeyValueCoding.NullValue : value;
            if (key.startsWith("?")) {
                item.addBinding(key.substring(1), value);
            } else {
                item.addProperty(key, value);
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
        return NSBundle.bundleForClass(getClass()).name() + "." + ERXStringUtilities.lastPropertyKeyInKeyPath(name());
    }
}
