package er.chronic.repeaters;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import er.chronic.tags.Pointer;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public class RepeaterMonthName extends Repeater<RepeaterMonthName.MonthName> {
  private static final Pattern JAN_PATTERN = Pattern.compile("^jan\\.?(uary)?$");
  private static final Pattern FEB_PATTERN = Pattern.compile("^feb\\.?(ruary)?$");
  private static final Pattern MAR_PATTERN = Pattern.compile("^mar\\.?(ch)?$");
  private static final Pattern APR_PATTERN = Pattern.compile("^apr\\.?(il)?$");
  private static final Pattern MAY_PATTERN = Pattern.compile("^may$");
  private static final Pattern JUN_PATTERN = Pattern.compile("^jun\\.?e?$");
  private static final Pattern JUL_PATTERN = Pattern.compile("^jul\\.?y?$");
  private static final Pattern AUG_PATTERN = Pattern.compile("^aug\\.?(ust)?$");
  private static final Pattern SEP_PATTERN = Pattern.compile("^sep\\.?(t\\.?|tember)?$");
  private static final Pattern OCT_PATTERN = Pattern.compile("^oct\\.?(ober)?$");
  private static final Pattern NOV_PATTERN = Pattern.compile("^nov\\.?(ember)?$");
  private static final Pattern DEC_PATTERN = Pattern.compile("^dec\\.?(ember)?$");

  private static final int MONTH_SECONDS = 2592000; // 30 * 24 * 60 * 60

  public static enum MonthName {
    _ZERO_MONTH, JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER
  }

  private Calendar _currentMonthBegin;

  public RepeaterMonthName(MonthName type) {
    super(type);
  }
  
  public int getIndex() {
    return getType().ordinal();
  }

  @Override
  protected Span _nextSpan(PointerType pointer) {
    if (_currentMonthBegin == null) {
      int targetMonth = getType().ordinal();
      int nowMonth = getNow().get(Calendar.MONTH) + 1;
      if (pointer == PointerType.FUTURE) {
        if (nowMonth < targetMonth) {
          _currentMonthBegin = Time.y(getNow(), targetMonth);
        }
        else if (nowMonth > targetMonth) {
          _currentMonthBegin = Time.cloneAndAdd(Time.y(getNow(), targetMonth), Calendar.YEAR, 1);
        }
      }
      else if (pointer == PointerType.NONE) {
        if (nowMonth <= targetMonth) {
          _currentMonthBegin = Time.y(getNow(), targetMonth);
        }
        else if (nowMonth > targetMonth) {
          _currentMonthBegin = Time.cloneAndAdd(Time.y(getNow(), targetMonth), Calendar.YEAR, 1);
        }
      }
      else if (pointer == PointerType.PAST) {
        if (nowMonth > targetMonth) {
          _currentMonthBegin = Time.y(getNow(), targetMonth);
        }
        else if (nowMonth < targetMonth) {
          _currentMonthBegin = Time.cloneAndAdd(Time.y(getNow(), targetMonth), Calendar.YEAR, -1);
        }
      }
      else {
        throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
      }
      if (_currentMonthBegin == null) {
        throw new IllegalStateException("Current month should be set by now.");
      }
    }
    else {
      if (pointer == PointerType.FUTURE) {
        _currentMonthBegin = Time.cloneAndAdd(_currentMonthBegin, Calendar.YEAR, 1);
      }
      else if (pointer == PointerType.PAST) {
        _currentMonthBegin = Time.cloneAndAdd(_currentMonthBegin, Calendar.YEAR, -1);
      }
      else {
        throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
      }
    }

    return new Span(_currentMonthBegin, Calendar.MONTH, 1);
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    Span span;
    if (pointer == Pointer.PointerType.PAST) {
      span = nextSpan(pointer);
    }
    else if (pointer == Pointer.PointerType.FUTURE || pointer == Pointer.PointerType.NONE) {
      span = nextSpan(Pointer.PointerType.NONE);
    }
    else {
      throw new IllegalArgumentException("Unable to handle pointer " + pointer + ".");
    }
    return span;
  }

  @Override
  public Span getOffset(Span span, float amount, PointerType pointer) {
    throw new IllegalStateException("Not implemented.");
  }

  @Override
  public int getWidth() {
    // WARN: Does not use Calendar
    return RepeaterMonthName.MONTH_SECONDS;
  }

  @Override
  public String toString() {
    return super.toString() + "-monthname-" + getType();
  }

  public static RepeaterMonthName scan(Token token) {
    Map<Pattern, RepeaterMonthName.MonthName> scanner = new HashMap<>();
    scanner.put(RepeaterMonthName.JAN_PATTERN, RepeaterMonthName.MonthName.JANUARY);
    scanner.put(RepeaterMonthName.FEB_PATTERN, RepeaterMonthName.MonthName.FEBRUARY);
    scanner.put(RepeaterMonthName.MAR_PATTERN, RepeaterMonthName.MonthName.MARCH);
    scanner.put(RepeaterMonthName.APR_PATTERN, RepeaterMonthName.MonthName.APRIL);
    scanner.put(RepeaterMonthName.MAY_PATTERN, RepeaterMonthName.MonthName.MAY);
    scanner.put(RepeaterMonthName.JUN_PATTERN, RepeaterMonthName.MonthName.JUNE);
    scanner.put(RepeaterMonthName.JUL_PATTERN, RepeaterMonthName.MonthName.JULY);
    scanner.put(RepeaterMonthName.AUG_PATTERN, RepeaterMonthName.MonthName.AUGUST);
    scanner.put(RepeaterMonthName.SEP_PATTERN, RepeaterMonthName.MonthName.SEPTEMBER);
    scanner.put(RepeaterMonthName.OCT_PATTERN, RepeaterMonthName.MonthName.OCTOBER);
    scanner.put(RepeaterMonthName.NOV_PATTERN, RepeaterMonthName.MonthName.NOVEMBER);
    scanner.put(RepeaterMonthName.DEC_PATTERN, RepeaterMonthName.MonthName.DECEMBER);
    for (Pattern scannerItem : scanner.keySet()) {
      if (scannerItem.matcher(token.getWord()).matches()) {
        return new RepeaterMonthName(scanner.get(scannerItem));
      }
    }
    return null;
  }

}
