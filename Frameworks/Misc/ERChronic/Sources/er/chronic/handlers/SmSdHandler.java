package er.chronic.handlers;

import java.util.Calendar;
import java.util.List;

import er.chronic.Options;
import er.chronic.tags.ScalarDay;
import er.chronic.tags.ScalarMonth;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public class SmSdHandler implements IHandler {
  public Span handle(List<Token> tokens, Options options) {
    int month = tokens.get(0).getTag(ScalarMonth.class).getType().intValue();
    int day = tokens.get(1).getTag(ScalarDay.class).getType().intValue();

    // MS: properly parse time in this format
    Span span = Handler.parseTime(tokens, 2, options.getNow().get(Calendar.YEAR), month, day, options);
    return span;
  }

}
