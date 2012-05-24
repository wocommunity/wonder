/*
 * WOEventDisplayPage.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import java.util.Enumeration;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOEvent;
import com.webobjects.eocontrol.EOEvent;
import com.webobjects.eocontrol.EOEventCenter;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

public class WOEventDisplayPage extends WOEventPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public EOEvent 		currentEvent;
    public NSArray	selectionPath;
    public int			_displayMode;
    public NSArray		events;
    public NSMutableDictionary	cache;
    public NSMutableArray webEvents, eofEvents;
    public WOEventDisplayPage _self = this;
    protected _EventComparator _eventAscendingComparator;
    
    public WOEventDisplayPage(WOContext aContext)  {
        super(aContext);
        selectionPath = new NSMutableArray();
        _displayMode = 1;
        cache = new NSMutableDictionary();
        // we sort these things descending, i.e. the opposite of normal sorting order
        _eventAscendingComparator = new _EventComparator(EOSortOrdering.CompareDescending, this);
    }

    public int displayMode() {
        return _displayMode;
    }

    public void setDisplayMode(Object ick) {
        if (ick == null) {
            _displayMode = 0;
        } else if (ick instanceof Number) {
            _displayMode = ((Number)ick).intValue();
        } else {
            try {
                _displayMode = Integer.parseInt(ick.toString());
            } catch (NumberFormatException e) {
                _displayMode = 0;
            }
        }
    }
    
    public int displayLevelForEvent(EOEvent e)
    {
        int	index, i, n;
        NSArray children;

        index = selectionPath.indexOfObject(e);
        if (index != NSArray.NotFound)
            return index;

        children = rootEventList();
        if (children.containsObject(e))
            return 0;

        int count = selectionPath.count();
        for (i = 0, n = count; i < n; i++) {
            children = (NSArray)cache.objectForKey(selectionPath.objectAtIndex(i));
            if (null==children)
                break;

            if (children.containsObject(e))
                return i+1;
        }

        return -1;
    }

    public NSArray filterEvents(NSArray evs, int level)
    {
        int i, n;
        NSArray filtered;
        if (evs == null) {
            return NSArray.EmptyArray;
        }
        // in the general case, it is sufficient to sort the events
        // by their plain duration, which is what the default implementation does.
        try {
            if (_displayMode != 4 || level != 0) {
                try {
                    filtered = evs.sortedArrayUsingComparator(_eventAscendingComparator);
                } catch (IllegalStateException ex) {
                    filtered = evs;
                }
            } else {

                // For association mode, we need to filter out unwanted events,
                // i.e. those which are not related to associations.
                int count = evs.count();
                NSMutableArray mutableFiltered = new NSMutableArray(count);

                for (i = 0, n = count; i < n; i++) {
                    if (childrenForEvent((EOEvent)evs.objectAtIndex(i)).count()!=0)
                        mutableFiltered.addObject(evs.objectAtIndex(i));
                }

                mutableFiltered.sortUsingComparator(_eventAscendingComparator);

                filtered = mutableFiltered;
            }

        } catch (NSComparator.ComparisonException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }

        return filtered;
    }

    public int groupTagForDisplayLevel(int level)
    {
        switch (_displayMode) {
            case 0:
                return -1;

            case 1:
                return -1;

            case 2:
                switch (level) {
                    case 0:
                        return 2;

                    case 1:
                        return 1;

                    default:
                        return -1;
                }

            case 3:
                if (level == 0)
                    return 2;
                else
                    return -1;

            case 4:
                if (level == 0)
                    return 2;
                else
                    return -1;

            default: // bogus
                return -1;
        }
    }

    public int aggregateTagForDisplayLevel(int level)
    {
        switch (_displayMode) {
            case 0:
                return -1;

            case 1:
                return 0;

            case 2:
                if (level <= 1)
                    return -1;
                else
                    return 0;

            case 3:
                if (level == 0)
                    return -1;
                else
                    return 0;

            case 4:
                if (level == 0)
                    return -1;
                else
                    return 3;

            default: // bogus
                return -1;
        }
    }

    public NSArray rootEventList()
    {
        if (null == events) {
            switch (_displayMode) {
                case 0:
                case 1:
                    events = EOEventCenter.rootEventsForAllCenters();
                    break;

                default:
                    events = EOEventCenter.allEventsForAllCenters();
                    break;
            }

            events = EOEvent.groupEvents(events, groupTagForDisplayLevel(0));

            events = EOEvent.aggregateEvents(events, aggregateTagForDisplayLevel(0));

            events = filterEvents(events, 0);
        }
        return events;
    }

    public EOEvent object()
    {
        return currentEvent;
    }

    public NSArray childrenForEvent(EOEvent event)
    {
        NSArray anArray;
        int level, tag;

        anArray = (NSArray)cache.objectForKey(event);
        if (null!=anArray) {
            if (anArray.count()==0)
                return null;
            else
                return anArray;
        }

        anArray = event.subevents();

        if (anArray == null || (anArray.count()==0)) {
            cache.setObjectForKey(NSArray.EmptyArray, event);
            return null;
        }

        level = displayLevelForEvent(event) + 1;
        if (level == -1)
            level = selectionPath.count() + 1;

        tag = groupTagForDisplayLevel(level);
        if (tag >= 0)
            anArray = EOEvent.groupEvents(anArray, tag);

        tag = aggregateTagForDisplayLevel(level);
        if (tag >= 0)
            anArray = EOEvent.aggregateEvents(anArray, tag);

        anArray = filterEvents(anArray, level);

        cache.setObjectForKey(anArray, event);

        return anArray;
    }

    public NSArray currentEventChildren()
    {
        NSArray result = childrenForEvent(currentEvent);
        return (result != null) ? result : NSArray.EmptyArray;
    }

    public boolean isDirectory()
    {
        return currentEventChildren().count() > 0;
    }

    public WOComponent resetLoggingClicked()
    {
        EOEventCenter.resetLoggingForAllCenters();
        return refreshLoggingClicked();
    }

    public WOComponent refreshLoggingClicked()
    {
        cache.removeAllObjects();
        selectionPath = new NSMutableArray();
        events = null;
        webEvents = null;
        eofEvents = null;

        return null;
    }

    public String displayComponentName()
    {
        if (currentEvent == null) {
            return "";
        }
        return currentEvent.displayComponentName();
    }

    public int eventCount()
    {
        return EOEventCenter.allEventsForAllCenters().count();
    }

    public long topmostDurationValue()
    {
        NSArray roots;

        roots = rootEventList();
        if (roots == null || (roots.count()==0))
            return 0;
        else
            return durationOfEvent((EOEvent)roots.objectAtIndex(0));
    }

    public long durationOfEvent(EOEvent e)
    {
        int i, n;
        long sum;
        NSArray kids;

        // mode 4 is in so far special as we need to filter out events which will not
        // be displayed, even if they may have a duration.
        if (_displayMode != 4) {
            sum = e.duration();
            return sum;
        }

        // if an event has no kids, but we are still here, it means that it is
        // an association event (because otherwise it would be filtered out).
        kids = childrenForEvent(e);
        if (kids == null) {
            kids = NSArray.EmptyArray;
        }
        n = kids.count();
        if (n!=0) {
            for (i = 0, sum = 0; i < n; i++) {
                sum += ((EOEvent)kids.objectAtIndex(i)).duration();
            }
        } else {
            sum = e.duration();
        }

        return sum;
    }

    public void _cacheWebEofEvents()
    {
        if (webEvents != null)
            return;

        NSArray allCenters = EOEventCenter.allEventsForAllCenters();
        int halfCount = allCenters.count() / 2;
        webEvents = new NSMutableArray(halfCount);
        eofEvents = new NSMutableArray(halfCount);

        Enumeration anEnumerator = allCenters.objectEnumerator();
        while (anEnumerator.hasMoreElements()) {
            EOEvent e = (EOEvent) anEnumerator.nextElement();
            if (e instanceof WOEvent)
                webEvents.addObject(e);
            else eofEvents.addObject(e);
        }
    }

    public int webEventDuration()
    {
        int i, n, time;

        _cacheWebEofEvents();

        n = (webEvents != null) ? webEvents.count() : 0;
        
        for (i = 0, time = 0; i < n; i++) {
            EOEvent	e = (EOEvent) webEvents.objectAtIndex(i);
            if (e instanceof WOEvent)
                time = time + (int) e.durationWithoutSubevents();
        }

        return time;
    }

    public int eofEventDuration()
    {
        int i, n, time;

        _cacheWebEofEvents();

        n = (eofEvents != null) ? eofEvents.count() : 0;
        for (i = 0, time = 0; i < n; i++) {
            EOEvent	e = (EOEvent) eofEvents.objectAtIndex(i);
            if (!(e instanceof WOEvent))
                time = time + (int) e.durationWithoutSubevents();
        }

        return time;
    }

    public int webEventCount() {
        _cacheWebEofEvents();
        return (webEvents != null) ? webEvents.count() : 0;
    }

    public int eofEventCount() {
        _cacheWebEofEvents();
        return (eofEvents != null) ? eofEvents.count() : 0;
    }
	
	public boolean isEmpty() {
		return eventCount() == 0;
	}
}
