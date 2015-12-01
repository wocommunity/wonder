package er.chronic.handlers;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import er.chronic.Chronic;
import er.chronic.Options;
import er.chronic.repeaters.Repeater;
import er.chronic.repeaters.RepeaterDay;
import er.chronic.repeaters.RepeaterFortnight;
import er.chronic.repeaters.RepeaterHour;
import er.chronic.repeaters.RepeaterMinute;
import er.chronic.repeaters.RepeaterMonth;
import er.chronic.repeaters.RepeaterWeek;
import er.chronic.repeaters.RepeaterWeekend;
import er.chronic.repeaters.RepeaterYear;
import er.chronic.tags.Pointer;
import er.chronic.tags.Scalar;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public class SRPHandler implements IHandler {

  public Span handle(List<Token> tokens, Span span, Options options) {
    float distance = tokens.get(0).getTag(Scalar.class).getType().floatValue();
    Repeater<?> repeater = tokens.get(1).getTag(Repeater.class);
    Pointer.PointerType pointer = tokens.get(2).getTag(Pointer.class).getType();
    Span offsetSpan = repeater.getOffset(span, distance, pointer);
    // Addition by Oliver Kohll
    // When not guessing a point time, return a span of the correct length
    if (!options.isGuess()) {
      Calendar beginCalendar = offsetSpan.getBeginCalendar();
      int calendarField = Calendar.SECOND;
      int spanLength = 1;
      switch (repeater.getWidth()) {
      case RepeaterMinute.MINUTE_SECONDS:
        calendarField = Calendar.MINUTE;
        break;
      case RepeaterHour.HOUR_SECONDS:
        calendarField = Calendar.HOUR;
        break;
      case RepeaterDay.DAY_SECONDS: // Also = RepeaterDayName.DAY_SECONDS
        // and RepeaterWeekday.DAY_SECONDS
        calendarField = Calendar.DAY_OF_MONTH;
        break;
      case RepeaterWeekend.WEEKEND_SECONDS:
        calendarField = Calendar.DAY_OF_MONTH;
        spanLength = 2;
        break;
      case RepeaterWeek.WEEK_SECONDS:
        calendarField = Calendar.DAY_OF_MONTH;
        // WEEK_OF_YEAR doesn't seem to be supported by DateUtils.truncate
        // Round to the beginning of the week manually
        int dayOfWeek = beginCalendar.get(Calendar.DAY_OF_WEEK);
        beginCalendar.add(Calendar.DAY_OF_WEEK, -1 * (dayOfWeek - beginCalendar.getFirstDayOfWeek()));
        spanLength = 7;
        break;
      case RepeaterFortnight.FORTNIGHT_SECONDS:
        calendarField = Calendar.DAY_OF_MONTH;
        dayOfWeek = beginCalendar.get(Calendar.DAY_OF_WEEK);
        beginCalendar.add(Calendar.DAY_OF_WEEK, -1 * (dayOfWeek - beginCalendar.getFirstDayOfWeek()));
        spanLength = 14;
        break;
      case RepeaterMonth.MONTH_SECONDS:
        calendarField = Calendar.MONTH;
        break;
      case RepeaterYear.YEAR_SECONDS:
        calendarField = Calendar.YEAR;
      }
      beginCalendar = DateUtils.truncate(beginCalendar, calendarField);
      offsetSpan = new Span(beginCalendar, Time.cloneAndAdd(beginCalendar, calendarField, spanLength));
    }
    return offsetSpan;
  }

  public Span handle(List<Token> tokens, Options options) {
    Repeater<?> repeater = tokens.get(1).getTag(Repeater.class);
    // DIFF: Missing fortnight
    /*
     * Span span; if (repeater instanceof RepeaterYear || repeater
     * instanceof RepeaterSeason || repeater instanceof RepeaterSeasonName
     * || repeater instanceof RepeaterMonth || repeater instanceof
     * RepeaterMonthName || repeater instanceof RepeaterWeek) { span =
     * chronic.parse("this hour", new Options(chronic.getNow(), false)); }
     * else if (repeater instanceof RepeaterWeekend || repeater instanceof
     * RepeaterDay || repeater instanceof RepeaterDayName || repeater
     * instanceof RepeaterDayPortion || repeater instanceof RepeaterHour) {
     * span = chronic.parse("this minute", new Options(chronic.getNow(),
     * false)); } else if (repeater instanceof RepeaterMinute || repeater
     * instanceof RepeaterSecond) { span = chronic.parse("this second", new
     * Options(chronic.getNow(), false)); } else { throw new
     * IllegalArgumentException("Invalid repeater: " + repeater); }
     */
    Span span = Chronic.parse("this second", new Options(options.getNow(), false));
    return handle(tokens, span, options);
  }
}
