package er.chronic.handlers;

import java.util.List;

import er.chronic.Options;
import er.chronic.utils.Span;
import er.chronic.utils.Token;

public class SRPAHandler extends SRPHandler {

  @Override
  public Span handle(List<Token> tokens, Options options) {
    Span anchorSpan = Handler.getAnchor(tokens.subList(3, tokens.size()), options);
    return super.handle(tokens, anchorSpan, options);
  }

}
