package com.webobjects.appserver._private;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.statistics.ERXStats;

/**
 * A generic container that collects timing stats.
 * @author Travis Cripps
 */
// Note: This class must be in the webobjects package in order to properly process the "statsKey" binding association.
public class ERXTimedGenericContainer extends WOGenericContainer {
    
    WOAssociation _statsKey;

    public ERXTimedGenericContainer(String name, NSDictionary associations, WOElement template) {
        super(name, associations, template);

        _statsKey = _associations.removeObjectForKey("statsKey");
    }
    
    @Override
    public WOActionResults invokeAction(WORequest aRequest, WOContext aContext) {
        String statsKey = statsKey(aContext);
        ERXStats.markStart(ERXStats.Group.ComponentInvokeAction, statsKey);
    	WOActionResults results = super.invokeAction(aRequest, aContext);
        ERXStats.markEnd(ERXStats.Group.ComponentInvokeAction, statsKey);
    	return results;
    }
    
    @Override
    public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
        String statsKey = statsKey(aContext);
        ERXStats.markStart(ERXStats.Group.ComponentTakeValuesFromRequest, statsKey);
    	super.takeValuesFromRequest(aRequest, aContext);
        ERXStats.markEnd(ERXStats.Group.ComponentTakeValuesFromRequest, statsKey);
    }

    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
        String statsKey = statsKey(context);
        ERXStats.markStart(ERXStats.Group.Component, statsKey);
        super.appendToResponse(response, context);
        ERXStats.markEnd(ERXStats.Group.Component, statsKey);
    }

    /**
     * Gets the key for the {@link ERXStats.LogEntry stats entry}.
     * @param context of the element
     * @return the key
     */
    private String statsKey(WOContext context) {
        String key;
        if (_statsKey != null) {
            key = (String)_statsKey.valueInComponent(context.component());
        } else {
            key = _elementNameInContext(context);
        }
        return key;
    }
    
}
