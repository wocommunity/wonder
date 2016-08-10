package er.ajax.example2.components;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;

import er.ajax.example2.util.Day;
import er.ajax.example2.util.Month;
import er.ajax.example2.util.Week;

public class Calendar extends AjaxWOWODCPage {
  public Month _selectedMonth;
  public Week _repetitionWeek;
  public Day _repetitionDay;
  public Day _selectedDay;

  public Calendar(WOContext context) {
    super(context);
    _selectedMonth = Month.thisMonth();
  }

  @Override
  protected boolean useDefaultComponentCSS() {
    return true;
  }

  public String dayClass() {
	StringBuilder dayClass = new StringBuilder();
    dayClass.append("day");
    if (_repetitionDay.weekend()) {
      dayClass.append(" weekend");
    }
    if (_repetitionDay.today()) {
      dayClass.append(" today");
    }
    return dayClass.toString();
  }

  public WOActionResults selectDay() {
    _selectedDay = _repetitionDay;
    return null;
  }
}