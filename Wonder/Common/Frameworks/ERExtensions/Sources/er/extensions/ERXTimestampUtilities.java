//
// NSTimestampUtilities.java
// Project vwdBussinessLogicJava
//
// Created by ak on Wed Jun 06 2001
//
package er.extensions;

import com.webobjects.foundation.*;
import java.util.GregorianCalendar;

/**
 * Collection of {@link NSTimestamp} utilities.
 */
public class ERXTimestampUtilities extends Object {

    /** holds a static reference to a GregorianCalendar */
    // FIXME: Not thread safe
    protected static GregorianCalendar _calendar = (GregorianCalendar)GregorianCalendar.getInstance();

    /**
     * Calculates a timestamp given a string. Currently supports
     * the strings: now, today, yesterday, tomorrow, distantPast
     * and distantFuture.
     * @param defaultValue string given above
     * @return timestamp equivalent to the string
     */
    public static NSTimestamp timestampForString(String defaultValue) {
        NSTimestamp value =  null;
        if (defaultValue.equals("now")) {
            value = new NSTimestamp();
        } else if (defaultValue.equals("today")) {
            value = ERXTimestampUtilities.today();
        } else if (defaultValue.equals("yesterday")) {
            value = ERXTimestampUtilities.yesterday();
        } else if (defaultValue.equals("tomorrow")) {
            value = ERXTimestampUtilities.tomorrow();
        } else if (defaultValue.equals("distantPast")) {
            value = ERXTimestampUtilities.distantPast();
        } else if (defaultValue.equals("distantFuture")) {
            value = ERXTimestampUtilities.distantFuture();
        }
        return value;
    }

    /**
     * Static inner class used for calculations for a given
     * timestamp. Only used internally.
     */
    static class ERXTimestamp {
        /** holds the timestamp */
        NSTimestamp ts;

        /**
         * Public constructor.
         * @param value timestamp to be wrapped
         */
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
        return new ERXTimestamp(ts);
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

    /**
     * Cover method for returning DistantPast
     * off of NSTimestamp.
     * @return a date in the distant past
     */
    // CHECKME: Is this needed?
    public static NSTimestamp distantPast() {
       return NSTimestamp.DistantPast;
    }

    /**
     * Cover method for returning DistantFuture
     * off of NSTimestamp.
     * @return a date in the distant future
     */    
    // CHECKME: Is this needed?
    public static NSTimestamp distantFuture() {
        return NSTimestamp.DistantFuture;
    }

    // CHECKME: This is the same as timestampByAddingTime
    public static NSTimestamp dateByAddingTime(NSTimestamp ts, NSTimestamp t1) {
        ERXTimestamp time = getInstance(t1);
        return ts.timestampByAddingGregorianUnits(0, 0, 0, time.hourOfDay(), time.minuteOfHour(), time.secondOfMinute());
    }

    public static NSTimestamp timestampByAddingTime(NSTimestamp ts, NSTimestamp t1) {
        ERXTimestamp time = getInstance(t1);
        return ts.timestampByAddingGregorianUnits(0, 0, 0, time.hourOfDay(), time.minuteOfHour(), time.secondOfMinute());
    }

    /************** Start Of UnixTimeAdditions ***************/
    
    /** holds a static reference to the epoch */
    static NSTimestamp _epoch;

    /**
     * Utility method used to retrun the epoch,
     * Jan 1st, 1970
     * @return the epoch as an NSTimestamp
     */
    public static NSTimestamp epoch() {
        if (_epoch == null) {
            _epoch = new NSTimestamp(1970, 1, 1, 0, 0, 0, null);
        }
        return _epoch;
    }

    /**
     * Compares two timestamps.
     * @param ts1 first timestamp
     * @param ts2 second timestamp
     * @return true if the the second timestamp is earlier than the
     *		first timestamp.
     */
    // CHECKME: Is this needed? java.sql.Timestamp has after and before
    public static boolean isEarlierThan(NSTimestamp ts1, NSTimestamp ts2) {
        return ts1.compare(ts2) == NSComparator.OrderedAscending;
    }

    /**
     * Compares two timestamps.
     * @param ts1 first timestamp
     * @param ts2 second timestamp
     * @return true if the the second timestamp is later than the
     *		first timestamp.
     */    
    // CHECKME: Is this needed? java.sql.Timestamp has after and before
    public static boolean isLaterThan(NSTimestamp ts1, NSTimestamp ts2) {
        return ts1.compare(ts2) == NSComparator.OrderedDescending;
    }
}
