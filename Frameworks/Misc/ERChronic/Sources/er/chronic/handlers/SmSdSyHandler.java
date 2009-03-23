package er.chronic.handlers;

import java.util.List;

import er.chronic.Options;
import er.chronic.tags.ScalarDay;
import er.chronic.tags.ScalarMonth;
import er.chronic.tags.ScalarYear;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public class SmSdSyHandler implements IHandler {

  public Span handle(List<Token> tokens, Options options) {
    int month = tokens.get(0).getTag(ScalarMonth.class).getType().intValue();
    int day = tokens.get(1).getTag(ScalarDay.class).getType().intValue();
    int year = tokens.get(2).getTag(ScalarYear.class).getType().intValue();
    Span span = Handler.parseTime(tokens, 3, year, month, day, options);
    return span;
  }

}
