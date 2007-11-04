package er.chronic.repeaters;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import er.chronic.tags.Pointer;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Range;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public abstract class RepeaterDayPortion<T> extends Repeater<T> {
  private static final Pattern AM_PATTERN = Pattern.compile("^ams?$");
  private static final Pattern PM_PATTERN = Pattern.compile("^pms?$");
  private static final Pattern MORNING_PATTERN = Pattern.compile("^mornings?$");
  private static final Pattern AFTERNOON_PATTERN = Pattern.compile("^afternoons?$");
  private static final Pattern EVENING_PATTERN = Pattern.compile("^evenings?$");
  private static final Pattern NIGHT_PATTERN = Pattern.compile("^(night|nite)s?$");

  private static final int FULL_DAY_SECONDS = 60 * 60 * 24;

  public static enum DayPortion {
    AM, PM, MORNING, AFTERNOON, EVENING, NIGHT
  }

  private Range _range;
  private Span _currentSpan;

  public RepeaterDayPortion(T type) {
    super(type);
    _range = createRange(type);
  }

  @Override
  protected Span _nextSpan(PointerType pointer) {
    Calendar rangeStart;
    Calendar rangeEnd;
    if (_currentSpan == null) {
      long nowSeconds = (getNow().getTimeInMillis() - Time.ymd(getNow()).getTimeInMillis()) / 1000;
      if (nowSeconds < _range.getBegin()) {
        if (pointer == Pointer.PointerType.FUTURE) {
          rangeStart = Time.cloneAndAdd(Time.ymd(getNow()), Calendar.SECOND, _range.getBegin());
        }
        else if (pointer == Pointer.PointerType.PAST) {
          rangeStart = Time.cloneAndAdd(Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, -1), Calendar.SECOND, _range.getBegin());
        }
        else {
          throw new IllegalArgumentException("Unable to handle pointer type " + pointer);
        }
      }
      else if (nowSeconds > _range.getBegin()) {
        if (pointer == Pointer.PointerType.FUTURE) {
          rangeStart = Time.cloneAndAdd(Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, 1), Calendar.SECOND, _range.getBegin());
        }
        else if (pointer == Pointer.PointerType.PAST) {
          rangeStart = Time.cloneAndAdd(Time.ymd(getNow()), Calendar.SECOND, _range.getBegin());
        }
        else {
          throw new IllegalArgumentException("Unable to handle pointer type " + pointer);
        }
      }
      else {
        if (pointer == Pointer.PointerType.FUTURE) {
          rangeStart = Time.cloneAndAdd(Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, 1), Calendar.SECOND, _range.getBegin());
        }
        else if (pointer == Pointer.PointerType.PAST) {
          rangeStart = Time.cloneAndAdd(Time.cloneAndAdd(Time.ymd(getNow()), Calendar.DAY_OF_MONTH, -1), Calendar.SECOND, _range.getBegin());
        }
        else {
          throw new IllegalArgumentException("Unable to handle pointer type " + pointer);
        }
      }

      _currentSpan = new Span(rangeStart, Time.cloneAndAdd(rangeStart, Calendar.SECOND, _range.getWidth()));
    }
    else {
      if (pointer == Pointer.PointerType.FUTURE) {
        // WARN: Does not use Calendar
        _currentSpan = _currentSpan.add(RepeaterDayPortion.FULL_DAY_SECONDS);
      }
      else if (pointer == Pointer.PointerType.PAST) {
        // WARN: Does not use Calendar
        _currentSpan = _currentSpan.subtract(RepeaterDayPortion.FULL_DAY_SECONDS);
      }
      else {
        throw new IllegalArgumentException("Unable to handle pointer type " + pointer);
      }
    }
    return _currentSpan;
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    Calendar rangeStart = Time.cloneAndAdd(Time.ymd(getNow()), Calendar.SECOND, _range.getBegin());
    _currentSpan = new Span(rangeStart, Time.cloneAndAdd(rangeStart, Calendar.SECOND, _range.getWidth()));
    return _currentSpan;
  }

  @Override
  public Span getOffset(Span span, int amount, PointerType pointer) {
    setStart(span.getBeginCalendar());
    Span portionSpan = nextSpan(pointer);
    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    portionSpan = portionSpan.add(direction * (amount - 1) * RepeaterDay.DAY_SECONDS);
    return portionSpan;
  }

  @Override
  public int getWidth() {
    if (_range == null) {
      throw new IllegalStateException("Range has not been set");
    }
    int width;
    if (_currentSpan != null) {
      width = (int) _currentSpan.getWidth();
    }
    else {
      width = _getWidth(_range);
    }
    return width;
  }
  
  protected abstract int _getWidth(Range range);
  
  protected abstract Range createRange(T type);

  @Override
  public String toString() {
    return super.toString() + "-dayportion-" + getType();
  }

  public static EnumRepeaterDayPortion scan(Token token) {
    Map<Pattern, RepeaterDayPortion.DayPortion> scanner = new HashMap<Pattern, RepeaterDayPortion.DayPortion>();
    scanner.put(RepeaterDayPortion.AM_PATTERN, RepeaterDayPortion.DayPortion.AM);
    scanner.put(RepeaterDayPortion.PM_PATTERN, RepeaterDayPortion.DayPortion.PM);
    scanner.put(RepeaterDayPortion.MORNING_PATTERN, RepeaterDayPortion.DayPortion.MORNING);
    scanner.put(RepeaterDayPortion.AFTERNOON_PATTERN, RepeaterDayPortion.DayPortion.AFTERNOON);
    scanner.put(RepeaterDayPortion.EVENING_PATTERN, RepeaterDayPortion.DayPortion.EVENING);
    scanner.put(RepeaterDayPortion.NIGHT_PATTERN, RepeaterDayPortion.DayPortion.NIGHT);
    for (Pattern scannerItem : scanner.keySet()) {
      if (scannerItem.matcher(token.getWord()).matches()) {
        return new EnumRepeaterDayPortion(scanner.get(scannerItem));
      }
    }
    return null;
  }

}
