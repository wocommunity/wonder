package er.extensions.statistics;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

import java.util.Enumeration;

/**
 * A very lightweight event for metrics collection.
 * @author Travis Cripps
 */
public class ERXMetricsEvent {

    public static interface Keys {
        public static final String EndTime = "endTime";
        public static final String IsClosed = "isClosed";
        public static final String StartTime = "startTime";
        public static final String Type = "type";
        public static final String UserInfo = "userInfo";
    }

    // Some common event types.
    public static interface EventTypes {
        public static final String ComponentRender = "Component Rendering";
        public static final String DBFetch = "DB Fetch";
    }

    private String type;
    private NSTimestamp startTime;
    private NSTimestamp endTime;
    private NSMutableDictionary userInfo = new NSMutableDictionary();
    private ERXMetricsEvent parentEvent;
    private NSMutableArray subEvents = new NSMutableArray();

    /**
     * Creates an event of the given type
     * @param eventType of the event
     */
    public ERXMetricsEvent(String eventType) {
        type = eventType;
    }

    /**
     * Creates an event of the given type and with the information from the provided userInfo dictionary
     * @param eventType of the event
     * @param userInfo information about the event
     */
    public ERXMetricsEvent(String eventType, NSDictionary userInfo) {
        type = eventType;
        this.userInfo.addEntriesFromDictionary(userInfo);
    }

    /**
     * Gets the type of the event.
     * @return the event type
     */
    public String type() {
        return type;
    }

    /**
     * Gets the start time of the event.
     * @return the start time
     */
    public NSTimestamp startTime() {
        return startTime;
    }

    /**
     * Gets the end time of the event.
     * @return the end time
     */
    public NSTimestamp endTime() {
        return endTime;
    }

    /**
     * Gets the context information about the event.
     * @return the information
     */
    public NSMutableDictionary userInfo() {
        return userInfo;
    }

    /**
     * Gets the parent event if available.
     * @return the parent event
     */
    public ERXMetricsEvent parentEvent() {
        return parentEvent;
    }

    /**
     * Sets the parent event.
     * @param event which is parental
     */
    public void setParentEvent(ERXMetricsEvent event) {
        parentEvent = event;
    }

    /**
     * Determines if the event has a parent.
     * @return true if it has a parent
     */
    public boolean hasParentEvent() {
        return parentEvent != null;
    }

    /**
     * Gets sub events of the current event, if any.
     * @return sub events
     */
    public NSArray subEvents() {
        return subEvents;
    }

    /**
     * Adds an event as a sub event of the current event.
     * @param event to add as a sub event
     */
    public void addSubEvent(ERXMetricsEvent event) {
        subEvents.addObject(event);
    }

    /**
     * Determines if the event has sub events.
     * @return true if it has sub events
     */
    public boolean hasSubEvents() {
        return subEvents.count() > 0;
    }

    /**
     * Determines if the current event is open.
     * @return true if open
     */
    public boolean isOpen() {
        return startTime != null && null == endTime;
    }

    /**
     * Determines if the current event is closed.
     * @return true if closed
     */
    public boolean isClosed() {
        return startTime != null && endTime != null;
    }

    /**
     * Marks the start of the event.
     */
    public void markStart() {
        if (!isOpen() && !isClosed()) {
            startTime = new NSTimestamp();
        }
    }

    /**
     * Marks the start of the event, storing the information from the provided userInfo as context about the event.
     * @param userInfo information about the event
     */
    public void markStartWithUserInfo(NSDictionary userInfo) {
        if (!isOpen() && !isClosed()) {
            this.userInfo.addEntriesFromDictionary(userInfo);
            startTime = new NSTimestamp();
        }
    }

    /**
     * Marks the end of the event.
     */
    public void markEnd() {
        if (!isClosed()) {
            endTime = new NSTimestamp();
        }
    }

    /**
     * Calculates the duration of the event.
     * @return the event duration
     */
    public long duration() {
        long result = 0L;
        if (isClosed()) {
            long startMillis = startTime.getTime();
            long endMillis = endTime.getTime();
            result = endMillis - startMillis;
        }
        return result;
    }

    /**
     * Gets the duration of the event without including the duration of any sub events.
     * @return the duration
     */
    public long durationWithoutSubEvents() {
        long result = duration();
        if (result > 0) {
            for (Enumeration eventsEnum = subEvents().objectEnumerator(); eventsEnum.hasMoreElements();) {
                ERXMetricsEvent subEvent = (ERXMetricsEvent)eventsEnum.nextElement();
                long subEventDuration = subEvent.durationWithoutSubEvents();
                result -= subEventDuration;
            }
        }
        return result;
    }

    /**
     * Compares the event to another.
     * @param event to which this will be compared
     * @return the comparison result, reflecting the ordering of the events
     */
    public int compare(ERXMetricsEvent event) {
        long thisDuration = duration();
		long thatDuration = event.duration();
		if (thisDuration == thatDuration) {
			return NSComparator.OrderedSame;
		}
		return (thisDuration < thatDuration) ? NSComparator.OrderedAscending : NSComparator.OrderedDescending;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        sb.append(getClass().getSimpleName());
        sb.append(" type: ").append(type());
        sb.append("; duration: ").append(duration());
        sb.append("; userInfo: ").append(userInfo()).append(".");
        return sb.toString();
    }
    
}
