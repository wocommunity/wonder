package er.chronic.handlers;

import java.util.Calendar;
import java.util.List;

import er.chronic.Options;
import er.chronic.tags.ScalarYear;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public class SyHandler implements IHandler {

  public Span handle(List<Token> tokens, Options options) {
    int year = tokens.get(0).getTag(ScalarYear.class).getType().intValue();

    Span span;
    try {
      Calendar dayStart = Time.construct(year, 1, 1);
      List<Token> timeTokens = tokens.subList(1, tokens.size());
      span = Handler.dayOrTime(dayStart, timeTokens, options);
      // make the year span last a year rather than a day
      if (!options.isGuess()) {
        Calendar beginCalendar = span.getBeginCalendar();
        span = new Span(beginCalendar, Time.cloneAndAdd(beginCalendar, Calendar.YEAR, 1));
      }
    } catch (IllegalArgumentException e) {
      if (options.isDebug()) {
        e.printStackTrace(System.out);
      }
      span = null;
    }
    return span;
  }

}
