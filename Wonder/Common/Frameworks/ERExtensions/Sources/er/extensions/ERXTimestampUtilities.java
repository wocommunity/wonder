//
// NSTimestampUtilities.java
// Project vwdBussinessLogicJava
//
// Created by ak on Wed Jun 06 2001
//
package er.extensions;

import com.webobjects.foundation.*;

public class ERXTimestampUtilities extends Object {
    public static NSTimestamp timestampForString(String defaultValue) {
        NSTimestamp value =  null;
        if(defaultValue.equals("now")) {
            value = new NSTimestamp();
        } else if(defaultValue.equals("today")) {
            value = ERXTimestampUtilities.today();
        } else if(defaultValue.equals("yesterday")) {
            value = ERXTimestampUtilities.yesterday();
        } else if(defaultValue.equals("tomorrow")) {
            value = ERXTimestampUtilities.tomorrow();
        } else if(defaultValue.equals("distantPast")) {
            value = ERXTimestampUtilities.distantPast();
        } else if(defaultValue.equals("distantFuture")) {
            value = ERXTimestampUtilities.distantFuture();
        }
        return value;
    }
    

    public static NSTimestamp unixDate(Number helpedNSNumber) {
        return ERXTimestampUtilities.epoch().timestampByAddingGregorianUnits(0, 0, 0, 0, 0, (int)helpedNSNumber.longValue()+60*60);
    }

    public static Integer unixTimestamp(NSTimestamp ts) {
        int seconds = 0;
        seconds = (int)ts.timeIntervalSinceTimestamp(ERXTimestampUtilities.epoch());
        return (new Integer(seconds-60*60));
    }

    public static NSTimestamp today() {
        NSTimestamp today = new NSTimestamp();
        return today.timestampByAddingGregorianUnits(0, 0, 0, -today.hourOfDay(), -today.minuteOfHour(), -today.secondOfMinute());
    }


    public static NSTimestamp tomorrow() {
        NSTimestamp tomorrow = new NSTimestamp();
        return tomorrow.timestampByAddingGregorianUnits(0, 0, 1, -tomorrow.hourOfDay(), -tomorrow.minuteOfHour(), -tomorrow.secondOfMinute());
    }


    public static NSTimestamp yesterday() {
        NSTimestamp yesterday = new NSTimestamp();
        return yesterday.timestampByAddingGregorianUnits(0, 0, -1, -yesterday.hourOfDay(), -yesterday.minuteOfHour(), -yesterday.secondOfMinute());
    }

    public static NSTimestamp distantPast() {
        NSTimestamp distantPast = new NSTimestamp();
        return distantPast.timestampByAddingGregorianUnits(-2000, 0, -1, 0, 0, 0);
    }

    public static NSTimestamp distantFuture() {
        NSTimestamp distantPast = new NSTimestamp();
        return distantPast.timestampByAddingGregorianUnits(+2000, 0, -1, 0, 0, 0);
    }

     
    public static NSTimestamp dateByAddingTime(NSTimestamp ts, NSTimestamp time) {
        return ts.timestampByAddingGregorianUnits(0, 0, 0, time.hourOfDay(), time.minuteOfHour(), time.secondOfMinute());
    }

    public static NSTimestamp timestampByAddingTime(NSTimestamp ts, NSTimestamp time) {
        return ts.timestampByAddingGregorianUnits(0, 0, 0, time.hourOfDay(), time.minuteOfHour(), time.secondOfMinute());
    }


    /************** Start Of UnixTimeAdditions ***************/

    static NSTimestamp _epoch;


    public static NSTimestamp epoch() {
        if (_epoch == null) {
            _epoch = new NSTimestamp(1970, 1, 1, 0, 0, 0, null);
        }

        return _epoch;
    }


    public static boolean isEarlierThan(NSTimestamp ts1, NSTimestamp ts2) {
        return ts1.compare(ts2) == NSComparator.OrderedAscending;
    }

    public static boolean isLaterThan(NSTimestamp ts1, NSTimestamp ts2) {
        return ts1.compare(ts2) == NSComparator.OrderedDescending;
    }
}
