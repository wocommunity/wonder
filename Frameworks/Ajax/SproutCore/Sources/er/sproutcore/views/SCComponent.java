package er.sproutcore.views;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;

import er.extensions.components.ERXNonSynchronizingComponent;
import er.extensions.foundation.ERXStringUtilities;
import er.sproutcore.views.SCView.Item;

public class SCComponent extends ERXNonSynchronizingComponent {

    public SCComponent(WOContext arg0) {
        super(arg0);
    }

    @Override
    public final void appendToResponse(WOResponse response, WOContext context) {
        Item item = SCView.pushItem(stringValueForBinding("id"), className());
        for (String key : ((NSArray<String>) bindingKeys())) {
            Object value = valueForBinding(key);
            value = value == null ? "null" : value;
            if (key.startsWith("?")) {
                item.addBinding(key.substring(1), value);
            } else {
                item.addProperty(key, value);
            }
        }
        doAppendToResponse(response, context);
        SCView.popItem();
    }

    protected void doAppendToResponse(WOResponse response, WOContext context) {
        super.appendToResponse(response, context);
    }

    protected String className() {
        return NSBundle.bundleForClass(getClass()).name() + "." +  ERXStringUtilities.lastPropertyKeyInKeyPath(name());
    }
}
