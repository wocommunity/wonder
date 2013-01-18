package er.chronic.repeaters;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import er.chronic.Options;
import er.chronic.tags.Pointer;
import er.chronic.tags.Pointer.PointerType;
import er.chronic.tags.Scalar;
import er.chronic.utils.Span;
import er.chronic.utils.StringUtils;
import er.chronic.utils.Tick;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public class RepeaterTime extends Repeater<Tick> {
  private static final Pattern TIME_PATTERN = Pattern.compile("^\\d{1,2}(:?\\d{2})?([\\.:]?\\d{2})?$");

  public RepeaterTime(String time) {
    super(null);
    String t = time.replaceAll(":", "");
    Tick type;
    int length = t.length();
    if (length <= 2) {
      int hours = Integer.parseInt(t);
      int hoursInSeconds = hours * 60 * 60;
      if (hours == 12) {
        type = new Tick(0 * 60 * 60, true);
      }
      else {
        type = new Tick(hoursInSeconds, true);
      }
    }
    else if (length == 3) {
      int hoursInSeconds = Integer.parseInt(t.substring(0, 1)) * 60 * 60;
      int minutesInSeconds = Integer.parseInt(t.substring(1)) * 60;
      type = new Tick(hoursInSeconds + minutesInSeconds, true);
    }
    else if (length == 4) {
      // DIFF: we need to support 05:45 vs 5:45
      //boolean ambiguous = (time.contains(":") && Integer.parseInt(t.substring(0, 1) != 0 && Integer.parseInt(t.substring(0, 2)) <= 12);
      boolean ambiguous = (time.contains(":") && Integer.parseInt(t.substring(0, 2)) <= 12);
      int hours = Integer.parseInt(t.substring(0, 2));
      int hoursInSeconds = hours * 60 * 60;
      int minutesInSeconds = Integer.parseInt(t.substring(2)) * 60;
      if (hours == 12) {
        type = new Tick(0 * 60 * 60 + minutesInSeconds, ambiguous);
      }
      else {
        type = new Tick(hoursInSeconds + minutesInSeconds, ambiguous);
      }
    }
    else if (length == 5) {
      int hoursInSeconds = Integer.parseInt(t.substring(0, 1)) * 60 * 60;
      int minutesInSeconds = Integer.parseInt(t.substring(1, 3)) * 60;
      int seconds = Integer.parseInt(t.substring(3));
      type = new Tick(hoursInSeconds + minutesInSeconds + seconds, true);
    }
    else if (length == 6) {
      // DIFF: we need to support 05:45 vs 5:45
      //boolean ambiguous = (time.contains(":") && Integer.parseInt(t.substring(0, 1)) != 0 && Integer.parseInt(t.substring(0, 2)) <= 12);
      boolean ambiguous = (time.contains(":") && Integer.parseInt(t.substring(0, 2)) <= 12);
      int hours = Integer.parseInt(t.substring(0, 2));
      int hoursInSeconds = hours * 60 * 60;
      int minutesInSeconds = Integer.parseInt(t.substring(2, 4)) * 60;
      int seconds = Integer.parseInt(t.substring(4, 6));
      //type = new Tick(hoursInSeconds + minutesInSeconds + seconds, ambiguous);
      if (hours == 12) {
        type = new Tick(0 * 60 * 60 + minutesInSeconds + seconds, ambiguous);
      }
      else {
        type = new Tick(hoursInSeconds + minutesInSeconds + seconds, ambiguous);
      }
    }
    else {
      throw new IllegalArgumentException("Time cannot exceed six digits");
    }
    setType(type);
  }

  private Calendar _currentTime;

  @Override
  protected Span _nextSpan(PointerType pointer) {
    int halfDay = RepeaterDay.DAY_SECONDS / 2;
    int fullDay = RepeaterDay.DAY_SECONDS;

    Calendar now = getNow();
    Tick tick = getType();
    boolean first = false;
    if (_currentTime == null) {
      first = true;
      Calendar midnight = Time.ymd(now);
      Calendar yesterdayMidnight = Time.cloneAndAdd(midnight, Calendar.SECOND, -fullDay);
      Calendar tomorrowMidnight = Time.cloneAndAdd(midnight, Calendar.SECOND, fullDay);
      
      int offsetFix = (midnight.get(Calendar.ZONE_OFFSET) - tomorrowMidnight.get(Calendar.ZONE_OFFSET)) / 1000;
      tomorrowMidnight.add(Calendar.SECOND, offsetFix);

      boolean done = false;
      if (pointer == Pointer.PointerType.FUTURE) {
        if (tick.isAmbiguous()) {
          List<Calendar> futureDates = new LinkedList<Calendar>();
          futureDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, tick.intValue() + offsetFix));
          futureDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, halfDay + tick.intValue() + offsetFix));
          futureDates.add(Time.cloneAndAdd(tomorrowMidnight, Calendar.SECOND, tick.intValue()));
          for (Calendar futureDate : futureDates) {
            if (futureDate.after(now) || futureDate.equals(now)) {
              _currentTime = futureDate;
              done = true;
              break;
            }
          }
        }
        else {
          List<Calendar> futureDates = new LinkedList<Calendar>();
          futureDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, tick.intValue() + offsetFix));
          futureDates.add(Time.cloneAndAdd(tomorrowMidnight, Calendar.SECOND, tick.intValue()));
          for (Calendar futureDate : futureDates) {
            if (futureDate.after(now) || futureDate.equals(now)) {
              _currentTime = futureDate;
              done = true;
              break;
            }
          }
        }
      }
      else {
        if (tick.isAmbiguous()) {
          List<Calendar> pastDates = new LinkedList<Calendar>();
          pastDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, halfDay + tick.intValue() + offsetFix));
          pastDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, tick.intValue() + offsetFix));
          pastDates.add(Time.cloneAndAdd(yesterdayMidnight, Calendar.SECOND, tick.intValue() + halfDay));
          for (Calendar pastDate : pastDates) {
            if (pastDate.before(now) || pastDate.equals(now)) {
              _currentTime = pastDate;
              done = true;
              break;
            }
          }
        }
        else {
          List<Calendar> pastDates = new LinkedList<Calendar>();
          pastDates.add(Time.cloneAndAdd(midnight, Calendar.SECOND, tick.intValue() + offsetFix));
          pastDates.add(Time.cloneAndAdd(yesterdayMidnight, Calendar.SECOND, tick.intValue()));
          for (Calendar pastDate : pastDates) {
            if (pastDate.before(now) || pastDate.equals(now)) {
              _currentTime = pastDate;
              done = true;
              break;
            }
          }
        }
      }

      if (!done && _currentTime == null) {
        throw new IllegalStateException("Current time cannot be null at this point.");
      }
    }

    if (!first) {
      int increment = (tick.isAmbiguous()) ? halfDay : fullDay;
      int direction = (pointer == Pointer.PointerType.FUTURE) ? 1 : -1;
      _currentTime.add(Calendar.SECOND, direction * increment);
    }

    return new Span(_currentTime, Time.cloneAndAdd(_currentTime, Calendar.SECOND, getWidth()));
  }

  @Override
  protected Span _thisSpan(PointerType pointer) {
    if (pointer == Pointer.PointerType.NONE) {
      pointer = Pointer.PointerType.FUTURE;
    }
    return nextSpan(pointer);
  }

  @Override
  public Span getOffset(Span span, float amount, PointerType pointer) {
    throw new IllegalStateException("Not implemented.");
  }

  @Override
  public int getWidth() {
    return 1;
  }

  @Override
  public String toString() {
    return super.toString() + "-time-" + getType();
  }

  public static RepeaterTime scan(Token token, List<Token> tokens, Options options) {
    // MS: added an exclusion so 3.25 doesn't match a time ...
    if (RepeaterTime.TIME_PATTERN.matcher(token.getWord()).matches() && !Scalar.FRACTIONAL_SCALAR_PATTERN.matcher(token.getWord()).matches()) {
    // if (RepeaterTime.TIME_PATTERN.matcher(token.getWord()).matches()) {
      return new RepeaterTime(token.getWord());
    }
    Integer intStrValue = StringUtils.integerValue(token.getWord());
    if (intStrValue != null) {
      return new RepeaterTime(intStrValue.toString());
    }
    return null;
  }
}
