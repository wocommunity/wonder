package er.chronic.repeaters;

import java.util.Calendar;

import er.chronic.tags.Pointer;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class RepeaterWeekend extends RepeaterUnit {
  public static final int WEEKEND_SECONDS = 172800; // (2 * 24 * 60 * 60);

  private Calendar _currentWeekStart;

  @Override
  protected Span _nextSpan(PointerType pointer) {
    if (_currentWeekStart == null) {
      if (pointer == Pointer.PointerType.FUTURE) {
        RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
        saturdayRepeater.setStart((Calendar) getNow().clone());
        Span nextSaturdaySpan = saturdayRepeater.nextSpan(Pointer.PointerType.FUTURE);
        _currentWeekStart = nextSaturdaySpan.getBeginCalendar();
      }
      else if (pointer == Pointer.PointerType.PAST) {
        RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
        saturdayRepeater.setStart(Time.cloneAndAdd(getNow(), Calendar.SECOND, RepeaterDay.DAY_SECONDS));
        Span lastSaturdaySpan = saturdayRepeater.nextSpan(Pointer.PointerType.PAST);
        _currentWeekStart = lastSaturdaySpan.getBeginCalendar();
      }
    }
    else {
      int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
      _currentWeekStart = Time.cloneAndAdd(_currentWeekStart, Calendar.SECOND, direction * RepeaterWeek.WEEK_SECONDS);
    }
    return new Span(_currentWeekStart, Time.cloneAndAdd(_currentWeekStart, Calendar.SECOND, RepeaterWeekend.WEEKEND_SECONDS));
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    Span thisSpan;
    if (pointer == Pointer.PointerType.FUTURE || pointer == Pointer.PointerType.NONE) {
      RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
      saturdayRepeater.setStart((Calendar) getNow().clone());
      Span thisSaturdaySpan = saturdayRepeater.nextSpan(Pointer.PointerType.FUTURE);
      thisSpan = new Span(thisSaturdaySpan.getBeginCalendar(), Time.cloneAndAdd(thisSaturdaySpan.getBeginCalendar(), Calendar.SECOND, RepeaterWeekend.WEEKEND_SECONDS));
    }
    else if (pointer == Pointer.PointerType.PAST) {
      RepeaterDayName saturdayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SATURDAY);
      saturdayRepeater.setStart((Calendar) getNow().clone());
      Span lastSaturdaySpan = saturdayRepeater.nextSpan(Pointer.PointerType.PAST);
      thisSpan = new Span(lastSaturdaySpan.getBeginCalendar(), Time.cloneAndAdd(lastSaturdaySpan.getBeginCalendar(), Calendar.SECOND, RepeaterWeekend.WEEKEND_SECONDS));
    }
    else {
      throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
    }
    return thisSpan;
  }

  @Override
  public Span getOffset(Span span, int amount, PointerType pointer) {
    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    RepeaterWeekend weekend = new RepeaterWeekend();
    weekend.setStart(span.getBeginCalendar());
    Calendar start = Time.cloneAndAdd(weekend.nextSpan(pointer).getBeginCalendar(), Calendar.SECOND, (amount - 1) * direction * RepeaterWeek.WEEK_SECONDS);
    return new Span(start, Time.cloneAndAdd(start, Calendar.SECOND, span.getWidth()));
  }

  @Override
  public int getWidth() {
    // WARN: Does not use Calendar
    return RepeaterWeekend.WEEKEND_SECONDS;
  }

  @Override
  public String toString() {
    return super.toString() + "-weekend";
  }
}
