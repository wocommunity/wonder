/*
 * WOEventSetupPage.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEvent;
import com.webobjects.eocontrol.EOEventCenter;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;

public class WOEventSetupPage extends WOEventPage {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public Class	currentClass;
    public String	currentEventDescription;
    public int		currentIndex;

    protected static final _ClassNameComparator _classNameAscendingComparator = new _ClassNameComparator(EOSortOrdering.CompareAscending);
    
    public WOEventSetupPage(WOContext aContext) {
        super(aContext);
    }

    public NSArray registeredEventClasses() {
        NSMutableArray	classes;


        classes = new NSMutableArray();
        classes.setArray(EOEventCenter.registeredEventClasses());
        
        try {
            classes.sortUsingComparator(_classNameAscendingComparator);
        } catch (NSComparator.ComparisonException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }

        return classes;
    }

    public boolean isClassRegisteredForLogging() {
         return EOEventCenter.recordsEventsForClass(currentClass);
    }

    public void setIsClassRegisteredForLogging(boolean yn) {
         EOEventCenter.setRecordsEvents(yn, currentClass);
    }

    protected void _setAllRegisteredEvents(boolean tf) {
        NSArray	registered;
        int i, n;
        Class c;

        registered = EOEventCenter.registeredEventClasses();
        int count = registered.count();
        for (i = 0, n = count; i < n; i++) {
            c = (Class)registered.objectAtIndex(i);
            EOEventCenter.setRecordsEvents(tf, c);
        }
    }
    
    public WOComponent selectAll() {
        _setAllRegisteredEvents(true);
        return null;
    }

    public WOComponent clearAll() {
        _setAllRegisteredEvents(false);
        return null;
    }
    
    public NSArray currentEventDescriptions() {
        NSMutableArray<String> descs;
        NSDictionary<String,String> map;

        map = EOEvent.eventTypeDescriptions(currentClass);

        descs = new NSMutableArray<String>();
        descs.setArray(map.allValues());
        descs.removeObject(map.objectForKey(EOEvent.EventGroupName));
        try {
            descs.sortUsingComparator(NSComparator.AscendingStringComparator);
        } catch (NSComparator.ComparisonException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
        descs.insertObjectAtIndex(map.objectForKey(EOEvent.EventGroupName), 0);

        return descs;
    }

    public boolean isClassName() {
        return (currentIndex == 0);
    }

}
