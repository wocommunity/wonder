package er.ajax.example2.util;

import java.util.Calendar;
import java.util.Date;

import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSTimestamp;

public class Day implements Comparable {
	private int myDayYear;
	private int myDayMonth;
	private int myDay;
	private NSTimestamp myStartDate;
	private NSTimestamp myEndDate;

	public Day(int _year, int _month, int _day) {
		myDayYear = _year;
		myDayMonth = _month;
		myDay = _day;
		myStartDate = DateUtils.timestamp(_year, _month, _day, 0, 0, 0, 0);
		myEndDate = DateUtils.timestamp(_year, _month, _day, 23, 59, 59, 999);
	}

	public Day(NSTimestamp timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(timestamp);

		myStartDate = DateUtils.timestamp(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0, 0);
		myEndDate = DateUtils.timestamp(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59, 999);
		myDayYear = cal.get(Calendar.YEAR);
		myDayMonth = cal.get(Calendar.MONTH) + 1;
		myDay = cal.get(Calendar.DAY_OF_MONTH);
	}

	@Override
	public int hashCode() {
		return myDayYear * 10000 + myDayMonth * 100 + myDay;
	}

	@Override
	public boolean equals(Object _obj) {
		boolean equals = (_obj instanceof Day);
		if (equals) {
			Day otherDay = (Day) _obj;
			equals = otherDay.myDayYear == myDayYear && otherDay.myDayMonth == myDayMonth && otherDay.myDay == myDay;
		}
		return equals;
	}

	public int year() {
		return myDayYear;
	}

	public int month() {
		return myDayMonth;
	}
	
	public Month monthObject() {
	  return new Month(myDayMonth, myDayYear);
	}

	public int day() {
		return myDay;
	}

	public Day next() {
		Calendar dayCal = endCalendar();
		dayCal.add(Calendar.HOUR, 12);
		return Day.day(dayCal);
	}

  public Day previous() {
    Calendar dayCal = startCalendar();
    dayCal.add(Calendar.HOUR, -12);
    return Day.day(dayCal);
  }

	public boolean today() {
		Calendar now = Calendar.getInstance();
		boolean today = myDayYear == now.get(Calendar.YEAR) && myDayMonth == (now.get(Calendar.MONTH) + 1) && myDay == now.get(Calendar.DAY_OF_MONTH);
		return today;
	}

	public boolean weekend() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(myStartDate);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		boolean weekend = (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY);
		return weekend;
	}

	public Calendar startCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(myStartDate);
		return cal;
	}

	public NSTimestamp startDate() {
		return myStartDate;
	}

	public NSTimestamp now() {
		Calendar nowCalendar = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		cal.setTime(myStartDate);
		cal.set(Calendar.HOUR_OF_DAY, nowCalendar.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, nowCalendar.get(Calendar.MINUTE));
		NSTimestamp now = new NSTimestamp(cal.getTime());
		return now;
	}

	public NSTimestamp endDate() {
		return myEndDate;
	}

	/**
	 * Returns the timestamp representing 9am on this day.
	 * 
	 * @return the timestamp representing 9am on this day
	 */
  public NSTimestamp workStartDate() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(myStartDate);
    cal.set(Calendar.HOUR_OF_DAY, 9);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    NSTimestamp workStart = new NSTimestamp(cal.getTime());
    return workStart;
  }

  /**
   * Returns the timestamp representing 6pm on this day.
   * 
   * @return the timestamp representing 6pm on this day
   */
  public NSTimestamp workEndDate() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(myEndDate);
    cal.set(Calendar.HOUR_OF_DAY, 18);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    NSTimestamp workEnd = new NSTimestamp(cal.getTime());
    return workEnd;
  }

	public Calendar endCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(myEndDate);
		return cal;
	}

	public Day monthsAgo(int months) {
		Calendar cal = startCalendar();
		cal.add(Calendar.MONTH, -1 * months);
		return new Day(new NSTimestamp(cal.getTime()));
	}

	public Day monthsFromNow(int months) {
		Calendar cal = startCalendar();
		cal.add(Calendar.MONTH, months);
		return new Day(new NSTimestamp(cal.getTime()));
	}

	public Day yearsAgo(int years) {
		Calendar cal = startCalendar();
		cal.add(Calendar.YEAR, -1 * years);
		return new Day(new NSTimestamp(cal.getTime()));
	}

	public Day yearsFromNow(int years) {
		Calendar cal = startCalendar();
		cal.add(Calendar.YEAR, 1 * years);
		return new Day(new NSTimestamp(cal.getTime()));
	}

	public Day daysAgo(int days) {
		Calendar cal = startCalendar();
		cal.add(Calendar.DATE, -1 * days);
		return new Day(new NSTimestamp(cal.getTime()));
	}

	public Day daysFromNow(int days) {
		Calendar cal = startCalendar();
		cal.add(Calendar.DATE, days);
		return new Day(new NSTimestamp(cal.getTime()));
	}
		
	/**
	 * Returns a qualifier that encompasses this day.
	 * 
	 * @param key the key name to qualify on
	 * @return the day qualifier
	 */
	public EOQualifier qualifier(String key) {
	  return DateUtils.keypathWithinDateRangeQualifier(key, startDate(), endDate());
	}
  
  /**
   * Returns a qualifier that encompasses this working day.
   * 
   * @param key the key name to qualify on
   * @return the working day qualifier
   */
  public EOQualifier workQualifier(String key) {
    return DateUtils.keypathWithinDateRangeQualifier(key, workStartDate(), workEndDate());
  }

	public Day weeksAgo(int weeks) {
		int days  = weeks * 7;
		Calendar cal = startCalendar();
		cal.add(Calendar.DATE, -1 * days);
		return new Day(new NSTimestamp(cal.getTime()));
	}

	public Day weeksFromNow(int weeks) {
		int days = weeks * 7;
		Calendar cal = startCalendar();
		cal.add(Calendar.DATE, days);
		return new Day(new NSTimestamp(cal.getTime()));
	}

	public boolean before(Day day) {
		return startDate().before(day.startDate());
	}

	public boolean after(Day day) {
		return endDate().after(day.endDate());
	}
	
	@Override
	public String toString() {
		return "[Day: " + myDayYear + "/" + myDayMonth + "/" + myDay + "]";
	}

	public static Day todayDay() {
		return Day.day(Calendar.getInstance());
	}

	public static Day day(Date _date) {
	  if (_date == null) {
	    throw new IllegalArgumentException("You must provide a date.");
	  }
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(_date);
		return Day.day(calendar);
	}

	public static Day day(Calendar _calendar) {
		Day today = new Day(_calendar.get(Calendar.YEAR), _calendar.get(Calendar.MONTH) + 1, _calendar.get(Calendar.DAY_OF_MONTH));
		return today;
	}

	public int compareTo(Object o) {
		Day day = (Day) o;
		if (myDayYear == day.myDayYear) {
			if (myDayMonth == day.myDayMonth) {
				if (myDay == day.myDay) {
					return 0;
				} else if (myDay > day.myDay) {
					return 1;
				} else if (myDay < day.myDay) {
					return -1;
				} else {
					throw new IllegalStateException("Invalid comparison state.");
				}
			} else if (myDayMonth > day.myDayMonth) {
				return 1;
			} else if (myDayMonth < day.myDayMonth) {
				return -1;
			} else {
				throw new IllegalStateException("Invalid comparison state.");
			}
		} else if (myDayYear > day.myDayYear) {
			return 1;
		} else if (myDayYear < day.myDayYear) {
			return -1;
		} else {
			throw new IllegalStateException("Invalid comparison state.");
		}
	}
}
