package er.chronic.handlers;

import java.util.List;

import er.chronic.Options;
import er.chronic.repeaters.RepeaterMonthName;
import er.chronic.tags.ScalarDay;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public class RmnSdHandler extends MDHandler {
  public Span handle(List<Token> tokens, Options options) {
    return handle(tokens.get(0).getTag(RepeaterMonthName.class), tokens.get(1).getTag(ScalarDay.class), tokens.subList(2, tokens.size()), options);
  }
}
