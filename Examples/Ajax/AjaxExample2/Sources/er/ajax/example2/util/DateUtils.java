package er.ajax.example2.util;

import java.util.Calendar;
import java.util.Date;

import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestamp;
import com.webobjects.foundation.NSTimestampFormatter;

import er.ajax.example2.helper.NSTimestampHelper;
import er.extensions.eof.ERXQ;
import er.extensions.qualifiers.ERXAndQualifier;
import er.extensions.qualifiers.ERXKeyValueQualifier;
import er.extensions.qualifiers.ERXOrQualifier;

public class DateUtils {
  public static final NSTimestampFormatter MONTH_NAME_AND_YEAR_FORMATTER = new NSTimestampFormatter("%B %Y");
  public static final NSTimestampFormatter MONTH_NAME_FORMATTER = new NSTimestampFormatter("%B");
  public static final NSTimestampFormatter SHORT_MONTH_NAME_FORMATTER = new NSTimestampFormatter("%b");
  public static final NSTimestampFormatter SHORT_MONTH_NAME_AND_YEAR_FORMATTER = new NSTimestampFormatter("%b %y");
  public static final NSTimestampFormatter SHORT_MONTH_NAME_AND_DAY_OF_WEEK_AND_YEAR_FORMATTER = new NSTimestampFormatter("%a, %b %d %y");
  public static final NSTimestampFormatter SHORT_MONTH_NAME_AND_DAY_OF_WEEK_FORMATTER = new NSTimestampFormatter("%a, %b %d");
  public static final NSTimestampFormatter SHORT_TIME_FORMATTER = new NSTimestampFormatter("%I:%M%p");

  public static final NSTimestampFormatter URL_FRIENDLY_FORMATTER = new NSTimestampFormatter("%Y%m%dT%H%M%S");

  public static final String[] DAYS_OF_WEEK = new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
  public static final String[] MONTHS = new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

  static {
    MONTH_NAME_AND_YEAR_FORMATTER.setDefaultFormatTimeZone(NSTimeZone.getGMT());
    MONTH_NAME_FORMATTER.setDefaultFormatTimeZone(NSTimeZone.getGMT());
    SHORT_MONTH_NAME_FORMATTER.setDefaultFormatTimeZone(NSTimeZone.getGMT());
    SHORT_MONTH_NAME_AND_YEAR_FORMATTER.setDefaultFormatTimeZone(NSTimeZone.getGMT());
    SHORT_MONTH_NAME_AND_DAY_OF_WEEK_AND_YEAR_FORMATTER.setDefaultFormatTimeZone(NSTimeZone.getGMT());
    SHORT_MONTH_NAME_AND_DAY_OF_WEEK_FORMATTER.setDefaultFormatTimeZone(NSTimeZone.getGMT());
    SHORT_TIME_FORMATTER.setDefaultFormatTimeZone(NSTimeZone.getGMT());
  }

