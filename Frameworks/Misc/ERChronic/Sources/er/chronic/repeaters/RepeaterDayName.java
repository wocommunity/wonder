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

public class RepeaterDayName extends Repeater<RepeaterDayName.DayName> {
  private static final Pattern MON_PATTERN = Pattern.compile("^m[ou]n(day)?$");
  private static final Pattern TUE_PATTERN = Pattern.compile("^t(ue|eu|oo|u|)s(day)?$");
  private static final Pattern TUE_PATTERN_1 = Pattern.compile("^tue$");
  private static final Pattern WED_PATTERN_1 = Pattern.compile("^we(dnes|nds|nns)day$");
  private static final Pattern WED_PATTERN_2 = Pattern.compile("^wed$");
  private static final Pattern THU_PATTERN_1 = Pattern.compile("^th(urs|ers)day$");
  private static final Pattern THU_PATTERN_2 = Pattern.compile("^thu$");
  private static final Pattern FRI_PATTERN = Pattern.compile("^fr[iy](day)?$");
  private static final Pattern SAT_PATTERN = Pattern.compile("^sat(t?[ue]rday)?$");
  private static final Pattern SUN_PATTERN = Pattern.compile("^su[nm](day)?$");

  public static final int DAY_SECONDS = 86400; // (24 * 60 * 60);

  public static enum DayName {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
  }

  private Calendar _currentDayStart;

  public RepeaterDayName(RepeaterDayName.DayName type) {
    super(type);
  }

  @Override
  protected Span _nextSpan(PointerType pointer) {
    int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
    if (_currentDayStart == null) {
      _currentDayStart = Time.ymd(getNow());
      _currentDayStart.add(Calendar.DAY_OF_MONTH, direction);

      int dayNum = getType().ordinal();

      while ((_currentDayStart.get(Calendar.DAY_OF_WEEK) - 1) != dayNum) {
        _currentDayStart.add(Calendar.DAY_OF_MONTH, direction);
      }
    }
    else {
      _currentDayStart.add(Calendar.DAY_OF_MONTH, direction * 7);
    }
    return new Span(_currentDayStart, Calendar.DAY_OF_MONTH, 1);
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    if (pointer == PointerType.NONE) {
      pointer = PointerType.FUTURE;
    }
    return super.nextSpan(pointer);
  }

  @Override
  public Span getOffset(Span span, float amount, PointerType pointer) {
    throw new IllegalStateException("Not implemented.");
  }

  @Override
  public int getWidth() {
    // WARN: Does not use Calendar
    return RepeaterDayName.DAY_SECONDS;
  }

  @Override
  public String toString() {
    return super.toString() + "-dayname-" + getType();
  }

  public static RepeaterDayName scan(Token token) {
    Map<Pattern, RepeaterDayName.DayName> scanner = new HashMap<>();
    scanner.put(RepeaterDayName.MON_PATTERN, RepeaterDayName.DayName.MONDAY);
    scanner.put(RepeaterDayName.TUE_PATTERN, RepeaterDayName.DayName.TUESDAY);
    scanner.put(RepeaterDayName.TUE_PATTERN_1, RepeaterDayName.DayName.TUESDAY);
    scanner.put(RepeaterDayName.WED_PATTERN_1, RepeaterDayName.DayName.WEDNESDAY);
    scanner.put(RepeaterDayName.WED_PATTERN_2, RepeaterDayName.DayName.WEDNESDAY);
    scanner.put(RepeaterDayName.THU_PATTERN_1, RepeaterDayName.DayName.THURSDAY);
    scanner.put(RepeaterDayName.THU_PATTERN_2, RepeaterDayName.DayName.THURSDAY);
    scanner.put(RepeaterDayName.FRI_PATTERN, RepeaterDayName.DayName.FRIDAY);
    scanner.put(RepeaterDayName.SAT_PATTERN, RepeaterDayName.DayName.SATURDAY);
    scanner.put(RepeaterDayName.SUN_PATTERN, RepeaterDayName.DayName.SUNDAY);
    for (Pattern scannerItem : scanner.keySet()) {
      if (scannerItem.matcher(token.getWord()).matches()) {
        return new RepeaterDayName(scanner.get(scannerItem));
      }
    }
    return null;
  }

}
