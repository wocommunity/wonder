package er.chronic.repeaters;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import er.chronic.utils.Token;

public abstract class RepeaterUnit extends Repeater<Object> {
  private static final Pattern YEAR_PATTERN = Pattern.compile("^(year|yr)s?$");
  private static final Pattern SEASON_PATTERN = Pattern.compile("^seasons?$");
  private static final Pattern MONTH_PATTERN = Pattern.compile("^(month|mon)s?$");
  private static final Pattern FORTNIGHT_PATTERN = Pattern.compile("^fortnights?$");
  private static final Pattern WEEK_PATTERN = Pattern.compile("^(week|wk)s?$");
  private static final Pattern WEEKEND_PATTERN = Pattern.compile("^weekends?$");
  private static final Pattern WEEKDAY_PATTERN = Pattern.compile("^(week|business)days?$");
  private static final Pattern DAY_PATTERN = Pattern.compile("^days?$");
  private static final Pattern HOUR_PATTERN = Pattern.compile("^(hour|hr)s?$");
  private static final Pattern MINUTE_PATTERN = Pattern.compile("^(minute|min)s?$");
  private static final Pattern SECOND_PATTERN = Pattern.compile("^(second|sec)s?$");

  public static enum UnitName {
    YEAR, SEASON, MONTH, FORTNIGHT, WEEK, WEEKEND, WEEKDAY, DAY, HOUR, MINUTE, SECOND
  }
  
  public RepeaterUnit() {
    super(null);
  }

  public static RepeaterUnit scan(Token token) {
    try {
      Map<Pattern, RepeaterUnit.UnitName> scanner = new HashMap<>();
      scanner.put(RepeaterUnit.YEAR_PATTERN, RepeaterUnit.UnitName.YEAR);
      scanner.put(RepeaterUnit.SEASON_PATTERN, RepeaterUnit.UnitName.SEASON);
      scanner.put(RepeaterUnit.MONTH_PATTERN, RepeaterUnit.UnitName.MONTH);
      scanner.put(RepeaterUnit.FORTNIGHT_PATTERN, RepeaterUnit.UnitName.FORTNIGHT);
      scanner.put(RepeaterUnit.WEEK_PATTERN, RepeaterUnit.UnitName.WEEK);
      scanner.put(RepeaterUnit.WEEKEND_PATTERN, RepeaterUnit.UnitName.WEEKEND);
      scanner.put(RepeaterUnit.WEEKDAY_PATTERN, RepeaterUnit.UnitName.WEEKDAY);
      scanner.put(RepeaterUnit.DAY_PATTERN, RepeaterUnit.UnitName.DAY);
      scanner.put(RepeaterUnit.HOUR_PATTERN, RepeaterUnit.UnitName.HOUR);
      scanner.put(RepeaterUnit.MINUTE_PATTERN, RepeaterUnit.UnitName.MINUTE);
      scanner.put(RepeaterUnit.SECOND_PATTERN, RepeaterUnit.UnitName.SECOND);
      for (Pattern scannerItem : scanner.keySet()) {
        if (scannerItem.matcher(token.getWord()).matches()) {
          RepeaterUnit.UnitName unitNameEnum = scanner.get(scannerItem);
          String unitName = unitNameEnum.name();
          String capitalizedUnitName = unitName.substring(0, 1) + unitName.substring(1).toLowerCase();
          String repeaterClassName = "er.chronic.repeaters.Repeater" + capitalizedUnitName;
          RepeaterUnit repeater = Class.forName(repeaterClassName).asSubclass(RepeaterUnit.class).newInstance();
          return repeater;
        }
      }
      return null;
    }
    catch (Throwable t) {
      throw new RuntimeException("Failed to create RepeaterUnit.", t);
    }
  }
}
