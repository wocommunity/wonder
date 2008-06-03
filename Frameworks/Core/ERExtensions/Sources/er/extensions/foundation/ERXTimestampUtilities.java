//
// NSTimestampUtilities.java
// Project vwdBussinessLogicJava
//
// Created by ak on Wed Jun 06 2001
//
package er.extensions.foundation;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSTimestamp;

/**
 * Collection of {@link com.webobjects.foundation.NSTimestamp NSTimestamp} utilities.
 */
public class ERXTimestampUtilities extends Object {

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
        /** the number of days elapsed from 4713ÊBC to 1 C.E. */
        private static final long _YEAR_1 = 1721426;
        /** holds the calendar instance */
        private GregorianCalendar _calendar;
        /** holds the timestamp */
        private NSTimestamp ts;

        /**
         * Public constructor.
         * @param value timestamp to be wrapped
         */
        ERXTimestamp(NSTimestamp value) {
            ts = value;
            _calendar = new GregorianCalendar();
            _calendar.setTime(ts);
        }

        /**
         * Returns the day of the common era.
         * Please note that even if leap years are handled but
         * result is not consistent with <code>NSTimestamp<code>'s.
         * The formula used is detailed here:
         * http://www.tondering.dk/claus/cal/node3.html#SECTION003151000000000000000
         * @return day of the common era
         */
        public long dayOfCommonEra() {
            int a = (14- (monthOfYear()+1) )/12;
            int y = yearOfCommonEra() + 4800 - a;
            long m = monthOfYear()+1 + 12*a - 3;
            long julianDays = dayOfMonth() + (153*m + 2)/5 + 365 * y + y/4 - y/100 + y/400 - 32045;
            return julianDays - _YEAR_1;
        }

        /**
         * Returns the day of the week as returned by
         * a GregorianCalendar.
         * @return day of the week as an int.
         */
        public int dayOfWeek() {
            return _calendar.get(Calendar.DAY_OF_WEEK);
        }

        /**
         * Returns the day of the month as returned by
         * a GregorianCalendar.
         * @return day of the month as an int.
         */
        public int dayOfMonth() {
            return _calendar.get(Calendar.DATE);
        }
        
        /**
         * Returns the day of the year as returned by
         * a GregorianCalendar.
         * @return day of the year as an int.
         */
        public int dayOfYear() {
            return _calendar.get(Calendar.DAY_OF_YEAR);
        }

        /**
         * Returns the hour of the day as returned by
         * a GregorianCalendar.
         * @return hour of the day as an int.
         */        
        public int hourOfDay() {
            return _calendar.get(Calendar.HOUR_OF_DAY);
        }

        /**
         * Returns the minute of the hour as returned by
         * a GregorianCalendar.
         * @return minute of the hour as an int.
         */
        public int minuteOfHour() {
            return _calendar.get(Calendar.MINUTE);
        }

        /**
         * Returns the seconds of the minute as returned by
         * a GregorianCalendar.
         * @return seconds of the minute as an int.
         */
        public int secondOfMinute() {
            return _calendar.get(Calendar.SECOND);
        }

        /**
         * Returns the month of the year as returned by
         * a GregorianCalendar.
         * @return month of the year as an int.
         */
        public int monthOfYear() {
            return _calendar.get(Calendar.MONTH);
        }

        /**
         * Returns the year of the common era as returned by
         * a GregorianCalendar.
         * @return year of the common era as an int.
         */
        public int yearOfCommonEra() {
            return _calendar.get(Calendar.YEAR);
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
     * @deprecated use <code>NSTimestamp.DistantPast</code> instead
     * @return a date in the distant past
     */
    public static NSTimestamp distantPast() {
       return NSTimestamp.DistantPast;
    }

    /**
     * Cover method for returning DistantFuture
     * off of NSTimestamp.
     * @deprecated use <code>NSTimestamp.DistantFuture</code> instead
     * @return a date in the distant future
     */    
    public static NSTimestamp distantFuture() {
        return NSTimestamp.DistantFuture;
    }

    /**
     * @deprecated use <code>timestampByAddingTime</code> instead
     */
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
     * @deprecated use <code>java.sql.Timestamp.before<code> instead.
     * @param ts1 first timestamp
     * @param ts2 second timestamp
     * @return true if the the second timestamp is earlier than the
     *		first timestamp.
     */
    public static boolean isEarlierThan(NSTimestamp ts1, NSTimestamp ts2) {
        return ts1.compare(ts2) == NSComparator.OrderedAscending;
    }

    /**
     * Compares two timestamps.
     * @deprecated use <code>java.sql.Timestamp.after<code> instead.
     * @param ts1 first timestamp
     * @param ts2 second timestamp
     * @return true if the the second timestamp is later than the
     *		first timestamp.
     */
    public static boolean isLaterThan(NSTimestamp ts1, NSTimestamp ts2) {
        return ts1.compare(ts2) == NSComparator.OrderedDescending;
    }    
    
    /************** Start Of UnixTimeAdditions ***************/
    
    /** holds a static reference to the epoch */
    static NSTimestamp _epoch = new NSTimestamp(1970, 1, 1, 0, 0, 0, null);

    /**
     * Utility method used to retrun the epoch,
     * Jan 1st, 1970
     * @return the epoch as an NSTimestamp
     */
    public static NSTimestamp epoch() {
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
