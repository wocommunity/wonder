package er.chronic.handlers;

import java.util.List;

import er.chronic.Options;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public class RHandler implements IHandler {

  public Span handle(List<Token> tokens, Options options) {
    List<Token> ddTokens = Handler.dealiasAndDisambiguateTimes(tokens, options);
    return Handler.getAnchor(ddTokens, options);
  }

}