  public static EOQualifier dateWithinKeypathRangeQualifier(String startDateKey, String endDateKey, NSTimestamp date, boolean exclusive) {
    NSMutableArray<EOQualifier> startQualifiers = new NSMutableArray<>();
    if (exclusive) {
      startQualifiers.addObject(new ERXKeyValueQualifier(startDateKey, EOQualifier.QualifierOperatorLessThan, date));
    }
    else {
      startQualifiers.addObject(new ERXKeyValueQualifier(startDateKey, EOQualifier.QualifierOperatorEqual, null));
      startQualifiers.addObject(new ERXKeyValueQualifier(startDateKey, EOQualifier.QualifierOperatorLessThanOrEqualTo, date));
    }
    EOQualifier startQualifier = new ERXOrQualifier(startQualifiers);

    NSMutableArray<EOQualifier> endQualifiers = new NSMutableArray<>();
    if (exclusive) {
      endQualifiers.addObject(new ERXKeyValueQualifier(endDateKey, EOQualifier.QualifierOperatorGreaterThan, date));
    }
    else {
      endQualifiers.addObject(new ERXKeyValueQualifier(endDateKey, EOQualifier.QualifierOperatorEqual, null));
      endQualifiers.addObject(new ERXKeyValueQualifier(endDateKey, EOQualifier.QualifierOperatorGreaterThanOrEqualTo, date));
    }
    EOQualifier endQualifier = new ERXOrQualifier(endQualifiers);

    EOAndQualifier qualifier = new ERXAndQualifier(new NSArray<>(new EOQualifier[] { startQualifier, endQualifier }));
    return qualifier;

    /*
    System.out.println("DateUtils.dateWithinKeypathRangeQualifier: " + startDateKey + ", " + endDateKey + ", " + date);
    
    EOQualifier startKeyBeforeDate = ERXQ.lessThan(startDateKey, date);
    EOQualifier startKeyOnDate = ERXQ.equals(startDateKey, date);
    EOQualifier startKeyAfterDate = ERXQ.greaterThan(startDateKey, date);
    
    EOQualifier endKeyBeforeDate = ERXQ.lessThan(endDateKey, date);
    EOQualifier endKeyOnDate = ERXQ.equals(endDateKey, date);
    EOQualifier endKeyAfterDate = ERXQ.greaterThan(endDateKey, date);
    
    EOQualifier qualifier;
    
    if(exclusive) {
    	qualifier = ERXQ.and(startKeyBeforeDate, endKeyAfterDate);
    } else {
    	qualifier = ERXQ.and(ERXQ.or(startKeyBeforeDate, startKeyOnDate), ERXQ.or(endKeyAfterDate, endKeyOnDate));
    }
    
    return qualifier;
    */
  }

  public static EOQualifier dateRangeOverlapsKeypathRangeQualifier(String startDateKey, String endDateKey, NSTimestamp startDate, NSTimestamp endDate, boolean exclusive) {
    if (endDate == null) {
      return DateUtils.dateWithinKeypathRangeQualifier(startDateKey, endDateKey, startDate, exclusive);
    }
    else if (startDate == null) {
      return DateUtils.dateWithinKeypathRangeQualifier(startDateKey, endDateKey, endDate, exclusive);
    }
    else if (endDate.before(startDate)) {
      throw new IllegalArgumentException("End date must be after start date (start date = " + startDate + ", end date = " + endDate + ")");
    }

    EOQualifier startKeyBeforeStartDate = ERXQ.lessThan(startDateKey, startDate);
    EOQualifier startKeyBeforeEndDate = ERXQ.lessThan(startDateKey, endDate);
    EOQualifier startKeyOnStartDate = ERXQ.equals(startDateKey, startDate);
    EOQualifier startKeyOnEndDate = ERXQ.equals(startDateKey, endDate);
    EOQualifier startKeyAfterStartDate = ERXQ.greaterThan(startDateKey, startDate);
    EOQualifier startKeyAfterEndDate = ERXQ.greaterThan(startDateKey, endDate);

    EOQualifier endKeyBeforeStartDate = ERXQ.lessThan(endDateKey, startDate);
    EOQualifier endKeyBeforeEndDate = ERXQ.lessThan(endDateKey, endDate);
    EOQualifier endKeyOnStartDate = ERXQ.equals(endDateKey, startDate);
    EOQualifier endKeyOnEndDate = ERXQ.equals(endDateKey, endDate);
    EOQualifier endKeyAfterStartDate = ERXQ.greaterThan(endDateKey, startDate);
    EOQualifier endKeyAfterEndDate = ERXQ.greaterThan(endDateKey, endDate);

    // Around Start
    EOQualifier aroundStart = ERXQ.and(startKeyAfterStartDate, startKeyBeforeEndDate);

    // Ends At Start
    EOQualifier endsAtStart = ERXQ.and(startKeyAfterStartDate, startKeyOnEndDate);

    // Surrounding
    EOQualifier surrounding = ERXQ.and(startKeyBeforeStartDate, endKeyAfterEndDate);

    // Directly Overlapping
    EOQualifier directlyOverlapping = ERXQ.and(startKeyOnStartDate, endKeyOnEndDate);

    // In Between
    EOQualifier inBetween = ERXQ.and(startKeyBeforeStartDate, endKeyAfterStartDate, endKeyAfterStartDate, endKeyAfterEndDate);

    // Around End
    EOQualifier aroundEnd = ERXQ.and(endKeyAfterStartDate, endKeyBeforeEndDate);

    // Starts At End
    EOQualifier startsAtEnd = ERXQ.and(endKeyOnStartDate, endKeyBeforeEndDate);

    EOQualifier qualifier;
    if (exclusive) {
      qualifier = ERXQ.or(aroundStart, surrounding, directlyOverlapping, inBetween, aroundEnd);
    }
    else {
      qualifier = ERXQ.or(aroundStart, startKeyOnStartDate, endKeyOnStartDate, endsAtStart, surrounding, directlyOverlapping, inBetween, aroundEnd, endKeyOnEndDate, startsAtEnd, startKeyOnEndDate);
    }

    return qualifier;
  }

