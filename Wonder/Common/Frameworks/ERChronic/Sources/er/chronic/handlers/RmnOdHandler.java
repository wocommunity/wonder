package er.chronic.handlers;

import java.util.List;

import er.chronic.Options;
import er.chronic.repeaters.RepeaterMonthName;
import er.chronic.tags.OrdinalDay;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public class RmnOdHandler extends MDHandler {
  public Span handle(List<Token> tokens, Options options) {
    return handle(tokens.get(0).getTag(RepeaterMonthName.class), tokens.get(1).getTag(OrdinalDay.class), tokens.subList(2, tokens.size()), options);
  }
}
