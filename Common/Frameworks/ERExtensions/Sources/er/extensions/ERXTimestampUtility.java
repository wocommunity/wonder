/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import java.util.*;

// A simle utility for providing deprecated functionality for NSTimestamps
public class ERXTimestampUtility {

    protected static GregorianCalendar _calendar = (GregorianCalendar)GregorianCalendar.getInstance();
    
    public static GregorianCalendar calendarForTimestamp(NSTimestamp t) {
        _calendar.setTime(t);
        return _calendar;
    }

    public static int dayOfCommonEra(NSTimestamp t) {
        return yearOfCommonEra(t)*365 + dayOfYear(t);
    }
    
    public static int dayOfWeek(NSTimestamp t) {
        return calendarForTimestamp(t).get(GregorianCalendar.DAY_OF_WEEK);        
    }

    public static int dayOfYear(NSTimestamp t) {
        return calendarForTimestamp(t).get(GregorianCalendar.DAY_OF_YEAR);
    }    
    
    public static int hourOfDay(NSTimestamp t) {
        return calendarForTimestamp(t).get(GregorianCalendar.HOUR_OF_DAY);
    }

    public static int minuteOfHour(NSTimestamp t) {
        return calendarForTimestamp(t).get(GregorianCalendar.MINUTE);        
    }

    public static int monthOfYear(NSTimestamp t) {
        return calendarForTimestamp(t).get(GregorianCalendar.MONTH);        
    }

    public static int yearOfCommonEra(NSTimestamp t) {
        return calendarForTimestamp(t).get(GregorianCalendar.YEAR);
    }
}
