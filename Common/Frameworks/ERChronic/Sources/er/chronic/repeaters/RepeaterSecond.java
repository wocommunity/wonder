package er.chronic.repeaters;

import java.util.Calendar;

import er.chronic.tags.Pointer;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class RepeaterSecond extends RepeaterUnit {
  public static final int SECOND_SECONDS = 1; // (60 * 60);

  private Calendar _secondStart;

  @Override
  protected Span _nextSpan(PointerType pointer) {
    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    if (_secondStart == null) {
      _secondStart = Time.cloneAndAdd(getNow(), Calendar.SECOND, direction);
    }
    else {
      _secondStart.add(Calendar.SECOND, direction);
    }

    return new Span(_secondStart, Calendar.SECOND, 1);
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    return new Span(getNow(), Calendar.SECOND, 1);
  }

  @Override
  public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    // WARN: Does not use Calendar
    return span.add(direction * amount * RepeaterSecond.SECOND_SECONDS);
  }

  @Override
  public int getWidth() {
    // WARN: Does not use Calendar
    return RepeaterSecond.SECOND_SECONDS;
  }

  @Override
  public String toString() {
    return super.toString() + "-second";
  }
}
