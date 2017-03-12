package er.ajax.example2.util;

import java.util.Calendar;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSTimestamp;

public class Month {
  private int _month;
  private int _year;
  private NSArray<NSArray<Day>> _weeks;
  private NSMutableArray<Week> _weekObjects;

  public Month(Calendar _cal) {
    this(_cal.get(Calendar.MONTH) + 1, _cal.get(Calendar.YEAR));
  }

  public Month(int month, int year) {
    _month = month;
    _year = year;
  }

  public int month() {
    return _month;
  }

  public int year() {
    return _year;
  }

  public Year yearObject() {
    return new Year(_year);
  }

  public void setYear(int year) {
    _year = year;
    _weeks = null;
    _weekObjects = null;
  }

  public String shortMonthName() {
    String monthFormat = DateUtils.SHORT_MONTH_NAME_FORMATTER.format(monthTimestamp());
    return monthFormat;
  }

  public String monthName() {
    String monthFormat = DateUtils.MONTH_NAME_FORMATTER.format(monthTimestamp());
    return monthFormat;
  }

  public String monthAndYearName() {
    String monthFormat = DateUtils.MONTH_NAME_AND_YEAR_FORMATTER.format(monthTimestamp());
    return monthFormat;
  }

  public String shortMonthAndYearName() {
    String monthFormat = DateUtils.SHORT_MONTH_NAME_AND_YEAR_FORMATTER.format(monthTimestamp());
    return monthFormat;
  }

  public Calendar monthCalendar() {
    Calendar monthCalendar = Calendar.getInstance();
    monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
    monthCalendar.set(Calendar.YEAR, _year);
    monthCalendar.set(Calendar.MONTH, _month - 1);
    return monthCalendar;
  }

  public NSTimestamp monthTimestamp() {
    NSTimestamp calendarTimestamp = DateUtils.timestamp(_year, _month, 1, 0, 0, 0, 0);
    return calendarTimestamp;
  }

  @Override
  public int hashCode() {
    return _month + _year;
  }

  @Override
  public boolean equals(Object _obj) {
    Month otherMonth = (Month) _obj;
    boolean equals = (otherMonth != null && (otherMonth._month == _month) && (otherMonth._year == _year));
    return equals;
  }

  public Day firstDay() {
    Calendar monthCalendar = monthCalendar();
    monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
    Day firstDay = Day.day(monthCalendar);
    return firstDay;
  }

  public Day lastDay() {
    Calendar monthCalendar = monthCalendar();
    monthCalendar.set(Calendar.DAY_OF_MONTH, monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
    Day lastDay = Day.day(monthCalendar);
    return lastDay;
  }

  public Month previousMonth() {
    Calendar previousMonthCalendar = monthCalendar();
    previousMonthCalendar.add(Calendar.MONTH, -1);
    Month previousMonth = new Month(previousMonthCalendar);
    return previousMonth;
  }

  public Month nextMonth() {
    Calendar nextMonthCalendar = monthCalendar();
    nextMonthCalendar.add(Calendar.MONTH, 1);
    Month nextMonth = new Month(nextMonthCalendar);
    return nextMonth;
  }

  public NSArray<NSArray<Day>> weeks() {
    if (_weeks == null) {
      Calendar calendar = monthCalendar();
      int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
      int maxDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
      calendar.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - 1));

      NSMutableArray<NSArray<Day>> weeks = new NSMutableArray<NSArray<Day>>();
      int maxDaysInWeek = calendar.getMaximum(Calendar.DAY_OF_WEEK);
      int maxDays = (dayOfWeek - 1) + maxDaysInMonth;
      int daysFromNextMonth = (maxDays % maxDaysInWeek);
      if (daysFromNextMonth != 0) {
        maxDays += (maxDaysInWeek - daysFromNextMonth);
      }

      for (int dayNum = 0; dayNum < maxDays;) {
        NSMutableArray<Day> daysInWeek = new NSMutableArray<>();
        for (int dayOfWeekNum = 0; dayOfWeekNum < maxDaysInWeek; dayOfWeekNum++, dayNum++) {
          Day day = new Day(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
          daysInWeek.addObject(day);
          calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        weeks.addObject(daysInWeek);
      }
      _weeks = weeks;
    }
    return _weeks;
  }

  public NSArray<Week> weekObjects() {
    NSMutableArray<Week> weekObjects = _weekObjects;
    if (weekObjects == null) {
      weekObjects = new NSMutableArray<>();
      weeks();
      for (int i = 0; i < _weeks.count(); i++) {
        NSArray<Day> week = _weeks.get(i);
        weekObjects.add(Week.containingWeekForDay(week.get(0)));
      }
      _weekObjects = weekObjects;
    }
    return weekObjects;
  }

  /**
   * Returns the date range of a "visible" calendar, which
   * includes the couple of days at the end of the previous
   * month and the couple of days at the beginning of the
   * next month (to form a block of weeks).
   * 
   * @return visible date range
   */
  public DateRange visibleDateRange() {
    NSArray<Week> weeks = weekObjects();
    NSTimestamp startDate = weeks.objectAtIndex(0).getStartTime();
    NSTimestamp endDate = weeks.lastObject().getEndTime();
    return new DateRange(startDate, endDate);
  }

  @Override
  public String toString() {
    return "[Month: " + _month + "/" + _year + "]";
  }

  public static Month month(NSTimestamp date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    Month month = new Month(cal);
    return month;
  }

  public static Month firstMonthOfThisYear() {
    Calendar now = Calendar.getInstance();
    Month month = new Month(1, now.get(Calendar.YEAR));
    return month;
  }

  public static Month firstMonthOfYear(int year) {
    Calendar now = Calendar.getInstance();
    Month month = new Month(1, year);
    return month;
  }

  public static NSArray<Month> allMonths(int year) {
    return Month.months(Month.firstMonthOfYear(year), 0, 12);
  }

  public static NSArray<Month> allMonths() {
    return Month.months(Month.firstMonthOfThisYear(), 0, 12);
  }

  public static NSArray<Month> months(Month startingMonth, Month endingMonth) {
    if (endingMonth._year < startingMonth._year || (endingMonth._year == startingMonth._year && endingMonth._month < startingMonth._month)) {
      throw new IllegalArgumentException("Ending month was before starting month.");
    }
    NSMutableArray<Month> months = new NSMutableArray<>();
    Calendar monthCalendar = startingMonth.monthCalendar();
    Month month = startingMonth;
    do {
      months.addObject(month);
      monthCalendar.add(Calendar.MONTH, 1);
      month = new Month(monthCalendar);
    } while (!month.equals(endingMonth));
    return months;
  }

  public static NSArray<Month> months(Month startingMonth, int offset, int count) {
    NSMutableArray<Month> months = new NSMutableArray<>();
    Calendar monthCalendar = startingMonth.monthCalendar();
    monthCalendar.add(Calendar.MONTH, offset);
    for (int i = 0; i < count; i++) {
      months.addObject(new Month(monthCalendar));
      monthCalendar.add(Calendar.MONTH, 1);
    }
    return months;
  }

  public static Month thisMonth() {
    Calendar now = Calendar.getInstance();
    Month thisMonth = new Month(now);
    return thisMonth;
  }
}
