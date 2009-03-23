package er.chronic.handlers;

import java.util.Calendar;
import java.util.List;

import er.chronic.Options;
import er.chronic.tags.ScalarDay;
import er.chronic.tags.ScalarMonth;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public class SmSdHandler implements IHandler {
  public Span handle(List<Token> tokens, Options options) {
    int month = tokens.get(0).getTag(ScalarMonth.class).getType().intValue();
    int day = tokens.get(1).getTag(ScalarDay.class).getType().intValue();

    // MS: properly parse time in this format
    Span span;
    try {
      List<Token> timeTokens = tokens.subList(2, tokens.size());
      Calendar dayStart = Time.construct(options.getNow().get(Calendar.YEAR), month, day);
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
