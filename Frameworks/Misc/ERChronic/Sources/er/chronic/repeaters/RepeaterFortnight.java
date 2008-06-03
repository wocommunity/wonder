package er.chronic.repeaters;

import java.util.Calendar;

import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class RepeaterFortnight extends RepeaterUnit {
  public static final int FORTNIGHT_SECONDS = 1209600; // (14 * 24 * 60 * 60)

  private Calendar _currentFortnightStart;

  @Override
  protected Span _nextSpan(PointerType pointer) {
    if (_currentFortnightStart == null) {
      if (pointer == PointerType.FUTURE) {
        RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
        sundayRepeater.setStart(getNow());
        Span nextSundaySpan = sundayRepeater.nextSpan(PointerType.FUTURE);
        _currentFortnightStart = nextSundaySpan.getBeginCalendar();
      }
      else if (pointer == PointerType.PAST) {
        RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
        sundayRepeater.setStart(Time.cloneAndAdd(getNow(), Calendar.SECOND, RepeaterDay.DAY_SECONDS));
        sundayRepeater.nextSpan(PointerType.PAST);
        sundayRepeater.nextSpan(PointerType.PAST);
        Span lastSundaySpan = sundayRepeater.nextSpan(PointerType.PAST);
        _currentFortnightStart = lastSundaySpan.getBeginCalendar();
      }
      else {
        throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
      }
    }
    else {
      int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
      _currentFortnightStart.add(Calendar.SECOND, direction * RepeaterFortnight.FORTNIGHT_SECONDS);
    }

    return new Span(_currentFortnightStart, Calendar.SECOND, RepeaterFortnight.FORTNIGHT_SECONDS);
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    if (pointer == null) {
      pointer = PointerType.FUTURE;
    }

    Span span;
    if (pointer == PointerType.FUTURE) {
      Calendar thisFortnightStart = Time.cloneAndAdd(Time.ymdh(getNow()), Calendar.SECOND, RepeaterHour.HOUR_SECONDS);
      RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
      sundayRepeater.setStart(getNow());
      sundayRepeater.thisSpan(PointerType.FUTURE);
      Span thisSundaySpan = sundayRepeater.thisSpan(PointerType.FUTURE);
      Calendar thisFortnightEnd = thisSundaySpan.getBeginCalendar();
      span = new Span(thisFortnightStart, thisFortnightEnd);
    }
    else if (pointer == PointerType.PAST) {
      Calendar thisFortnightEnd = Time.ymdh(getNow());
      RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
      sundayRepeater.setStart(getNow());
      Span lastSundaySpan = sundayRepeater.nextSpan(PointerType.PAST);
      Calendar thisFortnightStart = lastSundaySpan.getBeginCalendar();
      span = new Span(thisFortnightStart, thisFortnightEnd);
    }
    else {
      throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
    }

    return span;
  }

  @Override
  public Span getOffset(Span span, int amount, PointerType pointer) {
    int direction = (pointer == PointerType.FUTURE) ? 1 : -1;
    Span offsetSpan = span.add(direction * amount * RepeaterFortnight.FORTNIGHT_SECONDS);
    return offsetSpan;
  }

  @Override
  public int getWidth() {
    return RepeaterFortnight.FORTNIGHT_SECONDS;
  }

  @Override
  public String toString() {
    return super.toString() + "-fortnight";
  }

}
