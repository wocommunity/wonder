package er.extensions.foundation;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.webobjects.foundation.NSComparator;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

/**
 * Collection of {@link com.webobjects.foundation.NSTimestamp NSTimestamp} utilities.
 */
public class ERXTimestampUtilities {
    /** caches date formatter the first time it is used */
    private static NSTimestampFormatter _gregorianDateFormatterForJavaDate;

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
        /** the number of days elapsed from 4713 BC to 1 C.E. */
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
        return now.ts.timestampByAddingGregorianUnits(0, 0, 0, -now.hourOfDay(), -now.minuteOfHour(), -now.secondOfMinute());
    }

    /**
     * Timestamp representing tomorrow (12:00 AM). Implementation
     * wise this method subtracts the current hours, minutes and
     * seconds from the current time and then adds one day.
     * @return timestamp for tomorrow.
     */
    public static NSTimestamp tomorrow() {
        ERXTimestamp now = getInstance();
        return now.ts.timestampByAddingGregorianUnits(0, 0, 1, -now.hourOfDay(), -now.minuteOfHour(), -now.secondOfMinute());
    }

    /**
     * Timestamp representing yesterday (12:00 AM). Implementation
     * wise this method subtracts the current hours, minutes and
     * seconds from the current time and then subtracts one day.
     * @return timestamp for yesterday.
     */
    public static NSTimestamp yesterday() {
        ERXTimestamp now = getInstance();
        return now.ts.timestampByAddingGregorianUnits(0, 0, -1, -now.hourOfDay(), -now.minuteOfHour(), -now.secondOfMinute());
    }

    /**
     * Cover method for returning DistantPast
     * off of NSTimestamp.
     * @deprecated use {@link NSTimestamp#DistantPast}
     * @return a date in the distant past
     */
    @Deprecated
    public static NSTimestamp distantPast() {
       return NSTimestamp.DistantPast;
    }

    /**
     * Cover method for returning DistantFuture
     * off of NSTimestamp.
     * @deprecated use {@link NSTimestamp#DistantFuture}
     * @return a date in the distant future
     */
    @Deprecated
    public static NSTimestamp distantFuture() {
        return NSTimestamp.DistantFuture;
    }

    /**
     * @deprecated use {@link #timestampByAddingTime(NSTimestamp, NSTimestamp)}
     */
    @Deprecated
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
     * @deprecated use {@link Timestamp#before(Timestamp)}
     * @param ts1 first timestamp
     * @param ts2 second timestamp
     * @return true if the the second timestamp is earlier than the
     *		first timestamp.
     */
    @Deprecated
    public static boolean isEarlierThan(NSTimestamp ts1, NSTimestamp ts2) {
        return ts1.compare(ts2) == NSComparator.OrderedAscending;
    }

    /**
     * Compares two timestamps.
     * @deprecated use {@link Timestamp#after(Timestamp)}
     * @param ts1 first timestamp
     * @param ts2 second timestamp
     * @return true if the the second timestamp is later than the
     *		first timestamp.
     */
    @Deprecated
    public static boolean isLaterThan(NSTimestamp ts1, NSTimestamp ts2) {
        return ts1.compare(ts2) == NSComparator.OrderedDescending;
    }

    /************** Start Of UnixTimeAdditions ***************/

    /** holds a static reference to the epoch */
    static NSTimestamp _epoch = new NSTimestamp(1970, 1, 1, 0, 0, 0, null);

    /**
     * Utility method used to return the epoch,
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
        return Integer.valueOf((int)((seconds-60*60)/1000L));
    }

    /**
     * Returns the SimpleDateFormat pattern given an NSTimestampFormatter pattern. Note that these are not
     * 100% compatible -- SimpleDateFormat properly implements DST and TimeZones whereas NSTimestampFormatter
     * is ... kind of whacked, so you may notice your dates are off by a DST amount.
     * 
     * @param timestampFormatterPattern the NSTimestampFormatter pattern
     * @return a SimpleDateFormat pattern
     */
    public static String simpleDateFormatForNSTimestampFormat(String timestampFormatterPattern) {
    	StringBuilder dateFormat = new StringBuilder(timestampFormatterPattern.length());
    	int length = timestampFormatterPattern.length();
    	for (int i = 0; i < length; i ++) {
    		char ch = timestampFormatterPattern.charAt(i);
    		if (ch == '%') {
    			char nextCh = timestampFormatterPattern.charAt(++ i);
    			switch (nextCh) {
    			case '%':
    				dateFormat.append('%');
    				break;
    			case 'a':
    				dateFormat.append("EEE");
    				break;
    			case 'A':
    				dateFormat.append("EEEEE");
    				break;
    			case 'b':
    				dateFormat.append("MMM");
    				break;
    			case 'B':
    				dateFormat.append("MMMMM");
    				break;
    			case 'c':
    				dateFormat.append(((SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)).toPattern());
    				break;
    			case 'd':
    				dateFormat.append("dd");
    				break;
    			case 'e':
    				dateFormat.append('d');
    				break;
    			case 'F':
    				dateFormat.append("SSS");
    				break;
    			case 'H':
    				dateFormat.append("HH");
    				break;
    			case 'I':
    				dateFormat.append("hh");
    				break;
    			case 'j':
    				dateFormat.append("DDD");
    				break;
    			case 'm':
    				dateFormat.append("MM");
    				break;
    			case 'M':
    				dateFormat.append("mm");
    				break;
    			case 'p':
    				dateFormat.append("aa");
    				break;
    			case 'S':
    				dateFormat.append("ss");
    				break;
    			case 'w':
    				dateFormat.append("EEE"); // ???
    				break;
    			case 'x':
    				dateFormat.append(((SimpleDateFormat)SimpleDateFormat.getDateInstance(DateFormat.SHORT)).toPattern());
    				break;
    			case 'X':
    				dateFormat.append(((SimpleDateFormat)SimpleDateFormat.getTimeInstance(DateFormat.SHORT)).toPattern());
    				break;
    			case 'y':
    				dateFormat.append("yy");
    				break;
    			case 'Y':
    				dateFormat.append("yyyy");
    				break;
    			case 'z':
    				dateFormat.append('Z');
    				break;
    			case 'Z':
    				dateFormat.append('z');
    				break;
    			default:
    				dateFormat.append('%'); // (this is what NSTimstampFormatter did)
    			}
    		}
    		else {
    			dateFormat.append(ch);
    		}
    	}
    	return dateFormat.toString();
    }

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
        return new NSTimestamp(yearOfCommonEra(value), monthOfYear(value), -dayOfWeek(value) + 1, 0, 0, 0, NSTimeZone.defaultTimeZone());
    }

    public static NSTimestamp firstDateInSameMonth(NSTimestamp value) {
        return new NSTimestamp(yearOfCommonEra(value), monthOfYear(value), -dayOfMonth(value) + 1, 0, 0, 0, NSTimeZone.defaultTimeZone());
    }

    public static NSTimestamp firstDateInNextMonth(NSTimestamp value) {
        return firstDateInSameMonth(value).timestampByAddingGregorianUnits(0, 1, 0, 0, 0, 0);
    }

    public static long compareDatesInCommonEra(NSTimestamp t1, NSTimestamp t2, int mode) {
        return offsetForDateInCommonEra(t2, mode) - offsetForDateInCommonEra(t1, mode);
    }

    public static int dayOfCommonEra(NSTimestamp t) {
        return yearOfCommonEra(t) * 365 + dayOfYear(t);
    }

    public static int monthOfCommonEra(NSTimestamp t) {
        return yearOfCommonEra(t) * 12 + monthOfYear(t);
    }

    public static int weekOfCommonEra(NSTimestamp t) {
        return yearOfCommonEra(t) * 12 + weekOfYear(t);
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

    /**
     * Utility method to return a standard timestamp
     * formatter for the default string representation
     * of java dates.
     * @return timestamp formatter for java dates.
     */
    public static NSTimestampFormatter gregorianDateFormatterForJavaDate() {
        if (_gregorianDateFormatterForJavaDate == null)
            _gregorianDateFormatterForJavaDate = new NSTimestampFormatter("%a %b %d %H:%M:%S %Z %Y");
        return _gregorianDateFormatterForJavaDate;
    }
}
