/*
 * WOEventRow.java
 * (c) Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOAggregateEvent;
import com.webobjects.eocontrol.EOEvent;

public class WOEventRow extends WOComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
