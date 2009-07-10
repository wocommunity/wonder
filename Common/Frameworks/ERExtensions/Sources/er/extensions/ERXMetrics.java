package er.extensions;

import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.WeakHashMap;
import java.util.Enumeration;

/**
 * Lightweight page metrics support.  It is similar to EOEventCenter, but is much simpler.  It is intended for
 * page or component-level timing.
 * @author Travis Cripps
 */
public class ERXMetrics {

    public static final Logger log = Logger.getLogger(ERXMetrics.class);

    public static interface Keys {
        public static String MetricsEnabled = "metricsEnabled";
    }

    public static final String ERXMetricsEnabledKey = "er.extensions.ERXMetricsEnabled"; 

    private static WeakHashMap<WOSession, LinkedList<ERXMetricsEvent>> eventListBySession = new WeakHashMap<WOSession, LinkedList<ERXMetricsEvent>>();
    private static WeakHashMap<WOSession, NSMutableDictionary> eventsByTypeBySession = new WeakHashMap<WOSession, NSMutableDictionary>();

    /**
     * The list of events.
     * @return the list of events
     */
    private static LinkedList<ERXMetricsEvent> eventList() {
        WOSession session = ERXSession.session();
        LinkedList<ERXMetricsEvent> eventList = eventListBySession.get(session);
        if (null == eventList) {
            eventList = new LinkedList<ERXMetricsEvent>();
            eventListBySession.put(session, eventList);
        }
        return eventList;
    }

    /**
     * A dictionary of events grouped by event type.
     * @return the grouped events
     */
    private static NSMutableDictionary eventsByType() {
        WOSession session = ERXSession.session();
        NSMutableDictionary eventsDict = eventsByTypeBySession.get(session);
        if (null == eventsDict) {
            eventsDict = new NSMutableDictionary();
            eventsByTypeBySession.put(session, eventsDict);
        }
        return eventsDict;
    }

    /**
     * Gets the events.
     * @return the events
     */
    public static NSArray events() {
        return new NSArray(eventList().toArray());
    }

    /**
     * Gets events of the given type.
     * @param type of event
     * @return the events of the give type
     */
    public static NSMutableArray eventsOfType(String type) {
        return (NSMutableArray)eventsByType().valueForKey(type);
    }

    /**
     * Resets the event storage.
     */
    public static void reset() {
        eventsByType().removeAllObjects();
        eventList().clear();
    }

    /**
     * Determines if the metrics are enabled.
     * @return true if enabled
     */
    public static boolean metricsEnabled() {
        boolean defaultValue = ERXProperties.booleanForKeyWithDefault(ERXMetricsEnabledKey, false);
        return ERXExtensions.booleanFlagOnSessionForKeyWithDefault(ERXSession.session(), Keys.MetricsEnabled, defaultValue);
    }

    /**
     * Sets the flag enabling or disabling the metrics
     * @param value to set
     */
    public static void setMetricsEnabled(boolean value) {
        ERXExtensions.setBooleanFlagOnSessionForKey(ERXSession.session(), Keys.MetricsEnabled, value);
    }

    /**
     * Marks the start of the given event with the provided contextual information about the event.
     * @param event to start
     * @param userInfo information about the event
     */
    public static void markStartOfEvent(ERXMetricsEvent event, NSDictionary userInfo) {
        event.markStartWithUserInfo(userInfo);
        registerEvent(event);
    }

    /**
     * Creates a new event and immediately marks it started.
     * @param type of event
     * @return the new event
     */
    public static ERXMetricsEvent createAndMarkStartOfEvent(String type) {
        ERXMetricsEvent event = new ERXMetricsEvent(type);
        ERXMetrics.markStartOfEvent(event, null);
        return event;
    }

    /**
     * Marks the start of the given event with the provided contextual information about the event.
     * @param type of event
     * @param userInfo information about the event
     * @return the new event
     */
    public static ERXMetricsEvent createAndMarkStartOfEvent(String type, NSDictionary userInfo) {
        ERXMetricsEvent event = new ERXMetricsEvent(type);
        ERXMetrics.markStartOfEvent(event, userInfo);
        return event;
    }

    /**
     * Registers the event with the internal event storage data structures.
     * @param event to register
     */
    private static void registerEvent(ERXMetricsEvent event) {
        _addEvent(event);
        _addEventByType(event);
    }

    /**
     * Adds the event to the event list.
     * @param event to add
     */
    private static void _addEvent(ERXMetricsEvent event) {
        if (!metricsEnabled()) {
            return;
        }

        if (eventList().isEmpty()) {
            eventList().add(event);
        } else {
            ERXMetricsEvent latestEvent = eventList().getLast();
            if (latestEvent.isOpen()) {
                latestEvent.addSubEvent(event);
            } else {
                eventList().add(event);
            }
        }
    }

    /**
     * Adds the event to the events grouped by type.
     * @param event to add
     */
    private static void _addEventByType(ERXMetricsEvent event) {
        if (!metricsEnabled()) {
            return;
        }
        
        NSMutableArray eventsForType = (NSMutableArray)eventsByType().valueForKey(event.type());
        if (null == eventsForType) {
            eventsForType = new NSMutableArray();
            eventsByType().takeValueForKey(eventsForType, event.type());
        }
        eventsForType.addObject(event);
    }

    /**
     * Marks the end of the given event
     * @param event to end
     */
    public static void markEndOfEvent(ERXMetricsEvent event) {
        if (event != null) {
            event.markEnd();
        }
    }