  public static EOQualifier keypathWithinDateRangeQualifier(String _startDateKey, NSTimestamp _startDate, NSTimestamp _endDate) {
    if (_startDate != null) {
      if (_endDate != null) {
        EOQualifier qualifier1 = new ERXKeyValueQualifier(_startDateKey, EOQualifier.QualifierOperatorGreaterThanOrEqualTo, _startDate);
        EOQualifier qualifier2 = new ERXKeyValueQualifier(_startDateKey, EOQualifier.QualifierOperatorLessThanOrEqualTo, _endDate);
        return new ERXAndQualifier(new NSArray<>(new EOQualifier[] { qualifier1, qualifier2 }));
      }
      return new ERXKeyValueQualifier(_startDateKey, EOQualifier.QualifierOperatorGreaterThanOrEqualTo, _startDate);
    }
    else if (_endDate != null) {
      return new ERXKeyValueQualifier(_startDateKey, EOQualifier.QualifierOperatorLessThanOrEqualTo, _endDate);
    }

    return null;
  }

  public static NSTimestamp timestamp(int _year, int _month, int _day, int _hour, int _minute, int _second, int _millisecond) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, _year);
    cal.set(Calendar.MONTH, _month - 1);
    cal.set(Calendar.DAY_OF_MONTH, _day);
    cal.set(Calendar.HOUR_OF_DAY, _hour);
    cal.set(Calendar.MINUTE, _minute);
    cal.set(Calendar.SECOND, _second);
    cal.set(Calendar.MILLISECOND, _millisecond);
    NSTimestamp timestamp = new NSTimestamp(cal.getTime().getTime());
    return timestamp;
  }

  public static NSArray years(int _offset, int _count) {
    Calendar now = Calendar.getInstance();
    int year = now.get(Calendar.YEAR);
    return DateUtils.years(year, _offset, _count);
  }

  public static NSArray<Integer> years(int _year, int _offset, int _count) {
    NSMutableArray<Integer> years = new NSMutableArray<>();
    for (int i = 0; i < _count; i++) {
      years.addObject(Integer.valueOf(_year + _offset + i));
    }
    return years;
  }

  public static int getDurationInDays(Day _startDay, Day _endDay, boolean _inWeekDaysOnly) {
    int days = 0;

    Calendar start = _startDay.startCalendar();
    Calendar end = _endDay.startCalendar();

    while (!start.after(end)) {
      int dayOfWeek = start.get(Calendar.DAY_OF_WEEK);
      boolean weekend = (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY);
      if (!_inWeekDaysOnly || !weekend) {
        days++;
      }
      start.add(Calendar.DATE, 1);
    }

    return days;
  }

  public static String getDurationAsString(NSTimestamp _startTime, NSTimestamp _endTime) {
	StringBuilder buff = new StringBuilder();
    long seconds = getDurationInSeconds(_startTime, _endTime);
    long hours = seconds / 3600;
    long minutes = (seconds % 3600) / 60;

    buff.append(hours);
    buff.append(":");
    buff.append(minutes);
    buff.append(":");
    buff.append(seconds % 60);

    return buff.toString();
  }

  public static long getDurationInDays(NSTimestamp _startTime, NSTimestamp _endTime) {
    Calendar start = getCalendarFromTimestamp(_startTime);
    Calendar end = getCalendarFromTimestamp(_endTime);
    long millis = end.getTimeInMillis() - start.getTimeInMillis();
    return millis / 1000 / 60 / 60 / 24;
  }

  public static long getDurationInHours(NSTimestamp _startTime, NSTimestamp _endTime) {
    Calendar start = getCalendarFromTimestamp(_startTime);
    Calendar end = getCalendarFromTimestamp(_endTime);
    long millis = end.getTimeInMillis() - start.getTimeInMillis();
    return millis / 1000 / 60 / 60;
  }

  public static long getDurationInMinutes(NSTimestamp _startTime, NSTimestamp _endTime) {
    Calendar start = getCalendarFromTimestamp(_startTime);
    Calendar end = getCalendarFromTimestamp(_endTime);
    long seconds = end.getTimeInMillis() - start.getTimeInMillis();
    return seconds / 1000 / 60;

  }

  public static long getDurationInSeconds(NSTimestamp _startTime, NSTimestamp _endTime) {
    Calendar start = getCalendarFromTimestamp(_startTime);
    Calendar end = getCalendarFromTimestamp(_endTime);
    long seconds = end.getTimeInMillis() - start.getTimeInMillis();
    return seconds / 1000;

  }

  public static Calendar getCalendarFromTimestamp(Date timestamp) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(timestamp);
    return cal;
  }

  public static NSTimestamp daysFrom(int numberOfDays, Date timestamp) {
    Calendar cal = getCalendarFromTimestamp(timestamp);
    cal.add(Calendar.DATE, numberOfDays);
    return new NSTimestamp(cal.getTime());
  }

  /**
   * This method tells whether two timestamps are on the same day, reguardless of time.
   * 
   * @param aTime
   * @param anotherTime
   * @return
   */
  public static boolean isOnTheSameDay(NSTimestamp aTime, NSTimestamp anotherTime) {
    Calendar cal1 = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();

    cal1.setTime(aTime);
    cal2.setTime(anotherTime);

    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);

  }

  /**
   * Returns the difference in units of calendarField (Calendar.MONTH, Calendar.DAY_OF_MONTH, etc) between the two dates. This will return the ceil() value, so one second past 1 month = 2 months.
   * 
   * @param start
   *          the start date
   * @param end
   *          the end date
   * @param calendarField
   *          the calendar field to diff
   * @return the difference between two dates
   */
  public static int difference(NSTimestamp start, NSTimestamp end, int calendarField) {
    Calendar startCal;
    Calendar endCal;
    if (start.after(end)) {
      endCal = DateUtils.getCalendarFromTimestamp(start);
      startCal = DateUtils.getCalendarFromTimestamp(end);
    }
    else {
      startCal = DateUtils.getCalendarFromTimestamp(start);
      endCal = DateUtils.getCalendarFromTimestamp(end);
    }

    long startMillis = startCal.getTimeInMillis();
    long endMillis = endCal.getTimeInMillis();
    double diffMillis = endMillis - startMillis;

    double skipRateDouble = 10.0;
    if (calendarField == Calendar.ERA) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 24.0 * 365.0);
    }
    else if (calendarField == Calendar.YEAR) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 24.0 * 365.0);
    }
    else if (calendarField == Calendar.MONTH) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 24.0 * 30.0);
    }
    else if (calendarField == Calendar.WEEK_OF_YEAR) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 24.0 * 7);
    }
    else if (calendarField == Calendar.WEEK_OF_MONTH) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 24.0 * 7);
    }
    else if (calendarField == Calendar.DATE) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 24.0);
    }
    else if (calendarField == Calendar.DAY_OF_MONTH) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 24.0);
    }
    else if (calendarField == Calendar.DAY_OF_YEAR) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 24.0);
    }
    else if (calendarField == Calendar.DAY_OF_WEEK) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 24.0);
    }
    else if (calendarField == Calendar.DAY_OF_WEEK_IN_MONTH) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 24.0);
    }
    else if (calendarField == Calendar.AM_PM) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0 * 12.0);
    }
    else if (calendarField == Calendar.HOUR) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0);
    }
    else if (calendarField == Calendar.HOUR_OF_DAY) {
      skipRateDouble = diffMillis / (1000.0 * 60.0 * 60.0);
    }
    else if (calendarField == Calendar.MINUTE) {
      skipRateDouble = diffMillis / (1000.0 * 60.0);
    }
    else if (calendarField == Calendar.SECOND) {
      skipRateDouble = diffMillis / (1000.0);
    }
    else if (calendarField == Calendar.MILLISECOND) {
      skipRateDouble = diffMillis;
    }
    else {
      throw new IllegalArgumentException("Unknown calendar field: " + calendarField + ".");
    }

    boolean done = false;
    int skipRate = (int) Math.max(skipRateDouble, 1.0);
    int difference = 0;
    do {
      while (startCal.before(endCal) || startCal.equals(endCal)) {
        startCal.add(calendarField, skipRate);
        difference += skipRate;
      }

      if (skipRate > 1) {
        difference -= skipRate;
        startCal.add(calendarField, -skipRate);
        skipRate -= Math.ceil(skipRate / 2.0);
        skipRate = Math.max(skipRate, 1);
      }
      else {
        done = true;
      }

    } while (!done);

    return difference;
  }

  public static String timestampString(NSTimestamp timestamp, boolean includeTime) {
    String timestampString;
    if (includeTime) {
      if (timestamp != null) {
        timestampString = new NSTimestampHelper().format(timestamp);
      }
      else {
        timestampString = "";
      }
    }
    else {
      if (timestamp != null) {
        timestampString = new NSTimestampHelper().mdy(timestamp);
      }
      else {
        timestampString = "";
      }
    }
    return timestampString;
  }

  public static String timeFrom(NSTimestamp time, NSTimestamp baseTime) {
    return DurationNamer.timeFrom(time, baseTime);
  }

  public static String timeFromNow(NSTimestamp time) {
    return DurationNamer.timeFromNow(time);
  }

  @SuppressWarnings("unchecked")
  public static NSArray<Day> daysBetween(NSTimestamp start, NSTimestamp end) {
    if (start == null && end == null) {
      throw new IllegalArgumentException("Both start and end dates cannot be null");
    }
    if (start == null) {
      return new NSArray(new Day(end));
    }
    if (end == null) {
      return new NSArray(new Day(start));
    }

    if (DateUtils.isOnTheSameDay(start, end)) {
      return new NSArray(new Day(start));
    }

    if (end.before(start)) {
      NSTimestamp temp = start;
      start = end;
      end = temp;
    }

    NSMutableArray dates = new NSMutableArray();

    Day loopDay = new Day(start);
    Day startDay = new Day(start);
    Day endDay = new Day(end);

    while (loopDay.before(endDay)) {
      dates.addObject(loopDay);
      loopDay = loopDay.next();
    }

    dates.addObject(endDay);

    return dates;
  }

  public static String durationStringForURL(NSTimestamp start, NSTimestamp end) {
    int minutes = difference(start, end, Calendar.MINUTE);
    int hours = minutes / 60;
    minutes = minutes % 60;
    String results = "";
    results += String.format("%02d", Integer.valueOf(hours));
    results += String.format("%02d", Integer.valueOf(minutes));
    return results;
  }

  public static void main(String[] args) {
    NSTimestamp start = new NSTimestamp();
    NSTimestamp end = new NSTimestamp().timestampByAddingGregorianUnits(0, 0, 0, 0, 15, 0);
    System.out.println("start: " + start);
    System.out.println("end: " + end);
    System.out.println("duration: " + DateUtils.durationStringForURL(start, end));
  }

  public static NSTimestamp nearestQuarterHour(NSTimestamp time) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(time);
    int minute = cal.get(Calendar.MINUTE);
    int base = minute / 15;
    if (minute % 15 > 7)
      minute = 15;
    else
      minute = 0;
    cal.set(Calendar.MINUTE, (base * 15) + minute);
    return new NSTimestamp(cal.getTime());
  }

  public static NSTimestamp set(NSTimestamp timestamp, int calendarField, int value) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(timestamp);
    cal.set(calendarField, value);
    return new NSTimestamp(cal.getTime());
  }

  public static NSTimestamp add(NSTimestamp timestamp, int calendarField, int value) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(timestamp);
    cal.add(calendarField, value);
    return new NSTimestamp(cal.getTime());
  }
}
