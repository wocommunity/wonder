package com.webobjects.foundation;

import java.text.DateFormatSymbols;
import java.util.Enumeration;
import java.util.Locale;

import org.junit.Assert;

import er.erxtest.ERXTestCase;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXValueUtilities;

public class NSTimestampTest extends ERXTestCase {

    static NSTimestamp refDate = new NSTimestamp(1970, 1, 1, 0, 0, 0, NSTimeZone.getGMT());

    static final int SUN = 0;
    static final int MON = 1;
    static final int TUE = 2;
    static final int WED = 3;
    static final int THU = 4;
    static final int FRI = 5;
    static final int SAT = 6;

    static final int JAN = 1;
    static final int FEB = 2;
    static final int MAR = 3;
    static final int APR = 4;
    static final int MAY = 5;
    static final int JUN = 6;
    static final int JUL = 7;
    static final int AUG = 8;
    static final int SEP = 9;
    static final int OCT = 10;
    static final int NOV = 11;
    static final int DEC = 12;

    private static int day(String name) {
        if (name.equals("MON")) return MON;
        if (name.equals("TUE")) return TUE;
        if (name.equals("WED")) return WED;
        if (name.equals("THU")) return THU;
        if (name.equals("FRI")) return FRI;
        if (name.equals("SAT")) return SAT;
        if (name.equals("SUN")) return SUN;
        return -1;
    }

    public void testConstructors() {
        // public com.webobjects.foundation.NSTimestamp();
        // public com.webobjects.foundation.NSTimestamp(long);
        // public com.webobjects.foundation.NSTimestamp(long, int);
        // public com.webobjects.foundation.NSTimestamp(long, com.webobjects.foundation.NSTimestamp);
        // public com.webobjects.foundation.NSTimestamp(long, java.util.TimeZone);
        // public com.webobjects.foundation.NSTimestamp(long, int, java.util.TimeZone);
        // public com.webobjects.foundation.NSTimestamp(int, int, int, int, int, int, java.util.TimeZone);
        // public com.webobjects.foundation.NSTimestamp(java.util.Date);
        // public com.webobjects.foundation.NSTimestamp(java.sql.Timestamp);
    }

    public void testCurrentTimeIntervalSinceReferenceDate() {
        // public static long currentTimeIntervalSinceReferenceDate();
    }

    @SuppressWarnings("deprecation")
	public void testDistantFuture () {
        Assert.assertEquals(NSTimestamp.DistantFuture, NSTimestamp.distantFuture());
    }

    @SuppressWarnings("deprecation")
	public void testDistantPast () {
        Assert.assertEquals(NSTimestamp.DistantPast, NSTimestamp.distantPast());
    }

    public void testMillisecondsToTimeInterval () {
        // public static long millisecondsToTimeInterval(long);
    }

    public void testTimeIntervalToMilliseconds () {
        // public static long timeIntervalToMilliseconds(long);
    }

    public void testClassForCoder () {
        // public java.lang.Class classForCoder();
    }

    public void testDecodeObject () {
        // public static java.lang.Object decodeObject(com.webobjects.foundation.NSCoder);
    }

    public void testEncodeWithCoder () {
        // public void encodeWithCoder(com.webobjects.foundation.NSCoder);
    }