    /**
     * Gets the latest completed event of the given type.
     * @param type of event
     * @return the event
     */
    public static ERXMetricsEvent latestCompletedEventOfType(String type) {
        EOKeyValueQualifier typeQual = new EOKeyValueQualifier(ERXMetricsEvent.Keys.Type, EOQualifier.QualifierOperatorEqual, type);
        EOKeyValueQualifier closedQual = new EOKeyValueQualifier(ERXMetricsEvent.Keys.IsClosed, EOQualifier.QualifierOperatorEqual, Boolean.TRUE);
        EOAndQualifier andQual = new EOAndQualifier(new NSArray(new EOQualifier[] { typeQual, closedQual }));
        return latestEventMatchingQualifier(andQual);
    }

    /**
     * Gets events matching the given qualifier
     * @param qualifier against which events should be evaluated
     * @return the matching events
     */
    public static NSArray eventsMatchingQualifier(EOQualifierEvaluation qualifier) {
        return eventsInArrayMatchingQualifier(new NSArray(eventList().toArray()), qualifier);
    }

    /**
     * Gets events in the provided array matching the given qualifier
     * @param array of events
     * @param qualifier against which events should be evaluated
     * @return the matching events
     */
    private static NSArray eventsInArrayMatchingQualifier(NSArray array, EOQualifierEvaluation qualifier) {
        NSMutableArray result = new NSMutableArray();
        for (Enumeration eventEnum = array.objectEnumerator(); eventEnum.hasMoreElements();) {
            ERXMetricsEvent event = (ERXMetricsEvent)eventEnum.nextElement();
            boolean match = false;
            try {
                match = qualifier.evaluateWithObject(event);
            } catch (NSKeyValueCoding.UnknownKeyException uke) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not evaluate key: '" + uke.key() + "' on event " + uke.object());
                }
            }

            if (!match) {
                try {
                    match = qualifier.evaluateWithObject(event.userInfo());
                } catch (NSKeyValueCoding.UnknownKeyException uke) {
                    if (log.isDebugEnabled()) {
                        log.debug("Could not evaluate key: '" + uke.key() + "' on event userInfo" + uke.object());
                    }
                }
            }

            if (match) {
                result.addObject(event);
            }

            if (event.hasSubEvents()) {
                NSArray subEventsMatchingQualifier = eventsInArrayMatchingQualifier(event.subEvents(), qualifier);
                if (subEventsMatchingQualifier.count() > 0) {
                    result.addObjectsFromArray(subEventsMatchingQualifier);
                }
            }
        }
        return result;
    }

    /**
     * Gets the latest event matching the qualifier.
     * @param qualifier against which events should be evaluated
     * @return the event
     */
    public static ERXMetricsEvent latestEventMatchingQualifier(EOQualifierEvaluation qualifier) {
        NSArray events = eventsMatchingQualifier(qualifier);
        events = ERXArrayUtilities.sortedArraySortedWithKey(events, ERXMetricsEvent.Keys.StartTime);
        return (ERXMetricsEvent)events.lastObject();
    }

    /**
     * Gets aggregate event information (duration), grouped by event type.
     * @return the aggregate event information
     */
    public static NSDictionary aggregateEventInfoByType() {
        return aggregateEventInfoByTypeForEventsInArray(new NSArray(eventList().toArray()));
    }

    /**
     * Gets aggregate event information (duration), grouped by event type, from the events in the provided array.
     * @param array of events to aggregate
     * @return the aggregate event information
     */
    private static NSDictionary aggregateEventInfoByTypeForEventsInArray(NSArray array) {
        NSMutableDictionary eventsDict = new NSMutableDictionary();
        for (Enumeration eventEnum = array.objectEnumerator(); eventEnum.hasMoreElements();) {
            ERXMetricsEvent event = (ERXMetricsEvent)eventEnum.nextElement();
            String type = event.type();

            NSMutableDictionary dictForEventType = (NSMutableDictionary)eventsDict.objectForKey(type);
            if (null == dictForEventType) {
                dictForEventType = new NSMutableDictionary();
                eventsDict.setObjectForKey(dictForEventType, type);
            }

            // Handle the current event.
            Long duration = (Long)dictForEventType.valueForKey("duration");
            if (null == duration) {
                duration = 0L;
            }
            duration += event.duration();//durationWithoutSubEvents
            dictForEventType.setObjectForKey(duration, "duration");

            // Handle sub events.
            if (event.hasSubEvents()) {
                NSDictionary aggregateEventInfoByType = aggregateEventInfoByTypeForEventsInArray(event.subEvents());
                for (Enumeration keyEnum = aggregateEventInfoByType.allKeys().objectEnumerator(); keyEnum.hasMoreElements();) {
                    String aType = (String)keyEnum.nextElement();
                    Long subduration = (Long)aggregateEventInfoByType.valueForKey("duration");
                    if (null == subduration) {
                        subduration = 0L;
                    }

                    dictForEventType = (NSMutableDictionary)eventsDict.objectForKey(aType);
                    if (null == dictForEventType) {
                        dictForEventType = new NSMutableDictionary();
                        eventsDict.setObjectForKey(dictForEventType, aType);
                    }
                    Long aggregateDuration = (Long)dictForEventType.valueForKey("duration");
                    if (null == aggregateDuration) {
                        aggregateDuration = 0L;
                    }
                    aggregateDuration += subduration;
                    dictForEventType.setObjectForKey(aggregateDuration, "duration");
                }
            }
        }
        return eventsDict;
    }

    /**
     * Gets aggregate event information (duration), grouped by event type, from the events matching the qualifier.
     * @param qualifier against which events should be evaluated
     * @return the aggregate event information
     */
    public static NSDictionary aggregateEventInfoByTypeForEventsMatchingQualifier(EOQualifierEvaluation qualifier) {
        return aggregateEventInfoByTypeForEventsInArray(eventsMatchingQualifier(qualifier));
    }

}
