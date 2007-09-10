package er.extensions;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSDictionary;

/**
 * Records stats on how long the various phases in the request-response loop
 * took for the children. This uses ERXStats, so stats tracking must be
 * enabled.
 * 
 * @see er.extensions.ERXStats
 *
 * @author ak
 * @author mschrag
 */
public class ERXDebugTimer extends WODynamicGroup {
	private WOAssociation _displayName;

	public ERXDebugTimer(String name, NSDictionary associations, WOElement template) {
		super(name, associations, template);
		_displayName = (WOAssociation) associations.valueForKey("displayName");
	}

	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context) {
		if (ERXStats.isTrackingStatistics()) {
			String name = (_displayName != null) ? (String) _displayName.valueInComponent(context.component()) : "ERXDebugTimer";
			ERXStats.markStart(name + ": takeValuesFromRequest");
			try {
				super.takeValuesFromRequest(request, context);
			}
			finally {
				ERXStats.markEnd(name + ": takeValuesFromRequest");
			}
		}
		else {
			super.takeValuesFromRequest(request, context);
		}
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		if (ERXStats.isTrackingStatistics()) {
			String name = (_displayName != null) ? (String) _displayName.valueInComponent(context.component()) : "ERXDebugTimer";
			ERXStats.markStart(name + ": invokeAction");
			try {
				return super.invokeAction(request, context);
			}
			finally {
				ERXStats.markEnd(name + ": invokeAction");
			}
		}
		else {
			return super.invokeAction(request, context);
		}
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		if (ERXStats.isTrackingStatistics()) {
			String name = (_displayName != null) ? (String) _displayName.valueInComponent(context.component()) : "ERXDebugTimer";
			ERXStats.markStart(name + ": appendToResponse");
			try {
				super.appendToResponse(response, context);
			}
			finally {
				ERXStats.markEnd(name + ": appendToResponse");
			}
		}
		else {
			super.appendToResponse(response, context);
		}
	}
}
