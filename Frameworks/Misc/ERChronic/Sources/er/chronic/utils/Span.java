package er.chronic.utils;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * A Span represents a range of time. Since this class extends
 * Range, you can use #begin and #end to get the beginning and
 * ending times of the span (they will be of class Time)
 */
public class Span extends Range {

  public Span(Calendar begin, int field, long amount) {
    this(begin, Time.cloneAndAdd(begin, field, amount));
  }

  public Span(Calendar begin, Calendar end) {
    this(begin.getTimeInMillis() / 1000, end.getTimeInMillis() / 1000);
  }

  public Span(long begin, long end) {
    super(begin, end);
  }

  public Calendar getBeginCalendar() {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(getBegin() * 1000);
    return cal;
  }

  public Calendar getEndCalendar() {
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(getEnd() * 1000);
    return cal;
  }

  /**
   * Add a number of seconds to this span, returning the 
   * resulting Span
   */
  public Span add(long seconds) {
    return new Span(getBegin() + seconds, getEnd() + seconds);
  }

  /**
   * Subtract a number of seconds to this span, returning the 
   * resulting Span
   */
  public Span subtract(long seconds) {
    return add(-seconds);
  }

  @Override
  public String toString() {
    return "(" + DateFormat.getDateTimeInstance().format(getBeginCalendar().getTime()) + ".." + DateFormat.getDateTimeInstance().format(getEndCalendar().getTime()) + ")";
  }
}
