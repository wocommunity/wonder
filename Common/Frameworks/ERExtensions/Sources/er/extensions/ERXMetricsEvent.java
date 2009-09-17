package er.extensions;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;
import org.apache.log4j.Logger;

import java.util.Enumeration;

/**
 * A very lightweight event for metrics collection.
 * @author Travis Cripps
 */
public class ERXMetricsEvent implements NSKeyValueCoding._KeyBindingCreation {

    public static final Logger log = Logger.getLogger(ERXMetricsEvent.class);

    public static interface Keys {
        public static final String Duration = "duration";
        public static final String EndTime = "endTime";
        public static final String IsClosed = "isClosed";
        public static final String IsOpen = "isOpen";
        public static final String ParentEvent = "parentEvent";
        public static final String StartTime = "startTime";
        public static final String SubEvents = "subEvents";
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
     * Compares the duration of this event to that of another event.
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

    /**
     * <p>Creates <em>get</em> {@link NSKeyValueCoding._KeyBinding keyBindings} for the class.</p>
     *
     * <p>If the key cannot be resolved, returns a subclass of {@link @link NSKeyValueCoding._KeyBinding} that simply
     * returns <code>null</code> when it is queried for its value. As
     * {@link NSKeyValueCoding.DefaultImplementation#valueForKey} is called <strong>a lot</strong> on this class when
     * searching for events, this is an attempt to avoid generating
     * {@link NSKeyValueCoding.UnknownKeyException UnknownKey exceptions}, which can be relatively expensive.</p>
     *
     * @param key for which the binding should be created
     * @return the key binding.
     */
    public NSKeyValueCoding._KeyBinding _createKeyGetBindingForKey(String key) {
        NSKeyValueCoding._KeyBinding keyBinding = NSKeyValueCoding.DefaultImplementation._createKeyGetBindingForKey(this, key);
        if (null == keyBinding) {
            keyBinding = new _ReturnsNullKeyBinding(getClass(), key);
        }
        return keyBinding;
    }

    /**
     * <p>Creates <em>set</em> {@link NSKeyValueCoding._KeyBinding keyBindings} for the class.  Uses the
     * {@link NSKeyValueCoding.DefaultImplementation default implementation} of this method.
     * @param key for which the binding should be created
     * @return the key binding
     */
    public NSKeyValueCoding._KeyBinding _createKeySetBindingForKey(String key) {
        return NSKeyValueCoding.DefaultImplementation._createKeySetBindingForKey(this, key);
    }

    /**
     * <p>Gets the <em>get</em> {@link NSKeyValueCoding._KeyBinding keyBindings} for the key.  Uses the
     * {@link NSKeyValueCoding.DefaultImplementation default implementation} of this method.
     * @param key for which the binding should be created
     * @return the key binding
     */
    public NSKeyValueCoding._KeyBinding _keyGetBindingForKey(String key) {
        return NSKeyValueCoding.DefaultImplementation._keyGetBindingForKey(this, key);
    }

    /**
     * <p>Gets the <em>set</em> {@link NSKeyValueCoding._KeyBinding keyBindings} for the key.  Uses the
     * {@link NSKeyValueCoding.DefaultImplementation default implementation} of this method.
     * @param key for which the binding should be created
     * @return the key binding
     */
    public NSKeyValueCoding._KeyBinding _keySetBindingForKey(String key) {
        return NSKeyValueCoding.DefaultImplementation._keySetBindingForKey(this, key);
    }

    /**
     * A subclass of {@link @link NSKeyValueCoding._KeyBinding} that simply returns <code>null</code> when it is queried
     * for its value and does nothing when setting a value.
     */
    private static class _ReturnsNullKeyBinding extends NSKeyValueCoding._KeyBinding {
        private _ReturnsNullKeyBinding(Class targetClass, String key) {
            super(targetClass, key);
        }

        @Override
        public Object valueInObject(Object object) {
            return null;
        }

        @Override
        public void setValueInObject(Object value, Object object) {
            // Do nothing.
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        sb.append(getClass().getSimpleName());
        sb.append(" type: ").append(type());
        sb.append("; duration: ").append(duration());
        sb.append("; userInfo: ").append(userInfo()).append(" ]");
        return sb.toString();
    }

}
