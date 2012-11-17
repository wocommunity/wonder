/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.foundation;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;

/**
 * A simple utility for providing deprecated functionality for NSTimestamps.
 * 
 * @deprecated use {@link ERXTimestampUtilities} instead
 */
@Deprecated
public class ERXTimestampUtility {

    public static GregorianCalendar calendarForTimestamp(NSTimestamp t) {
        GregorianCalendar calendar = (GregorianCalendar) Calendar.getInstance();
        calendar.setTime(t);
        return calendar;
    }

    public static long offsetForDateInCommonEra(NSTimestamp t, int mode) {
        GregorianCalendar calendar = calendarForTimestamp(t);
        switch(mode) {
            case Calendar.YEAR:
                return calendar.get(Calendar.YEAR);
            case Calendar.MONTH:
                return calendar.get(Calendar.YEAR) * 12 + calendar.get(Calendar.MONTH);
            case Calendar.WEEK_OF_YEAR:
                return calendar.get(Calendar.YEAR) * 52 + calendar.get(Calendar.WEEK_OF_YEAR);
            case Calendar.DAY_OF_MONTH:
            case Calendar.DAY_OF_YEAR:
                return calendar.get(Calendar.YEAR) * 365 + calendar.get(Calendar.DAY_OF_YEAR);
            case Calendar.HOUR_OF_DAY:
            case Calendar.HOUR:
                return (calendar.get(Calendar.YEAR) * 365 + calendar.get(Calendar.DAY_OF_YEAR)) * 24 + calendar.get(Calendar.HOUR_OF_DAY);
            default:
                return 0;
        }
    }

    public static long differenceByDay(NSTimestamp t1, NSTimestamp t2) {
        return compareDatesInCommonEra(t1, t2, Calendar.DAY_OF_YEAR);
    }

    public static long differenceByWeek(NSTimestamp t1, NSTimestamp t2) {
        return compareDatesInCommonEra(t1, t2, Calendar.WEEK_OF_YEAR);
    }

    public static long differenceByMonth(NSTimestamp t1, NSTimestamp t2) {
        return compareDatesInCommonEra(t1, t2, Calendar.MONTH);
    }

    public static long differenceByYear(NSTimestamp t1, NSTimestamp t2) {
        return compareDatesInCommonEra(t1, t2, Calendar.YEAR);
    }

    public static NSTimestamp firstDateInSameWeek(NSTimestamp value) {
        return new NSTimestamp(ERXTimestampUtility.yearOfCommonEra(value), ERXTimestampUtility.monthOfYear(value), -ERXTimestampUtility.dayOfWeek(value) + 1, 0, 0, 0, NSTimeZone.defaultTimeZone());
    }

    public static NSTimestamp firstDateInSameMonth(NSTimestamp value) {
        return new NSTimestamp(ERXTimestampUtility.yearOfCommonEra(value), ERXTimestampUtility.monthOfYear(value), -ERXTimestampUtility.dayOfMonth(value)+1, 0, 0, 0, NSTimeZone.defaultTimeZone());
    }
    public static NSTimestamp firstDateInNextMonth(NSTimestamp value) {
        return firstDateInSameMonth(value).timestampByAddingGregorianUnits(0, 1, 0, 0, 0, 0);
    }
    
    public static long compareDatesInCommonEra(NSTimestamp t1, NSTimestamp t2, int mode) {
        return offsetForDateInCommonEra(t2, mode) - offsetForDateInCommonEra(t1, mode);
    }

    public static int dayOfCommonEra(NSTimestamp t) {
        return yearOfCommonEra(t)*365 + dayOfYear(t);
    }

    public static int monthOfCommonEra(NSTimestamp t) {
        return yearOfCommonEra(t)*12 + monthOfYear(t);
    }

    public static int weekOfCommonEra(NSTimestamp t) {
        return yearOfCommonEra(t)*12 + weekOfYear(t);
    }

    public static boolean isWeekDay(NSTimestamp t) {
        int day = dayOfWeek(t);
        return !((day == Calendar.SATURDAY) || (day == Calendar.SUNDAY));
    }

    public static int dayOfWeek(NSTimestamp t) {
        return calendarForTimestamp(t).get(Calendar.DAY_OF_WEEK);        
    }

    public static int dayOfMonth(NSTimestamp t) {
        return calendarForTimestamp(t).get(Calendar.DAY_OF_MONTH);
    }

    public static int weekOfYear(NSTimestamp t) {
        return calendarForTimestamp(t).get(Calendar.WEEK_OF_YEAR);
    }    

    public static int weekOfMonth(NSTimestamp t) {
        return calendarForTimestamp(t).get(Calendar.WEEK_OF_MONTH);
    }

    public static int dayOfYear(NSTimestamp t) {
        return calendarForTimestamp(t).get(Calendar.DAY_OF_YEAR);
    }

    public static int hourOfDay(NSTimestamp t) {
        return calendarForTimestamp(t).get(Calendar.HOUR_OF_DAY);
    }

    public static int minuteOfHour(NSTimestamp t) {
        return calendarForTimestamp(t).get(Calendar.MINUTE);        
    }

    public static int secondOfMinute(NSTimestamp t) {
        return calendarForTimestamp(t).get(Calendar.SECOND);        
    }

    public static int monthOfYear(NSTimestamp t) {
        return calendarForTimestamp(t).get(Calendar.MONTH);        
    }

    public static int yearOfCommonEra(NSTimestamp t) {
        return calendarForTimestamp(t).get(Calendar.YEAR);
    }
}
