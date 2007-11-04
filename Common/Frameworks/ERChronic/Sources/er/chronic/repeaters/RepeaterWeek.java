package er.chronic.repeaters;

import java.util.Calendar;

import er.chronic.tags.Pointer;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Span;
import er.chronic.utils.Time;

public class RepeaterWeek extends RepeaterUnit {
  public static final int WEEK_SECONDS = 604800; // (7 * 24 * 60 * 60);
  public static final int WEEK_DAYS = 7;

  private Calendar _currentWeekStart;

  @Override
  protected Span _nextSpan(PointerType pointer) {
    if (_currentWeekStart == null) {
      if (pointer == PointerType.FUTURE) {
        RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
        sundayRepeater.setStart((Calendar) getNow().clone());
        Span nextSundaySpan = sundayRepeater.nextSpan(Pointer.PointerType.FUTURE);
        _currentWeekStart = nextSundaySpan.getBeginCalendar();
      }
      else if (pointer == PointerType.PAST) {
        RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
        sundayRepeater.setStart(Time.cloneAndAdd(getNow(), Calendar.DAY_OF_MONTH, 1));
        sundayRepeater.nextSpan(Pointer.PointerType.PAST);
        Span lastSundaySpan = sundayRepeater.nextSpan(Pointer.PointerType.PAST);
        _currentWeekStart = lastSundaySpan.getBeginCalendar();
      }
      else {
        throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
      }
    }
    else {
      int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
      _currentWeekStart.add(Calendar.DAY_OF_MONTH, RepeaterWeek.WEEK_DAYS * direction);
    }

    return new Span(_currentWeekStart, Calendar.DAY_OF_MONTH, RepeaterWeek.WEEK_DAYS);
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    Span thisWeekSpan;
    Calendar thisWeekStart;
    Calendar thisWeekEnd;
    if (pointer == PointerType.FUTURE) {
      thisWeekStart = Time.cloneAndAdd(Time.ymdh(getNow()), Calendar.HOUR, 1);
      RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
      sundayRepeater.setStart((Calendar) getNow().clone());
      Span thisSundaySpan = sundayRepeater.thisSpan(Pointer.PointerType.FUTURE);
      thisWeekEnd = thisSundaySpan.getBeginCalendar();
      thisWeekSpan = new Span(thisWeekStart, thisWeekEnd);
    }
    else if (pointer == PointerType.PAST) {
      thisWeekEnd = Time.ymdh(getNow());
      RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
      sundayRepeater.setStart((Calendar) getNow().clone());
      Span lastSundaySpan = sundayRepeater.nextSpan(Pointer.PointerType.PAST);
      thisWeekStart = lastSundaySpan.getBeginCalendar();
      thisWeekSpan = new Span(thisWeekStart, thisWeekEnd);
    }
    else if (pointer == PointerType.NONE) {
      RepeaterDayName sundayRepeater = new RepeaterDayName(RepeaterDayName.DayName.SUNDAY);
      sundayRepeater.setStart((Calendar) getNow().clone());
      Span lastSundaySpan = sundayRepeater.nextSpan(Pointer.PointerType.PAST);
      thisWeekStart = lastSundaySpan.getBeginCalendar();
      thisWeekEnd = Time.cloneAndAdd(thisWeekStart, Calendar.DAY_OF_MONTH, RepeaterWeek.WEEK_DAYS);
      thisWeekSpan = new Span(thisWeekStart, thisWeekEnd);
    }
    else {
      throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
    }
    return thisWeekSpan;
  }

  @Override
  public Span getOffset(Span span, int amount, Pointer.PointerType pointer) {
    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    // WARN: Does not use Calendar
    return span.add(direction * amount * RepeaterWeek.WEEK_SECONDS);
  }

  @Override
  public int getWidth() {
    // WARN: Does not use Calendar
    return RepeaterWeek.WEEK_SECONDS;
  }

  @Override
  public String toString() {
    return super.toString() + "-week";
  }
}
