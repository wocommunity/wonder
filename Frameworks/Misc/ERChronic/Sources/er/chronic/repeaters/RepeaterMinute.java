package er.chronic.repeaters;

import java.util.Calendar;

import er.chronic.tags.Pointer;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class RepeaterMinute extends RepeaterUnit {
  public static final int MINUTE_SECONDS = 60;

  private Calendar _currentMinuteStart;

  @Override
  protected Span _nextSpan(PointerType pointer) {
    if (_currentMinuteStart == null) {
      if (pointer == PointerType.FUTURE) {
        _currentMinuteStart = Time.cloneAndAdd(Time.ymdhm(getNow()), Calendar.MINUTE, 1);
      }
      else if (pointer == PointerType.PAST) {
        _currentMinuteStart = Time.cloneAndAdd(Time.ymdhm(getNow()), Calendar.MINUTE, -1);
      }
      else {
        throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
      }
    }
    else {
      int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
      _currentMinuteStart.add(Calendar.MINUTE, direction);
    }
    
    return new Span(_currentMinuteStart, Calendar.SECOND, RepeaterMinute.MINUTE_SECONDS);
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    Calendar minuteBegin;
    Calendar minuteEnd;
    if (pointer == Pointer.PointerType.FUTURE) {
      minuteBegin = getNow();
      minuteEnd = Time.ymdhm(getNow());
    }
    else if (pointer == Pointer.PointerType.PAST) {
      minuteBegin = Time.ymdhm(getNow());
      minuteEnd = getNow();
    }
    else if (pointer == Pointer.PointerType.NONE) {
      minuteBegin = Time.ymdhm(getNow());
      minuteEnd = Time.cloneAndAdd(Time.ymdhm(getNow()), Calendar.SECOND, RepeaterMinute.MINUTE_SECONDS);
    }
    else {
      throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
    }
    return new Span(minuteBegin, minuteEnd);
  }

  @Override
  public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    // WARN: Does not use Calendar
    return span.add(direction * amount * RepeaterMinute.MINUTE_SECONDS);
  }

  @Override
  public int getWidth() {
    // WARN: Does not use Calendar
    return RepeaterMinute.MINUTE_SECONDS;
  }

  @Override
  public String toString() {
    return super.toString() + "-minute";
  }
}
