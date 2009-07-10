package com.webobjects.appserver._private;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;
import er.extensions.statistics.ERXMetrics;
import er.extensions.statistics.ERXMetricsEvent;

/**
 * A generic container that collects metrics.
 * @author Travis Cripps
 */
// Note: This class must be in the webobjects package in order to properly process the "userInfo" binding association.
public class ERXTimedGenericContainer extends WOGenericContainer {
    
    WOAssociation _eventInfo;

    public ERXTimedGenericContainer(String name, NSDictionary associations, WOElement template) {
        super(name, associations, template);

        _eventInfo = (WOAssociation)_associations.removeObjectForKey("userInfo");
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        ERXMetricsEvent event = ERXMetrics.createAndMarkStartOfEvent(ERXMetricsEvent.EventTypes.ComponentRender, eventInfoInContext(context));
        super.appendToResponse(response, context);
        ERXMetrics.markEndOfEvent(event);
    }

    /**
     * Gets contextual information for the {@link ERXMetricsEvent metrics event}.
     * @param context of the element
     * @return the event context information
     */
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
