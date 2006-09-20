/*
 * WOEventRow.java
 * © Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOAggregateEvent;
import com.webobjects.eocontrol.EOEvent;

public class WOEventRow extends WOComponent {
    public EOEvent 	object;
    public EOEvent 	event;
    public WOEventDisplayPage	controller;
    public int	displayMode;
    
    public WOEventRow(WOContext aContext)  {
        super(aContext);
    }

    public String loopCount()
    {
        if (object == event)
            return "1x";
        else
            return ((EOAggregateEvent)object).events().count()+"x";
    }

    public String eventDuration()
    {
        return controller.durationOfEvent(object)+" ms";
    }

    public String comment()
    {
        return object.comment();
    }

    public String title()
    {
        return object.title();
    }

    //--------------------------------------------------

    public String hyperlinkTitle()
    {
        return title();
    }

    public String descriptionTitle()
    {
        return comment();
    }

    public double percentOfMaxTime()
    {
        return (double)controller.durationOfEvent(object) / (double)controller.topmostDurationValue();
    }

}