	@SuppressWarnings("deprecation")
	public void testIncrementAcrossDST() {

        // Set up values used throughout this test.
        //
        NSTimeZone tz = null;
        NSTimestamp ts1, ts2;
        NSTimestampFormatter  formatter = null;
        StringBuffer dt = null;
        java.text.FieldPosition fp = new java.text.FieldPosition(0);

        NSDictionary data = (NSDictionary)NSPropertyListSerialization.propertyListWithPathURL(ERXFileUtilities.pathURLForResourceNamed("dates.plist", null, null));

        Enumeration dsts = ((NSArray)data.objectForKey("daylightSavingTimeTransitions")).objectEnumerator();

        while (dsts.hasMoreElements()) {
            NSDictionary dst = (NSDictionary)dsts.nextElement();

            // System.out.println("dst: "+dst);

            int year = ERXValueUtilities.intValue(dst.objectForKey("year"));
            int month = ERXValueUtilities.intValue(dst.objectForKey("month"));
            int day = ERXValueUtilities.intValue(dst.objectForKey("day"));
            tz = NSTimeZone.timeZoneWithName((String)dst.objectForKey("tz"), false);

            formatter = new NSTimestampFormatter("%Y %b %d %H:%M:%S %z", new DateFormatSymbols(Locale.US));
            formatter.setDefaultFormatTimeZone(tz);

            String before = (String)dst.objectForKey("before");
            String after = (String)dst.objectForKey("after");

            ts1 = new NSTimestamp(year, month, day, 1, 59, 59, tz);
            dt = new StringBuffer();
            formatter.format(ts1, dt, fp);
            // System.out.println("before: expected = \""+before+"\", found = \""+dt+"\"");
            Assert.assertEquals(before, dt.toString());

            ts2 = ts1.timestampByAddingGregorianUnits(0,0,0,0,0,2);
            dt = new StringBuffer();
            formatter.format(ts2, dt, fp);
            // System.out.println("after: expected = \""+after+"\", found = \""+dt+"\"");
            Assert.assertEquals(after, dt.toString());
        }
    }

    public void testTimestampByAddingGregorianUnits () {
        NSTimestamp ts1, ts2;

        ts1 = new NSTimestamp(1970, JAN, 1, 12, 0, 0, NSTimeZone.getGMT());

        ts2 = new NSTimestamp(1970, JAN, 1, 12, 0, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,0,0,0,0,0));

        ts2 = new NSTimestamp(1971, JAN, 1, 12, 0, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(1,0,0,0,0,0));

        ts2 = new NSTimestamp(1969, JAN, 1, 12, 0, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(-1,0,0,0,0,0));

