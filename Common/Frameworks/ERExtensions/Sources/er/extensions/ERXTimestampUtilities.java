//
// NSTimestampUtilities.java
// Project vwdBussinessLogicJava
//
// Created by ak on Wed Jun 06 2001
//
package er.extensions;

import com.webobjects.foundation.*;
import java.util.GregorianCalendar;

public class ERXTimestampUtilities extends Object {
    protected static GregorianCalendar _calendar = (GregorianCalendar)GregorianCalendar.getInstance();

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

    class ERXTimestamp {
        NSTimestamp ts;

        ERXTimestamp(NSTimestamp value) {
            ts = value;
        }

        public int dayOfCommonEra() {
            return yearOfCommonEra()*365 + dayOfYear();
        }

        public int dayOfWeek() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.DAY_OF_WEEK);
        }

        public int dayOfYear() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.DAY_OF_YEAR);
        }

        public int hourOfDay() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.HOUR_OF_DAY);
        }

        public int minuteOfHour() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.MINUTE);
        }

        public int secondOfMinute() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.SECOND);
        }

        public int monthOfYear() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.MONTH);
        }

        public int yearOfCommonEra() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.YEAR);
        }
    }

    static ERXTimestamp getInstance() {
        return getInstance(new NSTimestamp());
    }

    static ERXTimestamp getInstance(NSTimestamp ts) {
        return getInstance(ts);
    }

    public static NSTimestamp unixDate(Number helpedNSNumber) {
        return ERXTimestampUtilities.epoch().timestampByAddingGregorianUnits(0, 0, 0, 0, 0, (int)helpedNSNumber.longValue()+60*60);
    }

    public static Integer unixTimestamp(NSTimestamp ts) {
        long seconds = 0;
        seconds = ts.getTime() - epoch().getTime();
        return (new Integer((int)((seconds-60*60)/1000L)));
    }

    public static NSTimestamp today() {
        ERXTimestamp now = getInstance();
        return (new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 0, -now.hourOfDay(), -now.minuteOfHour(), -now.secondOfMinute());
    }


    public static NSTimestamp tomorrow() {
        ERXTimestamp now = getInstance();
        return (new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 1, -now.hourOfDay(), -now.minuteOfHour(), -now.secondOfMinute());
    }


    public static NSTimestamp yesterday() {
        ERXTimestamp now = getInstance();
        return (new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, -1, -now.hourOfDay(), -now.minuteOfHour(), -now.secondOfMinute());
    }

    public static NSTimestamp distantPast() {
       return NSTimestamp.DistantPast;
    }

    public static NSTimestamp distantFuture() {
        return NSTimestamp.DistantFuture;
    }

    public static NSTimestamp dateByAddingTime(NSTimestamp ts, NSTimestamp t1) {
        ERXTimestamp time = getInstance(t1);
        return ts.timestampByAddingGregorianUnits(0, 0, 0, time.hourOfDay(), time.minuteOfHour(), time.secondOfMinute());
    }

    public static NSTimestamp timestampByAddingTime(NSTimestamp ts, NSTimestamp t1) {
        ERXTimestamp time = getInstance(t1);
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
