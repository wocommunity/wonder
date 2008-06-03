package er.chronic.handlers;

import java.util.Calendar;
import java.util.List;

import er.chronic.Options;
import er.chronic.repeaters.RepeaterMonthName;
import er.chronic.tags.ScalarYear;
import er.chronic.utils.Span;
import er.chronic.utils.Time;
import er.chronic.utils.Token;

public class RmnSyHandler implements IHandler {

  public Span handle(List<Token> tokens, Options options) {
    int month = tokens.get(0).getTag(RepeaterMonthName.class).getType().ordinal();
    int year = tokens.get(1).getTag(ScalarYear.class).getType().intValue();

    Span span;
    try {
      Calendar start = Time.construct(year, month);
      Calendar end = Time.cloneAndAdd(start, Calendar.MONTH, 1);
      span = new Span(start, end);
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
