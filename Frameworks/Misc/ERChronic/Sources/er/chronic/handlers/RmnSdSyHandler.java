package er.chronic.handlers;

import java.util.Calendar;
import java.util.List;

import er.chronic.Options;
import er.chronic.repeaters.RepeaterMonthName;
import er.chronic.tags.ScalarDay;
import er.chronic.tags.ScalarYear;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public class RmnSdSyHandler implements IHandler {

  public Span handle(List<Token> tokens, Options options) {
    int month = tokens.get(0).getTag(RepeaterMonthName.class).getType().ordinal();
    int day = tokens.get(1).getTag(ScalarDay.class).getType().intValue();
    int year = tokens.get(2).getTag(ScalarYear.class).getType().intValue();

    Span span;
    try {
      List<Token> timeTokens = tokens.subList(3, tokens.size());
      Calendar dayStart = Time.construct(year, month, day);
      span = Handler.dayOrTime(dayStart, timeTokens, options);
    }
    catch (IllegalArgumentException e) {
      if (options.isDebug()) {
        e.printStackTrace(System.out);
      }
      span = null;
    }
    return span;
  }

}
