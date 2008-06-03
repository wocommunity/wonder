package er.chronic.repeaters;

import java.util.Calendar;

import er.chronic.tags.Pointer;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class RepeaterDay extends RepeaterUnit {
  public static final int DAY_SECONDS = 86400; // (24 * 60 * 60);

  private Calendar _currentDayStart;

  @Override
  protected Span _nextSpan(PointerType pointer) {
    if (_currentDayStart == null) {
      _currentDayStart = Time.ymd(getNow());
    }

    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    _currentDayStart.add(Calendar.DAY_OF_MONTH, direction);

    return new Span(_currentDayStart, Calendar.DAY_OF_MONTH, 1);
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    Calendar dayBegin;
    Calendar dayEnd;
    if (pointer == PointerType.FUTURE) {
      dayBegin = Time.cloneAndAdd(Time.ymdh(getNow()), Calendar.HOUR, 1);
      dayEnd = Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, 1);
    }
    else if (pointer == PointerType.PAST) {
      dayBegin = Time.ymd(getNow());
      dayEnd = Time.ymdh(getNow());
    }
    else if (pointer == PointerType.NONE) {
      dayBegin = Time.ymd(getNow());
      dayEnd = Time.cloneAndAdd(Time.ymdh(getNow()), Calendar.SECOND, RepeaterDay.DAY_SECONDS);
    }
    else {
      throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
    }
    return new Span(dayBegin, dayEnd);
  }

  @Override
  public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    // WARN: Does not use Calendar
    return span.add(direction * amount * RepeaterDay.DAY_SECONDS);
  }

  @Override
  public int getWidth() {
    // WARN: Does not use Calendar
    return RepeaterDay.DAY_SECONDS;
  }

  @Override
  public String toString() {
    return super.toString() + "-day";
  }
}
