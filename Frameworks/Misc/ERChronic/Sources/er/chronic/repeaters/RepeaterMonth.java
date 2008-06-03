package er.chronic.repeaters;

import java.util.Calendar;

import er.chronic.tags.Pointer;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class RepeaterMonth extends RepeaterUnit {
  private static final int MONTH_SECONDS = 2592000; // 30 * 24 * 60 * 60

  private Calendar _currentMonthStart;

  @Override
  protected Span _nextSpan(PointerType pointer) {
    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    if (_currentMonthStart == null) {
      _currentMonthStart = Time.cloneAndAdd(Time.ym(getNow()), Calendar.MONTH, direction);
    }
    else {
      _currentMonthStart = Time.cloneAndAdd(_currentMonthStart, Calendar.MONTH, direction);
    }

    return new Span(_currentMonthStart, Calendar.MONTH, 1);
  }

  @Override
  public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    return new Span(Time.cloneAndAdd(span.getBeginCalendar(), Calendar.MONTH, amount * direction), Time.cloneAndAdd(span.getEndCalendar(), Calendar.MONTH, amount * direction));
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    Calendar monthStart;
    Calendar monthEnd;
    if (pointer == PointerType.FUTURE) {
      monthStart = Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, 1);
      monthEnd = Time.cloneAndAdd(Time.ym(getNow()), Calendar.MONTH, 1);
    }
    else if (pointer == PointerType.PAST) {
      monthStart = Time.ym(getNow());
      monthEnd = Time.ymd(getNow());
    }
    else if (pointer == PointerType.NONE) {
      monthStart = Time.ym(getNow());
      monthEnd = Time.cloneAndAdd(Time.ym(getNow()), Calendar.MONTH, 1);
    }
    else {
      throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
    }
    return new Span(monthStart, monthEnd);
  }

  @Override
  public int getWidth() {
    // WARN: Does not use Calendar
    return RepeaterMonth.MONTH_SECONDS;
  }

  @Override
  public String toString() {
    return super.toString() + "-month";
  }
}
