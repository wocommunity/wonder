package er.extensions.appserver;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;

import er.extensions.statistics.ERXStats;
import er.extensions.statistics.ERXStats.Group;

/**
 * Records stats on how long the various phases in the request-response loop
 * took for the children. This uses ERXStats, so stats tracking must be
 * enabled.
 * 
 * @see er.extensions.statistics.ERXStats
 * @binding displayName name of the item in the stats (defaults to parent name)
 * @author ak
 * @author mschrag
 */
public class ERXDebugTimer extends WODynamicGroup {
	private WOAssociation _displayName;

	public ERXDebugTimer(String name, NSDictionary associations, WOElement template) {
		super(name, associations, template);
		_displayName = (WOAssociation) associations.valueForKey("displayName");
	}

	private String keyInContext(WOContext context, String suffix) {
		String name = (_displayName != null) ? (String) _displayName.valueInComponent(context.component()) : null;
		if(context.component() != null) {
			name = context.component().name();
		}
		if(name == null) {
			name = "ERXDebugTimer";
		}
		return name + "." + suffix;
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (ERXStats.isTrackingStatistics()) {
			String key = keyInContext(context, "takeValuesFromRequest");
			ERXStats.markStart(Group.ComponentTakeValuesFromRequest, key);
			super.takeValuesFromRequest(request, context);
			ERXStats.markEnd(Group.ComponentTakeValuesFromRequest, key);
		}
		else {
			super.takeValuesFromRequest(request, context);
		}
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		WOActionResults result;
		if (ERXStats.isTrackingStatistics()) {
			String key = keyInContext(context, "invokeAction");
			ERXStats.markStart(Group.ComponentInvokeAction, key);
			ERXStats.markEnd(Group.ComponentInvokeAction, key);
			result = super.invokeAction(request, context);
		}
		else {
			result = super.invokeAction(request, context);
		}
		return result;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		if (ERXStats.isTrackingStatistics()) {
			String key = keyInContext(context, "appendToResponse");
			ERXStats.markStart(Group.Component, key);
			super.appendToResponse(response, context);
			ERXStats.markEnd(Group.Component, key);
		}
		else {
			super.appendToResponse(response, context);
		}
	}
}
