package er.chronic.handlers;

import java.util.Calendar;
import java.util.List;

import er.chronic.Options;
import er.chronic.tags.ScalarDay;
import er.chronic.tags.ScalarMonth;
import er.chronic.utils.Time;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public class SmSdHandler implements IHandler {
  public Span handle(List<Token> tokens, Options options) {
    int month = tokens.get(0).getTag(ScalarMonth.class).getType().intValue();
    int day = tokens.get(1).getTag(ScalarDay.class).getType().intValue();
    Calendar start = Time.construct(options.getNow().get(Calendar.YEAR), month, day);
    Calendar end = Time.cloneAndAdd(start, Calendar.DAY_OF_MONTH, 1);
    return new Span(start, end);
  }

}
