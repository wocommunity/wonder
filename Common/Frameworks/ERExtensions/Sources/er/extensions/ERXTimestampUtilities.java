//
// NSTimestampUtilities.java
// Project vwdBussinessLogicJava
//
// Created by ak on Wed Jun 06 2001
//
package er.extensions;

import java.util.*;

import com.webobjects.foundation.*;

/**
 * Collection of {@link com.webobjects.foundation.NSTimestamp NSTimestamp} utilities.
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

        /**
         * Returns the day of the common era. This is the
         * year of the common era multiplied by 365 plus
         * the day of the year.
         * @return day of the common era
         */
        // FIXME: This isn't quite correct as this does not handle leap years
        public int dayOfCommonEra() {
            return yearOfCommonEra()*365 + dayOfYear();
        }

        /**
         * Returns the day of the week as returned by
         * a GregorianCalendar.
         * @return day of the week as an int.
         */
        public int dayOfWeek() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.DAY_OF_WEEK);
        }

        /**
         * Returns the day of the year as returned by
         * a GregorianCalendar.
         * @return day of the year as an int.
         */
        public int dayOfYear() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.DAY_OF_YEAR);
        }

        /**
         * Returns the hour of the day as returned by
         * a GregorianCalendar.
         * @return hour of the day as an int.
         */        
        public int hourOfDay() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.HOUR_OF_DAY);
        }

        /**
         * Returns the minute of the hour as returned by
         * a GregorianCalendar.
         * @return minute of the hour as an int.
         */
        public int minuteOfHour() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.MINUTE);
        }

        /**
         * Returns the seconds of the minute as returned by
         * a GregorianCalendar.
         * @return seconds of the minute as an int.
         */
        public int secondOfMinute() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.SECOND);
        }

        /**
         * Returns the month of the year as returned by
         * a GregorianCalendar.
         * @return month of the year as an int.
         */
        public int monthOfYear() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.MONTH);
        }

        /**
         * Returns the year of the common era as returned by
         * a GregorianCalendar.
         * @return year of the common era as an int.
         */
        public int yearOfCommonEra() {
            _calendar.setTime(ts);
            return _calendar.get(GregorianCalendar.YEAR);
        }
    }

    /**
     * Package level access for returning an
     * instance of the inner class ERXTimestamp for
     * the current time.
     * @return instance of ERXTimestamp for the current
     *		time.
     */
    static ERXTimestamp getInstance() {
        return getInstance(new NSTimestamp());
    }

    /**
     * Package level access for returning an
     * instance of the inner class ERXTimestamp for
     * the given time.
     * @param ts a timestamp
     * @return instance of ERXTimestamp for the given
     *		time.
     */
    static ERXTimestamp getInstance(NSTimestamp ts) {
        return new ERXTimestamp(ts);
    }

    /**
     * Timestamp representing today (12:00 AM). Implementation
     * wise this method subtracts the current hours, minutes and
     * seconds from the current time.
     * @return timestamp for today.
     */    
    public static NSTimestamp today() {
        ERXTimestamp now = getInstance();
        return (new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 0, -now.hourOfDay(), -now.minuteOfHour(), -now.secondOfMinute());
    }

    /**
     * Timestamp representing tomorrow (12:00 AM). Implementation
     * wise this method subtracts the current hours, minutes and
     * seconds from the current time and then adds one day.
     * @return timestamp for tomorrow.
     */    
    public static NSTimestamp tomorrow() {
        ERXTimestamp now = getInstance();
        return (new NSTimestamp()).timestampByAddingGregorianUnits(0, 0, 1, -now.hourOfDay(), -now.minuteOfHour(), -now.secondOfMinute());
    }
    
    /**
     * Timestamp representing yesterday (12:00 AM). Implementation
     * wise this method subtracts the current hours, minutes and
     * seconds from the current time and then subtracts one day.
     * @return timestamp for yesterday.
     */    
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

    // DELTEME: This is the same as timestampByAddingTime
    public static NSTimestamp dateByAddingTime(NSTimestamp ts, NSTimestamp t1) {
        ERXTimestamp time = getInstance(t1);
        return ts.timestampByAddingGregorianUnits(0, 0, 0, time.hourOfDay(), time.minuteOfHour(), time.secondOfMinute());
    }

    /**
     * Adds the time (hours, minutes and seconds) from
     * the second timestamp to the first timestamp.
     * @param ts timestamp to have the time added too.
     * @param t1 timestamp to add the time from
     * @return the first timestamp with the time of the
     *		second added to it.
     */
    public static NSTimestamp timestampByAddingTime(NSTimestamp ts, NSTimestamp t1) {
        ERXTimestamp time = getInstance(t1);
        return ts.timestampByAddingGregorianUnits(0, 0, 0, time.hourOfDay(), time.minuteOfHour(), time.secondOfMinute());
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
     * Converts an offset from the epoch into a
     * timestamp.
     * @param helpedNSNumber number offset from the epoch
     * @return timestamp representation of the offset from
     * 		the epoch.
     */
    public static NSTimestamp unixDate(Number helpedNSNumber) {
        return ERXTimestampUtilities.epoch().timestampByAddingGregorianUnits(0, 0, 0, 0, 0, (int)helpedNSNumber.longValue()+60*60);
    }

    /**
     * Converts a timestamp into the equivalent unix
     * offset from the epoch.
     * @param ts timestamp to be converted
     * @return timestamp represented as an integer offset
     *		from the epoch.
     */
    public static Integer unixTimestamp(NSTimestamp ts) {
        long seconds = 0;
        seconds = ts.getTime() - epoch().getTime();
        return (new Integer((int)((seconds-60*60)/1000L)));
    }    
}