        ts2 = new NSTimestamp(1970, FEB, 1, 12, 0, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,1,0,0,0,0));

        ts2 = new NSTimestamp(1969, DEC, 1, 12, 0, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,-1,0,0,0,0));

        ts2 = new NSTimestamp(1970, JAN, 2, 12, 0, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,0,1,0,0,0));

        ts2 = new NSTimestamp(1969, DEC, 31, 12, 0, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,0,-1,0,0,0));

        ts2 = new NSTimestamp(1970, JAN, 1, 13, 0, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,0,0,1,0,0));

        ts2 = new NSTimestamp(1970, JAN, 1, 11, 0, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,0,0,-1,0,0));

        ts2 = new NSTimestamp(1970, JAN, 1, 12, 1, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,0,0,0,1,0));

        ts2 = new NSTimestamp(1970, JAN, 1, 11, 59, 0, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,0,0,0,-1,0));

        ts2 = new NSTimestamp(1970, JAN, 1, 12, 0, 1, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,0,0,0,0,1));

        ts2 = new NSTimestamp(1970, JAN, 1, 11, 59, 59, NSTimeZone.getGMT());
        Assert.assertEquals(ts2, ts1.timestampByAddingGregorianUnits(0,0,0,0,0,-1));
    }

    public void testTimestampByAddingTimeInterval () {
        // public com.webobjects.foundation.NSTimestamp timestampByAddingTimeInterval(long);
    }

    public void testDayOfCommonEra () {
        // public long dayOfCommonEra();
    }

    @SuppressWarnings("deprecation")
	public void testDayOfMonth () {
        Assert.assertEquals(1, refDate.dayOfMonth());
    }

    @SuppressWarnings("deprecation")
	public void testDayOfWeek() {
        Assert.assertEquals(THU, refDate.dayOfWeek());
        Assert.assertEquals(WED, (new NSTimestamp(2009, MAY, 6, 0, 0, 0, NSTimeZone.getGMT())).dayOfWeek());
        Assert.assertEquals(FRI, (new NSTimestamp(2079, DEC, 1, 0, 0, 0, NSTimeZone.getGMT())).dayOfWeek());
    }

	@SuppressWarnings("deprecation")
	public void testFirstDaysOfYears() {
        NSDictionary data = (NSDictionary)NSPropertyListSerialization.propertyListWithPathURL(ERXFileUtilities.pathURLForResourceNamed("dates.plist", null, null));

        NSDictionary daysDict = (NSDictionary)data.objectForKey("firstDayForYears");
      
        Enumeration days = daysDict.allKeys().objectEnumerator();
        while (days.hasMoreElements()) {
            String key = (String)days.nextElement();
            int year = -1;
            try {
                year = (new Integer(key)).intValue();
            } catch (java.lang.NumberFormatException nfe) { }
            String dayName = (String)daysDict.objectForKey(key);
            Assert.assertEquals(day(dayName), (new NSTimestamp(year, JAN, 1, 0, 0, 0, NSTimeZone.getGMT())).dayOfWeek());
        } 
    }

    public void testDayOfYear () {
        // public int dayOfYear();
    }

    public void testHourOfDay () {
        // public int hourOfDay();
    }

    public void testMicrosecondOfSecond () {
        // public int microsecondOfSecond();
    }

    public void testMinuteOfHour () {
        // public int minuteOfHour();
    }

    public void testMonthOfYear () {
        //Assert.assertEquals(Calendar.JANUARY, refDate.monthOfYear());
    }

    public void testSecondOfMinute () {
        //Assert.assertEquals(0, refDate.monthOfYear());
    }

    public void testYearOfCommonEra () {
        //Assert.assertEquals(1970, refDate.yearOfCommonEra());
    }

    public void testGregorianUnitsSinceTimestamp () {
        // public void gregorianUnitsSinceTimestamp(com.webobjects.foundation.NSTimestamp$IntRef, com.webobjects.foundation.NSTimestamp$IntRef, com.webobjects.foundation.NSTimestamp$IntRef, com.webobjects.foundation.NSTimestamp$IntRef, com.webobjects.foundation.NSTimestamp$IntRef, com.webobjects.foundation.NSTimestamp$IntRef, com.webobjects.foundation.NSTimestamp);
    }

    public void testTimeIntervalSinceTimestamp () {
        // public long timeIntervalSinceTimestamp(com.webobjects.foundation.NSTimestamp);
    }

    public void testTimeIntervalSinceNow () {
        // public long timeIntervalSinceNow();
    }

    public void testTimeIntervalSinceReferenceDate () {
        // public long timeIntervalSinceReferenceDate();
    }

    public void testCompare () {
        // public int compare(com.webobjects.foundation.NSTimestamp);
    }

    public void testEarlierTimestamp () {
        // public com.webobjects.foundation.NSTimestamp earlierTimestamp(com.webobjects.foundation.NSTimestamp);
    }

    public void testLaterTimestamp () {
        // public com.webobjects.foundation.NSTimestamp laterTimestamp(com.webobjects.foundation.NSTimestamp);
    }

    public void testToString () {
        // public java.lang.String toString();
    }

    public void testTimeZone () {
        // public com.webobjects.foundation.NSTimeZone timeZone();
    }

    public void testSetNanos () {
        // public void setNanos(int);
    }

    public void testSetDate () {
        // public void setDate(int);
    }

    public void testSetHours () {
        // public void setHours(int);
    }

    public void testSetMinutes () {
        // public void setMinutes(int);
    }

    public void testSetMonth () {
        // public void setMonth(int);
    }

    public void testSetSeconds () {
        // public void setSeconds(int);
    }

    public void testSetTime () {
        // public void setTime(long);
    }

    public void testGetTime () {
        // public long getTime();
    }

    public void testgetNanos () {
        // public int getNanos();
    }

    public void testSetYear () {
        // public void setYear(int);
    }
}

