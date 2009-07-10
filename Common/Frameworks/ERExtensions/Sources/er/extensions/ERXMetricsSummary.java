package er.extensions;

import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;

/**
 * A component to display the summary of page metrics for the current page.
 * @author Travis Cripps
 */
public class ERXMetricsSummary extends ERXStatelessComponent {

    public String currentType;
    private NSDictionary _aggregateMetricsByType;
    private NSArray _eventTypes;

    public ERXMetricsSummary(WOContext context) {
        super(context);
    }

    /**
     * Gets the aggregate metrics grouped by event type.
     * @return the aggregate metrics
     */
    public NSDictionary aggregateMetricsByType() {
        if (null == _aggregateMetricsByType) {
            if (hasBinding("aggregateEventInfo")) {
                _aggregateMetricsByType = (NSDictionary)valueForBinding("aggregateEventInfo");
            } else {
                ERXMetrics.aggregateEventInfoByType();
            }
        }
        return _aggregateMetricsByType;
    }

    /**
     * Gets the array of event types.
     * @return the event types
     */
    public NSArray eventTypes() {
        if (null == _eventTypes) {
            _eventTypes = ERXArrayUtilities.sortedArrayUsingComparator(aggregateMetricsByType().allKeys(), NSComparator.AscendingCaseInsensitiveStringComparator);
        }
        return _eventTypes;
    }

    /**
     * Gets the aggregate duration for the current metrics type.
     * @return the duration
     */
    public long aggregateDurationForMetricsType() {
        NSDictionary metricsForType = (NSDictionary)aggregateMetricsByType().valueForKey(currentType);
        Long duration = (Long)metricsForType.valueForKey("duration");
        return duration != null ? duration : 0L;
    }

    /**
     * Determines if metrics are enabled by querying ERXMetrics about its state.
     * @return true if enabled
     */
    public boolean metricsEnabled() {
        return ERXMetrics.metricsEnabled();
    }

    public void reset() {
        super.reset();
        currentType = null;
        _aggregateMetricsByType = null;
        _eventTypes = null;
    }

}
