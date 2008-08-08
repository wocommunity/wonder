package er.ajax.example2.util;

import java.util.Calendar;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

public class Week {

	private Day _startDay;
	private Day _endDay;
	
	
	public static Week containingWeekForDay(Day day) {
		Calendar baseCalendar = day.startCalendar();
		
		Calendar sundayCal = (Calendar) baseCalendar.clone();
		sundayCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		Day sunday = Day.day(sundayCal);
		
		Calendar saturdayCal = (Calendar) baseCalendar.clone();
		saturdayCal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		Day saturday = Day.day(saturdayCal);
		
		Week week = new Week();
		week.setStartDay(sunday);
		week.setEndDay(saturday);
		
		return week;
	}
	
	public static Week thisWeek() {
		return containingWeekForDay(Day.todayDay());
	}
	
	public void setEndDay(Day endDay) {
		_endDay = endDay;
	}
	
	public Day getEndDay() {
		return _endDay;
	}
	
	public NSTimestamp getEndTime() {
		return _endDay.endDate();
	}
	
	public void setStartDay(Day startDay) {
		_startDay = startDay;
	}
	
	public Day getStartDay() {
		return _startDay;
	}
	
	public NSTimestamp getStartTime() {
		return _startDay.startDate();
	}
	
	public Week nextWeek() {
		return Week.containingWeekForDay(_endDay.daysFromNow(1));
	}
	
	public Week previousWeek() {
		return Week.containingWeekForDay(_startDay.daysFromNow(-1));
	}
	
	@Override
	public int hashCode() {
		return _startDay.hashCode() + _endDay.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Week && ((Week)obj)._startDay.equals(_startDay) && ((Week)obj)._endDay.equals(_endDay));
	}
	
	public NSArray<Day> getDays() {
		return DateUtils.daysBetween(_startDay.startDate(), _endDay.startDate());
	}
	
	public boolean containsDay(Day day) {
		return day.after(_startDay) && day.before(_endDay);
	}
	
	@Override
	public String toString() {
		return "Week: [" + getStartDay() + ", " + getEndDay() + "]";
	}
	
	public static void main(String[] args) {
		Week week = Week.containingWeekForDay(Day.todayDay().daysFromNow(2));
		
		System.out.println("Week.main: " + week.getStartDay() + " - " + week.getEndDay());
		System.out.println("Week.main: " + week.getDays());
	}
}
