package er.extensions.statistics;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOSwitchComponent;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * A switch component that collects metrics.
 * @author Travis Cripps
 */
public class ERXTimedSwitchComponent extends WOSwitchComponent {

    WOAssociation _eventInfo;

    public ERXTimedSwitchComponent(String name, NSDictionary associations, WOElement template) {
        super(name, associations, template);
        
        if (super.componentAttributes != null) {
            _eventInfo = (WOAssociation)super.componentAttributes.removeObjectForKey("userInfo");
        }
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        ERXMetricsEvent event = ERXMetrics.createAndMarkStartOfEvent(ERXMetricsEvent.EventTypes.ComponentRender, eventInfoInContext(context));
        super.appendToResponse(response, context);
        ERXMetrics.markEndOfEvent(event);
    }

    private NSMutableDictionary eventInfoInContext(WOContext context) {
        NSMutableDictionary result = new NSMutableDictionary();
        result.takeValueForKey(_elementNameInContext(context), "componentName");

        // Try to get additional event info from the userInfo bindings.
        if (_eventInfo != null) {
            Object userInfo = _eventInfo.valueInComponent(context.component());
            if (userInfo != null && userInfo instanceof NSDictionary) {
                result.addEntriesFromDictionary((NSDictionary)userInfo);
            }
        }
        
        return result;
    }
    
}
