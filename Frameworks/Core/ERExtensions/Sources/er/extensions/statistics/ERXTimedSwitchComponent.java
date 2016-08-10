package er.extensions.statistics;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOSwitchComponent;
import com.webobjects.foundation.NSDictionary;

/**
 * A switch component that collects timing stats.
 * @author Travis Cripps
 */
public class ERXTimedSwitchComponent extends WOSwitchComponent {

    WOAssociation _statsKey;

    public ERXTimedSwitchComponent(String name, NSDictionary associations, WOElement template) {
        super(name, associations, template);
        
        if (super.componentAttributes != null) {
            _statsKey = super.componentAttributes.removeObjectForKey("statsKey");
        }
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
